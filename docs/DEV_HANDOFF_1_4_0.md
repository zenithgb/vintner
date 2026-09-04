# Vintner 1.4.0 development handoff

- **Branch:** `feat/1.4.0-vineyard-management`
- **HEAD:** `576b684eb7127d0cc94df21b9f97552d8ee4e1a8` (`Fix estate plot reporting and validation`)
- **Remote state:** Local HEAD matches `origin/feat/1.4.0-vineyard-management` (`0` ahead, `0` behind).
- **Stable public version:** Vintner 1.3.1 — Cellar Crafting Patch.
- **Development version:** `mod_version=1.3.1`; the 1.4.0 bump remains release-preparation work.

## Integration state

The 1.4.0 branch implements the agreed Vineyard Expansion scope: expanded cultivars and vineyard management, terroir and field information, supporting village professions/content, estate registration and plots, ledger and reputation, physical-facility reporting, Estate Management Desks and Atlas, and read-only economic guidance.

Deferred scope remains unchanged: pantry and cooking work (1.5.0), deeper estates and hospitality (1.6.0), transactional trade (1.7.0), and later labour/logistics automation.

- **Automated validation:** Integration Gate A passed at this HEAD. Focused plot tests passed 6/6; standard and Serene Seasons suites each passed 162/162; clean build and release audit passed.
- **Manual QA:** Pending for plot overlap/dimension behavior, unloaded-plot desk presentation, desk UI/scrolling, multiplayer ownership isolation, and broader release visual gates.
- **Known risks:** Loaded plots are analyzed synchronously when the desk opens. The work is bounded (16 plots, 32×32 columns, 10 vertical positions), and unloaded regions are guarded by non-loading chunk-presence checks, but no runtime timing test covers the worst case. Several integration assertions listed below also lack direct GameTest coverage.
- **Documentation discrepancy:** `docs/RELEASE_SCHEDULE.md` appears to place the 1.2.0 release date after 1.3.0; leave for a later documentation correction.

## Integration Gate A evidence

- `JAVA_TOOL_OPTIONS='-Dfabric-api.gametest.filter=vintner:vintner_game_tests_*plot*' ./gradlew runGameTest` — 6/6 passed in 646.2 ms (124.42 s invocation). Report: `build/test-results/gametest/TEST-vintner-focused.xml`.
- `./gradlew clean build auditReleaseAssets` — passed in 3.22 s. Audit: 1,567 JSON files, 169 recipes, 159 public wood-family blocks, and 24 wood-preserving grapevine states.
- `./gradlew runGameTest` — 162/162 passed in 1.404 s (128.10 s invocation), using `build/gametest-standard-run`. Report: `build/test-results/gametest/TEST-vintner-standard.xml`.
- `./gradlew runGameTest -PsereneSeasonsTest` — 162/162 passed in 1.617 s (145.85 s invocation), using `build/gametest-serene-run`. Report: `build/test-results/gametest/TEST-vintner-serene.xml`.
- `git diff --check` — passed.
- Required-test failures: none. Clean GameTest directories log missing initial `server.properties`/`eula.txt`; the Fabric headless server then starts and completes normally. Compilation reports deprecated mock-player API usage.

### Focused coverage

- `overlappingNamedPlotsAreRejected` covers rejection of a differently named overlap in the same dimension and confirms plot count remains one.
- `plotReportsResolveAndGuardSavedDimensions` covers correct server-level resolution and refuses analysis against the wrong dimension.
- The focused wildcard also runs `unnamedAlmanacRegistersDefaultPlotInTwoSteps`, `namedPlotsTrackBoundariesAndLiveVines`, `namedPlotRecognizesPhysicalIrrigation`, and `almanacPlotCornersPersistOnTheItem`.

Direct coverage is still missing for coordinate reuse across dimensions; complete preservation of the original plot after rejection; unchanged ledger/reputation after rejection; same-dimension unloaded analysis without chunk loading; and static unloaded `PlotSummary` identity/dimension/bounds with unavailable live metrics. Desk multiplayer payload isolation is code-enforced but has no direct two-player payload test.

### Code-backed findings

`EstateDeskReport` selects estate, ledger, reputation, and plot data by the requesting player's UUID and sends the resulting payload only to that player. Each plot resolves through its saved dimension. `VineyardPlotReport.analyzeIfLoaded` checks every required chunk with `hasChunk` before analysis, so an unloaded plot does not call the block-scanning path. Unloaded summaries retain name, dimension, bounds, and area; `loaded=false` makes both desk sections and map details show unavailable rather than presenting placeholder zeroes as live values.

- **Next smallest task:** With approval, add a focused GameTest-only coverage slice for rejected-overlap preservation and cross-dimension coordinate reuse, without changing gameplay behavior.
