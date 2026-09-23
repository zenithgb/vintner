#!/usr/bin/env python3
"""Audit canonical and runtime Vintner village vineyard structures."""

from __future__ import annotations

import gzip
import hashlib
import json
import re
from collections import Counter
from pathlib import Path

from structure_nbt import (
    TAG_COMPOUND,
    TAG_INT,
    TAG_LIST,
    TAG_STRING,
    decode_gzip,
    encode_root,
    require_list,
    require_tag,
)


ROOT = Path(__file__).resolve().parents[1]
STRUCTURE_ROOT = ROOT / "src/main/resources/data/vintner/structure/village"
CANONICAL_PLAINS_SOURCE = (
    ROOT / "structure_sources/village/plains/vineyard_tier1.nbt"
)
CANONICAL_PLAINS_SHA256 = (
    "7914e27f3bce456181324a23c459c8159ce26c1b8d227d588f99b3b8986e8c94"
)
CANONICAL_PLAINS_SIZE = (17, 11, 11)
LEGACY_INTERIM_SIZE = (15, 10, 13)
PLAINS_JIGSAW_POSITION = (9, 1, 0)
EXPECTED_DATA_VERSIONS = {
    "plains": 5023,
    "desert": 4903,
    "savanna": 4903,
    "snowy": 4903,
    "taiga": 4903,
}
CULTURES = {
    "plains": "oak",
    "desert": "acacia",
    "savanna": "acacia",
    "snowy": "spruce",
    "taiga": "spruce",
}
FORBIDDEN_BLOCKS = {
    "minecraft:barrier",
    "minecraft:chain_command_block",
    "minecraft:command_block",
    "minecraft:composter",
    "minecraft:repeating_command_block",
    "minecraft:structure_block",
    "minecraft:structure_void",
}
IDENTIFIER = re.compile(r"^[a-z0-9_.-]+:[a-z0-9_./-]+$")


def list_any(compound: dict[str, object], name: str) -> list[object]:
    _element_type, values = require_tag(compound, name, TAG_LIST)
    return values


def palette_name(entry: dict[str, object]) -> str:
    for field in ("id", "Name"):
        tag = entry.get(field)
        if tag is not None:
            if tag[0] != TAG_STRING:
                raise ValueError(f"palette {field} is not a string")
            name = str(tag[1])
            if not IDENTIFIER.fullmatch(name):
                raise ValueError(f"invalid block identifier {name!r}")
            return name
    raise ValueError("palette entry has no block identifier")


def palette_properties(entry: dict[str, object]) -> dict[str, str]:
    for field in ("properties", "Properties"):
        tag = entry.get(field)
        if tag is None:
            continue
        if tag[0] != TAG_COMPOUND:
            raise ValueError(f"palette {field} is not a compound")
        result: dict[str, str] = {}
        for key, value_tag in tag[1].items():
            if value_tag[0] != TAG_STRING:
                raise ValueError(f"block property {key!r} is not a string")
            result[key] = str(value_tag[1])
        return result
    return {}


def block_position(block: dict[str, object]) -> tuple[int, int, int]:
    position = require_list(block, "pos", TAG_INT)
    if len(position) != 3:
        raise ValueError("block position does not have three coordinates")
    return tuple(int(value) for value in position)


def block_state_index(block: dict[str, object]) -> int:
    return int(require_tag(block, "state", TAG_INT))


def string_field(compound: dict[str, object], name: str) -> str:
    return str(require_tag(compound, name, TAG_STRING))


def press_name(wood: str) -> str:
    return (
        "vintner:grape_press"
        if wood == "oak"
        else f"vintner:{wood}_grape_press"
    )


def rack_name(wood: str) -> str:
    return (
        "vintner:wine_rack"
        if wood == "oak"
        else f"vintner:{wood}_wine_rack"
    )


