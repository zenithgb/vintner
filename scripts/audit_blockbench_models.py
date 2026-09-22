#!/usr/bin/env python3
"""Audit Vintner's Blockbench sources and generated model-family invariants."""

from __future__ import annotations

import copy
import json
import sys
from pathlib import Path
from typing import Any, Callable

from generate_wood_variants import (
    WOODS,
    aging_id,
    archive_id,
    cabinet_id,
    crate_id,
    estate_desk_id,
    fermentation_id,
    press_id,
    rack_id,
    shelf_id,
    stand_id,
    surveyors_map_table_id,
    tasting_service_id,
    trellis_id,
    wine_basket_id,
)


ROOT = Path(__file__).resolve().parents[1]
ASSETS = ROOT / "src/main/resources/assets/vintner"
MODELS = ASSETS / "models"
BLOCK_MODELS = MODELS / "block"
BLOCKBENCH = ROOT / "blockbench"

EXPECTED_WOODS = (
    "oak",
    "spruce",
    "birch",
    "jungle",
    "acacia",
    "dark_oak",
    "mangrove",
    "cherry",
    "pale_oak",
    "poplar",
    "bamboo",
    "crimson",
    "warped",
)

errors: list[str] = []


def fail(message: str) -> None:
    errors.append(message)


def load(path: Path) -> Any:
    try:
        return json.loads(path.read_text())
    except (OSError, UnicodeError, json.JSONDecodeError) as error:
        fail(f"cannot read {path.relative_to(ROOT)}: {error}")
        return {}


def block_model_path(identifier: str) -> Path:
    return BLOCK_MODELS / f"{identifier}.json"


def resolve_model(identifier: str) -> dict[str, Any]:
    path = block_model_path(identifier)
    model = load(path)
    if not isinstance(model, dict):
        return {}
    parent = model.get("parent")
    resolved: dict[str, Any] = {}
    if isinstance(parent, str) and parent.startswith("vintner:block/"):
        resolved = resolve_model(parent.removeprefix("vintner:block/"))
    resolved = copy.deepcopy(resolved)
    for key, value in model.items():
        if key == "textures" and isinstance(value, dict):
            resolved.setdefault("textures", {}).update(value)
        elif key != "parent":
            resolved[key] = copy.deepcopy(value)
    return resolved


def geometry_signature(identifier: str) -> Any:
    elements = resolve_model(identifier).get("elements", [])
    normalized = copy.deepcopy(elements)

    def scrub(value: Any) -> None:
        if isinstance(value, dict):
            for key, child in value.items():
                if key == "texture" and isinstance(child, str):
                    value[key] = "#material"
                else:
                    scrub(child)
        elif isinstance(value, list):
            for child in value:
                scrub(child)

    scrub(normalized)
    return normalized


def display_signature(identifier: str) -> Any:
    return resolve_model(identifier).get("display", {})


def trellis_model(wood: str) -> str:
    return (
        "trellis/trellis_single"
        if wood == "oak"
        else f"trellis/{wood}_trellis_single"
    )


def service_model(wood: str) -> str:
    identifier = tasting_service_id(wood)
    return (
        "tasting_service_base"
        if identifier == "tasting_service"
        else f"{identifier}_base"
    )


FAMILIES: tuple[tuple[str, Callable[[str], str]], ...] = (
    ("Trellis", trellis_model),
    ("Grape Press", press_id),
    ("Fermentation Barrel", fermentation_id),
    ("Aging Barrel", aging_id),
    ("Wine Rack", rack_id),
    ("Wine Crate", crate_id),
    ("Wine Basket", wine_basket_id),
    ("Vintage Archive", archive_id),
    ("Barrel Stand", stand_id),
    ("Labelled Cellar Shelf", shelf_id),
    ("Tasting Cabinet", cabinet_id),
    ("Tasting Service", service_model),
    ("Estate Management Desk", estate_desk_id),
    ("Surveyor's Map Table", surveyors_map_table_id),
)


def audit_resource_counts() -> None:
    all_models = sorted(MODELS.rglob("*.json"))
    block_models = sorted(BLOCK_MODELS.rglob("*.json"))
    item_models = sorted((MODELS / "item").rglob("*.json"))
    if len(all_models) != 907:
        fail(f"model JSON count changed: {len(all_models)} (expected 907)")
    if len(block_models) != 692:
        fail(f"block-model count changed: {len(block_models)} (expected 692)")
    if len(item_models) != 215:
        fail(f"item-model count changed: {len(item_models)} (expected 215)")


