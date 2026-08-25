#!/usr/bin/env python3
"""Generate the compact baked-model tasting service and its visual states."""

from __future__ import annotations

import json
from pathlib import Path


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
    "cork": "minecraft:block/stripped_spruce_log",
    "red_label": "minecraft:block/red_terracotta",
    "white_label": "minecraft:block/yellow_terracotta",
    "red_wine": "minecraft:block/red_concrete",
    "white_wine": "minecraft:block/yellow_terracotta",
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


def cup(center_x: float, center_z: float) -> list[dict[str, object]]:
    """A small square ceramic tasting cup with a clearly open centre."""
    x0 = center_x - 1.0
    x1 = center_x + 1.0
    z0 = center_z - 1.0
    z1 = center_z + 1.0
    return [
        cube([x0, 2.0, z0], [x1, 3.35, z0 + 0.3], "ceramic"),
        cube([x0, 2.0, z1 - 0.3], [x1, 3.35, z1], "ceramic"),
        cube([x0, 2.0, z0 + 0.3], [x0 + 0.3, 3.35, z1 - 0.3], "ceramic"),
        cube([x1 - 0.3, 2.0, z0 + 0.3], [x1, 3.35, z1 - 0.3], "ceramic"),
        cube([x0 + 0.25, 1.85, z0 + 0.25], [x1 - 0.25, 2.1, z1 - 0.25], "ceramic"),
    ]


CUP_CENTERS = ((3.0, 5.0), (6.35, 5.0), (9.65, 5.0), (13.0, 5.0))

BASE_ELEMENTS = [
    cube([1.0, 0.0, 1.0], [15.0, 1.0, 15.0], "wood"),
    cube([1.0, 1.0, 1.0], [15.0, 2.0, 1.6], "rim"),
    cube([1.0, 1.0, 14.4], [15.0, 2.0, 15.0], "rim"),
    cube([1.0, 1.0, 1.6], [1.6, 2.0, 14.4], "rim"),
    cube([14.4, 1.0, 1.6], [15.0, 2.0, 14.4], "rim"),
    cube([2.0, 1.05, 8.0], [6.0, 1.35, 13.5], "cloth"),
]

for center in CUP_CENTERS:
    BASE_ELEMENTS.extend(cup(*center))


def bottle_elements(label: str) -> list[dict[str, object]]:
    return [
        cube([9.6, 2.0, 9.4], [13.4, 7.0, 13.2], "bottle"),
        cube([10.2, 7.0, 10.0], [12.8, 8.2, 12.6], "bottle"),
        cube([10.8, 8.2, 10.6], [12.2, 10.8, 12.0], "bottle"),
        cube([10.65, 10.8, 10.45], [12.35, 11.5, 12.15], "cork"),
        cube([10.35, 4.0, 9.2], [12.65, 6.2, 9.45], label),
    ]


def fill_elements(texture: str, count: int) -> list[dict[str, object]]:
    result = []
    for center_x, center_z in CUP_CENTERS[:count]:
        result.append(
            cube(
                [center_x - 0.65, 3.0, center_z - 0.65],
                [center_x + 0.65, 3.18, center_z + 0.65],
                texture,
            )
        )
    return result


def model(elements: list[dict[str, object]]) -> dict[str, object]:
    return {
        "parent": "minecraft:block/block",
        "ambientocclusion": False,
        "textures": TEXTURES,
        "elements": elements,
    }


BLOCK_MODELS.mkdir(parents=True, exist_ok=True)
ITEM_MODELS.mkdir(parents=True, exist_ok=True)
BLOCKSTATES.mkdir(parents=True, exist_ok=True)

generated: dict[Path, dict[str, object]] = {
    BLOCK_MODELS / "tasting_service_base.json": model(BASE_ELEMENTS),
    BLOCK_MODELS / "tasting_service_bottle_red.json": model(
        bottle_elements("red_label")
    ),
    BLOCK_MODELS / "tasting_service_bottle_white.json": model(
        bottle_elements("white_label")
    ),
}

for colour, texture in (("red", "red_wine"), ("white", "white_wine")):
    for count in range(1, 5):
        generated[
            BLOCK_MODELS / f"tasting_service_fill_{colour}_{count}.json"
        ] = model(fill_elements(texture, count))

display_elements = (
    BASE_ELEMENTS
    + bottle_elements("red_label")
    + fill_elements("red_wine", 4)
)
generated[BLOCK_MODELS / "tasting_service_display.json"] = model(
    display_elements
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
