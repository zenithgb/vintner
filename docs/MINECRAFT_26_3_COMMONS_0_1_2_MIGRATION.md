# Vintner 1.4.0: Minecraft 26.3 and Commons 0.1.2 migration

Date: 2026-09-22
Phase: 10E3B
Status: Vintner-specific port and combined Tilth ecosystem acceptance passed locally; no publication authorized.

## Source baseline and isolation

The shared checkout at `/Users/zachariaheverson/Developer/Minecraft/vintner`
was inspected first. It remained on `phase/10d3b-commons-0.1.1` at
`f771aa21159911fb1092e9faf5bad1e420b0ff64` with substantial protected tracked
and untracked development work. Nothing in that checkout was reset, stashed,
cleaned, staged or edited.

The authoritative committed 1.4.0 source was the clean worktree at
`/private/tmp/vintner-four-qa-fixes.u82eQ4`, branch
`phase/1.4.0-four-manual-qa-fixes`, HEAD
`38e591ba528bd22366aada8959aeee13f98df1f5`. This contains the later four-QA
fixes, their acceptance record, the traditional green-bottle restoration and
placed-bottle Creative-pick correction. The isolated port worktree was created
from that commit on `phase/10e3b-minecraft-26.3-commons-0.1.2`.

The immediate pre-port artifact is
`vintner-1.4.0-mc26.2-pre-port.jar`: 1,535,094 bytes, 2,302 ZIP entries and
SHA-256 `89edf74388becf92fabbee1348ac596add8ecc2820b8a20196893726c4793d94`.

Recent Blockbench sources, generated models, textures, blockstates, QA records,
release notes and distribution preparation were treated as protected 1.4.0
work. A 1,088-path pre-port model/resource checksum manifest was captured before
the port. No Blockbench file, Vintner asset model, texture or grapevine
blockstate changed.

## Frozen Commons artifact

The accepted external dependency was verified before editing Vintner:

- source repository HEAD: `e605d335ca175818838ada24cff96c4f86ee117e`;
- artifact: `/Users/zachariaheverson/Developer/Minecraft/zenithgb-commons/build/libs/zenithgb-commons-0.1.2.jar`;
- size: 317,169 bytes;
- ZIP entries: 72;
- SHA-256: `f8285005dc74d62da902ec3da6ede05fdafc3d204913a5b36466a0e7b00dd7a5`;
- Fabric ID/version: `zenithgb_library` / `0.1.2`;
- Minecraft/Fabric API/Java: `~26.3`, `>=0.161.0+26.3`, `>=25`;
- protocol: 2;
- license: LGPL-3.0-or-later.

Commons was not rebuilt or modified. Vintner continues to consume it as a
separate exact-version dependency; it is not nested, shaded or embedded.

## Toolchain

| Component | Before | Accepted 26.3 candidate |
|---|---:|---:|
| Minecraft | 26.2 | 26.3 |
| Java | 25 | 25 |
| Fabric Loader | 0.19.3 | 0.19.5 |
| Fabric API | 0.158.0+26.2 | 0.161.0+26.3 |
| Loom | 1.17-SNAPSHOT | 1.17.20 |
| Gradle | 9.5.1 | 9.5.1 |
| Commons | exact 0.1.1 | exact 0.1.2 |
| Serene Seasons test artifact | 26.2-era artifact `13sXhUkI` | 26.1.2.0.7 (`V9PxJPuw`) |
| GlitchCore test artifact | 26.2-era artifact `SDUCBYRU` | 26.3.0.0.3 (`aaUghyGp`) |

Loader 0.19.3 was attempted first. The standalone suite ran, but the required
Serene Seasons suite crashed during MixinExtras transformation with an
`ArrayList` to `AnnotationNode` `ClassCastException` before Vintner tests ran.
Loader 0.19.5 is the smallest candidate tested that resolves that optional-mod
runtime incompatibility; both complete matrices then passed.

## Bounded compatibility changes

### Production common code

- Eighteen Vintner block classes dropped the obsolete 26.2 `MapCodec`,
  `simpleCodec` and `codec()` overrides after the 26.3 block superclass stopped
  requiring that contract. Registry identifiers and block/entity save formats
  are unchanged.
- `GrapevineBlock` accepts and forwards the new 26.3 `BonemealSource` argument.
  Growth rules and stored vine state are unchanged.
