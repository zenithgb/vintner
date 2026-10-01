#!/usr/bin/env python3
"""Audit saved natural village wineries and their authored physical blocks.

The world must have its village and winery chunks fully generated. The input
JSON is a list of distinct [x, z] coordinates returned by /locate structure.
Usage: python3 scripts/audit_natural_paired_fields.py WORLD CULTURE villages.json
"""

from __future__ import annotations

import argparse
import gzip
import json
import statistics
import zlib
from collections import Counter
from functools import lru_cache
from pathlib import Path

from audit_village_vineyards import load_structure, transformed
from structure_nbt import decode_root

PAIRED = {"savanna", "taiga"}
CULTURES = ("plains", "desert", "savanna", "snowy", "taiga")
ROTATIONS = {"NONE": 0, "CLOCKWISE_90": 90,
             "CLOCKWISE_180": 180, "COUNTERCLOCKWISE_90": 270}
ROOT = Path(__file__).resolve().parents[1]


def main() -> int:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("world", type=Path)
    parser.add_argument("culture", choices=CULTURES)
    parser.add_argument("villages", type=Path)
    args = parser.parse_args()
    region = args.world / "dimensions/minecraft/overworld/region"
    locations = json.loads(args.villages.read_text())
    if not isinstance(locations, list) or not locations:
        parser.error("villages must be a nonempty JSON list of [x, z]")
    if len({tuple(location) for location in locations}) != len(locations):
        parser.error("village coordinates must be distinct")

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
        section = next((section for section in data["sections"][1][1]
                        if section["Y"][1] == y >> 4), None)
        if section is None:
            return "MISSING_SECTION"
        states = section["block_states"][1]
        palette_type, palette = states["palette"][1]
        names = (palette if palette_type == 8 else
                 [entry.get("id", entry.get("Name", entry.get("")))[1]
                  for entry in palette])
        if len(names) == 1:
            return names[0]
        bits = max(4, (len(names) - 1).bit_length())
        index = ((y & 15) << 8) | ((z & 15) << 4) | (x & 15)
        value = (states["data"][1][index // (64 // bits)]
                 & ((1 << 64) - 1)) >> ((index % (64 // bits)) * bits)
        return names[value & ((1 << bits) - 1)]

    prefix = f"vintner:village/{args.culture}/"
    paired = args.culture in PAIRED
    field_id = prefix + ("vineyard_field_tier1" if paired else "vineyard")
    winery_id = prefix + "winery_tier1" if paired else field_id
    model_root = ROOT / f"src/main/resources/data/vintner/structure/village/{args.culture}"
    models = {field_id: load_structure(model_root / f"{field_id.split('/')[-1]}.nbt")}
    if paired:
        models[winery_id] = load_structure(model_root / "winery_tier1.nbt")

    summary = Counter()
    failures = []
    separations = []
    structure = f"minecraft:village_{args.culture}"

    def verify_piece(piece, template: str, village: int) -> None:
        model = models[template]
        box = piece["BB"][1]
        rotation = ROTATIONS[piece["rotation"][1]]
        vine_ids = {name for name in model["counts"]
                    if name.startswith("vintner:") and name.endswith("grapevine")}
        differences = []
        for local, (authored, _properties, _nbt) in model["positioned"].items():
            if not (authored.startswith("vintner:")
                    or (authored.startswith("minecraft:")
                        and authored.endswith("_door"))):
                continue
            dx, dy, dz = transformed(local, rotation, model["size"])
            actual = block(box[0] + dx, box[1] + dy, box[2] + dz)
            allowed = {authored}
            if authored.endswith("_trellis"):
                allowed.update(vine_ids)
                if actual in vine_ids:
                    summary["grown_upper_vines"] += 1
            if actual not in allowed:
                differences.append([list(local), authored, actual])
        if differences:
            summary["incomplete_features"] += 1
            failures.append(f"{village}: {template} {box}: {differences[:20]}")
        else:
            summary["complete_features"] += 1

    for number, location in enumerate(locations, 1):
        if len(location) != 2:
            parser.error(f"invalid village coordinate: {location!r}")
        x, z = map(int, location)
        start = chunk(x >> 4, z >> 4)
        if start is None:
            failures.append(f"{number}: village chunk absent at {location}")
            continue
        try:
            pieces = (start["structures"][1]["starts"][1]
                      [structure][1]["Children"][1][1])
        except KeyError:
            failures.append(f"{number}: village start absent at {location}")
            continue
        selected = {winery_id: [], field_id: []}
        for piece in pieces:
            element = piece.get("pool_element")
            if element is None:
                continue
            name = element[1].get("location", (None, None))[1]
            if name in selected:
                selected[name].append(piece)
        summary["villages"] += 1
        if paired:
            winery, field = selected[winery_id], selected[field_id]
            if not winery and not field:
                continue
            if len(winery) != 1 or len(field) != 1:
                summary["half_or_duplicate_pairs"] += 1
                failures.append(f"{number}: pair at {location}: "
                                f"winery={len(winery)}, field={len(field)}")
                continue
            summary["pairs"] += 1
            first, second = winery[0]["BB"][1], field[0]["BB"][1]
            distance = (((first[0] + first[3] - second[0] - second[3]) / 2) ** 2
                        + ((first[2] + first[5] - second[2] - second[5]) / 2) ** 2) ** 0.5
            separations.append(distance)
            verify_piece(winery[0], winery_id, number)
            verify_piece(field[0], field_id, number)
        else:
            combined = selected[field_id]
            if len(combined) > 1:
                summary["duplicates"] += 1
                failures.append(f"{number}: duplicate winery at {location}")
                continue
            if len(combined) == 1:
                summary["wineries"] += 1
                verify_piece(combined[0], field_id, number)

    if separations:
        summary["separation_min"] = round(min(separations), 1)
        summary["separation_median"] = round(statistics.median(separations), 1)
        summary["separation_max"] = round(max(separations), 1)
    print(json.dumps({"culture": args.culture, "counts": dict(summary),
                      "failures": failures}, indent=2))
    return 1 if failures else 0


if __name__ == "__main__":
    raise SystemExit(main())
