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
    fermentation_id,
    grapevine_id,
    press_id,
    rack_id,
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

        texture_variables: dict[str, str] = {}
        for _, data in chain:
            textures = data.get("textures", {})
            if isinstance(textures, dict):
                texture_variables.update(
                    {
                        key: value
                        for key, value in textures.items()
                        if isinstance(key, str)
                        and isinstance(value, str)
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
            }
        )
        grapevines.update(
            {
                grapevine_id(wood, "red"),
                grapevine_id(wood, "white"),
            }
        )
    return public_blocks, grapevines


def audit_wood_families() -> tuple[set[str], set[str]]:
    public_blocks, grapevines = expected_resource_ids()
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


def main() -> int:
    audit_all_json()
    reachable_models = audit_model_references()
    audit_model_textures(reachable_models)
    public_blocks, grapevines = audit_wood_families()
    audit_axe_tag(public_blocks)
    audit_translations()

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
