#!/usr/bin/env python3
"""Audit the authoritative Vintner village vineyard structure templates."""

from __future__ import annotations

import gzip
import struct
from collections import Counter
from pathlib import Path


ROOT = Path(__file__).resolve().parents[1]
STRUCTURE_ROOT = (
    ROOT / "src/main/resources/data/vintner/structure/village"
)
EXPECTED_SIZE = [15, 10, 13]
EXPECTED_DATA_VERSION = 4903
CULTURES = {
    "plains": "oak",
    "desert": "acacia",
    "savanna": "acacia",
    "snowy": "spruce",
    "taiga": "spruce",
}
FORBIDDEN_BLOCKS = {
    "minecraft:barrier",
    "minecraft:command_block",
    "minecraft:composter",
    "minecraft:jigsaw",  # allowed only through the exact connector check
    "minecraft:structure_block",
}

TAG_END = 0
TAG_BYTE = 1
TAG_INT = 3
TAG_STRING = 8
TAG_LIST = 9
TAG_COMPOUND = 10


class Reader:
    def __init__(self, data: bytes) -> None:
        self.data = data
        self.offset = 0

    def take(self, length: int) -> bytes:
        end = self.offset + length
        if end > len(self.data):
            raise ValueError("truncated NBT payload")
        value = self.data[self.offset:end]
        self.offset = end
        return value

    def byte(self) -> int:
        return struct.unpack(">b", self.take(1))[0]

    def unsigned_short(self) -> int:
        return struct.unpack(">H", self.take(2))[0]

    def integer(self) -> int:
        return struct.unpack(">i", self.take(4))[0]

    def string(self) -> str:
        return self.take(self.unsigned_short()).decode("utf-8")

    def payload(self, tag_type: int) -> object:
        if tag_type == TAG_BYTE:
            return self.byte()
        if tag_type == TAG_INT:
            return self.integer()
        if tag_type == TAG_STRING:
            return self.string()
        if tag_type == TAG_LIST:
            element_type = self.byte()
            length = self.integer()
            if length < 0:
                raise ValueError("negative NBT list length")
            if element_type == TAG_END and length:
                raise ValueError(
                    "non-empty NBT list declares TAG_End elements at "
                    f"offset {self.offset} (length {length})"
                )
            return [self.payload(element_type) for _ in range(length)]
        if tag_type == TAG_COMPOUND:
            result: dict[str, object] = {}
            while True:
                child_type = self.byte()
                if child_type == TAG_END:
                    return result
                child_name = self.string()
                result[child_name] = self.payload(child_type)
        raise ValueError(f"unsupported NBT tag type {tag_type}")


def decode(path: Path) -> dict[str, object]:
    reader = Reader(gzip.decompress(path.read_bytes()))
    if reader.byte() != TAG_COMPOUND:
        raise ValueError("root is not an NBT compound")
    if reader.string() != "":
        raise ValueError("root compound must have an empty name")
    root = reader.payload(TAG_COMPOUND)
    if reader.offset != len(reader.data):
        raise ValueError("trailing bytes after root compound")
    if not isinstance(root, dict):
        raise ValueError("decoded root is not a compound")
    return root


def state_name(
    palette: list[dict[str, object]],
    block: dict[str, object],
) -> str:
    state_index = block.get("state")
    if not isinstance(state_index, int):
        raise ValueError("block state index is not an integer")
    if state_index < 0 or state_index >= len(palette):
        raise ValueError(f"block state index {state_index} is out of range")
    name = palette[state_index].get("Name")
    if not isinstance(name, str):
        raise ValueError("palette entry is missing a string Name")
    return name


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


def transformed(
    position: tuple[int, int, int],
    rotation: int,
) -> tuple[int, int, int]:
    x, y, z = position
    width, _, depth = EXPECTED_SIZE
    if rotation == 0:
        return x, y, z
    if rotation == 90:
        return depth - 1 - z, y, x
    if rotation == 180:
        return width - 1 - x, y, depth - 1 - z
    if rotation == 270:
        return z, y, width - 1 - x
    raise ValueError(f"unsupported rotation {rotation}")


