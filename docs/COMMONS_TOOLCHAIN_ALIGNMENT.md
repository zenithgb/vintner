# Phase 6P — Commons toolchain alignment prerequisite

The first sections preserve the initial Phase 6P evidence. The Phase 6P2
continuation below records the authorized packaging cleanup and final runtime
verification. **Phase 6P2 PASS; Phase 6P complete.** It supersedes the initial
PARTIAL decision. Phase 6A is technically unblocked but has not begun.

## Initial Phase 6P scope and baseline

Vintner started clean at `d008358` on `feat/1.4.0-vineyard-management`,
with origin `https://github.com/zenithgb/vintner.git` and comparison `0 9`.
Development version remains `1.3.1`.

Commons was a read-only reference at
`/Users/zachariaheverson/Developer/Minecraft/zenithgb-commons`, clean `main`,
HEAD `be3ae97b4db6a05c2718daec21186bbbb0e213f6`, with no remotes.
Both its source and existing `zenithgb-commons-0.1.0-dev.jar` metadata require
Fabric API `>=0.158.0+26.2`. Commons was neither modified nor rebuilt.

Only `gradle.properties` changes behavior: Fabric API moves from
`0.155.2+26.2` to `0.158.0+26.2`. This document is the other changed file.
No production Java, tests, metadata, generated resources or gameplay changed.
Commons is not a dependency yet; there is no module registration.
Almanac and Vintage Archives remain untouched.

| Tool | Before | After | Commons reference |
| --- | --- | --- | --- |
| Minecraft | 26.2 | 26.2 | 26.2 |
| Java target | 25 | 25 | 25 |
| Fabric Loader | 0.19.3 | 0.19.3 | 0.19.3 |
| Fabric API | 0.155.2+26.2 | 0.158.0+26.2 | >=0.158.0+26.2 |
| Loom declaration | 1.17-SNAPSHOT | 1.17-SNAPSHOT | 1.17.20 |
| Resolved Loom | 1.17.20 | 1.17.20 | 1.17.20 |
| Gradle wrapper | 9.5.1 | 9.5.1 | 9.5.1 |

`buildEnvironment` resolves the snapshot plugin marker to
`net.fabricmc:fabric-loom:1.17.20`; build/runtime output also reports
`Fabric Loom: 1.17.20`. No instability justified changing the declaration.
The local JVM is Homebrew OpenJDK 25.0.3 on macOS 26.6.2/aarch64.

## Compatibility inspection

Inspected Fabric uses in attachments, creative tabs, item/block/entity
registration, POIs, loot, gamerules, player interactions, server lifecycle and
entity callbacks, resource reload, payload registration/sending/receiving,
GameTests, client renderer registration and datagen. No custom command or
client tick callback registration was found in the inspected source.

Compared the two installed Fabric API artifacts' component versions. Among
changed components are attachments, interactions, items, rendering and datagen;
networking, lifecycle and GameTest component versions are unchanged. All
current Vintner call sites compile without adjustment. Runtime suites below
exercise the existing behavior; no preemptive API rewrites were made.

The project uses common/client source sets and GameTests in `src/main/java`.
There is no `src/test`; Gradle's test compilation and unit test tasks are
`NO-SOURCE`. Datagen has an empty provider entrypoint; the established data
acceptance check is `auditReleaseAssets`, wired into `check`. No unnecessary
resource generation was performed.

## Validation evidence

Evidence: `/tmp/vintner-phase6p-gHNmgX`. Full suite XML, logs and worlds are
preserved separately before the clean build. Runtime smoke checks use external
disposable directories via an uncommitted external Gradle init script; existing
user worlds and repository runtime configuration are not modified.

