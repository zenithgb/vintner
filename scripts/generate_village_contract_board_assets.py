#!/usr/bin/env python3
"""Generate the persistent village contract notice board assets."""

import json
from pathlib import Path
from generate_wood_variants import WOODS

ROOT = Path(__file__).resolve().parents[1]
ASSETS = ROOT / "src/main/resources/assets/vintner"
DATA = ROOT / "src/main/resources/data/vintner"

def write(path, value):
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(value, indent=2) + "\n")

def cube(start, end, texture):
    return {"from": start, "to": end, "faces": {side: {"texture": texture}
            for side in ("north", "east", "south", "west", "up", "down")}}

def main():
    elements = [
        cube([1, 0, 7], [2.5, 16, 9], "#dark"),
        cube([13.5, 0, 7], [15, 16, 9], "#dark"),
        cube([1, 5, 6.5], [15, 15, 9.5], "#wood"),
        cube([2, 6, 6.2], [14, 14, 6.5], "#board"),
        cube([3, 7, 6.0], [8, 11, 6.2], "#paper"),
        cube([9, 9, 6.0], [13, 13, 6.2], "#paper"),
        cube([5.25, 7.75, 5.85], [5.75, 8.25, 6.05], "#seal"),
        cube([10.75, 11.75, 5.85], [11.25, 12.25, 6.05], "#seal"),
        cube([0.5, 15, 6.5], [15.5, 16, 9.5], "#wood"),
    ]
    write(ASSETS / "models/block/village_contract_board.json", {
        "parent": "minecraft:block/block",
        "textures": {"wood": "minecraft:block/oak_planks", "dark": "minecraft:block/stripped_oak_log", "board": "minecraft:block/spruce_planks", "paper": "minecraft:block/bone_block_side", "seal": "minecraft:block/red_wool", "particle": "minecraft:block/oak_planks"},
        "elements": elements,
    })
    for wood, props in WOODS.items():
        if wood != "oak":
            write(ASSETS / f"models/block/village_contract_board_{wood}.json", {"parent": "vintner:block/village_contract_board", "textures": {"wood": f"minecraft:block/{wood}_planks", "dark": props["beam"], "particle": f"minecraft:block/{wood}_planks"}})
    variants = {}
    rotations = {"north": 0, "east": 90, "south": 180, "west": 270}
    for wood in WOODS:
        model = "vintner:block/village_contract_board" + ("" if wood == "oak" else f"_{wood}")
        for facing, y in rotations.items():
            value = {"model": model, "uvlock": True}
            if y: value["y"] = y
            variants[f"facing={facing},wood={wood}"] = value
    write(ASSETS / "blockstates/village_contract_board.json", {"variants": variants})
    write(ASSETS / "models/item/village_contract_board.json", {"parent": "vintner:block/village_contract_board"})
    write(ASSETS / "items/village_contract_board.json", {"model": {"type": "minecraft:model", "model": "vintner:item/village_contract_board"}})
    write(DATA / "loot_table/blocks/village_contract_board.json", {"type": "minecraft:block", "pools": [{"rolls": 1, "entries": [{"type": "minecraft:item", "name": "vintner:village_contract_board"}], "conditions": [{"condition": "minecraft:survives_explosion"}]}]})
    write(DATA / "recipe/village_contract_board.json", {"type": "minecraft:crafting_shaped", "category": "misc", "pattern": ["PPP", "QBQ", "S S"], "key": {"P": "minecraft:oak_planks", "Q": "minecraft:paper", "B": "minecraft:book", "S": "minecraft:stick"}, "result": {"id": "vintner:village_contract_board"}})
    write(DATA / "advancement/recipes/vintner/village_contract_board.json", {"parent": "minecraft:recipes/root", "criteria": {"has_book": {"trigger": "minecraft:inventory_changed", "conditions": {"items": [{"items": "minecraft:book"}]}}, "has_recipe": {"trigger": "minecraft:recipe_unlocked", "conditions": {"recipe": "vintner:village_contract_board"}}}, "requirements": [["has_book", "has_recipe"]], "rewards": {"recipes": ["vintner:village_contract_board"]}})

if __name__ == "__main__": main()
