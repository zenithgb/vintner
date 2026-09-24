# Vintner 1.4.0 Player-Built Tier-1 Winery Import

## Scope and provenance

This focused pass starts from the accepted village-vineyard release candidate
at `9300dbbbf307f828fb813b3bf4b02617d4c7568e`. It imports Zach's finished
Plains Tier-1 winery without redesigning its visible architecture.

The authoritative player export is:

`/Users/zachariaheverson/Library/Application Support/ModrinthApp/profiles/Showcase/saves/New World/generated/vintner/structure/village/plains/vineyard_tier1.nbt`

The inspected export is 7,954 bytes with SHA-256
`7914e27f3bce456181324a23c459c8159ce26c1b8d227d588f99b3b8986e8c94`.
It uses DataVersion 5023, measures 17 x 11 x 11, contains 67 palette states,
2,057 positioned blocks, ten block entities and zero entities. It contains no
Composter, Structure Block, Jigsaw, Barrier, command/debug block or invalid
identifier.

## Deterministic source of truth

The byte-exact export is tracked at
`structure_sources/village/plains/vineyard_tier1.nbt`. The release generator
refuses an unexpected source hash, decodes and re-encodes the complete NBT with
the repository's standard-library lossless NBT codec, and derives the runtime
Plains template deterministically. Running generation twice produces no Git
or byte churn.

Only two technical anchor edits are permitted. The boundary dirt-path block at
local `(9, 1, 0)` becomes air, and the source air block directly above at
`(9, 2, 0)` becomes the street connector
`minecraft:jigsaw[orientation=north_up]`, whose final state is
`minecraft:dirt_path` and whose target pool is the Plains village streets
pool. Raising the connector one local block makes Minecraft place the entire
authored winery exactly one world block lower while keeping the village road
at its original grade. Every other positioned block, block state and
block-entity payload is identical to the player export. The source
visible-equivalence signature is
`8de7a8808f57ea431a9ef367cfe2a2b094e49f44395d0a74631d1e3158da9975`.

`scripts/audit_village_vineyards.py` enforces that two-position anchor contract,
the complete 17 x 11 x 11 volume, bounds, state identifiers, block entities,
required functional inventory, forbidden blocks, entities, connector and all
four rotations. The original export is also frozen outside Git in the phase
evidence directory.

## Canonical Tier-1 contract

The imported Plains winery contains:

- one `vintner:grape_press[facing=east]` at local `(8, 1, 7)`;
- six oak Wine Racks at local x=12..14, y=1..2, z=2;
- one Fermentation Barrel at `(14, 2, 7)`;
- one oak Aging Barrel at `(14, 2, 8)`;
- two oak Barrel Stands beneath those barrels;
- one oak Tasting Service at `(14, 5, 3)`;
- 27 Vineyard Soil blocks and 52 established red Grapevine blocks;
- two oak Trellis blocks and the player-authored three-row vineyard layout;
- the original walls, roof, foundation, interior, entrance and decoration.

The Grape Press is the registered `vintner:winemaker` POI workstation. The
press and its approach remain unobstructed. No Composter or competing Farmer
POI is present.

## Village integration and biome policy

The runtime ID remains `vintner:village/plains/vineyard`. It is injected as a
rigid pool element into the vanilla Plains houses pool with weight 24, and the
existing one-vineyard-per-village limiter is unchanged. The authored boundary
path connector joins the vanilla Plains streets pool from one local block
higher, lowering the complete structure by one world block.

For Vintner 1.4.0, the player build becomes canonical for Plains only. Desert,
Savanna, Snowy and Taiga retain the already accepted interim 15 x 10 x 13
templates and their culture-appropriate Grape Presses. Automatically
recolouring the player build or inventing four unseen architectural variants
would violate the visible-equivalence and no-redesign requirements; unique
future variants are deliberately deferred.

## Rotation compatibility

The import exposed additional directional blocks that needed the normal
Minecraft structure rotation/mirror contract. Fermentation Barrel, Aging
Barrel, Barrel Stand, Tasting Service and Trellis now rotate and mirror their
authored states. Grapevines inherit the Trellis transform. Existing Grape
Press and Wine Rack handling remains in place.

Both the decoded-structure audit and the actual template GameTest verify 0,
90, 180 and 270 degree rotations. The connector, press, racks, barrels,
stands, tasting service, trellises and vine connections retain the expected
positions and facings.

## Validation and runtime evidence

Focused structure, generation, rotation, ordinary-terrain, POI and profession
checks pass. Standard and Serene Seasons GameTests each pass 197/197. The
advancement, serving/contact, release-asset, Blockbench/model-family, Nursery,
recipe/resource and deterministic-generation audits pass. Counts remain 2,025
JSON resources, 196 recipes, 215 blockstates, 907 model JSON files and 100 PNG
textures; this phase replaces one NBT and adds one tracked NBT source without
changing gameplay resource counts.

The exact Minecraft 26.3 candidate ran with Java 25, Fabric Loader 0.19.5,
Fabric API 0.161.0+26.3 and exact Commons 0.1.2. The dedicated server reached
readiness, injected all fifteen Vintner village entries and generated the
canonical Plains winery through the real `minecraft:village_plains` jigsaw
path. In the land village, the rotated Grape Press appeared at `(557, 63,
372)`, registered `vintner:winemaker`, and a naturally generated Plains
villager claimed it and became `vintner:winemaker`. After save, clean stop and
full restart, that villager, profession, POI and west-facing rotated press all
persisted. No accidental Composter/Farmer workplace was introduced.

The original exact client pass initialized Vintner and Commons, loaded the
resource atlases and audio, connected to the exact server, displayed the
player-authored build, and stopped cleanly after the server test. The final
placement-only candidate was then installed into the same exact profile and
again initialized Vintner, Commons, resource atlases and audio successfully.
That client is intentionally left open for Zach's one-block-lower visual check.

Evidence is frozen at:

`/Users/zachariaheverson/Developer/Minecraft/vintner-release-evidence/1.4.0-player-tier1-winery-20260923/`

The evidence includes the original source export, audits, both 197-test XML
reports, first-run/restart server logs, exact-client log, candidate JAR and
screenshots of the canonical exterior, vineyard/entrance, interior work area
and real generated-village context. Screenshots prepare review; they do not
self-approve subjective appearance. The existing screenshots and server
worldgen log predate the one-block-lower anchor adjustment; the new exact
client log is preserved separately and Zach's current visual check is the
authoritative height gate.

## Release candidate and deferred work

The structure-inclusive candidate is `vintner-1.4.0.jar`, 1,584,777 bytes,
SHA-256
`cc7bdeb02b78af5aee5ba6cd291efe830debde285fd460ff3369f5cc5478d52c`.
It supersedes the pre-shift player-winery candidate
`ae08e6050ee07a78029e6d38c36b7d6efb75d23f2b619e87b005eb1e54fb68a3`,
which remains preserved in the evidence directory.

Deferred work is limited to distinct player-designed Desert, Savanna, Snowy
and Taiga winery variants and Zach's subjective shader/no-shader visual
acceptance. No future architecture was synthesized in this phase.