| Command/check | Result |
| --- | --- |
| `./gradlew buildEnvironment tasks --all --console plain` | Passed; plugin resolution/tasks recorded |
| `./gradlew compileJava compileClientJava compileTestJava test --warning-mode all --console plain` | Passed; common/client and in-main GameTests compile; unit tests NO-SOURCE |
| `./gradlew runGameTest` | 168 passed, 0 failed, 0 skipped |
| `./gradlew runGameTest -PsereneSeasonsTest` | 168 passed, 0 failed, 0 skipped |
| `./gradlew clean build --warning-mode all --console plain` | Passed; all eight actionable tasks executed |
| Release audit through clean build/check | Passed: 1,567 JSON files, 169 recipes, 159 public wood-family blocks, 24 wood-preserving grapevine states |
| Dedicated `runReleaseServer` with external run-directory override | Passed: readiness, reload, post-reload `list`, clean `stop`, exit 0 |
| Client `runClient` with external run-directory override | Common/client startup reaches rendering; JFR confirms AccessibilityOnboardingScreen; title/game state not verified |

Serene Seasons uses the existing Modrinth artifact `13sXhUkI`, resolved version
`26.1.2.0.4`, plus GlitchCore `SDUCBYRU`, resolved `26.2.0.0.0`.
Both runs resolve Fabric API `0.158.0+26.2` without Commons.
Standard coverage includes 12 estate, 10 plot and seven village-named tests,
plus existing wine lifecycle, cultivation, harvest, scoring and consumption
tests. These are subsets of the 168 tests, not extra executions.

The dedicated server logs Vintner initialization and readiness, reloads 1,754
recipes/1,871 advancements, executes Vintner's post-reload village callback,
responds to `list`, and saves/stops normally. The fixture binds localhost in
offline mode; its authentication warnings are expected test configuration.

Client evidence is logs and a 20-second JFR execution sample, not visual QA.
Vintner common initialization, client resource reload and texture atlas creation
complete. Render-thread samples show `AccessibilityOnboardingScreen` rendering
normally. Fabric's client initialization reached subsequent resource/render
startup without entrypoint errors; VintnerClient itself has no success log.
Its renderer/network callback registration was source-inspected and compiled,
but exercising those callbacks through gameplay was not part of this smoke.
The UI tool did not list the Java game window and rejected `getApp("java")`;
therefore the onboarding screen could not be advanced to a title/game state.
After observation, SIGTERM stopped only the identified disposable client PID;
Gradle exited 1 because of controlled process termination, not an initialization
crash. This is an incomplete client acceptance check, not a PASS.

## Warnings and packaging boundary

Compilation reports the same 59 mock-player removal warnings and general
client deprecation notice as the preserved earlier build, not a newly observed
API incompatibility. Serene Seasons/GlitchCore emit development refmap warnings,
first-run GameTest properties/EULA messages and untranslated-tag warnings also
present in prior preserved compatibility evidence. No test failed.
Client warnings include the absent `build/resources/client` directory
(`processClientResources` is NO-SOURCE), vanilla shader attribute-link notices,
and native macOS Java messages. They did not prevent onboarding rendering;
their baseline equivalence was not independently tested. No new gameplay
failure is inferred from them.

Final artifact: `build/libs/vintner-1.3.1.jar`. Metadata remains Vintner 1.3.1,
Minecraft ~26.2, Loader >=0.19.3, Java >=25 and `fabric-api: "*"` (the existing
runtime metadata does not encode an API minimum). Gradle/runtime resolve the
aligned API. No Commons classes/dependency, nested JARs or temporary QA code
were added. Almanac/Vintage Archive classes/assets remain. Entry-name sets and
all asset/data bytes match the preserved pre-change artifact.

**Pre-existing package-check failure:** the production JAR includes
`VintnerGameTests.class`, three anonymous capture classes and `DeskOwnerState`,
plus the `fabric-gametest` metadata entrypoint. The same five classes and
entrypoint existed before alignment, because GameTests reside in main sources.
This violates Phase 6P's explicit no-test-classes packaging requirement. It is
not an API regression. Removing them safely from the production artifact while
preserving dev GameTests requires a separately reviewed packaging change; this
alignment does not silently broaden into that work.

## Initial Phase 6P gate status

