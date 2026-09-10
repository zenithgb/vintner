# Commons 0.1.0 dependency migration — Phase 10B2

Validated 2026-09-10: **PASS**. One scoped local migration commit; no push,
release, version bump or downstream Phase 10B3 work.

Normal Vintner development requires **Commons 0.1.0**, as a separate external
Fabric dependency. Future API/schema/protocol migrations require explicit
coordinated validation. No arbitrary future version compatibility is implied.
Vintner remains version **1.3.1**.

## Verified baselines

- Vintner: `/Users/zachariaheverson/Developer/Minecraft/vintner`, branch
  `feat/1.4.0-vineyard-management`, starting clean at
  `041af024084580cacca064e8cd3a0f4be00ac427`. No staged or unrelated work;
  origin `https://github.com/zenithgb/vintner.git`, 0 behind / 15 ahead.
- Commons: `/Users/zachariaheverson/Developer/Minecraft/zenithgb-commons`, clean
  `main` at `21e1bd462f956abcd49ee15216bbd039d09512b5`.
- Tilth: `/Users/zachariaheverson/.codex/.chatgpt-projects/g-p-6a2f94148e3081918bf79d2ea65fd1c3/zenithgb-farming`,
  clean `phase/10b1-commons-0.1.0` at
  `42f7847ebfe26abcf3046ac9c047573411c8af7c`, no remotes.
- Toolchain unchanged: Minecraft 26.2; Java 25 (installed 25.0.3); Loader 0.19.3;
  Fabric API 0.158.0+26.2; Loom declaration 1.17-SNAPSHOT resolving to 1.17.20;
  Gradle 9.5.1.

The accepted Commons artifact was positively identified before editing:

`/Users/zachariaheverson/Developer/Minecraft/zenithgb-commons/build/libs/zenithgb-commons-0.1.0.jar`

SHA-256: `800b54e355602b9b1c7e12b73e553e9c55968396d07943182d66646a2772bae8`.
Packaged ID/version/license are `zenithgb_library` / `0.1.0` /
`LGPL-3.0-or-later`. The accepted encoder/decoder use protocol 2. No rebuild or
source modification was needed. Commons' prior 167-test acceptance is reference
evidence, not a newly executed suite in this phase.

## Migration and development command

Production metadata now requires exactly `"zenithgb_library": "0.1.0"`, replacing
`0.1.0-dev`. All other metadata requirements and the Vintner version are preserved.
The existing external `implementation files(...)` mechanism remains. No shading,
nesting, source copy, extra repository or dependency mechanism was introduced.

```sh
./gradlew \
  -PcommonsJar=/Users/zachariaheverson/Developer/Minecraft/zenithgb-commons/build/libs/zenithgb-commons-0.1.0.jar \
  clean build --warning-mode all --console plain
```

The previous guard checked only file existence. It now reads the supplied JAR's
`fabric.mod.json` and requires exact mod ID `zenithgb_library` and version `0.1.0`.
Missing files, unreadable artifacts and wrong identities/versions produce
actionable failures before compilation. This identity guard supplements the
verified release checksum; it does not claim to authenticate arbitrary JARs.

All original `0.1.0-dev` occurrences were classified. Active Gradle guidance,
production metadata and README commands changed. Historical Phase 6 integration,
Almanac audit and toolchain-alignment documents retain their original versions,
commands, checksums and rejection evidence. README points to this current record.

## Focused and gameplay validation

Evidence directory: `/tmp/vintner-10b2-Q2jdon`. Each GameTest run has separate
XML, launcher logs, summary and disposable run-directory evidence.

| Gate | Passed | Failed | Skipped |
| --- | ---: | ---: | ---: |
| Existing Commons registration/contribution GameTests | 2 | 0 | 0 |
| Full standard GameTests | 170 | 0 | 0 |
| Full Serene Seasons GameTests | 170 | 0 | 0 |

The existing identity test additionally asserts the actual loaded Commons
ID/version. Vintner's module remains `vintner:vintner` / Vintner, with version
read from actual Fabric metadata. Existing contribution assertions still prove
exact four categories, nine entries, membership/order, internal resolution and
safe structural optional references. No global registry reset or manual
registration was introduced.

The full suites preserve cultivation, vineyards, plots, estates, pressing,
fermentation, ageing, scoring, vintages, consumption, village roles/trades,
persistence and report/networking coverage. No gameplay assertion was weakened.

## Negative dependency acceptance

Controlled metadata-only JAR fixtures were external to all repositories:

| Supplied artifact | Result |
| --- | --- |
| `zenithgb_library` / `0.1.0-dev` | Gradle exit 1: `Expected zenithgb_library version 0.1.0; supplied zenithgb_library version 0.1.0-dev` |
| `wrong_library` / `0.1.0` | Gradle exit 1: `Expected zenithgb_library version 0.1.0; supplied wrong_library version 0.1.0` |
| `zenithgb_library` / `0.1.1` | Gradle exit 1: `Expected zenithgb_library version 0.1.0; supplied zenithgb_library version 0.1.1` |

A disposable Fabric KnotServer launch excluded only Commons from its normal
server classpath. It exited 1 before Vintner initialization, with:

`Mod 'Vintner' (vintner) 1.3.1 requires version 0.1.0 of zenithgb_library, which is missing!`

There is no fallback. The real accepted Commons artifact/repository was never
removed, renamed or modified. These four nonzero exits are expected negative
acceptance results, not failed migration gates.

## Vintner-only runtime

A real dedicated server and connected client ran Commons 0.1.0 + Vintner 1.3.1
without Tilth. The server reached readiness and registered exactly one
`vintner:vintner` / Vintner / 1.3.1 module. Server and client contained exactly
1 module / 4 categories / 9 entries at revision 1.

Commons' actual pause-menu control opened its synchronized screen. Search for
`vineyard` found Vineyard Sites with Vintner ownership. Its internal Grapevines
link and Back worked; no broken Soil Health control appeared without Tilth.
Normal Controls callbacks bound the existing configurable keybinding to K; that
binding opened the same received presentation.

Vanilla reload advanced revision 1 → 2, preserving the sole module and exact
counts. The open immutable screen retained its old snapshot; closing/reopening
used the current synchronized snapshot. Search/navigation checks passed again.
Server `list` remained operational. Dedicated-server inspection showed neither
Commons KnowledgeScreen nor VintnerClient loaded.

The existing Almanac air-use path opened its real server-authored six-page guide;
Next/Previous/Escape worked. Actual `vintner:almanac_report` and `vintner:estate_desk`
receivers remained registered. Normal disconnect cleared world/player and Commons
revision to −1 with 0/0/0 counts. The keybinding did not reopen stale knowledge
outside a world. Normal client Quit Game and server `stop` exited 0.

External JDI probes invoked existing input/widget/item-use callbacks and read
actual runtime state. No Commons screen, snapshot, packet or knowledge resource
was manufactured. This is connected runtime callback evidence, not a broad
physical-input or visual-polish review.

## Ownership boundary

Commons owns shared identity, reference infrastructure, presentation,
synchronization, access, search and localization. Vintner retains all gameplay,
Almanac, Archives, Estate Desk, persistence and specialist networking. Tilth is
optional; the unchanged relation is `vintner:vineyard_sites` →
`zenithgb_farming:soil/soil_health`. No specialist migration, gameplay
interoperability, article/schema/protocol edit or production Java change occurs.

## Combined Tilth regression

The accepted Tilth Phase 10B1 artifact was verified to require Commons 0.1.0;
its packaged contribution matches clean source. SHA-256:
`1320aff49b3c9b2aa4a151396e26c36d3990faf3803c3d7a26aeaaadba8580a2`.
It was supplied only through external runtime configuration, never added as a
required Vintner dependency.

The real combined server/client passed with exactly 2 modules / 8 categories /
15 entries: Vintner 4/9 and Tilth 4/6. Both pause access and the configurable
binding opened synchronized content. Search found Vineyard Sites. Its single
Soil Health control opened `zenithgb_farming:soil/soil_health`; the reciprocal
single control opened `vintner:vineyard_sites`. Back restored each prior target
and module context. Internal Grapevines navigation remained operational.

Vanilla reload advanced revision 1 → 2 with unchanged counts and registrations.
Reopening used the new snapshot; both link directions and Back passed again
without duplicate controls. Almanac navigation and both specialist receivers
remained available. Disconnect cleared Commons to revision −1 and 0/0/0; the
binding did not reopen stale content outside a world. Server `list` and clean
client/server shutdown passed, exit 0. The inspected client-only classes were
absent from the dedicated server. No Tilth source was modified or rebuilt.

## Clean build, audit and package comparison

Warnings-enabled clean build passed, compiling common, client and GameTest source
sets. The existing `check` audit and a separate explicit release audit passed:

