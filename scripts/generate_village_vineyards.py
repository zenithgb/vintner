#!/usr/bin/env python3
"""Generate Vintner's village vineyard runtime structures."""

from __future__ import annotations

import argparse
import gzip
import hashlib
from pathlib import Path

from structure_nbt import (
    TAG_COMPOUND,
    TAG_END,
    TAG_INT,
    TAG_STRING,
    decode_root,
    encode_gzip,
    encode_root,
    require_list,
    require_tag,
    tag_compound,
    tag_int,
    tag_list,
    tag_string,
)


ROOT = Path(__file__).resolve().parents[1]
STRUCTURE_ROOT = (
    ROOT / "src/main/resources/data/vintner/structure/village"
)
DATA_VERSION = 4903
SIZE = (15, 10, 13)
CANONICAL_PLAINS_SOURCE = (
    ROOT
    / "structure_sources/village/plains/vineyard_tier1.nbt"
)
CANONICAL_PLAINS_SHA256 = (
    "7914e27f3bce456181324a23c459c8159ce26c1b8d227d588f99b3b8986e8c94"
)
PLAINS_SOURCE_PATH_POSITION = (9, 1, 0)
PLAINS_JIGSAW_POSITION = (9, 2, 0)

CULTURES = {
    "plains": "oak",
    "desert": "acacia",
    "savanna": "acacia",
    "snowy": "spruce",
    "taiga": "spruce",
}

# The four player exports are immutable. Each connector replaces one boundary
# block at the authored street approach; its final_state restores that block.
PLAYER_SOURCES = {
    "desert": (
        "7be4b217c7f987829e1b2a2eeabdc87bbf83054984daa2db56610eb1f712d805",
        (13, 10, 13), (6, 0, 0), "north_up",
    ),
    "savanna": (
        "60348dbd4b0a5c4443029af827e3f7806d1a22783f3b4f19bf5c29ce2bf1b80b",
        (22, 8, 18), (7, 0, 0), "north_up",
    ),
    "snowy": (
        "1fdfc017cca2ea97318bb61b06296972ea3ebeef9208222f69430ff048e00549",
        (13, 16, 10), (2, 0, 9), "south_up",
    ),
    "taiga": (
        "f3a924c609b69c36e9a0b627478585402fc2f7537eaadfc6b6f02ad91a9f2a9a",
        (23, 11, 15), (12, 1, 0), "north_up",
    ),
}


def create_player_structure(culture: str) -> bytes:
    expected_hash, expected_size, connector, orientation = PLAYER_SOURCES[culture]
    source = ROOT / "structure_sources/village" / culture / "vineyard_tier1.nbt"
    source_bytes = source.read_bytes()
    if hashlib.sha256(source_bytes).hexdigest() != expected_hash:
        raise ValueError(f"{culture}: player source hash changed")
    payload = gzip.decompress(source_bytes)
    root = decode_root(payload)
    if encode_root(root) != payload:
        raise ValueError(f"{culture}: player source is not codec-lossless")
    if tuple(require_list(root, "size", TAG_INT)) != expected_size:
        raise ValueError(f"{culture}: player source size changed")
    if require_tag(root, "entities", 9)[1]:
        raise ValueError(f"{culture}: player source contains entities")

    palette = require_list(root, "palette", TAG_COMPOUND)
    blocks = require_list(root, "blocks", TAG_COMPOUND)
    if len(blocks) != expected_size[0] * expected_size[1] * expected_size[2]:
        raise ValueError(f"{culture}: player source volume is incomplete")
    names = [str(require_tag(entry, "id", TAG_STRING)) for entry in palette]
    if "minecraft:jigsaw" in names or "minecraft:composter" in names:
        raise ValueError(f"{culture}: source contains a jigsaw or Composter")
    original = None
    for block in blocks:
        if tuple(require_list(block, "pos", TAG_INT)) == connector:
            original = block
            break
    if original is None or "nbt" in original:
        raise ValueError(f"{culture}: connector block is missing or has data")
    original_state = palette[int(require_tag(original, "state", TAG_INT))]
    final_state = str(require_tag(original_state, "id", TAG_STRING))
    properties_tag = original_state.get("properties")
    if properties_tag is not None:
        properties = properties_tag[1]
        final_state += "[" + ",".join(
            f"{key}={value[1]}" for key, value in sorted(properties.items())
        ) + "]"

    original["state"] = tag_int(len(palette))
    original["nbt"] = tag_compound({
        "joint": tag_string("aligned"),
        "final_state": tag_string(final_state),
        "name": tag_string("minecraft:building_entrance"),
        "pool": tag_string(f"minecraft:village/{culture}/streets"),
        "id": tag_string("minecraft:jigsaw"),
        "target": tag_string("minecraft:building_entrance"),
    })
    palette.append({
        "id": tag_string("minecraft:jigsaw"),
        "properties": tag_compound({"orientation": tag_string(orientation)}),
    })
    return encode_gzip(root)