def audit_wood_family_geometry() -> None:
    if tuple(WOODS) != EXPECTED_WOODS:
        fail(f"wood order changed: {tuple(WOODS)}")
        return

    for family, model_for in FAMILIES:
        reference_id = model_for("oak")
        reference_geometry = geometry_signature(reference_id)
        reference_display = display_signature(reference_id)
        if not reference_geometry:
            fail(f"{family}: oak reference has no resolved geometry")
            continue
        for wood in EXPECTED_WOODS:
            identifier = model_for(wood)
            path = block_model_path(identifier)
            if not path.is_file():
                fail(f"{family}: missing {wood} model {path.relative_to(ROOT)}")
                continue
            if geometry_signature(identifier) != reference_geometry:
                fail(f"{family}: {wood} geometry differs from oak")
            if display_signature(identifier) != reference_display:
                fail(f"{family}: {wood} display transforms differ from oak")

            raw = load(path)
            texture_strings = {
                value
                for value in raw.get("textures", {}).values()
                if isinstance(value, str)
            }
            if wood != "oak" and not any(
                f"minecraft:block/{wood}_" in value
                or value == f"minecraft:block/{wood}_planks"
                or value == WOODS[wood]["beam"]
                or value == WOODS[wood]["end"]
                for value in texture_strings
            ):
                fail(f"{family}: {wood} model has no family texture mapping")


def bb_element_geometry(element: dict[str, Any]) -> dict[str, Any]:
    result: dict[str, Any] = {
        "from": element.get("from"),
        "to": element.get("to"),
    }
    if "rotation" in element:
        rotation = element.get("rotation")
        if isinstance(rotation, list):
            nonzero = [
                (axis, angle)
                for axis, angle in zip("xyz", rotation)
                if angle
            ]
            if len(nonzero) == 1:
                result["rotation"] = {
                    "origin": element.get("origin"),
                    "axis": nonzero[0][0],
                    "angle": nonzero[0][1],
                }
                if element.get("rescale"):
                    result["rotation"]["rescale"] = True
        elif isinstance(rotation, dict):
            result["rotation"] = rotation
    return result


def runtime_element_geometry(element: dict[str, Any]) -> dict[str, Any]:
    result = {
        "from": element.get("from"),
        "to": element.get("to"),
    }
    rotation = element.get("rotation")
    if isinstance(rotation, dict) and rotation.get("angle"):
        result["rotation"] = copy.deepcopy(rotation)
        if result["rotation"].get("rescale") is False:
            result["rotation"].pop("rescale")
    return result


def audit_trellis_sources() -> None:
    pairs = (
        (
            BLOCKBENCH / "trellis/trellis_single.bbmodel",
            BLOCK_MODELS / "trellis/trellis_single.json",
            1,
        ),
        (
            BLOCKBENCH / "trellis/wire_level.bbmodel",
            BLOCK_MODELS / "trellis/wire_level.json",
            4,
        ),
    )
    for source_path, runtime_path, expected_count in pairs:
        source = load(source_path)
        runtime = load(runtime_path)
        source_elements = source.get("elements", [])
        runtime_elements = runtime.get("elements", [])
        if len(source_elements) != expected_count:
            fail(
                f"{source_path.relative_to(ROOT)} has {len(source_elements)} "
                f"elements (expected {expected_count})"
            )
        if [bb_element_geometry(element) for element in source_elements] != [
            runtime_element_geometry(element) for element in runtime_elements
        ]:
            fail(
                f"{source_path.relative_to(ROOT)} geometry has drifted from "
                f"{runtime_path.relative_to(ROOT)}"
            )


