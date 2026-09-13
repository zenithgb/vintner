#!/usr/bin/env python3
"""Validate the Vintner tutorial graph and invisible recipe helpers."""

from __future__ import annotations

import json
from pathlib import Path


ROOT = Path(__file__).resolve().parents[1]
DATA = ROOT / "src/main/resources/data/vintner"
ASSETS = ROOT / "src/main/resources/assets/vintner"
VISIBLE = DATA / "advancement/vintner"
RECIPE_HELPERS = DATA / "advancement/recipes/vintner"
RECIPES = DATA / "recipe"
LANG = ASSETS / "lang/en_us.json"

EXPECTED_PARENTS = {
    "root": None,
    "survey_vineyard": "root",
    "propagate_cutting": "survey_vineyard",
    "graft_vine": "propagate_cutting",
    "craft_trellis": "root",
    "plant_grapevine": "craft_trellis",
    "harvest_grapes": "plant_grapevine",
    "press_must": "harvest_grapes",
    "ferment_wine": "press_must",
    "proper_pour": "ferment_wine",
    "to_good_company": "proper_pour",
    "build_cellar": "ferment_wine",
    "ideal_cellar": "build_cellar",
    "inspect_wine": "ferment_wine",
    "age_wine": "ferment_wine",
    "choose_aging_style": "age_wine",
    "master_cooper": "choose_aging_style",
    "catalogue_vintage": "age_wine",
    "found_estate": "catalogue_vintage",
    "register_plot": "found_estate",
    "manage_estate": "register_plot",
    "map_estate": "manage_estate",
}

CUSTOM_CRITERIA = {
    "plant_grapevine": {"red_grapevine", "white_grapevine"},
    "harvest_grapes": {"harvested"},
    "press_must": {"red_must", "white_must"},
    "ferment_wine": {"red_wine", "white_wine"},
    "age_wine": {"aged_red_wine", "aged_white_wine"},
    "inspect_wine": {"inspected"},
    "survey_vineyard": {"surveyed"},
    "propagate_cutting": {"propagated"},
    "graft_vine": {"grafted"},
    "choose_aging_style": {
        "applied",
        "toasted",
        "seasoned",
        "cellar_cask",
    },
    "proper_pour": {"proper_pour"},
    "to_good_company": {"good_company"},
    "ideal_cellar": {"ideal_cellar"},
    "catalogue_vintage": {"catalogued"},
    "found_estate": {"founded"},
    "register_plot": {"registered"},
    "manage_estate": {"opened"},
    "map_estate": {"mapped"},
}

NEW_TUTORIAL_IDS = {
    "harvest_grapes",
    "propagate_cutting",
    "graft_vine",
    "catalogue_vintage",
    "found_estate",
    "register_plot",
    "manage_estate",
    "map_estate",
}


def load(path: Path) -> dict:
    with path.open(encoding="utf-8") as handle:
        value = json.load(handle)
    if not isinstance(value, dict):
        raise AssertionError(f"{path}: expected a JSON object")
    return value


def fail(errors: list[str], message: str) -> None:
    errors.append(message)


def main() -> int:
    errors: list[str] = []
    language = load(LANG)
    visible_paths = sorted(VISIBLE.glob("*.json"))
    visible = {path.stem: load(path) for path in visible_paths}

    if set(visible) != set(EXPECTED_PARENTS):
        fail(
            errors,
            "visible IDs differ: expected "
            f"{sorted(EXPECTED_PARENTS)}, found {sorted(visible)}",
        )

    for identifier, advancement in visible.items():
        expected_parent = EXPECTED_PARENTS.get(identifier)
        actual_parent = advancement.get("parent")
        normalized_parent = (
            None
            if actual_parent is None
            else str(actual_parent).removeprefix("vintner:vintner/")
        )
        if normalized_parent != expected_parent:
            fail(
                errors,
                f"{identifier}: parent {actual_parent!r}, expected "
                f"{expected_parent!r}",
            )

        display = advancement.get("display")
        if not isinstance(display, dict):
            fail(errors, f"{identifier}: visible advancement has no display")
            continue
        if display.get("hidden", False):
            fail(errors, f"{identifier}: tutorial advancement must be visible")
        for field in ("title", "description"):
            value = display.get(field)
            key = value.get("translate") if isinstance(value, dict) else None
            if not isinstance(key, str) or key not in language:
                fail(errors, f"{identifier}: missing localized {field} key")
        icon = display.get("icon")
        icon_id = icon.get("id") if isinstance(icon, dict) else None
        if not isinstance(icon_id, str) or not icon_id.startswith("vintner:"):
            fail(errors, f"{identifier}: invalid Vintner icon {icon_id!r}")
        elif not (ASSETS / f"items/{icon_id.split(':', 1)[1]}.json").is_file():
            fail(errors, f"{identifier}: icon {icon_id} has no item definition")

        criteria = advancement.get("criteria")
        requirements = advancement.get("requirements")
        if not isinstance(criteria, dict) or not criteria:
            fail(errors, f"{identifier}: missing criteria")
            continue
        if not isinstance(requirements, list) or not requirements:
            fail(errors, f"{identifier}: missing requirements")
            continue
        if identifier in NEW_TUTORIAL_IDS and "rewards" in advancement:
            fail(errors, f"{identifier}: new tutorial advancement has rewards")
        required_names = {
            name
            for group in requirements
            if isinstance(group, list)
            for name in group
        }
        if not required_names or not required_names.issubset(criteria):
            fail(errors, f"{identifier}: invalid criteria requirements")
        expected_custom = CUSTOM_CRITERIA.get(identifier)
        if expected_custom is not None:
            if set(criteria) != expected_custom:
                fail(
                    errors,
                    f"{identifier}: custom criteria {sorted(criteria)} do not "
                    f"match {sorted(expected_custom)}",
                )
            for name, criterion in criteria.items():
                if criterion.get("trigger") != "minecraft:impossible":
                    fail(
                        errors,
                        f"{identifier}/{name}: must be server-awarded",
                    )

    for identifier in visible:
        seen: set[str] = set()
        current: str | None = identifier
        while current is not None:
            if current in seen:
                fail(errors, f"{identifier}: advancement parent cycle")
                break
            seen.add(current)
            current = EXPECTED_PARENTS.get(current)

    recipe_paths = sorted(RECIPES.glob("*.json"))
    helper_paths = sorted(RECIPE_HELPERS.glob("*.json"))
    recipe_ids = {path.stem for path in recipe_paths}
    helper_ids = {path.stem for path in helper_paths}
    if len(helper_paths) != 182:
        fail(errors, f"recipe helper count is {len(helper_paths)}, expected 182")
    if helper_ids != recipe_ids:
        fail(errors, "recipe helper IDs do not exactly match recipe IDs")
    for path in helper_paths:
        advancement = load(path)
        identifier = path.stem
        if "display" in advancement:
            fail(errors, f"{identifier}: recipe helper must remain invisible")
        if advancement.get("parent") != "minecraft:recipes/root":
            fail(errors, f"{identifier}: invalid recipe-helper parent")
        if advancement.get("rewards", {}).get("recipes") != [
            f"vintner:{identifier}"
        ]:
            fail(errors, f"{identifier}: recipe-helper reward mismatch")

    if errors:
        print("Advancement audit FAILED:")
        for error in errors:
            print(f"- {error}")
        return 1

    print(
        "Advancement audit PASS: "
        f"{len(visible_paths) + len(helper_paths)} total, "
        f"{len(visible_paths)} visible tutorial/progression, "
        f"{len(helper_paths)} invisible recipe unlocks."
    )
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
