# Vintner 1.4.0 development handoff

- **Branch:** `feat/1.4.0-vineyard-management`
- **Gate B base:** `a020bc4` (`Document Vintner 1.4.0 integration state`), the local Gate A housekeeping commit; it remains unpushed.
- **Stable public version:** Vintner 1.3.1 — Cellar Crafting Patch.
- **Development version:** `mod_version=1.3.1`; the 1.4.0 bump remains release-preparation work.

## Integration state

The 1.4.0 branch implements the agreed Vineyard Expansion scope: expanded cultivars and vineyard management, terroir and field information, supporting village professions/content, estate registration and plots, ledger and reputation, physical-facility reporting, Estate Management Desks and Atlas, and read-only economic guidance.

Deferred scope remains unchanged: pantry and cooking work (1.5.0), deeper estates and hospitality (1.6.0), transactional trade (1.7.0), and later labour/logistics automation.

- **Automated validation:** Integration Gate B passed. Its focused plot tests passed 8/8; the Winemaker recovery passed three isolated runs, two consecutive 164/164 standard suites, and a 164/164 Serene Seasons suite; the final clean build and release audit passed.
- **Manual QA:** Pending for unloaded-plot desk presentation, desk UI/scrolling, multiplayer ownership isolation, and broader release visual gates.
- **Known risks:** Loaded plots are analyzed synchronously when the desk opens. The work is bounded (16 plots, 32×32 columns, 10 vertical positions), but no runtime timing test covers the worst case. Unloaded regions are guarded by non-loading chunk-presence checks, but the behavior still lacks direct GameTest coverage.
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

## Remaining coverage gaps

- Same-dimension unloaded plot reporting without loading chunks.
- Static unloaded payload-field validation, including identity, dimension, bounds, area, `loaded=false`, and unavailable live metrics.
- Two-player Estate Management Desk payload isolation.
- Loaded-plot scan performance characterization.

## Next integration gate

**Integration Gate C — Unloaded Plot Reporting:** add a focused test-only slice proving same-dimension unloaded plot reporting does not load chunks and returns correct static fields with live metrics unavailable. Do not change gameplay logic unless the test establishes a defect and a correction is separately approved.
