#!/usr/bin/env python3
"""Audit Vintner resources that must be complete before a public release."""

from __future__ import annotations

import json
import math
import sys
from collections.abc import Iterable
from pathlib import Path
from typing import Any

from audit_serving_decor_contact import check_contact

from generate_wood_variants import (
    WOODS,
    aging_id,
    archive_id,
    cabinet_id,
    crate_id,
    estate_desk_id,
    fermentation_id,
    grapevine_id,
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
RESOURCES = ROOT / "src/main/resources"
ASSETS = RESOURCES / "assets/vintner"
DATA = RESOURCES / "data/vintner"
LANG_PATH = ASSETS / "lang/en_us.json"
AXE_TAG_PATH = (
    RESOURCES / "data/minecraft/tags/block/mineable/axe.json"
)
RECIPE_PATH = DATA / "recipe"
RECIPE_ADVANCEMENT_PATH = DATA / "advancement/recipes/vintner"
JAVA_SOURCE = ROOT / "src/main/java/com/zenith/vintner"
NOTIFICATION_GATE_PATH = (
    JAVA_SOURCE / "util/VintnerNotifications.java"
)

RECIPE_OUTPUT_OVERRIDES = {
    "rootstock_cutting_from_red": "vintner:rootstock_cutting",
    "rootstock_cutting_from_white": "vintner:rootstock_cutting",
}

errors: list[str] = []
documents: dict[Path, Any] = {}


def fail(message: str) -> None:
    errors.append(message)


def relative(path: Path) -> str:
    try:
        return str(path.relative_to(ROOT))
    except ValueError:
        return str(path)


def reject_duplicate_keys(
    pairs: list[tuple[str, Any]],
) -> dict[str, Any]:
    value: dict[str, Any] = {}
    for key, child in pairs:
        if key in value:
            raise ValueError(f"duplicate key {key!r}")
        value[key] = child
    return value


def load_json(path: Path) -> Any | None:
    if path in documents:
        return documents[path]
    if not path.is_file():
        fail(f"missing file: {relative(path)}")
        return None
    try:
        value = json.loads(
            path.read_text(),
            object_pairs_hook=reject_duplicate_keys,
        )
    except (OSError, UnicodeError, json.JSONDecodeError, ValueError) as error:
        fail(f"invalid JSON: {relative(path)}: {error}")
        return None
    documents[path] = value
    return value


def require_file(path: Path) -> None:
    if not path.is_file():
        fail(f"missing file: {relative(path)}")


def walk(value: Any) -> Iterable[tuple[str | None, Any]]:
    if isinstance(value, dict):
        for key, child in value.items():
            yield key, child
            yield from walk(child)
    elif isinstance(value, list):
        for child in value:
            yield None, child
            yield from walk(child)


def strings(value: Any) -> set[str]:
    return {
        child
        for _, child in walk(value)
        if isinstance(child, str)
    }


def model_path(reference: str) -> Path | None:
    namespace, separator, resource = reference.partition(":")
    if not separator:
        namespace = "minecraft"
        resource = reference
    if namespace != "vintner":
        return None
    return ASSETS / "models" / f"{resource}.json"


def texture_path(reference: str) -> Path | None:
    namespace, separator, resource = reference.partition(":")
    if not separator:
        namespace = "minecraft"
        resource = reference
    if namespace != "vintner":
        return None
    return ASSETS / "textures" / f"{resource}.png"


def audit_all_json() -> None:
    for path in sorted(RESOURCES.rglob("*.json")):
        load_json(path)


def audit_model_references() -> set[Path]:
    roots: set[Path] = set()
    reachable: set[Path] = set()
    reference_sources = [
        *sorted((ASSETS / "blockstates").glob("*.json")),
        *sorted((ASSETS / "items").glob("*.json")),
    ]
    for path in reference_sources:
        data = load_json(path)
        if data is None:
            continue
        for key, value in walk(data):
            if (
                key == "model"
                and isinstance(value, str)
                and value.startswith("vintner:")
            ):
                target = model_path(value)
                if target is not None and not target.is_file():
                    fail(
                        f"missing model {value!r} referenced by "
                        f"{relative(path)}"
                    )
                elif target is not None:
                    roots.add(target)
                    reachable.add(target)

    pending = list(reachable)
    while pending:
        path = pending.pop()
        data = load_json(path)
        if not isinstance(data, dict):
            continue
        parent = data.get("parent")
        if isinstance(parent, str) and parent.startswith("vintner:"):
            target = model_path(parent)
            if target is not None and not target.is_file():
                fail(
                    f"missing parent model {parent!r} referenced by "
                    f"{relative(path)}"
                )
            elif target is not None and target not in reachable:
                reachable.add(target)
                pending.append(target)

    return roots


def model_chain(path: Path) -> list[tuple[Path, dict[str, Any]]]:
    result: list[tuple[Path, dict[str, Any]]] = []
    visited: set[Path] = set()
    current = path
    while current.is_file():
        if current in visited:
            fail(f"model parent cycle: {relative(path)}")
            break
        visited.add(current)
        data = load_json(current)
        if not isinstance(data, dict):
            break
        result.append((current, data))
        parent = data.get("parent")
        if not isinstance(parent, str) or not parent.startswith("vintner:"):
            break
        next_path = model_path(parent)
        if next_path is None:
            break
        current = next_path
    result.reverse()
    return result


def audit_model_textures(paths: set[Path]) -> None:
    for path in sorted(paths):
        chain = model_chain(path)
        if not chain:
            continue

        texture_variables: dict[str, object] = {}
        for _, data in chain:
            textures = data.get("textures", {})
            if isinstance(textures, dict):
                texture_variables.update(
                    {
                        key: value
                        for key, value in textures.items()
                        if isinstance(key, str)
                        and isinstance(value, (str, dict))
                    }
                )

        referenced_variables: set[str] = set()
        for _, data in chain:
            for key, value in walk(data.get("elements", [])):
                if (
                    key == "texture"
                    and isinstance(value, str)
                    and value.startswith("#")
                ):
                    referenced_variables.add(value[1:])

        for variable in sorted(referenced_variables):
            seen: set[str] = set()
            current = variable
            while True:
                if current in seen:
                    fail(
                        f"texture variable cycle #{variable} in "
                        f"{relative(path)}"
                    )
                    break
                seen.add(current)
                value = texture_variables.get(current)
                if value is None:
                    fail(
                        f"unresolved texture variable #{variable} in "
                        f"{relative(path)}"
                    )
                    break
                if isinstance(value, dict):
                    sprite = value.get("sprite")
                    if not isinstance(sprite, str):
                        fail(
                            f"invalid texture object #{variable} in "
                            f"{relative(path)}"
                        )
                        break
                    target = texture_path(sprite)
                    if target is not None and not target.is_file():
                        fail(
                            f"missing texture {sprite!r} used by "
                            f"{relative(path)}"
                        )
                    break
                if value.startswith("#"):
                    current = value[1:]
                    continue
                target = texture_path(value)
                if target is not None and not target.is_file():
                    fail(
                        f"missing texture {value!r} used by "
                        f"{relative(path)}"
                    )
                break

        for value in texture_variables.values():
            if isinstance(value, dict):
                sprite = value.get("sprite")
                if isinstance(sprite, str):
                    target = texture_path(sprite)
                    if target is not None and not target.is_file():
                        fail(
                            f"missing texture {sprite!r} declared by "
                            f"{relative(path)}"
                        )
                continue
            if value.startswith("#"):
                continue
            target = texture_path(value)
            if target is not None and not target.is_file():
                fail(
                    f"missing texture {value!r} declared by "
                    f"{relative(path)}"
                )


def expected_resource_ids() -> tuple[set[str], set[str]]:
    public_blocks: set[str] = set()
    grapevines: set[str] = set()
    for wood in WOODS:
        public_blocks.update(
            {
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
            }
        )
        grapevines.update(
            {
                grapevine_id(wood, "red"),
                grapevine_id(wood, "white"),
            }
        )
    public_blocks.update(
        {
            "chestnut_aging_barrel",
            "neutral_aging_barrel",
            "large_cask",
        }
    )
    return public_blocks, grapevines


def audit_wood_families() -> tuple[set[str], set[str]]:
    public_blocks, grapevines = expected_resource_ids()
    compatibility_only = {
        "chestnut_aging_barrel",
        "neutral_aging_barrel",
        "large_cask",
    }
    lang = load_json(LANG_PATH)
    if not isinstance(lang, dict):
        lang = {}

    for block_id in sorted(public_blocks):
        paths = {
            "blockstate": ASSETS / f"blockstates/{block_id}.json",
            "item definition": ASSETS / f"items/{block_id}.json",
            "item model": ASSETS / f"models/item/{block_id}.json",
            "loot table": DATA / f"loot_table/blocks/{block_id}.json",
            "recipe": DATA / f"recipe/{block_id}.json",
            "recipe advancement": (
                DATA
                / f"advancement/recipes/vintner/{block_id}.json"
            ),
        }
        if block_id in compatibility_only:
            paths.pop("recipe")
            paths.pop("recipe advancement")
        for label, path in paths.items():
            if not path.is_file():
                fail(
                    f"{block_id}: missing {label}: {relative(path)}"
                )

        item_definition = load_json(paths["item definition"])
        expected_model = f"vintner:item/{block_id}"
        if (
            item_definition is not None
            and expected_model not in strings(item_definition)
        ):
            fail(
                f"{block_id}: item definition does not reference "
                f"{expected_model}"
            )

        namespaced_id = f"vintner:{block_id}"
        for label in ("loot table", "recipe", "recipe advancement"):
            if label not in paths:
                continue
            document = load_json(paths[label])
            if (
                document is not None
                and namespaced_id not in strings(document)
            ):
                fail(
                    f"{block_id}: {label} does not reference "
                    f"{namespaced_id}"
                )

        translation_key = f"block.vintner.{block_id}"
        if translation_key not in lang:
            fail(f"{block_id}: missing language key {translation_key}")

    for grapevine in sorted(grapevines):
        require_file(ASSETS / f"blockstates/{grapevine}.json")
        translation_key = f"block.vintner.{grapevine}"
        if translation_key not in lang:
            fail(f"{grapevine}: missing language key {translation_key}")

    return public_blocks, grapevines


def audit_cooperage_kits() -> None:
    lang = load_json(LANG_PATH)
    for item_id in (
        "coopers_mallet",
        "toasting_kit",
        "seasoning_kit",
        "cask_conversion_kit",
    ):
        paths = (
            ASSETS / f"items/{item_id}.json",
            ASSETS / f"models/item/{item_id}.json",
            DATA / f"recipe/{item_id}.json",
            DATA / f"advancement/recipes/vintner/{item_id}.json",
        )
        for path in paths:
            require_file(path)
        if isinstance(lang, dict):
            key = f"item.vintner.{item_id}"
            if key not in lang:
                fail(f"{item_id}: missing language key {key}")


def recipe_result_id(recipe: Any) -> str | None:
    if not isinstance(recipe, dict):
        return None
    result = recipe.get("result")
    if isinstance(result, str):
        return result
    if isinstance(result, dict) and isinstance(result.get("id"), str):
        return result["id"]
    return None


def ingredient_signature(value: Any) -> str:
    return json.dumps(value, sort_keys=True, separators=(",", ":"))


def audit_recipes() -> int:
    recipe_paths = sorted(RECIPE_PATH.glob("*.json"))
    advancement_paths = sorted(RECIPE_ADVANCEMENT_PATH.glob("*.json"))
    recipe_ids = {path.stem for path in recipe_paths}
    advancement_ids = {path.stem for path in advancement_paths}
    signatures: dict[str, list[str]] = {}

    for missing in sorted(recipe_ids - advancement_ids):
        fail(f"recipe {missing}: missing recipe advancement")
    for orphan in sorted(advancement_ids - recipe_ids):
        fail(f"recipe advancement {orphan}: missing recipe")

    for path in recipe_paths:
        recipe = load_json(path)
        if not isinstance(recipe, dict):
            continue

        recipe_id = f"vintner:{path.stem}"
        recipe_type = recipe.get("type")
        signature: Any = None
        if recipe_type == "minecraft:crafting_shaped":
            pattern = recipe.get("pattern")
            key = recipe.get("key")
            if (
                not isinstance(pattern, list)
                or not 1 <= len(pattern) <= 3
                or not all(isinstance(row, str) for row in pattern)
                or not pattern
                or not 1 <= len(pattern[0]) <= 3
                or any(len(row) != len(pattern[0]) for row in pattern)
            ):
                fail(f"recipe {path.stem}: invalid shaped pattern")
            elif not isinstance(key, dict):
                fail(f"recipe {path.stem}: invalid shaped key")
            else:
                used_symbols = set("".join(pattern)) - {" "}
                key_symbols = set(key)
                if used_symbols != key_symbols:
                    fail(
                        f"recipe {path.stem}: pattern symbols "
                        f"{sorted(used_symbols)} do not match key symbols "
                        f"{sorted(key_symbols)}"
                    )
                if not used_symbols:
                    fail(f"recipe {path.stem}: shaped pattern is empty")
                if any(
                    not isinstance(symbol, str)
                    or len(symbol) != 1
                    or symbol == " "
                    for symbol in key
                ):
                    fail(f"recipe {path.stem}: invalid shaped key symbol")
                grid = [
                    [None if symbol == " " else key.get(symbol) for symbol in row]
                    for row in pattern
                ]
                mirrored_grid = [list(reversed(row)) for row in grid]
                signature = "shaped:" + min(
                    ingredient_signature(grid),
                    ingredient_signature(mirrored_grid),
                )
        elif recipe_type in (
            "minecraft:crafting_shapeless",
            "vintner:grape_bowl",
        ):
            ingredients = recipe.get("ingredients")
            if (
                not isinstance(ingredients, list)
                or not 1 <= len(ingredients) <= 9
            ):
                fail(f"recipe {path.stem}: invalid shapeless ingredients")
            else:
                signature = "shapeless:" + ingredient_signature(
                    sorted(
                        (ingredient_signature(value) for value in ingredients)
                    )
                )
        else:
            fail(f"recipe {path.stem}: unsupported type {recipe_type!r}")

        if signature is not None:
            signatures.setdefault(signature, []).append(path.stem)

        result_id = recipe_result_id(recipe)
        if not isinstance(result_id, str):
            fail(f"recipe {path.stem}: missing result id")
        expected_result_id = RECIPE_OUTPUT_OVERRIDES.get(
            path.stem,
            recipe_id,
        )
        if (
            isinstance(result_id, str)
            and result_id != expected_result_id
        ):
            fail(
                f"recipe {path.stem}: result is {result_id}, expected "
                f"{expected_result_id}"
            )
        elif isinstance(result_id, str) and result_id.startswith("vintner:"):
            item_id = result_id.removeprefix("vintner:")
            if not (ASSETS / f"items/{item_id}.json").is_file():
                fail(
                    f"recipe {path.stem}: result {result_id} has no "
                    "item definition"
                )

        result = recipe.get("result")
        if isinstance(result, dict):
            count = result.get("count", 1)
            if not isinstance(count, int) or isinstance(count, bool) or count < 1:
                fail(f"recipe {path.stem}: invalid result count {count!r}")

        advancement_path = RECIPE_ADVANCEMENT_PATH / path.name
        if not advancement_path.is_file():
            continue
        advancement = load_json(advancement_path)
        if not isinstance(advancement, dict):
            continue
        rewards = advancement.get("rewards")
        rewarded_recipes = (
            rewards.get("recipes") if isinstance(rewards, dict) else None
        )
        if rewarded_recipes != [recipe_id]:
            fail(
                f"recipe advancement {path.stem}: rewards must be "
                f"exactly [{recipe_id!r}]"
            )
        criteria = advancement.get("criteria")
        recipe_condition = None
        if isinstance(criteria, dict):
            recipe_unlocked = criteria.get("has_the_recipe")
            if isinstance(recipe_unlocked, dict):
                conditions = recipe_unlocked.get("conditions")
                if isinstance(conditions, dict):
                    recipe_condition = conditions.get("recipe")
        if recipe_condition != recipe_id:
            fail(
                f"recipe advancement {path.stem}: has_the_recipe must "
                f"reference {recipe_id}"
            )

    for duplicate_ids in signatures.values():
        if len(duplicate_ids) > 1:
            fail(
                "conflicting crafting inputs: "
                + ", ".join(sorted(duplicate_ids))
            )

    return len(recipe_paths)


def audit_axe_tag(public_blocks: set[str]) -> None:
    data = load_json(AXE_TAG_PATH)
    values = set()
    if isinstance(data, dict) and isinstance(data.get("values"), list):
        values = {
            value
            for value in data["values"]
            if isinstance(value, str)
        }
    for block_id in sorted(public_blocks):
        namespaced_id = f"vintner:{block_id}"
        if namespaced_id not in values:
            fail(f"axe tag is missing {namespaced_id}")


def audit_translations() -> None:
    lang = load_json(LANG_PATH)
    if not isinstance(lang, dict):
        return
    for path in sorted(RESOURCES.rglob("*.json")):
        data = load_json(path)
        if data is None:
            continue
        for key, value in walk(data):
            if (
                key == "translate"
                and isinstance(value, str)
                and ".vintner." in value
                and value not in lang
            ):
                fail(
                    f"missing language key {value!r} referenced by "
                    f"{relative(path)}"
                )


def audit_serving_decor() -> None:
    try:
        check_contact()
    except (AssertionError, AttributeError, TypeError, OSError, KeyError, ValueError) as error:
        fail(f"serving decor bottle contact: {error}")
    cultivar_visuals = {
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
    palettes = set(cultivar_visuals.values())
    bowl_state = load_json(ASSETS / "blockstates/grape_bowl.json")
    variants = (
        bowl_state.get("variants")
        if isinstance(bowl_state, dict)
        else None
    )
    expected_states = {
        f"cultivar={cultivar},servings={servings}"
        for cultivar in cultivar_visuals
        for servings in range(5)
    }
    if not isinstance(variants, dict) or set(variants) != expected_states:
        fail("grape bowl blockstate must cover all 60 cultivar/serving states")

    empty_bowl_path = ASSETS / "models/block/grape_bowl_empty.json"
    require_file(empty_bowl_path)
    empty_bowl = load_json(empty_bowl_path)
    empty_elements = (
        empty_bowl.get("elements")
        if isinstance(empty_bowl, dict)
        else None
    )
    if not isinstance(empty_elements, list) or len(empty_elements) != 18:
        fail("grape bowl must retain its grounded stepped body and rim")
    elif min(float(element["from"][1]) for element in empty_elements) != 0.0:
        fail("grape bowl body must rest on the block surface")
    elif any(
        all(
            max(first["from"][axis], second["from"][axis])
            < min(first["to"][axis], second["to"][axis])
            for axis in range(3)
        )
        for index, first in enumerate(empty_elements)
        for second in empty_elements[index + 1:]
    ):
        fail("grape bowl body parts must not intersect each other")
    if isinstance(empty_elements, list):
        # Inspect every planar cell formed by the authored edges. Sampling
        # only the floor centre missed the open green seams along its sides.
        x_edges = sorted({4.0, 12.0, *(float(e[k][0]) for e in empty_elements for k in ("from", "to") if 4 <= e[k][0] <= 12)})
        z_edges = sorted({4.0, 12.0, *(float(e[k][2]) for e in empty_elements for k in ("from", "to") if 4 <= e[k][2] <= 12)})
        for x0, x1 in zip(x_edges, x_edges[1:]):
            for z0, z1 in zip(z_edges, z_edges[1:]):
                x, z = (x0 + x1) / 2, (z0 + z1) / 2
                if not any(e["from"][0] <= x <= e["to"][0]
                           and e["from"][2] <= z <= e["to"][2]
                           and e["from"][1] <= 0.8 <= e["to"][1] for e in empty_elements):
                    fail("grape bowl floor and side joins must have no through-gaps")
    require_file(ASSETS / "items/grape_bowl.json")
    for palette in sorted(palettes):
        require_file(
            ASSETS / f"textures/block/grape_bowl_{palette}_grapes.png"
        )
        for servings in range(5):
            require_file(
                ASSETS / f"items/grape_bowl_{palette}_{servings}.json"
            )
            if servings > 0:
                bowl_model_path = (
                    ASSETS
                    / f"models/block/grape_bowl_{palette}_{servings}.json"
                )
                require_file(bowl_model_path)
                bowl_model = load_json(bowl_model_path)
                geometry_parent = f"vintner:block/grape_bowl_geometry_{servings}"
                if bowl_model.get("parent") != geometry_parent:
                    fail(f"{bowl_model_path.name} must use shared serving geometry")
                geometry = load_json(ASSETS / f"models/block/grape_bowl_geometry_{servings}.json")
                bowl_elements = (
                    geometry.get("elements", [])
                    if isinstance(geometry, dict)
                    else []
                )
                grape_elements = [
                    element
                    for element in bowl_elements
                    if isinstance(element, dict)
                    and "#grapes" in strings(element.get("faces", {}))
                ]
                expected_grapes = (0, 63, 126, 189, 243)[servings]
                if len(grape_elements) != expected_grapes:
                    fail(
                        f"{bowl_model_path.name} must show exactly "
                        f"{expected_grapes} rounded berry parts"
                    )
                for grape in grape_elements:
                    start = grape.get("from")
                    end = grape.get("to")
                    if "rotation" in grape:
                        fail(
                            f"{bowl_model_path.name} grapes must not use "
                            "shard-like element rotations"
                        )
                    if (
                        not isinstance(start, list)
                        or not isinstance(end, list)
                        or len(start) != 3
                        or len(end) != 3
                        or any(float(end[index]) <= float(start[index]) for index in range(3))
                    ):
                        fail(
                            f"{bowl_model_path.name} grapes must use rounded "
                            "positive-volume berry slices"
                        )
                berries = {}
                for grape in grape_elements:
                    berries.setdefault(grape.get("name", ""), []).append(grape)
                for berry_name, parts in berries.items():
                    if not berry_name.startswith("bunch_") or len(parts) != 9:
                        fail(f"{bowl_model_path.name} must keep identifiable complete berries")
                    for axis in range(3):
                        low = min(part["from"][axis] for part in parts)
                        high = max(part["to"][axis] for part in parts)
                        if abs(high - low - 1.7) > 0.001:
                            fail(f"{bowl_model_path.name} berry must have a round, equal-axis envelope")
                    if any(
                        all(max(first["from"][axis], second["from"][axis])
                            < min(first["to"][axis], second["to"][axis]) - 0.00001
                            for axis in range(3))
                        for index, first in enumerate(parts) for second in parts[index + 1:]
                    ):
                        fail(f"{bowl_model_path.name} berry slices must not self-intersect")
                centres = {
                    name: tuple((min(p["from"][axis] for p in parts) + max(p["to"][axis] for p in parts)) / 2
                                for axis in range(3))
                    for name, parts in berries.items()
                }
                # A nonempty serving is one connected pile, including its
                # lowest state. Separate miniature bunches must not reappear.
                unseen = set(centres)
                connected = [unseen.pop()] if unseen else []
                while connected:
                    current = connected.pop()
                    neighbours = {name for name in unseen if math.dist(centres[current], centres[name]) <= 1.70}
                    unseen -= neighbours
                    connected.extend(neighbours)
                if unseen:
                    fail(f"{bowl_model_path.name} grapes must form a connected bushel without detached tails")
                if any(
                    part["from"][0] < 4.5 or part["to"][0] > 11.5
                    or part["from"][2] < 4.5 or part["to"][2] > 11.5
                    for part in grape_elements
                ):
                    fail(f"{bowl_model_path.name} grapes must fit inside the bowl floor")
                if servings > 1:
                    previous = load_json(ASSETS / f"models/block/grape_bowl_geometry_{servings - 1}.json")
                    current_by_name = {name: parts for name, parts in berries.items()}
                    for element in previous.get("elements", []):
                        if "#grapes" in strings(element.get("faces", {})):
                            if element not in current_by_name.get(element.get("name"), []):
                                fail(f"{bowl_model_path.name} eating must not move or replace remaining grapes")
                if min(float(start[1]) for start in (
                    element["from"] for element in grape_elements
                )) > 1.11:
                    fail(
                        f"{bowl_model_path.name} grapes must rest on the "
                        "bowl floor"
                    )
                stem_elements = [
                    element for element in bowl_elements
                    if isinstance(element, dict)
                    and "#stem" in strings(element.get("faces", {}))
                ]
                leaf_elements = [
                    element for element in bowl_elements
                    if isinstance(element, dict)
                    and "#leaf" in strings(element.get("faces", {}))
                ]
                expected_bunches = (0, 1, 2, 3, 3)[servings]
                if (
                    len(stem_elements) != expected_bunches
                    or len(leaf_elements) != expected_bunches
                ):
                    fail(
                        f"{bowl_model_path.name} must show {expected_bunches} "
                        "complete bunches with their own stem and leaf"
                    )

    expected_basket_states = {
        f"facing={facing},has_bottle={occupied}"
        for facing in ("north", "east", "south", "west")
        for occupied in ("false", "true")
    }
    for wood in WOODS:
        block_id = wine_basket_id(wood)
        basket_state = load_json(ASSETS / f"blockstates/{block_id}.json")
        basket_variants = (
            basket_state.get("variants")
            if isinstance(basket_state, dict)
            else None
        )
        if (
            not isinstance(basket_variants, dict)
            or set(basket_variants) != expected_basket_states
        ):
            fail(
                f"{block_id} blockstate must cover facing and occupancy states"
            )
        model_path = ASSETS / f"models/block/{block_id}.json"
        require_file(model_path)
        model = load_json(model_path)
        elements = model.get("elements") if isinstance(model, dict) else None
        if not isinstance(elements, list) or len(elements) != 30:
            fail(f"{block_id} must retain the shallow woven cradle and handle")
        elif min(float(element["from"][1]) for element in elements) != 0.0:
            fail(f"{block_id} basket body must rest on the block surface")
        elif any(
            isinstance(element, dict) and "rotation" in element
            for element in elements
        ):
            fail(f"{block_id} must not contain overlapping rotated basket parts")
        elif any(
            all(
                max(first["from"][axis], second["from"][axis])
                < min(first["to"][axis], second["to"][axis])
                for axis in range(3)
            )
            for index, first in enumerate(elements)
            for second in elements[index + 1:]
            if isinstance(first, dict)
            and isinstance(second, dict)
            and isinstance(first.get("from"), list)
            and isinstance(first.get("to"), list)
            and isinstance(second.get("from"), list)
            and isinstance(second.get("to"), list)
        ):
            fail(f"{block_id} basket parts must not intersect each other")
        for path in (
            ASSETS / f"items/{block_id}.json",
            DATA / f"recipe/{block_id}.json",
            DATA / f"advancement/recipes/vintner/{block_id}.json",
            DATA / f"loot_table/blocks/{block_id}.json",
        ):
            require_file(path)

    for style in ("red", "white", "aged_red", "aged_white"):
        model_id = f"wine_basket_bottle_{style}"
        definition_path = ASSETS / f"items/{model_id}.json"
        model_path = ASSETS / f"models/item/{model_id}.json"
        require_file(definition_path)
        require_file(model_path)
        bottle_model = load_json(model_path)
        bottle_elements = (
            bottle_model.get("elements")
            if isinstance(bottle_model, dict)
            else None
        )
        if not isinstance(bottle_elements, list) or len(bottle_elements) != 11:
            fail(f"{model_id} must retain the compact 3D bottle silhouette")

    for path in (
        DATA / "recipe/grape_bowl.json",
        DATA / "advancement/recipes/vintner/grape_bowl.json",
        DATA / "loot_table/blocks/grape_bowl.json",
        DATA / "tags/item/grapes.json",
        ROOT / "src/client/java/com/zenith/vintner/client/render/WineBasketRenderer.java",
    ):
        require_file(path)

    axe_tag = load_json(AXE_TAG_PATH)
    axe_values = (
        set(axe_tag.get("values", []))
        if isinstance(axe_tag, dict)
        else set()
    )
    for block_id in (
        "grape_bowl",
        *(wine_basket_id(wood) for wood in WOODS),
    ):
        if f"vintner:{block_id}" not in axe_values:
            fail(f"axe tag is missing vintner:{block_id}")

    lang = load_json(LANG_PATH)
    if isinstance(lang, dict):
        for key in (
            "block.vintner.grape_bowl",
            "item.vintner.grape_bowl_named",
            "tooltip.vintner.grape_bowl.servings",
            "tag.item.vintner.grapes",
            *(
                f"block.vintner.{wine_basket_id(wood)}"
                for wood in WOODS
            ),
        ):
            if key not in lang:
                fail(f"serving decor is missing language key {key}")


def audit_young_grapevine_wires(
    grapevines: set[str],
) -> None:
    directions = {"north", "east", "south", "west"}

    for grapevine in sorted(grapevines):
        path = ASSETS / f"blockstates/{grapevine}.json"
        data = load_json(path)
        if not isinstance(data, dict):
            continue

        covered: set[str] = set()
        for part in data.get("multipart", []):
            if not isinstance(part, dict):
                continue
            when = part.get("when")
            if not isinstance(when, dict):
                continue
            if (
                when.get("age") != "0|1"
                or when.get("upper") != "false"
                or when.get("has_above") != "false"
            ):
                continue

            for direction in directions:
                if when.get(direction) == "level":
                    covered.add(direction)

        missing = directions - covered
        if missing:
            fail(
                f"{grapevine}: young lower vine is missing wire "
                f"coverage without a stacked trellis for "
                f"{sorted(missing)}"
            )


def audit_fermentation_airlock_bounds() -> None:
    path = ASSETS / "models/block/fermentation_airlock.json"
    data = load_json(path)
    if not isinstance(data, dict):
        return

    for index, element in enumerate(data.get("elements", [])):
        if not isinstance(element, dict):
            continue
        for bound_name in ("from", "to"):
            bound = element.get(bound_name)
            if not isinstance(bound, list) or len(bound) != 3:
                fail(
                    f"fermentation airlock element {index} has an "
                    f"invalid {bound_name} bound"
                )
                continue
            if any(
                not isinstance(value, (int, float))
                or value < 0
                or value > 16
                for value in bound
            ):
                fail(
                    f"fermentation airlock element {index} extends "
                    f"outside one block: {bound_name}={bound}"
                )


def audit_barrel_status_indicators() -> None:
    indicator_path = (
        ASSETS / "models/block/barrel_status_indicator.json"
    )
    indicator = load_json(indicator_path)
    if isinstance(indicator, dict):
        elements = indicator.get("elements", [])
        if len(elements) != 1 or not isinstance(elements[0], dict):
            fail("barrel status indicator must contain one flush element")
        else:
            start = elements[0].get("from")
            end = elements[0].get("to")
            if (
                not isinstance(start, list)
                or not isinstance(end, list)
                or len(start) != 3
                or len(end) != 3
                or start[2] < -0.25
                or end[2] != 0
                or start[1] < 6
                or end[1] > 7
            ):
                fail(
                    "barrel status indicator must sit flush below the "
                    f"front tap handle: from={start}, to={end}"
                )

            faces = elements[0].get("faces")
            if not isinstance(faces, dict) or set(faces) != {
                "north",
                "east",
                "south",
                "west",
                "up",
                "down",
            }:
                fail(
                    "barrel status indicator must define all six faces"
                )
            else:
                for face_name, face in faces.items():
                    uv = face.get("uv") if isinstance(face, dict) else None
                    if (
                        not isinstance(uv, list)
                        or len(uv) != 4
                        or any(
                            not isinstance(value, (int, float))
                            or value < 0
                            or value > 16
                            for value in uv
                        )
                    ):
                        fail(
                            "barrel status indicator face "
                            f"{face_name} must use explicit in-bounds UVs: "
                            f"uv={uv}"
                        )

    aliases = (
        "cask_bung",
        "fermentation_airlock_indicator",
    )
    for alias in aliases:
        path = ASSETS / f"models/block/{alias}.json"
        data = load_json(path)
        if (
            isinstance(data, dict)
            and data.get("parent")
            != "vintner:block/barrel_status_indicator"
        ):
            fail(f"{alias} does not use the shared front indicator")

    rotations = {"north", "east", "south", "west"}
    fermentation_overlays = {
        "vintner:block/fermentation_barrel_red_fermenting",
        "vintner:block/fermentation_barrel_white_fermenting",
        "vintner:block/fermentation_barrel_ready",
    }
    for wood in WOODS:
        block_id = fermentation_id(wood)
        data = load_json(ASSETS / f"blockstates/{block_id}.json")
        if not isinstance(data, dict):
            continue
        coverage = {
            (apply.get("model"), when.get("facing"))
            for part in data.get("multipart", [])
            if isinstance(part, dict)
            and isinstance((when := part.get("when")), dict)
            and isinstance((apply := part.get("apply")), dict)
            and apply.get("model") in fermentation_overlays
        }
        expected = {
            (model, facing)
            for model in fermentation_overlays
            for facing in rotations
        }
        if coverage != expected:
            fail(
                f"{block_id}: fermenting status indicator is missing "
                "one or more facing/state combinations"
            )


def audit_estate_management_desk() -> None:
    block_id = "estate_management_desk"
    paths = {
        "blockstate": ASSETS / f"blockstates/{block_id}.json",
        "item definition": ASSETS / f"items/{block_id}.json",
        "block model": ASSETS / f"models/block/{block_id}.json",
        "item model": ASSETS / f"models/item/{block_id}.json",
        "loot table": DATA / f"loot_table/blocks/{block_id}.json",
        "recipe": DATA / f"recipe/{block_id}.json",
    }
    for label, path in paths.items():
        if not path.is_file():
            fail(f"{block_id}: missing {label}: {relative(path)}")

    item_definition = load_json(paths["item definition"])
    expected_model = f"vintner:item/{block_id}"
    if (
        item_definition is not None
        and expected_model not in strings(item_definition)
    ):
        fail(
            f"{block_id}: item definition does not reference "
            f"{expected_model}"
        )

    require_file(ROOT / "scripts/generate_estate_desk_assets.py")
    colors = {
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
    }
    overlay_colors = colors - {"green"}
    for color in overlay_colors:
        require_file(
            ASSETS
            / "models/block"
            / f"{block_id}_blotter_{color}.json"
        )
    require_file(ASSETS / f"models/block/{block_id}_ledger.json")
    require_file(ASSETS / f"models/block/{block_id}_map_frame.json")
    if (ASSETS / f"models/block/{block_id}_map.json").exists():
        fail(
            f"{block_id}: obsolete painted map model should be removed"
        )

    blockstate = load_json(paths["blockstate"])
    if not isinstance(blockstate, dict):
        return
    multipart = blockstate.get("multipart", [])
    if not isinstance(multipart, list):
        fail(f"{block_id}: blockstate must use multipart models")
        return

    facings = {"north", "east", "south", "west"}
    base_coverage: set[str] = set()
    ledger_coverage: set[str] = set()
    map_frame_coverage: set[str] = set()
    blotter_coverage: set[tuple[str, str]] = set()
    for part in multipart:
        if not isinstance(part, dict):
            continue
        when = part.get("when")
        apply = part.get("apply")
        if not isinstance(when, dict) or not isinstance(apply, dict):
            continue
        facing = when.get("facing")
        model = apply.get("model")
        if facing not in facings:
            continue
        if model == f"vintner:block/{block_id}" and len(when) == 1:
            base_coverage.add(facing)
        if (
            model == f"vintner:block/{block_id}_ledger"
            and when.get("has_ledger") == "true"
        ):
            ledger_coverage.add(facing)
        if (
            model == f"vintner:block/{block_id}_map_frame"
            and when.get("has_map") == "true"
        ):
            map_frame_coverage.add(facing)
        color = when.get("blotter_color")
        if (
            color in overlay_colors
            and model == f"vintner:block/{block_id}_blotter_{color}"
        ):
            blotter_coverage.add((facing, color))

    if base_coverage != facings:
        fail(f"{block_id}: base model is missing a facing")
    if ledger_coverage != facings:
        fail(f"{block_id}: ledger model is missing a facing")
    if map_frame_coverage != facings:
        fail(f"{block_id}: live map frame is missing a facing")
    expected_blotters = {
        (facing, color)
        for facing in facings
        for color in overlay_colors
    }
    if blotter_coverage != expected_blotters:
        fail(
            f"{block_id}: dyeable blotter is missing one or more "
            "color/facing combinations"
        )

    for wood in WOODS:
        variant_id = estate_desk_id(wood)
        variant_state = load_json(
            ASSETS / f"blockstates/{variant_id}.json"
        )
        if not isinstance(variant_state, dict):
            continue
        expected_base = f"vintner:block/{variant_id}"
        coverage = {
            part.get("when", {}).get("facing")
            for part in variant_state.get("multipart", [])
            if isinstance(part, dict)
            and isinstance(part.get("when"), dict)
            and isinstance(part.get("apply"), dict)
            and part["apply"].get("model") == expected_base
        }
        if coverage != facings:
            fail(f"{variant_id}: desk base model is missing a facing")
        audit_estate_workstation_connections(
            variant_id,
            variant_state.get("multipart", []),
            facings,
        )
        if any(
            f"vintner:block/{block_id}_map" in strings(part)
            for part in variant_state.get("multipart", [])
        ):
            fail(f"{variant_id}: still references the painted map model")


def audit_estate_workstation_connections(
    block_id: str,
    multipart: list[Any],
    facings: set[str],
) -> None:
    for side in ("left", "right"):
        model = f"vintner:block/{block_id}_connection_{side}"
        require_file(ASSETS / f"models/block/{block_id}_connection_{side}.json")
        coverage = {
            part.get("when", {}).get("facing")
            for part in multipart
            if isinstance(part, dict)
            and isinstance(part.get("when"), dict)
            and isinstance(part.get("apply"), dict)
            and part["apply"].get("model") == model
            and part["when"].get(f"{side}_connected") == "true"
        }
        if coverage != facings:
            fail(
                f"{block_id}: {side} workstation connection is missing "
                "a facing"
            )


def audit_surveyors_map_table() -> None:
    require_file(ROOT / "scripts/generate_surveyors_map_table_assets.py")
    require_file(
        ASSETS / "models/block/surveyors_map_table_maps.json"
    )
    facings = {"north", "east", "south", "west"}
    for wood in WOODS:
        block_id = surveyors_map_table_id(wood)
        blockstate = load_json(ASSETS / f"blockstates/{block_id}.json")
        if not isinstance(blockstate, dict):
            continue
        multipart = blockstate.get("multipart")
        if not isinstance(multipart, list):
            fail(f"{block_id}: blockstate must use multipart models")
            continue

        base_model = f"vintner:block/{block_id}"
        overlay_model = "vintner:block/surveyors_map_table_maps"
        base_coverage = {
            part.get("when", {}).get("facing")
            for part in multipart
            if isinstance(part, dict)
            and isinstance(part.get("when"), dict)
            and isinstance(part.get("apply"), dict)
            and part["apply"].get("model") == base_model
            and len(part["when"]) == 1
        }
        overlay_coverage = {
            part.get("when", {}).get("facing")
            for part in multipart
            if isinstance(part, dict)
            and isinstance(part.get("when"), dict)
            and isinstance(part.get("apply"), dict)
            and part["apply"].get("model") == overlay_model
            and part["when"].get("has_maps") == "true"
        }
        if base_coverage != facings:
            fail(f"{block_id}: table base model is missing a facing")
        if overlay_coverage != facings:
            fail(f"{block_id}: stored-map overlay is missing a facing")
        audit_estate_workstation_connections(
            block_id,
            multipart,
            facings,
        )


def audit_vintage_archive_connections() -> None:
    facings = {"north", "east", "south", "west"}
    for wood in WOODS:
        block_id = archive_id(wood)
        blockstate = load_json(ASSETS / f"blockstates/{block_id}.json")
        if not isinstance(blockstate, dict):
            continue
        multipart = blockstate.get("multipart")
        if not isinstance(multipart, list):
            fail(f"{block_id}: blockstate must use multipart models")
            continue
        base_model = f"vintner:block/{block_id}"
        base_coverage = {
            part.get("when", {}).get("facing")
            for part in multipart
            if isinstance(part, dict)
            and isinstance(part.get("when"), dict)
            and isinstance(part.get("apply"), dict)
            and part["apply"].get("model") == base_model
            and len(part["when"]) == 1
        }
        if base_coverage != facings:
            fail(f"{block_id}: archive base model is missing a facing")
        audit_estate_workstation_connections(
            block_id,
            multipart,
            facings,
        )


def audit_notification_delivery() -> None:
    require_file(NOTIFICATION_GATE_PATH)
    for path in sorted(JAVA_SOURCE.rglob("*.java")):
        if path == NOTIFICATION_GATE_PATH:
            continue
        try:
            lines = path.read_text().splitlines()
        except (OSError, UnicodeError) as error:
            fail(
                "cannot inspect notification delivery: "
                f"{relative(path)}: {error}"
            )
            continue
        for line_number, line in enumerate(lines, start=1):
            if ".sendSystemMessage(" in line:
                fail(
                    f"{relative(path)}:{line_number}: Vintner notifications "
                    "must use VintnerNotifications to avoid client/server duplicates"
                )


def main() -> int:
    audit_all_json()
    reachable_models = audit_model_references()
    audit_model_textures(reachable_models)
    public_blocks, grapevines = audit_wood_families()
    audit_cooperage_kits()
    recipe_count = audit_recipes()
    audit_axe_tag(public_blocks)
    audit_translations()
    audit_serving_decor()
    audit_young_grapevine_wires(grapevines)
    audit_fermentation_airlock_bounds()
    audit_barrel_status_indicators()
    audit_estate_management_desk()
    audit_surveyors_map_table()
    audit_vintage_archive_connections()
    audit_notification_delivery()

    if errors:
        print(
            f"Vintner release asset audit failed with "
            f"{len(errors)} error(s):",
            file=sys.stderr,
        )
        for error in errors:
            print(f"  - {error}", file=sys.stderr)
        return 1

    print(
        "Vintner release asset audit passed: "
        f"{len(documents)} JSON files, "
        f"{recipe_count} recipes, "
        f"{len(public_blocks)} public wood-family blocks, "
        f"{len(grapevines)} wood-preserving grapevine states."
    )
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
