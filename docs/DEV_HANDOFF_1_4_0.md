# Vintner 1.4.0 development handoff

- **Branch:** `feat/1.4.0-vineyard-management`
- **Gate B base:** `a020bc4` (`Document Vintner 1.4.0 integration state`), the local Gate A housekeeping commit; it remains unpushed.
- **Gate C base:** `6abf06c` (`Add plot persistence and dimension GameTests`). Gate C was committed separately as `4513969` (`Add unloaded plot reporting GameTest`) before Gate D. These local commits remain unpushed.
- **Gate D state:** accepted after Gate D-R5 fixture hardening and the complete validation sequence below. This change consolidates the ownership test, hardened Winemaker fixture, concise failure diagnostics and investigation handoff as `Harden estate and Winemaker integration coverage`; nothing is pushed.
- **Investigation outcome:** the recovered Gate D failure was reproduced in R4 and classified by roadmap as a **GameTest setup/geometry defect**. R5 corrects the unsupported fixture and validates natural approach from outside assignment range. No Vintner production defect was established; Gate B's exact historical cause remains unproven.
- **Stable public version:** Vintner 1.3.1 — Cellar Crafting Patch.
- **Development version:** `mod_version=1.3.1`; the 1.4.0 bump remains release-preparation work.

## Integration state

The 1.4.0 branch implements the agreed Vineyard Expansion scope: expanded cultivars and vineyard management, terroir and field information, supporting village professions/content, estate registration and plots, ledger and reputation, physical-facility reporting, Estate Management Desks and Atlas, and read-only economic guidance.

Deferred scope remains unchanged: pantry and cooking work (1.5.0), deeper estates and hospitality (1.6.0), transactional trade (1.7.0), and later labour/logistics automation.

- **Automated validation:** Gates C and D passed. R5: Winemaker standard 10/10 and compatibility 10/10; ownership 1/1; estate 11/11; village/workstation 7/7; complete standard and Serene Seasons suites each 166/166; clean build, release audit and formatting checks passed. Earlier failed-run evidence remains below.
- **Manual QA:** Pending for unloaded-plot desk presentation, desk UI/scrolling, multiplayer ownership isolation, and broader release visual gates.
- **Latest automated validation:** Gate H passed plot 10/10, estate 11/11, ownership 1/1, standard 167/167 and Serene Seasons 167/167, followed by clean build and release audit. Prior gate evidence remains below.
- **Gate I focused validation:** facility persistence 1/1, estate 12/12 and ownership 1/1 passed. Recognition, disk persistence and removal/rebuilding match the documented persistent-reputation model; production code is unchanged.
- **Known risks:** Loaded plots are analyzed synchronously when the desk opens. Gate H reduced the measured maximum-estate range to 5.848–13.666 ms per report (median 11.717 ms) on the recorded development machine. The remaining cost needs final release-risk assessment; this is not a whole-tick measurement or comprehensive worst-case guarantee. Gate C covers a same-dimension unloaded region and its outgoing payload; client rendering still needs manual QA.
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

## Integration Gate D evidence

- Starting inspection matched the Gate C handoff: branch `feat/1.4.0-vineyard-management`, HEAD `6abf06c`, origin comparison `0 2`, and only the approved Gate C test/documentation diff. Nothing was staged; there were no unrelated or production modifications and no Python bytecode. `git diff --check` passed.
- The approved Gate C diff was inspected and committed as `45139691135592e3a507099d639260dabfa3c6d4` (`Add unloaded plot reporting GameTest`). The working tree was confirmed clean afterward, and the origin comparison became `0 3`. Neither earlier commit was amended or squashed.
- Gate D adds exactly one GameTest, `estateDeskPayloadsRemainIsolatedBetweenOwners`, with test-local capture/assertion helpers in `VintnerGameTests.java`. The only other Gate D change is this handoff. No production code, visibility seam, version, villager/POI/workstation behavior, or timeout changed.

### Scenario and assertions

- Two simulated players have distinct UUIDs and names (`amber-owner`, `cobalt-owner`), registered as `Amber Estate` and `Cobalt Estate`. Both use the same desk position and open it in A → B → A order.
- A owns `Amber Rows 1`; B owns `Cobalt Rows 1` and `Cobalt Rows 2`. All plots are 8×6 (48 blocks each); A and B deliberately share the first plot's coordinates, so location cannot substitute for ownership. Remote plots are registered as metadata and their margin-inclusive chunks are checked absent using the Gate C helper.
- Public ledger APIs record each plot registration, distinct harvest details/totals (11 versus 29), and distinct bottling details, batch IDs (101 versus 202), and quality (61 versus 89). Ledger counts are three versus four. Registration and ledger operations establish the fixture before reporting begins.
- The Gate C public `ServerPlayer`/`ServerGamePacketListenerImpl` approach captures actual `EstateDeskPayload` packets sent by `EstateDeskReport.open`. Simulated players are not inserted into the level/player list, and there is no production accessor or reflection seam.
- Exact assertions cover the recipient capture counts after every opening, estate title and subtitle, complete ordered `PlotSummary` lists, section titles, plot counts/areas, reputation tier/score, harvest and best-quality values, bottle label, complete vineyard lines, and complete ledger lines. These structures must equal the requesting owner's saved state; extra foreign plot/ledger records or values fail equality.
- Both owners' entire immutable estate profiles, plot lists, ledger event lists, and reputation profiles are compared with their pre-opening snapshots after every open. All comparisons passed in every run, including the failing compatibility suite. A's second payload must equal its first payload after B has opened the same desk, guarding against stale owner state. Owner UUID/name associations are asserted in the registered profiles; the payload itself does not carry an owner UUID field.

### Validation and stop evidence

Each XML was preserved before another run could replace it. Logs and XML are under `/tmp/vintner-gate-d-5tgHuB/`.

| Check | Result | Preserved evidence |
| --- | --- | --- |
| Exact `vintner:vintner_game_tests_estate_desk_payloads_remain_isolated_between_owners` filter | 1/1 passed; 525.3 ms suite completion; Gradle 2m 21s | `exact.xml`, `exact.log` |
| `vintner:vintner_game_tests_*estate*` filter | 11/11 passed; 700.3 ms; Gradle 2m 5s | `estate.xml`, `estate.log` |
| Complete standard suite | 166/166 passed; 1.543 s; Gradle 2m 7s | `standard.xml`, `standard.log` |
| Complete Serene Seasons suite | 165/166 passed; one failure, zero errors; 1.752 s; Gradle failed after 2m 28s; separate `build/gametest-serene-run` | `serene.xml`, `serene.log` |
| Gate D clean build / release audit | Not run: stopped after the compatibility failure | No Gate D build/audit evidence |

The new ownership test passed in all four runs (testcase durations: 0.077 s exact, 0.011 s standard, 0.016 s Serene Seasons). It did not establish an ownership or persistence defect.

**Recurring failure:** `vintner:vintner_game_tests_unemployed_villager_claims_grape_press`. The preserved XML records: `(-7870031, -59, -4895336) The villager beside a grape press should become a Winemaker on tick 302`.

- Expected: the adjacent villager's profession matches `ModVillagers.WINEMAKER` within the existing 300-tick test limit.
- Actual: that predicate was still false at tick 302. The XML does not record which profession the villager held. The same test passed in this gate's standard run.
- The earlier historical classification was **A non-reproduced timing or suite-load failure observed once during Gate B.** It has now recurred during Gate D's Serene Seasons suite; it must no longer be described as only a non-reproduced incident. This is a confirmed recurrence, not a confirmed production root cause or a definitively fixed issue.
- The assertion depends on ordinary villager POI/job acquisition. Existing evidence does not establish whether the cause is scheduling, suite interactions, or another defect, and does not establish Serene Seasons as the cause. No test weakening, timeout extension, production correction, or additional reproduction run was attempted after the stop.
- The last successful release audit remains Gate C's baseline: 1,567 JSON files, 169 recipes, 159 public wood-family blocks, and 24 wood-preserving grapevine states. These counts were not revalidated for Gate D after the stop.
- Final formatting/status housekeeping found a passing `git diff --check`, no staged files, no unrelated modifications, and no Python bytecode under `scripts/`. Only the Gate D test file and this handoff are modified. Gate D remains uncommitted; nothing was pushed; `mod_version` remains `1.3.1`.

### Limits of the ownership evidence

- The tests cover server-authored outgoing payload objects and the two owners' stored records in this scenario, not client rendering, codec/network transport, save/reload, or arbitrary concurrency/multiple-desk arrangements.
- No facility or market differences were added. Desk opening still invokes existing `syncFromLedger` and `recordInfrastructure`; no changes were observed in this fixture, but general read-only behavior when newly recognized facilities are discovered is not proven.
- The complete Gate D acceptance gate is blocked by the recurring Winemaker failure and the unrun clean build/audit, despite the ownership test itself passing.

## Gate D-R — Winemaker profession acquisition investigation

### Starting state and scope

- Verified branch `feat/1.4.0-vineyard-management`, HEAD `45139691135592e3a507099d639260dabfa3c6d4`, and origin comparison `0 3`. Only the expected Gate D test file and this handoff were modified; nothing was staged, no unrelated changes or Python bytecode existed, and `mod_version=1.3.1`.
- The pre-investigation diff and both modified files were copied into `/tmp/vintner-gate-dr-UmqxuI/` as `gate-d-baseline.patch`, `VintnerGameTests.before.java`, and `DEV_HANDOFF.before.md` before edits. Existing Gate D code is preserved byte-for-byte.
- No production change, new GameTest, timeout increase, AI intervention, forced POI acquisition, profession assignment, configuration change, commit, push, or version bump was made. The original `@GameTest(maxTicks = 300)` and Winemaker success predicate remain intact.

### Inspected registration and runtime chain

1. `Vintner.onInitialize` initializes `ModBlocks` before `ModVillagers`. `ModBlocks.GRAPE_PRESS` is the oak `vintner:grape_press`; `grapePressBlocks()` supplies every registered wood variant.
2. `ModVillagers.WINEMAKER_POI` calls Fabric `PoiHelper.register` with identifier `vintner:winemaker`, one ticket, valid range two, and all grape-press variants. The installed Fabric helper expands each block's possible states. Live `PoiTypes.forState` and `PoiManager.getType` both reported `vintner:winemaker` for the placed state in every run.
3. `data/minecraft/tags/point_of_interest_type/acquirable_job_site.json` adds the Winemaker and Cooper POIs without replacing the tag. Minecraft's unemployed profession (`NONE`) uses `PoiTypeTags.ACQUIRABLE_JOB_SITE` as its acquisition predicate.
4. `ModVillagers.registerProfession` uses the same `holder.is(WINEMAKER_POI_KEY)` predicate for both `acquirableJobSite` and `heldJobSite`. Runtime checks of both predicates and the acquirable tag were true throughout the observations.
5. Minecraft 26.2 `VillagerGoalPackages` runs `AcquirePoi` with `JOB_SITE` absent and acquires `POTENTIAL_JOB_SITE`. `AcquirePoi` searches available POIs within 48 blocks, considers up to five, computes a reachable path, takes a ticket, and records the potential position. It has randomized initial/repeated scan intervals and increasing randomized pathfinding retries; the source alone does not show which retry caused either historical failure.
6. `GoToPotentialJobSite` drives the existing movement behavior. `AssignProfessionFromJobSite` requires proximity within two blocks of the POI center (unless the spawn-assignment flag applies), promotes potential memory to `JOB_SITE`, finds a registered profession whose held-site predicate accepts that POI, then assigns it and refreshes the brain.
7. The existing test places the press at `FIRST`, sets the default world clock to 2000, spawns a villager one block west, and waits only for the profession predicate. The spawn helper does not explicitly set the villager's profession/age or constrain wandering; the test previously recorded no discovery, memory, registration, or timing evidence. It did not prove that the profession came from this specific press.

Source inspection used the repository files, the local Minecraft 26.2 source archive, and installed Fabric bytecode (`fabric-object-builder-api-v1 24.1.0+6fcd5f039e`, GameTest API `4.0.21+4a7fa0819e`). Vintner's `SeasonalContext`/`SereneSeasonsIntegration` read the external calendar. The installed Serene Seasons `SeasonHandler` advances its own season clock from world-clock deltas; inspected level mixins hook weather/precipitation, with no direct villager-profession hook identified. Compatibility configuration retained `progress_season_while_offline=true` and `day_duration=24000`. This inspection does not rule out an indirect interaction.

### Diagnostic method and evidence limits

