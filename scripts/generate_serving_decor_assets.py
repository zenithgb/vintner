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
    "crimson": ((112, 25, 38, 255), (145, 37, 51, 255), (75, 16, 27, 255)),
    "shaded": ((103, 42, 73, 255), (139, 58, 99, 255), (68, 26, 48, 255)),
    "sunlit": ((117, 42, 25, 255), (153, 60, 36, 255), (78, 26, 16, 255)),
    "riverside": ((106, 38, 59, 255), (142, 52, 81, 255), (70, 24, 39, 255)),
    "golden": ((148, 129, 55, 255), (190, 168, 77, 255), (104, 89, 37, 255)),
    "frosted": ((139, 143, 119, 255), (176, 182, 153, 255), (94, 99, 80, 255)),
    "honeyed": ((158, 119, 51, 255), (199, 154, 72, 255), (109, 79, 33, 255)),
    "stony": ((128, 141, 105, 255), (164, 181, 138, 255), (86, 96, 69, 255)),
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
    # Build one sideways, tapered grape bunch instead of a round mound or
    # scattered cubes. The narrow left end and broad right shoulder mirror a
    # harvested cluster laid across the bowl. Nested counts preserve that
    # silhouette as servings are removed.
    grape_positions = (
        # The first serving retains the tapered spine and narrow tail.
        (4.55, 7.55, 3.00),
        (5.50, 7.05, 3.15), (5.55, 8.20, 3.05),
        (6.45, 6.65, 3.10), (6.55, 7.80, 3.30),
        (6.50, 8.95, 3.05), (7.50, 7.65, 3.25),
        # Additional servings broaden the bunch toward its stem end.
        (7.45, 6.40, 3.05), (7.55, 8.85, 3.10),
        (8.40, 6.70, 3.10), (8.45, 7.85, 3.25),
        (8.35, 9.00, 3.05),
        (9.30, 6.35, 3.00), (9.45, 7.45, 3.20),
        (9.35, 8.60, 3.10), (10.25, 6.75, 3.00),
        (10.35, 7.90, 3.05), (10.25, 9.00, 3.00),
        # A raised layer gives the full bunch the clustered grape silhouette.
        (5.90, 7.60, 4.20), (7.00, 7.15, 4.35),
        (7.15, 8.25, 4.30), (8.30, 7.20, 4.40),
        (8.45, 8.35, 4.30), (9.45, 7.75, 4.25),
    )
    grape_counts = (0, 7, 12, 18, 24)
    for x, z, y in grape_positions[:grape_counts[servings]]:
        elements.append(cube(
            [x, y, z], [x + 1.15, y + 1.15, z + 1.15], "#grapes",
        ))
    if servings > 0:
        # The stem and leaf emerge from the broad end and lie with the bunch.
        elements.extend((
            rotated_cube(
                [9.55, 4.85, 6.95], [11.85, 5.25, 7.35], "#stem",
                origin=[9.75, 5.05, 7.15], axis="y", angle=-22.5,
            ),
            rotated_cube(
                [9.55, 4.95, 7.25], [11.45, 5.20, 9.20], "#leaf",
                origin=[9.75, 5.05, 7.45], axis="y", angle=-22.5,
            ),
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
                        "stem": "minecraft:block/stripped_oak_log",
                        "leaf": "minecraft:block/moss_block",
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
    elements = [
        # Five open slats keep the base visibly woven instead of bed-like.
        *(
            cube([x, 0.45, 4.25], [x + 1.15, 1.0, 11.75], "#weave")
            for x in (4.25, 5.82, 7.42, 9.02, 10.60)
        ),
        # Non-overlapping stepped walls form a clean octagonal basket.
        cube([4.25, 0.8, 3.50], [11.75, 2.75, 4.25], "#weave"),
        cube([4.25, 0.8, 11.75], [11.75, 2.75, 12.50], "#weave"),
        cube([2.75, 0.8, 5.00], [3.50, 2.75, 11.00], "#weave"),
        cube([12.50, 0.8, 5.00], [13.25, 2.75, 11.00], "#weave"),
        cube([3.50, 0.8, 4.25], [4.25, 2.75, 5.00], "#weave"),
        cube([11.75, 0.8, 4.25], [12.50, 2.75, 5.00], "#weave"),
        cube([3.50, 0.8, 11.00], [4.25, 2.75, 11.75], "#weave"),
        cube([11.75, 0.8, 11.00], [12.50, 2.75, 11.75], "#weave"),
        # The rim follows the same footprint with face-touching joins only.
        cube([4.25, 2.75, 3.15], [11.75, 3.40, 4.25], "#rim"),
        cube([4.25, 2.75, 11.75], [11.75, 3.40, 12.85], "#rim"),
        cube([2.25, 2.75, 5.25], [3.25, 3.40, 10.75], "#rim"),
        cube([12.75, 2.75, 5.25], [13.75, 3.40, 10.75], "#rim"),
        cube([3.25, 2.75, 4.25], [4.25, 3.40, 5.25], "#rim"),
        cube([11.75, 2.75, 4.25], [12.75, 3.40, 5.25], "#rim"),
        cube([3.25, 2.75, 10.75], [4.25, 3.40, 11.75], "#rim"),
        cube([11.75, 2.75, 10.75], [12.75, 3.40, 11.75], "#rim"),
        # A stepped arch avoids the self-intersections of rotated handle parts.
        cube([3.25, 3.40, 7.55], [4.00, 6.40, 8.45], "#rim"),
        cube([4.00, 6.40, 7.55], [4.85, 7.15, 8.45], "#rim"),
        cube([4.85, 7.15, 7.55], [5.85, 7.90, 8.45], "#rim"),
        cube([5.85, 7.90, 7.55], [7.00, 8.55, 8.45], "#rim"),
        cube([7.00, 8.55, 7.55], [9.00, 9.15, 8.45], "#rim"),
        cube([9.00, 7.90, 7.55], [10.15, 8.55, 8.45], "#rim"),
        cube([10.15, 7.15, 7.55], [11.15, 7.90, 8.45], "#rim"),
        cube([11.15, 6.40, 7.55], [12.00, 7.15, 8.45], "#rim"),
        cube([12.00, 3.40, 7.55], [12.75, 6.40, 8.45], "#rim"),
    ]
    return elements


def basket_bottle_elements() -> list[dict[str, object]]:
    """Compact horizontal bottle authored for the open basket cradle."""
    return [
        # Cross-shaped sections give a softly stepped round silhouette without
        # rotating the upright placed-bottle model and exposing internal caps.
        cube([6.2, 6.65, 4.25], [9.8, 9.35, 13.05], "#bottle"),
        cube([6.65, 6.2, 4.25], [9.35, 9.8, 13.05], "#bottle"),
        cube([6.4, 6.45, 12.85], [9.6, 9.55, 13.7], "#bottle_dark"),
        cube([6.55, 6.75, 3.55], [9.45, 9.25, 4.65], "#bottle"),
        cube([7.1, 7.25, 1.65], [8.9, 8.75, 3.9], "#bottle"),
        cube([7.35, 7.0, 1.65], [8.65, 9.0, 3.9], "#bottle"),
        cube([7.0, 7.15, 1.15], [9.0, 8.85, 2.05], "#neck_foil"),
        cube([7.25, 7.35, 0.75], [8.75, 8.65, 1.35], "#cork"),
        # A small bordered label sits on the visible upper side.
        cube([6.15, 9.72, 7.2], [9.85, 9.84, 10.25], "#label_border"),
        cube([6.4, 9.82, 7.48], [9.6, 9.91, 9.98], "#label"),
        cube([7.72, 9.90, 7.9], [8.28, 9.96, 9.55], "#label_ink"),
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

    for style in ("red", "white", "aged_red", "aged_white"):
        model_id = f"wine_basket_bottle_{style}"
        write_json(
            ASSETS / f"models/item/{model_id}.json",
            {
                "parent": f"vintner:block/wine_bottle_palette_{style}",
                "ambientocclusion": False,
                "elements": basket_bottle_elements(),
            },
        )
        write_json(
            ASSETS / f"items/{model_id}.json",
            {
                "model": {
                    "type": "minecraft:model",
                    "model": f"vintner:item/{model_id}",
                }
            },
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
