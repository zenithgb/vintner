"""Shared modular furniture dimensions, in Minecraft model pixels.

All three workstations use this carcass. Keep accessories above the 11.5-pixel
desktop and inside its edges; only the connection pieces reach X=0/16.
"""

from __future__ import annotations


def cuboid(start, end, texture, top=None, name=None):
    element = {
        "from": start,
        "to": end,
        "faces": {
            side: {"texture": top if side == "up" and top else texture}
            for side in ("north", "east", "south", "west", "down", "up")
        },
    }
    if name:
        element["name"] = name
    return element


def carcass(texture, top=None):
    elements = []
    for x0, x1 in ((1.0, 2.5), (13.5, 15.0)):
        for z0, z1 in ((1.5, 3.0), (12.5, 14.0)):
            elements.append(cuboid([x0, 0, z0], [x1, 10.25, z1], texture, name="leg"))
            elements.append(cuboid(
                [x0 - .25, 0, z0 - .25], [x1 + .25, .5, z1 + .25],
                texture, name="foot",
            ))
    for start, end in (
        ([1.25, 3.25, 3], [2.75, 4.25, 12.5]),
        ([13.25, 3.25, 3], [14.75, 4.25, 12.5]),
        ([2.5, 3.25, 1.5], [13.5, 4.25, 3]),
        ([2.5, 3.25, 12.5], [13.5, 4.25, 14]),
    ):
        elements.append(cuboid(start, end, texture, name="stretcher"))
    elements.extend((
        cuboid([.5, 10.25, 1], [15.5, 11.5, 15], texture, top, "desktop"),
        cuboid([.25, 9.75, .75], [15.75, 10.5, 2.25], texture, name="front_moulding"),
        cuboid([1, 11.5, 13.25], [15, 14.25, 14.75], texture, name="rear_rail"),
        cuboid([.75, 14.25, 13], [15.25, 14.75, 15], texture, top, "rear_crown"),
    ))
    return elements


def connection(left, texture, top=None):
    """Identical end profiles let every same-facing family pair meet flush."""
    bounds = (
        ("desktop", 0.5, 10.25, 11.5, 1, 15, top),
        ("front_moulding", 0.25, 9.75, 10.5, .75, 2.25, None),
        ("rear_rail", 1, 11.5, 14.25, 13.25, 14.75, None),
        ("rear_crown", .75, 14.25, 14.75, 13, 15, top),
    )
    return [
        cuboid(
            [0 if left else 16 - inset, y0, z0],
            [inset if left else 16, y1, z1], texture, face_top, name,
        )
        for name, inset, y0, y1, z0, z1, face_top in bounds
    ]


def archive_model():
    elements = carcass("#wood")
    # Cabinet storage sits inside the common feet and desktop envelope.
    elements.extend((
        cuboid([1.25, .5, 1.5], [14.75, 1.5, 14], "#wood", name="cabinet_plinth"),
        cuboid([1.5, 1.5, 1.3], [14.5, 9.75, 14], "#wood", name="cabinet"),
    ))
    for x0, x1 in ((2, 7.5), (8.5, 14)):
        for y0, y1 in ((2, 5.5), (6, 9.5)):
            elements.extend((
                cuboid([x0, y0, .85], [x1, y1, 1.3], "#wood"),
                cuboid([x0 + 1.6, y0 + 1, .55], [x0 + 3.9, y0 + 2.2, .85], "#metal"),
                cuboid([x0 + 2, y0 + 1.35, .4], [x0 + 3.5, y0 + 1.85, .55], "#paper"),
            ))
    # Keep the archive's books and open ledger, seated within the shared rail.
    for x0, x1 in ((1.25, 2.25), (13.75, 14.75)):
        elements.append(cuboid([x0, 11.5, 10.75], [x1, 14.25, 14.25], "#wood"))
    for x0, x1 in ((2.25, 7.5), (8.5, 13.75)):
        books = cuboid([x0, 11.5, 10.75], [x1, 14.25, 13.25], "#wood")
        books["faces"]["north"]["texture"] = "#records"
        elements.append(books)
    elements.extend((
        cuboid([3.75, 11.5, 2], [12.25, 11.75, 7.5], "#cover"),
        cuboid([4, 11.75, 2.25], [7.85, 12.05, 7.25], "#paper"),
        cuboid([8.15, 11.75, 2.25], [12, 12.05, 7.25], "#paper"),
        cuboid([7.85, 12, 2.25], [8.15, 12.25, 7.25], "#ink"),
        cuboid([12.6, 11.5, 3.25], [13.6, 12.5, 4.25], "#ink"),
    ))
    return {
        "parent": "minecraft:block/block",
        "textures": {
            "wood": "minecraft:block/oak_planks",
            "records": "minecraft:block/chiseled_bookshelf_occupied",
            "paper": "minecraft:block/quartz_block_top",
            "ink": "minecraft:block/black_wool",
            "cover": "minecraft:block/red_terracotta",
            "metal": "minecraft:block/gold_block",
            "particle": "minecraft:block/oak_planks",
        },
        "elements": elements,
    }
