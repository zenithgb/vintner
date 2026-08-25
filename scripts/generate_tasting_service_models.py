#!/usr/bin/env python3
"""Generate the compact baked-model tasting service and its visual states."""

from __future__ import annotations

import json
from pathlib import Path

from generate_wood_variants import (
    bottle_elements as canonical_bottle_elements,
)


ROOT = Path(__file__).resolve().parents[1]
ASSETS = ROOT / "src/main/resources/assets/vintner"
BLOCK_MODELS = ASSETS / "models/block"
ITEM_MODELS = ASSETS / "models/item"
BLOCKSTATES = ASSETS / "blockstates"

TEXTURES = {
    "wood": "minecraft:block/oak_planks",
    "rim": "minecraft:block/stripped_oak_log",
    "ceramic": "minecraft:block/white_terracotta",
    "cloth": "minecraft:block/red_wool",
    "bottle": "minecraft:block/green_terracotta",
    "bottle_dark": "minecraft:block/green_concrete",
    "bottle_highlight": "minecraft:block/lime_terracotta",
    "cork": "minecraft:block/stripped_spruce_log",
    "label": "minecraft:block/smooth_sandstone",
    "label_border": "minecraft:block/brown_terracotta",
    "label_ink": "minecraft:block/brown_concrete",
    "neck_foil": "minecraft:block/red_terracotta",
    "seal": "minecraft:block/red_terracotta",
    "red_seal": "minecraft:block/red_terracotta",
    "white_seal": "vintner:block/white_wine",
    "red_wine": "minecraft:block/red_concrete",
    "white_wine": "vintner:block/white_wine",
    "particle": "minecraft:block/oak_planks",
}


def faces(texture: str) -> dict[str, dict[str, str]]:
    return {
        direction: {"texture": f"#{texture}"}
        for direction in ("north", "east", "south", "west", "up", "down")
    }


def cube(
    start: list[float],
    end: list[float],
    texture: str,
) -> dict[str, object]:
    return {
        "from": start,
        "to": end,
        "faces": faces(texture),
    }


def rotated_cube(
    start: list[float],
    end: list[float],
    texture: str,
    origin: list[float],
    angle: float,
) -> dict[str, object]:
    element = cube(start, end, texture)
    element["rotation"] = {
        "origin": origin,
        "axis": "y",
        "angle": angle,
        "rescale": False,
    }
    return element


def cup(center_x: float, center_z: float) -> list[dict[str, object]]:
    """A compact chamfered ceramic tasting cup with an open centre."""
    y0 = 2.0
    y1 = 3.1
    half = 0.78
    straight = 0.43
    thickness = 0.22
    corner_offset = 0.57
    corner_half_length = 0.29
    corner_half_width = 0.11

    elements = [
        cube(
            [center_x - straight, y0, center_z - half],
            [center_x + straight, y1, center_z - half + thickness],
            "ceramic",
        ),
        cube(
            [center_x - straight, y0, center_z + half - thickness],
            [center_x + straight, y1, center_z + half],
            "ceramic",
        ),
        cube(
            [center_x - half, y0, center_z - straight],
            [center_x - half + thickness, y1, center_z + straight],
            "ceramic",
        ),
        cube(
            [center_x + half - thickness, y0, center_z - straight],
            [center_x + half, y1, center_z + straight],
            "ceramic",
        ),
        cube(
            [center_x - 0.48, 1.84, center_z - 0.48],
            [center_x + 0.48, 2.06, center_z + 0.48],
            "ceramic",
        ),
    ]

    for offset_x, offset_z, angle in (
        (-corner_offset, -corner_offset, 45.0),
        (corner_offset, -corner_offset, -45.0),
        (-corner_offset, corner_offset, -45.0),
        (corner_offset, corner_offset, 45.0),
    ):
        origin = [center_x + offset_x, (y0 + y1) / 2, center_z + offset_z]
        elements.append(
            rotated_cube(
                [
                    origin[0] - corner_half_length,
                    y0,
                    origin[2] - corner_half_width,
                ],
                [
                    origin[0] + corner_half_length,
                    y1,
                    origin[2] + corner_half_width,
                ],
                "ceramic",
                origin,
                angle,
            )
        )

    return elements


CUP_CENTERS = ((3.0, 5.0), (6.35, 5.0), (9.65, 5.0), (13.0, 5.0))

BASE_ELEMENTS = [
    cube([1.0, 0.0, 1.0], [15.0, 1.0, 15.0], "wood"),
    cube([1.0, 1.0, 1.0], [15.0, 2.0, 1.6], "rim"),
    cube([1.0, 1.0, 14.4], [15.0, 2.0, 15.0], "rim"),
    cube([1.0, 1.0, 1.6], [1.6, 2.0, 14.4], "rim"),
    cube([14.4, 1.0, 1.6], [15.0, 2.0, 14.4], "rim"),
    cube([2.15, 1.04, 9.6], [5.65, 1.2, 13.75], "cloth"),
]

