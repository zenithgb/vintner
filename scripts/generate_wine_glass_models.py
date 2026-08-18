#!/usr/bin/env python3
"""Generate the placed wine-glass models from a compact octagonal profile."""

from __future__ import annotations

import json
import math
from pathlib import Path


ROOT = Path(__file__).resolve().parents[1]
MODEL_DIR = ROOT / "src/main/resources/assets/vintner/models/item"
FACES = {
    direction: {"texture": "#glass"}
    for direction in ("north", "east", "south", "west", "up", "down")
}
WINE_FACES = {
    direction: {"texture": "#wine"}
    for direction in ("north", "east", "south", "west", "up", "down")
}


def clean(value: float) -> float:
    return round(value, 4)


def element(
    start: list[float],
    end: list[float],
    *,
    wine: bool = False,
    rotation: dict[str, object] | None = None,
) -> dict[str, object]:
    result: dict[str, object] = {
        "from": [clean(value) for value in start],
        "to": [clean(value) for value in end],
        "faces": WINE_FACES if wine else FACES,
    }
    if rotation is not None:
        result["rotation"] = rotation
    return result


def octagonal_wall(
    y0: float,
    y1: float,
    diameter: float,
    thickness: float = 0.18,
) -> list[dict[str, object]]:
    radius = diameter / 2.0
    side = 2.0 * radius * math.tan(math.pi / 8.0)
    corner = radius / math.sqrt(2.0)
    half_side = side / 2.0
    half_thickness = thickness / 2.0
    parts = [
        element(
            [8.0 - half_side, y0, 8.0 - radius - half_thickness],
            [8.0 + half_side, y1, 8.0 - radius + half_thickness],
        ),
        element(
            [8.0 - half_side, y0, 8.0 + radius - half_thickness],
            [8.0 + half_side, y1, 8.0 + radius + half_thickness],
        ),
        element(
            [8.0 - radius - half_thickness, y0, 8.0 - half_side],
            [8.0 - radius + half_thickness, y1, 8.0 + half_side],
        ),
        element(
            [8.0 + radius - half_thickness, y0, 8.0 - half_side],
            [8.0 + radius + half_thickness, y1, 8.0 + half_side],
        ),
    ]

    diagonal_specs = (
        (-corner, -corner, 45.0),
        (corner, -corner, -45.0),
        (-corner, corner, -45.0),
        (corner, corner, 45.0),
    )
    for x_offset, z_offset, angle in diagonal_specs:
        center_x = 8.0 + x_offset
        center_z = 8.0 + z_offset
        parts.append(
            element(
                [
                    center_x - half_side,
                    y0,
                    center_z - half_thickness,
                ],
                [
                    center_x + half_side,
                    y1,
                    center_z + half_thickness,
                ],
                rotation={
                    "origin": [clean(center_x), clean((y0 + y1) / 2.0), clean(center_z)],
                    "axis": "y",
                    "angle": angle,
                },
            )
        )
    return parts


glass_elements = [
    element([6.6, 1.0, 7.6], [9.4, 1.42, 8.4]),
    element([7.6, 1.0, 6.6], [8.4, 1.42, 9.4]),
    element([7.78, 1.35, 7.78], [8.22, 5.78, 8.22]),
    element([7.3, 5.55, 7.3], [8.7, 6.05, 8.7]),
]

for tier in (
    (5.85, 6.65, 1.55),
    (6.55, 7.35, 1.95),
    (7.25, 8.05, 2.35),
    (7.95, 8.85, 2.75),
    (8.75, 9.8, 3.15),
):
    glass_elements.extend(octagonal_wall(*tier))

glass_model = {
    "parent": "vintner:item/wine_glass_placed_base",
    "elements": glass_elements,
}

wine_model = {
    "parent": "vintner:item/wine_glass_placed_base",
    "elements": [
        element([7.2, 7.65, 7.2], [8.8, 8.0, 8.8], wine=True),
        element([6.95, 8.0, 6.95], [9.05, 8.35, 9.05], wine=True),
    ],
}

base_model = {
    "parent": "minecraft:block/block",
    "ambientocclusion": False,
    "textures": {
        "glass": {
            "force_translucent": True,
            "sprite": "minecraft:block/light_gray_stained_glass",
        },
        "wine": {
            "force_translucent": True,
            "sprite": "minecraft:block/red_stained_glass",
        },
        "particle": "minecraft:block/light_gray_stained_glass",
    },
    "display": {
        "fixed": {
            "rotation": [0, 0, 0],
            "translation": [0, 0, 0],
            "scale": [0.64, 0.64, 0.64],
        }
    },
}

for name, model in (
    ("wine_glass_placed.json", glass_model),
    ("filled_wine_glass_placed_fill.json", wine_model),
    ("wine_glass_placed_base.json", base_model),
):
    (MODEL_DIR / name).write_text(json.dumps(model, indent=2) + "\n")

print(
    "Generated placed wine-glass models "
    f"({len(glass_elements)} glass elements)."
)
