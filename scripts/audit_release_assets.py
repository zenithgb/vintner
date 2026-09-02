#!/usr/bin/env python3
"""Audit Vintner resources that must be complete before a public release."""

from __future__ import annotations

import json
import sys
from collections.abc import Iterable
from pathlib import Path
from typing import Any

from generate_wood_variants import (
    WOODS,
    aging_id,
    archive_id,
    cabinet_id,
    crate_id,
    fermentation_id,
    grapevine_id,
    press_id,
    rack_id,
    shelf_id,
    stand_id,
    tasting_service_id,
    trellis_id,
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
        elif recipe_type == "minecraft:crafting_shapeless":
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
        elif result_id != recipe_id:
            fail(
                f"recipe {path.stem}: result is {result_id}, expected "
                f"{recipe_id}"
            )
        elif result_id.startswith("vintner:"):
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


def main() -> int:
    audit_all_json()
    reachable_models = audit_model_references()
    audit_model_textures(reachable_models)
    public_blocks, grapevines = audit_wood_families()
    audit_cooperage_kits()
    recipe_count = audit_recipes()
    audit_axe_tag(public_blocks)
    audit_translations()
    audit_young_grapevine_wires(grapevines)
    audit_fermentation_airlock_bounds()

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
