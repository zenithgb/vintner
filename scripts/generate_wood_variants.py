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

GLASS_COLORS = (
    "clear",
    "white",
    "orange",
    "magenta",
    "light_blue",
    "yellow",
    "lime",
    "pink",
    "gray",
    "light_gray",
    "cyan",
    "purple",
    "blue",
    "brown",
    "green",
    "red",
    "black",
)


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


def stand_id(wood: str) -> str:
    return "barrel_stand" if wood == "oak" else f"{wood}_barrel_stand"


def shelf_id(wood: str) -> str:
    return (
        "labelled_cellar_shelf"
        if wood == "oak"
        else f"{wood}_labelled_cellar_shelf"
    )


def cabinet_id(wood: str) -> str:
    return (
        "tasting_cabinet"
        if wood == "oak"
        else f"{wood}_tasting_cabinet"
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

        for block_id, parent in (
            (stand_id(wood), "barrel_stand"),
            (shelf_id(wood), "labelled_cellar_shelf"),
            (cabinet_id(wood), "tasting_cabinet"),
        ):
            if block_id == parent:
                continue
            write_json(
                ASSETS / f"models/block/{block_id}.json",
                {
                    "parent": f"vintner:block/{parent}",
                    "textures": {
                        "wood": textures["wood"],
                        "beam": textures["beam"],
                        "end": textures["end"],
                        "particle": textures["particle"],
                    },
                },
            )


def aging_barrel_blockstate(
    models: dict[str, str] | None = None,
    legacy_model: str | None = None,
) -> dict[str, object]:
    multipart: list[dict[str, object]] = []
    rotations = {
        "north": 0,
        "east": 90,
        "south": 180,
        "west": 270,
    }

    for facing, rotation in rotations.items():
        if legacy_model is not None:
            apply: dict[str, object] = {"model": legacy_model}
            if rotation:
                apply["y"] = rotation
            multipart.append({
                "when": {"facing": facing},
                "apply": apply,
            })
            continue

        if models is None:
            raise ValueError("Ordinary aging barrels require vessel models")

        for vessel, model in models.items():
            apply = {"model": model}
            if rotation:
                apply["y"] = rotation
            multipart.append({
                "when": {"facing": facing, "vessel": vessel},
                "apply": apply,
            })

    overlays = (
        (1, 1, "aging_barrel_red_aging"),
        (1, 2, "aging_barrel_white_aging"),
        (2, 1, "aging_barrel_red_ready"),
        (2, 2, "aging_barrel_white_ready"),
    )
    for status, wine_type, model in overlays:
        multipart.append({
            "when": {
                "status": str(status),
                "wine_type": str(wine_type),
            },
            "apply": {"model": f"vintner:block/{model}"},
        })

    return {"multipart": multipart}


def cube_faces(texture: str) -> dict[str, dict[str, str]]:
    return {
        face: {"texture": texture}
        for face in ("north", "east", "south", "west", "up", "down")
    }


CANONICAL_BOTTLE_CUBOIDS = (
    ((-1.5, 0.0, -1.5), (1.5, 5.4, 1.5), "#bottle"),
    ((-1.35, 5.4, -1.35), (1.35, 5.9, 1.35), "#bottle"),
    ((-1.0, 5.9, -1.0), (1.0, 6.4, 1.0), "#bottle"),
    ((-0.6, 6.4, -0.6), (0.6, 8.8, 0.6), "#bottle_dark"),
    ((-0.72, 8.6, -0.72), (0.72, 9.2, 0.72), "#bottle_dark"),
    ((-0.5, 9.0, -0.5), (0.5, 9.7, 0.5), "#cork"),
    # Four thin panels form a paper label wrapped around the bottle without
    # replacing the bottle volume with a large white cube.
    ((-1.52, 1.9, -1.56), (1.52, 3.4, -1.5), "#label"),
    ((-1.52, 1.9, 1.5), (1.52, 3.4, 1.56), "#label"),
    ((-1.56, 1.9, -1.5), (-1.5, 3.4, 1.5), "#label"),
    ((1.5, 1.9, -1.5), (1.56, 3.4, 1.5), "#label"),
)


def bottle_elements(
    center_x: float,
    base_y: float,
    center_z: float,
    scale: float,
    *,
    horizontal: bool = False,
    include_seal: bool = False,
) -> list[dict[str, object]]:
    cuboids = list(CANONICAL_BOTTLE_CUBOIDS)

    if include_seal:
        cuboids.append(
            ((-0.45, 2.2, -1.63), (0.45, 3.1, -1.56), "#seal")
        )

    elements = []

    for start, end, texture in cuboids:
        if horizontal:
            world_from = [
                center_x + start[0] * scale,
                base_y + start[2] * scale,
                center_z - end[1] * scale,
            ]
            world_to = [
                center_x + end[0] * scale,
                base_y + end[2] * scale,
                center_z - start[1] * scale,
            ]
        else:
            world_from = [
                center_x + start[0] * scale,
                base_y + start[1] * scale,
                center_z + start[2] * scale,
            ]
            world_to = [
                center_x + end[0] * scale,
                base_y + end[1] * scale,
                center_z + end[2] * scale,
            ]

        elements.append({
            "from": [round(value, 4) for value in world_from],
            "to": [round(value, 4) for value in world_to],
            "faces": cube_faces(texture),
        })

    return elements


def generate_canonical_bottle_models() -> None:
    write_json(
        ASSETS / "models/block/wine_bottle_palette.json",
        {
            "parent": "minecraft:block/block",
            "ambientocclusion": False,
            "textures": {
                "bottle": "minecraft:block/green_terracotta",
                "bottle_dark": "minecraft:block/green_concrete",
                "cork": "minecraft:block/stripped_oak_log_top",
                "label": "minecraft:block/white_terracotta",
                "seal": "minecraft:block/red_terracotta",
                "wine": "minecraft:block/red_concrete",
                "glass": "minecraft:block/white_stained_glass",
                "particle": "minecraft:block/green_terracotta",
            },
        },
    )

    for servings in range(5):
        elements = bottle_elements(
            8.0,
            0.0,
            8.0,
            1.0,
            include_seal=False,
        )

        # A row of four restrained wax marks on the front label makes the
        # remaining pours legible without changing the accepted silhouette.
        for marker in range(servings):
            x0 = 6.55 + marker * 0.75
            elements.append({
                "from": [x0, 2.25, 6.34],
                "to": [x0 + 0.45, 2.8, 6.43],
                "faces": cube_faces("#wine"),
            })

        write_json(
            ASSETS / f"models/block/wine_bottle_fill_{servings}.json",
            {
                "parent": "vintner:block/wine_bottle_palette",
                "ambientocclusion": False,
                "elements": elements,
            },
        )

    # Keep the canonical model name as the fully sealed bottle for existing
    # model references and asset previews.
    write_json(
        ASSETS / "models/block/wine_bottle.json",
        {
            "parent": "vintner:block/wine_bottle_fill_4",
        },
    )

    variants = {}
    rotations = {"north": 0, "east": 90, "south": 180, "west": 270}
    for facing, rotation in rotations.items():
        for servings in range(5):
            entry = {
                "model": f"vintner:block/wine_bottle_fill_{servings}",
            }
            if rotation:
                entry["y"] = rotation
            variants[f"facing={facing},servings={servings}"] = entry

    write_json(
        ASSETS / "blockstates/wine_bottle.json",
        {"variants": variants},
    )

    # A compact, genuinely three-dimensional goblet. The former model was a
    # near-flat white outline; this uses a square Minecraft-style bowl, clear
    # glass, a restrained stem, and a broad enough foot to read cleanly both
    # in inventory and when arranged on a table.
    glass_elements = [
        {"from": [5.75, 1.0, 5.75], "to": [10.25, 2.0, 10.25],
         "faces": cube_faces("#glass_edge")},
        {"from": [7.5, 2.0, 7.5], "to": [8.5, 5.5, 8.5],
         "faces": cube_faces("#glass_edge")},
        {"from": [6.5, 5.5, 6.5], "to": [9.5, 6.5, 9.5],
         "faces": cube_faces("#glass_edge")},
        {"from": [5.5, 6.5, 5.5], "to": [6.5, 10.5, 10.5],
         "faces": cube_faces("#glass")},
        {"from": [9.5, 6.5, 5.5], "to": [10.5, 10.5, 10.5],
         "faces": cube_faces("#glass")},
        {"from": [6.5, 6.5, 5.5], "to": [9.5, 10.5, 6.5],
         "faces": cube_faces("#glass")},
        {"from": [6.5, 6.5, 9.5], "to": [9.5, 10.5, 10.5],
         "faces": cube_faces("#glass")},
        {"from": [5.25, 10.5, 5.25], "to": [10.75, 11.25, 6.25],
         "faces": cube_faces("#glass_edge")},
        {"from": [5.25, 10.5, 9.75], "to": [10.75, 11.25, 10.75],
         "faces": cube_faces("#glass_edge")},
        {"from": [5.25, 10.5, 6.25], "to": [6.25, 11.25, 9.75],
         "faces": cube_faces("#glass_edge")},
        {"from": [9.75, 10.5, 6.25], "to": [10.75, 11.25, 9.75],
         "faces": cube_faces("#glass_edge")},
    ]
    item_display = {
        "gui": {"rotation": [25, 225, 0], "translation": [0, -0.25, 0],
                "scale": [1.7, 1.7, 1.7]},
        "ground": {"translation": [0, 2, 0], "scale": [0.5, 0.5, 0.5]},
        "fixed": {"rotation": [0, 180, 0], "scale": [0.9, 0.9, 0.9]},
        "thirdperson_righthand": {
            "rotation": [75, 45, 0], "translation": [0, 2.5, 0],
            "scale": [0.7, 0.7, 0.7],
        },
        "firstperson_righthand": {
            "rotation": [0, 45, 0], "translation": [0, 2, 0],
            "scale": [0.8, 0.8, 0.8],
        },
    }

    for name, filled in (("wine_glass", False), ("filled_wine_glass", True)):
        elements = list(glass_elements)
        if filled:
            elements.append({
                "from": [6.6, 6.5, 6.6],
                "to": [9.4, 9.35, 9.4],
                "faces": cube_faces("#wine"),
            })
        write_json(
            ASSETS / f"models/item/{name}.json",
            {
                "parent": "minecraft:block/block",
                "ambientocclusion": False,
                "textures": {
                    "glass": {
                        "force_translucent": True,
                        "sprite": "minecraft:block/glass",
                    },
                    "glass_edge": "minecraft:block/light_gray_concrete",
                    "wine": "minecraft:block/red_concrete",
                    "particle": "minecraft:block/light_gray_concrete",
                },
                "display": item_display,
                "elements": elements,
            },
        )
        write_json(
            ASSETS / f"items/{name}.json",
            {
                "model": {
                    "type": "minecraft:model",
                    "model": f"vintner:item/{name}",
                }
            },
        )


def generate_cellar_fixture_base_models() -> None:
    wood_faces = cube_faces("#wood")
    beam_faces = cube_faces("#beam")
    metal_faces = cube_faces("#metal")
    label_faces = cube_faces("#label")

    write_json(
        ASSETS / "models/block/barrel_stand.json",
        {
            "parent": "minecraft:block/block",
            "textures": {
                "wood": "minecraft:block/oak_planks",
                "beam": "minecraft:block/oak_log",
                "end": "minecraft:block/oak_log_top",
                "particle": "minecraft:block/oak_planks",
            },
            "elements": [
                # A compact cooper's trestle: two substantial end frames are
                # joined by low stretchers instead of a forest of thin posts.
                {"from": [1.5, 0, 2], "to": [3.5, 13, 4], "faces": copy.deepcopy(beam_faces)},
                {"from": [12.5, 0, 2], "to": [14.5, 13, 4], "faces": copy.deepcopy(beam_faces)},
                {"from": [1.5, 0, 12], "to": [3.5, 13, 14], "faces": copy.deepcopy(beam_faces)},
                {"from": [12.5, 0, 12], "to": [14.5, 13, 14], "faces": copy.deepcopy(beam_faces)},
                # Broad feet and side stretchers make the empty trestle read
                # as cellar furniture while keeping its centre visually open.
                {"from": [0.75, 0, 1.5], "to": [15.25, 1.25, 4.5], "faces": copy.deepcopy(wood_faces)},
                {"from": [0.75, 0, 11.5], "to": [15.25, 1.25, 14.5], "faces": copy.deepcopy(wood_faces)},
                {"from": [2, 4.5, 3.5], "to": [4, 5.75, 12.5], "faces": copy.deepcopy(wood_faces)},
                {"from": [12, 4.5, 3.5], "to": [14, 5.75, 12.5], "faces": copy.deepcopy(wood_faces)},
                # Framed caps and inset bearer rails form a believable
                # load-bearing platform. The bearers nearly meet the barrel
                # above without introducing a coplanar flicker.
                {"from": [0.75, 12.5, 1.5], "to": [15.25, 14.5, 4.5], "faces": copy.deepcopy(wood_faces)},
                {"from": [0.75, 12.5, 11.5], "to": [15.25, 14.5, 14.5], "faces": copy.deepcopy(wood_faces)},
                {"from": [3, 14.25, 3.5], "to": [5, 15.875, 12.5], "faces": copy.deepcopy(wood_faces)},
                {"from": [11, 14.25, 3.5], "to": [13, 15.875, 12.5], "faces": copy.deepcopy(wood_faces)},
            ],
        },
    )

    write_json(
        ASSETS / "models/block/labelled_cellar_shelf.json",
        {
            "parent": "minecraft:block/block",
            "textures": {
                "wood": "minecraft:block/oak_planks",
                "beam": "minecraft:block/oak_log",
                "end": "minecraft:block/oak_log_top",
                "label": "minecraft:block/cut_copper",
                "particle": "minecraft:block/oak_planks",
            },
            "elements": [
                # Framed four-bay cellar shelving with a solid back.
                {"from": [0.5, 0, 0.5], "to": [2, 16, 15.5], "faces": copy.deepcopy(beam_faces)},
                {"from": [14, 0, 0.5], "to": [15.5, 16, 15.5], "faces": copy.deepcopy(beam_faces)},
                {"from": [2, 0, 1], "to": [14, 2, 15], "faces": copy.deepcopy(wood_faces)},
                {"from": [2, 7, 1], "to": [14, 9, 15], "faces": copy.deepcopy(wood_faces)},
                {"from": [2, 14, 1], "to": [14, 16, 15], "faces": copy.deepcopy(wood_faces)},
                {"from": [2, 2, 14], "to": [14, 14, 15.5], "faces": copy.deepcopy(wood_faces)},
                {"from": [7.5, 2, 1], "to": [8.5, 14, 15], "faces": copy.deepcopy(beam_faces)},
                # Front lips stop the bottles reading as if they float.
                {"from": [2, 2, 0.5], "to": [14, 3, 1.5], "faces": copy.deepcopy(wood_faces)},
                {"from": [2, 9, 0.5], "to": [14, 10, 1.5], "faces": copy.deepcopy(wood_faces)},
                # One label holder for each storage bay.
                {"from": [3.25, 6.25, 0.2], "to": [6.75, 7, 0.75], "faces": copy.deepcopy(label_faces)},
                {"from": [9.25, 6.25, 0.2], "to": [12.75, 7, 0.75], "faces": copy.deepcopy(label_faces)},
                {"from": [3.25, 13.25, 0.2], "to": [6.75, 14, 0.75], "faces": copy.deepcopy(label_faces)},
                {"from": [9.25, 13.25, 0.2], "to": [12.75, 14, 0.75], "faces": copy.deepcopy(label_faces)},
            ],
        },
    )

    write_json(
        ASSETS / "models/block/tasting_cabinet.json",
        {
            "parent": "minecraft:block/block",
            "textures": {
                "wood": "minecraft:block/oak_planks",
                "beam": "minecraft:block/oak_log",
                "end": "minecraft:block/oak_log_top",
                "metal": "minecraft:block/gold_block",
                "cloth": "minecraft:block/red_wool",
                "particle": "minecraft:block/oak_planks",
            },
            "elements": [
                # Furniture-style case with an overhanging crown and plinth.
                {"from": [0, 0, 0.5], "to": [16, 1.5, 15.5], "faces": copy.deepcopy(wood_faces)},
                {"from": [0, 14.5, 0.5], "to": [16, 16, 15.5], "faces": copy.deepcopy(wood_faces)},
                {"from": [0.75, 1.5, 0.75], "to": [2.5, 14.5, 15.25], "faces": copy.deepcopy(beam_faces)},
                {"from": [13.5, 1.5, 0.75], "to": [15.25, 14.5, 15.25], "faces": copy.deepcopy(beam_faces)},
                {"from": [2.5, 7, 1], "to": [13.5, 9, 15], "faces": copy.deepcopy(wood_faces)},
                {"from": [2.5, 1.5, 14], "to": [13.5, 14.5, 15.25], "faces": copy.deepcopy(wood_faces)},
                # Central mullion and door rails create four display panels.
                {"from": [7.5, 1.5, 0.4], "to": [8.5, 14.5, 1.2], "faces": copy.deepcopy(wood_faces)},
                {"from": [2.5, 6.25, 0.4], "to": [13.5, 7.25, 1.2], "faces": copy.deepcopy(wood_faces)},
                {"from": [2.5, 13.5, 0.4], "to": [13.5, 14.5, 1.2], "faces": copy.deepcopy(wood_faces)},
                # A red tasting-cloth strip and paired brass pulls distinguish
                # this curated cabinet from utilitarian cellar shelving.
                {"from": [2.5, 8.75, 0.2], "to": [13.5, 9.35, 1], "faces": copy.deepcopy(cube_faces("#cloth"))},
                {"from": [6.75, 7.35, 0], "to": [7.5, 8.25, 0.75], "faces": copy.deepcopy(metal_faces)},
                {"from": [8.5, 7.35, 0], "to": [9.25, 8.25, 0.75], "faces": copy.deepcopy(metal_faces)},
            ],
        },
    )

    slot = 0
    for y in (2.0, 9.0):
        for x in (3.0, 6.33, 9.67, 13.0):
            slot += 1
            write_json(
                ASSETS / f"models/block/cellar_fixture_bottle_slot_{slot}.json",
                {
                    "parent": "vintner:block/wine_bottle_palette",
                    "elements": bottle_elements(x, y, 8.0, 0.68),
                },
            )

    glass_faces = {
        "north": {"texture": "#glass"},
        "south": {"texture": "#glass"},
    }
    shelf_glass_elements = [
        {"from": [2.5, 3, 1.5], "to": [7.5, 6.2, 1.625], "faces": copy.deepcopy(glass_faces)},
        {"from": [8.5, 3, 1.5], "to": [13.5, 6.2, 1.625], "faces": copy.deepcopy(glass_faces)},
        {"from": [2.5, 10, 1.5], "to": [7.5, 13.45, 1.625], "faces": copy.deepcopy(glass_faces)},
        {"from": [8.5, 10, 1.5], "to": [13.5, 13.45, 1.625], "faces": copy.deepcopy(glass_faces)},
    ]
    cabinet_glass_elements = [
        {"from": [2.5, 1.75, 1.2], "to": [7.5, 6.25, 1.325], "faces": copy.deepcopy(glass_faces)},
        {"from": [8.5, 1.75, 1.2], "to": [13.5, 6.25, 1.325], "faces": copy.deepcopy(glass_faces)},
        {"from": [2.5, 9.35, 1.2], "to": [7.5, 13.5, 1.325], "faces": copy.deepcopy(glass_faces)},
        {"from": [8.5, 9.35, 1.2], "to": [13.5, 13.5, 1.325], "faces": copy.deepcopy(glass_faces)},
    ]
    for color in GLASS_COLORS:
        texture = (
            "minecraft:block/glass"
            if color == "clear"
            else f"minecraft:block/{color}_stained_glass"
        )
        write_json(
            ASSETS / f"models/block/cellar_fixture_glass_{color}.json",
            {
                "parent": "minecraft:block/block",
                "textures": {
                    "glass": {
                        "force_translucent": True,
                        "sprite": texture,
                    },
                    "particle": texture,
                },
                "elements": copy.deepcopy(shelf_glass_elements),
            },
        )
        write_json(
            ASSETS / f"models/block/tasting_cabinet_glass_{color}.json",
            {
                "parent": "minecraft:block/block",
                "textures": {
                    "glass": {
                        "force_translucent": True,
                        "sprite": texture,
                    },
                    "particle": texture,
                },
                "elements": copy.deepcopy(cabinet_glass_elements),
            },
        )


def generate_cellar_fixture_blockstates() -> None:
    rotations = {"north": 0, "east": 90, "south": 180, "west": 270}
    for wood in WOODS:
        for block_id in (stand_id(wood),):
            multipart = []
            for facing, rotation in rotations.items():
                apply = {"model": f"vintner:block/{block_id}"}
                if rotation:
                    apply["y"] = rotation
                multipart.append({"when": {"facing": facing}, "apply": apply})
            write_json(ASSETS / f"blockstates/{block_id}.json", {"multipart": multipart})

        for block_id in (shelf_id(wood), cabinet_id(wood)):
            multipart = []
            for facing, rotation in rotations.items():
                apply = {"model": f"vintner:block/{block_id}"}
                if rotation:
                    apply["y"] = rotation
                multipart.append({"when": {"facing": facing}, "apply": apply})
            for slot in range(1, 9):
                visible = "|".join(str(value) for value in range(slot, 9))
                for facing, rotation in rotations.items():
                    apply = {"model": f"vintner:block/cellar_fixture_bottle_slot_{slot}"}
                    if rotation:
                        apply["y"] = rotation
                    multipart.append({
                        "when": {"facing": facing, "bottle_count": visible},
                        "apply": apply,
                    })
            for color in GLASS_COLORS:
                for facing, rotation in rotations.items():
                    glass_prefix = (
                        "tasting_cabinet_glass"
                        if block_id == cabinet_id(wood)
                        else "cellar_fixture_glass"
                    )
                    apply = {
                        "model": (
                            "vintner:block/"
                            f"{glass_prefix}_{color}"
                        )
                    }
                    if rotation:
                        apply["y"] = rotation
                    multipart.append({
                        "when": {
                            "facing": facing,
                            "glass_color": color,
                        },
                        "apply": apply,
                    })
            write_json(ASSETS / f"blockstates/{block_id}.json", {"multipart": multipart})


def generate_special_aging_vessels() -> None:
    vessels = {
        "chestnut_aging_barrel": {
            "wood": "minecraft:block/dark_oak_planks",
            "end": "minecraft:block/stripped_dark_oak_log_top",
            "band": "minecraft:block/iron_block",
            "label": "minecraft:block/cut_copper",
        },
        "neutral_aging_barrel": {
            "wood": "minecraft:block/stripped_oak_log",
            "end": "minecraft:block/stripped_oak_log_top",
            "band": "minecraft:block/iron_block",
            "label": "minecraft:block/birch_planks",
        },
        "large_cask": {
            "wood": "minecraft:block/spruce_planks",
            "end": "minecraft:block/spruce_log_top",
            "band": "minecraft:block/copper_block",
            "label": "minecraft:block/cut_copper",
        },
    }
    base_cask = read_json(ASSETS / "models/block/cask.json")

    def hoop(y_min: float, y_max: float) -> list[dict[str, object]]:
        band_faces = cube_faces("#band")
        return [
            {"from": [1.25, y_min, 1.5], "to": [1.5, y_max, 14.5], "faces": copy.deepcopy(band_faces)},
            {"from": [14.5, y_min, 1.5], "to": [14.75, y_max, 14.5], "faces": copy.deepcopy(band_faces)},
            {"from": [1.5, y_min, 1.25], "to": [14.5, y_max, 1.5], "faces": copy.deepcopy(band_faces)},
            {"from": [1.5, y_min, 14.5], "to": [14.5, y_max, 14.75], "faces": copy.deepcopy(band_faces)},
        ]

    def scaled_large_cask() -> list[dict[str, object]]:
        elements = copy.deepcopy(base_cask["elements"])
        for element in elements:
            for bound in ("from", "to"):
                coordinates = element[bound]
                coordinates[0] = round(
                    max(0, min(16, 8 + (coordinates[0] - 8) * 1.12)),
                    3,
                )
                coordinates[2] = round(
                    max(0, min(16, 8 + (coordinates[2] - 8) * 1.12)),
                    3,
                )
        return elements

    vessel_elements = {
        # The toasted profile contributes stronger tannin and is visually
        # secured with a third central hoop and a copper cooper's plate.
        "chestnut_aging_barrel": (
            copy.deepcopy(base_cask["elements"])
            + hoop(7.5, 8.5)
            + [{
                "from": [6, 9, 0],
                "to": [10, 11.5, 0.6],
                "faces": cube_faces("#label"),
            }]
        ),
        # The seasoned profile is a low-extraction barrel. Its broad pale
        # cellar label differentiates it from a fresh barrel at a glance.
        "neutral_aging_barrel": (
            copy.deepcopy(base_cask["elements"])
            + [{
                "from": [5, 9, 0],
                "to": [11, 12, 0.65],
                "faces": cube_faces("#label"),
            }]
        ),
        # The large cask nearly fills its block and carries four copper hoops,
        # communicating its doubled capacity without becoming a multiblock.
        "large_cask": (
            scaled_large_cask()
            + hoop(6, 6.75)
            + hoop(9.25, 10)
        ),
    }
    for block_id, textures in vessels.items():
        write_json(
            ASSETS / f"models/block/{block_id}.json",
            {
                "parent": "minecraft:block/block",
                "ambientocclusion": False,
                "textures": {
                    **textures,
                    "particle": textures["wood"],
                },
                "elements": vessel_elements[block_id],
            },
        )
        write_json(
            ASSETS / f"blockstates/{block_id}.json",
            aging_barrel_blockstate(
                legacy_model=f"vintner:block/{block_id}",
            ),
        )
        write_json(
            ASSETS / f"models/item/{block_id}.json",
            {"parent": f"vintner:block/{block_id}"},
        )
        write_json(
            ASSETS / f"items/{block_id}.json",
            {"model": {"type": "minecraft:model", "model": f"vintner:item/{block_id}"}},
        )
        write_json(DATA / f"loot_table/blocks/{block_id}.json", loot_table(block_id))

    write_json(DATA / "tags/item/aging_barrels.json", {
        "replace": False,
        "values": [f"vintner:{aging_id(wood)}" for wood in WOODS],
    })

    profile_parents = {
        "toasted": "chestnut_aging_barrel",
        "seasoned": "neutral_aging_barrel",
        "cellar_cask": "large_cask",
    }
    for wood in WOODS:
        block_id = aging_id(wood)
        textures = machine_textures(wood)
        for profile, parent in profile_parents.items():
            write_json(
                ASSETS / f"models/block/{block_id}_{profile}.json",
                {
                    "parent": f"vintner:block/{parent}",
                    "textures": {
                        "wood": textures["wood"],
                        "end": textures["end"],
                        "particle": textures["particle"],
                    },
                },
            )

    obsolete_recipes = (
        "chestnut_aging_barrel",
        "neutral_aging_barrel",
        "large_cask",
    )
    for recipe_id in obsolete_recipes:
        for path in (
            DATA / f"recipe/{recipe_id}.json",
            DATA / f"advancement/recipes/vintner/{recipe_id}.json",
        ):
            if path.exists():
                path.unlink()


def generate_cooperage_kits() -> None:
    recipes = {
        "toasting_kit": {
            "pattern": [" C ", "CAC", " C "],
            "key": {
                "C": "minecraft:charcoal",
                "A": "minecraft:iron_ingot",
            },
            "unlock": "minecraft:charcoal",
            "texture": "vintner:item/toasting_kit",
        },
        "seasoning_kit": {
            "pattern": [" H ", "HBH", " H "],
            "key": {
                "H": "minecraft:honeycomb",
                "B": "minecraft:water_bucket",
            },
            "unlock": "minecraft:honeycomb",
            "texture": "vintner:item/seasoning_kit",
        },
        "cask_conversion_kit": {
            "pattern": ["PCP", "PPP", "PCP"],
            "key": {
                "P": "minecraft:spruce_planks",
                "C": "minecraft:copper_ingot",
            },
            "unlock": "minecraft:copper_ingot",
            "texture": "vintner:item/cask_conversion_kit",
        },
    }
    for item_id, recipe in recipes.items():
        write_json(DATA / f"recipe/{item_id}.json", {
            "type": "minecraft:crafting_shaped",
            "category": "misc",
            "pattern": recipe["pattern"],
            "key": recipe["key"],
            "result": {"id": f"vintner:{item_id}", "count": 1},
        })
        write_json(
            DATA / f"advancement/recipes/vintner/{item_id}.json",
            recipe_advancement(item_id, recipe["unlock"]),
        )
        write_json(
            ASSETS / f"models/item/{item_id}.json",
            {
                "parent": "minecraft:item/generated",
                "textures": {"layer0": recipe["texture"]},
            },
        )
        write_json(
            ASSETS / f"items/{item_id}.json",
            {
                "model": {
                    "type": "minecraft:model",
                    "model": f"vintner:item/{item_id}",
                }
            },
        )

    criteria = {
        "toasted": {
            "conditions": {"recipe_id": "vintner:toasting_kit"},
            "trigger": "minecraft:recipe_crafted",
        },
        "seasoned": {
            "conditions": {"recipe_id": "vintner:seasoning_kit"},
            "trigger": "minecraft:recipe_crafted",
        },
        "cellar_cask": {
            "conditions": {
                "recipe_id": "vintner:cask_conversion_kit",
            },
            "trigger": "minecraft:recipe_crafted",
        },
    }
    choose_path = DATA / "advancement/vintner/choose_aging_style.json"
    choose = read_json(choose_path)
    choose["criteria"] = copy.deepcopy(criteria)
    choose["requirements"] = [list(criteria)]
    choose["display"]["icon"]["id"] = "vintner:toasting_kit"
    write_json(choose_path, choose)

    master_path = DATA / "advancement/vintner/master_cooper.json"
    master = read_json(master_path)
    master_criteria = {
        "mallet": {
            "conditions": {"recipe_id": "vintner:coopers_mallet"},
            "trigger": "minecraft:recipe_crafted",
        },
        **copy.deepcopy(criteria),
    }
    master["criteria"] = master_criteria
    master["requirements"] = [
        [criterion]
        for criterion in master_criteria
    ]
    master["display"]["icon"]["id"] = "vintner:coopers_mallet"
    write_json(master_path, master)


def generate_rack_bottle_models() -> None:
    slots = (
        (5.0, 2.875),
        (11.0, 2.875),
        (5.0, 10.375),
        (11.0, 10.375),
    )

    for slot, (x, y) in enumerate(slots, start=1):
        write_json(
            ASSETS / f"models/block/wine_rack_bottle_{slot}.json",
            {
                "parent": "vintner:block/wine_bottle_palette",
                "elements": bottle_elements(
                    x,
                    y,
                    11.6,
                    0.75,
                    horizontal=True,
                ),
            },
        )


def generate_crate_bottle_models() -> None:
    centers = (3.0, 6.33, 9.67, 13.0)

    slot = 0

    for z_center in centers:
        for x_center in centers:
            slot += 1
            write_json(
                ASSETS / f"models/block/wine_crate_bottle_slot_{slot}.json",
                {
                    "parent": "vintner:block/wine_bottle_palette",
                    "elements": bottle_elements(
                        x_center,
                        2.0,
                        z_center,
                        0.68,
                    ),
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
        if base_id == "aging_barrel":
            for wood in WOODS:
                block_id = id_factory(wood)
                write_json(
                    ASSETS / f"blockstates/{block_id}.json",
                    aging_barrel_blockstate({
                        "oak": f"vintner:block/{block_id}",
                        "chestnut": (
                            f"vintner:block/{block_id}_toasted"
                        ),
                        "neutral": (
                            f"vintner:block/{block_id}_seasoned"
                        ),
                        "large_cask": (
                            f"vintner:block/{block_id}_cellar_cask"
                        ),
                    }),
                )
            continue

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
    def fixture_item_model(block_id: str) -> dict[str, object]:
        variant = read_json(ASSETS / f"models/block/{block_id}.json")
        parent = variant.get("parent", "")
        base = variant
        if parent.startswith("vintner:block/"):
            parent_id = parent.removeprefix("vintner:block/")
            base = read_json(ASSETS / f"models/block/{parent_id}.json")

        glass_prefix = (
            "tasting_cabinet_glass"
            if block_id.endswith("tasting_cabinet")
            else "cellar_fixture_glass"
        )
        glass = read_json(
            ASSETS / f"models/block/{glass_prefix}_clear.json"
        )
        textures = {
            **base.get("textures", {}),
            **variant.get("textures", {}),
            "glass": glass["textures"]["glass"],
        }
        return {
            "parent": "minecraft:block/block",
            "textures": textures,
            "elements": (
                copy.deepcopy(base.get("elements", []))
                + copy.deepcopy(glass.get("elements", []))
            ),
        }

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
            (
                stand_id(wood),
                f"vintner:block/{stand_id(wood)}",
            ),
            (
                shelf_id(wood),
                f"vintner:block/{shelf_id(wood)}",
            ),
            (
                cabinet_id(wood),
                f"vintner:block/{cabinet_id(wood)}",
            ),
        )

        for block_id, parent in ids_and_models:
            if block_id in {
                shelf_id(wood),
                cabinet_id(wood),
            }:
                item_model = fixture_item_model(block_id)
            else:
                item_model = {"parent": parent}
            write_json(
                ASSETS / f"models/item/{block_id}.json",
                item_model,
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
            stand_id(wood),
            shelf_id(wood),
            cabinet_id(wood),
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
            stand_id(wood): {
                "type": "minecraft:crafting_shaped",
                "category": "misc",
                "pattern": ["S S", "PSP", "P P"],
                "key": {"P": planks, "S": f"minecraft:{wood}_slab"},
                "result": {"id": f"vintner:{stand_id(wood)}", "count": 1},
            },
            shelf_id(wood): {
                "type": "minecraft:crafting_shaped",
                "category": "misc",
                "pattern": ["PPP", "SPS", "PNP"],
                "key": {"P": planks, "S": "minecraft:stick", "N": "minecraft:name_tag"},
                "result": {"id": f"vintner:{shelf_id(wood)}", "count": 1},
            },
            cabinet_id(wood): {
                "type": "minecraft:crafting_shaped",
                "category": "misc",
                "pattern": ["PGP", "PBP", "PGP"],
                "key": {"P": planks, "G": "minecraft:glass_pane", "B": "minecraft:book"},
                "result": {"id": f"vintner:{cabinet_id(wood)}", "count": 1},
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

    axe_blocks.extend(
        f"vintner:{block_id}"
        for block_id in (
            "chestnut_aging_barrel",
            "neutral_aging_barrel",
            "large_cask",
        )
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
        language[f"block.vintner.{stand_id(wood)}"] = (
            f"{title} Barrel Stand"
        )
        language[f"block.vintner.{shelf_id(wood)}"] = (
            f"{title} Labelled Cellar Shelf"
        )
        language[f"block.vintner.{cabinet_id(wood)}"] = (
            f"{title} Tasting Cabinet"
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
    # Registry IDs stay unchanged so existing worlds remain compatible.
    language["block.vintner.chestnut_aging_barrel"] = (
        "Toasted Aging Barrel"
    )
    language["block.vintner.neutral_aging_barrel"] = (
        "Seasoned Aging Barrel"
    )
    language["block.vintner.large_cask"] = "Cellar Cask"
    language["item.vintner.toasting_kit"] = "Toasting Kit"
    language["item.vintner.seasoning_kit"] = "Seasoning Kit"
    language["item.vintner.cask_conversion_kit"] = (
        "Cask Conversion Kit"
    )
    language["tag.item.vintner.aging_barrels"] = "Vintner Aging Barrels"
    language["aging_vessel.vintner.oak"] = "Oak barrel"
    language["aging_vessel.vintner.chestnut"] = "Toasted aging barrel"
    language["aging_vessel.vintner.neutral"] = "Seasoned aging barrel"
    language["aging_vessel.vintner.large_cask"] = "Cellar cask"
    language["message.vintner.almanac.vessel"] = (
        "Aged in: %s"
    )
    language["message.vintner.almanac.vessel_guide"] = (
        "Vessel guide: %s"
    )
    language["message.vintner.almanac.vessel_capacity"] = (
        "Capacity: %s bottles | Ageing time: %s seconds"
    )
    language["aging_vessel.vintner.guide.oak"] = (
        "Balanced fresh-oak ageing with firm tannin, moderate oxygen, and low risk. Best for red wine."
    )
    language["aging_vessel.vintner.guide.chestnut"] = (
        "Bold choice: fast ageing, warm spice, and firm structure for red wine, with greater spoilage risk."
    )
    language["aging_vessel.vintner.guide.neutral"] = (
        "Gentle choice: slow, low-extraction ageing that preserves fruit and acidity with very low risk."
    )
    language["aging_vessel.vintner.guide.large_cask"] = (
        "Bulk choice: very slow, gentle maturation for eight bottles with very low oxygen and soft tannin."
    )
    language["aging_vessel.vintner.crafting.oak"] = (
        "Start here. Every wood family begins with this balanced profile; apply a cooperage kit to an empty barrel to specialise it."
    )
    language["aging_vessel.vintner.crafting.chestnut"] = (
        "Apply a Toasting Kit to an empty ordinary Aging Barrel."
    )
    language["aging_vessel.vintner.crafting.neutral"] = (
        "Apply a Seasoning Kit to an empty ordinary Aging Barrel."
    )
    language["aging_vessel.vintner.crafting.large_cask"] = (
        "Apply a Cask Conversion Kit to an empty ordinary Aging Barrel."
    )
    language["message.vintner.aging.upgrade_applied"] = (
        "Barrel configured for %s."
    )
    language["message.vintner.aging.upgrade_empty_required"] = (
        "Empty the barrel before changing its cooperage treatment."
    )
    language["message.vintner.aging.upgrade_already_applied"] = (
        "This barrel is already configured for %s."
    )
    language["message.vintner.aging.upgrade_recover_first"] = (
        "This barrel already uses %s. Break it to recover the current kit before refitting it."
    )
    language["advancement.vintner.choose_aging_style.title"] = (
        "Choose an Aging Style"
    )
    language["advancement.vintner.choose_aging_style.description"] = (
        "Refit an Aging Barrel for bold, gentle, or bulk maturation"
    )
    language["advancement.vintner.master_cooper.title"] = "Master Cooper"
    language["advancement.vintner.master_cooper.description"] = (
        "Craft all three cooperage upgrade kits"
    )
    language["wine_style.vintner.red"] = "Red"
    language["wine_style.vintner.white"] = "White"
    language["message.vintner.almanac.style_estate"] = (
        "Style: %s | Estate: %s"
    )
    language["message.vintner.almanac.value"] = (
        "Estimated value: %s emeralds | Cellar prestige: %s"
    )
    language["tasting_note.vintner.light_body"] = "light-bodied"
    language["tasting_note.vintner.rustic_body"] = "rustic-bodied"
    language["tasting_note.vintner.medium_body"] = "medium-bodied"
    language["tasting_note.vintner.full_body"] = "full-bodied"
    language["message.vintner.labelled_cellar_shelf.empty"] = (
        "The labelled cellar shelf is empty."
    )
    language["message.vintner.labelled_cellar_shelf.full"] = (
        "The labelled cellar shelf is full."
    )
    language["message.vintner.labelled_cellar_shelf.incompatible"] = (
        "The shelf label is reserved for a different batch."
    )
    language["message.vintner.labelled_cellar_shelf.summary"] = (
        "Labelled Shelf: %s/%s bottles | Cellar: %s"
    )
    language["message.vintner.tasting_cabinet.empty"] = (
        "The tasting cabinet is empty."
    )
    language["message.vintner.tasting_cabinet.full"] = (
        "The tasting cabinet is full."
    )
    language["message.vintner.tasting_cabinet.incompatible"] = (
        "That bottle cannot be stored in the tasting cabinet."
    )
    language["message.vintner.tasting_cabinet.summary"] = (
        "Tasting Cabinet: %s/%s bottles | Cellar: %s"
    )
    language["message.vintner.cellar_collection.selection"] = (
        "%s wine | Year %s | Batch %s | %s"
    )
    write_json(path, language)


def main() -> None:
    generate_trellis_models()
    generate_trellis_blockstates()
    generate_grapevine_blockstates()
    generate_machine_models()
    generate_canonical_bottle_models()
    generate_cellar_fixture_base_models()
    generate_cellar_fixture_blockstates()
    generate_special_aging_vessels()
    generate_cooperage_kits()
    generate_rack_bottle_models()
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
