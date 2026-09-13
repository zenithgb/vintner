#!/usr/bin/env python3
"""Check the actual rendered bottle pose against the authored basket solids."""

import itertools
import json
import math
import re
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]


def check_contact() -> None:
    renderer = (ROOT / "src/client/java/com/zenith/vintner/client/render/WineBasketRenderer.java").read_text()
    # Read the renderer values so editing only Java cannot leave this check
    # testing a stale copy of the pose.
    height = float(re.search(r"translate\(0\.5F, ([\d.]+)F / 16\.0F", renderer)[1])
    offset_match = re.search(r"translate\((-?[\d.]+)F / 16\.0F, 0\.0F, (-?[\d.]+)F / 16\.0F", renderer)
    offset, offset_z = (float(offset_match[index]) for index in (1, 2))
    rotations = [(axis, float(angle)) for axis, angle in re.findall(
        r"Axis\.([XYZ])P.rotationDegrees\((-?[\d.]+)F\)", renderer)]
    scale = float(re.search(r"poseStack.scale\(([\d.]+)F", renderer)[1])

    def rotate(point, sequence=rotations):
        x, y, z = point
        for axis, angle in reversed(sequence):
            c, s = math.cos(math.radians(angle)), math.sin(math.radians(angle))
            if axis == "X":
                y, z = c * y - s * z, s * y + c * z
            elif axis == "Y":
                x, z = c * x + s * z, -s * x + c * z
            else:
                x, y = c * x - s * y, s * x + c * y
        return x, y, z

    def world(point):
        x, y, z = rotate(tuple(scale * (v - 8) for v in point))
        return (x + 8 + offset, y + height, z + 8 + offset_z)

    def vertices(element, transform=lambda p: p):
        return [transform(p) for p in itertools.product(*zip(element["from"], element["to"]))]

    def dot(a, b):
        return sum(x * y for x, y in zip(a, b))

    def cross(a, b):
        return (a[1] * b[2] - a[2] * b[1], a[2] * b[0] - a[0] * b[2],
                a[0] * b[1] - a[1] * b[0])

    basis = [(1, 0, 0), (0, 1, 0), (0, 0, 1)]
    bottle_axes = [rotate(axis) for axis in basis]
    axes = basis + bottle_axes + [cross(a, b) for a in basis for b in bottle_axes]
    axes = [axis for axis in axes if dot(axis, axis) > 1e-8]

    def penetration(first, second):
        return min(
            (min(max(dot(p, axis) for p in first), max(dot(p, axis) for p in second))
             - max(min(dot(p, axis) for p in first), min(dot(p, axis) for p in second)))
            / math.sqrt(dot(axis, axis)) for axis in axes
        )

    assets = ROOT / "src/main/resources/assets/vintner/models"
    basket = json.loads((assets / "block/wine_basket.json").read_text())["elements"]
    for variant_path in (assets / "block").glob("*wine_basket.json"):
        variant = json.loads(variant_path.read_text())["elements"]
        assert variant == basket, f"{variant_path.name}: wood variants must share the approved basket geometry"
    target = json.loads((ROOT / "scripts/fixtures/wine_basket_approved_pose.json").read_text())["bottle"]

    def approved_vertices(element):
        rx, ry, rz = element["rotation"]
        def transform(point):
            rotated = rotate(tuple(value - centre for value, centre in zip(point, element["origin"])),
                             [("Z", rz), ("Y", ry), ("X", rx)])
            return tuple(value + centre + offset for value, centre, offset in
                         zip(rotated, element["origin"], (8, 0, 8)))
        return vertices(element, transform)

    approved = [approved_vertices(element) for element in target]
    for style in ("red", "white", "aged_red", "aged_white"):
        bottle = json.loads((assets / f"item/wine_basket_bottle_{style}.json").read_text())["elements"]
        rendered = [vertices(part, world) for part in bottle]
        bottom = min(p[1] for points in rendered for p in points)
        # Preserve the exact approved pose, including its small 0.1373-unit
        # clearance above the slats, rather than silently changing its angle.
        assert 0.55 <= bottom <= 0.70, f"{style}: approved base clearance changed: {bottom}"
        assert len(rendered) == len(approved), f"{style}: approved bottle part count changed"
        for index, (points, expected) in enumerate(zip(rendered, approved)):
            assert all(min(math.dist(point, match) for match in expected) < 0.0001 for point in points), (
                f"{style}: bottle part {index} no longer matches the user-approved Blockbench pose")
        for index, points in enumerate(rendered):
            for basket_index, solid in enumerate(basket):
                # The approved body rests into the rear rim. Retain only its
                # two measured contact regions; all other solids must clear.
                allowed = {(0, 14): 0.1015, (1, 14): 0.3625}.get((index, basket_index), 0.0001)
                assert penetration(points, vertices(solid)) <= allowed, (
                    f"{style}: bottle part {index} clips basket part {basket_index}")


if __name__ == "__main__":
    check_contact()
    print("Serving decor pose passed: all wood variants and four bottle styles match the approved pose; only the two approved rear-rim contacts remain.")