- Test-only `winemakerAcquisitionState` reads test/game/day ticks, actual profession, UUID, position/distance, alive/removed/baby/no-AI flags, `JOB_SITE`, `POTENTIAL_JOB_SITE`, expected press state, state-to-POI mapping, live POI identity/free tickets, acquisition/held predicates, and current activity.
- The unchanged per-tick `succeedWhen` predicate logs identity/memory transitions, 50-tick samples, success, and ticks 299 onward. Its failure message now includes the complete concise diagnostic snapshot, so the next timeout can retain actual state in XML.
- POI reads use public single-position `getType`/`getDebugPoiInfo` at the already placed press. These methods can access/load POI-section storage; they do not create a world chunk, place a POI, take a ticket, or force pathfinding. No broad nearby-POI scan was added.
- Standard run 1 accidentally expanded the debug POI's full block-state list in its log. The original log/XML were preserved; subsequent runs emit only its free-ticket count. This diagnostic formatting change did not change setup or acceptance. Logging/storage inspection can still have observer effects, so these are instrumented observations rather than proof that the historical problem is gone.
- Acquisition tick means the first observed true predicate. The test ends on success; later lifetime behavior is not monitored. Potential-job transitions that happen entirely inside one AI tick can occur between samples.

### Isolated reproduction matrix

All XML and matching `.log` files below are under `/tmp/vintner-gate-dr-UmqxuI/`. Each report was copied before a later run could overwrite `build/test-results/gametest/TEST-vintner.xml`. `results.json` contains the machine-readable summary; `summarize.py` regenerates it from preserved evidence.

Exact standard command:

`JAVA_TOOL_OPTIONS='-Dfabric-api.gametest.filter=vintner:vintner_game_tests_unemployed_villager_claims_grape_press' ./gradlew runGameTest`

Serene Seasons uses the same filter with `-PsereneSeasonsTest` and its separate `build/gametest-serene-run` directory.

| Standard run | Result | Acquisition/final tick | JOB_SITE = expected press (x, y, z) | Evidence stem |
| --- | --- | --- | --- | --- |
| 1 | 1/1 passed | 19 | (5068701, -56, -8547469) | `standard-1` |
| 2 | 1/1 passed | 17 | (-14229436, -56, -2837466) | `standard-2` |
| 3 | 1/1 passed | 11 | (-8546555, -56, 20075) | `standard-3` |
| 4 | 1/1 passed | 68 | (6580183, -56, -9257639) | `standard-4` |
| 5 | 1/1 passed | 8 | (-3302934, -56, -14324426) | `standard-5` |

| Serene Seasons run | Result | Acquisition/final tick | JOB_SITE = expected press (x, y, z) | Evidence stem |
| --- | --- | --- | --- | --- |
| 1 | 1/1 passed | 8 | (-8901532, -56, 5787572) | `serene-1` |
| 2 | 1/1 passed | 19 | (12196350, -56, 5948145) | `serene-2` |
| 3 | 1/1 passed | 7 | (7500480, -56, -7296171) | `serene-3` |
| 4 | 1/1 passed | 8 | (-5637082, -56, -13634789) | `serene-4` |
| 5 | 1/1 passed | 15 | (-13641693, -56, -14428844) | `serene-5` |

For **every row**, initial profession was `minecraft:none`; final profession was `vintner:winemaker`; final `JOB_SITE` was present in `minecraft:overworld` at the listed press; `jobMatches=true`; the expected block remained `vintner:grape_press[facing=north,input_level=0,input_type=0,output_type=0]`; both state mapping and live POI were `vintner:winemaker`. The POI had one free ticket initially and zero finally. The acquirable tag and both Winemaker predicates were true. Villagers remained alive, adult, not removed, and AI-enabled. Final potential-job memory was empty and activity was `work`. Final distance to the press center was approximately 1.803 blocks.

- Standard acquisition min/median/max: **8 / 17 / 68 ticks**; smallest observed margin to the 300-tick limit: **232 ticks**.
- Serene Seasons min/median/max: **7 / 8 / 19 ticks**; smallest observed margin: **281 ticks**.
- Standard run 4 remained unemployed with both job memories empty at tick 50, despite an intact, correctly registered, unclaimed POI. It acquired the expected job by tick 68. This identifies an observed pre-acquisition delay, not a registration failure, and does not establish why the historical runs exceeded the deadline.
- These five-sample distributions do not show a Serene Seasons slowdown or low timing margin. The lower compatibility median is not evidence that the integration improves acquisition. No run was extended past the original deadline, and no isolated failure was reproduced.

### Small suite-context probes

Only after all isolated runs passed, the native `vintner:vintner_game_tests_*villag*` filter was run once per environment. It includes seven existing tests: workstation-variant recognition, village structure registration, vineyard structure content, specialist trades, village-chest grape supplies, Cooper acquisition, and Winemaker acquisition.

| Environment | Result | Winemaker acquisition tick | Evidence |
| --- | --- | --- | --- |
| Standard | 7/7 passed | 16 | `group-standard.xml`, `group-standard.log` |
| Serene Seasons | 7/7 passed | 11 | `group-serene.xml`, `group-serene.log` |

Both Winemaker snapshots retained the expected profession/POI/job-site relationship and all validity checks described above. No predecessor-specific contamination or ordering dependence was demonstrated. The group uses the normal runner and registration paths, but reducing the set changes layout/batching. Its XML ordering differs from the historical full-suite report, and XML result ordering is not proof of exact execution order. This was not an exact replay of the original failing batch or its prior world history.

### Interpretation and handoff

- **Best-supported classification: unresolved.** Current incident label remains **Recurrent Winemaker profession-acquisition integration failure; root cause not established.** The ten isolated successes and two group successes do not solve or dismiss the two earlier failures.
- Missing/incorrect registration, wrong profession, wrong held job site, and invalid initial villager state were not observed. Acquisition was never near the deadline in this matrix, so the evidence does not satisfy a diagnosis of test timing sensitivity. Randomized scan/retry behavior and uncontrolled movement are source-level dependencies, not established test-setup defects.
- No reproducible production defect, Serene Seasons-dependent failure, or minimal suite-order/state leak was demonstrated. No production fix or timeout change is justified by this evidence alone.
- Twelve invocations ran: ten isolated test cases plus two groups of seven, **24/24 testcase executions passed**. The Winemaker test passed in all twelve invocations. No full 166-test suite, clean build, or release audit was run, and Gate D was not resumed.
- Only `VintnerGameTests.java` (diagnostic imports, the existing Winemaker callback, and its diagnostic helper) and this handoff were changed for D-R. Existing Gate D code from the Cooper test onward was checked against the saved baseline and remained byte-for-byte identical. Final `git diff --check` passed; nothing is staged; no Python bytecode remains. HEAD is still `4513969`, origin comparison `0 3`, and version `1.3.1`. All Gate D and D-R edits remain uncommitted and unpushed.
- **Recommended next experiment:** retain the diagnostics and run the original failing compatibility batch's membership, recording actual test-start order, placement, and batch/chunk transitions. Preserve that world if the failure returns, then reduce the failing context to a predecessor pair or minimal group. This needs roadmap review; do not automatically resume Gate D or modify production behavior.

## Gate D-R2 — Historical compatibility-batch reproduction

### Verified starting state and preservation

- Branch `feat/1.4.0-vineyard-management`, HEAD `45139691135592e3a507099d639260dabfa3c6d4`, origin comparison `0 3`, and `mod_version=1.3.1` matched the handoff. Only this handoff and `src/main/java/com/zenith/vintner/test/VintnerGameTests.java` were modified; nothing was staged, no unrelated changes or Python bytecode existed, and `git diff --check` passed.
- Evidence is under `/tmp/vintner-gate-dr2-cN2FGx/`. `starting.patch`, `VintnerGameTests.java.before`, and `DEV_HANDOFF_1_4_0.md.before` preserve the initial work. `before-run/` preserves the entire compatibility run directory before replaying anything.
- **No additional test-code edit was needed in D-R2.** The entire test file remains byte-for-byte identical to the D-R2 starting copy. The Gate D ownership test and D-R instrumentation remain intact. The earlier pre-D-R baseline was also compared from the Cooper test onward, including the ownership test and its helpers, with no differences.
- Each replay used exactly `./gradlew runGameTest -PsereneSeasonsTest`, with no filter, verify/retry mode, timeout change, or configuration edit. Each completed run was copied before the next launch: `replay-N.xml`, full launcher output `replay-N.log`, and the complete `replay-N-run/` directory including runtime logs, configuration, and saved world.

### Historical batch and harness reconstruction

- Gate B failed in the **standard** 164-test suite (163/164); Gate D's latest recurrence was the **Serene Seasons** 166-test suite (165/166). Gate D's original XML/log remain `/tmp/vintner-gate-d-5tgHuB/serene.xml` and `serene.log`. The initial Gate B failure's exact world/layout was not recovered; the documented recovery followed by clean builds does not preserve that original world.
- Inspected the repository's `build.gradle` launcher and exact local Minecraft 26.2 sources: `GameTestServer`, `GameTestBatchFactory`, `GameTestRunner`, `GameTestTicker`, `GameTestInfo`, `StructureGridSpawner`, `StructureUtils`, `TestInstanceBlockEntity`, and `JUnitLikeTestReporter`. Installed Fabric GameTest API `4.0.21+4a7fa0819e` launcher, annotation discovery, and registration bytecode were also inspected. Extracted source and bytecode evidence is retained in the temporary directory.
- Fabric discovers annotated methods via `Class.getDeclaredMethods()` without an explicit sort. The server consumes registry element order, groups by environment, and partitions into at most 50 tests. Source-file order is not the execution-order contract. These runs used `minecraft:default` batches of **50, 50, 50, 16**.
- All tests in a batch are added to one ticker in one `ServerLevel` and progress concurrently, with callbacks processed on the server thread. Batches advance after all their tests finish. A test can wait for its structure chunks to be loaded/ticking before beginning, so allocation order is not a complete timestamped start-order trace. XML appends testcases on success/failure and therefore records **completion order**.
- `GameTestServer` uses `StructureGridSpawner(startPos, 8, false)` without enabling `clearBetweenBatches`. The grid persists between batches. Its increments are padded test-bound width plus five blocks and row depth plus six. Saved 8×8×8 fixtures here have **15-block column pitch and 17-block row pitch** (one-block padding). Rotation is `none` in these saved test-instance records.
- Before placement, the target test bounds are cleared of blocks, scheduled block work, and non-player entities; active structures are enclosed in barriers. Success discards non-player entities within structure bounds inflated by one and removes barriers, but does not erase fixture blocks/POIs. Failed test entities and barriers can remain. End-of-batch handling releases forced chunk tickets; it does not erase the world.
- The launcher reuses `build/gametest-serene-run/world` on disk. Each server invocation constructs fresh test level settings; existing region/POI/entity files remain. World generation uses seed **0**, flat Overworld, and disabled generated structures, confirmed in the saved `world_gen_settings.dat`. The test-grid origin is independently drawn from the level RNG in X/Z within ±14,999,992 at Y=-59. That seed alone does not reconstruct runtime AI randomness, origin, or exact callback timing.

### Recovered original Gate D world

The pre-replay world still contained all **166 original Gate D fixtures**, including the failed test block at `(-7870031, -59, -4895336)` with the original tick-302 failure message. The log's grid origin was `(-7870091, -59, -4895608)`. Sorting saved grid positions by row/column reconstructs allocation order: Winemaker occupied **zero-based index 132, batch 2, slot 32**, after `almanacReportPaginatesLongEntries` and before `differentWoodTrellisesConnect`. This is an allocation predecessor, not proof of which callback ran immediately before acquisition attempts.

`historical-gate-d-layout.json` records all fixtures; `historical-gate-d-state.json` records the saved villager, nearby fixtures and POIs, and batch membership. This is a **shutdown/persisted snapshot recovered after intervening D-R launches**, not a newly captured live tick-302 trace:

- Villager UUID `f7dc15d2-a3c8-4c6b-8c0f-ce27f9e5dc51`; saved profession `minecraft:none`, health 20, age 0, death time 0. Saved position `(-7870027.799775155, -57.0, -4895330.195989812)`.
- `JOB_SITE` absent; `POTENTIAL_JOB_SITE` present in `minecraft:overworld` at **the expected press** `(-7870028, -56, -4895332)`.
- That block remains `vintner:grape_press`, facing north, with input level/type and output type zero. Live-world saved POI identity is `vintner:winemaker`, with zero free tickets (the `PoiRecord` codec defaults an omitted `free_tickets` field to zero). Air is directly beneath the press, with the retained barrier floor one block lower.
- A second saved Winemaker POI at `(-7870073, -56, -4895332)` is 45 blocks west, with one free ticket, in `cultivarIdentityStacksAndReachesWineProvenance`'s fixture. The saved potential memory points to the test's own press, not this alternative. No other saved villager was found in the compatibility world before the replays.
- Distance from the saved villager position to its selected POI centre is **2.0100516696492123 blocks**. Vanilla `AssignProfessionFromJobSite` requires `closerToCenterThan(position, 2.0)` unless the spawn-assignment shortcut applies. The recovered snapshot therefore narrows historical evidence to **after POI selection/reservation and before job/profession assignment**. It does not establish how long this state persisted or why the villager did not approach more closely.

