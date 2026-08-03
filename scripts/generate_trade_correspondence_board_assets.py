#!/usr/bin/env python3
"""Generate the Trade Correspondence Board for every wood family."""

from __future__ import annotations

import json
from pathlib import Path

from generate_wood_variants import WOODS, trade_correspondence_board_id


ROOT = Path(__file__).resolve().parents[1]
ASSETS = ROOT / "src/main/resources/assets/vintner"
DATA = ROOT / "src/main/resources/data/vintner"
ROTATIONS = {"north": 0, "east": 90, "south": 180, "west": 270}


def write_json(path: Path, value: object) -> None:
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(value, indent=2) + "\n")


def faces(texture: str) -> dict[str, dict[str, str]]:
    return {
        side: {"texture": texture}
        for side in ("north", "east", "south", "west", "up", "down")
    }


def cube(start: list[float], end: list[float], texture: str) -> dict:
    return {"from": start, "to": end, "faces": faces(texture)}


def model() -> dict:
    elements = [
        # Slender writing bureau legs and lower stretcher.
        cube([1, 0, 2], [2.5, 10, 3.5], "#wood"),
        cube([13.5, 0, 2], [15, 10, 3.5], "#wood"),
        cube([1, 0, 12.5], [2.5, 10, 14], "#wood"),
        cube([13.5, 0, 12.5], [15, 10, 14], "#wood"),
        cube([1.5, 2, 2.5], [14.5, 3, 3.5], "#wood"),
        cube([1.5, 2, 12.5], [14.5, 3, 13.5], "#wood"),
        # Sloped correspondence ledge and raised pigeonhole back.
        cube([0.5, 9.5, 1], [15.5, 11.5, 9], "#wood"),
        cube([1, 11.5, 8], [15, 16, 14.5], "#wood"),
        cube([1.5, 12, 7.6], [14.5, 15.5, 8.3], "#dark"),
        cube([5.5, 12, 7.2], [6.2, 15.5, 8.5], "#wood"),
        cube([10, 12, 7.2], [10.7, 15.5, 8.5], "#wood"),
        cube([1.5, 13.5, 7.15], [14.5, 14.1, 8.5], "#wood"),
        # Filed letters, dispatch paper and wax seals on the player side.
        cube([2, 12.2, 7.0], [5, 13.25, 7.25], "#paper"),
        cube([6.4, 14.25, 7.0], [9.8, 15.25, 7.25], "#paper"),
        cube([10.9, 12.2, 7.0], [14, 13.25, 7.25], "#paper"),
        cube([3.2, 12.35, 6.85], [3.8, 12.95, 7.05], "#seal"),
        cube([7.8, 14.4, 6.85], [8.4, 15, 7.05], "#seal"),
        cube([12.1, 12.35, 6.85], [12.7, 12.95, 7.05], "#seal"),
        cube([3, 11.52, 2], [12.5, 11.68, 7.5], "#paper"),
        cube([6.8, 11.69, 4.2], [7.7, 11.9, 5.1], "#seal"),
    ]
    return {
        "parent": "minecraft:block/block",
        "textures": {
            "wood": "minecraft:block/oak_planks",
            "dark": "minecraft:block/stripped_oak_log",
            "paper": "minecraft:block/bone_block_side",
            "seal": "minecraft:block/red_wool",
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


def blockstate(identifier: str) -> dict:
    variants = {}
    for facing, rotation in ROTATIONS.items():
        value = {"model": f"vintner:block/{identifier}", "uvlock": True}
        if rotation:
            value["y"] = rotation
        variants[f"facing={facing}"] = value
    return {"variants": variants}


def loot(identifier: str) -> dict:
    return {
        "type": "minecraft:block",
        "pools": [{
            "rolls": 1,
            "entries": [{"type": "minecraft:item", "name": f"vintner:{identifier}"}],
            "conditions": [{"condition": "minecraft:survives_explosion"}],
        }],
    }


def advancement(identifier: str, planks: str) -> dict:
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
    write_json(ASSETS / "models/block/trade_correspondence_board.json", model())
    lang_path = ASSETS / "lang/en_us.json"
    lang = json.loads(lang_path.read_text())
    axe_path = ROOT / "src/main/resources/data/minecraft/tags/block/mineable/axe.json"
    axe = json.loads(axe_path.read_text())

    for wood, properties in WOODS.items():
        identifier = trade_correspondence_board_id(wood)
        if wood != "oak":
            write_json(
                ASSETS / f"models/block/{identifier}.json",
                {
                    "parent": "vintner:block/trade_correspondence_board",
                    "textures": {
                        "wood": f"minecraft:block/{wood}_planks",
                        "dark": properties["beam"],
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
        write_json(DATA / f"loot_table/blocks/{identifier}.json", loot(identifier))
        planks = f"minecraft:{wood}_planks"
        slab = f"minecraft:{wood}_slab"
        write_json(
            DATA / f"recipe/{identifier}.json",
            {
                "type": "minecraft:crafting_shaped",
                "category": "misc",
                "pattern": ["PQP", "PBP", "S S"],
                "key": {
                    "P": planks,
                    "Q": "minecraft:paper",
                    "B": "minecraft:book",
                    "S": slab,
                },
                "result": {"id": f"vintner:{identifier}", "count": 1},
            },
        )
        write_json(
            DATA / f"advancement/recipes/vintner/{identifier}.json",
            advancement(identifier, planks),
        )
        lang[f"block.vintner.{identifier}"] = (
            f"{properties['title']} Trade Correspondence Board"
        )
        namespaced = f"vintner:{identifier}"
        if namespaced not in axe["values"]:
            axe["values"].append(namespaced)

    write_json(lang_path, lang)
    write_json(axe_path, axe)
    print(f"Generated Trade Correspondence Board assets for {len(WOODS)} wood families.")


if __name__ == "__main__":
    main()