def load_structure(path: Path) -> dict[str, object]:
    root = decode_gzip(path)
    size = tuple(int(value) for value in require_list(root, "size", TAG_INT))
    palette = require_list(root, "palette", TAG_COMPOUND)
    blocks = require_list(root, "blocks", TAG_COMPOUND)
    entities = list_any(root, "entities")
    data_version = int(require_tag(root, "DataVersion", TAG_INT))
    if not all(isinstance(entry, dict) for entry in palette):
        raise ValueError(f"{path}: malformed palette entry")
    if not all(isinstance(entry, dict) for entry in blocks):
        raise ValueError(f"{path}: malformed block entry")
    state_names = [palette_name(entry) for entry in palette]
    state_properties = [palette_properties(entry) for entry in palette]
    positioned: dict[
        tuple[int, int, int],
        tuple[str, dict[str, str], dict[str, object]],
    ] = {}
    counts: Counter[str] = Counter()
    block_entity_count = 0
    for block in blocks:
        position = block_position(block)
        if position in positioned:
            raise ValueError(f"{path}: duplicate block at {position}")
        state = block_state_index(block)
        if not 0 <= state < len(palette):
            raise ValueError(f"{path}: invalid state index {state}")
        positioned[position] = (
            state_names[state],
            state_properties[state],
            block,
        )
        counts[state_names[state]] += 1
        if "nbt" in block:
            if block["nbt"][0] != TAG_COMPOUND:
                raise ValueError(f"{path}: block NBT is not a compound")
            block_entity_count += 1
    return {
        "path": path,
        "root": root,
        "size": size,
        "palette": palette,
        "blocks": blocks,
        "entities": entities,
        "data_version": data_version,
        "positioned": positioned,
        "counts": counts,
        "block_entity_count": block_entity_count,
    }


def transformed(
    position: tuple[int, int, int],
    rotation: int,
    size: tuple[int, int, int],
) -> tuple[int, int, int]:
    x, y, z = position
    width, _height, depth = size
    if rotation == 0:
        return x, y, z
    if rotation == 90:
        return depth - 1 - z, y, x
    if rotation == 180:
        return width - 1 - x, y, depth - 1 - z
    if rotation == 270:
        return z, y, width - 1 - x
    raise ValueError(f"unsupported rotation {rotation}")


def audit_common(
    culture: str,
    model: dict[str, object],
    expected_size: tuple[int, int, int],
) -> None:
    if model["size"] != expected_size:
        raise ValueError(f"{culture}: unexpected size {model['size']}")
    if model["data_version"] != EXPECTED_DATA_VERSIONS[culture]:
        raise ValueError(
            f"{culture}: unexpected DataVersion {model['data_version']}"
        )
    if model["entities"]:
        raise ValueError(f"{culture}: vineyard must not author entities")

    blocks = model["blocks"]
    positioned = model["positioned"]
    counts = model["counts"]
    expected_volume = expected_size[0] * expected_size[1] * expected_size[2]
    if len(blocks) != expected_volume:
        raise ValueError(
            f"{culture}: expected fully authored {expected_volume}-block "
            f"volume, found {len(blocks)}"
        )
    if len(positioned) != expected_volume:
        raise ValueError(f"{culture}: authored volume has missing positions")
    for position in positioned:
        if not all(
            0 <= position[index] < expected_size[index]
            for index in range(3)
        ):
            raise ValueError(f"{culture}: out-of-bounds block at {position}")

    forbidden = {name: counts[name] for name in FORBIDDEN_BLOCKS if counts[name]}
    if forbidden:
        raise ValueError(f"{culture}: forbidden blocks present: {forbidden}")
    if counts["minecraft:jigsaw"] != 1:
        raise ValueError(f"{culture}: expected exactly one jigsaw connector")
    if counts["minecraft:composter"]:
        raise ValueError(f"{culture}: accidental Farmer POI remains")

    for rotation in (0, 90, 180, 270):
        rotated = {
            transformed(position, rotation, expected_size)
            for position in positioned
        }
        if len(rotated) != expected_volume:
            raise ValueError(f"{culture}: collision at {rotation} degrees")
        rotated_width = (
            expected_size[0]
            if rotation in (0, 180)
            else expected_size[2]
        )
        rotated_depth = (
            expected_size[2]
            if rotation in (0, 180)
            else expected_size[0]
        )
        if any(
            not (
                0 <= x < rotated_width
                and 0 <= y < expected_size[1]
                and 0 <= z < rotated_depth
            )
            for x, y, z in rotated
        ):
            raise ValueError(f"{culture}: out-of-bounds rotation")


