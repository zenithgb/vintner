#!/usr/bin/env python3
"""Generate Vintner's save-compatible vanilla wood-family resources."""

from __future__ import annotations

import copy
import json
from pathlib import Path


ROOT = Path(__file__).resolve().parents[1]
ASSETS = ROOT / "src/main/resources/assets/vintner"
DATA = ROOT / "src/main/resources/data/vintner"

WOODS = {
    "oak": {
        "title": "Oak",
        "beam": "minecraft:block/oak_log",
        "end": "minecraft:block/oak_log_top",
    },
    "spruce": {
        "title": "Spruce",
        "beam": "minecraft:block/spruce_log",
        "end": "minecraft:block/spruce_log_top",
    },
    "birch": {
        "title": "Birch",
        "beam": "minecraft:block/birch_log",
        "end": "minecraft:block/birch_log_top",
    },
    "jungle": {
        "title": "Jungle",
        "beam": "minecraft:block/jungle_log",
        "end": "minecraft:block/jungle_log_top",
    },
    "acacia": {
        "title": "Acacia",
        "beam": "minecraft:block/acacia_log",
        "end": "minecraft:block/acacia_log_top",
    },
    "dark_oak": {
        "title": "Dark Oak",
        "beam": "minecraft:block/dark_oak_log",
        "end": "minecraft:block/dark_oak_log_top",
    },
    "mangrove": {
        "title": "Mangrove",
        "beam": "minecraft:block/mangrove_log",
        "end": "minecraft:block/mangrove_log_top",
    },
    "cherry": {
        "title": "Cherry",
        "beam": "minecraft:block/cherry_log",
        "end": "minecraft:block/cherry_log_top",
    },
    "pale_oak": {
        "title": "Pale Oak",
        "beam": "minecraft:block/pale_oak_log",
        "end": "minecraft:block/pale_oak_log_top",
    },
    "bamboo": {
        "title": "Bamboo",
        "beam": "minecraft:block/bamboo_block",
        "end": "minecraft:block/bamboo_block_top",
    },
    "crimson": {
        "title": "Crimson",
        "beam": "minecraft:block/crimson_stem",
        "end": "minecraft:block/crimson_stem_top",
    },
    "warped": {
        "title": "Warped",
        "beam": "minecraft:block/warped_stem",
        "end": "minecraft:block/warped_stem_top",
    },
}


def write_json(path: Path, value: object) -> None:
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(value, indent=2) + "\n")


def read_json(path: Path) -> object:
    return json.loads(path.read_text())


def trellis_id(wood: str) -> str:
    return f"{wood}_trellis"


def press_id(wood: str) -> str:
    return "grape_press" if wood == "oak" else f"{wood}_grape_press"


def fermentation_id(wood: str) -> str:
    return (
        "fermentation_barrel"
        if wood == "oak"
        else f"{wood}_fermentation_barrel"
    )


def aging_id(wood: str) -> str:
    return (
        "aging_barrel"
        if wood == "dark_oak"
        else f"{wood}_aging_barrel"
    )


def rack_id(wood: str) -> str:
    return "wine_rack" if wood == "oak" else f"{wood}_wine_rack"


def crate_id(wood: str) -> str:
    return "wine_crate" if wood == "oak" else f"{wood}_wine_crate"


def archive_id(wood: str) -> str:
    return (
        "vintage_archive"
        if wood == "oak"
        else f"{wood}_vintage_archive"
    )


def grapevine_id(wood: str, color: str) -> str:
    return (
        f"{color}_grapevine"
        if wood == "oak"
        else f"{wood}_{color}_grapevine"
    )


def trellis_single_model(wood: str) -> str:
    return (
        "vintner:block/trellis/trellis_single"
        if wood == "oak"
        else f"vintner:block/trellis/{wood}_trellis_single"
    )


def end_brace_model(wood: str) -> str:
    return (
        "vintner:block/trellis/end_brace"
        if wood == "oak"
        else f"vintner:block/trellis/{wood}_end_brace"
    )