FOUNDATIONS = {
    "plains": "minecraft:dirt",
    "desert": "minecraft:sandstone",
    "savanna": "minecraft:dirt",
    "snowy": "minecraft:dirt",
    "taiga": "minecraft:dirt",
}

SURFACES = {
    "plains": "minecraft:grass_block",
    "desert": "minecraft:sand",
    "savanna": "minecraft:grass_block",
    "snowy": "minecraft:snow_block",
    "taiga": "minecraft:podzol",
}

HOUSE_FOUNDATIONS = {
    "plains": "minecraft:cobblestone",
    "desert": "minecraft:smooth_sandstone",
    "savanna": "minecraft:cobblestone",
    "snowy": "minecraft:cobblestone",
    "taiga": "minecraft:cobblestone",
}

WALLS = {
    "plains": "minecraft:oak_planks",
    "desert": "minecraft:smooth_sandstone",
    "savanna": "minecraft:acacia_planks",
    "snowy": "minecraft:spruce_planks",
    "taiga": "minecraft:spruce_planks",
}

def palette_entry(
    name: str,
    properties: dict[str, str] | None = None,
) -> dict[str, tuple[int, object]]:
    entry: dict[str, tuple[int, object]] = {
        "Name": tag_string(name),
    }
    if properties:
        entry["Properties"] = tag_compound(
            {
                key: tag_string(value)
                for key, value in properties.items()
            }
        )
    return entry


def block_entry(
    position: tuple[int, int, int],
    state: int,
    nbt: dict[str, tuple[int, object]] | None = None,
) -> dict[str, tuple[int, object]]:
    entry: dict[str, tuple[int, object]] = {
        "pos": tag_list(TAG_INT, list(position)),
        "state": tag_int(state),
    }
    if nbt:
        entry["nbt"] = tag_compound(nbt)
    return entry


def grapevine_name(wood: str, colour: str) -> str:
    if wood == "oak":
        return f"vintner:{colour}_grapevine"
    return f"vintner:{wood}_{colour}_grapevine"


def vine_properties(
    x: int,
    upper: bool,
    row_start: int,
    row_end: int,
) -> dict[str, str]:
    return {
        "age": "3",
        "upper": str(upper).lower(),
        "facing": "north",
        "north": "none",
        "east": "none" if x == row_end else "level",
        "south": "none",
        "west": "none" if x == row_start else "level",
        "isolated": "false",
        "has_above": str(not upper).lower(),
        "has_below": str(upper).lower(),
    }


