# Vintner 1.4.0 development handoff

- **Branch:** `feat/1.4.0-vineyard-management`
- **Gate B base:** `a020bc4` (`Document Vintner 1.4.0 integration state`), the local Gate A housekeeping commit; it remains unpushed.
- **Gate C base:** `6abf06c` (`Add plot persistence and dimension GameTests`); Gate A and B remain local and unpushed. Gate C changes are uncommitted.
- **Stable public version:** Vintner 1.3.1 — Cellar Crafting Patch.
- **Development version:** `mod_version=1.3.1`; the 1.4.0 bump remains release-preparation work.

## Integration state

The 1.4.0 branch implements the agreed Vineyard Expansion scope: expanded cultivars and vineyard management, terroir and field information, supporting village professions/content, estate registration and plots, ledger and reputation, physical-facility reporting, Estate Management Desks and Atlas, and read-only economic guidance.

Deferred scope remains unchanged: pantry and cooking work (1.5.0), deeper estates and hospitality (1.6.0), transactional trade (1.7.0), and later labour/logistics automation.

- **Automated validation:** Integration Gate C passed: exact unloaded-plot test 1/1, focused plot tests 9/9, clean build and release audit, standard suite 165/165, and Serene Seasons suite 165/165. Gate B evidence and its non-reproduced timing incident remain recorded below.
- **Manual QA:** Pending for unloaded-plot desk presentation, desk UI/scrolling, multiplayer ownership isolation, and broader release visual gates.
- **Known risks:** Loaded plots are analyzed synchronously when the desk opens. The work is bounded (16 plots, 32×32 columns, 10 vertical positions), but no runtime timing test covers the worst case. Gate C now directly covers a same-dimension unloaded region and its outgoing desk payload; client rendering still needs manual QA.
- **Documentation discrepancy:** `docs/RELEASE_SCHEDULE.md` appears to place the 1.2.0 release date after 1.3.0; leave for a later documentation correction.

## Integration Gate A evidence

- Housekeeping and the initial handoff were committed locally as `a020bc4` (`Document Vintner 1.4.0 integration state`). The commit added the Python-bytecode ignore rules and did not change production code.
- Focused plot tests passed 6/6; standard and Serene Seasons suites each passed 162/162; clean build, release audit, and `git diff --check` passed.
- The Gate A audit counted 1,567 JSON files, 169 recipes, 159 public wood-family blocks, and 24 wood-preserving grapevine states.

## Integration Gate B evidence

### Plot coverage

- `identicalPlotCoordinatesAreAllowedAcrossDimensions` proves that identical bounds can coexist in the Overworld and Nether while preserving deterministic insertion order and same-dimension overlap rejection.
- `rejectedOverlapPreservesPlotLedgerAndReputation` proves that a rejected overlap retains the original plot object and every persisted plot field, does not insert the rejected name, and leaves ledger entries and reputation unchanged.
- The focused plot filter passed 8/8 in 619.0 ms (121.80 s invocation).
- No production source was changed.

### Original standard-suite failure and recovery

The first Gate B standard suite passed 163/164 in 1.641 s (124.48 s invocation). `unemployedVillagerClaimsGrapePress` failed at tick 302 because the naturally spawned unemployed villager had not acquired the registered Winemaker profession before the test's 300-tick limit. The paired Cooper workstation test passed. Gate B paused immediately; Serene Seasons was not run at that point.

The test uses ordinary villager POI discovery and job-site acquisition during work time. The grape press is registered under the `vintner:winemaker` POI and both profession predicates use that POI. The empty GameTest template contributes no authored villager or competing POI. The test waits for eventual profession acquisition rather than assuming a fixed delay, so AI scheduling within its timeout is the apparent nondeterministic dependency.

The exact isolated filter was:

`JAVA_TOOL_OPTIONS='-Dfabric-api.gametest.filter=vintner:vintner_game_tests_unemployed_villager_claims_grape_press' ./gradlew runGameTest`

- Isolated run 1: 1/1 passed; 487.7 ms GameTest completion, 0.060 s testcase duration, 143.89 s invocation. Report: `/tmp/vintner-winemaker-isolated-1.xml`.
- Isolated run 2: 1/1 passed; 475.4 ms GameTest completion, 0.062 s testcase duration, 122.97 s invocation. Report: `/tmp/vintner-winemaker-isolated-2.xml`.
- Isolated run 3: 1/1 passed; 495.7 ms GameTest completion, 0.060 s testcase duration, 126.87 s invocation. Report: `/tmp/vintner-winemaker-isolated-3.xml`.
- Standard recovery run 1: 164/164 passed in 1.533 s (126.38 s invocation). Report: `/tmp/vintner-standard-recovery-1.xml`.
- Standard recovery run 2: 164/164 passed in 1.613 s (127.41 s invocation). Report: `/tmp/vintner-standard-recovery-2.xml`.
- Serene Seasons: 164/164 passed in 1.393 s (126.78 s invocation), using `build/gametest-serene-run`. Report: `/tmp/vintner-serene-recovery.xml`.

The failure was not reproduced. Record it as: **A non-reproduced timing or suite-load failure observed once during Gate B.** No correction was made, it is not classified as definitively fixed, and no production defect was established.

### Final validation

- `./gradlew clean build` passed in 2.67 s. The build's `check` dependency ran the release asset audit successfully: 1,567 JSON files, 169 recipes, 159 public wood-family blocks, and 24 wood-preserving grapevine states.
- `git diff --check` passed.
- Ignored generated Python bytecode was removed; no `.pyc` or `.pyo` files remain under `scripts/`.
- Compilation continues to report existing deprecation warnings for the GameTest mock-player helper and other deprecated APIs.