**PARTIAL:** compilation, both 168-test suites, clean build/audit and dedicated
server readiness/reload/stop passed. The API mismatch is removed locally, but
the pre-existing package requirement and title/game-state client verification
remain unmet. No PASS commit was created; the two intended files remain
unstaged for review. `git diff --check` passed, audit bytecode was removed, no
generated-resource changes occurred, and Commons remains clean and unchanged.

Before accepting Phase 6P, roadmap review should authorize a minimal production
JAR exclusion for existing GameTest classes/entrypoint while preserving dev test
registration, and complete the client title/game-state check in a controllable
UI environment. The API/toolchain prerequisite for Phase 6A is aligned locally,
but the acceptance gate is not fully cleared or committed.
Do not begin Phase 6A. Commons wiring, module registration, knowledge content,
Almanac migration, Tilth/Reeve links and gameplay interoperability remain
unimplemented and untested.

## Phase 6P2 — Supported GameTest separation

Started again at `d008358` with only the validated Fabric API change and this
untracked document present, nothing staged and no unrelated work. Commons
remained clean at the accepted baseline. Neither version nor other toolchain
declarations changed; Fabric API is still `0.158.0+26.2`, Loom resolves 1.17.20.

The original five class files all came from one source file in `src/main/java`:
`VintnerGameTests`, `$1`, `$2`, `$3` and `$DeskOwnerState`. They contain tests and
test helpers only, not production initialization. Other production sources do
not reference them. Their separate package already accesses public production
APIs; moving source sets needs no package-private visibility changes.

Inspected Loom 1.17.20's actual `GameTestSettings`, `FabricApiTesting` and
`FabricApiAbstractSourceSet` implementation. `fabricApi.configureTests` supports
creating a `gametest` source set, inheriting production dependencies/outputs and
registering a separate development mod. Production packaging is not required.

Chosen configuration:

- `configureTests { createSourceSet = true; modId = "vintner_gametest";
  enableGameTests = false; enableClientGameTests = false }` creates the supported
  source set without generating competing run tasks. The established
  `runGameTest` points at `gametest`, preserving its report location and separate
  standard/Serene directories. `check` additionally depends on `gametestClasses`.
- Moved the Java file unchanged to
  `src/gametest/java/com/zenith/vintner/test/VintnerGameTests.java`.
  Before/after SHA-256 is
  `609791a4b9931729cb9e3662ee786026bb151269cec26faeaf023f29f87b0a43`.
- Moved only the `fabric-gametest` entrypoint from production metadata into
  `src/gametest/resources/fabric.mod.json`, owned by test-only mod
  `vintner_gametest`. The production ID, version, main/client/datagen entrypoints,
  mixins and dependencies are unchanged. No custom classloader or JAR filtering
  is used.
- Fabric discovers the same 168 annotated methods through the test mod's
  entrypoint. Test identifiers now use `vintner_gametest:` because Fabric derives
  the namespace from the entrypoint provider. Focused filters must use, e.g.,
  `-Dfabric-api.gametest.filter=vintner_gametest:vintner_game_tests_*plot*`.
  Full `./gradlew runGameTest` and `-PsereneSeasonsTest` commands are unchanged.

### Final packaging validation

Evidence directory: `/tmp/vintner-phase6p2-DYZnbg`. The original artifact,
Loom inspection, unchanged-source checksum, separate suite XML/log/world copies,
build log and package comparison are preserved there.

| Post-change validation | Result |
| --- | --- |
| `./gradlew compileJava compileClientJava compileGametestJava --warning-mode all --console plain` | Passed |
| `./gradlew runGameTest` | 168/168 passed |
| `./gradlew runGameTest -PsereneSeasonsTest` | 168/168 passed |
| `./gradlew clean build --warning-mode all --console plain` | Passed; ten actionable tasks executed |
| Release audit through `check` | 1,567 JSON files, 169 recipes, 159 public wood-family blocks, 24 wood-preserving grapevine states; passed |
| Production JAR inspection | No GameTest classes/entrypoint, no test fixtures, no Commons or nested JARs |

