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
weight 1. The same runtime injection also adds one Winemaker house and one
Cooper house at weight 1. `JigsawPlacementPlacerMixin` gives the vineyard
candidate first-fit priority until one succeeds, then removes all further
vineyard candidates so no village can receive more than one. No processor list
replaces the vineyard workstation.

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

## Natural-generation discoverability correction

An exact fresh-world sample reproduced the reported low appearance rate: only
two of ten naturally generated Plains villages contained the vineyard under
the former weighted-pool policy. The raised connector/lowered building still
generated at the same seed and anchor, ruling out the height adjustment as the
cause. A weight-only increase did not reliably improve the result and was not
retained.

Village jigsaw placement now tries one culture-matched vineyard candidate
first at eligible house connections until a collision-free placement succeeds,
then removes vineyard candidates for the rest of that village. The same sample
improved to five of ten natural Plains villages, with at most one winery in
each. Villages whose piece layout cannot accommodate the winery's footprint
remain valid misses rather than receiving intersecting or forced structures.
Only newly generated villages and unexplored chunks use this policy; existing
saved villages are unchanged.

## Validation status

The focused vineyard suite passes 4/4. Standard and Serene Seasons GameTests
each pass 197/197, an increase of three tests over the accepted 194 baseline.
Clean build, advancement (218 total: 22 visible plus 196 recipe unlocks),
release-resource (2,025 JSON/196 recipes), Blockbench (907 models), Nursery,
serving/contact and vineyard audits pass. Blockstate/model/PNG counts remain
215/907/100, and two consecutive datagen runs report zero generated files and
zero churn.

The latest exact candidate is `vintner-1.4.0.jar`, 1,584,942 bytes, with
SHA-256
`b9a063e482e246c943857284cfc3e673244a3d17c6871cf6d9d289bc611f25cf`.
It supersedes the lower-placement player-winery candidate
`cc7bdeb02b78af5aee5ba6cd291efe830debde285fd460ff3369f5cc5478d52c`.

An exact-JAR Minecraft 26.3 dedicated server loaded Vintner 1.4.0, Fabric API
0.161.0+26.3 and exact Commons 0.1.2, injected all fifteen Vintner pool
entries, generated real Plains villages and placed the rebuilt Grape Press at
`(584, 80, -25)`. The corresponding POI region contains
`vintner:winemaker`; an eligible villager in that generated work area claimed
the site and became `vintner:winemaker`. After a save, clean stop and full
server restart, that villager still loaded as `vintner:winemaker`. The exact
client loaded the same JAR, completed Vintner/Commons client initialization,
rebuilt the block and item atlases, initialized audio, connected to the exact
server and disconnected cleanly when the server stopped. No structure,
registry, model or texture load error occurred.

The exact candidate, logs, test XML and screenshots are frozen in the external
evidence directory above. The human checklist is
`docs/qa/village-vineyard-manual-acceptance-1.4.0.md` and remains pending.

## Intentionally deferred

- Expansion to new village types or new structure families.
- Wild vineyards, estate-scale wineries and other future worldgen content.
- Subjective shader/no-shader promotional acceptance by Zach.