## Integration Gate C evidence

- Initial repository verification matched the handoff exactly: branch `feat/1.4.0-vineyard-management`, HEAD `6abf06c`, clean working tree, origin comparison `0 2`, passing `git diff --check`, and no Python bytecode under `scripts/`.
- Only `src/main/java/com/zenith/vintner/test/VintnerGameTests.java` and this handoff changed. No production code, visibility seam, gameplay logic, release metadata, or version changed. No production defect was established.

### Report path and test design

- `VineyardPlotReport.analyzeIfLoaded` rejects a mismatched dimension and calls `hasLoadedAnalysisArea` before the block-scanning `analyze` path. That guard checks `ServerChunkCache.hasChunk` for the entire plot plus a 12-block X/Z margin and returns `Optional.empty()` if any required chunk is unavailable.
- `EstateDeskReport.open` retrieves the owner's plots, calls private `analyzePlots` (including saved-dimension resolution and `analyzeIfLoaded`), builds the private `vineyards` section and `plotSummaries`, and sends an `EstateDeskPayload` through `ServerPlayNetworking.send`.
- `unloadedPlotReportPreservesStaticDataWithoutLoadingChunks` uses Minecraft's existing public `ServerPlayer` and `ServerGamePacketListenerImpl` constructors, with a test-local `send` override capturing the real outgoing payload. The player is not inserted into the level/player list, and no network channel or target-region chunk tickets are created by the fixture.
- The plot is registered as metadata in the reporting level's dimension, from `(1000000, 64, -1000000)` to `(1000007, 64, -999995)`. Registration uses coordinate/time metadata, with no target-region block access, `getChunk`, teleportation, or entity spawning.
- The margin-inclusive analysis area covers chunk X `62499..62501` and chunk Z `-62501..-62499`: nine chunks. Every chunk is asserted absent via `hasChunk` before reporting, after `analyzeIfLoaded`, and after the real `EstateDeskReport.open` call. The direct analysis must also return no live report. Passing these 27 presence assertions proves the tested report paths did not synchronously load the target analysis region.

### Exact payload and unavailable-data assertions

- The captured desk payload must contain exactly one plot: name `Remote Rows`, dimension equal to the reporting level (`minecraft:overworld` in these runs), `minX=1000000`, `minZ=-1000000`, `maxX=1000007`, `maxZ=-999995`, `width=8`, `depth=6`, and `area=48`.
- `loaded=false` and `variety="Unavailable"` are required. The vineyard section must contain exactly `vineyards.plot_unloaded` with the name, dimension, width, and depth, followed by the dark-gray `vineyards.unloaded` component with no metric arguments. No live condition/health/yield/quality/irrigation line is allowed.
- Numeric payload slots still contain existing transport zero placeholders; zero is **not** asserted or interpreted as unavailable data. Availability is established by the explicit flag and unavailable presentation components. Source inspection confirms the client's `loaded=false` branch shows static size and `map.unloaded`, omitting live metrics. This GameTest captures server output; it does not execute or visually validate the client screen or exercise codec round-tripping.

### Validation and preserved reports

Validation followed the requested order, with each XML copied outside `build/` before a later run or clean could overwrite it:

| Check | Result | Preserved evidence |
| --- | --- | --- |
| Exact Gate C filter | 1/1 passed; 689.1 ms suite completion; Gradle 2m 22s | `/tmp/vintner-gate-c-20260904/focused.xml`, `focused.log` |
| `vintner:vintner_game_tests_*plot*` | 9/9 passed; 676.5 ms; Gradle 2m 20s | `/tmp/vintner-gate-c-20260904/plots.xml`, `plots.log` |
| `./gradlew clean build` | Passed; Gradle 2s; release audit executed through `check` | `/tmp/vintner-gate-c-20260904/build.log` |
| Complete standard suite | 165/165 passed; 1.581 s; Gradle 2m 4s | `/tmp/vintner-gate-c-20260904/standard.xml`, `standard.log` |
| Serene Seasons suite | 165/165 passed; 1.567 s; Gradle 2m 6s; separate `build/gametest-serene-run` directory | `/tmp/vintner-gate-c-20260904/serene.xml`, `serene.log` |

- The release audit passed with 1,567 JSON files, 169 recipes, 159 public wood-family blocks, and 24 wood-preserving grapevine states.
- All four preserved XML reports have zero failures and zero errors, and each includes the Gate C test. The Gate B Winemaker test also passed in the complete standard suite; the earlier incident remains **A non-reproduced timing or suite-load failure observed once during Gate B.** No fix is claimed.
- `git diff --check` passed before testing and after validation. Ignored audit-generated Python bytecode was removed; final inspection found no `.pyc` or `.pyo` under `scripts/`.
- No staging, commit, push, merge, tag, or publication was performed. HEAD remains `6abf06c`, with the two prior local commits preserved and origin comparison `0 2`.
- Proposed commit message, only if separately authorized: `Add unloaded plot reporting GameTest`.

## Remaining coverage gaps

- Two-player Estate Management Desk payload isolation.
- Loaded-plot scan performance characterization.
- Manual unloaded-plot desk presentation, UI/scrolling QA, and broader release visual/in-game acceptance.

## Next integration gate

**Smallest next integration task:** add one test-only two-player Estate Management Desk payload-isolation test, reusing the public packet-capture approach to assert that each player receives only their own estate and plot data. Do not expand it into scan-performance or manual UI work.