def replace_model(value: object, old: str, new: str) -> object:
    if isinstance(value, dict):
        return {
            key: replace_model(child, old, new)
            for key, child in value.items()
        }
    if isinstance(value, list):
        return [replace_model(child, old, new) for child in value]
    return new if value == old else value


def generate_trellis_models() -> None:
    for wood in WOODS:
        if wood == "oak":
            continue

        textures = {
            "wood": f"minecraft:block/{wood}_planks",
            "particle": f"minecraft:block/{wood}_planks",
        }
        write_json(
            ASSETS
            / f"models/block/trellis/{wood}_trellis_single.json",
            {
                "parent": "vintner:block/trellis/trellis_single",
                "textures": textures,
            },
        )
        write_json(
            ASSETS / f"models/block/trellis/{wood}_end_brace.json",
            {
                "parent": "vintner:block/trellis/end_brace",
                "textures": textures,
            },
        )


def generate_trellis_blockstates() -> None:
    template = read_json(
        ASSETS / "blockstates/oak_trellis.json"
    )

    for wood in WOODS:
        state = replace_model(
            copy.deepcopy(template),
            "vintner:block/trellis/trellis_single",
            trellis_single_model(wood),
        )
        state = replace_model(
            state,
            "vintner:block/trellis/end_brace",
            end_brace_model(wood),
        )
        write_json(
            ASSETS / f"blockstates/{trellis_id(wood)}.json",
            state,
        )


def generate_grapevine_blockstates() -> None:
    for color in ("red", "white"):
        template = read_json(
            ASSETS / f"blockstates/{color}_grapevine.json"
        )

        for wood in WOODS:
            state = replace_model(
                copy.deepcopy(template),
                "vintner:block/trellis/trellis_single",
                trellis_single_model(wood),
            )
            state = replace_model(
                state,
                "vintner:block/trellis/end_brace",
                end_brace_model(wood),
            )
            write_json(
                ASSETS
                / f"blockstates/{grapevine_id(wood, color)}.json",
                state,
            )


def machine_textures(wood: str) -> dict[str, str]:
    properties = WOODS[wood]
    return {
        "wood": f"minecraft:block/{wood}_planks",
        "beam": properties["beam"],
        "end": properties["end"],
        "particle": f"minecraft:block/{wood}_planks",
    }


def generate_machine_models() -> None:
    for wood in WOODS:
        textures = machine_textures(wood)

        press = press_id(wood)
        if press != "grape_press":
            write_json(
                ASSETS / f"models/block/{press}.json",
                {
                    "parent": "vintner:block/grape_press",
                    "textures": textures,
                },
            )

        fermentation = fermentation_id(wood)
        if fermentation != "fermentation_barrel":
            write_json(
                ASSETS / f"models/block/{fermentation}.json",
                {
                    "parent": "vintner:block/fermentation_barrel",
                    "textures": {
                        "wood": textures["wood"],
                        "end": textures["end"],
                        "particle": textures["particle"],
                    },
                },
            )

        aging = aging_id(wood)
        if aging != "aging_barrel":
            write_json(
                ASSETS / f"models/block/{aging}.json",
                {
                    "parent": "vintner:block/aging_barrel",
                    "textures": {
                        "wood": textures["wood"],
                        "end": textures["end"],
                        "particle": textures["particle"],
                    },
                },
            )

        rack = rack_id(wood)
        if rack != "wine_rack":
            write_json(
                ASSETS / f"models/block/{rack}.json",
                {
                    "parent": "vintner:block/wine_rack",
                    "textures": {
                        "wood": textures["wood"],
                        "particle": textures["particle"],
                    },
                },
            )

        crate = crate_id(wood)
        if crate != "wine_crate":
            write_json(
                ASSETS / f"models/block/{crate}.json",
                {
                    "parent": "vintner:block/wine_crate",
                    "textures": {
                        "wood": textures["wood"],
                        "beam": textures["beam"],
                        "end": textures["end"],
                        "particle": textures["particle"],
                    },
                },
            )

        archive = archive_id(wood)
        if archive != "vintage_archive":
            write_json(
                ASSETS / f"models/block/{archive}.json",
                {
                    "parent": "vintner:block/vintage_archive",
                    "textures": {
                        "wood": textures["wood"],
                        "particle": textures["particle"],
                    },
                },
            )


