# Commons Integration C — First Vintner Knowledge Contribution

Phase 6C adds static reference content. It does not complete Commons Phase 6,
replace the Almanac, move Archive records or Estate Desk state into Commons,
or implement gameplay interoperability. This document follows the integration
record naming requested for this phase; the preceding audit remains
`COMMONS_ALMANAC_AUDIT.md`.

## Baselines and ownership

- Vintner: `feat/1.4.0-vineyard-management`, starting commit
  `d53d16421d3b524b0dd9b6241e0207a3e8e486e6`, clean; development version `1.3.1`.
- Commons: `main`, `be3ae97b4db6a05c2718daec21186bbbb0e213f6`, clean, no remotes.
- Fresh Commons artifact: `zenithgb-commons/build/libs/zenithgb-commons-0.1.0-dev.jar`;
  SHA-256 `10a6e01ddb6ed2d9b57c93da40ed3db6c1fa991fa6e088256adc2ec267008ef4`.
- Existing explicit `-PcommonsJar=/absolute/path/to/zenithgb-commons-0.1.0-dev.jar`
  wiring and required `zenithgb_library:0.1.0-dev` dependency remain unchanged.
- Exactly one existing module: `vintner:vintner`, `Vintner`, runtime version `1.3.1`.
- Commons owns discovery, validation, assembly, synchronization, keybinding and UI.
  Vintner contributes authored JSON only. No production Java, metadata or build
  configuration changes are part of this phase.

## Authored structure

All IDs below have the `vintner:` prefix; every owner is `vintner:vintner`.
Resources live under `src/main/resources/data/vintner/zenithgb_knowledge/`.

| Category ID | Display name | Order | Ordered entry IDs |
| --- | --- | ---: | --- |
| getting_started | Getting Started | 10 | introduction |
| viticulture | Viticulture | 20 | grapevines, vineyard_sites |
| winemaking | Winemaking | 30 | pressing_and_must, fermentation |
| cellaring_quality | Cellaring & Quality | 40 | ageing_and_treatments, cellars, wine_scoring, vintages_and_archives |

| Entry ID / title | Related entry IDs in authored order |
| --- | --- |
| introduction / Vintner | grapevines, pressing_and_must |
| grapevines / Grapevines | vineyard_sites, pressing_and_must |
| vineyard_sites / Vineyard Sites | grapevines |
| pressing_and_must / Pressing and Must | fermentation, grapevines |
| fermentation / Fermentation | pressing_and_must, ageing_and_treatments |
| ageing_and_treatments / Ageing and Treatments | fermentation, cellars, wine_scoring |
| cellars / Cellars | ageing_and_treatments, wine_scoring |
| wine_scoring / Wine Scoring | ageing_and_treatments, vintages_and_archives |
| vintages_and_archives / Vintages and Archives | wine_scoring, cellars |

There are no conditions, self-links, duplicate links, unresolved targets or
Tilth/Reeve IDs. These resources need only Commons and Vintner. Resource paths
provide IDs; no custom schema, generated prose or new transport is introduced.

## Content source-of-truth review

Current implementation was inspected alongside README, English translations,
recipes and the existing GameTests. The Phase 6B audit guided inspection but
was not treated as a replacement for executable behavior.

| Article | Principal current implementation and verification sources |
| --- | --- |
| Vintner | `VintnerAlmanacItem`, `AlmanacInspection`, estate/Archive use paths; README progression |
| Grapevines | `GrapeCuttingItem`, `GrapevineBlock`, `GrapeCultivar`, compost use; cutting/harvest/wood-preservation tests |
| Vineyard Sites | `TerroirEvaluator`, `TerroirReport`, `SoilProbeItem`, `VineyardSurveyRecord`, Almanac land/plot registration; site/plot tests |
| Pressing and Must | `GrapePressBlock`, `GrapePressBlockEntity`, `WineMetadata`; press capacity/metadata/mixed-batch tests |
| Fermentation | `FermentationBarrelBlock`, `FermentationBarrelBlockEntity`; completion, compatible press-lot combination and provenance rejection tests |
| Ageing and Treatments | `AgingBarrelBlock`, `AgingBarrelBlockEntity`, `AgingVessel`, `CoopersMalletItem`, `AlmanacInspection`; cooperage wood-preservation and mid-batch rejection tests |
| Cellars | `CellarConditions`, rack/crate/collection block entities and storage history; humidity/storage tests |
| Wine Scoring | `WineQualityProfile`, vessel contribution/penalty calculation, `WineMarketOutlook`, bottle appraisal; scoring/ageing tests |
| Vintages and Archives | `WineMetadata`, vintage calendar, `VintageArchiveBlock`, `VintageArchiveBlockEntity`; unique-batch catalogue and persistent bottle-number tests |

