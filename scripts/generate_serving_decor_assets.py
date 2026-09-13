#!/usr/bin/env python3
"""Generate resources for the cultivar grape bowl and wood wine baskets."""

from __future__ import annotations

import json
from pathlib import Path

from generate_wood_variants import WOODS, write_rgba_texture


ROOT = Path(__file__).resolve().parents[1]
ASSETS = ROOT / "src/main/resources/assets/vintner"
DATA = ROOT / "src/main/resources/data/vintner"

CULTIVARS = {
    "ember_noir": "crimson",
    "vale_pinot": "shaded",
    "suncrest": "sunlit",
    "ironwood_red": "crimson",
    "nightberry": "shaded",
    "river_garnet": "riverside",
    "golden_vale": "golden",
    "frostling": "frosted",
    "greenwake": "golden",
    "silverleaf": "frosted",
    "honeycrest": "honeyed",
    "stoneflower": "stony",
}
PALETTES = tuple(dict.fromkeys(CULTIVARS.values()))
GRAPE_COLORS = {
    "crimson": ((100, 18, 28, 255), (185, 33, 52, 255), (56, 10, 16, 255)),
    "shaded": ((100, 34, 70, 255), (185, 63, 129, 255), (56, 19, 39, 255)),
    "sunlit": ((100, 31, 16, 255), (185, 58, 30, 255), (56, 17, 9, 255)),
    "riverside": ((100, 30, 51, 255), (185, 56, 94, 255), (56, 17, 29, 255)),
    "golden": ((135, 122, 51, 255), (232, 209, 88, 255), (94, 84, 36, 255)),
    "frosted": ((130, 135, 108, 255), (223, 232, 190, 255), (82, 88, 68, 255)),
    "honeyed": ((151, 112, 45, 255), (236, 187, 83, 255), (91, 62, 26, 255)),
    "stony": ((121, 135, 97, 255), (207, 232, 167, 255), (75, 88, 61, 255)),
}


def write_json(path: Path, value: object) -> None:
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(value, indent=2) + "\n")


def cube(
    start: list[float], end: list[float], texture: str,
    *, shade: bool = True,
) -> dict[str, object]:
    return {
        "from": start,
        "to": end,
        "shade": shade,
        "faces": {
            face: {"texture": texture}
            for face in ("down", "up", "north", "south", "west", "east")
        },
    }


def rotated_cube(
    start: list[float], end: list[float], texture: str,
    *, origin: list[float], axis: str, angle: float,
) -> dict[str, object]:
    element = cube(start, end, texture)
    element["rotation"] = {
        "origin": origin,
        "axis": axis,
        "angle": angle,
        "rescale": True,
    }
    return element


def bowl_elements(servings: int) -> list[dict[str, object]]:
    elements = [
        # A stepped octagonal footprint reads as a real bowl instead of a box.
        cube([5, 0, 3.5], [11, 0.8, 12.5], "#base"),
        cube([3.5, 0, 5], [12.5, 0.8, 11], "#base"),
        cube([4, 0.8, 4], [12, 1.45, 12], "#inside"),
        cube([4.1, 1.25, 3], [11.9, 2.8, 4.1], "#bowl"),
        cube([4.1, 1.25, 11.9], [11.9, 2.8, 13], "#bowl"),
        cube([3, 1.25, 4.1], [4.1, 2.8, 11.9], "#bowl"),
        cube([11.9, 1.25, 4.1], [13, 2.8, 11.9], "#bowl"),
        cube([4.0, 2.65, 2.7], [12.0, 3.35, 4.0], "#rim"),
        cube([4.0, 2.65, 12.0], [12.0, 3.35, 13.3], "#rim"),
        cube([2.7, 2.65, 4.0], [4.0, 3.35, 12.0], "#rim"),
        cube([12.0, 2.65, 4.0], [13.3, 3.35, 12.0], "#rim"),
        cube([3.3, 1.5, 3.3], [4.8, 2.9, 4.8], "#bowl"),
        cube([11.2, 1.5, 3.3], [12.7, 2.9, 4.8], "#bowl"),
        cube([3.3, 1.5, 11.2], [4.8, 2.9, 12.7], "#bowl"),
        cube([11.2, 1.5, 11.2], [12.7, 2.9, 12.7], "#bowl"),
    ]
    # Add grapes in balanced pairs so every serving state stays centred.
    grape_positions = (
        (6.0, 6.0, 0.0), (8.2, 8.0, 22.5),
        (8.5, 5.7, -22.5), (5.8, 8.3, 22.5),
        (5.0, 5.2, -22.5), (9.3, 8.9, 0.0),
        (9.5, 4.8, 22.5), (4.8, 9.4, -22.5),
    )
    for x, z, angle in grape_positions[: servings * 2]:
        elements.append(rotated_cube(
            [x, 1.45, z], [x + 1.75, 3.2, z + 1.75], "#grapes",
            origin=[x + 0.875, 1.45, z + 0.875],
            axis="y",
            angle=angle,
        ))
    return elements