### Full Serene Seasons replay matrix

| Replay | Complete result | Acquisition tick | Margin to 300 | Winemaker allocation | Suite time / Gradle invocation |
| --- | --- | --- | --- | --- | --- |
| 1 | 166/166 passed | 8 | 292 | index 160; batch 3, slot 10 | 1.542 s / 2m 8s |
| 2 | 166/166 passed | 11 | 289 | index 132; batch 2, slot 32 | 1.726 s / 3m 22s |
| 3 | 166/166 passed | 65 | 235 | index 132; batch 2, slot 32 | 1.865 s / 2m 13s |

- **3/3 full replays passed, 498/498 testcase executions; no failure recurred.** Acquisition min/median/max was **8 / 11 / 65 ticks**. Replay 3 still had no job or potential-job memory at tick 50, with an intact correctly mapped POI and one free ticket; it acquired its own press at tick 65. None approached the deadline.
- Every replay began with profession `minecraft:none`, no job memories, adult/alive/AI-enabled villager, and the correct intact press/POI. At success, profession was `vintner:winemaker`, `JOB_SITE` matched the expected press exactly, `POTENTIAL_JOB_SITE` was empty, free tickets changed from one to zero, and acquisition/held-site predicates were true. Full diagnostic lines, including UUID, positions, game/day ticks and activity, are in the logs and `replay-N-summary.json`/`results.json`.
- Grid origins were respectively `(-3727183, -59, -5083846)`, `(1195747, -59, 3196697)`, and `(10099583, -59, -13626676)`. `replay-N-layout.json` records every saved allocation. Relative test identity placement differed from historical Gate D in 118 slots for replay 1 and 130 for replay 2. Replay 2 nevertheless shared the Winemaker index and immediate predecessor.
- **Replay 3 reproduced all 166 test identities in the historical relative allocation order/layout**, including the same batch membership and Winemaker neighborhood, and passed at tick 65. Its absolute origin and runtime random/timing state differed. Historical exact start timestamps and per-test overlap durations were not logged and cannot be reconstructed from XML or the saved fixtures.
- No new failing context existed to reduce, so no predecessor-pair or reduced-group experiments were run. No fourth full replay, standard suite, clean build, or release audit was run. Gradle's successful `runGameTest` output is not a clean-build/audit result.

### Spatial/concurrency inspection and interpretation

- Vanilla `AcquirePoi.SCAN_RANGE` is **48**; acquisition queries up to five nearest available matching POIs before pathfinding. The 15/17-block grid does not exclude neighboring structures from that query radius. Saved non-test POIs within 48 blocks of the Winemaker press were found in every replay (own press plus two alternatives in replay 1; own press plus one in replays 2 and 3).
- Thus nearby tests can expose alternative POI candidates and completed fixtures can retain POIs. **Candidate proximity is not proof of reachable competition or interference:** active barriers constrain movement, pathfinding must succeed, and chunk availability also matters. Historical and current evidence does not demonstrate a neighboring POI being selected instead of the expected press, a foreign villager reserving it, or an unexpected cross-test mutation. The historical saved memory instead selects the correct press.
- Existing source uses `GoToPotentialJobSite` to request movement toward the selected site, while `AssignProfessionFromJobSite` applies the two-block centre-distance guard. The recovered 2.010-block state makes approach geometry/navigation a specific hypothesis for the next investigation. It is not enough to classify a test-setup defect, timing sensitivity, production defect, or vanilla defect; navigation/WALK_TARGET history at the failure is missing.
- Cumulative evidence: Gate B had one standard failure followed by three isolated successes, two full standard 164/164 recoveries and one compatibility 164/164 recovery; Gate D had a standard 166/166 pass and compatibility 165/166 recurrence; D-R had 5/5 isolated successes per environment and 7/7 groups per environment (Winemaker passed all 12 invocations); D-R2 adds three compatibility 166/166 passes, including one full historical relative-layout match. These passes do not erase the two observed failures.
- **Current classification: Unresolved recurrent suite-context failure.** No reproducible production defect, cross-test state leak, neighboring-test interference, or Serene Seasons-specific interaction has been demonstrated. No production correction or timeout change is indicated by this evidence. Gate D remains stopped and unaccepted.
- **Smallest justified next experiment, for roadmap review:** a focused test-only investigation of the recovered post-selection approach geometry, retaining the original press height, natural POI acquisition and 300-tick acceptance window, with narrowly scoped read-only `WALK_TARGET`, navigation/path-completion and centre-distance diagnostics. Determine whether the villager can remain just outside assignment distance after reserving its own press; do not change the fixture or AI as a proposed fix before reproducing the condition.

### Final state

- D-R2 changes only this handoff; the test file's existing uncommitted Gate D/D-R changes are untouched. No production files, version, configuration, timeout or predicates changed. Nothing was staged, committed or pushed.
- Final `git diff --check` passed. Only the expected test file and this handoff remain modified, HEAD remains `4513969`, origin comparison remains `0 3`, version remains `1.3.1`, and no Python bytecode remains under `scripts/`.

## Gate D-R3 — Winemaker approach and navigation diagnostics

### Starting state and diagnostic design

- Verified branch `feat/1.4.0-vineyard-management`, HEAD `45139691135592e3a507099d639260dabfa3c6d4`, origin comparison `0 3`, version `1.3.1`, and only the expected test/handoff modifications. Nothing was staged; there were no unrelated changes or Python bytecode; initial `git diff --check` passed. Baseline comparisons confirmed Gate D ownership logic remained unchanged and D-R2 had changed only the handoff, leaving the D-R test instrumentation intact.
- Evidence directory: `/tmp/vintner-gate-dr3-m8woggkp/`. Starting files and diff are preserved as `VintnerGameTests.java.before`, `DEV_HANDOFF_1_4_0.md.before`, and `starting.patch`. Each run has its own `.xml`, full launcher `.log`, diagnostic `.json`, and complete `-run/` snapshot including runtime logs and world. `results.json` summarizes the matrix; the temporary runner stops on a failed invocation or test.
- Added only a per-invocation `WinemakerApproachDiagnostics` observer to the existing Winemaker callback. Original press placement, clock/setup, villager creation, `@GameTest(maxTicks = 300)`, and profession success predicate are unchanged. Existing D-R profession/POI/job-memory diagnostics remain active. No new GameTest, production seam, path creation, movement, memory assignment, AI change, or retry was added.
- Read-only observations cover `WALK_TARGET` position/block, speed and completion radius; navigation target, active/done/stuck flags; existing path target, whether it matches the press, reachability, node progress/count, endpoint and completion; `CANT_REACH_WALK_TARGET_SINCE`; and the exact vanilla two-block centre-distance predicate. Existing diagnostics retain actual test/game/day ticks, profession, position/distance, job memories, press integrity, POI identity/tickets and registration predicates.
- The observer samples each GameTest callback, keeping a minimum distance before assignment and a separate minimum while the correct press is reserved but `JOB_SITE` is absent. It logs state transitions, every 50 ticks, every 10 ticks while a potential job site is present, at tick 299 onward, and on success. No broad brain dump is emitted.
- **Observation limits:** an AI tick can select a potential job site and assign the profession before the callback samples it. `unobserved` therefore does not mean the potential memory never existed. `navDone=true` also covers a null path, so it is not evidence of successful navigation completion. `completedPathSeen` requires a non-null completed path; `canReach` describes only an existing sampled path, not a diagnostic pathfinding probe. `AcquirePoi` can populate the navigation target while merely computing a candidate path without installing an active movement path.

### Standard isolated matrix

Command: `JAVA_TOOL_OPTIONS='-Dfabric-api.gametest.filter=vintner:vintner_game_tests_unemployed_villager_claims_grape_press' ./gradlew runGameTest`.

| Run | Result | Acquisition tick | Closest before assignment | Walk target observed | Reserved/unassigned state observed | Active/completed path observed |
| --- | --- | --- | --- | --- | --- | --- |
| `standard-1` | 1/1 passed | 8 | 1.118033988749895 | No | No | No / No |
| `standard-2` | 1/1 passed | 13 | 1.118033988749895 | Yes | No | No / No |
| `standard-3` | 1/1 passed | 74 | 1.118033988749895 | Yes | No | No / No |
| `standard-4` | 1/1 passed | 9 | 1.118033988749895 | Yes | No | No / No |
| `standard-5` | 1/1 passed | 8 | 1.118033988749895 | Yes | No | No / No |

- **5/5 passed**, acquisition min/median/max **8 / 9 / 74**, smallest deadline margin **226 ticks**. Every success had `JOB_SITE` exactly at the press, `vintner:winemaker`, intact correctly mapped POI, zero free tickets and a distance of **1.8027756377319946**.
- The minimum of 1.118 blocks includes the initial airborne spawn position. The villager settles one block lower on the fixture floor, explaining the 1.803-block success distance; the initial minimum is not evidence about approach after reservation.
- Observed early idle walk targets used speed 0.5/radius 0 and pointed away from the press, sometimes changing or disappearing with a cannot-reach timestamp. No active path was sampled. Run 3 remained at distance 1.803 with no job memories at tick 50, then acquired the correct job by tick 74. These successful traces do not establish that idle targets caused the historical failure.

### Serene Seasons isolated matrix

The exact isolated command adds `-PsereneSeasonsTest` and uses the separate compatibility run directory.

| Run | Result | Acquisition tick | Closest before assignment | Closest reserved/unassigned | Walk target observed | Active/completed path observed |
| --- | --- | --- | --- | --- | --- | --- |
| `serene-1` | 1/1 passed | 16 | 1.118033988749895 | Unobserved | Yes | No / No |
| `serene-2` | 1/1 passed | 8 | 1.118033988749895 | Unobserved | No | No / No |
| `serene-3` | 1/1 passed | 18 | 1.118033988749895 | Unobserved | Yes | No / No |
| `serene-4` | 1/1 passed | 82 | 1.118033988749895 | 1.9982328284128672 | Yes | Yes / No |
| `serene-5` | 1/1 passed | 18 | 1.118033988749895 | Unobserved | Yes | No / No |

- **5/5 passed**, acquisition min/median/max **8 / 18 / 82**, smallest deadline margin **218 ticks**. All acquired the expected Winemaker profession and exact press job site. The five-sample comparison does not demonstrate a Serene Seasons-specific navigation effect or deadline sensitivity.
- `serene-4` captured the previously unobserved reserved-but-unassigned stage. Its full trace is in `serene-4.log` and `serene-4.json`; expected press `(-14797410, -56, 12468103)`.

| Tick | Distance to press centre | Observed approach state |
| --- | --- | --- |
| 50 | 1.8027756377319946 | No job memories or walk target |
| 56–57 | 1.8027756377319946 | Idle walk target away from press; speed 0.5/radius 0; a three-node path becomes active |
| 67 | 2.019723318687771 | Outside assignment range, still following the idle target; no potential job site yet |
| 73 | 2.286590289040975 | Walk target and active path cleared; still no job memories |
| 74 | 2.3068266712604255 | Correct `POTENTIAL_JOB_SITE` appears; press ticket reserved; `JOB_SITE` absent; walk target exactly at press centre, speed 0.5/radius 1 |
| 75 | 2.3181071213372304 | Active two-node approach path; navigation target one block above the press; endpoint `(-14797410, -57, 12468104)`; path `canReach=false` |
| 80 | 2.02192677190554 | Correct potential site and walk target remain; path is null and navigation reports done; still outside assignment range |
| 81 | 1.9982328284128672 | Inside assignment range, still reserved/unassigned; one-node path active to the same endpoint |
| 82 | 1.9854587697094672 | `JOB_SITE` equals press, profession becomes Winemaker, potential/walk memory and active path absent |

- The approach moved naturally across the threshold and succeeded; no repositioning, forced memory or AI intervention occurred. POI identity, press integrity and reservation remained correct. No stuck flag or non-null completed path was observed. A null path after active navigation does not identify whether completion or another stop condition cleared it.
- The sampled approach path's `canReach=false` applies to its navigation target **above** the press, not a failed POI registration or selection. The walk target and potential memory continued to name the actual press. Despite the unsuccessful path-reach flag, the villager moved within assignment distance and acquired the profession.
- This run provides a live **successful** comparison to Gate D's persisted 2.010-block state. It does not prove that the historical villager followed the same trajectory or that geometry, timing, or navigation caused either historical timeout.