The five test classes and their directory are the only removed JAR entries.
No entries were added. All retained gameplay class bytes and asset/data bytes
match the preserved pre-cleanup artifact. Almanac and Vintage Archive remain.
There are still no separate unit tests (`test` is NO-SOURCE). The 59 existing
mock-player deprecations are now confined to GameTest compilation. No test
semantics were changed or weakened.

### Final runtime verification

Runtime checks use only external disposable server/client directories and an
external Gradle init script; no runtime configuration or observer is shipped.
The production server reaches readiness, completes reload and responds to
`list`, without `vintner_gametest` on its mod list. It accepts the localhost
client, then saves and stops cleanly with exit 0. This demonstrates ordinary
runtime has no dependency on the removed GameTest implementation.

Client onboarding was handled only in the disposable profile's `options.txt`:
`onboardAccessibility:false`, `skipMultiplayerWarning:true`, `tutorialStep:none`.
These are existing Minecraft options inspected in 26.2 source, not production
code changes. The normal `--quickPlayMultiplayer 127.0.0.1:25568` argument entered
the isolated server directly. No real Minecraft profile was touched.

Vintner common initialization and resource/atlas loading completed. An external
read-only JDI probe, attached through a localhost-only temporary debug socket,
observed a live `ClientLevel`, `LocalPlayer`, and `Gui.screen == null` twice.
This is normal play state beyond onboarding, not merely a title screen or
loading screen. It also read Fabric's installed play receiver map and confirmed
`vintner:almanac_report` and `vintner:estate_desk`, directly evidencing the
Vintner client initializer's networking registration. The source's renderer
registration precedes these registrations and raised no initialization error.

Server logs confirm `VintnerDev joined the game` and successful grants of
Crimson Grapes, Vintner's Almanac and Oak Estate Management Desk. A 20-second
JFR trace confirms active GameRenderer/world rendering after join. Client
startup, play networking and content registration therefore passed; this does
not claim visual correctness or exercise the dynamic desk/Almanac screens.
Evidence is runtime logs, read-only JVM field inspection and sampled render
stacks, not direct visual inspection or UI automation.

After observation the server's normal `stop` completed. The disposable client
was terminated deliberately with SIGTERM: Java exit 143 and Gradle exit 1.
This is the sole nonzero execution in final runtime validation and is controlled
shutdown, not a crash. Read-only inspection also tried the common source JAR
for client Options before locating it in the client-only source JAR; that
unmatched-entry warning was an inspection correction, not a build failure.

Warnings remain bounded: existing compiler deprecations; optional integration
refmap/initial GameTest properties/EULA warnings; empty client resource path;
vanilla shader/native notices; localhost server offline-mode warnings. The
unauthenticated development client logged HTTP 401 fetching its profile signing
key, but successfully joined the offline localhost server. No Fabric API
initialization exception or gameplay regression was observed.

### Final change set and decision

- `gradle.properties`: retained the validated Fabric API alignment only.
- `build.gradle`: supported GameTest source-set setup, existing run configuration
  source selection, and GameTest compilation in `check`.
- `src/main/resources/fabric.mod.json`: removed only the test entrypoint.
- `src/main/java/.../test/VintnerGameTests.java` →
  `src/gametest/java/.../test/VintnerGameTests.java`: unchanged file relocation.
- `src/gametest/resources/fabric.mod.json`: development-only test registration.
- This report: initial and final evidence, limitations and focused-filter change.

`git diff --check` and staged diff review passed; audit-generated ignored
bytecode was removed. All temporary scripts, options, debug configuration and
JVM probes remain external evidence only and are absent from source/JAR/staging.
Commons stays clean and unchanged. No production gameplay source or data
changed. The complete prerequisite is committed as
`build: align toolchain for Zenithgb Commons`; nothing is pushed.

**Phase 6A — Vintner Required Dependency and Module Registration is technically
unblocked.** Phase 6A has not begun. Dedicated performance work, knowledge
content, Almanac migration, Vintage Archives migration, cross-mod behavior and
visual release acceptance remain outside this prerequisite.
