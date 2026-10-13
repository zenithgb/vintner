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
from generate_village_vineyards import (
    DESERT_FLOOR_Y_LOCAL,
    PLAYER_SOURCES,
    create_player_structure,
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
RUNTIME_PLAINS_SIZE = (16, 11, 11)
PLAINS_SOURCE_PATH_POSITION = (9, 1, 0)
PLAINS_SOURCE_JIGSAW_POSITION = (9, 2, 0)
PLAINS_RUNTIME_PATH_POSITION = (8, 1, 0)
PLAINS_RUNTIME_JIGSAW_POSITION = (8, 2, 0)
EXPECTED_DATA_VERSIONS = {
    "plains": 5023,
    "desert": 5023,
    "savanna": 5023,
    "snowy": 5023,
    "taiga": 5023,
}
DESERT_OMITTED_LOWER_AIR = 1034
CULTURES = {
    "plains": "oak",
    "desert": "acacia",
    "savanna": "acacia",
    "snowy": "spruce",
    "taiga": "spruce",
}
PAIRED_SOURCES = {
    "savanna": {
        "winery_tier1": ("7f50357028418cdd2599164fdf40d6ebbd625748832b7147f37128bcca1fa91a", (15, 8, 18)),
        "vineyard_field_tier1": ("39733138bb05eed5f9b48e0028a0a2587b7b5bdefa147c5afd0b9cb4c5d2b799", (7, 3, 18)),
    },
    "taiga": {
        "winery_tier1": ("8198a9905ceb011c55a7546087ca33ef74b6bdd19d74877d8504e8db189583fb", (11, 11, 15)),
        "vineyard_field_tier1": ("b9ce1de6fdbf215960f310b59664e8b223111bab62eac98bc976ff69b95e45c3", (11, 3, 13)),
    },
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
    expected_blocks = expected_volume - (
        DESERT_OMITTED_LOWER_AIR if culture == "desert" else 0
    )
    if len(blocks) != expected_blocks:
        raise ValueError(
            f"{culture}: expected {expected_blocks} runtime blocks, "
            f"found {len(blocks)}"
        )
    if len(positioned) != expected_blocks:
        raise ValueError(f"{culture}: runtime has duplicate positions")
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
        if len(rotated) != expected_blocks:
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
            if final_state not in ("minecraft:dirt_path", "minecraft:air"):
                raise ValueError("unexpected jigsaw final state")
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
    if model["counts"]["minecraft:dirt_path"] != 9:
        raise ValueError("runtime Plains path inventory changed")

    source_visible = visible_signature(source)
    runtime_visible = visible_signature(model)
    source_technical_positions = {
        PLAINS_SOURCE_PATH_POSITION,
        PLAINS_SOURCE_JIGSAW_POSITION,
    }
    runtime_technical_positions = {
        PLAINS_RUNTIME_PATH_POSITION,
        PLAINS_RUNTIME_JIGSAW_POSITION,
    }
    source_authored = []
    for entry in source_visible:
        position = tuple(entry[0])
        if position[0] == 0 or position in source_technical_positions:
            continue
        source_authored.append(
            [
                (position[0] - 1, position[1], position[2]),
                entry[1],
                entry[2],
            ]
        )
    source_authored.sort(key=lambda entry: entry[0])
    runtime_authored = [
        entry for entry in runtime_visible
        if tuple(entry[0]) not in runtime_technical_positions
    ]
    if runtime_authored != source_authored:
        raise ValueError(
            "runtime Plains template changes player-authored blocks outside "
            "the trimmed terrain margin and technical road connector"
        )
    if model["positioned"][PLAINS_RUNTIME_PATH_POSITION][0] != "minecraft:dirt_path":
        raise ValueError("runtime Plains source road path was not preserved")
    if source["positioned"][PLAINS_SOURCE_JIGSAW_POSITION][0] != "minecraft:air":
        raise ValueError("canonical source connector cell is not air")

    for position, (_name, _properties, source_block) in source[
        "positioned"
    ].items():
        if position[0] == 0 or position in source_technical_positions:
            continue
        runtime_position = (position[0] - 1, position[1], position[2])
        runtime_block = model["positioned"][runtime_position][2]
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
        PLAINS_RUNTIME_JIGSAW_POSITION,
        "north_up",
        "minecraft:air",
    )
    counts = model["counts"]
    positioned = model["positioned"]
    if counts["vintner:grape_press"] != 1:
        raise ValueError("plains: expected exactly one Grape Press")
    press_name_actual, press_properties, _block = positioned[(7, 1, 7)]
    if press_name_actual != "vintner:grape_press":
        raise ValueError("plains: canonical Grape Press moved")
    if press_properties.get("facing") != "east":
        raise ValueError("plains: canonical Grape Press facing changed")
    for approach in ((8, 1, 7), (9, 1, 7)):
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
        for x in range(RUNTIME_PLAINS_SIZE[0])
        for z in range(RUNTIME_PLAINS_SIZE[2])
    ):
        raise ValueError("plains: foundation layer has a gap")

    print(
        "PASS plains: canonical source 17x11x11/2057 blocks, runtime "
        "16x11x11/1936 blocks after trimming one terrain-only margin, "
        "67 source/68 runtime states, 10 source/11 runtime block entities, "
        "1 technical jigsaw, 1 Winemaker Grape Press, 0 entities, "
        "0 composters, authored-equivalent outside the terrain-only margin "
        "and ground-level road path, rotations 0/90/180/270"
    )
    print(f"PASS plains visible SHA-256: {visible_hash}")
    return visible_hash


