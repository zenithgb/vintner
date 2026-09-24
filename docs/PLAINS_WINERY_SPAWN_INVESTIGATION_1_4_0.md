# Vintner 1.4.0 Plains Winery Spawn Investigation

## Scope and method

This pass investigates only natural generation of
`vintner:village/plains/vineyard`. It uses Minecraft 26.3, Java 25, Fabric
Loader 0.19.5, Fabric API 0.161.0+26.3 and exact Commons 0.1.2. All comparison
worlds use seed 1400 and the same 25 deterministic natural Plains-village
targets. A hit means the exact vineyard template occurs in the village
structure-start children; a nearby `vintner:winemaker` POI is not sufficient
because the separate Winemaker specialist house uses the same POI.

## Runtime pool and lifecycle

Before Vintner injection, `minecraft:village/plains/houses` had 37 raw entries
and effective weight 87. The accepted runtime added the winery at weight 24,
for 40 raw entries and effective weight 113 after the three Vintner additions.
The winery remained exactly one raw entry with 24 expanded occurrences across
successful data-pack reload. The pool object identity remained stable and the
reload added zero duplicates. Injection and reload lifecycle are therefore not
the defect.

The one-vineyard guard is a field on each jigsaw `Placer`. It begins false for
each village and becomes true only after a vineyard piece is actually added.
There is no static, cross-village or stale-reload suppression.

## Showcase secondary control

The Showcase profile was inspected read-only. It contains one Vintner JAR with
SHA-256 `84da0383a9a5bbb4dcbfd4e81bd37ad6a3dcc199a20812d8454663697fd9f6d1`,
the older Blockbench-audited build, rather than any player-winery candidate.
There is no world datapack overriding village pools and no duplicate Vintner
JAR. The profile also contains WorldEdit, Serene Seasons and visual/client mods,
but no second village/worldgen mod was identified. Existing Showcase chunks
remain non-retroactive. This stale profile is a secondary environment issue;
it does not explain the independently reproduced collision defect in clean
exact-JAR fresh worlds.

## Controlled results

| Candidate | Exact vineyard hits | Rate |
| --- | ---: | ---: |
| Pre-height accepted candidate (`ae08e605...`) | 3/25 | 12% |
| One-block-lower, full-width candidate (`cc7bdeb...`) | 1/25 | 4% |
| One-block-lower, full-width first-fit diagnostic (`b9a063e...`) | 2/25 | 8% |
| Final lowered 16-wide Plains-only policy | 4/25 | 16% |

The final scan also found one separate adjacent village outside the fixed
target rectangles; it is excluded from the 25-village denominator. No village
structure start contained more than one winery.

## Root cause

The primary classification is `STRUCTURE_PLACEMENT_REJECTION`. Diagnostic
instrumentation observed the winery repeatedly reach rotation/placement
evaluation, then reject because its bounding box intersected meeting points,
streets or already placed pieces. Representative candidate bounds overlapped
`plains_meeting_point_2`, `streets/straight_04` and
`streets/straight_02`. Connector metadata matches working vanilla Plains house
semantics, and replacing the winery geometry with a vanilla large library
control placed normally. This rules out missing pool injection, reload loss,
invalid connector schema, global one-per-village state and silent processor
replacement.

## Production correction

The byte-exact player source remains 17 x 11 x 11 with SHA-256
`7914e27f3bce456181324a23c459c8159ce26c1b8d227d588f99b3b8986e8c94`.
Its x=0 edge contains only exported air/terrain (99 air, 11 dirt and 11 grass)
and no architecture or decoration. The deterministic runtime generator omits
that edge and shifts the remaining blocks one local x coordinate, producing a
16 x 11 x 11 collision box. The already accepted raised connector still lowers
the winery one world block. Every structural/decorative block and block entity
remains equivalent to the source after the coordinate shift.

Only the Plains winery is first-fit until it successfully places. Desert,
Savanna, Snowy and Taiga keep weight 24 and their vanilla shuffled ordering.
After a successful vineyard placement, the existing per-village maximum of one
remains enforced. The correction does not guarantee a winery where no
collision-free house connection exists, and it affects only newly generated
villages or unexplored chunks.

## Acceptance result

Structure-start scanning provides the natural-generation result. The structure
audit separately proves one Grape Press, zero Composters and valid connector
data. Standard and Serene Seasons GameTests pass 197/197; deterministic
generation, clean build, release-resource, advancement, model-family, Nursery
and serving/contact audits pass.

The exact candidate survived `/reload` with zero duplicate additions and
generated a further fresh Plains village afterward. In one of the four natural
winery hits, the Grape Press at `(16913, 74, 21)` registered the
`vintner:winemaker` POI. An unemployed Plains villager reached it, became
`vintner:winemaker`, retained job-site memory and preserved both after a full
server restart.

The final candidate is `vintner-1.4.0.jar`, 1,585,112 bytes, SHA-256
`9149278b342a60f702f1069f11bdf37132069daf3a7b91ca4484cf45fb475ba6`.
The exact player-built source remains unchanged, no other village culture was
rebalanced, and nothing was pushed, tagged, uploaded or published.