The prose omits internal formulas, fixed calendar lengths, full cultivar catalogue,
live countdowns, actual estate/batch values and guaranteed merchant prices.
Compatible press lots can combine during fermentation; it does not claim that
every input must share an identical batch ID.

### Phase 6B accuracy findings

1. Additional ageing-vessel guidance requires sneak-use of the Almanac. The new
   article says so; the older README guidance is not silently repaired here.
2. The season heading's formatting-argument issue remains deferred. Static prose
   describes inspection without reproducing that generated heading.
3. Ready vessel reports can select fill-to-start helper text. Articles describe
   actual full/start/ready/collection behavior rather than copying that helper.
4. Vessel quality penalties are deterministic; the article does not invent random
   spoilage of otherwise identical wine.
5. Eight active cultivars are distinct from compatibility-only enum identities.
   This introductory contribution does not enumerate or overstate their count.
6. Decorative wood family is separate from toasted/seasoned/cask treatment.
7. Market guidance is an estimate, not a guaranteed merchant offer.
8. The short Almanac guide remains; these longer reference articles complement it.
9. Vintage/calendar identity is distinct from batch/bottle identity and elapsed
   ageing/storage history. `Ageing` follows existing README/guide prose; the
   existing item label `Aging Barrel` is preserved when naming that item.

No unrelated production bug was fixed.

## Consumer contract test

`commonsKnowledgeContributionMatchesApprovedStructure` runs through Commons'
actual resource preparation and presentation assembly with the actual registered
Vintner module. A test-only same-package bridge accesses Commons' package-private
loader using isolated catalogue instances. It neither resets nor publishes to
Commons' global catalogues. The bridge is excluded from the production JAR.
This is deliberately small test coupling to the pinned development Commons API;
it does not add a production visibility seam or duplicate the parser/assembler.

Assertions cover exact decoded IDs, owners, names, category/member order,
single membership, all related links (including the three-link ageing entry),
absence of module conditions and exact assembled 1-module/4-category/9-entry
structure. Existing gameplay assertions are unchanged.

## Validation record

Evidence directory: `/tmp/vintner-phase6c-TD67oA` (local disposable QA evidence,
not a runtime dependency). Separate logs/XML/world copies preserve each run.

- Fresh Commons clean build and regression suite: PASS, 117 tests, zero failures.
- Focused contribution GameTest: PASS, 1/1.
- Full standard GameTests: PASS, 170/170, zero failures.
- Full Serene Seasons GameTests: PASS, 170/170, zero failures, separate run directory.
- Vintner `clean build`: PASS, including common/client/GameTest compilation and
  release asset audit through `check`.
- Explicit `auditReleaseAssets`: PASS, 1,580 JSON files, 169 recipes, 159 public
  wood-family blocks and 24 wood-preserving grapevine states. The increase from
  1,567 is exactly four categories plus nine entries; other counts are unchanged.
- Final JAR inspection: PASS, exact 4/9 source-byte-identical resources, required
  Commons metadata/helper retained, no Commons classes/nested JAR, no GameTest
  implementation/entrypoint/test bridge, Almanac/Archive/Desk classes retained.
  Commons' separate JAR contains no Vintner classes/resources.
- Dedicated server and connected-client lifecycle/navigation: PASS, detailed below.

### Dedicated server

Commons + Vintner, without Tilth/Reeve, ran in a new disposable localhost world.
Both initialized and the server reached `Done`. External JDI inspection on the
server thread observed one module (`vintner:vintner`, `Vintner`, `1.3.1`) and the
real transport publication at revision 1 with the exact ordered 4/9 contribution
and related lists. After vanilla `reload`, revision 2 retained exactly the same
structure and one registration. `list` reported the connected `VintnerDev`.
Neither Commons `KnowledgeScreen` nor `VintnerClient` was loaded on the dedicated
server. Normal `stop` saved the disposable world and exited 0.

Evidence: `server-before.log`, `server-after-reload.log`, `server-launcher.log`,
`server-result.json` and `server/logs/latest.log` beneath the evidence directory.

### Connected client and specialist coexistence

The actual client joined localhost with `ClientLevel` and `LocalPlayer` present
and no screen open. Received revision 1 contained one Vintner module, four
categories and nine entries in the exact category/member/related order.

Disposable external JDI probes invoked existing controls/input callbacks on the
client render thread. They did not construct Commons screens, packets, snapshots
or presentation state, and did not write Commons fields. This is runtime
screen/state/callback evidence, not direct visual or physical-input acceptance.

- Normal Pause → Options → Controls → Key Binds callbacks opened the vanilla
  binding screen. Its existing Commons change-button callback and `keyPressed`
  assigned K; the saved temporary options recorded
  `key_key.zenithgb_library.open:key.keyboard.k`.
