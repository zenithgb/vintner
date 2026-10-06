#!/usr/bin/env python3
"""Audit terrain support below authored vineyard soil in a saved village.

The world must be fully stopped and the village chunks fully generated.
Usage: python3 scripts/audit_vineyard_support.py WORLD CULTURE X Z
"""

from __future__ import annotations

import argparse
import gzip
import json
import zlib
from collections import Counter
from functools import lru_cache
from pathlib import Path

from audit_village_vineyards import load_structure, transformed
from structure_nbt import decode_root


AIR = {"minecraft:air", "minecraft:cave_air", "minecraft:void_air"}
LIQUIDS = {"minecraft:water", "minecraft:lava"}
REPLACEABLE = AIR | LIQUIDS | {
    "minecraft:short_grass",
    "minecraft:tall_grass",
    "minecraft:fern",
    "minecraft:large_fern",
    "minecraft:dead_bush",
    "minecraft:snow",
}
PAIRED = {"savanna", "taiga"}
ROTATIONS = {
    "NONE": 0,
    "CLOCKWISE_90": 90,
    "CLOCKWISE_180": 180,
    "COUNTERCLOCKWISE_90": 270,
}
ROOT = Path(__file__).resolve().parents[1]


def main() -> int:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("world", type=Path)
    parser.add_argument("culture", choices=sorted(PAIRED))
    parser.add_argument("x", type=int)
    parser.add_argument("z", type=int)
    parser.add_argument("--max-depth", type=int, default=128)
    args = parser.parse_args()

    region = args.world / "dimensions/minecraft/overworld/region"
    if not region.is_dir():
        parser.error(f"missing overworld region directory: {region}")

    @lru_cache(maxsize=512)
    def chunk(cx: int, cz: int):
        path = region / f"r.{cx >> 5}.{cz >> 5}.mca"
        if not path.is_file():
            return None
        data = path.read_bytes()
        index = (cx & 31) + (cz & 31) * 32
        entry = int.from_bytes(data[index * 4:index * 4 + 4], "big")
        if not entry:
            return None
        offset = (entry >> 8) * 4096
        length = int.from_bytes(data[offset:offset + 4], "big")
        kind = data[offset + 4]
        payload = data[offset + 5:offset + 4 + length]
        raw = zlib.decompress(payload) if kind == 2 else (
            gzip.decompress(payload) if kind == 1 else payload)
        return decode_root(raw)

    def block(x: int, y: int, z: int) -> str:
        data = chunk(x >> 4, z >> 4)
        if data is None or data["Status"][1] != "minecraft:full":
            return "MISSING_CHUNK"
        section = next((item for item in data["sections"][1][1]
                        if item["Y"][1] == y >> 4), None)
        if section is None:
            return "minecraft:air"
        states = section["block_states"][1]
        palette_type, palette = states["palette"][1]
        names = (palette if palette_type == 8 else
                 [entry.get("id", entry.get("Name", entry.get("")))[1]
                  for entry in palette])
        if len(names) == 1:
            return names[0]
        bits = max(4, (len(names) - 1).bit_length())
        packed = 64 // bits
        index = ((y & 15) << 8) | ((z & 15) << 4) | (x & 15)
        value = (states["data"][1][index // packed]
                 & ((1 << 64) - 1)) >> ((index % packed) * bits)
        return names[value & ((1 << bits) - 1)]

    start = chunk(args.x >> 4, args.z >> 4)
    if start is None:
        parser.error("village start chunk is absent")
    structure_id = f"minecraft:village_{args.culture}"
    try:
        pieces = (start["structures"][1]["starts"][1]
                  [structure_id][1]["Children"][1][1])
    except KeyError:
        parser.error(f"{structure_id} start is absent at [{args.x}, {args.z}]")

    template_id = f"vintner:village/{args.culture}/vineyard_field_tier1"
    matches = []
    for piece in pieces:
        element = piece.get("pool_element")
        if element is None:
            continue
        if element[1].get("location", (None, None))[1] == template_id:
            matches.append(piece)
    if len(matches) != 1:
        parser.error(f"expected one {template_id} piece; found {len(matches)}")
    piece = matches[0]

    template_path = (ROOT / "src/main/resources/data/vintner/structure"
                     / f"village/{args.culture}/vineyard_field_tier1.nbt")
    model = load_structure(template_path)
    rotation_name = piece["rotation"][1]
    rotation = ROTATIONS[rotation_name]
    box = piece["BB"][1]
    origin = [piece["PosX"][1], piece["PosY"][1], piece["PosZ"][1]]
    soil = []
    for local, (authored, _properties, _nbt) in model["positioned"].items():
        if authored != "vintner:vineyard_soil":
            continue
        dx, dy, dz = transformed(local, rotation, model["size"])
        x, y, z = box[0] + dx, box[1] + dy, box[2] + dz
        below = block(x, y - 1, z)
        air_depth = 0
        liquids = []
        below_profile = [
            {"depth": depth, "block": block(x, y - depth, z)}
            for depth in range(1, 9)
        ]
        first_non_air = None
        first_solid = None
        for depth in range(1, args.max_depth + 1):
            state = block(x, y - depth, z)
            if state in AIR:
                if first_non_air is None:
                    air_depth += 1
                continue
            if first_non_air is None:
                first_non_air = {"depth": depth, "block": state}
            if state in LIQUIDS:
                liquids.append({"depth": depth, "block": state})
                continue
            if state in REPLACEABLE:
                continue
            first_solid = {"depth": depth, "block": state}
            break
        open_sides = []
        for name, ox, oz in (("west", -1, 0), ("east", 1, 0),
                             ("north", 0, -1), ("south", 0, 1)):
            if block(x + ox, y - 1, z + oz) in AIR:
                open_sides.append(name)
        soil.append({
            "local": list(local),
            "world": [x, y, z],
            "below": below,
            "consecutive_air_depth": air_depth,
            "first_non_air": first_non_air,
            "first_solid": first_solid,
            "liquids": liquids,
            "below_profile": below_profile,
            "ravine_exposed": bool(open_sides),
            "open_sides": open_sides,
        })

    def depth_bucket(depth: int) -> str:
        if depth == 0:
            return "supported_immediately"
        if depth == 1:
            return "1_block_gap"
        if depth == 2:
            return "2_block_gap"
        if depth <= 4:
            return "3_4_block_gap"
        if depth <= 8:
            return "5_8_block_gap"
        return "deeper_or_open_ravine"

    distribution = Counter(depth_bucket(row["consecutive_air_depth"])
                           for row in soil)
    exact_depths = Counter(str(row["consecutive_air_depth"]) for row in soil)
    chunks = sorted({(row["world"][0] >> 4, row["world"][2] >> 4)
                     for row in soil})
    result = {
        "world": str(args.world),
        "culture": args.culture,
        "village": [args.x, args.z],
        "runtime_structure_id": structure_id,
        "source_structure_id": template_id,
        "pool_element_type": piece["pool_element"][1]["element_type"][1],
        "projection": piece["pool_element"][1]["projection"][1],
        "piece_type": piece["id"][1],
        "field_origin": origin,
        "field_bounds": box,
        "rotation": rotation_name,
        "ground_level_delta": piece["ground_level_delta"][1],
        "intersected_chunks": [list(value) for value in chunks],
        "authored_soil_count": len(soil),
        "missing_chunk_count": sum(
            row["below"] == "MISSING_CHUNK"
            or any(value["block"] == "MISSING_CHUNK"
                   for value in row["below_profile"])
            for row in soil
        ),
        "unsupported_immediate_count": sum(row["below"] in REPLACEABLE
                                           for row in soil),
        "liquid_column_count": sum(bool(row["liquids"]) for row in soil),
        "ravine_exposed_count": sum(row["ravine_exposed"] for row in soil),
        "maximum_air_depth": max(row["consecutive_air_depth"] for row in soil),
        "depth_distribution": dict(distribution),
        "exact_air_depths": dict(sorted(exact_depths.items(),
                                        key=lambda item: int(item[0]))),
        "soil": soil,
    }
    print(json.dumps(result, indent=2))
    return 1 if (result["unsupported_immediate_count"]
                 or result["missing_chunk_count"]) else 0


if __name__ == "__main__":
    raise SystemExit(main())
