#!/usr/bin/env python3
"""Generate the modular Estate Management Desk block assets."""

from __future__ import annotations

import json
from pathlib import Path


ROOT = Path(__file__).resolve().parents[1]
ASSETS = ROOT / "src/main/resources/assets/vintner"
MODEL_DIR = ASSETS / "models/block"
BLOCKSTATE = ASSETS / "blockstates/estate_management_desk.json"

COLORS = (
    "white",
    "orange",
    "magenta",
    "light_blue",
    "yellow",
    "lime",
    "pink",
    "gray",
    "light_gray",
    "cyan",
    "purple",
    "blue",
    "brown",
    "green",
    "red",
    "black",
)

ROTATIONS = {
    "north": 0,
    "east": 90,
    "south": 180,
    "west": 270,
}


def faces(texture: str, top: str | None = None) -> dict[str, dict[str, str]]:
    result = {
        direction: {"texture": texture}
        for direction in ("north", "east", "south", "west", "down")
    }
    result["up"] = {"texture": top or texture}
    return result


def cube(
    start: list[float],
    end: list[float],
    texture: str,
    top: str | None = None,
    rotation: dict[str, object] | None = None,
) -> dict[str, object]:
    element: dict[str, object] = {
        "from": start,
        "to": end,
        "faces": faces(texture, top),
    }
    if rotation is not None:
        element["rotation"] = rotation
    return element


def write_json(path: Path, data: object) -> None:
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(data, indent=2) + "\n")


def base_model() -> dict[str, object]:
    elements: list[dict[str, object]] = []

    # Four narrow legs with slightly wider feet.
    for x0, x1 in ((1.0, 2.5), (13.5, 15.0)):
        for z0, z1 in ((1.5, 3.0), (12.5, 14.0)):
            elements.append(cube([x0, 0, z0], [x1, 10.25, z1], "#frame"))
            elements.append(cube(
                [x0 - 0.25, 0, z0 - 0.25],
                [x1 + 0.25, 0.5, z1 + 0.25],
                "#frame",
            ))

    # Traditional stretchers keep the silhouette open and furniture-like.
    elements.extend((
        cube([1.25, 3.25, 3], [2.75, 4.25, 12.5], "#frame"),
        cube([13.25, 3.25, 3], [14.75, 4.25, 12.5], "#frame"),
        cube([2.5, 3.25, 1.5], [13.5, 4.25, 3], "#frame"),
        cube([2.5, 3.25, 12.5], [13.5, 4.25, 14], "#frame"),
    ))

    # Writing surface, moulded front edge, and twin shallow drawers.
    elements.extend((
        cube([0.5, 10.25, 1], [15.5, 11.5, 15], "#frame", "#writing"),
        cube([0.25, 9.75, 0.75], [15.75, 10.5, 2.25], "#frame"),
        cube([1.25, 7.5, 1.25], [14.75, 9.75, 3], "#frame"),
        cube([2, 8, 0.85], [7.5, 9.2, 1.3], "#writing"),
        cube([8.5, 8, 0.85], [14, 9.2, 1.3], "#writing"),
        cube([4.55, 8.4, 0.55], [5.25, 8.85, 0.9], "#brass"),
        cube([10.75, 8.4, 0.55], [11.45, 8.85, 0.9], "#brass"),
    ))

    # Raised document gallery at the back of the desk.
    elements.extend((
        cube([1, 11.5, 13.25], [15, 14.25, 14.75], "#frame"),
        cube([0.75, 14.25, 13], [15.25, 14.75, 15], "#frame", "#writing"),
        cube([1.75, 12, 10.75], [14.25, 12.5, 14], "#frame", "#writing"),
        cube([1.25, 11.5, 10.75], [2.25, 14.25, 14.25], "#frame"),
        cube([13.75, 11.5, 10.75], [14.75, 14.25, 14.25], "#frame"),
        cube([7.6, 12.5, 13], [8.4, 14.25, 14.75], "#writing"),
    ))

    # The green leather blotter is part of the crafted default desk.
    elements.append(cube(
        [3.25, 11.5, 3],
        [12.75, 11.65, 9.75],
        "#blotter",
    ))

    # A small inkwell and quill make the base desk recognizable when bare.
    elements.extend((
        cube([1.9, 11.5, 9.8], [3.2, 12.4, 11.1], "#ink"),
        cube([2.1, 12.4, 10], [3, 12.65, 10.9], "#brass"),
        cube(
            [2.45, 12.35, 10.35],
            [2.75, 15.25, 10.65],
            "#quill",
            rotation={
                "origin": [2.6, 12.35, 10.5],
                "axis": "z",
                "angle": -22.5,
            },
        ),
    ))

    return {
        "parent": "minecraft:block/block",
        "textures": {
            "frame": "minecraft:block/dark_oak_planks",
            "writing": "minecraft:block/oak_planks",
            "blotter": "minecraft:block/green_wool",
            "brass": "minecraft:block/raw_gold_block",
            "ink": "minecraft:block/black_wool",
            "quill": "minecraft:block/light_gray_wool",
            "particle": "minecraft:block/dark_oak_planks",
        },
        "elements": elements,
        "display": {
            "gui": {
                "rotation": [30, 225, 0],
                "translation": [0, -1, 0],
                "scale": [0.68, 0.68, 0.68],
            },
            "ground": {
                "translation": [0, 3, 0],
                "scale": [0.25, 0.25, 0.25],
            },
            "fixed": {
                "rotation": [0, 180, 0],
                "scale": [0.5, 0.5, 0.5],
            },
        },
    }


