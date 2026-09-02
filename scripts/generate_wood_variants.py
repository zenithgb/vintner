#!/usr/bin/env python3
"""Generate Vintner's save-compatible vanilla wood-family resources."""

from __future__ import annotations

import copy
import binascii
import json
import struct
import zlib
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


def write_rgba_texture(
    path: Path,
    rows: list[list[tuple[int, int, int, int]]],
) -> None:
    """Write a small RGBA texture without adding a Pillow dependency."""
    width = len(rows[0])
    height = len(rows)
    if width == 0 or any(len(row) != width for row in rows):
        raise ValueError("Texture rows must have one non-zero width")

    def chunk(kind: bytes, payload: bytes) -> bytes:
        checksum = binascii.crc32(kind + payload) & 0xFFFFFFFF
        return (
            struct.pack(">I", len(payload))
            + kind
            + payload
            + struct.pack(">I", checksum)
        )

    raw = b"".join(
        b"\x00" + bytes(channel for pixel in row for channel in pixel)
        for row in rows
    )
    header = struct.pack(">IIBBBBB", width, height, 8, 6, 0, 0, 0)
    png = (
        b"\x89PNG\r\n\x1a\n"
        + chunk(b"IHDR", header)
        + chunk(b"IDAT", zlib.compress(raw, level=9))
        + chunk(b"IEND", b"")
    )
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_bytes(png)


def generate_white_wine_texture() -> None:
    """Generate pale straw wine centred on the requested #EEEDC4."""
    base = (0xEE, 0xED, 0xC4, 0xFF)
    highlight = (0xF7, 0xF6, 0xDD, 0xFF)
    shadow = (0xD9, 0xD6, 0xAD, 0xFF)
    warm = (0xE7, 0xE3, 0xB5, 0xFF)
    rows = [[base for _ in range(16)] for _ in range(16)]

    # Sparse, low-contrast pixel clusters retain the exact base colour over
    # most of the surface while preventing the liquid from reading as a flat
    # debug swatch in the tasting cups and bottle label medallion.
    for x, y in (
        (2, 2), (3, 2), (11, 3), (12, 3), (7, 6), (8, 6),
        (3, 10), (4, 10), (12, 12), (13, 12),
    ):
        rows[y][x] = highlight
    for x, y in (
        (5, 3), (6, 3), (13, 6), (2, 7), (9, 10), (10, 10),
        (5, 14), (6, 14),
    ):
        rows[y][x] = shadow
    for x, y in ((9, 2), (4, 6), (11, 8), (7, 12), (8, 12)):
        rows[y][x] = warm

    write_rgba_texture(
        ASSETS / "textures/block/white_wine.png",
        rows,
    )


def generate_tasting_liquid_textures() -> None:
    """Generate subtly translucent wine used only in poured tasting cups."""
    alpha = 0xE0
    accents = (
        (2, 2), (3, 2), (11, 3), (12, 3), (7, 6), (8, 6),
        (3, 10), (4, 10), (12, 12), (13, 12),
    )
    shadows = (
        (5, 3), (6, 3), (13, 6), (2, 7), (9, 10), (10, 10),
        (5, 14), (6, 14),
    )

    palettes = {
        "red_wine_liquid": (
            (0x79, 0x18, 0x28, alpha),
            (0x98, 0x29, 0x3B, alpha),
            (0x55, 0x10, 0x1C, alpha),
        ),
        "white_wine_liquid": (
            (0xEE, 0xED, 0xC4, alpha),
            (0xF7, 0xF6, 0xDD, alpha),
            (0xD9, 0xD6, 0xAD, alpha),
        ),
    }

    for texture_name, (base, highlight, shadow) in palettes.items():
        rows = [[base for _ in range(16)] for _ in range(16)]
        for x, y in accents:
            rows[y][x] = highlight
        for x, y in shadows:
            rows[y][x] = shadow

        # The tasting service renders one square surface per cup. Transparent
        # corner pixels turn it into an octagon without assembling several
        # coplanar translucent model elements, which would produce seams.
        for y in range(16):
            for x in range(16):
                corner_distance = min(
                    x + y,
                    x + (15 - y),
                    (15 - x) + y,
                    (15 - x) + (15 - y),
                )
                if corner_distance < 4:
                    red, green, blue, _ = rows[y][x]
                    rows[y][x] = (red, green, blue, 0)
        write_rgba_texture(
            ASSETS / f"textures/block/{texture_name}.png",
            rows,
        )


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


