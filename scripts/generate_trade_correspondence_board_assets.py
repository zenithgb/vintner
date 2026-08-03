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


def faces(
    texture: str,
    *,
    omit: tuple[str, ...] = (),
) -> dict[str, dict[str, str]]:
    return {
        side: {"texture": texture}
        for side in ("north", "east", "south", "west", "up", "down")
        if side not in omit
    }


def cube(
    start: list[float],
    end: list[float],
    texture: str,
    *,
    omit: tuple[str, ...] = (),
) -> dict:
    return {"from": start, "to": end, "faces": faces(texture, omit=omit)}


def model() -> dict:
    elements = [
        # A correspondence cabinet module: storage and pigeonholes, not a
        # duplicate writing desk. The full-width counter joins the desk top.
        cube([1.25, 0, 1.75], [2.75, 10.25, 3.5], "#dark"),
        cube([13.25, 0, 1.75], [14.75, 10.25, 3.5], "#dark"),
        cube([1.25, 0, 12.5], [2.75, 10.25, 14.25], "#dark"),
        cube([13.25, 0, 12.5], [14.75, 10.25, 14.25], "#dark"),
        cube([1.75, 2, 2.25], [14.25, 2.75, 3.25], "#wood"),
        cube([1.75, 2, 12.75], [14.25, 2.75, 13.75], "#wood"),
        cube([1.5, 6.25, 2], [2.5, 7, 14], "#wood"),
        cube([13.5, 6.25, 2], [14.5, 7, 14], "#wood"),
        # Four compact dispatch drawers identify the cabinet from the front.
        cube([2, 6.5, 1.5], [14, 10.25, 4.25], "#wood"),
        cube([2.5, 7.05, 1.3], [7.5, 8.35, 1.65], "#dark"),
        cube([8.5, 7.05, 1.3], [13.5, 8.35, 1.65], "#dark"),
        cube([2.5, 8.55, 1.3], [7.5, 9.85, 1.65], "#dark"),
        cube([8.5, 8.55, 1.3], [13.5, 9.85, 1.65], "#dark"),
        cube([6, 7.85, 1.1], [6.5, 8.35, 1.35], "#seal"),
        cube([9.5, 7.85, 1.1], [10, 8.35, 1.35], "#seal"),
        cube([0, 10.25, 0], [16, 11.5, 16], "#wood"),
        # A recessed six-hole postal hutch. Faces hidden against the counter
        # and top cap are omitted so the cabinet has no coplanar surfaces to
        # flicker when viewed from above or while the player moves.
        cube(
            [0.75, 11.5, 8],
            [1.5, 15.6, 14.5],
            "#dark",
            omit=("up", "down"),
        ),
        cube(
            [14.5, 11.5, 8],
            [15.25, 15.6, 14.5],
            "#dark",
            omit=("up", "down"),
        ),
        cube(
            [1.5, 12, 13.75],
            [14.5, 15.6, 14.5],
            "#dark",
            omit=("up", "down"),
        ),
        cube(
            [1.5, 11.5, 7.75],
            [14.5, 12, 13.75],
            "#wood",
            omit=("down",),
        ),
        cube([1.5, 13.55, 7.75], [14.5, 14.05, 13.75], "#wood"),
        cube([0.5, 15.6, 7.5], [15.5, 16, 14.75], "#wood"),
        cube(
            [5.45, 12, 7.75],
            [6.05, 15.6, 14.5],
            "#wood",
            omit=("up", "down"),
        ),
        cube(
            [9.95, 12, 7.75],
            [10.55, 15.6, 14.5],
            "#wood",
            omit=("up", "down"),
        ),
        # Four banded rolls sit inside the recesses. Two empty holes keep the
        # grid legible as pigeonholes rather than another bank of drawers.
        # Their square ends are inset from the front rail, making the depth of
        # each compartment visible.
        cube([2.55, 12.25, 8.75], [3.9, 13.2, 11.0], "#paper"),
        cube([2.95, 12.15, 8.55], [3.5, 13.3, 11.2], "#seal"),
        cube([7.15, 12.25, 8.75], [8.55, 13.2, 11.0], "#paper"),
        cube([7.6, 12.15, 8.55], [8.15, 13.3, 11.2], "#band"),
        cube([11.7, 14.3, 8.75], [13.1, 15.25, 11.0], "#paper"),
        cube([12.15, 14.2, 8.55], [12.7, 15.35, 11.2], "#seal"),
        cube([2.55, 14.3, 8.75], [3.9, 15.25, 11.0], "#paper"),
        cube([2.95, 14.2, 8.55], [3.5, 15.35, 11.2], "#band"),
    ]
    return {
        "parent": "minecraft:block/block",
        "textures": {
            "wood": "minecraft:block/oak_planks",
            "dark": "minecraft:block/stripped_oak_log",
            "paper": "minecraft:block/bone_block_side",
            "seal": "minecraft:block/red_wool",
            "band": "minecraft:block/yellow_wool",
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
    for connection in ("none", "left", "right", "front", "back"):
        for facing, rotation in ROTATIONS.items():
            value = {
                "model": f"vintner:block/{identifier}",
                "uvlock": True,
            }
            if rotation:
                value["y"] = rotation
            variants[
                f"connection={connection},facing={facing}"
            ] = value
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