def create_canonical_plains_structure() -> bytes:
    """Prepare Zach's build for reliable lowered village placement."""
    source_bytes = CANONICAL_PLAINS_SOURCE.read_bytes()
    source_sha256 = hashlib.sha256(source_bytes).hexdigest()
    if source_sha256 != CANONICAL_PLAINS_SHA256:
        raise ValueError(
            "canonical Plains Tier-1 winery changed unexpectedly: "
            f"{source_sha256}"
        )

    source_payload = gzip.decompress(source_bytes)
    root = decode_root(source_payload)
    if encode_root(root) != source_payload:
        raise ValueError(
            "canonical Plains Tier-1 winery is not lossless through the "
            "maintained NBT codec"
        )

    size = require_list(root, "size", TAG_INT)
    if size != [17, 11, 11]:
        raise ValueError(f"canonical Plains size changed: {size}")

    palette = require_list(root, "palette", TAG_COMPOUND)
    blocks = require_list(root, "blocks", TAG_COMPOUND)
    if len(blocks) != 17 * 11 * 11:
        raise ValueError(
            "canonical Plains structure must author its complete volume"
        )

    state_names: list[str] = []
    for entry in palette:
        if not isinstance(entry, dict):
            raise ValueError("canonical Plains palette entry is malformed")
        state_names.append(str(require_tag(entry, "id", TAG_STRING)))
    if "minecraft:jigsaw" in state_names:
        raise ValueError("canonical player source must not contain a jigsaw")

    source_path_block = None
    connector_block = None
    for block in blocks:
        if not isinstance(block, dict):
            raise ValueError("canonical Plains block entry is malformed")
        position = tuple(require_list(block, "pos", TAG_INT))
        if position == PLAINS_SOURCE_PATH_POSITION:
            source_path_block = block
        elif position == PLAINS_JIGSAW_POSITION:
            connector_block = block
    if source_path_block is None:
        raise ValueError("canonical Plains road path position is absent")
    if connector_block is None:
        raise ValueError("canonical Plains road connector position is absent")

    source_path_state = int(require_tag(source_path_block, "state", TAG_INT))
    if state_names[source_path_state] != "minecraft:dirt_path":
        raise ValueError(
            "canonical Plains road anchor must start as the center dirt path"
        )
    connector_state = int(require_tag(connector_block, "state", TAG_INT))
    if state_names[connector_state] != "minecraft:air":
        raise ValueError("lowered Plains connector must replace authored air")
    air_state = state_names.index("minecraft:air")

    # The player's x=0 edge is only an exported terrain/air margin. Keeping
    # it in the runtime box makes the otherwise vanilla-sized winery collide
    # with village road pieces before it can be placed. Trim that non-built
    # margin while keeping the authored source byte-exact and every structural
    # or decorative block unchanged.
    runtime_blocks = []
    for block in blocks:
        position = require_list(block, "pos", TAG_INT)
        if position[0] == 0:
            continue
        position[0] -= 1
        runtime_blocks.append(block)
    blocks[:] = runtime_blocks
    size[0] = 16

    jigsaw_state = len(palette)
    palette.append(
        {
            "id": tag_string("minecraft:jigsaw"),
            "properties": tag_compound(
                {"orientation": tag_string("north_up")}
            ),
        }
    )
    # Raising the connector one local block makes jigsaw alignment lower the
    # complete player-authored structure by one world block. Move the authored
    # boundary path with the connector so the village road remains at grade.
    source_path_block["state"] = tag_int(air_state)
    connector_block["state"] = tag_int(jigsaw_state)
    connector_block["nbt"] = tag_compound(
        {
            "joint": tag_string("aligned"),
            "final_state": tag_string("minecraft:dirt_path"),
            "name": tag_string("minecraft:building_entrance"),
            "pool": tag_string("minecraft:village/plains/streets"),
            "id": tag_string("minecraft:jigsaw"),
            "target": tag_string("minecraft:building_entrance"),
        }
    )
    return encode_gzip(root)


