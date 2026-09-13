#!/usr/bin/env python3
"""Generate resources for the cultivar grape bowl and single-bottle basket."""

from __future__ import annotations

import json
import shutil
from pathlib import Path


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


def bowl_elements(servings: int) -> list[dict[str, object]]:
    elements = [
        cube([3, 0, 3], [13, 1, 13], "#bowl"),
        cube([3, 1, 3], [4, 4, 13], "#rim"),
        cube([12, 1, 3], [13, 4, 13], "#rim"),
        cube([4, 1, 3], [12, 4, 4], "#rim"),
        cube([4, 1, 12], [12, 4, 13], "#rim"),
    ]
    grape_positions = (
        (4.2, 4.2), (7.0, 4.0), (9.8, 4.4), (5.4, 6.7),
        (8.3, 6.5), (10.2, 8.2), (6.0, 9.3), (8.7, 9.6),
    )
    for x, z in grape_positions[: servings * 2]:
        elements.append(cube(
            [x, 1.25, z], [x + 2.2, 3.45, z + 2.2], "#grapes",
            shade=False,
        ))
    return elements


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
        "bowl": "minecraft:block/terracotta",
        "rim": "minecraft:block/brown_terracotta",
        "particle": "minecraft:block/terracotta",
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
        block_texture = ASSETS / f"textures/block/grape_bowl_{palette}_grapes.png"
        block_texture.parent.mkdir(parents=True, exist_ok=True)
        shutil.copyfile(
            ASSETS / f"textures/item/{palette}_grapes.png",
            block_texture,
        )
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


def generate_wine_basket() -> None:
    variants: dict[str, object] = {}
    rotations = {"north": 0, "east": 90, "south": 180, "west": 270}
    for facing, rotation in rotations.items():
        for occupied in ("false", "true"):
            entry: dict[str, object] = {"model": "vintner:block/wine_basket"}
            if rotation:
                entry["y"] = rotation
            variants[f"facing={facing},has_bottle={occupied}"] = entry
    write_json(ASSETS / "blockstates/wine_basket.json", {"variants": variants})

    basket_elements = [
        cube([4, 0, 1], [12, 1.25, 15], "#weave"),
        cube([4, 1.25, 1], [5, 3.8, 15], "#weave"),
        cube([11, 1.25, 1], [12, 3.8, 15], "#weave"),
        cube([5, 1.25, 1], [11, 4.8, 2], "#rim"),
        cube([5, 1.25, 14], [11, 4.8, 15], "#rim"),
        cube([3.75, 3.6, 1], [5.25, 4.8, 15], "#rim"),
        cube([10.75, 3.6, 1], [12.25, 4.8, 15], "#rim"),
        cube([4, 1.1, 4.2], [12, 1.65, 5.1], "#binding"),
        cube([4, 1.1, 10.9], [12, 1.65, 11.8], "#binding"),
    ]
    write_json(
        ASSETS / "models/block/wine_basket.json",
        {
            "textures": {
                "weave": "minecraft:block/oak_planks",
                "rim": "minecraft:block/stripped_oak_log",
                "binding": "minecraft:block/hay_block_side",
                "particle": "minecraft:block/oak_planks",
            },
            "elements": basket_elements,
            "display": display_transforms(),
        },
    )
    write_json(
        ASSETS / "items/wine_basket.json",
        {
            "model": {
                "type": "minecraft:model",
                "model": "vintner:block/wine_basket",
            }
        },
    )
    write_json(
        DATA / "recipe/wine_basket.json",
        {
            "type": "minecraft:crafting_shaped",
            "category": "misc",
            "pattern": ["S S", "SWS", "WWW"],
            "key": {"S": "minecraft:stick", "W": "minecraft:wheat"},
            "result": {"id": "vintner:wine_basket", "count": 1},
        },
    )
    write_json(
        DATA / "advancement/recipes/vintner/wine_basket.json",
        recipe_advancement("wine_basket", "minecraft:wheat"),
    )
    write_json(DATA / "loot_table/blocks/wine_basket.json", self_drop("wine_basket"))


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