- Exactly one existing Commons `KeyMapping` was found. Its normal click dispatch
  was consumed by Commons' existing end-client-tick handler. The resulting real
  `KnowledgeScreen` used the received snapshot by object identity.
- All four categories and all nine articles were opened. Narration and selected
  article state confirmed each target. Eight normal Scroll down callbacks made
  all three ageing related buttons visible/active. Fermentation, Cellars and
  Wine Scoring links each opened the correct article, and Back restored the
  previous ageing detail and scroll position. Category Back and Escape worked.
- Reload received revision 2 with unchanged exact 4/9 counts. The already-open
  screen retained its older snapshot; Escape and the same Commons binding
  reopened with the current revision-2 snapshot by object identity. No duplicates.
- A vanilla server inventory command supplied the existing Almanac to the QA
  player. Normal client item-use callback opened the existing server-authored
  six-page `BookViewScreen`. Next/Previous moved between pages 1 and 2; Escape
  returned to gameplay. Registered `vintner:almanac_report` and
  `vintner:estate_desk` receivers remained present. No specialist state moved.
- Normal Pause → Disconnect cleared level/player and Commons state: revision
  −1, zero modules/categories/entries. Back reached the title screen and normal
  Quit Game exited the client successfully (Gradle exit 0).

Evidence: `client-initial.log`, `navigation-summary.json`, `ui-01` through
`ui-69` logs, `client-receivers.log`, and `client-launcher.log`. Snapshot identity
comparisons were made within each debugger attachment; numeric debugger object
IDs across separate attachments are not treated as stable identity evidence.

### Commands and nonzero attempts

All Vintner Gradle commands supplied the explicit fresh `commonsJar` path above:

```sh
# Commons repository
./gradlew clean build

# Vintner repository
JAVA_TOOL_OPTIONS='-Dfabric-api.gametest.filter=vintner_gametest:vintner_game_tests_commons_knowledge_contribution_matches_approved_structure' ./gradlew runGameTest -PcommonsJar="$COMMONS_JAR"
./gradlew runGameTest -PcommonsJar="$COMMONS_JAR"
./gradlew runGameTest -PsereneSeasonsTest -PcommonsJar="$COMMONS_JAR"
./gradlew clean build -PcommonsJar="$COMMONS_JAR"
./gradlew auditReleaseAssets -PcommonsJar="$COMMONS_JAR"
./gradlew runReleaseServer -PcommonsJar="$COMMONS_JAR" -I <disposable-runtime-init.gradle>
./gradlew runClient -PcommonsJar="$COMMONS_JAR" -I <disposable-runtime-init.gradle>
git diff --check
git diff --cached --check
```

`COMMONS_JAR` above denotes the absolute fresh artifact path, not a repository
configuration change. Runtime init/probe programs were external temporary QA and
removed before staging. XML parsing recorded zero skipped tests in every suite.
ZIP inspection compared all thirteen packaged resources byte-for-byte against
source and inspected metadata, excluded classes and retained specialist classes.

One disposable widget-listing probe initially failed with `NoSuchElementException`:
an empty search label matched a non-button control and attempted to find its
press method. It was corrected to avoid dispatch during listing, then rerun.
This was a QA-tool issue; no mod source or synchronized state was changed. No
build, GameTest or Commons regression invocation failed.

### Warnings and limits

- Existing GameTest mock-player API compilation produced 59 deprecation/removal
  warnings. No assertions were weakened.
- Fresh GameTest directories logged missing server properties/EULA before normal
  test setup. Loom referenced the empty client-resource output directory.
- Serene Seasons/GlitchCore development refmaps and untranslated item-tag warnings
  remain; the full compatibility suite passed.
- The deliberately localhost-only offline server logged its authentication warning.
  The development client logged the expected unauthenticated profile-key HTTP 401
  and native shader attribute warnings. Connected play and clean exit succeeded.
- No direct visual polish, authenticated public-server play, cross-mod links or
  gameplay interoperability is claimed. Runtime navigation used the user-approved
  callback evidence path. The prior missing-dependency test was not repeated.
- The test-only package bridge depends on the pinned Commons loader API. Commons
  source, production gameplay Java, metadata and build wiring remain unchanged.
- Authored content was reread against source. Datagen does not author these files;
  repeated build/audit caused no generated-resource churn. Ignored audit bytecode
  was removed before staging.

Phase 6C's required checks passed. Recommend accepting Phase 6C only; final
visual quality and the deferred scopes below remain separate decisions.

## Deferred scope

Phase 6D cross-mod links, Almanac transition/retirement, Archive contextual links,
later articles, search/indexing, icons, player discovery, shared gameplay APIs,
Reeve and final visual polish require separate scope. No next phase starts here.