def write_grape_texture(palette: str) -> None:
    base, highlight, shadow = GRAPE_COLORS[palette]
    rows = []
    for y in range(16):
        row = []
        for x in range(16):
            value = base
            if (x + y * 3) % 11 == 0:
                value = highlight
            elif (x * 3 + y) % 13 == 0:
                value = shadow
            row.append(value)
        rows.append(row)
    for x, y in ((3, 3), (4, 3), (11, 6), (7, 11)):
        rows[y][x] = highlight
    write_rgba_texture(
        ASSETS / f"textures/block/grape_bowl_{palette}_grapes.png",
        rows,
    )


def display_transforms() -> dict[str, object]:
    return {
        "gui": {
            "rotation": [30, 225, 0],
            "translation": [0, 1, 0],
            "scale": [0.8, 0.8, 0.8],
        },
        "ground": {
            "translation": [0, 3, 0],
            "scale": [0.5, 0.5, 0.5],
        },
        "fixed": {"scale": [0.72, 0.72, 0.72]},
    }


def generate_grape_bowl() -> None:
    variants: dict[str, object] = {}
    for cultivar, palette in CULTIVARS.items():
        for servings in range(5):
            model = (
                "vintner:block/grape_bowl_empty"
                if servings == 0
                else f"vintner:block/grape_bowl_{palette}_{servings}"
            )
            variants[f"cultivar={cultivar},servings={servings}"] = {
                "model": model,
            }
    write_json(ASSETS / "blockstates/grape_bowl.json", {"variants": variants})

    base_textures = {
        "base": "minecraft:block/stripped_oak_log_top",
        "inside": "minecraft:block/oak_planks",
        "bowl": "minecraft:block/oak_planks",
        "rim": "minecraft:block/stripped_oak_log",
        "particle": "minecraft:block/oak_planks",
    }
    write_json(
        ASSETS / "models/block/grape_bowl_empty.json",
        {
            "textures": base_textures,
            "elements": bowl_elements(0),
            "display": display_transforms(),
        },
    )
    for palette in PALETTES:
        write_grape_texture(palette)
        for servings in range(1, 5):
            model_id = f"grape_bowl_{palette}_{servings}"
            write_json(
                ASSETS / f"models/block/{model_id}.json",
                {
                    "textures": {
                        **base_textures,
                        "grapes": f"vintner:block/grape_bowl_{palette}_grapes",
                    },
                    "elements": bowl_elements(servings),
                    "display": display_transforms(),
                },
            )
            write_json(
                ASSETS / f"items/{model_id}.json",
                {
                    "model": {
                        "type": "minecraft:model",
                        "model": f"vintner:block/{model_id}",
                    }
                },
            )
    write_json(
        ASSETS / "items/grape_bowl.json",
        {
            "model": {
                "type": "minecraft:model",
                "model": "vintner:block/grape_bowl_crimson_4",
            }
        },
    )
    for palette in PALETTES:
        write_json(
            ASSETS / f"items/grape_bowl_{palette}_0.json",
            {
                "model": {
                    "type": "minecraft:model",
                    "model": "vintner:block/grape_bowl_empty",
                }
            },
        )

    write_json(
        DATA / "recipe/grape_bowl.json",
        {
            "type": "vintner:grape_bowl",
            "category": "misc",
            "ingredients": ["minecraft:bowl"]
            + ["#vintner:grapes"] * 8,
            "result": {"id": "vintner:grape_bowl", "count": 1},
        },
    )
    write_json(
        DATA / "advancement/recipes/vintner/grape_bowl.json",
        recipe_advancement("grape_bowl", "#vintner:grapes"),
    )
    write_json(
        DATA / "tags/item/grapes.json",
        {
            "replace": False,
            "values": ["vintner:red_grapes", "vintner:white_grapes"],
        },
    )
    write_json(DATA / "loot_table/blocks/grape_bowl.json", self_drop("grape_bowl"))


def wine_basket_id(wood: str) -> str:
    return "wine_basket" if wood == "oak" else f"{wood}_wine_basket"


