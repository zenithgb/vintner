#!/usr/bin/env python3
"""Generate resources for the cultivar grape bowl and wood wine baskets."""

from __future__ import annotations

import json
from math import cos, radians, sin
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


def berry_elements(x: float, y: float, z: float, name: str, identity: int) -> list[dict[str, object]]:
    """A subtly irregular berry, stable across palettes and serving states."""
    size = 1.52 + ((identity * 7 + 3) % 17) * 0.02
    x_radius = size * (0.96 + (identity * 3 % 5) * 0.02) / 2
    y_radius = size * (0.95 + ((identity * 5 + 1) % 7) * 0.02) / 2
    z_radius = size * (0.96 + ((identity * 2 + 2) % 5) * 0.02) / 2
    cap = 0.67 + ((identity * 5 + 2) % 6) * 0.025
    band = 0.40 + ((identity * 2 + 1) % 5) * 0.04
    corner = 0.14 + (identity * 3 % 5) * 0.016
    # Lower fruit sits on the existing bowl floor despite differing sizes.
    if y == 0:
        y = 1.10 + y_radius
    parts = []
    for bottom, top, breadth in (
        (-y_radius, -y_radius * band, cap),
        (-y_radius * band, y_radius * band, 1.0),
        (y_radius * band, y_radius, cap),
    ):
        half = x_radius * breadth
        depth_radius = z_radius * breadth
        inset = half * 2 * corner
        for left, right, depth in (
            (-half, -half + inset, depth_radius * (1 - 2 * corner)),
            (-half + inset, half - inset, depth_radius),
            (half - inset, half, depth_radius * (1 - 2 * corner)),
        ):
            part = cube(
                [round(x + left, 4), round(y + bottom, 4), round(z - depth, 4)],
                [round(x + right, 4), round(y + top, 4), round(z + depth, 4)],
                "#grapes",
            )
            part["name"] = name
            u0, u1 = (left + x_radius) / (2 * x_radius), (right + x_radius) / (2 * x_radius)
            v0, v1 = (y_radius - top) / (2 * y_radius), (y_radius - bottom) / (2 * y_radius)
            d0, d1 = (z_radius - depth) / (2 * z_radius), (z_radius + depth) / (2 * z_radius)
            # Four atlas cells offer restrained tone/highlight differences.
            # Crop and mirror the complete berry UV domain, never each slice.
            tile = (identity * 3 + identity // 4) % 4
            crop = 0.88 + (identity % 4) * 0.03
            shift_u = ((identity * 3) % 5) / 4 * (1 - crop)
            shift_v = ((identity * 2) % 5) / 4 * (1 - crop)
            for face_name, face in part["faces"].items():
                uv = (
                    (u0, d0, u1, d1) if face_name in ("up", "down")
                    else (d0, v0, d1, v1) if face_name in ("east", "west")
                    else (u0, v0, u1, v1)
                )
                if identity % 3 == 1:
                    uv = (1 - uv[0], uv[1], 1 - uv[2], uv[3])
                face["uv"] = [round((tile % 2) * 8 + (shift_u + uv[0] * crop) * 8, 4),
                              round((tile // 2) * 8 + (shift_v + uv[1] * crop) * 8, 4),
                              round((tile % 2) * 8 + (shift_u + uv[2] * crop) * 8, 4),
                              round((tile // 2) * 8 + (shift_v + uv[3] * crop) * 8, 4)]
            parts.append(part)
    return parts


def bowl_elements(servings: int) -> list[dict[str, object]]:
    elements = [
        # The inset base and floor meet at y=0.65; the surrounding stepped
        # walls reach y=0 so the whole bowl visibly rests on its block.
        cube([4.5, 0.0, 4.5], [11.5, 0.65, 11.5], "#base"),
        cube([4.5, 0.65, 4.5], [11.5, 1.10, 11.5], "#inside"),
        cube([4.5, 0.0, 3.0], [11.5, 2.65, 4.5], "#bowl"),
        cube([4.5, 0.0, 11.5], [11.5, 2.65, 13.0], "#bowl"),
        cube([3.0, 0.0, 4.5], [4.5, 2.65, 11.5], "#bowl"),
        cube([11.5, 0.0, 4.5], [13.0, 2.65, 11.5], "#bowl"),
        cube([4.0, 0.0, 4.0], [4.5, 2.65, 4.5], "#bowl"),
        cube([11.5, 0.0, 4.0], [12.0, 2.65, 4.5], "#bowl"),
        cube([4.0, 0.0, 11.5], [4.5, 2.65, 12.0], "#bowl"),
        cube([11.5, 0.0, 11.5], [12.0, 2.65, 12.0], "#bowl"),
        # A matching eight-piece rim keeps every join face-touching.
        cube([4.5, 2.65, 2.7], [11.5, 3.35, 4.5], "#rim"),
        cube([4.5, 2.65, 11.5], [11.5, 3.35, 13.3], "#rim"),
        cube([2.7, 2.65, 4.5], [4.5, 3.35, 11.5], "#rim"),
        cube([11.5, 2.65, 4.5], [13.3, 3.35, 11.5], "#rim"),
        cube([4.0, 2.65, 4.0], [4.5, 3.35, 4.5], "#rim"),
        cube([11.5, 2.65, 4.0], [12.0, 3.35, 4.5], "#rim"),
        cube([4.0, 2.65, 11.5], [4.5, 3.35, 12.0], "#rim"),
        cube([11.5, 2.65, 11.5], [12.0, 3.35, 12.0], "#rim"),
    ]
    # Three diagonal bunches nest together into one bushel. Every nonempty
    # state keeps complete seven-berry bunches; the first serving removes
    # only the extra crown fruit, followed by a whole bunch at a time.
    bunches = (
        (9.35, 6.48, 135.0),
        (9.98, 8.20, 121.0),
        (7.80, 6.15, 149.0),
    )
    # Individually composed shoulders, tails and crowns break cloned rows
    # and towers while retaining one nested diagonal bushel.
    berry_patterns = (
        ((0.50, -0.64, 0), (0.74, 0.61, 0),
         (1.66, -0.68, 0), (1.83, 0.49, 0), (2.67, -0.06, 0),
         (0.96, -0.12, 2.95), (2.07, 0.13, 2.83),
         (0.48, -0.20, 3.75), (1.51, 0.30, 3.62)),
        ((0.55, -0.60, 0), (0.68, 0.64, 0),
         (1.57, -0.76, 0), (1.78, 0.50, 0), (2.65, 0.05, 0),
         (0.71, 0.06, 2.89), (1.91, -0.08, 3.10),
         (0.10, -0.12, 3.55), (1.29, -0.23, 4.08)),
        ((0.65, -0.55, 0), (0.60, 0.61, 0),
         (1.60, -0.62, 0), (1.68, 0.60, 0), (2.55, -0.13, 0),
         (1.10, 0.10, 3.12), (2.01, -0.19, 2.91),
         (0.34, 0.22, 3.81), (1.23, -0.24, 3.53)),
    )
    counts = ((), (7,), (7, 7), (7, 7, 7), (9, 9, 9))[servings]
    for bunch_index, ((anchor_x, anchor_z, angle), count) in enumerate(zip(bunches, counts)):
        angle_radians = radians(angle)
        for berry_index, (local_x, local_z, y) in enumerate(berry_patterns[bunch_index][:count]):
            x = round(
                anchor_x + local_x * cos(angle_radians)
                - local_z * sin(angle_radians),
                2,
            )
            z = round(
                anchor_z + local_x * sin(angle_radians)
                + local_z * cos(angle_radians),
                2,
            )
            elements.extend(berry_elements(x, y, z, f"bunch_{bunch_index}_berry_{berry_index}", bunch_index * 9 + berry_index))

        # Each bunch owns its foliage. When that serving is eaten, the stem
        # and leaf disappear with its berries instead of floating behind.
        elements.extend((
            rotated_cube(
                [anchor_x - 0.65, 2.70, anchor_z - 0.12],
                [anchor_x + 0.65, 2.92, anchor_z + 0.12],
                "#stem", origin=[anchor_x, 2.81, anchor_z],
                axis="y", angle=(45.0, 22.5, 45.0)[bunch_index],
            ),
            rotated_cube(
                [anchor_x - 0.05, 2.87, anchor_z - 0.60],
                [anchor_x + (0.75, 0.62, 0.85)[bunch_index], 3.00,
                 anchor_z + (0.10, 0.23, 0.04)[bunch_index]],
                "#leaf", origin=[anchor_x + 0.20, 2.92, anchor_z],
                axis="y", angle=(22.5, 45.0, 0.0)[bunch_index],
            ),
        ))
    return elements


def write_grape_texture(palette: str) -> None:
    base, highlight, shadow = GRAPE_COLORS[palette]
    rows = []
    tones = (1.0, 0.94, 1.06, 0.98)
    highlights = ((3, 3, 4, 3), (5, 2, 3, 4), (2, 5, 3, 2), (4, 4, 2, 3))
    for y in range(32):
        row = []
        for x in range(32):
            tile = x // 16 + (y // 16) * 2
            local_x, local_y = x % 16, y % 16
            left, top, width, height = highlights[tile]
            value = base
            if left <= local_x < left + width and top <= local_y < top + height:
                value = highlight
            elif local_x >= 12 and local_y >= 11:
                value = shadow
            row.append(tuple(min(255, round(channel * tones[tile])) for channel in value[:3]) + (255,))
        rows.append(row)
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
            "ambientocclusion": False,
            "textures": base_textures,
            "elements": bowl_elements(0),
            "display": display_transforms(),
        },
    )
    # Geometry is shared by all eight cultivar palettes. Keep one authored
    # shape per serving state instead of duplicating the detailed fruit mesh.
    for servings in range(1, 5):
        write_json(
            ASSETS / f"models/block/grape_bowl_geometry_{servings}.json",
            {
                "ambientocclusion": False,
                "textures": {
                    **base_textures,
                    "grapes": "vintner:block/grape_bowl_crimson_grapes",
                    "stem": "minecraft:block/stripped_oak_log",
                    "leaf": "minecraft:block/moss_block",
                },
                "elements": bowl_elements(servings),
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
                    "parent": f"vintner:block/grape_bowl_geometry_{servings}",
                    "textures": {
                        "grapes": f"vintner:block/grape_bowl_{palette}_grapes",
                    },
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
            cube([x, 0.0, 4.25], [x + 1.15, 0.55, 11.75], "#weave")
            for x in (4.25, 5.82, 7.42, 9.02, 10.60)
        ),
        # Non-overlapping stepped walls form a clean octagonal basket.
        cube([4.25, 0.0, 3.50], [11.75, 2.75, 4.25], "#weave"),
        cube([4.25, 0.0, 11.75], [11.75, 2.75, 12.50], "#weave"),
        cube([2.75, 0.0, 5.00], [3.50, 2.75, 11.00], "#weave"),
        cube([12.50, 0.0, 5.00], [13.25, 2.75, 11.00], "#weave"),
        cube([3.50, 0.0, 4.25], [4.25, 2.75, 5.00], "#weave"),
        cube([11.75, 0.0, 4.25], [12.50, 2.75, 5.00], "#weave"),
        cube([3.50, 0.0, 11.00], [4.25, 2.75, 11.75], "#weave"),
        cube([11.75, 0.0, 11.00], [12.50, 2.75, 11.75], "#weave"),
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
        # Disjoint sections meet at their boundaries. The previous crossed
        # body rods and overlapping end cap left competing surface faces.
        cube([6.2, 6.65, 4.25], [6.65, 9.35, 13.05], "#bottle"),
        cube([6.65, 6.2, 4.25], [9.35, 9.8, 13.05], "#bottle"),
        cube([9.35, 6.65, 4.25], [9.8, 9.35, 13.05], "#bottle"),
        cube([6.4, 6.45, 13.05], [9.6, 9.55, 13.7], "#bottle_dark"),
        cube([6.55, 6.75, 3.55], [9.45, 9.25, 4.25], "#bottle"),
        cube([7.1, 7.25, 1.65], [8.9, 8.75, 3.55], "#bottle"),
        cube([7.0, 7.15, 1.15], [9.0, 8.85, 1.65], "#neck_foil"),
        cube([7.25, 7.35, 0.75], [8.75, 8.65, 1.15], "#cork"),
        # A small bordered label sits on the visible upper side.
        cube([6.70, 9.80, 7.2], [9.30, 9.84, 10.25], "#label_border"),
        cube([6.90, 9.84, 7.48], [9.10, 9.88, 9.98], "#label"),
        cube([7.72, 9.88, 7.9], [8.28, 9.92, 9.55], "#label_ink"),
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