### Broad compatibility matrix

Only after all ten isolated runs passed, the full unfiltered command `./gradlew runGameTest -PsereneSeasonsTest` ran three times. No fourth replay was attempted.

| Run | Result | Acquisition tick | Closest before assignment | Closest reserved/unassigned | Walk target observed | Active/completed path observed |
| --- | --- | --- | --- | --- | --- | --- |
| `broad-1` | 166/166 passed | 82 | 1.118033988749895 | 1.970216089536259 | Yes | Yes / No |
| `broad-2` | 166/166 passed | 66 | 1.118033988749895 | Unobserved | Yes | No / No |
| `broad-3` | 166/166 passed | 18 | 1.118033988749895 | Unobserved | Yes | No / No |

- **3/3 suites passed, 498/498 testcase executions**; acquisition min/median/max **18 / 66 / 82 ticks**. All thirteen D-R3 invocations passed (**508/508 testcase executions** including isolation), with at least **218 ticks** of margin in every Winemaker run. No new failure/world-at-timeout evidence was produced.
- `broad-1` captured a second successful approach. Idle navigation began at tick 45, crossed outside assignment range by tick 68, and the correct press `POTENTIAL_JOB_SITE` and walk target appeared at tick 71, distance **2.2786208011206934**. The existing idle path was still sampled at that boundary; by tick 72 the path target changed to one block above the press and its endpoint to an adjacent floor cell. The potential memory and walk target continued to point to the actual press.
- At tick 80 the distance was **2.033635564076035**, with navigation active; at tick 81 it was **1.970216089536259**, inside range and still reserved/unassigned. At tick 82 the correct `JOB_SITE`/Winemaker profession appeared, distance **1.9150458352974806**, and potential/walk memory and active path were absent. The sampled approach path reported `canReach=false`; no stuck flag or non-null completed path was sampled. `broad-1-approach.json` preserves a compact timeline alongside the full log.
- `broad-2` and `broad-3` followed the common sampled transition from no job memories to the exact expected `JOB_SITE`, without a potential-site interval or active path observed. Success distance was **1.8027756377319946** in both.

### Findings, limits and next action

- Every run started with profession `minecraft:none` and finished `vintner:winemaker`, with the intact expected press, correct POI mapping/identity, correct job-site position and expected ticket consumption. No wrong workstation, missing POI, registration predicate failure, dead/baby/AI-disabled villager, or observed loss of a previously assigned job was found. The observer ends on success; later job retention is not tested.
- `WALK_TARGET` was sampled in **11/13** runs. Early targets were idle destinations with speed 0.5/radius 0; they must not be mistaken for selection of another job-site POI. In the **two observed reserved/unassigned sequences**, walk targets named the correct press at speed 0.5/radius 1, navigation became active, distance crossed into the strict vanilla two-block range, and job assignment followed at the next sampled tick.
- No non-null completed path or stuck flag was sampled in any run. Most successes required no observed active navigation. In the two approach sequences, path clearing/termination was observed, but normal completion versus another stop condition cannot be reconstructed inside one AI tick. A negative reachability flag on a sampled partial path did not prevent either successful approach; it is not by itself a production defect.
- **No failure reproduced. Current classification remains: Unresolved recurrent suite-context profession-acquisition failure, now narrowed to the approach/navigation/assignment stage.** Two successful sequences show that idle movement can take the villager outside range before it reserves its own press and naturally returns. They do not establish why the historical Gate D villager remained unassigned at shutdown, or prove navigation sensitivity, test geometry defect, timing sensitivity, cross-test interference or a Serene Seasons-specific effect as the cause of the historical timeouts.
- No production correction is indicated by these results. Production code was not changed. Gate D remains stopped; its acceptance/build/audit work was not resumed. No clean build, release audit, or full standard suite was run in this diagnostic phase; compilation was exercised by the requested GameTest runs.
- **Smallest justified next step, for separate roadmap review:** resume the recovered historical failed villager on a disposable copy of its preserved world and observe its existing press reservation, walk target and navigation for a bounded diagnostic window. Preserve the original snapshot, position, press geometry, profession and memories; do not force a target or change the acceptance test. This would probe the actual saved post-selection state instead of relying on fresh spawns to enter it again. A resumed shutdown snapshot would still not reconstruct historical in-memory path state or prove the original tick-by-tick cause.

### Final repository verification

- Only `VintnerGameTests.java` (the narrowly scoped D-R3 observer/callback changes) and this handoff were edited. Gate D ownership code and the original Winemaker setup remain byte-for-byte identical to their starting baselines. All earlier Gate B/C/D/D-R/D-R2 evidence is preserved.
- Final `git diff --check` passed. `git status --short` contains only the expected two modified files; nothing is staged, no unrelated modifications or Python bytecode remain, HEAD is `4513969`, origin comparison is `0 3`, and `mod_version=1.3.1`.
- No commit, push, timeout increase, success-predicate weakening, production change, or automatic Gate D resumption occurred.

## Gate D-R4 — Recovered failed-world continuation

### Starting state, source and integrity

- Verified branch `feat/1.4.0-vineyard-management`, HEAD `45139691135592e3a507099d639260dabfa3c6d4`, origin comparison `0 3`, and version `1.3.1`. Only this handoff and `VintnerGameTests.java` were modified; nothing was staged, no unrelated production changes or Python bytecode existed, and initial `git diff --check` passed. The test file matched the preserved D-R3 final file.
- **Original historical world:** `/tmp/vintner-gate-dr2-cN2FGx/before-run/world`. This is the recovered post-shutdown snapshot described in D-R2, not a live tick-302 capture. It contains the unique historical UUID and failed test-instance record.
- **Disposable copy:** `/tmp/vintner-gate-dr4-xbv8jzf2/run/world`, created with `shutil.copytree`. All 534 original world files were SHA-256 inventoried before launch in `original-world-sha256.json`. The copied world was verified byte-for-byte identical before the successful load. The original file set and every original hash still matched after continuation; `integrity-result.json` records that result. SHA-256 of the inventory file: `19ed78ae8867e1ba8a8ea83ed9e409ece2c5519ed2cbcdd8b9855065144ba687`.
- Exact villager UUID **`f7dc15d2-a3c8-4c6b-8c0f-ce27f9e5dc51`**, dimension **`minecraft:overworld`**, saved position **`(-7870027.799775155, -57.0, -4895330.195989812)`**. Expected press **`(-7870028, -56, -4895332)`**. `persisted-villager.json` preserves the original entity record; `continued-villager.json` preserves the post-experiment record.

### Observation tooling and launch

- Added a clearly marked, temporary `RecoveredWinemakerObserver` in the existing test file, enabled only by `-Dvintner.observeRecoveredWinemaker=true`. It uses public Fabric entity-load, server-start and end-server-tick events and reuses the R3 navigation observer. Everything from the original `FIRST` declaration onward, including all existing tests, setup, predicates and Gate D assertions, remained byte-for-byte unchanged.
- A temporary Gradle init script, `/tmp/vintner-gate-dr4-xbv8jzf2/observer.init.gradle`, directs the existing **release-server development launcher** into the disposable directory and uses an ephemeral network port. Command: `./gradlew runReleaseServer -PsereneSeasonsTest -I /tmp/vintner-gate-dr4-xbv8jzf2/observer.init.gradle`. No GameTest runner, new fixture, replacement villager, clean build, audit, or suite repetition was used.
- The original run's server properties and Serene Seasons configuration were copied unchanged. The first launch stopped at the EULA check before world loading because the GameTest source had `eula=false`; its log is `launch-before-world-load.log`. The already accepted `build/release-server-run/eula.txt` was then copied into the disposable run. World hashes were still identical before the second launch, which performed the sole natural continuation.
- On server start, existing chunks in a 9×9 area around the press received force-load tickets so the entity and its 48-block POI-search neighborhood could tick. This changes chunk availability only in the disposable copy. No villager position/yaw, motion, brain memory, profession, press, POI record, gamerule, clock, or AI behavior was manually changed. The observer ends on recovery or at 300 post-load observations/300 entity ticks, whichever comes first.
- `continuation.log` contains full launcher output; `run/logs/` contains runtime logs; `trace.log`/`trace.json` contain every observation. This was a dedicated-server observation, so no GameTest XML or test-pass count applies. The launcher exited successfully after the observer stopped it; that exit is not an acquisition success.

### Persisted state versus load reconstruction

- At server start: game time **23**, day time **2016**. At the entity-load callback: game time **25**, day time **2018**, **entity tick count 0**. The world had advanced two ordinary ticks while loading, but the historical villager had not ticked. No time-setting call was made.
- The earliest entity-load observation preserved the UUID, exact position and **2.0100516696492123-block** distance. Profession was `minecraft:none`; the villager was alive, adult, AI-enabled and not removed. `POTENTIAL_JOB_SITE` still named its expected press in the Overworld; `JOB_SITE` remained absent. The press was intact, facing north, with input level/type and output type zero. POI identity remained `vintner:winemaker`, **zero free tickets**.
- At load, `WALK_TARGET` was absent, navigation target null, path absent, navigation inactive/done, and stuck false. This is reconstructed runtime state, not proof of what navigation contained when the historical test timed out.
- Exact local 26.2 source confirms that `JOB_SITE` and `POTENTIAL_JOB_SITE` have persistence codecs, while `WALK_TARGET`, `PATH` and `CANT_REACH_WALK_TARGET_SINCE` do not. `LivingEntity` loads the packed brain, `Villager.readAdditionalSaveData` refreshes goals, and `Mob` constructs a new navigation object. Thus the important saved reservation survived, but historical behavior/path progress did not survive as a resumable in-memory navigation trace.

### Natural continuation result

**Outcome B: persisted stuck state reproduced.** There was no natural recovery during the bounded observation. The observer recorded one entity-load sample plus **300 end-tick samples**, spanning entity ticks **0–299**. The first end-tick sample preceded the entity's first tick, so the experiment covers **299 actual natural entity ticks**, not 300; it stopped at the conservative post-load cap without extension.

| Natural entity tick | Position/distance | Job memories and navigation |
| --- | --- | --- |
| 0, entity load | Exact saved position; 2.0100516696492123 | Correct potential site retained; no job/walk memory; no path |
| 1 | Unchanged | Walk target created naturally at press centre `(-7870027.5, -55.5, -4895331.5)`, speed 0.5, radius 1 |
| 2 | Unchanged | Navigation active; one-node partial path to `(-7870028, -55, -4895332)`, endpoint `(-7870028, -57, -4895331)`; `canReach=false`; cannot-reach timestamp 27 |
| 3 | Unchanged | Correct walk/potential target remains; path absent, navigation done/inactive |
| 4–299 | Unchanged throughout | Active one-node path and absent path alternate; correct reservation/walk target retained; no assignment |

- **Closest and farthest sampled distance were identical: 2.0100516696492123.** All 301 observations had exactly the same position. No approach into the two-block radius, movement, or positional oscillation was observed; only navigation state cycled.
- Across the 300 end-tick samples, navigation was active **149** times and inactive **151** times. Every sampled non-null path had the same one-node endpoint, `canReach=false`, and `done=false`; the alternate samples had no path. No stuck flag or non-null completed path was observed at callback boundaries.
- From entity tick 1 onward, every sampled walk target named the expected press with speed 0.5/radius 1. No different walk target or job-site selection was observed. `POTENTIAL_JOB_SITE` remained correct, `JOB_SITE` never appeared, profession stayed `minecraft:none`, and the intact press/Winemaker POI retained zero free tickets throughout.
- Final stop: `acquired=false entityTicks=299 ticksSinceLoad=300`, game time **324**, day time **2317**. Post-shutdown NBT still has the exact original position, unemployed profession and same potential-site reservation. The original source snapshot was not changed.

### Minimal behavioral inspection

- `GoToPotentialJobSite` recreates the correct walk target. There is no sampled evidence of another behavior replacing it. `MoveToTargetSink` accepts a non-null partial path even when `canReach=false`, while retaining `CANT_REACH_WALK_TARGET_SINCE`.
- `GroundPathNavigation.findSurfacePosition` raises a solid-block destination to the space above it, consistent with the logged navigation target one block above the press. This is not a selection of another POI.
- The saved villager is already close to the partial path's final node centre: horizontal offsets approximately **0.299775155** and **0.304010188**, vertical offset zero. `PathNavigation.followThePath` uses waypoint tolerance `0.75 - width/2` for normal villager width 0.6 (approximately **0.45**), and advances a node when those offsets fit. Its tick method does not issue movement toward a path that has become done. `MoveToTargetSink` then stops/clears finished navigation, while the potential-job behavior can recreate the same request.
- This source-level mechanism is consistent with the observed stationary active/absent path cycle: a position can satisfy waypoint arrival tolerance yet remain outside `AssignProfessionFromJobSite`'s strict two-block centre-distance guard. That assignment guard is demonstrably false in every sample. The exact internal path-advance branch was not separately instrumented, so distinguish this source-supported explanation from the direct live observations.
- Offline inspection of the copied local block data found the press at Y=-56, air directly below and around the final node, and the retained GameTest barrier floor at Y=-58. This is artificial GameTest geometry. Only one villager was found in the saved world; another unclaimed Winemaker POI is 45 blocks west, but the observed reservation and walk target never selected it. No foreign-entity, competing-POI or changed-block interference was demonstrated.

