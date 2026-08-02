# Phase 7D — Physical Estate Upgrades

Phase 7D begins the estate-upgrade roadmap with facilities that are built in
the world. There is no upgrade shop and no detached management screen.

## Implemented facilities

- **Barrel workshop:** at least two aging barrels mounted directly on barrel
  stands.
- **Temperature-controlled cellar:** at least two cellar stations in
  ideal cellar conditions.
- **Warehouse:** at least four wine racks, wine crates, labelled cellar
  shelves, or tasting cabinets in the surveyed area.
- **Tasting room:** a tasting cabinet and Vintage Archive in the surveyed
  area.

Sneak-use the Vintner's Almanac in the air after founding an estate to survey
the facilities within 16 horizontal and 8 vertical blocks of the player.
Removing a required block removes the facility from the next survey.

## Functional ageing benefit

The ageing-barrel contribution is fixed when the batch finishes:

The stand activates the facility bonus; unmounted barrels retain the existing
ageing balance regardless of their surroundings.

| Mounted-barrel conditions | Quality contribution |
| --- | ---: |
| Poor cellar | 0 |
| Basic cellar | +1 |
| Good cellar | +2 |
| Ideal cellar | +3 |

Use the Vintner's Almanac on an aging barrel to see its current projected
facility contribution. Moving or removing the equipment after a batch has
finished does not rewrite that finished batch.

## Manual acceptance

1. Place two barrel stands and put an aging barrel directly above each one.
2. Build an underground, sheltered, dark cellar with nearby water and no heat
   or piston disturbance.
3. Add four storage fixtures, plus a tasting cabinet and Vintage Archive.
4. Sneak-use the Almanac in the air. Workshop, cellar, warehouse, and tasting
   room should all report `Ready` when their physical requirements are met.
5. Remove one mounted barrel and survey again. The workshop should fall back
   to `1/2`.
6. Inspect a mounted barrel in the ideal cellar with the Almanac. It should
   show a `+3` projected facility contribution.
7. Finish identical batches in an ideal mounted barrel and an exposed,
   unmounted barrel. The mounted-cellar batch should receive the higher ageing
   quality score.

## Deferred roadmap items

Better presses, larger vats, bottling lines, guest houses, and merchant offices
need their own distinct physical builds and are not simulated by this phase.