def audit_structure(culture: str, wood: str) -> Counter[str]:
    path = STRUCTURE_ROOT / culture / "vineyard.nbt"
    if not path.is_file():
        raise ValueError(f"missing structure {path.relative_to(ROOT)}")

    root = decode(path)
    if root.get("size") != EXPECTED_SIZE:
        raise ValueError(f"{culture}: unexpected size {root.get('size')}")
    if root.get("DataVersion") != EXPECTED_DATA_VERSION:
        raise ValueError(
            f"{culture}: unexpected DataVersion {root.get('DataVersion')}"
        )
    if root.get("entities") != []:
        raise ValueError(f"{culture}: vineyard must not author entities")

    palette = root.get("palette")
    blocks = root.get("blocks")
    if not isinstance(palette, list) or not isinstance(blocks, list):
        raise ValueError(f"{culture}: palette or blocks list is missing")
    if not all(isinstance(entry, dict) for entry in palette):
        raise ValueError(f"{culture}: malformed palette entry")
    if not all(isinstance(entry, dict) for entry in blocks):
        raise ValueError(f"{culture}: malformed block entry")

    expected_volume = EXPECTED_SIZE[0] * EXPECTED_SIZE[1] * EXPECTED_SIZE[2]
    if len(blocks) != expected_volume:
        raise ValueError(
            f"{culture}: expected a fully authored {expected_volume}-block "
            f"volume, found {len(blocks)}"
        )

    positions: set[tuple[int, int, int]] = set()
    positioned: dict[tuple[int, int, int], tuple[str, dict[str, object]]] = {}
    counts: Counter[str] = Counter()
    for block in blocks:
        position_raw = block.get("pos")
        if not (
            isinstance(position_raw, list)
            and len(position_raw) == 3
            and all(isinstance(value, int) for value in position_raw)
        ):
            raise ValueError(f"{culture}: malformed block position")
        position = tuple(position_raw)
        if position in positions:
            raise ValueError(f"{culture}: duplicate block at {position}")
        positions.add(position)
        if not all(
            0 <= position[index] < EXPECTED_SIZE[index]
            for index in range(3)
        ):
            raise ValueError(f"{culture}: out-of-bounds block at {position}")
        name = state_name(palette, block)
        counts[name] += 1
        positioned[position] = (name, block)

    forbidden = {
        name: counts[name]
        for name in FORBIDDEN_BLOCKS - {"minecraft:jigsaw"}
        if counts[name]
    }
    if forbidden:
        raise ValueError(f"{culture}: forbidden blocks present: {forbidden}")
    if counts["minecraft:jigsaw"] != 1:
        raise ValueError(f"{culture}: expected exactly one jigsaw connector")

    connector_name, connector = positioned[(0, 2, 6)]
    connector_nbt = connector.get("nbt")
    if connector_name != "minecraft:jigsaw" or not isinstance(
        connector_nbt, dict
    ):
        raise ValueError(f"{culture}: missing exact road connector")
    expected_pool = f"minecraft:village/{culture}/streets"
    if connector_nbt.get("pool") != expected_pool:
        raise ValueError(f"{culture}: connector targets wrong street pool")
    final_state = connector_nbt.get("final_state")
    if not (
        isinstance(final_state, str)
        and final_state.startswith(f"minecraft:{wood}_stairs[")
        and "facing=east" in final_state
    ):
        raise ValueError(f"{culture}: connector final state is invalid")

    expected_press = press_name(wood)
    all_presses = sum(
        count
        for name, count in counts.items()
        if name.endswith("grape_press")
    )
    if all_presses != 1 or counts[expected_press] != 1:
        raise ValueError(
            f"{culture}: expected one culture-matched {expected_press}, "
            f"found {all_presses} grape presses"
        )
    if positioned[(11, 3, 6)][0] != expected_press:
        raise ValueError(f"{culture}: Winemaker workstation moved unexpectedly")
    if positioned[(10, 3, 6)][0] != "minecraft:air":
        raise ValueError(f"{culture}: grape-press approach cell is obstructed")
    if positioned[(9, 3, 6)][0] != "minecraft:air":
        raise ValueError(f"{culture}: interior door-to-workstation aisle blocked")

    if counts[rack_name(wood)] != 1:
        raise ValueError(f"{culture}: missing culture-matched wine rack")
    if counts["vintner:vineyard_soil"] != 10:
        raise ValueError(f"{culture}: expected ten vineyard soil plots")
    grapevine_count = sum(
        count
        for name, count in counts.items()
        if name.endswith("grapevine")
    )
    if grapevine_count != 20:
        raise ValueError(f"{culture}: expected twenty grapevine blocks")
    if counts[f"minecraft:{wood}_door"] != 2:
        raise ValueError(f"{culture}: cottage door is incomplete")
    if counts["minecraft:glass"] != 7:
        raise ValueError(f"{culture}: cottage window count changed")
    if counts[f"minecraft:{wood}_stairs"] != 64:
        raise ValueError(f"{culture}: gabled roof stair shell is incomplete")
    if counts[f"minecraft:{wood}_slab"] != 8:
        raise ValueError(f"{culture}: roof ridge is incomplete")
    if counts["minecraft:dirt_path"] != 27:
        raise ValueError(f"{culture}: vineyard access path is incomplete")

    # The template engine rotates block states at placement. Verify the full
    # authored volume stays unique and bounded in all four supported rotations.
    for rotation in (0, 90, 180, 270):
        rotated = {transformed(position, rotation) for position in positions}
        if len(rotated) != expected_volume:
            raise ValueError(f"{culture}: collision at {rotation} degrees")
        rotated_width = EXPECTED_SIZE[0] if rotation in (0, 180) else EXPECTED_SIZE[2]
        rotated_depth = EXPECTED_SIZE[2] if rotation in (0, 180) else EXPECTED_SIZE[0]
        if any(
            not (
                0 <= x < rotated_width
                and 0 <= y < EXPECTED_SIZE[1]
                and 0 <= z < rotated_depth
            )
            for x, y, z in rotated
        ):
            raise ValueError(
                f"{culture}: out-of-bounds block at {rotation} degrees"
            )

    print(
        f"PASS {culture}: {len(blocks)} blocks, {len(palette)} states, "
        f"10 plants, {grapevine_count} vine blocks, 1 {expected_press}, "
        "0 composters, rotations 0/90/180/270"
    )
    return counts


def main() -> None:
    for culture, wood in CULTURES.items():
        audit_structure(culture, wood)
    print("Village vineyard structure audit: PASS (5/5 templates)")


if __name__ == "__main__":
    main()