### Classification and roadmap boundary

- **Best-supported classification: Persisted stuck state reproduced.** This phase establishes a reproducible diagnostic artifact and natural failure to complete acquisition in the recovered GameTest state. It is stronger evidence than the original isolated 2.010-block shutdown snapshot.
- It does **not** reconstruct the original 300-tick live trajectory, establish the cause of Gate B's separate failure, or demonstrate a Vintner production defect under ordinary gameplay independent of the artificial fixture. Navigation/assignment tolerance and test geometry now warrant concrete roadmap review; no production correction was implemented or selected.
- This is the final recovered-world diagnostic for roadmap review. **Recommended decision process:** review the captured stationary path cycle and waypoint/assignment tolerance mismatch before choosing between test hardening/redesign, a bounded vanilla-AI limitation, or further defect work. Do not automatically accept any of those outcomes, resume Gate D, or run another repetition campaign.
- Only the expected test file and handoff were edited; the Gradle init script and all other observation artifacts are outside the repository. Final `git diff --check` passed, only those two files remain modified, nothing is staged, version remains `1.3.1`, HEAD remains `4513969`, origin comparison remains `0 3`, and no Python bytecode remains. No production code, timeout or original success semantics changed; nothing was committed or pushed.

## Gate D-R5 — Hardened Winemaker fixture and resumed Gate D

### Decision, starting state and geometry

- Roadmap classified the reproduced historical Gate D failure as a **GameTest setup/geometry defect**. R4 reproduced the exact reserved, unemployed villager remaining stationary for 299 natural entity ticks, while waypoint tolerance was satisfied outside the profession-assignment radius. No Vintner production defect was established.
- **The earlier Gate B Winemaker failure is consistent with the subsequently reproduced GameTest geometry issue, but its exact historical cause was not independently proven.**
- Starting branch `feat/1.4.0-vineyard-management`, HEAD `4513969`, origin comparison `0 3`, development version `1.3.1`. Only the expected test file and this handoff were modified, nothing was staged, no unrelated changes or Python bytecode existed, and `git diff --check` passed. The starting test file matched R4's preserved final version.
- Original relative press `(2,1,2)`, villager spawned west at block `(1,1,2)` / feet `(1.5,1,2.5)`: initial distance to press centre **sqrt(1.25) = 1.118033989**. The unsupported fixture allowed a fall onto the lower barrier floor, leaving a 1.5-block vertical offset from the press centre and the reproduced tolerance mismatch.
- Corrected press remains `(2,1,2)`. A stone floor spans the 8-by-8 fixture at `y=0`; the villager spawns three blocks east at `(5,1,2)` / feet `(5.5,1,2.5)`. Initial centre-distance is **sqrt(9.25) = 3.0413812651491097**. The floor supports both the workstation and approach cells, removing the original fall while requiring a short natural approach from outside assignment range.
- The 300-tick deadline, profession success predicate, clock setup, workstation and natural AI acquisition remain unchanged. No post-spawn repositioning, profession/memory assignment, retry, production code, or version change was made. Gate D ownership assertions and helpers remained byte-for-byte unchanged.

### Focused geometry repetitions

Evidence directory: `/tmp/vintner-gate-dr5-zziegit0`. Every invocation preserves its own launcher log, XML, diagnostic JSON and complete run-directory snapshot under `standard-N` or `serene-N`. Exact filter: `vintner:vintner_game_tests_unemployed_villager_claims_grape_press`; compatibility adds `-PsereneSeasonsTest`.

All runs began at distance **3.0413812651491097**. Closest distances below are callback-sampled minima before assignment, rounded to six decimals; raw full-precision values remain in the logs/JSON.

| Run | Standard result / acquisition tick | Closest before assignment | Serene Seasons result / acquisition tick | Closest before assignment |
| --- | --- | --- | --- | --- |
| 1 | 1/1 pass / 28 | 1.989709 | 1/1 pass / 28 | 1.989709 |
| 2 | 1/1 pass / 21 | 1.989709 | 1/1 pass / 21 | 1.989709 |
| 3 | 1/1 pass / 27 | 1.989709 | 1/1 pass / 15 | 1.988206 |
| 4 | 1/1 pass / 30 | 1.989709 | 1/1 pass / 20 | 1.989709 |
| 5 | 1/1 pass / 18 | 1.989709 | 1/1 pass / 16 | 1.786656 |
| 6 | 1/1 pass / 29 | 1.989709 | 1/1 pass / 85 | 1.997640 |
| 7 | 1/1 pass / 15 | 1.989709 | 1/1 pass / 22 | 1.989709 |
| 8 | 1/1 pass / 17 | 1.989709 | 1/1 pass / 19 | 1.989709 |
| 9 | 1/1 pass / 18 | 1.989709 | 1/1 pass / 21 | 1.989709 |
| 10 | 1/1 pass / 16 | 1.989462 | 1/1 pass / 80 | 1.931569 |

- **Standard 10/10**, acquisition range **15–30 ticks**; **Serene Seasons 10/10**, range **15–85 ticks**. At least 215 ticks remained before the unchanged deadline. Every run naturally crossed inside the assignment radius. These repetitions validate the hardened fixture; they do not assert identical timing or remove vanilla AI scheduling variability.

### Diagnostic cleanup

- Removed the property-gated R4 recovered-world observer, historical UUID/coordinates, lifecycle hooks, forced-chunk observation setup and automatic server-stop tooling from the test class.
- Removed the R3 navigation tracing accumulator, transition logging, successful-run logging and investigation-only imports. Preserved all historical traces outside the repository.
- Retained a concise failure-message helper reporting test tick, actual profession, position/distance, potential/job memories, expected press/block, live POI identity and free tickets. It only reads existing state when the original profession predicate is false.
- Formatting checks passed after cleanup; Gate D and subsequent tests/helpers still matched the starting copy exactly.

### Resumed validation

All following checks ran after temporary diagnostic cleanup, in the required order. Each GameTest has separately preserved XML, full launcher log and run snapshot under the evidence directory; `clean build` did not erase the preserved reports.

| Check | Command/filter | Result | Evidence stem |
| --- | --- | --- | --- |
| Ownership isolation | exact `estate_desk_payloads_remain_isolated_between_owners` filter | **1/1 pass** | `ownership` |
| Estate group | `vintner:vintner_game_tests_*estate*` | **11/11 pass** | `estate` |
| Village/workstation group | `vintner:vintner_game_tests_*villag*` | **7/7 pass** | `village` |
| Full standard | `./gradlew runGameTest` without filter | **166/166 pass** | `full-standard` |
| Full compatibility | `./gradlew runGameTest -PsereneSeasonsTest` without filter | **166/166 pass** | `full-serene` |
| Clean build | `./gradlew clean build` | **Pass** | `build.log` |
| Explicit release audit | `./gradlew auditReleaseAssets` | **Pass** | `audit.log` |

- Both the audit within `check` and the explicit audit passed with **1,567 JSON files, 169 recipes, 159 public wood-family blocks, 24 wood-preserving grapevine states**. `build.gradle` makes `check` depend on `auditReleaseAssets`. Counts did not change.
- Ownership coverage remains the original Amber/Cobalt A → B → A scenario: exact estate identity, owner-specific plots and ledger/reputation values, real `EstateDeskReport.open` outbound payloads, unchanged saved state for both owners after each opening, and identical repeated A payload. No ownership assertions were rewritten.
- **Gate D PASS / accepted for its automated server integration scope.** All mandatory R5 checks passed; the change is eligible for the single roadmap-authorized commit. No production file, development version, timeout or profession success predicate changed. No claim of manual client/release acceptance is made.
- Final diff review is limited to `VintnerGameTests.java` and this handoff. Temporary R4 hooks/tracing and their imports are absent from source. The single ignored audit-generated bytecode file was removed; no Python bytecode remains. Final `git diff --check` passed, with only these two expected files modified and nothing staged before the authorized commit.

## Gate E — Loaded-plot reporting performance

### Baseline and production-path inspection

- Baseline matched: branch `feat/1.4.0-vineyard-management`, HEAD `9c8ee9fc9febdbeb31859f634d23a75c02edc7dc`, clean working tree, nothing staged, remote-only/local-only comparison `0 4`, version `1.3.1`, no Python bytecode, and passing `git diff --check`. No fetch was performed; all four local commits were preserved. Gate D was not rerun.
- `EstateManagementDeskBlock` calls `EstateDeskReport.open` on server-side desk interaction. The screen retains its received payload; tab/map/scroll interactions do not request periodic report refreshes. No report cache or application-level desk-report throttle was found. Reopening requests a fresh report; repeated players/openings can therefore add synchronous work to a server tick.
- `open` performs estate/ledger lookup, infrastructure survey, reputation synchronization, plot analysis, market/terroir evaluation, section construction, atlas lookup and outgoing payload dispatch. It analyses each registered plot once and reuses those per-call snapshots for text and structured summaries. The eight-plot text limit does not cap analysis; all 16 supported plots are analysed and included in structured summaries.
- Maximum registration is 16 plots per owner, each at most 32×32. `analyzeIfLoaded` rejects dimension mismatch or any absent chunk in the plot's inclusive X/Z bounds expanded by 12 blocks, using `hasChunk` before block scanning. The desk resolves each plot's actual dimension first. The margin is a chunk-availability guard, not an additional 24-wide block sweep.
- Main scan: X/Z columns, Y from `anchorY - 4` through `anchorY + 5` (ten levels), stopping after the first lower vine in each column. Thus **640 / 10,240 / 163,840** are upper bounds for this loop for the three scenarios, not complete report-operation counts. The measured half-planted fixtures have their lower vines at anchor Y: calculated loop visits are **480 / 7,680 / 122,880**. These are source/fixture calculations, not instrumented counters.
- Additional work includes irrigation for every vine, up to 32 condition samples per plot, and one shared terroir sample per plot. Dry irrigation can probe 40 nonzero Manhattan-radius-four offsets at two heights (80 fluid lookups); the direct per-vine and sampled-condition irrigation calls in these fixtures account for **5,120 / 43,520 / 696,320** such probes. This is separate from the main-loop counts and excludes other condition/terroir accesses.
- Infrastructure always sweeps **33×17×33 = 18,513** positions around the desk, plus content-dependent cellar checks. Atlas discovery checks 74 neighboring positions. Terroir adds soil, terrain, sky/biome and up-to-radius-12 water inspection. No single 163,840-position estimate describes the full report cost.
- `SeasonalContext.current` in sampled condition evaluation has a Serene Seasons-specific reflective calendar path when installed. This campaign measures the requested standard baseline only; optional-integration timing equivalence is not established. No separate compatibility campaign or optimisation was performed.

### Method, fixture and environment

