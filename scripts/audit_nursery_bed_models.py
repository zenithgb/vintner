#!/usr/bin/env python3
"""Focused structural and reproducibility audit for the Nursery Bed models."""

from __future__ import annotations

import json
import subprocess
import sys
from pathlib import Path
from typing import Any


ROOT = Path(__file__).resolve().parents[1]
ASSETS = ROOT / "src/main/resources/assets/vintner"
DATA = ROOT / "src/main/resources/data/vintner"
SOURCE = ROOT / "blockbench/vineyard/nursery_bed.bbmodel"
ITEM_SOURCE = ROOT / "blockbench/vineyard/nursery_bed_item.bbmodel"
BLOCK_CLASS = ROOT / "src/main/java/com/zenith/vintner/block/NurseryBedBlock.java"
BLOCK_REGISTRY = ROOT / "src/main/java/com/zenith/vintner/registry/ModBlocks.java"

EXPECTED_TOPS = (6.5, 8.0, 10.0, 12.0)
DISPLAY_SLOTS = {
    "thirdperson_righthand",
    "thirdperson_lefthand",
    "firstperson_righthand",
    "firstperson_lefthand",
    "ground",
    "gui",
    "fixed",
}
PLANTS = {
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


def load(path: Path) -> Any:
    return json.loads(path.read_text())


def fail(errors: list[str], message: str) -> None:
    errors.append(message)


def grouped(project: dict[str, Any]) -> dict[str, list[dict[str, Any]]]:
    by_id = {element["uuid"]: element for element in project["elements"]}
    return {
        group["name"]: [by_id[child] for child in group["children"]]
        for group in project["outliner"]
        if isinstance(group, dict)
    }


def geometry(element: dict[str, Any]) -> tuple[Any, Any, Any]:
    return element["from"], element["to"], element["faces"]


def main() -> int:
    errors: list[str] = []
    for path in (SOURCE, ITEM_SOURCE, BLOCK_CLASS, BLOCK_REGISTRY):
        if not path.is_file():
            fail(errors, f"missing required file: {path.relative_to(ROOT)}")
    if errors:
        return report(errors)

    project = load(SOURCE)
    item_project = load(ITEM_SOURCE)
    groups = grouped(project)
    expected_groups = {"bed_frame", *(f"growth_age_{age}" for age in range(4))}
    if set(groups) != expected_groups:
        fail(errors, f"unexpected Blockbench groups: {sorted(groups)}")
    if len(project.get("elements", [])) != 58:
        fail(errors, "authoritative Blockbench source must contain 58 elements")
    if len(project.get("textures", [])) != 3:
        fail(errors, "Blockbench preview must embed oak, soil and plant textures")

    base = groups.get("bed_frame", [])
    if len(base) != 10:
        fail(errors, "bed frame must contain 10 non-overlapping elements")
    else:
        starts = [element["from"] for element in base]
        ends = [element["to"] for element in base]
        bounds = (
            min(value[0] for value in starts),
            min(value[1] for value in starts),
            min(value[2] for value in starts),
            max(value[0] for value in ends),
            max(value[1] for value in ends),
            max(value[2] for value in ends),
        )
        if bounds != (0, 0, 0, 16, 5, 16):
            fail(errors, f"bed frame bounds changed: {bounds}")
        by_name = {element["name"]: element for element in base}
        soil = by_name.get("soil_medium", {})
        if soil.get("from") != [1.75, 0, 1.75] or soil.get("to") != [14.25, 4.25, 14.25]:
            fail(errors, "soil must remain recessed 0.75 pixels below the rim")
        divider = by_name.get("propagation_divider", {})
        if divider.get("from") != [7.75, 4.25, 1.75] or divider.get("to") != [8.25, 4.75, 14.25]:
            fail(errors, "propagation divider dimensions changed")

    for age, expected_top in enumerate(EXPECTED_TOPS):
        growth = groups.get(f"growth_age_{age}", [])
        if len(growth) != 12:
            fail(errors, f"age {age} must contain six crossed cuttings (12 planes)")
            continue
        if {element["from"][1] for element in growth} != {4.125}:
            fail(errors, f"age {age} cuttings must be buried 0.125 pixels into soil")
        if {element["to"][1] for element in growth} != {expected_top}:
            fail(errors, f"age {age} top must be {expected_top}")
        if any(
            min(element["from"][0], element["from"][2]) < 2
            or max(element["to"][0], element["to"][2]) > 14
            for element in growth
        ):
            fail(errors, f"age {age} foliage escapes the inner bed")
        active_faces = [
            {side for side, face in element["faces"].items() if "texture" in face}
            for element in growth
        ]
        if active_faces.count({"north", "south"}) != 6 or active_faces.count({"east", "west"}) != 6:
            fail(errors, f"age {age} must use six two-plane cuttings")

    expected_item_elements = base
    if len(item_project.get("elements", [])) != 10:
        fail(errors, "item Blockbench source must contain only the empty frame and soil")
    elif [geometry(element) for element in item_project["elements"]] != [
        geometry(element) for element in expected_item_elements
    ]:
        fail(errors, "item Blockbench geometry differs from the empty frame and soil")
    if any(
        face.get("texture") == 2
        for element in item_project.get("elements", [])
        for face in element.get("faces", {}).values()
    ):
        fail(errors, "item Blockbench source must not reference planted-growth texture geometry")
    display = item_project.get("display", {})
    if set(display) != DISPLAY_SLOTS:
        fail(errors, f"item display slots changed: {sorted(display)}")
    if display.get("gui", {}).get("scale") != [0.95, 0.95, 0.95]:
        fail(errors, "GUI display scale must preserve the reviewed 0.95 transform")
    if project.get("display") != display:
        fail(errors, "main and item Blockbench display transforms differ")

    blockstate = load(ASSETS / "blockstates/nursery_bed.json")
    multipart = blockstate.get("multipart", [])
    if not multipart or multipart[0] != {"apply": {"model": "vintner:block/nursery_bed"}}:
        fail(errors, "Nursery Bed blockstate must always apply the frame model")
    occupied = multipart[1:]
    if len(occupied) != 16:
        fail(errors, "Nursery Bed blockstate must map four ages for four plant families")
    expected_states = {
        (str(age), plant)
        for age in range(4)
        for plant in ("red_grape", "white_grape", "adapted_rootstock", "resistant_rootstock")
    }
    actual_states = {
        (part.get("when", {}).get("age"), part.get("when", {}).get("plant"))
        for part in occupied
        if part.get("when", {}).get("occupied") == "true"
    }
    if actual_states != expected_states:
        fail(errors, "Nursery Bed growth-state visual map is incomplete")

    for family, textures in PLANTS.items():
        for age, texture in enumerate(textures):
            wrapper = load(ASSETS / f"models/block/nursery_bed_{family}_{age}.json")
            expected = {
                "parent": f"vintner:block/nursery_bed_growth_{age}",
                "textures": {"plant": texture},
            }
            if wrapper != expected:
                fail(errors, f"nursery_bed_{family}_{age}: wrapper changed")

    item_model = load(ASSETS / "models/item/nursery_bed.json")
    if len(item_model.get("elements", [])) != 10 or item_model.get("display") != display:
        fail(errors, "runtime item model must contain the empty frame and reviewed display transforms")
    if "plant" in item_model.get("textures", {}):
        fail(errors, "runtime item model must not retain a planted-growth texture")
    recipe = load(DATA / "recipe/nursery_bed.json")
    if recipe.get("result") != {"id": "vintner:nursery_bed", "count": 1}:
        fail(errors, "Nursery Bed recipe result identity changed")
    if recipe.get("pattern") != ["P P", "PVP", "PPP"]:
        fail(errors, "Nursery Bed recipe pattern changed")

    block_source = BLOCK_CLASS.read_text()
    if "Block.box(0, 0, 0, 16, 5, 16)" not in block_source:
        fail(errors, "Nursery Bed 16x5x16 selection/collision shape changed")
    registry_source = BLOCK_REGISTRY.read_text()
    if '"nursery_bed"' not in registry_source or ".noOcclusion()" not in registry_source:
        fail(errors, "Nursery Bed registry ID or no-occlusion setting changed")

    check = subprocess.run(
        [sys.executable, str(ROOT / "scripts/export_nursery_bed_models.py"), "--check"],
        cwd=ROOT,
        text=True,
        capture_output=True,
    )
    if check.returncode != 0:
        fail(errors, check.stderr.strip() or check.stdout.strip() or "export reproducibility failed")

    return report(errors)


def report(errors: list[str]) -> int:
    if errors:
        print(f"Nursery Bed model audit failed with {len(errors)} error(s):", file=sys.stderr)
        for error in errors:
            print(f"  - {error}", file=sys.stderr)
        return 1
    print(
        "Nursery Bed model audit passed: 58 authored elements, 10-element "
        "16x5x16 frame, six cuttings across four visual ages (tops "
        "6.5/8/10/12), empty 10-element transformed item model, 16 state-family "
        "mappings, unchanged recipe/registry/shape, reproducible export."
    )
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