def create_structure(culture: str, wood: str) -> bytes:
    palette: list[dict[str, tuple[int, object]]] = []
    palette_indexes: dict[
        tuple[str, tuple[tuple[str, str], ...]],
        int,
    ] = {}

    def state(
        name: str,
        properties: dict[str, str] | None = None,
    ) -> int:
        key = (name, tuple(sorted((properties or {}).items())))
        if key not in palette_indexes:
            palette_indexes[key] = len(palette)
            palette.append(palette_entry(name, properties))
        return palette_indexes[key]

    soil = state("vintner:vineyard_soil")
    air = state("minecraft:air")
    path = state("minecraft:dirt_path")
    foundation = state(FOUNDATIONS[culture])
    surface = state(SURFACES[culture])
    house_foundation = state(HOUSE_FOUNDATIONS[culture])
    planks = state(f"minecraft:{wood}_planks")
    wall = state(WALLS[culture])
    border_x = state(
        f"minecraft:stripped_{wood}_log",
        {"axis": "x"},
    )
    border_z = state(
        f"minecraft:stripped_{wood}_log",
        {"axis": "z"},
    )
    post = state(
        f"minecraft:stripped_{wood}_log",
        {"axis": "y"},
    )
    glass = state("minecraft:glass")
    door_lower = state(
        f"minecraft:{wood}_door",
        {
            "facing": "west",
            "half": "lower",
            "hinge": "left",
            "open": "false",
            "powered": "false",
        },
    )
    door_upper = state(
        f"minecraft:{wood}_door",
        {
            "facing": "west",
            "half": "upper",
            "hinge": "left",
            "open": "false",
            "powered": "false",
        },
    )
    roof_north = state(
        f"minecraft:{wood}_stairs",
        {
            "facing": "north",
            "half": "bottom",
            "shape": "straight",
            "waterlogged": "false",
        },
    )
    roof_south = state(
        f"minecraft:{wood}_stairs",
        {
            "facing": "south",
            "half": "bottom",
            "shape": "straight",
            "waterlogged": "false",
        },
    )
    roof_ridge = state(
        f"minecraft:{wood}_slab",
        {"type": "bottom", "waterlogged": "false"},
    )
    press_name = (
        "vintner:grape_press"
        if wood == "oak"
        else f"vintner:{wood}_grape_press"
    )
    press = state(
        press_name,
        {
            "facing": "west",
            "input_level": "0",
            "input_type": "0",
            "output_type": "0",
        },
    )
    rack_name = (
        "vintner:wine_rack"
        if wood == "oak"
        else f"vintner:{wood}_wine_rack"
    )
    rack = state(
        rack_name,
        {
            "facing": "west",
            "bottles": "0",
            "display_style": "red",
            "display_age": "table",
        },
    )
    barrel = state(
        "minecraft:barrel",
        {"facing": "up", "open": "false"},
    )
    lantern = state(
        "minecraft:lantern",
        {"hanging": "false", "waterlogged": "false"},
    )
    jigsaw = state(
        "minecraft:jigsaw",
        {"orientation": "west_up"},
    )
    entrance_stairs = (
        f"minecraft:{wood}_stairs"
        "[facing=east,half=bottom,shape=straight,waterlogged=false]"
    )

    blocks: dict[
        tuple[int, int, int],
        dict[str, tuple[int, object]],
    ] = {}

    # Village houses use rigid pool elements. A complete buried foundation and
    # surface layer make the combined cottage and vineyard a deliberate terrace
    # rather than leaving corners floating over ordinary village-grade slopes.
    for x in range(SIZE[0]):
        for z in range(SIZE[2]):
            blocks[(x, 0, z)] = block_entry((x, 0, z), foundation)
            blocks[(x, 1, z)] = block_entry((x, 1, z), foundation)
            blocks[(x, 2, z)] = block_entry((x, 2, z), surface)
            for y in range(3, SIZE[1]):
                blocks[(x, y, z)] = block_entry((x, y, z), air)

    # Keep the retaining timber in the buried foundation. Exposed terrain cuts
    # read as a deliberate vineyard terrace, while level sites still blend into
    # the village. Leave the road entrance as the local foundation material.
    for x in range(SIZE[0]):
        for y in (0, 1):
            blocks[(x, y, 0)] = block_entry((x, y, 0), border_x)
            blocks[(x, y, SIZE[2] - 1)] = block_entry(
                (x, y, SIZE[2] - 1),
                border_x,
            )
    for z in range(1, SIZE[2] - 1):
        for y in (0, 1):
            if z != 6:
                blocks[(0, y, z)] = block_entry((0, y, z), border_z)
            blocks[(SIZE[0] - 1, y, z)] = block_entry(
                (SIZE[0] - 1, y, z),
                border_z,
            )

    # A compact U-shaped access walk ties both planted rows to the front door.
    for x in range(1, 8):
        blocks[(x, 2, 6)] = block_entry((x, 2, 6), path)
        blocks[(x, 2, 2)] = block_entry((x, 2, 2), path)
        blocks[(x, 2, 10)] = block_entry((x, 2, 10), path)
    for z in range(2, 11):
        blocks[(1, 2, z)] = block_entry((1, 2, z), path)

    # Ten mature plants make the plot visually established without granting a
    # large early-game harvest. Each row keeps a consistent five-vine rhythm.
    row_start = 2
    row_end = 6
    for z, colour in ((1, "red"), (11, "white")):
        vine_name = grapevine_name(wood, colour)
        for x in range(row_start, row_end + 1):
            blocks[(x, 2, z)] = block_entry((x, 2, z), soil)
            for y, upper in ((3, False), (4, True)):
                vine = state(
                    vine_name,
                    vine_properties(
                        x,
                        upper,
                        row_start,
                        row_end,
                    ),
                )
                blocks[(x, y, z)] = block_entry((x, y, z), vine)

    # A finished six-by-seven cottage workhouse occupies the east side. The
    # floor and buried masonry are continuous, and every wall has deliberate
    # timber corners, openings, and a complete gabled roof.
    for x in range(8, 14):
        for z in range(3, 10):
            for y in (0, 1):
                blocks[(x, y, z)] = block_entry(
                    (x, y, z),
                    house_foundation,
                )
            blocks[(x, 2, z)] = block_entry((x, 2, z), planks)

    for y in range(3, 6):
        for x in range(8, 14):
            for z in range(3, 10):
                if x not in (8, 13) and z not in (3, 9):
                    continue
                block_state = (
                    post
                    if x in (8, 13) and z in (3, 9)
                    else house_foundation
                    if y == 3
                    else wall
                )
                blocks[(x, y, z)] = block_entry(
                    (x, y, z),
                    block_state,
                )

    # Usable entrance and windows. Full glass blocks are intentional here: they
    # remain visually closed and rotation-safe in every generated village.
    blocks[(8, 3, 6)] = block_entry((8, 3, 6), door_lower)
    blocks[(8, 4, 6)] = block_entry((8, 4, 6), door_upper)
    for position in (
        (8, 4, 4),
        (8, 4, 8),
        (13, 4, 6),
        (10, 4, 3),
        (11, 4, 3),
        (10, 4, 9),
        (11, 4, 9),
    ):
        blocks[position] = block_entry(position, glass)

    # Gable infill sits beneath a complete, one-block-overhanging stair roof.
    for y, z_start, z_end in (
        (6, 3, 9),
        (7, 4, 8),
        (8, 5, 7),
    ):
        for x in (8, 13):
            for z in range(z_start, z_end + 1):
                blocks[(x, y, z)] = block_entry((x, y, z), wall)

    for x in range(7, 15):
        for y, north_z, south_z in (
            (6, 2, 10),
            (7, 3, 9),
            (8, 4, 8),
            (9, 5, 7),
        ):
            blocks[(x, y, north_z)] = block_entry(
                (x, y, north_z),
                roof_north,
            )
            blocks[(x, y, south_z)] = block_entry(
                (x, y, south_z),
                roof_south,
            )
        blocks[(x, 9, 6)] = block_entry((x, 9, 6), roof_ridge)

    # The actual Winemaker POI is the culture-matched grape press. Its west
    # approach cell and the direct line from the door remain unobstructed.
    blocks[(11, 3, 6)] = block_entry((11, 3, 6), press)
    blocks[(12, 3, 4)] = block_entry((12, 3, 4), rack)
    blocks[(12, 3, 8)] = block_entry((12, 3, 8), barrel)
    blocks[(12, 4, 8)] = block_entry((12, 4, 8), lantern)

    blocks[(0, 2, 6)] = block_entry(
        (0, 2, 6),
        jigsaw,
        {
            "joint": tag_string("aligned"),
            "final_state": tag_string(entrance_stairs),
            "name": tag_string("minecraft:building_entrance"),
            "pool": tag_string(
                f"minecraft:village/{culture}/streets"
            ),
            "id": tag_string("minecraft:jigsaw"),
            "target": tag_string("minecraft:building_entrance"),
        },
    )

    root = {
        "size": tag_list(TAG_INT, list(SIZE)),
        "entities": tag_list(TAG_END, []),
        "blocks": tag_list(
            TAG_COMPOUND,
            [blocks[position] for position in sorted(blocks)],
        ),
        "palette": tag_list(TAG_COMPOUND, palette),
        "DataVersion": tag_int(DATA_VERSION),
    }
    return encode_gzip(root)


def main() -> None:
    parser = argparse.ArgumentParser()
    parser.add_argument(
        "--check",
        action="store_true",
        help="fail if committed structures differ from generated bytes",
    )
    args = parser.parse_args()

    for culture, wood in CULTURES.items():
        output = STRUCTURE_ROOT / culture / "vineyard.nbt"
        generated = (
            create_canonical_plains_structure()
            if culture == "plains"
            else create_player_structure(culture)
        )
        relative = output.relative_to(ROOT)

        if args.check:
            if not output.is_file() or output.read_bytes() != generated:
                raise SystemExit(
                    f"OUT OF DATE: {relative}; run "
                    "scripts/generate_village_vineyards.py"
                )
            print(f"Verified {relative}")
            continue

        output.parent.mkdir(parents=True, exist_ok=True)
        output.write_bytes(generated)
        print(f"Generated {relative}")


if __name__ == "__main__":
    main()