- Evidence: **`/tmp/vintner-gate-e-iiKKx6Mr`**. `launcher.log` and `run/logs/` preserve startup/mod/configuration and all observations; `samples.json` holds all 48 individual nanosecond measurements; `summary.json` holds first/min/median/max and individual measured milliseconds; `run/measurement-world` preserves the disposable world. `environment.txt`, `compile.log` and `final-compile.log` record environment/compilation.
- Apple M4 Pro, arm64, 24 GiB RAM; macOS 26.6.2 build 25G83. Homebrew OpenJDK 25.0.3, 64-bit Server VM, default collector/settings apart from explicit **`-Xmx2G`** (observed max heap 2,147,483,648 bytes). Minecraft 26.2, Fabric Loader 0.19.3, Fabric API 0.155.2+26.2, Vintner 1.3.1 and the normal Fabric development dependencies; no Serene Seasons/GlitchCore. Full runtime arguments/mod list remain in the launcher log.
- Existing `runReleaseServer` launcher, standard mod set, disposable seed-0 flat plains world, no generated structures, view/simulation distances four, no connected players, ephemeral port, and empty-server pausing disabled for observation. The server used ordinary paced ticks; it was not the accelerated GameTest runner. No existing test assertions or regular-suite membership changed.
- Three separate registered owners: one 8×8 plot at X/Z `(0,0)`; one 32×32 at `(128,0)`; sixteen non-overlapping 32×32 plots in a 4×4 grid from `(512,0)` with 48-block pitch. All anchors Y=64. Lower/upper mature grapevine pairs grow on vineyard soil over dirt in every other Z row, alternating red and white rows, with dirt walkways. Counts: **32 / 512 / 8,192 roots**, twice as many vine blocks. No all-air substitution, irrigation water, greenhouse cover, managed cultivar/age history, cellar inventory, maps or ledger history was added. This is a consistent populated rain-fed fixture, not the densest conceivable estate.
- Loaded and force-kept all fixture/margin/desk-survey chunks plus a halo before timing; preparation and generation were outside measurement. After a 100-tick settling period, called on `ServerTickEvents.END_SERVER_TICK`, verified the normal server thread, and made one call every five ticks. Scenario order was small → large → maximum in one JVM. Each had **one first call, five warm-ups, ten measured calls**. First means first for that scenario; only the small scenario was first in the process. Later scenarios share JVM/JIT warm-up.
- `System.nanoTime()` brackets only the real **`EstateDeskReport.open`** call. A public `ServerGamePacketListenerImpl.send` override captures its actual `EstateDeskPayload`, following Gate C/D. Included: complete report path and capture/list insertion. Excluded: fixture setup/loading, validation, result logging, socket serialization/transmission, client/UI work and the rest of the server tick. There is no loaded-report result cache; these are fresh scans with naturally warm JVM/data/chunk caches. No separate scan or whole-tick timing was taken.
- After every call, verified one payload, exact expected plot count, every `loaded=true`, matching names/bounds/areas and exact vine count per plot. Every prepared chunk remained present before/after each call, and total loaded-chunk count was unchanged at **2,854 → 2,854** during all 48 calls. No incorrect payload, observed chunk-count increase or severe delay occurred. An observation-only one-second safety stop was present but never triggered; it is not an acceptance threshold.

### Measured results

Durations in milliseconds. Min/median/max use only the ten post-warm-up samples; first calls are separate.

| Scenario | Plots / roots | First | Minimum | Median | Maximum |
| --- | --- | --- | --- | --- | --- |
| Small vineyard | 1×8×8 / 32 | 24.435 | 3.255 | 3.709 | 7.011 |
| Large vineyard | 1×32×32 / 512 | 8.571 | 1.304 | 5.575 | 7.694 |
| Maximum estate | 16×32×32 / 8,192 | 28.276 | 18.032 | 25.994 | 29.042 |

- Individual measured ms, small: `6.088666, 6.525375, 7.011417, 3.575500, 3.533917, 3.843375, 3.353875, 3.503583, 3.254833, 4.299833`.
- Large: `6.537375, 5.952541, 6.349916, 5.998000, 1.303542, 7.693875, 1.768084, 5.196583, 4.935000, 1.708459`.
- Maximum: `18.264000, 28.587791, 29.042000, 23.152291, 18.032375, 25.242542, 28.824542, 27.952958, 26.744458, 19.570750`.
- **Characterisation complete; specific concern demonstrated:** a maximum populated estate consumes approximately **36–58% of a nominal 50 ms tick budget** in this report call alone (median about 52%). Repeated opens are fresh synchronous work, so coincident requests warrant targeted profiling before deciding whether performance work is needed. No threshold was invented, no whole-tick overrun was measured, and these samples do not prove multiplayer performance safety or a production correctness defect.
- Limits: one machine/JVM, synthetic rain-fed fixtures, fixed scenario order, ten measured calls each, no busy multiplayer workload, and no allocator/GC/CPU attribution. Warm-up and run-to-run variance are visible; the large scenario's minimum being below the small scenario's minimum does not establish a size reversal. No isolated pathological outlier or failure prompted a repeat. Denser planting, populated facilities/maps/ledger, different hardware and optional season integration remain unmeasured. Do not extrapolate these numbers to all release workloads.

### Tooling cleanup and validation

- Temporary source `LoadedReportMeasurement.java` and its six-line opt-in hook were preserved in the evidence directory (`LoadedReportMeasurement.java`, `opt-in-hook.patch`, `measurement.init.gradle`) then removed from the repository. No measurement framework or permanent hook remains.
- Reproduction: restore the saved observer under the test package and apply the saved hook; use a new disposable directory with the preserved `server.properties`/EULA and adjust the init script's run directory; launch `./gradlew runReleaseServer -I <init-script>`. The property enables observation; no GameTest filter/annotations are involved. Do not reuse an existing measured world without intentionally accounting for its saved owners/plots. Restore/remove only those temporary additions after collecting evidence.
- Initial `./gradlew compileJava`, the dedicated-server measurement invocation, and final `./gradlew compileJava` after cleanup all passed. Existing deprecated mock-player warnings remain unchanged. No full suites, clean build, release audit or Gate D repetition ran.
- Final source matches HEAD, including all tests, production code and version. Only this handoff is modified; nothing staged/committed/pushed, four local commits preserved, no Python bytecode, and `git diff --check` passed.

## Gate F — Maximum-estate CPU/allocation attribution

### Baseline and preserved Gate E commit

- Verified expected branch, HEAD `9c8ee9f`, origin comparison `0 4`, version `1.3.1`, only the Gate E handoff modified, nothing staged, no unrelated/untracked files or bytecode, and passing `git diff --check`. Reviewed the entire Gate E documentation diff and committed it separately as **`4e3a699` — `Document loaded estate reporting performance`**. The tree was clean afterward; comparison became `0 5`. No push occurred.
- Re-inspection confirmed the Gate E path: every eligible desk opening runs a fresh infrastructure survey, one analysis per registered plot, per-vine irrigation, up to 32 condition samples per plot, shared per-plot terroir, and payload assembly. Per-call plot snapshots are already shared by text/structured sections. The broad fluid neighborhoods overlap spatially between vines, and sampled vines repeat irrigation in condition evaluation. Transient report lists/components exist, but their cost must be distinguished from the world lookups.
- A player can close/reopen to request another full report. No Vintner server-side cooldown or duplicate-request guard is present. Screen navigation itself does not regenerate the report. This is code-path evidence of retriggerability, not a measured multiplayer request-rate experiment.

### Profile method and evidence

- Unique evidence directory: **`/tmp/vintner-gate-f-hJE515RG`**. `report.jfr`, `events.json`, `profile-summary.json`, and `profile.log` preserve the profile and analysis; `before.log`, `after.log`, `comparison.json`, and `payloads.json` preserve controlled timing/payload evidence. Separate disposable worlds remain in `run`, `before-run`, and `after-run`.
- Reused Gate E's fixture/observer: three owners, 32/512/8,192 mature rain-fed roots, maximum 4×4 grid of sixteen 32×32 plots at 48-block pitch, preloaded margins and desk-survey halo. Same M4 Pro/24 GiB/macOS 26.6.2, OpenJDK 25.0.3, standard Minecraft 26.2/Fabric/Vintner mod set, 2 GiB heap, no players, and paced normal server thread. No new dependency or production profiling seam.
- The original 48-call small/large/maximum sequence ran first. Then JDK Flight Recorder recorded **60 additional maximum-estate calls**, five server ticks apart, after setup and warm-up. Enabled `jdk.ExecutionSample` at 1 ms with stacks and `jdk.ObjectAllocationSample` at 1,000/s with stacks, plus GC events. The 15-second recording contained 1,193 execution samples and 2,400 allocation samples overall. Three GC events were recorded, without attributing their pauses to individual report calls.
- Export command: `jfr print --json --stack-depth 128 --events jdk.ExecutionSample,jdk.ObjectAllocationSample report.jfr`. Analysis includes only stacks containing `EstateDeskReport.open`: **1,019 CPU samples** and **2,365 allocation samples**, with about **1.268 GB allocation sample weight** across 60 calls. Allocation weights are statistical estimates, not exact allocated-byte counters; inclusive stack percentages overlap and must not be summed. Profiler-on timings were not used for the before/after comparison.

| Report call path | Inclusive CPU samples | Share of report CPU samples | Share of report allocation weight |
| --- | --- | --- | --- |
| Plot analysis | 957 | 93.9% | 99.6% |
| Irrigation checks | 753 | **73.9%** | **79.3%** |
| Condition evaluation | 63 | 6.2% | 5.1% |
| Terroir inspection | 57 | 5.6% | 3.5% |
| Infrastructure survey | 50 | 4.9% | Not a leading sampled allocation path |

- `BlockPos` accounts for approximately **1.233 GB / 97.2%** of report allocation sample weight. The irrigation path accounts for about **1.005 GB** of weight (roughly 16.8 MB/call estimated). This supports investigating per-probe coordinate allocation, rather than assuming infrastructure or payload collections dominate. Main-loop coordinates are another allocation source; they were not changed.
- The largest theoretical scan was not used as a CPU proxy. Fluid lookup/navigation through level/chunk data is part of the irrigation stack, and a hot allocating path does not prove that removing its allocation will proportionally reduce elapsed time.

### One local candidate, measured and rejected

- Tried exactly one candidate in `VineyardIrrigation.isIrrigated`: allocate a method-local `MutableBlockPos`, reuse it with `setWithOffset(rootPos, x, y, z)`, and retain the same loops, radius, vertical range, order, fluid predicate and early return. No world mutation, cache, threading, ownership, persistence or save-format change.
- Local Minecraft source inspection confirmed `Level.getFluidState` and `LevelChunk.getFluidState` consume coordinates synchronously, with the latter immediately reading integer coordinates; no retained mutable position was introduced. Existing wet/dry and radius-boundary tests were identified, but acceptance suites were not run because the candidate failed the performance-retention criterion first.
- Before/after used separate identical fresh worlds and the same unprofiled 48-call sequence: first, five warm-ups, ten measured calls per scenario, every five ticks. The extra baseline captured complete payload values. Setup, validation, payload logging and network/client work stayed outside the timer, as in Gate E. Each call verified correct loaded plot bounds/counts and vine counts; prepared chunks stayed present and loaded counts stayed **2,854 → 2,854**.

Maximum-estate report durations in milliseconds; changes are candidate minus comparable unprofiled baseline (negative means faster):

| Metric | Baseline | Candidate (rejected) | Absolute change | Percentage change |
| --- | --- | --- | --- | --- |
| First | 27.717 | 26.018 | -1.699 | -6.13% |
| Minimum | 19.972 | 20.822 | +0.850 | +4.26% |
| Median | 26.341 | 24.942 | -1.399 | -5.31% |
| Maximum | 29.991 | 27.960 | -2.031 | -6.77% |

| Sanity fixture | Baseline first / min / median / max ms | Candidate first / min / median / max ms |
| --- | --- | --- |
| One 8×8 | 33.228 / 2.859 / 4.295 / 5.506 | 28.444 / 2.160 / 3.670 / 5.119 |
| One 32×32 | 9.910 / 1.875 / 5.915 / 7.543 | 8.003 / 5.162 / 6.434 / 8.619 |

- **Rejected:** maximum median improved only 1.399 ms (5.3%), with heavily overlapping ranges, a higher minimum, and a slightly higher large-plot median. This bounded evidence does not distinguish a robust latency benefit from run-to-run variation. Gate E's independent maximum median was 25.994 ms, also close to the candidate. No further candidate was attempted; no optimisation is retained.
- **Payload equivalence:** captured each scenario's complete final `EstateDeskPayload` record representation before/after; all three matched exactly, including estate/subtitle, every section/component, structured plot metrics, map list and atlas flag. Every call also passed explicit loaded/bounds/area/vine-count checks. `payloads.json` preserves those full values. This is meaningful-field fixture equivalence, not exhaustive equivalence for every possible vineyard, facility or map inventory.

### Cleanup, validation and decision

- Preserved `rejected-candidate.patch`, `ProfileObserver.java`, `MeasurementObserver.java`, `opt-in-hook.patch`, and the three launcher init scripts outside the repository. To reproduce, restore the relevant observer as `LoadedReportMeasurement.java` under the test package and apply the opt-in hook; direct the corresponding init script to a new disposable run directory with the preserved server properties/EULA, then run `./gradlew runReleaseServer -I <init-script>`. Never reuse an already populated owner/plot world as a fresh fixture.
- Removed the candidate and all temporary observer/hook code with scoped patches. **All production and test source matches HEAD again.** The three dedicated-server invocations compiled/ran successfully; final `./gradlew compileJava` after cleanup passed (`final-compile.log`). No focused/full acceptance suites, clean build or release audit were run: no production optimisation survived the timing decision. These checks are **not run**, not claimed passed.
- Final `git diff --check` passed; only this handoff is modified, nothing staged, no Python bytecode or unrelated files, version `1.3.1`, HEAD `4e3a699`, origin comparison `0 5`. Gate F documentation remains uncommitted; no optimisation commit or push was made.
- **Release concern remains:** supported maximum-size populated estates still cost roughly half a nominal tick budget per synchronous report on this machine and may be rapidly retriggered. This is not evidence of whole-tick failure or an architectural requirement. The profile narrows the work to irrigation; the allocation-only candidate did not establish a sufficient latency improvement. One machine, short runs, fixed fixture order, sampling/JIT/GC effects and dry synthetic vineyards limit the conclusion. Optional seasons, dense planting, facilities, maps and busy multiplayer remain unmeasured.
- **Smallest justified next action:** roadmap review of a bounded design to share overlapping irrigation fluid reads within a single report. Preserve fresh-report semantics and avoid cross-report caching; do not implement another candidate without that next scope decision.