for center in CUP_CENTERS:
    BASE_ELEMENTS.extend(cup(*center))


def bottle_elements(colour: str) -> list[dict[str, object]]:
    """Use the same bottle silhouette as every Vintner storage display."""
    return canonical_bottle_elements(
        11.5,
        2.0,
        11.3,
        0.74,
        include_seal=True,
        profile=colour,
    )


def fill_elements(texture: str, count: int) -> list[dict[str, object]]:
    result = []
    for center_x, center_z in CUP_CENTERS[:count]:
        result.extend(
            [
                cube(
                    [center_x - 0.5, 2.88, center_z - 0.5],
                    [center_x + 0.5, 3.04, center_z + 0.5],
                    texture,
                ),
                cube(
                    [center_x - 0.62, 2.88, center_z - 0.3],
                    [center_x + 0.62, 3.04, center_z + 0.3],
                    texture,
                ),
                cube(
                    [center_x - 0.3, 2.88, center_z - 0.62],
                    [center_x + 0.3, 3.04, center_z + 0.62],
                    texture,
                ),
            ]
        )
    return result


def model(
    elements: list[dict[str, object]],
    *,
    seal_texture: str | None = None,
    neck_foil_texture: str | None = None,
) -> dict[str, object]:
    textures = dict(TEXTURES)
    if seal_texture is not None:
        textures["seal"] = seal_texture
    if neck_foil_texture is not None:
        textures["neck_foil"] = neck_foil_texture
    return {
        "parent": "minecraft:block/block",
        "ambientocclusion": False,
        "textures": textures,
        "elements": elements,
    }


BLOCK_MODELS.mkdir(parents=True, exist_ok=True)
ITEM_MODELS.mkdir(parents=True, exist_ok=True)
BLOCKSTATES.mkdir(parents=True, exist_ok=True)

generated: dict[Path, dict[str, object]] = {
    BLOCK_MODELS / "tasting_service_base.json": model(BASE_ELEMENTS),
    BLOCK_MODELS / "tasting_service_bottle_red.json": model(
        bottle_elements("red"),
        seal_texture=TEXTURES["red_seal"],
    ),
    BLOCK_MODELS / "tasting_service_bottle_white.json": model(
        bottle_elements("white"),
        seal_texture=TEXTURES["white_seal"],
        neck_foil_texture=TEXTURES["white_seal"],
    ),
}

for colour, texture in (("red", "red_wine"), ("white", "white_wine")):
    for count in range(1, 5):
        generated[
            BLOCK_MODELS / f"tasting_service_fill_{colour}_{count}.json"
        ] = model(fill_elements(texture, count))

display_elements = (
    BASE_ELEMENTS
    + bottle_elements("red")
    + fill_elements("red_wine", 4)
)
generated[BLOCK_MODELS / "tasting_service_display.json"] = model(
    display_elements,
    seal_texture=TEXTURES["red_seal"],
)
generated[ITEM_MODELS / "tasting_service.json"] = {
    "parent": "vintner:block/tasting_service_display",
    "display": {
        "gui": {
            "rotation": [30, 225, 0],
            "translation": [0, 0, 0],
            "scale": [0.62, 0.62, 0.62],
        },
        "ground": {
            "translation": [0, 3, 0],
            "scale": [0.35, 0.35, 0.35],
        },
        "fixed": {"scale": [0.55, 0.55, 0.55]},
    },
}

for path, contents in generated.items():
    path.write_text(json.dumps(contents, indent=2) + "\n")

rotations = {"north": 0, "east": 90, "south": 180, "west": 270}
multipart: list[dict[str, object]] = []

for facing, y_rotation in rotations.items():
    apply: dict[str, object] = {
        "model": "vintner:block/tasting_service_base",
        "uvlock": True,
    }
    if y_rotation:
        apply["y"] = y_rotation
    multipart.append({"when": {"facing": facing}, "apply": apply})

    for colour, white_value in (("red", "false"), ("white", "true")):
        bottle_apply: dict[str, object] = {
            "model": f"vintner:block/tasting_service_bottle_{colour}",
            "uvlock": True,
        }
        if y_rotation:
            bottle_apply["y"] = y_rotation
        multipart.append(
            {
                "when": {
                    "facing": facing,
                    "has_bottle": "true",
                    "white_wine": white_value,
                },
                "apply": bottle_apply,
            }
        )

        for count in range(1, 5):
            fill_apply: dict[str, object] = {
                "model": f"vintner:block/tasting_service_fill_{colour}_{count}",
                "uvlock": True,
            }
            if y_rotation:
                fill_apply["y"] = y_rotation
            multipart.append(
                {
                    "when": {
                        "facing": facing,
                        "servings": str(count),
                        "white_wine": white_value,
                    },
                    "apply": fill_apply,
                }
            )

(BLOCKSTATES / "tasting_service.json").write_text(
    json.dumps({"multipart": multipart}, indent=2) + "\n"
)

print(
    "Generated tasting-service base, bottle, four red/white fill states, "
    "display model, and multipart blockstate."
)