def generate_crate_bottle_models() -> None:
    bottle_faces = {
        face: {"texture": "#bottle"}
        for face in ("north", "east", "south", "west", "up", "down")
    }
    cork_faces = {
        face: {"texture": "#cork"}
        for face in ("north", "east", "south", "west", "up", "down")
    }
    centers = (3.0, 6.33, 9.67, 13.0)

    slot = 0

    for z_center in centers:
        for x_center in centers:
            slot += 1
            elements = [
                {
                    "from": [x_center - 1, 2, z_center - 1],
                    "to": [x_center + 1, 8, z_center + 1],
                    "faces": copy.deepcopy(bottle_faces),
                },
                {
                    "from": [x_center - 0.72, 8, z_center - 0.72],
                    "to": [x_center + 0.72, 9, z_center + 0.72],
                    "faces": copy.deepcopy(bottle_faces),
                },
                {
                    "from": [x_center - 0.38, 9, z_center - 0.38],
                    "to": [x_center + 0.38, 11.3, z_center + 0.38],
                    "faces": copy.deepcopy(bottle_faces),
                },
                {
                    "from": [x_center - 0.43, 11.3, z_center - 0.43],
                    "to": [x_center + 0.43, 11.8, z_center + 0.43],
                    "faces": copy.deepcopy(cork_faces),
                },
            ]

            write_json(
                ASSETS / f"models/block/wine_crate_bottle_slot_{slot}.json",
                {
                    "parent": "minecraft:block/block",
                    "textures": {
                        "bottle": "minecraft:block/green_concrete",
                        "cork": "minecraft:block/stripped_oak_log_top",
                        "particle": "minecraft:block/green_concrete",
                    },
                    "elements": elements,
                },
            )


def generate_crate_blockstate() -> None:
    rotations = {
        "north": 0,
        "east": 90,
        "south": 180,
        "west": 270,
    }
    multipart = []

    for facing, rotation in rotations.items():
        apply = {"model": "vintner:block/wine_crate"}
        if rotation:
            apply["y"] = rotation
        multipart.append(
            {
                "when": {"facing": facing},
                "apply": apply,
            }
        )

    for slot in range(1, 17):
        visible_at = "|".join(str(value) for value in range(slot, 17))

        for facing, rotation in rotations.items():
            apply = {
                "model": f"vintner:block/wine_crate_bottle_slot_{slot}"
            }
            if rotation:
                apply["y"] = rotation
            multipart.append(
                {
                    "when": {
                        "facing": facing,
                        "bottle_count": visible_at,
                    },
                    "apply": apply,
                }
            )

    write_json(
        ASSETS / "blockstates/wine_crate.json",
        {"multipart": multipart},
    )


def generate_machine_blockstates() -> None:
    families = (
        (
            "grape_press",
            press_id,
        ),
        (
            "fermentation_barrel",
            fermentation_id,
        ),
        (
            "aging_barrel",
            aging_id,
        ),
        (
            "wine_rack",
            rack_id,
        ),
        (
            "wine_crate",
            crate_id,
        ),
        (
            "vintage_archive",
            archive_id,
        ),
    )

    for base_id, id_factory in families:
        template = read_json(
            ASSETS / f"blockstates/{base_id}.json"
        )
        base_model = f"vintner:block/{base_id}"

        for wood in WOODS:
            block_id = id_factory(wood)
            state = replace_model(
                copy.deepcopy(template),
                base_model,
                f"vintner:block/{block_id}",
            )
            write_json(
                ASSETS / f"blockstates/{block_id}.json",
                state,
            )