def audit_nursery_sources() -> None:
    source = load(BLOCKBENCH / "vineyard/nursery_bed.bbmodel")
    item = load(BLOCKBENCH / "vineyard/nursery_bed_item.bbmodel")
    groups = {
        group.get("name")
        for group in source.get("outliner", [])
        if isinstance(group, dict)
    }
    expected_groups = {
        "bed_frame",
        "growth_age_0",
        "growth_age_1",
        "growth_age_2",
        "growth_age_3",
    }
    if groups != expected_groups or len(source.get("elements", [])) != 58:
        fail("Nursery Bed source must retain five groups and 58 elements")
    if len(item.get("elements", [])) != 10:
        fail("Nursery Bed item source must remain the empty 10-element bed")
    if set(source.get("display", {})) != {
        "thirdperson_righthand",
        "thirdperson_lefthand",
        "firstperson_righthand",
        "firstperson_lefthand",
        "ground",
        "gui",
        "fixed",
    }:
        fail("Nursery Bed source must retain all seven item display contexts")


def audit_workstation_review_source() -> None:
    source_path = BLOCKBENCH / "workstations/connected_estate_workstations.bbmodel"
    source = load(source_path)
    elements = {
        element.get("uuid"): element
        for element in source.get("elements", [])
        if isinstance(element, dict)
    }
    groups = [
        group
        for group in source.get("outliner", [])
        if isinstance(group, dict)
    ]
    expected_families = (
        "vintage_archive",
        "estate_management_desk",
        "surveyors_map_table",
    )
    if [group.get("name") for group in groups] != list(expected_families):
        fail("connected workstation source must retain Archive/Desk/Map groups")
        return
    if len(elements) != 109:
        fail(f"connected workstation source has {len(elements)} elements (expected 109)")

    for index, (group, family) in enumerate(zip(groups, expected_families)):
        model_ids = [family]
        if index > 0:
            model_ids.append(f"{family}_connection_left")
        if index < 2:
            model_ids.append(f"{family}_connection_right")
        expected: list[dict[str, Any]] = []
        offset = (index - 1) * 16
        for identifier in model_ids:
            model = load(block_model_path(identifier))
            for element in model.get("elements", []):
                geometry = runtime_element_geometry(element)
                geometry["from"] = copy.deepcopy(geometry["from"])
                geometry["to"] = copy.deepcopy(geometry["to"])
                geometry["from"][0] += offset
                geometry["to"][0] += offset
                if "rotation" in geometry:
                    geometry["rotation"] = copy.deepcopy(geometry["rotation"])
                    geometry["rotation"]["origin"][0] += offset
                expected.append(geometry)
        children = group.get("children", [])
        actual = [
            bb_element_geometry(elements[child])
            for child in children
            if child in elements
        ]
        if actual != expected:
            fail(f"connected workstation {family} group drifted from runtime geometry")

    textures = source.get("textures", [])
    if len(textures) != 10 or any(
        not isinstance(texture.get("source"), str)
        or not texture["source"].startswith("data:image/png;base64,")
        for texture in textures
        if isinstance(texture, dict)
    ):
        fail("connected workstation source must embed ten preview textures")


def audit_complex_model_contracts() -> None:
    expected_elements = {
        "estate_management_desk": 29,
        "surveyors_map_table": 25,
        "vintage_archive": 39,
        "wine_basket": 30,
        "wine_rack": 14,
        "wine_crate": 27,
        "labelled_cellar_shelf": 14,
        "tasting_cabinet": 12,
        "grape_press": 13,
        "barrel_stand": 12,
        "grape_bowl_empty": 18,
    }
    for identifier, expected in expected_elements.items():
        actual = len(resolve_model(identifier).get("elements", []))
        if actual != expected:
            fail(f"{identifier}: resolved element count {actual} (expected {expected})")

    desk_display = display_signature("estate_management_desk")
    table_display = display_signature("surveyors_map_table")
    if set(desk_display) != {"gui", "ground", "fixed"}:
        fail("Estate Desk must retain reviewed GUI, ground and fixed transforms")
    if table_display != desk_display:
        fail("Surveyor's Map Table transforms must match the Estate Desk")


def main() -> int:
    audit_resource_counts()
    audit_wood_family_geometry()
    audit_trellis_sources()
    audit_nursery_sources()
    audit_workstation_review_source()
    audit_complex_model_contracts()
    if errors:
        print(
            f"Blockbench/model-family audit failed with {len(errors)} error(s):",
            file=sys.stderr,
        )
        for error in errors:
            print(f"  - {error}", file=sys.stderr)
        return 1
    print(
        "Blockbench/model-family audit passed: 907 models, five maintained "
        "Blockbench projects, 14 complete 13-wood families, matching "
        "workstation/Nursery/trellis sources, stable geometry and transforms."
    )
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
