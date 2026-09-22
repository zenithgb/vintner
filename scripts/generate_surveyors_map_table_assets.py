#!/usr/bin/env python3
"""Generate the modular Surveyor's Map Table assets for every wood family."""

from __future__ import annotations

import json
from pathlib import Path

from generate_wood_variants import WOODS
from estate_workstation_geometry import carcass, connection


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
    elements = carcass("#wood")
    elements.extend((
        # Three shallow labelled map drawers face the player.
        cube([1.25, 7.5, 1.25], [14.75, 9.75, 3], "#wood"),
        cube([2, 8, 0.85], [5.6, 9.2, 1.3], "#drawer"),
        cube([6.2, 8, 0.85], [9.8, 9.2, 1.3], "#drawer"),
        cube([10.4, 8, 0.85], [14, 9.2, 1.3], "#drawer"),
        cube([3.5, 8.4, 0.55], [4.1, 8.85, 0.9], "#brass"),
        cube([7.7, 8.4, 0.55], [8.3, 8.85, 0.9], "#brass"),
        cube([11.9, 8.4, 0.55], [12.5, 8.85, 0.9], "#brass"),
        # Raised ruler rail and corner weights identify the surveying station.
        cube([1.25, 11.5, 1.25], [2.25, 12, 2.25], "#brass"),
        cube([13.75, 11.5, 1.25], [14.75, 12, 2.25], "#brass"),
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
            cube([2.0, 11.51, 2.25], [10.25, 11.59, 10.5], "#paper"),
            cube([5.5, 11.6, 4.75], [13.75, 11.68, 13.0], "#paper"),
            cube([2.6, 11.6, 2.85], [3.0, 11.72, 8.9], "#ink"),
            cube([8.75, 11.68, 5.35], [9.15, 11.8, 11.4], "#ink"),
            cube([5.0, 11.68, 8.5], [11.6, 11.8, 8.9], "#ink"),
            cube([12.8, 11.68, 11.65], [14.2, 12.15, 13.05], "#leather"),
        ],
    }


def connection_model(left: bool) -> dict:
    """Join the shared desktop, moulding and complete rear rail."""
    return {
        "parent": "minecraft:block/block",
        "textures": {
            "wood": "minecraft:block/oak_planks",
            "particle": "minecraft:block/oak_planks",
        },
        "elements": connection(left, "#wood"),
    }


def connection_id(identifier: str, left: bool) -> str:
    side = "left" if left else "right"
    return f"{identifier}_connection_{side}"


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
    for prop, left in (
        ("left_connected", True),
        ("right_connected", False),
    ):
        for facing, rotation in ROTATIONS.items():
            apply = {
                "model": f"vintner:block/{connection_id(model, left)}",
                "uvlock": True,
            }
            if rotation:
                apply["y"] = rotation
            multipart.append({
                "when": {"facing": facing, prop: "true"},
                "apply": apply,
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
                "conditions": {"recipes": f"vintner:{identifier}"},
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
    for left in (True, False):
        write_json(
            ASSETS
            / f"models/block/{connection_id('surveyors_map_table', left)}.json",
            connection_model(left),
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
            for left in (True, False):
                write_json(
                    ASSETS
                    / f"models/block/{connection_id(identifier, left)}.json",
                    {
                        "parent": (
                            "vintner:block/"
                            + connection_id("surveyors_map_table", left)
                        ),
                        "textures": {
                            "wood": f"minecraft:block/{wood}_planks",
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
