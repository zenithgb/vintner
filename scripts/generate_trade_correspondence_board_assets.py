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
        # Fine legs, side rails, and foot-level stretchers keep the bureau open.
        cube([1.25, 0, 1.75], [2.75, 10, 3.5], "#dark"),
        cube([13.25, 0, 1.75], [14.75, 10, 3.5], "#dark"),
        cube([1.25, 0, 12.5], [2.75, 10, 14.25], "#dark"),
        cube([13.25, 0, 12.5], [14.75, 10, 14.25], "#dark"),
        cube([1.75, 2, 2.25], [14.25, 2.75, 3.25], "#wood"),
        cube([1.75, 2, 12.75], [14.25, 2.75, 13.75], "#wood"),
        cube([1.5, 6.25, 2], [2.5, 7, 14], "#wood"),
        cube([13.5, 6.25, 2], [14.5, 7, 14], "#wood"),
        # A small central dispatch drawer gives the bureau a functional front.
        cube([4, 6.75, 1.5], [12, 9.5, 4.25], "#wood"),
        cube([4.5, 7.2, 1.3], [7.75, 9, 1.65], "#dark"),
        cube([8.25, 7.2, 1.3], [11.5, 9, 1.65], "#dark"),
        cube([6, 7.85, 1.1], [6.5, 8.35, 1.35], "#seal"),
        cube([9.5, 7.85, 1.1], [10, 8.35, 1.35], "#seal"),
        # Stepped writing slope, dark front apron, and narrow side edging.
        cube([0.75, 9, 1.25], [15.25, 10.25, 2.5], "#dark"),
        cube([0.75, 10, 1.5], [15.25, 10.8, 5.25], "#wood"),
        cube([1, 10.45, 5.25], [15, 11.25, 8.75], "#wood"),
        cube([0.75, 9.75, 1.25], [1.5, 11.3, 9], "#dark"),
        cube([14.5, 9.75, 1.25], [15.25, 11.3, 9], "#dark"),
        # Raised postal hutch with six deep pigeonholes.
        cube([0.75, 11, 7.75], [15.25, 16, 14.25], "#wood"),
        cube([1.25, 11.5, 7.5], [14.75, 15.5, 8.15], "#dark"),
        cube([5.5, 11.5, 7.2], [6.15, 15.5, 8.4], "#wood"),
        cube([9.85, 11.5, 7.2], [10.5, 15.5, 8.4], "#wood"),
        cube([1.25, 13.35, 7.15], [14.75, 14, 8.4], "#wood"),
        cube([0.5, 15.35, 7.5], [15.5, 16, 14.5], "#dark"),
        # Partially filed correspondence reads as paper without white panels.
        cube([2.1, 12, 7], [4.55, 12.65, 7.25], "#paper"),
        cube([6.9, 14.3, 7], [9.1, 14.9, 7.25], "#paper"),
        cube([11.25, 12.05, 7], [13.7, 12.7, 7.25], "#paper"),
        cube([3.05, 12.1, 6.85], [3.55, 12.6, 7.05], "#seal"),
        cube([7.75, 14.35, 6.85], [8.25, 14.85, 7.05], "#seal"),
        cube([12.2, 12.15, 6.85], [12.7, 12.65, 7.05], "#seal"),
        # One compact letter on the writing surface marks the dispatch point.
        cube([4.25, 10.82, 2.25], [11.75, 10.98, 4.75], "#paper"),
        cube([7.65, 10.99, 3.15], [8.35, 11.18, 3.85], "#seal"),
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
