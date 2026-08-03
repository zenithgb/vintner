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
    estate_desk_id,
    fermentation_id,
    grapevine_id,
    press_id,
    rack_id,
    shelf_id,
    stand_id,
    surveyors_map_table_id,
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
        if any(
            f"vintner:block/{block_id}_map" in strings(part)
            for part in variant_state.get("multipart", [])
        ):
            fail(f"{variant_id}: still references the painted map model")


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


def main() -> int:
    audit_all_json()
    reachable_models = audit_model_references()
    audit_model_textures(reachable_models)
    public_blocks, grapevines = audit_wood_families()
    audit_cooperage_kits()
    audit_axe_tag(public_blocks)
    audit_translations()
    audit_young_grapevine_wires(grapevines)
    audit_fermentation_airlock_bounds()
    audit_barrel_status_indicators()
    audit_estate_management_desk()
    audit_surveyors_map_table()

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
        f"{len(public_blocks)} public wood-family blocks, "
        f"{len(grapevines)} wood-preserving grapevine states."
    )
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