- `ModLootTables` uses `ContextIntProviders.exactly` and `between` in place of
  the removed number-provider factories. Loot quantities are unchanged.
- `CompostItem` and `VineyardSoilInteraction` unwrap the 26.3 holder-backed
  `SoundEvents.HOE_TILL` value. Gameplay behavior is unchanged.
- `WineItem` returns the 26.3 item-aware success result after placing a bottle.
  This prevents `usingConvertsTo(GLASS_BOTTLE)` from also awarding a phantom
  empty bottle while retaining the existing server-authoritative manual stack
  decrement and exact wine metadata placement.

### Production client code

`EstateManagementDeskRenderer`, `WineBasketRenderer` and
`WineStorageRenderer` replace the removed `mulPose(Axis.rotationDegrees(...))`
calls with the equivalent 26.3 `rotateDegrees(Axis, angle)` API. Transform
order, angles, scales, geometry and assets are unchanged.

### Resources and generation

- Six village processor-list resources serialize `output_state` using the 26.3
  block-state string form.
- The 182 recipe-helper advancements use the 26.3 `recipes` condition field.
- The three visible recipe-crafted advancements use the 26.3 `recipes` field.
- The three affected asset generators and the release audit emit/accept the new
  schema so regeneration remains deterministic.

### Tests only

- GameTests use `getStructureTemplateManager`, pass the new bonemeal source and
  assert the 26.3 interaction-result contract.
- The ownership snapshot fixture records both owners' infrastructure surveys
  before comparison, avoiding adjacent 26.3 GameTest-grid fixture interference.
- Bottle placement now runs in Survival and verifies the server-authoritative
  stack reaches zero without a phantom glass bottle.

No persisted Vintner identifier, component, codec, ownership key, plot/estate
schema, block-entity format or network payload changed. No migration is
required for wine, batch, vintage, provenance, vineyard, estate, map or cellar
data. Multiplayer recipient and owner-selection code is unchanged.

## Compile-first classification

- `TOOLCHAIN_ONLY`: `gradle.properties`, dependency coordinates and active
  Fabric metadata/documentation.
- `REQUIRED_PORT`: block codec-contract removal, bonemeal signature, loot
  providers, holder-backed sound access, item-aware bottle-placement result,
  three renderer rotation calls and 26.3 resource schemas.
- `TEST_ONLY`: structure-template accessor, bonemeal test call, interaction
  assertions and isolated ownership fixture setup.
- `OPTIONAL_COMPAT`: Serene Seasons/GlitchCore 26.3 artifacts and Loader 0.19.5.
- `NO_CHANGE`: Commons API/protocol/module contract, knowledge resources,
  persistence, networking, ownership, specialist Vintner state and all visual
  geometry/assets.

## Automated validation

- Common/main compile: PASS.
- Client compile: PASS.
- GameTest compile: PASS; 82 warnings are only the existing deprecation of
  `makeMockServerPlayerInLevel`.
- Datagen/resource compile and `runDatagen`: PASS with no generated-file churn.
- Focused exact Commons guard: real 0.1.2 accepted; 0.1.1, 0.1.0, 0.1.3, wrong
  ID, missing metadata and missing path all rejected.
- Standard GameTests: 194 passed, 0 failed, 0 errored, 0 skipped.
- Serene Seasons GameTests: 194 passed, 0 failed, 0 errored, 0 skipped.
- Release audit: 1,915 JSON resources, 182 recipes, 159 public wood-family
  blocks and 24 grapevine states.
- Advancement audit: 204 advancements, including 22 visible and 182 recipe
  unlock helpers.
- Nursery audit: 58 authored placed-model elements, 10-element empty item
  model, 16 state families and deterministic export.
- Serving pose/contact audit: PASS.
- Warning-enabled clean build and release audit: PASS.

## Exact-JAR runtime validation

The exact 26.3 candidate, exact Commons 0.1.2 and exact Fabric API
0.161.0+26.3 were installed in a clean dedicated-server fixture. The server:

- initialized Vintner and Commons;
- registered exactly one consumer module;
- loaded nine knowledge entries in four categories;
- reached readiness;
- reloaded with zero duplicate village additions;
- saved and stopped cleanly;
- restarted the saved world successfully;
- loaded no client-only Vintner or Commons class server-side.

An isolated Modrinth profile with only those three mod JARs used Minecraft 26.3
and Loader 0.19.5. The exact client connected to the dedicated server and
validated:

- synchronized revision, one `vintner:vintner` module, four categories and all
  nine entries;
- the real pause-menu Commons button;
- registered Commons keybinding and client-tick opening path;
- every article selectable, Vineyard Sites search, internal related navigation
  and Back restoration;
- server reload freshness (`revision=2` then `revision=3`);
- disconnect clearing (`revision=-1`, no modules/categories or level);
- reconnect synchronization;
- six-page Vintner Almanac pagination and close behavior;
- Estate Management Desk screen with Overview, Vineyards, Cellar, Markets,
  Ledger and Map tabs;
- clean client disconnect/stop.

Direct screenshots from the running exact client show the Estate Desk, Vintage
Archive, mature Nursery Bed, Wine Basket and full Grape Bowl without missing
textures, purple/black models, phantom/disconnected geometry or transform
failure. The Commons catalogue and Estate Desk screens render without clipping.
The complete broader human release checklist remains separate from this
port-specific regression smoke.

After Vintner's standalone gate passed, Tilth Phase 10E3A reached its accepted
committed baseline at `87e07946835bfca340239e91e05df79bb291ed60`. The exact
93,076-byte Tilth artifact (127 entries, SHA-256
`5546a5c0ae4a1ceb5ded5bdf27c733e023467abf7749e1ff0c8a72b9262519cc`)
was added to the same isolated server and client without metadata overrides.
The combined runtime registered exactly `vintner:vintner` and
`zenithgb_farming:tilth`, assembled eight categories and 15 entries, and made
all entries selectable. Vineyard and soil searches returned their respective
modules. Vineyard Sites linked once to Soil Health and Soil Health linked once
back to Vineyard Sites; both directions and Back restoration passed. Server
reload advanced the client presentation revision from 1 to 2, disconnect
cleared the synchronized catalogue, reconnect restored both modules, and both
processes saved/stopped cleanly.

## Persistence, gameplay and ownership

The 194-test suites cover the established cultivation, plots/overlap,
trellises, pressing, fermentation, ageing/treatment, cellar storage, wine
identity/scoring/consumption, vintages, provenance, professions/trades,
estate/reporting, Nursery, tutorial, save/reload and ownership isolation
contracts. The exact dedicated server also performed save, full restart and
reload. Because no serialized format or identifier changed, no 26.3 data
migration was introduced.

This phase does not claim a new real two-human-player session or a fresh copy of
a public 1.3.1 world upgraded through 26.3. Those remain broader final-release
evidence, not demonstrated regressions caused by this port.

## Package comparison and inspection

The current 26.3 candidate contains the same 2,302 paths as the immediate 26.2
baseline: zero added and zero removed. There are 233 changed entries:

- 40 class files, consisting of the required production adaptations plus
  target-generation remapping/recompilation effects;
- 192 JSON files: 182 recipe-unlock advancements, three visible advancements,
  six processor lists and `fabric.mod.json`;
- one manifest;
- zero changed PNGs, Blockbench files or Vintner asset models.

Final candidate: 1,529,803 bytes and
SHA-256 `7e0406b138852cb4dd29b72a02c33fe15f0e7cb574cbb95dc41771e81a43034f`.

JAR inspection confirms Vintner 1.4.0, Minecraft `~26.3`, Loader `>=0.19.5`,
Fabric API `>=0.161.0+26.3` and exact Commons `0.1.2`. The JAR contains 869
Vintner model JSON files, 100 PNG textures, 199 blockstates, four knowledge
categories and nine entries. It contains no nested Commons, Tilth or Reeve JAR,
no Tilth/Reeve required dependency, no GameTest implementation/entrypoint and
no QA logs, worlds or temporary probe files.

## Consumer preservation and remaining gate

Commons remained at the frozen accepted HEAD and checksum. The separate Tilth
26.3/Commons 0.1.2 migration completed independently during this phase. Its
accepted committed artifact was used read-only for the combined no-override
runtime; its checkout was not modified. Reeve is not an immediate Vintner
dependency and was also left unchanged.

`COMBINED_TILTH_VINTNER_26_3_ACCEPTANCE_COMPLETE`

The Vintner-specific port and the combined consumer ecosystem gate are ready
locally. Final Vintner 1.4.0 release-candidate acceptance still requires the
separately tracked final-release manual gates. Commons 0.1.2 publication
preparation and final Vintner release-candidate acceptance are separate future
steps. No push, tag, upload or publication occurred in this phase.