def estate_desk_id(wood: str) -> str:
    return (
        "estate_management_desk"
        if wood == "oak"
        else f"{wood}_estate_management_desk"
    )


def surveyors_map_table_id(wood: str) -> str:
    return (
        "surveyors_map_table"
        if wood == "oak"
        else f"{wood}_surveyors_map_table"
    )


def tasting_service_id(wood: str) -> str:
    return (
        "tasting_service"
        if wood == "oak"
        else f"{wood}_tasting_service"
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

        desk = estate_desk_id(wood)
        if desk != "estate_management_desk":
            write_json(
                ASSETS / f"models/block/{desk}.json",
                {
                    "parent": "vintner:block/estate_management_desk",
                    "textures": {
                        "frame": textures["wood"],
                        "writing": textures["wood"],
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
        for facing, rotation in rotations.items():
            apply = {"model": f"vintner:block/{model}"}
            if rotation:
                apply["y"] = rotation
            multipart.append({
                "when": {
                    "facing": facing,
                    "status": str(status),
                    "wine_type": str(wine_type),
                },
                "apply": apply,
            })

    return {"multipart": multipart}


def fermentation_barrel_blockstate(
    barrel_model: str,
) -> dict[str, object]:
    """Build a facing-aware fermenter with a front-mounted status light."""
    multipart: list[dict[str, object]] = [{
        "apply": {"model": "vintner:block/fermentation_airlock"},
    }]
    rotations = {
        "north": 0,
        "east": 90,
        "south": 180,
        "west": 270,
    }

    for facing, rotation in rotations.items():
        apply: dict[str, object] = {"model": barrel_model}
        if rotation:
            apply["y"] = rotation
        multipart.append({
            "when": {"facing": facing},
            "apply": apply,
        })

    overlays = (
        (1, 1, "fermentation_barrel_red_fermenting"),
        (1, 2, "fermentation_barrel_white_fermenting"),
        (2, None, "fermentation_barrel_ready"),
    )
    for status, wine_type, model in overlays:
        for facing, rotation in rotations.items():
            when: dict[str, str] = {
                "facing": facing,
                "status": str(status),
            }
            if wine_type is not None:
                when["wine_type"] = str(wine_type)

            apply = {"model": f"vintner:block/{model}"}
            if rotation:
                apply["y"] = rotation
            multipart.append({"when": when, "apply": apply})

    return {"multipart": multipart}


def cube_faces(texture: str) -> dict[str, dict[str, str]]:
    return {
        face: {"texture": texture}
        for face in ("north", "east", "south", "west", "up", "down")
    }


RED_BOTTLE_CUBOIDS = (
    # Bordeaux-inspired: a firm heel, long straight body, squared shoulders,
    # foil collar, and proud cork. The extra transitions give the bottle a
    # crafted silhouette without making storage displays visually noisy.
    ((-1.55, 0.0, -1.45), (1.55, 0.38, 1.45), "#bottle_dark"),
    ((-1.45, 0.28, -1.35), (1.45, 5.05, 1.35), "#bottle"),
    ((-1.38, 5.05, -1.28), (1.38, 5.48, 1.28), "#bottle"),
    ((-1.12, 5.48, -1.06), (1.12, 5.88, 1.06), "#bottle"),
    ((-0.78, 5.88, -0.72), (0.78, 6.28, 0.72), "#bottle_dark"),
    ((-0.5, 6.22, -0.48), (0.5, 9.35, 0.48), "#bottle_dark"),
    ((-0.66, 8.9, -0.62), (0.66, 9.58, 0.62), "#neck_foil"),
    ((-0.42, 9.48, -0.4), (0.42, 10.28, 0.4), "#cork"),
)


WHITE_BOTTLE_CUBOIDS = (
    # Burgundy-inspired: a broader body and five gentler shoulder steps make
    # white bottles identifiable by form as well as by their pale label mark.
    ((-1.58, 0.0, -1.48), (1.58, 0.38, 1.48), "#bottle_dark"),
    ((-1.48, 0.28, -1.38), (1.48, 4.55, 1.38), "#bottle"),
    ((-1.4, 4.55, -1.3), (1.4, 4.98, 1.3), "#bottle"),
    ((-1.24, 4.98, -1.16), (1.24, 5.36, 1.16), "#bottle"),
    ((-1.04, 5.36, -0.98), (1.04, 5.72, 0.98), "#bottle"),
    ((-0.82, 5.72, -0.76), (0.82, 6.08, 0.76), "#bottle"),
    ((-0.62, 6.08, -0.58), (0.62, 6.38, 0.58), "#bottle_dark"),
    ((-0.5, 6.32, -0.48), (0.5, 9.35, 0.48), "#bottle_dark"),
    ((-0.66, 8.9, -0.62), (0.66, 9.58, 0.62), "#neck_foil"),
    ((-0.42, 9.48, -0.4), (0.42, 10.28, 0.4), "#cork"),
)


def bottle_elements(
    center_x: float,
    base_y: float,
    center_z: float,
    scale: float,
    *,
    horizontal: bool = False,
    include_seal: bool = False,
    profile: str = "red",
    finish_scale: float = 1.0,
) -> list[dict[str, object]]:
    if profile not in {"red", "white"}:
        raise ValueError(f"Unknown bottle profile: {profile}")

    cuboids = list(
        WHITE_BOTTLE_CUBOIDS
        if profile == "white"
        else RED_BOTTLE_CUBOIDS
    )

    if finish_scale != 1.0:
        cuboids = [
            (
                (
                    start[0] * finish_scale,
                    start[1],
                    start[2] * finish_scale,
                ),
                (
                    end[0] * finish_scale,
                    end[1],
                    end[2] * finish_scale,
                ),
                texture,
            )
            if texture in {"#neck_foil", "#cork"}
            else (start, end, texture)
            for start, end, texture in cuboids
        ]
    front_z = -1.38 if profile == "white" else -1.35

    # Two slim reflections keep the green glass from reading as one flat
    # cuboid. They remain subtle enough to survive the reduced rack scale.
    cuboids.extend((
        ((-1.08, 0.62, front_z - 0.035),
         (-0.78, 4.72, front_z + 0.005),
         "#bottle_highlight"),
        ((-0.34, 6.48, -0.505),
         (-0.18, 8.78, -0.475),
         "#bottle_highlight"),
    ))

    # A bordered paper label, fine rules, and a central vintage medallion
    # create a real front face while leaving the sides and back as glass.
    cuboids.extend((
        ((-1.08, 1.42, front_z - 0.08),
         (1.08, 4.05, front_z - 0.015),
         "#label_border"),
        ((-0.94, 1.58, front_z - 0.145),
         (0.94, 3.89, front_z - 0.085),
         "#label"),
        ((-0.62, 1.84, front_z - 0.205),
         (0.62, 2.02, front_z - 0.15),
         "#label_ink"),
        ((-0.62, 3.46, front_z - 0.205),
         (0.62, 3.64, front_z - 0.15),
         "#label_ink"),
    ))

    if include_seal:
        cuboids.append(
            ((-0.44, 2.36, front_z - 0.27),
             (0.44, 3.12, front_z - 0.21),
             "#seal")
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


STORAGE_BOTTLE_SCALE = 0.80
STORAGE_BOTTLE_FINISH_SCALE = 0.85
STORAGE_BOTTLE_BODY_LOWER_EXTENT = 1.45
STORAGE_BOTTLE_SURFACE_CLEARANCE = 0.02


def horizontal_storage_bottle_center_y(surface_y: float) -> float:
    """Seat the canonical bottle body on a fixture shelf."""
    return (
        surface_y
        + STORAGE_BOTTLE_BODY_LOWER_EXTENT * STORAGE_BOTTLE_SCALE
        + STORAGE_BOTTLE_SURFACE_CLEARANCE
    )


def storage_bottle_elements(
    center_x: float,
    base_y: float,
    center_z: float,
    *,
    horizontal: bool = False,
    scale: float = STORAGE_BOTTLE_SCALE,
) -> list[dict[str, object]]:
    """One sealed canonical bottle shared by cellar cabinets and racks."""
    return bottle_elements(
        center_x,
        base_y,
        center_z,
        scale,
        horizontal=horizontal,
        include_seal=True,
        profile="red",
        finish_scale=STORAGE_BOTTLE_FINISH_SCALE,
    )


def generate_canonical_bottle_models() -> None:
    generate_white_wine_texture()
    generate_tasting_liquid_textures()
    palettes = {
        "red": {
            "bottle": "minecraft:block/green_terracotta",
            "bottle_dark": "minecraft:block/green_concrete",
            "bottle_highlight": "minecraft:block/lime_terracotta",
            "neck_foil": "minecraft:block/red_terracotta",
            "seal": "minecraft:block/red_terracotta",
        },
        "white": {
            "bottle": "minecraft:block/green_terracotta",
            "bottle_dark": "minecraft:block/green_concrete",
            "bottle_highlight": "minecraft:block/lime_terracotta",
            "neck_foil": "vintner:block/white_wine",
            "seal": "vintner:block/white_wine",
        },
    }

    for colour, palette in palettes.items():
        write_json(
            ASSETS / f"models/block/wine_bottle_palette_{colour}.json",
            {
                "parent": "minecraft:block/block",
                "ambientocclusion": False,
                "textures": {
                    **palette,
                    "cork": "minecraft:block/stripped_oak_log_top",
                    "label": "minecraft:block/sandstone_top",
                    "label_border": "minecraft:block/brown_terracotta",
                    "label_ink": "minecraft:block/brown_concrete",
                    "particle": palette["bottle"],
                },
            },
        )

        for servings in range(5):
            write_json(
                ASSETS
                / f"models/block/wine_bottle_{colour}_fill_{servings}.json",
                {
                    "parent": f"vintner:block/wine_bottle_palette_{colour}",
                    "ambientocclusion": False,
                    "elements": bottle_elements(
                        8.0,
                        0.0,
                        8.0,
                        1.0,
                        include_seal=servings > 0,
                        profile=colour,
                    ),
                },
            )

    # Preserve the original palette/model names for storage fixtures and
    # external resource packs that use the established canonical bottle.
    write_json(
        ASSETS / "models/block/wine_bottle_palette.json",
        {
            "parent": "vintner:block/wine_bottle_palette_red",
        },
    )
    for servings in range(5):
        write_json(
            ASSETS / f"models/block/wine_bottle_fill_{servings}.json",
            {
                "parent": f"vintner:block/wine_bottle_red_fill_{servings}",
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
        for colour, white_wine in (("red", "false"), ("white", "true")):
            for servings in range(5):
                entry = {
                    "model": (
                        f"vintner:block/wine_bottle_{colour}_fill_{servings}"
                    ),
                }
                if rotation:
                    entry["y"] = rotation
                variants[
                    f"facing={facing},servings={servings},"
                    f"white_wine={white_wine}"
                ] = entry

    write_json(
        ASSETS / "blockstates/wine_bottle.json",
        {"variants": variants},
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
                # Split the centre divider around the middle shelf. Keeping a
                # full-height divider here made both cuboids occupy the same
                # volume at Y 7-9, producing a moving triangular artifact on
                # the front edge.
                {"from": [7.5, 2, 1], "to": [8.5, 7, 15], "faces": copy.deepcopy(beam_faces)},
                {"from": [7.5, 9, 1], "to": [8.5, 14, 15], "faces": copy.deepcopy(beam_faces)},
                # Front lips stop the bottles reading as if they float.
                {"from": [2, 2, 0.5], "to": [14, 3, 1.5], "faces": copy.deepcopy(wood_faces)},
                {"from": [2, 9, 0.5], "to": [14, 10, 1.5], "faces": copy.deepcopy(wood_faces)},
                # Compact copper labels sit on the solid shelf fronts rather
                # than spanning the openings or sharing a coplanar edge with
                # the shelf above them.
                {"from": [3.65, 0.55, 0.6], "to": [6.35, 1.45, 1.05], "faces": copy.deepcopy(label_faces)},
                {"from": [9.65, 0.55, 0.6], "to": [12.35, 1.45, 1.05], "faces": copy.deepcopy(label_faces)},
                {"from": [3.65, 7.55, 0.6], "to": [6.35, 8.45, 1.05], "faces": copy.deepcopy(label_faces)},
                {"from": [9.65, 7.55, 0.6], "to": [12.35, 8.45, 1.05], "faces": copy.deepcopy(label_faces)},
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

    # Cellar fixtures use the exact same sealed bottle scale and silhouette as
    # the wine rack. Only their shelf heights differ.
    fixture_bottle_layouts = (
        (
            "cellar_fixture_bottle_slot",
            (3.75, 6.25, 9.75, 12.25),
            (2.0, 9.0),
        ),
        (
            "tasting_cabinet_bottle_slot",
            (3.75, 6.25, 9.75, 12.25),
            (1.5, 9.0),
        ),
    )

    for model_prefix, x_centres, shelf_surfaces in fixture_bottle_layouts:
        slot = 0
        for surface_y in shelf_surfaces:
            centre_y = horizontal_storage_bottle_center_y(surface_y)
            for x in x_centres:
                slot += 1
                write_json(
                    ASSETS / f"models/block/{model_prefix}_{slot}.json",
                    {
                        "parent": "vintner:block/wine_bottle_palette",
                        "elements": storage_bottle_elements(
                            x,
                            centre_y,
                            11.25,
                            horizontal=True,
                        ),
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
                    bottle_prefix = (
                        "tasting_cabinet_bottle_slot"
                        if block_id == cabinet_id(wood)
                        else "cellar_fixture_bottle_slot"
                    )
                    apply = {
                        "model": f"vintner:block/{bottle_prefix}_{slot}"
                    }
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
                "P": "#minecraft:planks",
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
        [criterion] for criterion in master_criteria
    ]
    master["display"]["icon"]["id"] = "vintner:coopers_mallet"
    write_json(master_path, master)


def generate_rack_bottle_models() -> None:
    slots = (
        (5.0, horizontal_storage_bottle_center_y(1.5)),
        (11.0, horizontal_storage_bottle_center_y(1.5)),
        (5.0, horizontal_storage_bottle_center_y(9.0)),
        (11.0, horizontal_storage_bottle_center_y(9.0)),
    )

    for slot, (x, y) in enumerate(slots, start=1):
        write_json(
            ASSETS / f"models/block/wine_rack_bottle_{slot}.json",
            {
                "parent": "vintner:block/wine_bottle_palette",
                "elements": storage_bottle_elements(
                    x,
                    y,
                    11.6,
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
        if base_id == "fermentation_barrel":
            for wood in WOODS:
                block_id = id_factory(wood)
                write_json(
                    ASSETS / f"blockstates/{block_id}.json",
                    fermentation_barrel_blockstate(
                        f"vintner:block/{block_id}"
                    ),
                )
            continue

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
            (
                estate_desk_id(wood),
                f"vintner:block/{estate_desk_id(wood)}",
            ),
            (
                surveyors_map_table_id(wood),
                f"vintner:block/{surveyors_map_table_id(wood)}",
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


def generate_vineyard_recipe_advancements() -> None:
    unlocks = {
        "grafting_knife": "minecraft:iron_ingot",
        "nursery_bed": "vintner:vineyard_soil",
        "resistant_rootstock_cutting": "minecraft:nether_wart",
        "rootstock_cutting_from_red": "vintner:red_grape_cutting",
        "rootstock_cutting_from_white": "vintner:white_grape_cutting",
        "vineyard_netting": "minecraft:string",
    }
    for recipe_id, material in unlocks.items():
        write_json(
            DATA / f"advancement/recipes/vintner/{recipe_id}.json",
            recipe_advancement(recipe_id, material),
        )


def generate_survival_data() -> None:
    axe_blocks: list[str] = []

    for wood in WOODS:
        planks = f"minecraft:{wood}_planks"
        ids = [
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
            estate_desk_id(wood),
            surveyors_map_table_id(wood),
            tasting_service_id(wood),
        ]
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
                "result": {"id": f"vintner:{stand_id(wood)}", "count": 2},
            },
            shelf_id(wood): {
                "type": "minecraft:crafting_shaped",
                "category": "misc",
                "pattern": ["PPP", "SPS", "PNP"],
                "key": {"P": planks, "S": "minecraft:stick", "N": "minecraft:paper"},
                "result": {"id": f"vintner:{shelf_id(wood)}", "count": 1},
            },
            cabinet_id(wood): {
                "type": "minecraft:crafting_shaped",
                "category": "misc",
                "pattern": ["PGP", "PBP", "PGP"],
                "key": {"P": planks, "G": "minecraft:glass_pane", "B": "minecraft:book"},
                "result": {"id": f"vintner:{cabinet_id(wood)}", "count": 1},
            },
            estate_desk_id(wood): {
                "type": "minecraft:crafting_shaped",
                "category": "misc",
                "pattern": ["BKG", "PPP", "S S"],
                "key": {
                    "B": "minecraft:writable_book",
                    "K": "minecraft:green_carpet",
                    "G": "minecraft:gold_nugget",
                    "P": planks,
                    "S": f"minecraft:{wood}_slab",
                },
                "result": {
                    "id": f"vintner:{estate_desk_id(wood)}",
                    "count": 1,
                },
            },
            surveyors_map_table_id(wood): {
                "type": "minecraft:crafting_shaped",
                "category": "misc",
                "pattern": ["PMP", "PCP", "S S"],
                "key": {
                    "P": planks,
                    "M": "minecraft:map",
                    "C": "minecraft:cartography_table",
                    "S": f"minecraft:{wood}_slab",
                },
                "result": {
                    "id": f"vintner:{surveyors_map_table_id(wood)}",
                    "count": 1,
                },
            },
            tasting_service_id(wood): {
                "type": "minecraft:crafting_shaped",
                "category": "misc",
                "pattern": ["G G", "PPP", "G G"],
                "key": {
                    "G": "minecraft:glass_pane",
                    "P": planks,
                },
                "result": {
                    "id": f"vintner:{tasting_service_id(wood)}",
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
        language[f"block.vintner.{estate_desk_id(wood)}"] = (
            f"{title} Estate Management Desk"
        )
        language[f"block.vintner.{surveyors_map_table_id(wood)}"] = (
            f"{title} Surveyor's Map Table"
        )
        language[f"block.vintner.{tasting_service_id(wood)}"] = (
            f"{title} Tasting Service"
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
        "Start here. Every wood family begins with this balanced profile; use a Cooper's Mallet and treatment kit on an empty barrel to specialise it."
    )
    language["aging_vessel.vintner.crafting.chestnut"] = (
        "Hold a Cooper's Mallet and Toasting Kit, then use either one on an empty ordinary Aging Barrel."
    )
    language["aging_vessel.vintner.crafting.neutral"] = (
        "Hold a Cooper's Mallet and Seasoning Kit, then use either one on an empty ordinary Aging Barrel."
    )
    language["aging_vessel.vintner.crafting.large_cask"] = (
        "Hold a Cooper's Mallet and Cask Conversion Kit, then use either one on an empty ordinary Aging Barrel."
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
        "This barrel already uses %s. Sneak-use a Cooper's Mallet to remove it before refitting."
    )
    language["advancement.vintner.choose_aging_style.title"] = (
        "Choose an Aging Style"
    )
    language["advancement.vintner.choose_aging_style.description"] = (
        "Refit an Aging Barrel for bold, gentle, or bulk maturation"
    )
    language["advancement.vintner.master_cooper.title"] = "Master Cooper"
    language["advancement.vintner.master_cooper.description"] = (
        "Craft a Cooper's Mallet and all three treatment kits"
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
    generate_vineyard_recipe_advancements()
    generate_craft_trellis_advancement()
    generate_language()
    print(
        "Generated 12 wood families for trellises, grape presses, "
        "fermentation barrels, aging barrels, wine racks, wine "
        "crates, vintage archives, tasting services, and grapevine "
        "supports."
    )


if __name__ == "__main__":
    main()