def audit_player_variant(culture: str, model: dict[str, object]) -> None:
    expected_hash, size, connector, orientation = PLAYER_SOURCES[culture]
    source_path = ROOT / "structure_sources/village" / culture / "vineyard_tier1.nbt"
    source_bytes = source_path.read_bytes()
    if hashlib.sha256(source_bytes).hexdigest() != expected_hash:
        raise ValueError(f"{culture}: immutable player source hash changed")
    source = load_structure(source_path)
    if encode_root(source["root"]) != gzip.decompress(source_bytes):
        raise ValueError(f"{culture}: source is not NBT codec-lossless")
    if source["size"] != size or source["data_version"] != 5023:
        raise ValueError(f"{culture}: source dimensions or version changed")
    if source["entities"]:
        raise ValueError(f"{culture}: source contains entities")
    if len(source["positioned"]) != size[0] * size[1] * size[2]:
        raise ValueError(f"{culture}: incomplete player export")
    if any(source["counts"][name] for name in FORBIDDEN_BLOCKS | {"minecraft:jigsaw"}):
        raise ValueError(f"{culture}: forbidden source block")
    if model["path"].read_bytes() != create_player_structure(culture):
        raise ValueError(f"{culture}: runtime is not deterministic from source")

    original_name, original_properties, original_block = source["positioned"][connector]
    final_state = original_name
    if original_properties:
        final_state += "[" + ",".join(
            f"{key}={value}" for key, value in sorted(original_properties.items())
        ) + "]"
    audit_jigsaw(culture, model, connector, orientation, final_state)
    omitted = {
        position for position, (name, _properties, _block)
        in source["positioned"].items()
        if culture == "desert"
        and position[1] < DESERT_FLOOR_Y_LOCAL
        and name == "minecraft:air"
    }
    if culture == "desert" and len(omitted) != DESERT_OMITTED_LOWER_AIR:
        raise ValueError("desert: unexpected lower-air count")
    if set(model["positioned"]) != set(source["positioned"]) - omitted:
        raise ValueError(f"{culture}: runtime omission exceeds lower Desert air")
    for position, authored in source["positioned"].items():
        if position in omitted:
            continue
        if position == connector:
            continue
        if model["positioned"][position] != authored:
            raise ValueError(f"{culture}: player block changed at {position}")
    if original_block.get("nbt"):
        raise ValueError(f"{culture}: connector replaces authored block entity")
    presses = [
        (position, name, properties)
        for position, (name, properties, _block) in source["positioned"].items()
        if name.endswith("grape_press")
    ]
    if len(presses) != 1 or model["positioned"][presses[0][0]][:2] != presses[0][1:]:
        raise ValueError(f"{culture}: Winemaker workstation changed")
    if not any(name.endswith("grapevine") for name in source["counts"]):
        raise ValueError(f"{culture}: source has no vineyard")
    print(
        f"PASS {culture}: source/runtime {size}, "
        f"{len(source['blocks'])}/{len(model['blocks'])} blocks, "
        f"{source['block_entity_count']} authored block entities, 0 entities, "
        f"0 Composters, 1 press {presses[0]}, connector {connector} {orientation}, "
        f"{len(omitted)} lower-air cells omitted, "
        "all retained authored blocks and data byte-equivalent, "
        "rotations 0/90/180/270 in bounds"
    )


def main() -> None:
    for culture in CULTURES:
        path = STRUCTURE_ROOT / culture / "vineyard.nbt"
        if not path.is_file():
            raise ValueError(f"missing structure {path.relative_to(ROOT)}")
        model = load_structure(path)
        expected_size = (
            RUNTIME_PLAINS_SIZE
            if culture == "plains"
            else PLAYER_SOURCES[culture][1]
        )
        audit_common(culture, model, expected_size)
        if culture == "plains":
            audit_canonical_plains(model)
        else:
            audit_player_variant(culture, model)
    for culture, pieces in PAIRED_SOURCES.items():
        for name, (expected_hash, expected_size) in pieces.items():
            source_path = ROOT / "structure_sources/village" / culture / f"{name}.nbt"
            runtime_path = STRUCTURE_ROOT / culture / f"{name}.nbt"
            source_bytes = source_path.read_bytes()
            if hashlib.sha256(source_bytes).hexdigest() != expected_hash:
                raise ValueError(f"{culture}/{name}: immutable split source hash changed")
            if runtime_path.read_bytes() != source_bytes:
                raise ValueError(f"{culture}/{name}: runtime differs from source")
            model = load_structure(source_path)
            if model["size"] != expected_size or model["entities"]:
                raise ValueError(f"{culture}/{name}: dimensions or entities changed")
            if any(model["counts"][block] for block in FORBIDDEN_BLOCKS | {"minecraft:jigsaw"}):
                raise ValueError(f"{culture}/{name}: forbidden block")
            presses = sum(count for block, count in model["counts"].items()
                          if block.endswith("grape_press"))
            if presses != (1 if name == "winery_tier1" else 0):
                raise ValueError(f"{culture}/{name}: wrong workstation count")
            if name == "vineyard_field_tier1" and not any(
                    block.endswith("grapevine") for block in model["counts"]):
                raise ValueError(f"{culture}/{name}: no vineyard vines")
            print(f"PASS {culture}/{name}: source/runtime byte-identical, "
                  f"SHA-256 {expected_hash}, size {expected_size}, "
                  f"{presses} press, 0 entities and forbidden blocks")
    print(
        "Village vineyard structure audit: PASS (five player-authored wineries)"
    )


if __name__ == "__main__":
    main()