def blotter_model() -> dict[str, object]:
    return {
        "parent": "minecraft:block/block",
        "textures": {
            "blotter": "minecraft:block/green_wool",
            "particle": "minecraft:block/dark_oak_planks",
        },
        "elements": [
            cube([3.25, 11.65, 3], [12.75, 11.72, 9.75], "#blotter")
        ],
    }


def ledger_model() -> dict[str, object]:
    return {
        "parent": "minecraft:block/block",
        "textures": {
            "cover": "minecraft:block/red_wool",
            "pages": "minecraft:block/bone_block_side",
            "spine": "minecraft:block/brown_wool",
            "brass": "minecraft:block/raw_gold_block",
            "particle": "minecraft:block/dark_oak_planks",
        },
        "elements": [
            cube([3.6, 11.72, 4.1], [7.45, 12.05, 8.75], "#cover"),
            cube([3.9, 12.05, 4.35], [7.2, 12.22, 8.5], "#pages"),
            cube([3.45, 11.72, 4.1], [4.05, 12.22, 8.75], "#spine"),
            cube([6.85, 12.22, 6.05], [7.35, 12.34, 6.8], "#brass"),
        ],
    }


def map_model() -> dict[str, object]:
    return {
        "parent": "minecraft:block/block",
        "textures": {
            "paper": "minecraft:block/bone_block_side",
            "edge": "minecraft:block/stripped_birch_log",
            "water": "minecraft:block/blue_wool",
            "land": "minecraft:block/green_wool",
            "mark": "minecraft:block/red_wool",
            "particle": "minecraft:block/dark_oak_planks",
        },
        "elements": [
            cube([8, 11.72, 4], [12.85, 11.86, 9.2], "#paper"),
            cube([8, 11.86, 4], [12.85, 11.92, 4.25], "#edge"),
            cube([8, 11.86, 8.95], [12.85, 11.92, 9.2], "#edge"),
            cube([8, 11.86, 4.25], [8.25, 11.92, 8.95], "#edge"),
            cube([12.6, 11.86, 4.25], [12.85, 11.92, 8.95], "#edge"),
            cube([8.6, 11.92, 4.8], [9.4, 11.98, 7.7], "#water"),
            cube([9.35, 11.92, 6.9], [11.7, 11.98, 8.45], "#land"),
            cube([11.4, 11.92, 5.2], [12.1, 11.98, 6.3], "#land"),
            cube([10.55, 11.98, 7.25], [10.85, 12.04, 7.55], "#mark"),
        ],
    }


def blockstate() -> dict[str, object]:
    multipart: list[dict[str, object]] = []
    for facing, rotation in ROTATIONS.items():
        apply: dict[str, object] = {
            "model": "vintner:block/estate_management_desk",
            "uvlock": True,
        }
        if rotation:
            apply["y"] = rotation
        multipart.append({"when": {"facing": facing}, "apply": apply})

    for prop, model in (
        ("has_ledger", "estate_management_desk_ledger"),
        ("has_map", "estate_management_desk_map"),
    ):
        for facing, rotation in ROTATIONS.items():
            apply = {
                "model": f"vintner:block/{model}",
                "uvlock": True,
            }
            if rotation:
                apply["y"] = rotation
            multipart.append({
                "when": {"facing": facing, prop: "true"},
                "apply": apply,
            })

    # Green is baked into the default model; other colors overlay it.
    for color in COLORS:
        if color == "green":
            continue
        for facing, rotation in ROTATIONS.items():
            apply = {
                "model": f"vintner:block/estate_management_desk_blotter_{color}",
                "uvlock": True,
            }
            if rotation:
                apply["y"] = rotation
            multipart.append({
                "when": {
                    "facing": facing,
                    "blotter_color": color,
                },
                "apply": apply,
            })

    return {"multipart": multipart}


def main() -> None:
    write_json(MODEL_DIR / "estate_management_desk.json", base_model())
    write_json(
        MODEL_DIR / "estate_management_desk_blotter.json",
        blotter_model(),
    )
    write_json(
        MODEL_DIR / "estate_management_desk_ledger.json",
        ledger_model(),
    )
    write_json(
        MODEL_DIR / "estate_management_desk_map.json",
        map_model(),
    )
    for color in COLORS:
        if color == "green":
            continue
        write_json(
            MODEL_DIR / f"estate_management_desk_blotter_{color}.json",
            {
                "parent": "vintner:block/estate_management_desk_blotter",
                "textures": {
                    "blotter": f"minecraft:block/{color}_wool"
                },
            },
        )
    write_json(BLOCKSTATE, blockstate())
    print("Generated the modular Estate Management Desk assets.")


if __name__ == "__main__":
    main()