def audit_jigsaw(
    culture: str,
    model: dict[str, object],
    position: tuple[int, int, int],
    orientation: str,
    final_state: str,
) -> None:
    name, properties, block = model["positioned"][position]
    if name != "minecraft:jigsaw":
        raise ValueError(f"{culture}: connector moved from {position}")
    if properties.get("orientation") != orientation:
        raise ValueError(f"{culture}: connector orientation is invalid")
    nbt_tag = block.get("nbt")
    if nbt_tag is None or nbt_tag[0] != TAG_COMPOUND:
        raise ValueError(f"{culture}: connector has no jigsaw NBT")
    nbt = nbt_tag[1]
    expected = {
        "id": "minecraft:jigsaw",
        "joint": "aligned",
        "name": "minecraft:building_entrance",
        "target": "minecraft:building_entrance",
        "pool": f"minecraft:village/{culture}/streets",
        "final_state": final_state,
    }
    actual = {key: string_field(nbt, key) for key in expected}
    if actual != expected:
        raise ValueError(f"{culture}: invalid connector NBT {actual}")


def visible_signature(model: dict[str, object]) -> list[object]:
    signature = []
    for position, (name, properties, block) in sorted(
        model["positioned"].items()
    ):
        if name == "minecraft:jigsaw":
            nbt = require_tag(block, "nbt", TAG_COMPOUND)
            final_state = string_field(nbt, "final_state")
            if final_state != "minecraft:dirt_path":
                raise ValueError("canonical jigsaw does not resolve to dirt path")
            name = final_state
            properties = {}
        signature.append([position, name, sorted(properties.items())])
    return signature


def audit_canonical_plains(model: dict[str, object]) -> str:
    source_bytes = CANONICAL_PLAINS_SOURCE.read_bytes()
    source_sha256 = hashlib.sha256(source_bytes).hexdigest()
    if source_sha256 != CANONICAL_PLAINS_SHA256:
        raise ValueError(f"canonical source SHA-256 changed: {source_sha256}")
    source = load_structure(CANONICAL_PLAINS_SOURCE)
    source_payload = gzip.decompress(source_bytes)
    if encode_root(source["root"]) != source_payload:
        raise ValueError("canonical source is not codec-lossless")
    if source["size"] != CANONICAL_PLAINS_SIZE:
        raise ValueError(f"canonical source has wrong size {source['size']}")
    if source["data_version"] != 5023:
        raise ValueError("canonical source has wrong DataVersion")
    if source["entities"]:
        raise ValueError("canonical source contains entities")
    if len(source["palette"]) != 67 or len(source["blocks"]) != 2057:
        raise ValueError("canonical source inventory changed")
    if source["block_entity_count"] != 10:
        raise ValueError("canonical source block-entity inventory changed")

    source_forbidden = {
        name: source["counts"][name]
        for name in FORBIDDEN_BLOCKS | {"minecraft:jigsaw"}
        if source["counts"][name]
    }
    if source_forbidden:
        raise ValueError(
            f"canonical source contains unwanted blocks: {source_forbidden}"
        )

    if len(model["palette"]) != 68 or model["block_entity_count"] != 11:
        raise ValueError("integrated Plains technical inventory changed")
    if source["counts"]["minecraft:dirt_path"] != 9:
        raise ValueError("canonical source path inventory changed")
    if model["counts"]["minecraft:dirt_path"] != 8:
        raise ValueError("runtime path diff exceeds the connector replacement")

    source_visible = visible_signature(source)
    runtime_visible = visible_signature(model)
    if runtime_visible != source_visible:
        raise ValueError("runtime Plains template changes player-visible blocks")

    for position, (_name, _properties, source_block) in source[
        "positioned"
    ].items():
        if position == PLAINS_JIGSAW_POSITION:
            continue
        runtime_block = model["positioned"][position][2]
        if source_block.get("nbt") != runtime_block.get("nbt"):
            raise ValueError(
                f"runtime Plains block entity differs at {position}"
            )

    visible_hash = hashlib.sha256(
        json.dumps(source_visible, separators=(",", ":")).encode("utf-8")
    ).hexdigest()

    audit_jigsaw(
        "plains",
        model,
        PLAINS_JIGSAW_POSITION,
        "north_up",
        "minecraft:dirt_path",
    )
    counts = model["counts"]
    positioned = model["positioned"]
    if counts["vintner:grape_press"] != 1:
        raise ValueError("plains: expected exactly one Grape Press")
    press_name_actual, press_properties, _block = positioned[(8, 1, 7)]
    if press_name_actual != "vintner:grape_press":
        raise ValueError("plains: canonical Grape Press moved")
    if press_properties.get("facing") != "east":
        raise ValueError("plains: canonical Grape Press facing changed")
    for approach in ((9, 1, 7), (10, 1, 7)):
        if positioned[approach][0] != "minecraft:air":
            raise ValueError(f"plains: Grape Press aisle blocked at {approach}")
    if counts["vintner:wine_rack"] != 6:
        raise ValueError("plains: player-authored wine racks changed")
    if counts["vintner:vineyard_soil"] != 27:
        raise ValueError("plains: player-authored vineyard plots changed")
    grapevine_count = sum(
        count for name, count in counts.items() if name.endswith("grapevine")
    )
    if grapevine_count != 52:
        raise ValueError("plains: player-authored grapevines changed")
    if any(
        positioned[(x, 0, z)][0] == "minecraft:air"
        for x in range(CANONICAL_PLAINS_SIZE[0])
        for z in range(CANONICAL_PLAINS_SIZE[2])
    ):
        raise ValueError("plains: foundation layer has a gap")

    print(
        "PASS plains: canonical player NBT 17x11x11, 2057 blocks, "
        "67 source/68 runtime states, 10 source/11 runtime block entities, "
        "1 technical jigsaw, 1 Winemaker Grape Press, 0 entities, "
        "0 composters, visible-equivalent, rotations 0/90/180/270"
    )
    print(f"PASS plains visible SHA-256: {visible_hash}")
    return visible_hash