def generate_items() -> None:
    for wood in WOODS:
        ids_and_models = (
            (
                trellis_id(wood),
                trellis_single_model(wood),
            ),
            (
                press_id(wood),
                f"vintner:block/{press_id(wood)}",
            ),
            (
                fermentation_id(wood),
                f"vintner:block/{fermentation_id(wood)}",
            ),
            (
                aging_id(wood),
                f"vintner:block/{aging_id(wood)}",
            ),
            (
                rack_id(wood),
                f"vintner:block/{rack_id(wood)}",
            ),
            (
                crate_id(wood),
                f"vintner:block/{crate_id(wood)}",
            ),
            (
                archive_id(wood),
                f"vintner:block/{archive_id(wood)}",
            ),
        )

        for block_id, parent in ids_and_models:
            write_json(
                ASSETS / f"models/item/{block_id}.json",
                {"parent": parent},
            )
            write_json(
                ASSETS / f"items/{block_id}.json",
                {
                    "model": {
                        "type": "minecraft:model",
                        "model": f"vintner:item/{block_id}",
                    }
                },
            )


def loot_table(block_id: str) -> dict[str, object]:
    return {
        "type": "minecraft:block",
        "pools": [
            {
                "rolls": 1,
                "entries": [
                    {
                        "type": "minecraft:item",
                        "name": f"vintner:{block_id}",
                    }
                ],
                "conditions": [
                    {
                        "condition": "minecraft:survives_explosion",
                    }
                ],
            }
        ],
        "random_sequence": f"vintner:blocks/{block_id}",
    }


def recipe_advancement(
    recipe_id: str,
    material: str,
) -> dict[str, object]:
    return {
        "parent": "minecraft:recipes/root",
        "criteria": {
            "has_material": {
                "conditions": {
                    "items": [
                        {
                            "items": material,
                        }
                    ]
                },
                "trigger": "minecraft:inventory_changed",
            },
            "has_the_recipe": {
                "conditions": {
                    "recipe": f"vintner:{recipe_id}",
                },
                "trigger": "minecraft:recipe_unlocked",
            },
        },
        "requirements": [
            [
                "has_material",
                "has_the_recipe",
            ]
        ],
        "rewards": {
            "recipes": [
                f"vintner:{recipe_id}",
            ]
        },
        "sends_telemetry_event": False,
    }


def generate_survival_data() -> None:
    axe_blocks: list[str] = []

    for wood in WOODS:
        planks = f"minecraft:{wood}_planks"
        ids = (
            trellis_id(wood),
            press_id(wood),
            fermentation_id(wood),
            aging_id(wood),
            rack_id(wood),
            crate_id(wood),
            archive_id(wood),
        )
        axe_blocks.extend(f"vintner:{block_id}" for block_id in ids)

        recipes = {
            trellis_id(wood): {
                "type": "minecraft:crafting_shaped",
                "category": "building",
                "pattern": [
                    "PCP",
                    "P P",
                ],
                "key": {
                    "P": planks,
                    "C": "minecraft:iron_chain",
                },
                "result": {
                    "id": f"vintner:{trellis_id(wood)}",
                    "count": 2,
                },
            },
            press_id(wood): {
                "type": "minecraft:crafting_shaped",
                "category": "misc",
                "pattern": [
                    " I ",
                    "SPS",
                    "BBB",
                ],
                "key": {
                    "I": "minecraft:iron_ingot",
                    "S": "minecraft:stick",
                    "P": "minecraft:piston",
                    "B": planks,
                },
                "result": {
                    "id": f"vintner:{press_id(wood)}",
                    "count": 1,
                },
            },
            fermentation_id(wood): {
                "type": "minecraft:crafting_shaped",
                "category": "misc",
                "pattern": [
                    "PPP",
                    "P P",
                    "PPP",
                ],
                "key": {
                    "P": planks,
                },
                "result": {
                    "id": f"vintner:{fermentation_id(wood)}",
                    "count": 1,
                },
            },
            aging_id(wood): {
                "type": "minecraft:crafting_shaped",
                "category": "misc",
                "pattern": [
                    "PPP",
                    "I I",
                    "PPP",
                ],
                "key": {
                    "P": planks,
                    "I": "minecraft:iron_ingot",
                },
                "result": {
                    "id": f"vintner:{aging_id(wood)}",
                    "count": 1,
                },
            },
            rack_id(wood): {
                "type": "minecraft:crafting_shaped",
                "category": "misc",
                "pattern": [
                    "PPP",
                    "S S",
                    "PPP",
                ],
                "key": {
                    "P": planks,
                    "S": "minecraft:stick",
                },
                "result": {
                    "id": f"vintner:{rack_id(wood)}",
                    "count": 1,
                },
            },
            crate_id(wood): {
                "type": "minecraft:crafting_shaped",
                "category": "misc",
                "pattern": [
                    "PPP",
                    "PCP",
                    "PPP",
                ],
                "key": {
                    "P": planks,
                    "C": "minecraft:chest",
                },
                "result": {
                    "id": f"vintner:{crate_id(wood)}",
                    "count": 1,
                },
            },
            archive_id(wood): {
                "type": "minecraft:crafting_shaped",
                "category": "misc",
                "pattern": [
                    "PPP",
                    "PBP",
                    "PCP",
                ],
                "key": {
                    "P": planks,
                    "B": "minecraft:book",
                    "C": "minecraft:chest",
                },
                "result": {
                    "id": f"vintner:{archive_id(wood)}",
                    "count": 1,
                },
            },
        }

        for recipe_id, recipe in recipes.items():
            write_json(
                DATA / f"recipe/{recipe_id}.json",
                recipe,
            )
            write_json(
                DATA
                / f"advancement/recipes/vintner/{recipe_id}.json",
                recipe_advancement(recipe_id, planks),
            )
            write_json(
                DATA / f"loot_table/blocks/{recipe_id}.json",
                loot_table(recipe_id),
            )

    write_json(
        ROOT
        / "src/main/resources/data/minecraft/tags/block/mineable/axe.json",
        {
            "replace": False,
            "values": axe_blocks,
        },
    )