## Gate G — Single-analysis irrigation reuse experiment

### Baseline and preserved Gate F record

- Verified branch `feat/1.4.0-vineyard-management`, HEAD `4e3a699`, origin comparison `0 5`, development version `1.3.1`, only the Gate F handoff modified, nothing staged, no Python bytecode or unrelated files, and passing `git diff --check`. Inspected the entire accepted Gate F documentation diff and committed it separately as **`a26e9c0` — `Document estate reporting profile`**. The working tree was clean afterward. No push or amendment occurred.
- Used Gate F's JFR and unmodified-production baseline measurements as the starting evidence. No allocation-only candidate from Gate F remained in source.

### Exact duplication and bounded candidate

- `VineyardIrrigation.isIrrigated` visits offsets in X/Z loop order, excluding distance zero and Manhattan distance greater than four. Each of the 40 remaining horizontal offsets checks Y=-1 and Y=0, returning immediately when its fluid state is tagged water. Dry roots therefore perform 80 fluid queries and create 80 offset positions.
- `VineyardPlotReport.analyze` calls irrigation for each lower vine. The first 32 vines additionally call `GrapeQualityEvaluator.inspectWithTerroir`, which repeats irrigation. Adjacent vines in the every-other-row fixture share most queried coordinates. Nearby/adjacent plot margins can also overlap, although the benchmark's 48-block plot pitch leaves the irrigation neighborhoods of separate plots disjoint. Terroir performs its own differently defined water-block search; it was not merged with fluid-tag irrigation.
- Enumerated the actual dry-fixture coordinate sets, preserving the same offsets and root positions. These are **calculated query counts, not runtime counters**; `calculated-lookup-counts.json` preserves them. Water early exits would change these counts in wet fixtures.

| Fixture | Original fluid queries including condition repeats | Distinct queries with candidate, summed per plot | Calculated queries avoided |
| --- | --- | --- | --- |
| 1×8×8, 32 roots | 5,120 | 388 | 4,732 / 92.4% |
| 1×32×32, 512 roots | 43,520 | 2,980 | 40,540 / 93.2% |
| 16×32×32, 8,192 roots | 696,320 | 47,680 | 648,640 / 93.2% |

- Tried one local candidate: a `VineyardIrrigation.ReportLookup` created inside each synchronous plot analysis, bound to that level, with the existing Minecraft FastUtil primitive long-to-byte map keyed by packed XYZ. Entries distinguish unobserved, dry and water. Only previously requested positions are memoized; lookup order/early return, tag, radius and vertical extent remain the same. No eager margin scanning or additional world reads were introduced.
- Reused each vine's already computed irrigation boolean through a condition-evaluator overload, removing its duplicate irrigation calculation. The plot-analysis local variable was the natural lifetime boundary; no report context already existed. No map was stored in a snapshot, static field, saved state or player object. Every plot and every new opening received a fresh map. Separate plots did not share a map, intentionally keeping level/ownership scope and lifecycle simple.
- No dependency, client payload, persistence, off-thread access, cooldown or cross-report invalidation system was introduced. The entire candidate was later removed.

### Untimed semantic gate before measurement

- Evidence directory: **`/tmp/vintner-gate-g-KwHQ2EEo`**. Restored the existing opt-in measurement observer temporarily; no regular GameTest membership or assertions changed.
- Before candidate timing, a separate `equivalence-run` constructed all three deterministic fixtures and called the real `EstateDeskReport.open` **without timing**. Each complete payload record representation was compared against the corresponding unmodified-production Gate F value (`payloads.json`, baseline side), copied as `expected-small.txt`, `expected-large.txt`, and `expected-maximum.txt`.
- **All three complete meaningful payloads matched exactly.** This includes estate identity/subtitle, every section/component, plot names/dimensions/bounds, loaded flags, all vineyard metrics, market/ledger/facility presentation, maps and atlas flag. Empty fixture sections remain a limitation, not coverage of populated inventories. The synthetic owner names and fixture data were equivalent; arbitrary UUIDs are not payload fields.
- Per-call structural checks also verified exact plot counts, loaded flags, bounds/areas and 32/512/8,192 roots. All prepared chunks remained present, with loaded count **2,854 → 2,854**. `equivalence.log` records `GATE_G_EQUIVALENCE_COMPLETE`. No payload mismatch or unexpected chunk loading occurred, so measurement proceeded.

### Timing and repeat JFR

- Same standard environment as E/F: M4 Pro/24 GiB, macOS 26.6.2, OpenJDK 25.0.3, 2 GiB heap, Minecraft 26.2/Fabric Loader 0.19.3/Fabric API 0.155.2+26.2/Vintner 1.3.1, no optional seasons, no connected players. Fresh disposable flat seed-0 worlds, populated rain-fed rows, identical plot pitch/coverage and fixture preparation outside timing.
- A separate fresh `measurement-run` used the established 48-call sequence: first + five warm-ups + ten measured calls per scenario, one call every five ordinary server ticks, small → large → maximum. The first 48 calls were unprofiled. JFR then recorded 60 additional warmed maximum-estate calls, using the same 1 ms CPU and 1,000/s allocation settings as F. Complete report construction and packet capture are timed; setup, checks/logging, socket/client work and the rest of the tick are excluded.
- Before values below are Gate F's comparable **unmodified-production unprofiled** baseline, not its rejected mutable-position timings. They are a prior same-environment run, not a simultaneous paired measurement. `comparison.json`, `timing-summary.json`, `samples.json`, and `measurement.log` preserve individual timings and all 108 calls. Every call retained the expected loaded payload and unchanged chunk count.

| Fixture | Baseline first / min / median / max ms | Candidate first / min / median / max ms | Median change |
| --- | --- | --- | --- |
| One 8×8 | 33.228 / 2.859 / 4.295 / 5.506 | 20.385 / 2.325 / 3.834 / 5.042 | -0.462 ms / -10.7% |
| One 32×32 | 9.910 / 1.875 / 5.915 / 7.543 | 5.663 / 3.144 / 4.645 / 6.751 | -1.270 ms / -21.5% |
| Sixteen 32×32 | 27.717 / 19.972 / 26.341 / 29.991 | 25.133 / 20.055 / 25.625 / 27.835 | **-0.716 ms / -2.7%** |

`report.jfr`, `events.json` (exported with `--stack-depth 128`) and `profile-summary.json` preserve the candidate profile. Only stacks containing `EstateDeskReport.open` are counted; inclusive percentages overlap. Allocation sample weight estimates bytes statistically and is not an exact byte counter.

| Profile measure | Gate F baseline, 60 calls | Gate G candidate, 60 calls |
| --- | --- | --- |
| Report CPU samples | 1,019 | 930 |
| Irrigation inclusive CPU share | 73.9% | 68.3% |
| Infrastructure CPU share | 4.9% | 6.5% |
| Report allocation samples | 2,365 | 584 |
| Total report allocation sample weight | 1,267,875,656 bytes | 333,349,592 bytes |
| Irrigation lookup allocation share | 79.3% | 12.0% |
| BlockPos allocation weight | 1,232,817,792 bytes | 254,204,616 bytes |

- Allocation weight fell approximately **73.7%** overall. Primitive map arrays are visible in the candidate profile (about 25.5 MB long arrays and 10.3 MB byte arrays across the sampled report stacks). Irrigation remained the dominant CPU path; no unrelated system replaced it. Per-vine offset enumeration and map probes still occur even when world queries are avoided. The profile does not establish a proportional CPU saving from the removed allocations.

### Rejected candidate and cleanup

- **Rejected under Gate G's retention rule.** Exact payload equivalence and a large allocation reduction were demonstrated, but the maximum-estate median improved only 2.7%, with heavily overlapping ranges and effectively the same minimum. Gate E's 25.994 ms maximum median is also close to this candidate's 25.625 ms. CPU attribution still centers on irrigation. The profiler gain was not sufficient to justify carrying the added map/overload complexity without a convincing maximum-report latency gain. No second candidate or tuning campaign was attempted.
- Preserved the full rejected source diff as `rejected-candidate.patch`, observer source as `LoadedReportMeasurement.java`, hook as `opt-in-hook.patch`, and both launcher init scripts. To reproduce, apply the preserved candidate/hook and observer under the test package, use a fresh disposable directory with the preserved server properties/EULA, and run the equivalence init script before the measurement init script. Adjust evidence/run paths when replaying; do not overwrite these worlds or baseline files.
- Removed all candidate production code and temporary observer/hook code using scoped patches. **Production and existing test source match HEAD exactly.** Final `./gradlew compileJava` passed (`final-compile.log`), as did `git diff --check`. No focused/full acceptance suites, clean build or audit ran for the rejected candidate; those results are **not run**, not passed.
- Only this handoff remains modified and uncommitted; nothing staged, no Python bytecode or unrelated files, HEAD `a26e9c0`, remote-only/local-only comparison `0 6`, version `1.3.1`. No optimisation commit, push, version bump or change to prior commits occurred.
- **Maximum-estate reporting remains a release concern:** production still performs the original synchronous work, and rapid reopen requests remain possible. This experiment establishes duplicate-read/allocation removal without sufficient elapsed-time benefit; it does not demonstrate a whole-tick overrun or require persistent caching. Dry synthetic fixtures, short same-machine runs, JIT/GC/sampling variation and no real multiplayer load constrain interpretation.
- **Smallest next action:** roadmap review of a bounded per-plot irrigation evaluation that reduces the repeated per-vine probe loop itself, rather than only memoizing its world queries. Any new implementation requires a separate scope decision; facility persistence and manual client/multiplayer QA remain separate.

## Gate H — Final irrigation enumeration optimisation

### Starting state and scope

- Verified expected branch, HEAD `a26e9c0`, remote-only/local-only comparison `0 6`, development version `1.3.1`, only the accepted Gate G handoff modified, nothing staged, no unrelated files or bytecode, and passing `git diff --check`. Inspected the Gate G diff and committed it separately as **`63a5e0a` — `Document irrigation optimization experiment`**. The tree was clean afterward. Prior commits were preserved and nothing was pushed.
- This is the final dedicated 1.4.0 performance optimisation attempt. No cooldown, persistent world-state cache, async access, background index, payload/save-format change or general estate optimisation is included.

### Enumeration finding and candidate

- The original irrigation method executes a 9×9 horizontal loop for every root and every sampled condition: 81 radius/distance checks, retaining 40 nonzero Manhattan-radius-four offsets at two heights. Neighbors and sampled roots repeat the same geometry. There are no coordinate streams/lists, but offsets allocate positions repeatedly. Gate G removed many world queries while retaining those per-vine loops/map probes, which explains why that experiment did not by itself demonstrate sufficient CPU reduction.
- Plot bounds and the fixed ten-level root scan provide a natural bounded alternative. The candidate creates one `VineyardIrrigation.PlotInfluence` inside an already-loaded plot analysis. On the first root at a given Y, it examines water at Y-1/Y across the plot rectangle expanded by four blocks. For each water column, it marks the **exact nonzero Manhattan-radius-four** root influence into a `boolean[width * depth]` grid. Subsequent roots at that height read one grid entry. Only heights actually containing scanned roots allocate/build a grid; at most ten grids exist per analysis.
- This inverts the geometry without changing its predicate: a root is irrigated iff a water-tagged fluid exists at horizontal distance 1–4 and vertical offset -1 or 0. The water column directly under/at the root remains excluded. Influence markings are idempotent and dimension-local. Each vine's resulting boolean is reused for its condition sample through an overload of `inspectWithTerroir`; other callers retain direct irrigation calculation.
- Each analysis discards its level-bound grids on return. No context survives into the payload, another plot, another tick or another opening. The original direct irrigation method remains available and unchanged. `VineyardPlotReport.analyze` uses the grid only if the existing 12-block-margin chunk guard passes; partially loaded direct callers, including the Almanac path, retain the original per-root traversal. The desk's `analyzeIfLoaded` guard and wrong-dimension behavior are unchanged.
- The rectangular scan reads some positions outside the union of actual vine neighborhoods, but only within the already-loaded guard. It creates no new chunk requirement. Wet columns execute the exact influence loop; dry columns do not enumerate geometric offsets. Very wet/dense terrain is a performance limitation to assess separately, not an approximation of the irrigation result.