def basket_elements() -> list[dict[str, object]]:
    return [
        # Stepped ends make a shallow oval cradle rather than a square crate.
        cube([4.0, 0.35, 1.4], [12.0, 1.2, 14.6], "#weave"),
        cube([3.2, 0.35, 2.4], [12.8, 1.2, 13.6], "#weave"),
        cube([4.3, 1.0, 1.2], [11.7, 3.0, 2.25], "#weave"),
        cube([4.3, 1.0, 13.75], [11.7, 3.0, 14.8], "#weave"),
        cube([3.0, 1.0, 2.4], [4.05, 3.35, 13.6], "#weave"),
        cube([11.95, 1.0, 2.4], [13.0, 3.35, 13.6], "#weave"),
        # Broad rounded-looking rim and three slim woven side bands.
        cube([4.1, 2.75, 0.85], [11.9, 3.65, 2.2], "#rim"),
        cube([4.1, 2.75, 13.8], [11.9, 3.65, 15.15], "#rim"),
        cube([2.65, 3.0, 2.3], [4.15, 3.9, 13.7], "#rim"),
        cube([11.85, 3.0, 2.3], [13.35, 3.9, 13.7], "#rim"),
        cube([2.75, 1.35, 2.5], [3.15, 1.7, 13.5], "#binding"),
        cube([2.75, 2.1, 2.5], [3.15, 2.45, 13.5], "#binding"),
        cube([12.85, 1.35, 2.5], [13.25, 1.7, 13.5], "#binding"),
        cube([12.85, 2.1, 2.5], [13.25, 2.45, 13.5], "#binding"),
        # Thin raised handle, arched in four restrained Minecraft segments.
        cube([3.05, 3.2, 7.35], [3.85, 8.1, 8.65], "#rim"),
        cube([12.15, 3.2, 7.35], [12.95, 8.1, 8.65], "#rim"),
        rotated_cube(
            [3.35, 7.5, 7.35], [7.0, 8.3, 8.65], "#rim",
            origin=[3.75, 7.9, 8.0], axis="z", angle=-22.5,
        ),
        rotated_cube(
            [9.0, 7.5, 7.35], [12.65, 8.3, 8.65], "#rim",
            origin=[12.25, 7.9, 8.0], axis="z", angle=22.5,
        ),
        cube([6.35, 9.0, 7.35], [9.65, 9.8, 8.65], "#rim"),
    ]


def generate_wine_basket() -> None:
    rotations = {"north": 0, "east": 90, "south": 180, "west": 270}
    for wood, properties in WOODS.items():
        block_id = wine_basket_id(wood)
        variants: dict[str, object] = {}
        for facing, rotation in rotations.items():
            for occupied in ("false", "true"):
                entry: dict[str, object] = {
                    "model": f"vintner:block/{block_id}"
                }
                if rotation:
                    entry["y"] = rotation
                variants[f"facing={facing},has_bottle={occupied}"] = entry
        write_json(
            ASSETS / f"blockstates/{block_id}.json",
            {"variants": variants},
        )

        planks = f"minecraft:block/{wood}_planks"
        write_json(
            ASSETS / f"models/block/{block_id}.json",
            {
                "ambientocclusion": False,
                "textures": {
                    "weave": planks,
                    "rim": properties["beam"],
                    "binding": planks,
                    "particle": planks,
                },
                "elements": basket_elements(),
                "display": display_transforms(),
            },
        )
        write_json(
            ASSETS / f"items/{block_id}.json",
            {
                "model": {
                    "type": "minecraft:model",
                    "model": f"vintner:block/{block_id}",
                }
            },
        )
        ingredient = f"minecraft:{wood}_planks"
        write_json(
            DATA / f"recipe/{block_id}.json",
            {
                "type": "minecraft:crafting_shaped",
                "category": "misc",
                "pattern": ["P P", "PWP", "WWW"],
                "key": {"P": ingredient, "W": "minecraft:wheat"},
                "result": {"id": f"vintner:{block_id}", "count": 1},
            },
        )
        write_json(
            DATA / f"advancement/recipes/vintner/{block_id}.json",
            recipe_advancement(block_id, ingredient),
        )
        write_json(
            DATA / f"loot_table/blocks/{block_id}.json",
            self_drop(block_id),
        )


def self_drop(block_id: str) -> dict[str, object]:
    return {
        "type": "minecraft:block",
        "pools": [{
            "rolls": 1,
            "entries": [{
                "type": "minecraft:item",
                "name": f"vintner:{block_id}",
                "conditions": [{"condition": "minecraft:survives_explosion"}],
            }],
        }],
    }


def recipe_advancement(recipe_id: str, material: str) -> dict[str, object]:
    return {
        "parent": "minecraft:recipes/root",
        "criteria": {
            "has_material": {
                "conditions": {"items": [{"items": material}]},
                "trigger": "minecraft:inventory_changed",
            },
            "has_the_recipe": {
                "conditions": {"recipe": f"vintner:{recipe_id}"},
                "trigger": "minecraft:recipe_unlocked",
            },
        },
        "requirements": [["has_material", "has_the_recipe"]],
        "rewards": {"recipes": [f"vintner:{recipe_id}"]},
        "sends_telemetry_event": False,
    }


def main() -> None:
    generate_grape_bowl()
    generate_wine_basket()


if __name__ == "__main__":
    main()
