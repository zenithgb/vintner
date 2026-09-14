# Nursery Bed Blockbench source

`nursery_bed.bbmodel` is the authoritative editable source for the Nursery
Bed frame and all four growth geometries. It is a grouped free-model assembly
so each `growth_age_*` group can be shown independently with `bed_frame` in
Blockbench. The embedded textures make the source immediately reviewable.

`nursery_bed_item.bbmodel` is the Blockbench item-presentation companion. It
contains the exact frame plus representative age-2 growth and the reviewed GUI,
hand, ground and fixed transforms. Runtime geometry remains derived from the
authoritative grouped source; the focused audit rejects geometry drift between
the two files.

After editing in Blockbench, export deterministically and validate with:

```sh
python3 scripts/export_nursery_bed_models.py
python3 scripts/audit_nursery_bed_models.py
```

The exporter writes the common frame, four growth geometries, 16 plant-family
wrappers and the item model. Plant-family wrappers intentionally preserve the
existing red/white grapevine, hanging-roots and crimson-roots textures. A
cultivar-specific renderer is out of scope because exact cultivar identity is
server-side saved data rather than a Nursery Bed blockstate property.

Final authored envelope and review notes:

- frame/soil body: 16 x 5 x 16 pixels, 10 elements;
- soil: 1.75..14.25 x 0..4.25 x 1.75..14.25, recessed 0.75 pixels;
- six crossed cuttings in a 2 x 3 propagation layout;
- growth tops: 6.5, 8, 10 and 12 pixels for ages 0..3;
- no rotated or coplanar duplicate elements; perpendicular plant planes cross
  intentionally and begin 0.125 pixels below the soil surface;
- Java selection/collision shape remains the matching simple 16 x 5 x 16 box.
