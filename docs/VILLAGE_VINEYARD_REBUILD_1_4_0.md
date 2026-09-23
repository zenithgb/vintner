# Vintner 1.4.0 Village Vineyard Rebuild

## Scope and baseline

This focused 1.4.0 pass starts from accepted commit
`4f9fb8bc803476303c798e9533d5f6092f540921` on
`phase/1.4.0-blockbench-final-audit`. It changes the existing village
vineyard/Winemaker workplace only; trades, profession progression, villager AI,
crop balance, recipes, estate systems, machine behavior and storage are not
redesigned.

The previous five templates were 11 x 5 x 9 raised plots containing two vine
rows and a level-4 `minecraft:composter`. They had no building or interior.
The Composter was authored directly by `scripts/generate_village_vineyards.py`
at local position `(9, 2, 4)`; it was not introduced by a processor.

## Generation architecture

`scripts/generate_village_vineyards.py` is the authoritative source for these
five direct NBT templates:

- `vintner:village/plains/vineyard`
- `vintner:village/desert/vineyard`
- `vintner:village/savanna/vineyard`
- `vintner:village/snowy/vineyard`
- `vintner:village/taiga/vineyard`

`ModVillageStructures` injects each template as a rigid
`SinglePoolElement` into the corresponding vanilla village houses pool with
weight 24. The same runtime injection also adds one Winemaker house and one
Cooper house at weight 1. `JigsawPlacementPlacerMixin` limits vineyards to one
per village. No processor list replaces the vineyard workstation.

Supported village palettes remain exactly Plains, Desert, Savanna, Snowy and
Taiga. Plains uses oak, Desert and Savanna use acacia, and Snowy and Taiga use
spruce. Desert uses smooth sandstone for the lower wall course; the other
palettes use cobblestone.

## Winemaker identity

- Profession: `vintner:winemaker`
- POI: `vintner:winemaker`
- Workstation state set: every registered Vintner Grape Press block state

The natural template workstation is palette-matched:

- Plains: `vintner:grape_press[facing=west]`
- Desert/Savanna: `vintner:acacia_grape_press[facing=west]`
- Snowy/Taiga: `vintner:spruce_grape_press[facing=west]`

The Composter and therefore the accidental Farmer POI are absent from every
replacement template.

## Structural redesign

Each vineyard is now a 15 x 10 x 13 village-scale cottage winery with:

- a full two-layer buried foundation and stripped-log retaining edge;
- a real central entrance and connected U-shaped work path;
- two deliberately spaced five-vine rows, with ten age-3 plants total;
- palette-matched walls, lower masonry course and timber corner posts;
- a two-block wooden door, seven windows and clear headroom;
- a complete stair-gable roof with overhang and slab ridge;
- an accessible culture-matched Grape Press at local `(11, 3, 6)`;
- a culture-matched empty Wine Rack, barrel storage and lantern;
- an unobstructed door aisle and west-side approach to the Grape Press.

This keeps the planted area modest and established without adding rare
cultivars or a high-value harvest. The interior communicates winemaking without
turning the building into a machine showcase.

## Rotation and block-state correction

Structure placement exposed an existing production defect: vanilla rotated the
template geometry, but `GrapePressBlock` and `WineRackBlock` did not implement
state rotation/mirroring, so their facing remained west. Both blocks now use
Minecraft's horizontal facing rotation and mirror contracts. Focused placement
tests exercise the actual structure template at 0, 90, 180 and 270 degrees and
assert the transformed press, rack, door and approach geometry.

## Determinism and integrity

The generator now supports `--check`. Gradle runs this byte-for-byte
determinism check and `scripts/audit_village_vineyards.py` as part of the
release asset audit. The structural audit decodes each NBT, verifies the full
1,950-position volume, data version, bounds, block states, unique jigsaw,
palette workstation/rack, entrance aisle, foundation, vines, roof and all four
rotations. It rejects Composters, debug/structure/command/barrier blocks,
invalid identifiers and malformed coordinates.

Current direct audit result: five of five templates pass. Each contains one
valid Grape Press, ten plants/twenty vine blocks, one street jigsaw, no
Composter and no forbidden block.

## Focused behavior coverage

The GameTest suite now proves:

1. all five templates contain their valid palette Grape Press and no Composter;
2. all four rotations preserve positions and block facing;
3. the full foundation remains supported on flat, one-block and two-block
   village-grade terrain steps;
4. an unemployed villager spawned outside the cottage reaches the generated
   press, claims `vintner:winemaker`, becomes `vintner:winemaker`, retains the
   job-site memory and leaves the Winemaker POI registered after claiming.

The profession test uses a supported floor and a deliberate path distance; it
does not use the earlier interaction-threshold edge case.

## Runtime generation and visual evidence

A disposable Minecraft 26.3 server/client world placed the old and rebuilt
templates and generated a Plains village through
`/place structure minecraft:village_plains`. The runtime jigsaw path selected
the rebuilt vineyard, created its Winemaker POI at world position `(62, 74,
113)`, connected the entrance path to the village road and logged no structure
or parsing exception. The disposable world was saved for a subsequent restart
check of that same POI. The final structure was inspected from the exterior,
front, interior, ordinary terrain and generated-village context.

The evidence set is frozen outside the repository at:

`/Users/zachariaheverson/Developer/Minecraft/vintner-release-evidence/1.4.0-village-vineyard-rebuild-20260923/`

It includes the old terrace, final exterior/front/interior/terrain views, an
actual jigsaw-generated village context, focused/full test logs and exact-JAR
runtime logs. Screenshots demonstrate review readiness but do not replace
Zach's manual visual verdict.

## Validation status

The focused vineyard suite passes 4/4. Standard and Serene Seasons GameTests
each pass 197/197, an increase of three tests over the accepted 194 baseline.
Clean build, advancement (218 total: 22 visible plus 196 recipe unlocks),
release-resource (2,025 JSON/196 recipes), Blockbench (907 models), Nursery,
serving/contact and vineyard audits pass. Blockstate/model/PNG counts remain
215/907/100, and two consecutive datagen runs report zero generated files and
zero churn. The generated-village POI remains discoverable at the same world
position after save and full server restart.

Exact candidate identity and exact-JAR runtime results are recorded in the
evidence manifest after the committed-source gates complete. The human checklist is
`docs/qa/village-vineyard-manual-acceptance-1.4.0.md` and remains pending.

## Intentionally deferred

- Expansion to new village types or new structure families.
- Wild vineyards, estate-scale wineries and other future worldgen content.
- Subjective shader/no-shader promotional acceptance by Zach.