def audit_interim_variant(
    culture: str,
    wood: str,
    model: dict[str, object],
) -> None:
    audit_jigsaw(
        culture,
        model,
        (0, 2, 6),
        "west_up",
        f"minecraft:{wood}_stairs["
        "facing=east,half=bottom,shape=straight,waterlogged=false]",
    )
    counts = model["counts"]
    positioned = model["positioned"]
    expected_press = press_name(wood)
    all_presses = sum(
        count for name, count in counts.items() if name.endswith("grape_press")
    )
    if all_presses != 1 or counts[expected_press] != 1:
        raise ValueError(f"{culture}: wrong Winemaker workstation")
    if positioned[(11, 3, 6)][0] != expected_press:
        raise ValueError(f"{culture}: Winemaker workstation moved")
    if positioned[(10, 3, 6)][0] != "minecraft:air":
        raise ValueError(f"{culture}: press approach is obstructed")
    if positioned[(9, 3, 6)][0] != "minecraft:air":
        raise ValueError(f"{culture}: workstation aisle is obstructed")
    if counts[rack_name(wood)] != 1:
        raise ValueError(f"{culture}: culture-matched wine rack missing")
    if counts["vintner:vineyard_soil"] != 10:
        raise ValueError(f"{culture}: vineyard plot count changed")
    grapevine_count = sum(
        count for name, count in counts.items() if name.endswith("grapevine")
    )
    if grapevine_count != 20:
        raise ValueError(f"{culture}: grapevine count changed")
    print(
        f"PASS {culture}: interim 15x10x13 template, {len(model['blocks'])} "
        f"blocks, 1 {expected_press}, 1 jigsaw, 0 entities, 0 composters, "
        "rotations 0/90/180/270"
    )


def main() -> None:
    for culture, wood in CULTURES.items():
        path = STRUCTURE_ROOT / culture / "vineyard.nbt"
        if not path.is_file():
            raise ValueError(f"missing structure {path.relative_to(ROOT)}")
        model = load_structure(path)
        expected_size = (
            CANONICAL_PLAINS_SIZE
            if culture == "plains"
            else LEGACY_INTERIM_SIZE
        )
        audit_common(culture, model, expected_size)
        if culture == "plains":
            audit_canonical_plains(model)
        else:
            audit_interim_variant(culture, wood, model)
    print(
        "Village vineyard structure audit: PASS (canonical Plains import + "
        "4 interim biome templates)"
    )


if __name__ == "__main__":
    main()