def generate_craft_trellis_advancement() -> None:
    path = DATA / "advancement/vintner/craft_trellis.json"
    advancement = read_json(path)
    criteria = {
        f"crafted_{wood}": {
            "conditions": {
                "recipe_id": f"vintner:{trellis_id(wood)}",
            },
            "trigger": "minecraft:recipe_crafted",
        }
        for wood in WOODS
    }
    advancement["criteria"] = criteria
    advancement["requirements"] = [list(criteria)]
    write_json(path, advancement)


def generate_language() -> None:
    path = ASSETS / "lang/en_us.json"
    language = read_json(path)

    for wood, properties in WOODS.items():
        title = properties["title"]
        language[f"block.vintner.{trellis_id(wood)}"] = (
            f"{title} Trellis"
        )
        language[f"block.vintner.{press_id(wood)}"] = (
            f"{title} Grape Press"
        )
        language[f"block.vintner.{fermentation_id(wood)}"] = (
            f"{title} Fermentation Barrel"
        )
        language[f"block.vintner.{aging_id(wood)}"] = (
            f"{title} Aging Barrel"
        )
        language[f"block.vintner.{rack_id(wood)}"] = (
            f"{title} Wine Rack"
        )
        language[f"block.vintner.{crate_id(wood)}"] = (
            f"{title} Wine Crate"
        )
        language[f"block.vintner.{archive_id(wood)}"] = (
            f"{title} Vintage Archive"
        )
        language[
            f"block.vintner.{grapevine_id(wood, 'red')}"
        ] = f"{title} Red Grapevine"
        language[
            f"block.vintner.{grapevine_id(wood, 'white')}"
        ] = f"{title} White Grapevine"

    language["advancement.vintner.craft_trellis.description"] = (
        "Craft a trellis for your first vine"
    )
    write_json(path, language)


def main() -> None:
    generate_trellis_models()
    generate_trellis_blockstates()
    generate_grapevine_blockstates()
    generate_machine_models()
    generate_crate_bottle_models()
    generate_crate_blockstate()
    generate_machine_blockstates()
    generate_items()
    generate_survival_data()
    generate_craft_trellis_advancement()
    generate_language()
    print(
        "Generated 12 wood families for trellises, grape presses, "
        "fermentation barrels, aging barrels, wine racks, wine "
        "crates, vintage archives, and grapevine supports."
    )


if __name__ == "__main__":
    main()
