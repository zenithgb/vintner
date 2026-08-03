#!/usr/bin/env python3
"""Generate the modular Surveyor's Map Table assets for every wood family."""

from __future__ import annotations

import json
from pathlib import Path

from generate_wood_variants import WOODS


ROOT = Path(__file__).resolve().parents[1]
ASSETS = ROOT / "src/main/resources/assets/vintner"
DATA = ROOT / "src/main/resources/data/vintner"
ROTATIONS = {"north": 0, "east": 90, "south": 180, "west": 270}


def write_json(path: Path, value: object) -> None:
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(value, indent=2) + "\n")


def block_id(wood: str) -> str:
    return (
        "surveyors_map_table"
        if wood == "oak"
        else f"{wood}_surveyors_map_table"
    )


def faces(texture: str) -> dict[str, dict[str, str]]:
    return {
        side: {"texture": texture}
        for side in ("north", "east", "south", "west", "up", "down")
    }


def cube(start: list[float], end: list[float], texture: str) -> dict:
    return {"from": start, "to": end, "faces": faces(texture)}


def base_model() -> dict:
    elements = []
    # A fitted atlas cabinet, rather than a second work table. Its counter and
    # gallery match the Estate Desk exactly so either side can join cleanly.
    for x0, x1 in ((1.0, 2.5), (13.5, 15.0)):
        for z0, z1 in ((1.5, 3.0), (12.5, 14.0)):
            elements.append(cube([x0, 0, z0], [x1, 10.25, z1], "#wood"))
    elements.extend((
        cube([1.25, 3.25, 3], [2.75, 4.25, 12.5], "#wood"),
        cube([13.25, 3.25, 3], [14.75, 4.25, 12.5], "#wood"),
        cube([0, 10.25, 0], [16, 11.5, 16], "#wood"),
        # Three shallow labelled map drawers sit below the shared worktop.
        cube([1.5, 6.75, 1], [14.5, 10.25, 3], "#wood"),
        cube([2, 7.25, 0.65], [5.6, 9.5, 1.1], "#drawer"),
        cube([6.2, 7.25, 0.65], [9.8, 9.5, 1.1], "#drawer"),
        cube([10.4, 7.25, 0.65], [14, 9.5, 1.1], "#drawer"),
        cube([3.5, 8.55, 0.4], [4.1, 9, 0.7], "#brass"),
        cube([7.7, 8.55, 0.4], [8.3, 9, 0.7], "#brass"),
        cube([11.9, 8.55, 0.4], [12.5, 9, 0.7], "#brass"),
        # The rear roll gallery continues the desk's document gallery.
        cube([1, 11.5, 13.25], [15, 14.25, 14.75], "#wood"),
        cube([0.75, 14.25, 13], [15.25, 14.75, 15], "#wood"),
        cube([1.25, 11.5, 10.75], [2.25, 14.25, 14.25], "#wood"),
        cube([13.75, 11.5, 10.75], [14.75, 14.25, 14.25], "#wood"),
        cube([5.25, 11.75, 12.75], [5.85, 13.75, 14], "#brass"),
        cube([10.15, 11.75, 12.75], [10.75, 13.75, 14], "#brass"),
    ))
    return {
        "parent": "minecraft:block/block",
        "textures": {
            "wood": "minecraft:block/oak_planks",
            "drawer": "minecraft:block/stripped_oak_log",
            "brass": "minecraft:block/raw_gold_block",
            "particle": "minecraft:block/oak_planks",
        },
        "elements": elements,
        "display": {
            "gui": {
                "rotation": [30, 225, 0],
                "translation": [0, -1, 0],
                "scale": [0.68, 0.68, 0.68],
            },
            "ground": {"translation": [0, 3, 0], "scale": [0.25, 0.25, 0.25]},
            "fixed": {"rotation": [0, 180, 0], "scale": [0.5, 0.5, 0.5]},
        },
    }


def map_overlay_model() -> dict:
    return {
        "parent": "minecraft:block/block",
        "textures": {
            "paper": "minecraft:block/bone_block_side",
            "ink": "minecraft:block/blue_terracotta",
            "leather": "minecraft:block/brown_wool",
            "particle": "minecraft:block/oak_planks",
        },
        "elements": [
            # Filed survey rolls live in the gallery; the desk remains the
            # only visible place where maps are handled and reviewed.
            cube([2.5, 11.9, 12.6], [4.5, 13.8, 13.4], "#paper"),
            cube([6.8, 11.9, 12.6], [8.8, 13.8, 13.4], "#paper"),
            cube([11.3, 11.9, 12.6], [13.3, 13.8, 13.4], "#paper"),
            cube([2.35, 12.55, 12.45], [4.65, 12.9, 13.55], "#ink"),
            cube([6.65, 12.55, 12.45], [8.95, 12.9, 13.55], "#ink"),
            cube([11.15, 12.55, 12.45], [13.45, 12.9, 13.55], "#ink"),
        ],
    }