Calculated dry-fixture counts (not instrumented counters), preserved in `calculated-enumeration.json`:

| Fixture | Original horizontal checks | Original fluid probes | Unique original probe positions (per-plot sum) | Candidate water columns | Candidate fluid probes |
| --- | --- | --- | --- | --- | --- |
| 1×8×8 / 32 roots | 5,184 | 5,120 | 388 | 256 | 512 |
| 1×32×32 / 512 roots | 44,064 | 43,520 | 2,980 | 1,600 | 3,200 |
| 16×32×32 / 8,192 roots | 705,024 | 696,320 | 47,680 | 25,600 | 51,200 |

- Original probe duplication is 92.4% / 93.2% / 93.2% relative to unique positions. The candidate deliberately uses a simple bounded rectangle rather than a hash-based unique-position set. For these dry fixtures it performs no water-influence offset loop, removing the repeated per-root geometric work rather than merely accelerating its world queries.

### Equivalence before timing

- Evidence directory: **`/tmp/vintner-gate-h-iDP6vEsj`**. A separate untimed `equivalence-run` used the preserved three deterministic populated fixtures and compared complete production-authored `EstateDeskPayload` record values against the unmodified-production Gate F baselines (`expected-small.txt`, `expected-large.txt`, `expected-maximum.txt`).
- **All three payload comparisons passed before timing.** Estate/ownership presentation, every section, loaded flags, plot bounds, irrigation, condition/yield/quality metrics and map/atlas fields matched exactly. Empty facility/ledger/map fixture content remains a coverage limit. Per-call checks confirmed all plots loaded, expected root counts and unchanged loaded chunks (**2,854 → 2,854**). No unexpected loading or meaningful payload difference occurred.
- Added one permanent regression test, `plotIrrigationInfluenceMatchesDirectQueriesAcrossHeights`: compares the grid to the original direct query at 48 positions across three heights, repeats after channel removal using a fresh grid, and explicitly checks zero-horizontal-offset exclusion. Existing plot tests also verify physical irrigation coverage and fresh dry results after removing water.

### Timing and JFR

- Same standard environment and fixture as E/F/G: M4 Pro/24 GiB, macOS 26.6.2, OpenJDK 25.0.3, 2 GiB heap, Minecraft 26.2, Fabric Loader 0.19.3, Fabric API 0.155.2+26.2, Vintner 1.3.1, no Serene Seasons or connected players. Fresh flat seed-0 world, alternating mature red/white rows, 32/512/8,192 roots, maximum 4×4 plot grid at 48-block pitch, all chunk/fixture preparation outside timing.
- In a fresh `measurement-run`, the first 48 calls were unprofiled: each scenario had a separately recorded first call, five warm-ups and ten measured calls, five ordinary server ticks apart. Timer brackets only real `EstateDeskReport.open` and public packet capture; validation/logging, setup, socket/client work and the rest of the tick are excluded. Scenario order small → large → maximum is fixed; only the first small call is process-cold.
- Then JFR recorded 60 additional warmed maximum-estate calls with the same 1 ms execution sampling and 1,000/s allocation sampling as F/G. No broad campaign or repeated full suites were used for measurement. `measurement.log`, `samples.json`, `timing-summary.json`, `comparison.json`, `report.jfr`, `events.json` and `profile-summary.json` preserve raw evidence. All 108 calls passed loaded-payload/chunk-count checks.

Baseline below is Gate F's comparable unmodified-production run on the same environment, not either rejected candidate. Durations are ms, first / minimum / median / maximum:

| Fixture | Baseline | Gate H | Median change |
| --- | --- | --- | --- |
| One 8×8 | 33.228 / 2.859 / 4.295 / 5.506 | 19.400 / 2.823 / 3.458 / 4.379 | -19.5% |
| One 32×32 | 9.910 / 1.875 / 5.915 / 7.543 | 4.394 / 1.668 / 4.122 / 5.476 | -30.3% |
| Sixteen 32×32 | 27.717 / 19.972 / 26.341 / 29.991 | 15.714 / 5.848 / 11.717 / 13.666 | **-14.624 ms / -55.5%** |

- All ten maximum-estate measured calls fall below the baseline's measured minimum. The result is also substantially below Gate E's 25.994 ms and Gate G's 25.625 ms medians. This clears the 15–20% practical retention threshold within the bounded evidence; smaller fixtures show no meaningful regression.

| Maximum-estate profile, 60 calls | Gate F baseline | Gate H |
| --- | --- | --- |
| Report-stack CPU samples | 1,019 | 436 |
| Irrigation inclusive CPU share | 73.9% | **14.5%** |
| Terroir inclusive CPU share | 5.6% | 16.7% |
| Infrastructure CPU share | 4.9% | 14.7% |
| Condition evaluator CPU share | 6.2% | 4.8% |
| Total report allocation sample weight | 1,267,875,656 bytes | 337,715,448 bytes |
| Irrigation inclusive allocation share | 79.3% | 22.4% |

- Profile attribution filters stacks containing `EstateDeskReport.open`, exported with stack depth 128. Inclusive percentages overlap; allocation weight is statistical rather than exact bytes. The remaining vineyard block/age work, terroir and infrastructure occupy larger relative shares after irrigation falls. Their relative increases do not establish new regressions. Gate H has allocation weight similar to G but much lower irrigation CPU and elapsed time, supporting the enumeration-focused decision. No additional hotspot was optimised.

### Acceptance validation and release boundary

- **Retained and accepted for Gate H:** exact payload equivalence, unchanged observed chunk counts, substantial latency/CPU reduction and all mandatory validation passed. Temporary profiling source/hook has been removed and preserved externally. The optimisation, regression test and this record are committed together as `Compute plot irrigation influence once per height`; nothing is pushed.

| Validation | Result | Preserved evidence under `/tmp/vintner-gate-h-iDP6vEsj` |
| --- | --- | --- |
| Plot-focused group, including new equivalence test | 10/10 passed | `plots.xml`, `plots.log` |
| Estate-focused group | 11/11 passed | `estate.xml`, `estate.log` |
| Exact ownership isolation | 1/1 passed | `ownership.xml`, `ownership.log` |
| Complete standard suite | 167/167 passed | `full-standard.xml`, `full-standard.log` |
| Complete Serene Seasons suite | 167/167 passed | `full-serene.xml`, `full-serene.log` |
| Clean build | Passed; audit ran through `check` | `build.log` |
| Explicit release audit | Passed: 1,567 JSON files, 169 recipes, 159 public wood-family blocks, 24 wood-preserving grapevine states | `audit.log` |
| Final hygiene | `git diff --check` passed; ignored audit bytecode removed; only five intended source/test/documentation files included | Final Git review |

- Existing ownership and Winemaker test logic remains unchanged. No payload format, persistence, cooldown, global cache or version change is included; development version remains `1.3.1`.
- This evidence is from one machine, ten timed calls per scenario and synthetic rain-fed fixtures; it does not measure whole-tick latency, worst possible water density, map/cellar inventory or multiplayer concurrency. Reporting remains synchronous and retriggerable. The remaining on-demand cost belongs in final release-risk assessment, not another dedicated optimisation phase without a newly demonstrated reason.

## Gate I — Facility and reputation persistence verification

### Baseline and production model

- Started on `feat/1.4.0-vineyard-management`, HEAD `1f374e1`, remote-only/local-only `0 8`, clean tree, nothing staged, no Python bytecode, development version `1.3.1`, and passing `git diff --check`. Gate G/H commits were present. No performance work or production changes were made.
- `EstateInfrastructureReport.survey` scans a 33×17×33 box around the report origin. It derives four facility bits: workshop (two aging barrels on stands), controlled cellar (two stations rated IDEAL), warehouse (four racks/crates/collection fixtures), and tasting room (a tasting cabinet plus archive). Current readiness is not saved; removing required blocks changes the next survey.
- `EstateDeskReport.open` finds the requesting owner's estate, imports existing ledger evidence through `syncFromLedger`, surveys infrastructure, calls `recordInfrastructure`, and authors the payload. Recognition itself creates no ledger event. Opening the report intentionally mutates reputation when a new facility type is first recognized.
- `EstateReputationProfile.withFacilities` ORs the observed mask into historical evidence. `score` adds ten points per distinct bit, not per building or recognition. `EstateReputationSavedData.storeIfChanged` stores and marks dirty only changed records. Its public `TYPE` codec persists complete profiles, including `facility_mask`, in overworld saved data keyed by owner UUID. Ledger synchronization preserves the mask.
- This agrees with `PHASE_7D_PHYSICAL_ESTATE_UPGRADES_ACCEPTANCE.md` facility behavior and `PHASE_7E_ESTATE_REPUTATION_ACCEPTANCE.md` manual steps 6–7: newly recognized facilities earn persistent points; removal changes Ready status but retains earned reputation. Rebuilding the same type therefore cannot earn another bit. No removal/rebuild design ambiguity was found.

### Representative test and evidence

- Added `estateFacilityReputationPersistsAcrossReloadAndRebuild`. Four real wine crates form a warehouse in a cleared survey region offset from neighboring GameTests. This avoids cellar geometry/weather setup; all four facilities share the same mask/persistence machinery.
- Uses existing public packet-listener capture and real `EstateDeskReport.open`, nine openings total. Asserts exact outgoing facility-ready components and reputation components, real survey counts/mask, and full reputation/profile/plot/ledger equality where appropriate. No direct reputation award, synthetic infrastructure record, ledger insertion or production visibility seam is used.
- Initial recognition: score **5 → 15**, mask **0 → WAREHOUSE_MASK**, reputation marked dirty. Repeated openings retain **15** and do not mark the unchanged store dirty.
- Save/reload: writes the actual earned reputation store with Minecraft `SavedDataStorage` into a temporary directory, closes it, reads it through a new storage instance, verifies a distinct reconstructed object with every reputation field equal, and installs it through the public saved-data API before reopening the desk. Real disk encoding/decoding and post-load production report behavior are exercised. This is not a whole-server restart or a facility-chunk reload; world blocks remain present during this step.
- Removal: four crates become three, current warehouse readiness disappears and the live storage count changes to three. Earned score/mask remain unchanged with no ledger event or additional dirty mark. Restoration returns count/readiness to four/Ready, retaining the same historical reputation without another award.
- Chunk unload/reload was deliberately not exercised. Infrastructure survey directly calls `getBlockState` in its local box, with no loaded-area guard or unavailable-facility representation. Unlike plot reporting, it does not establish a no-force-load contract. This gate makes no claim that facility reporting avoids loading absent chunks; chunk/server restart behavior remains outside this focused persistence round trip.
- Evidence directory: `/tmp/vintner-gate-i-pXwOGe`. Each required run preserves its own XML, full launcher log, summary JSON and run/world directory. The actual serialized stores are also preserved in `facility-saved-data` and `estate-saved-data`.

| Required validation | Result | Evidence |
| --- | --- | --- |
| Exact facility persistence test | 1/1 passed | `facility.xml`, `facility.log` |
| Estate-focused group, including the new test | 12/12 passed | `estate.xml`, `estate.log` |
| Exact ownership isolation test | 1/1 passed | `ownership.xml`, `ownership.log` |
| `git diff --check` | Passed | Final review |
| Repository hygiene | Only test/documentation changes; no bytecode; version 1.3.1 | Final review |

### Decision and limitations

- **PASS — intended behavior verified.** All required focused validation passed; no production defect or unintended report mutation was demonstrated. Only GameTest coverage and this document changed, committed together as `Verify estate facility reputation persistence`. Nothing was pushed. Full standard/Serene suites, clean build and audit were not rerun for this test/documentation-only gate.
- This verifies historical facility reputation through the shared persistence path, not every facility's detection geometry, aging bonuses, whole-server restart, chunk unloading, or manual client presentation. Existing Gate D ownership assertions remain unchanged. Gate H performance evidence and release-audit counts were not remeasured.

## Remaining coverage gaps

- Remaining synchronous maximum-estate reporting cost is a reduced release concern after Gate H; final release-risk assessment remains pending.
- Manual unloaded-plot desk presentation, UI/scrolling QA, and broader release visual/in-game acceptance.
- Whole-server/chunk lifecycle and manual facility presentation beyond Gate I's reputation disk round trip.

## Next integration gate

**Smallest next integration task:** final 1.4.0 release-risk assessment using Gate H performance and Gate I persistence evidence, with manual desk presentation/scrolling and multiplayer/in-game acceptance still pending. Dedicated performance optimisation remains closed unless a new demonstrated reason appears.
