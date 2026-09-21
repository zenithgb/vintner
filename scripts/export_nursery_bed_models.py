#!/usr/bin/env python3
"""Export deterministic Nursery Bed runtime models from the Blockbench source."""

from __future__ import annotations

import argparse
import json
from pathlib import Path


ROOT = Path(__file__).resolve().parents[1]
SOURCE = ROOT / "blockbench/vineyard/nursery_bed.bbmodel"
ITEM_SOURCE = ROOT / "blockbench/vineyard/nursery_bed_item.bbmodel"
MODEL_DIR = ROOT / "src/main/resources/assets/vintner/models"
BLOCK_DIR = MODEL_DIR / "block"
ITEM_DIR = MODEL_DIR / "item"
TEXTURE_KEYS = {"0": "wood", "1": "soil", "2": "plant"}
PLANT_FAMILIES = {
    "red": (
        "vintner:block/red_grapevine_age_0",
        "vintner:block/red_grapevine_age_1",
        "vintner:block/red_grapevine_age_1",
        "vintner:block/red_grapevine_age_2",
    ),
    "white": (
        "vintner:block/white_grapevine_age_0",
        "vintner:block/white_grapevine_age_1",
        "vintner:block/white_grapevine_age_1",
        "vintner:block/white_grapevine_age_2",
    ),
    "rootstock": ("minecraft:block/hanging_roots",) * 4,
    "resistant_rootstock": ("minecraft:block/crimson_roots",) * 4,
}


def load_source() -> tuple[dict[str, object], dict[str, list[dict[str, object]]]]:
    project = json.loads(SOURCE.read_text())
    by_id = {element["uuid"]: element for element in project["elements"]}
    groups = {
        group["name"]: [by_id[child] for child in group["children"]]
        for group in project["outliner"]
        if isinstance(group, dict)
    }
    return project, groups


def runtime_element(source: dict[str, object]) -> dict[str, object]:
    faces: dict[str, object] = {}
    for side, raw_face in source["faces"].items():
        raw_face = dict(raw_face)
        if "texture" not in raw_face:
            continue
        key = TEXTURE_KEYS[str(raw_face.pop("texture"))]
        faces[side] = {"uv": raw_face["uv"], "texture": f"#{key}"}
    return {"from": source["from"], "to": source["to"], "faces": faces}


def write_json(path: Path, value: object, check: bool) -> bool:
    expected = json.dumps(value, indent=2) + "\n"
    if check:
        return path.is_file() and path.read_text() == expected
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(expected)
    return True


def export(check: bool = False) -> None:
    project, groups = load_source()
    item_project = json.loads(ITEM_SOURCE.read_text())
    outputs: dict[Path, object] = {}
    base = [runtime_element(element) for element in groups["bed_frame"]]
    outputs[BLOCK_DIR / "nursery_bed.json"] = {
        "ambientocclusion": True,
        "textures": {"wood": "minecraft:block/oak_planks", "soil": "vintner:block/vineyard_soil", "particle": "vintner:block/vineyard_soil"},
        "elements": base,
    }
    for age in range(4):
        outputs[BLOCK_DIR / f"nursery_bed_growth_{age}.json"] = {
            "ambientocclusion": False,
            "textures": {"particle": "#plant"},
            "elements": [runtime_element(element) for element in groups[f"growth_age_{age}"]],
        }
    for family, textures in PLANT_FAMILIES.items():
        for age, texture in enumerate(textures):
            outputs[BLOCK_DIR / f"nursery_bed_{family}_{age}.json"] = {
                "parent": f"vintner:block/nursery_bed_growth_{age}",
                "textures": {"plant": texture},
            }
    # The ordinary Nursery Bed item is empty. Keep its reviewed transforms and
    # frame/soil geometry in the dedicated Blockbench item source instead of
    # borrowing an occupied growth stage from the placed-block source.
    item_elements = [
        runtime_element(element)
        for element in item_project["elements"]
    ]
    outputs[ITEM_DIR / "nursery_bed.json"] = {
        "ambientocclusion": False,
        "textures": {"wood": "minecraft:block/oak_planks", "soil": "vintner:block/vineyard_soil", "particle": "vintner:block/vineyard_soil"},
        "elements": item_elements,
        "display": item_project["display"],
    }

    failures = [path.relative_to(ROOT) for path, value in outputs.items() if not write_json(path, value, check)]
    if failures:
        joined = "\n".join(f"  {path}" for path in failures)
        raise SystemExit(f"Nursery Bed export differs from source:\n{joined}")
    action = "Verified" if check else "Exported"
    print(f"{action} {len(outputs)} Nursery Bed models from {SOURCE.relative_to(ROOT)}")


def main() -> None:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--check", action="store_true", help="fail if generated models differ from the Blockbench source")
    args = parser.parse_args()
    export(args.check)


if __name__ == "__main__":
    main()