def blockstate(model: str) -> dict:
    multipart = []
    for facing, rotation in ROTATIONS.items():
        base_apply = {"model": f"vintner:block/{model}", "uvlock": True}
        map_apply = {
            "model": "vintner:block/surveyors_map_table_maps",
            "uvlock": True,
        }
        if rotation:
            base_apply["y"] = rotation
            map_apply["y"] = rotation
        multipart.append({"when": {"facing": facing}, "apply": base_apply})
        multipart.append({
            "when": {"facing": facing, "has_maps": "true"},
            "apply": map_apply,
        })
    return {"multipart": multipart}


def loot_table(identifier: str) -> dict:
    return {
        "type": "minecraft:block",
        "pools": [{
            "rolls": 1,
            "entries": [{
                "type": "minecraft:item",
                "name": f"vintner:{identifier}",
            }],
            "conditions": [{"condition": "minecraft:survives_explosion"}],
        }],
    }


def recipe_advancement(identifier: str, planks: str) -> dict:
    return {
        "parent": "minecraft:recipes/root",
        "criteria": {
            "has_planks": {
                "trigger": "minecraft:inventory_changed",
                "conditions": {"items": [{"items": planks}]},
            },
            "has_the_recipe": {
                "trigger": "minecraft:recipe_unlocked",
                "conditions": {"recipe": f"vintner:{identifier}"},
            },
        },
        "requirements": [["has_planks", "has_the_recipe"]],
        "rewards": {"recipes": [f"vintner:{identifier}"]},
    }


def main() -> None:
    write_json(ASSETS / "models/block/surveyors_map_table.json", base_model())
    write_json(
        ASSETS / "models/block/surveyors_map_table_maps.json",
        map_overlay_model(),
    )
    lang_path = ASSETS / "lang/en_us.json"
    lang = json.loads(lang_path.read_text())
    axe_path = ROOT / "src/main/resources/data/minecraft/tags/block/mineable/axe.json"
    axe = json.loads(axe_path.read_text())

    for wood, properties in WOODS.items():
        identifier = block_id(wood)
        if wood != "oak":
            write_json(
                ASSETS / f"models/block/{identifier}.json",
                {
                    "parent": "vintner:block/surveyors_map_table",
                    "textures": {
                        "wood": f"minecraft:block/{wood}_planks",
                        "drawer": properties["beam"],
                        "particle": f"minecraft:block/{wood}_planks",
                    },
                },
            )
        write_json(ASSETS / f"blockstates/{identifier}.json", blockstate(identifier))
        write_json(
            ASSETS / f"models/item/{identifier}.json",
            {"parent": f"vintner:block/{identifier}"},
        )
        write_json(
            ASSETS / f"items/{identifier}.json",
            {"model": {"type": "minecraft:model", "model": f"vintner:item/{identifier}"}},
        )
        write_json(DATA / f"loot_table/blocks/{identifier}.json", loot_table(identifier))
        planks = f"minecraft:{wood}_planks"
        slab = f"minecraft:{wood}_slab"
        write_json(
            DATA / f"recipe/{identifier}.json",
            {
                "type": "minecraft:crafting_shaped",
                "category": "misc",
                "pattern": ["PMP", "PCP", "S S"],
                "key": {
                    "P": planks,
                    "M": "minecraft:map",
                    "C": "minecraft:cartography_table",
                    "S": slab,
                },
                "result": {"id": f"vintner:{identifier}", "count": 1},
            },
        )
        write_json(
            DATA / f"advancement/recipes/vintner/{identifier}.json",
            recipe_advancement(identifier, planks),
        )
        lang[f"block.vintner.{identifier}"] = (
            f"{properties['title']} Surveyor's Map Table"
        )
        namespaced = f"vintner:{identifier}"
        if namespaced not in axe["values"]:
            axe["values"].append(namespaced)

    write_json(lang_path, lang)
    write_json(axe_path, axe)
    print(f"Generated Surveyor's Map Table assets for {len(WOODS)} wood families.")


if __name__ == "__main__":
    main()
