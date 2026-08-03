#!/usr/bin/env python3
"""Generate Vintner's culture-matched village vineyard structures."""

from __future__ import annotations

import gzip
import struct
from pathlib import Path


ROOT = Path(__file__).resolve().parents[1]
STRUCTURE_ROOT = (
    ROOT / "src/main/resources/data/vintner/structure/village"
)
DATA_VERSION = 4903
SIZE = (11, 5, 9)

CULTURES = {
    "plains": "oak",
    "desert": "acacia",
    "savanna": "acacia",
    "snowy": "spruce",
    "taiga": "spruce",
}

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

TAG_END = 0
TAG_BYTE = 1
TAG_INT = 3
TAG_STRING = 8
TAG_LIST = 9
TAG_COMPOUND = 10


def tag_byte(value: int) -> tuple[int, int]:
    return TAG_BYTE, value


def tag_int(value: int) -> tuple[int, int]:
    return TAG_INT, value


def tag_string(value: str) -> tuple[int, str]:
    return TAG_STRING, value


def tag_list(
    element_type: int,
    values: list[object],
) -> tuple[int, tuple[int, list[object]]]:
    return TAG_LIST, (element_type, values)


def tag_compound(
    value: dict[str, tuple[int, object]],
) -> tuple[int, dict[str, tuple[int, object]]]:
    return TAG_COMPOUND, value


def encode_string(value: str) -> bytes:
    encoded = value.encode("utf-8")
    return struct.pack(">H", len(encoded)) + encoded


def encode_payload(tag_type: int, value: object) -> bytes:
    if tag_type == TAG_BYTE:
        return struct.pack(">b", int(value))
    if tag_type == TAG_INT:
        return struct.pack(">i", int(value))
    if tag_type == TAG_STRING:
        return encode_string(str(value))
    if tag_type == TAG_LIST:
        element_type, values = value
        return (
            struct.pack(">bi", element_type, len(values))
            + b"".join(
                encode_payload(element_type, element)
                for element in values
            )
        )
    if tag_type == TAG_COMPOUND:
        encoded = bytearray()
        for name, (child_type, child_value) in value.items():
            encoded.extend(struct.pack(">b", child_type))
            encoded.extend(encode_string(name))
            encoded.extend(encode_payload(child_type, child_value))
        encoded.extend(struct.pack(">b", TAG_END))
        return bytes(encoded)
    raise ValueError(f"Unsupported NBT tag type: {tag_type}")


def encode_root(value: dict[str, tuple[int, object]]) -> bytes:
    return (
        struct.pack(">b", TAG_COMPOUND)
        + encode_string("")
        + encode_payload(TAG_COMPOUND, value)
    )


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
) -> dict[str, str]:
    return {
        "age": "3",
        "upper": str(upper).lower(),
        "facing": "north",
        "north": "none",
        "east": "none" if x == 9 else "level",
        "south": "none",
        "west": "none" if x == 2 else "level",
        "isolated": "false",
        "has_above": str(not upper).lower(),
        "has_below": str(upper).lower(),
    }


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
    border_x = state(
        f"minecraft:stripped_{wood}_log",
        {"axis": "x"},
    )
    border_z = state(
        f"minecraft:stripped_{wood}_log",
        {"axis": "z"},
    )
    composter = state("minecraft:composter", {"level": "4"})
    contract_board = state(
        "vintner:village_contract_board",
        {"facing": "west", "wood": wood},
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

    # Village farms are rigid pieces. A complete buried foundation and surface
    # layer create a deliberate terrace instead of leaving parts of the farm
    # floating over, or swallowed by, uneven village terrain.
    for x in range(SIZE[0]):
        for z in range(SIZE[2]):
            blocks[(x, 0, z)] = block_entry((x, 0, z), foundation)
            blocks[(x, 1, z)] = block_entry((x, 1, z), surface)
            for y in range(2, SIZE[1]):
                blocks[(x, y, z)] = block_entry((x, y, z), air)

    # Keep the retaining timber in the buried foundation rather than turning
    # it into a full-height rail around the farm. Exposed terrain cuts still
    # read as a deliberate terrace, while level sites blend into the village.
    # Leave the entrance foundation as the local ground material.
    for x in range(SIZE[0]):
        blocks[(x, 0, 0)] = block_entry((x, 0, 0), border_x)
        blocks[(x, 0, SIZE[2] - 1)] = block_entry(
            (x, 0, SIZE[2] - 1),
            border_x,
        )
    for z in range(1, SIZE[2] - 1):
        if z != 4:
            blocks[(0, 0, z)] = block_entry((0, 0, z), border_z)
        blocks[(SIZE[0] - 1, 0, z)] = block_entry(
            (SIZE[0] - 1, 0, z),
            border_z,
        )

    for x in range(1, SIZE[0] - 1):
        blocks[(x, 1, 4)] = block_entry((x, 1, 4), path)

    for z, colour in ((1, "red"), (7, "white")):
        vine_name = grapevine_name(wood, colour)
        for x in range(2, 10):
            blocks[(x, 1, z)] = block_entry((x, 1, z), soil)
            for y, upper in ((2, False), (3, True)):
                vine = state(
                    vine_name,
                    vine_properties(x, upper),
                )
                blocks[(x, y, z)] = block_entry((x, y, z), vine)

    blocks[(9, 2, 4)] = block_entry((9, 2, 4), composter)
    blocks[(1, 2, 3)] = block_entry((1, 2, 3), contract_board)
    blocks[(0, 1, 4)] = block_entry(
        (0, 1, 4),
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
    return gzip.compress(encode_root(root), mtime=0)


def main() -> None:
    for culture, wood in CULTURES.items():
        output = STRUCTURE_ROOT / culture / "vineyard.nbt"
        output.parent.mkdir(parents=True, exist_ok=True)
        output.write_bytes(create_structure(culture, wood))
        print(f"Generated {output.relative_to(ROOT)}")


if __name__ == "__main__":
    main()