- 1,580 JSON files.
- 169 recipes.
- 159 public wood-family blocks.
- 24 wood-preserving grapevine states.

Counts are unchanged. The existing 59 mock-player deprecation/removal warnings
remain; no migration-specific compiler warning or compilation error occurred.

Final `build/libs/vintner-1.3.1.jar` SHA-256:
`4e405acb170923e6ebe549c9d6c51581ba7776210ff2213a72f4cdbe48f868be`.

Comparison against the preserved accepted pre-migration artifact found identical
entry sets and **only `fabric.mod.json` changed**. All 179 production class files,
four category/nine article resources, gameplay assets/data and other JAR entries
are byte-identical. Knowledge resources also match current source exactly.
Production metadata requires Commons 0.1.0, retains Vintner 1.3.1 and introduces
no required Tilth/Reeve dependency. Commons/Tilth classes or nested JARs,
GameTests, test entrypoint and temporary QA are absent. Module helper, Almanac,
Vintage Archive and Estate Desk are retained. There are no unexpected contents.

## Commands and evidence

All Vintner commands use the full accepted artifact path shown above as
`-PcommonsJar`. Meaningful checks:

```sh
git status --short --branch
git rev-parse HEAD
git remote -v
git rev-list --left-right --count origin/feat/1.4.0-vineyard-management...HEAD
./gradlew --version
# Focused run additionally sets JAVA_TOOL_OPTIONS to:
# -Dfabric-api.gametest.filter=vintner_gametest:vintner_game_tests_commons*
./gradlew -PcommonsJar="$C" runGameTest
./gradlew -PcommonsJar="$C" runGameTest -PsereneSeasonsTest
./gradlew -PcommonsJar="$C" clean build --warning-mode all --console plain
./gradlew -PcommonsJar="$C" auditReleaseAssets
```

Here `C` denotes the full release JAR path, not an additional build mechanism.
The focused run preceded the two full suites. Separate evidence names are
`focused`, `standard`, `serene`, `build` and `audit` beneath the evidence directory.

Additional commands/checks: three negative-fixture `help --console plain` runs;
external `missingCommonsProbe` using KnotServer; both configurations' existing
`runReleaseServer`/`runClient` tasks with isolated run directories; Java/JDI
input/widget/state probes; normal Controls/binding/pause/search/link navigation;
server `reload`, fixture Almanac item placement, `list`, `stop`; client normal
Disconnect/Quit; package ZIP entry/content comparison and SHA-256 checks;
unstaged/staged `git diff --check` and complete scoped diff review.

The only nonzero validation commands were the four intentional dependency
rejections described above. No corrective test/UI invocation retries were needed.
Source searches with no matches are not treated as validation failures.

## Cleanup, integrity and limits

Temporary external Java/classes, Python runners, Gradle init scripts, command
files and negative fixture JARs were removed before staging. Only logs, XML,
summaries, the before-artifact and disposable worlds remain as evidence.
No temporary QA entered source or production packaging. Only ignored generated
Python bytecode was removed from `scripts`; none remains.

Commons remains clean at the accepted HEAD with the exact accepted artifact
checksum. Tilth remains clean at the accepted Phase 10B1 HEAD. Reeve was not
accessed or changed. No toolchain or Vintner version change occurred.

Warnings and evidence boundaries:

- Build: existing 59 GameTest mock-player API warnings, no new migration warning.
- Tests: all focused/standard/compatibility cases passed, zero skips.
- GameTests: existing empty client-resource classpath and fresh properties/EULA
  setup notices; no failed cases.
- Serene Seasons: existing development refmap and untranslated-tag notices.
- Server: deliberately offline, bound to localhost; no public-server claim.
- Client: existing profile-key HTTP 401/Realms and native shader notices; actual
  connection, synchronization, navigation and orderly exit passed.
- Environment: disposable worlds only. Physical input and broad visual-polish
  review were not repeated; real UI callbacks and received state were exercised.
- Architecture: no protocol handling or transport was added to Vintner. Its
  test-only Commons loader bridge remains coupled to the pinned dependency.

No claim is made for Vintner/Commons publication, authenticated public multiplayer,
exhaustive human gameplay, a full combined gameplay suite, or final gameplay
interoperability. Archive/Desk preservation is package/source, receiver and
existing regression evidence, not an exhaustive manual specialist tour.

Recommend accepting Phase 10B2. Phase 10B3 — Reeve migration may proceed only as
its separate roadmap-authorized task, including protected Storehouse handling.
It was not begun here.
