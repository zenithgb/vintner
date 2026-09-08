# Commons Integration E — Full Vintner acceptance

Phase 6E **PASS**. Recommend marking Phase 6 — Vintner Migration / Integration
**COMPLETE**. This is acceptance of the current integration, not release approval
or authorization to begin Phase 7. No production or test source changed.

## Verified baselines

| Repository | Branch | Starting HEAD | State |
| --- | --- | --- | --- |
| Vintner | `feat/1.4.0-vineyard-management` | `17502de49cb445372b75859f13a8a01978d26ad0` | Clean; version `1.3.1`; origin 0 behind / 14 ahead |
| Commons | `main` | `be3ae97b4db6a05c2718daec21186bbbb0e213f6` | Clean; no remotes |
| Tilth | `phase/6d2-reciprocal-knowledge-link` | `bf5de8fb8b9590614972c0cfbb6b6b1d480b3dcd` | Clean; no remotes |

Vintner and Commons are under `/Users/zachariaheverson/Developer/Minecraft/`.
Tilth is at `/Users/zachariaheverson/.codex/.chatgpt-projects/g-p-6a2f94148e3081918bf79d2ea65fd1c3/zenithgb-farming`.
Vintner's unchanged origin is `https://github.com/zenithgb/vintner.git`.
No unrelated or staged changes existed at the start. Commons and Tilth remain
source-clean at these commits. Nothing is pushed or version-bumped.

Commons was freshly clean-built and tested before Vintner acceptance. Its separate
artifact is `zenithgb-commons/build/libs/zenithgb-commons-0.1.0-dev.jar`, SHA-256:

`10a6e01ddb6ed2d9b57c93da40ed3db6c1fa991fa6e088256adc2ec267008ef4`

All acceptance builds/runtimes use that artifact through `-PcommonsJar`.
Tilth was rebuilt without source edits. Its external optional artifact,
`zenithgb-farming-0.0.1-dev.jar`, has SHA-256:

`6a6a2543a92db5d89263c60daa88d8ca94529fcccae99e2d5e12a29acc37077a`

## Accepted architecture and ownership

Production Fabric metadata requires `zenithgb_library:0.1.0-dev`. The explicit
external JAR dependency supplies development compilation/runtime without nesting,
shading or publication. Vintner does not require Tilth, Reeve or another specialist.
The common initializer calls `VintnerCommons.initialize()` once, registering
`vintner:vintner`, display name `Vintner`, version read from Fabric runtime metadata
(`1.3.1`). No reload registration, reset or duplicate suppression exists.

Commons owns shared reference knowledge loading, validation, assembly,
synchronization and presentation. Vintner owns wine gameplay and specialist
operational interfaces. The only production Commons Java imports are the module
descriptor and registration API. Vintner has no client-local Commons resource
authority, live catalogue reads, presentation transport, access binding or direct
Commons screen construction. There is no gameplay interoperability with Tilth.

## Exact contribution

All categories and entries belong to `vintner:vintner`. The actual loader and
assembler tests validate exhaustive ownership, membership, order, titles,
conditions and related IDs, without resetting global Commons registries.

| Category, in order | Entries, in order |
| --- | --- |
| `vintner:getting_started` — Getting Started (10) | `vintner:introduction` |
| `vintner:viticulture` — Viticulture (20) | `vintner:grapevines`, `vintner:vineyard_sites` |
| `vintner:winemaking` — Winemaking (30) | `vintner:pressing_and_must`, `vintner:fermentation` |
| `vintner:cellaring_quality` — Cellaring & Quality (40) | `vintner:ageing_and_treatments`, `vintner:cellars`, `vintner:wine_scoring`, `vintner:vintages_and_archives` |

The only cross-mod references are reciprocal:

- `vintner:vineyard_sites` → `zenithgb_farming:soil/soil_health`.
- `zenithgb_farming:soil/soil_health` → `vintner:vineyard_sites`.

Vineyard Sites retains related order `[vintner:grapevines,
zenithgb_farming:soil/soil_health]`, including when Tilth is absent. No Compost
link, self/duplicate link, Reeve reference or resource condition was added.

## Automated acceptance

Separate evidence is preserved at `/tmp/vintner-phase6e-hfoVjs`. XML, launcher
logs and disposable GameTest/runtime worlds are retained independently.

| Gate | Passed | Failed | Skipped | Evidence |
| --- | ---: | ---: | ---: | --- |
| Commons fresh clean build/full suite | 117 | 0 | 0 | `commons-build.log`, `commons-xml/`, `commons.json` |
| Vintner full standard GameTests | 170 | 0 | 0 | `standard.log`, `standard.xml`, `standard-run/` |
| Vintner full Serene Seasons GameTests | 170 | 0 | 0 | `serene.log`, `serene.xml`, `serene-run/` |
| Tilth initial full suite, optional Vintner fixture not supplied | 123 | 0 | 1 | `tilth-test.log`, `tilth-xml/` |
| Tilth full suite with real Vintner artifact | 124 | 0 | 0 | `tilth-combined-test.log`, `tilth-combined-xml/` |

The initial Tilth skip was its explicitly property-gated
`realOptionalVintnerArtifactAssemblesReciprocalContribution`. Supplying the real
artifact via external test-JVM configuration enabled it; the complete 124-test
rerun passed. The final Vintner JAR is byte-identical to the one used in that
test. No assertion was changed. Vintner's module and complete contribution
checks are included in both 170-test suites, so no redundant isolated run was
needed.

The Vintner suites retain coverage of cultivation, grapevines, vineyards, plots,
estates, pressing, fermentation, ageing, cellars, scoring, vintages, consumption,
village roles/trades, persistence and specialist payload/report behavior.
Production gameplay Java is unchanged.

## Runtime A — Commons + Vintner only

A real dedicated server and connected client used disposable offline localhost
worlds, without Tilth. Both initialized normally. Server registry inspection
showed exactly `vintner:vintner` / Vintner / `1.3.1`. Server and client presentation
revision 1 contained exactly 1 module / 4 categories / 9 entries, with the expected
order, ownership, membership and structural optional related ID.

External JDI probes invoked existing game input/widget callbacks on the normal
client thread and read actual received state. They did not construct Commons
screens, snapshots, payloads or contribution resources. Normal Controls callbacks
bound the existing Commons keybinding to K; dispatch through that binding opened
the real synchronized screen. This is connected runtime/UI callback evidence,
not direct visual or physical keyboard inspection.

All nine articles were opened through their categories and their detail controls
scrolled. Vineyard Sites had no Soil Health control while Tilth was absent.
Its internal Grapevines link opened correctly and Back restored Vineyard Sites.
Back, Escape, Close and reopening worked.

Vanilla server `reload` advanced revision 1 → 2 with exact unchanged counts and
one module registration. An already-open screen retained its immutable old
snapshot, as designed; close/reopen used the new received snapshot, checked by
object equality within the same debugger attachment. The absent related-control
check passed again. `list` reported the connected VintnerDev player.

Normal Disconnect cleared the client world/player and Commons state to revision
−1, 0 modules / 0 categories / 0 entries. Dispatching the existing binding outside
the world did not reopen stale knowledge. Normal Quit Game and server `stop`
both exited 0. The dedicated server had loaded neither Commons KnowledgeScreen
nor VintnerClient. See `absent/` logs and `server-result.json`.

## Specialist coexistence

Both connected scenarios retained `vintner:almanac_report` and `vintner:estate_desk`
receivers, verified from the actual client receiver registry (`receivers.log`).
The existing Almanac air-use API sent its real server-authored six-page guide
and opened Minecraft's BookViewScreen. Next Page, Previous Page and Escape worked.
An ordinary targeted use also opened its existing one-page block-feedback report.

Archive block/entity/resources and persistence remain Vintner-owned, including
sixteen copied batch snapshots and saved selected index. Estate Desk classes,
screen, renderer, payload and operational state remain unchanged. Existing
GameTests cover their report/persistence behavior. No Archive or Estate state was
moved into Commons. A separate manual Archive/Desk interaction tour was not run.

Two initial QA pagination attempts expected Next Page on the one-page targeted
block report and exited nonzero. Inspection confirmed the valid report text:
"The Almanac has no useful reading for this block." Looking upward still hit a
block in that disposable spawn. The final check exercised Minecraft's existing
air-item-use API instead, reaching Vintner's ordinary server guide path. No
production change, fake payload or screen substitution was made. These probe
setup errors are retained in `absent/flow-finish*.log`.

## Runtime B — Commons + Vintner + real Tilth

The actual separate Tilth artifact contributed its accepted reciprocal relation.
Server registry inspection showed:

- `vintner:vintner` / Vintner / `1.3.1`.
- `zenithgb_farming:tilth` / Tilth / `0.0.1-dev`.

Vintner contributed 4 categories / 9 entries; Tilth contributed 4 / 6. The actual
server and connected client presentation contained 2 / 8 / 15 at revision 1.
Combined category order was Vintner Getting Started, Tilth Getting Started,
Vintner Viticulture, Tilth Soil, Vintner Winemaking, Tilth Crops, Vintner
Cellaring & Quality, Tilth Crop Rotation. Vintner's relative ordering is unchanged.

The client initially attempted Quick Play just before server readiness and got
connection refused. Once ready, normal Direct Connection controls connected to
the same server. This was startup ordering, not dependency or content failure;
the original error remains in `present/client-launcher.log`.

Using the shared binding and actual synchronized UI:

1. Vineyard Sites showed exactly one active Soil Health control.
2. Opening it selected canonical `zenithgb_farming:soil/soil_health` with
   `From Tilth (zenithgb_farming:tilth)` context.
3. Soil Health showed exactly one active Vineyard Sites control.
4. Opening it selected canonical `vintner:vineyard_sites` with
   `From Vintner (vintner:vintner)` context.
5. Three alternating two-link cycles passed; all six Back traversals restored
   the expected article/module context without duplicate controls or history
   accumulation. The internal Grapevines link also remained valid.

Connected server reload advanced revision 1 → 2 and preserved 2 / 8 / 15 counts.
Reopening used the current received snapshot. Another three two-link cycles and
all six Back traversals passed, with exactly one control for each reciprocal
target. Thus both directions were exercised six times each across pre/post-reload
cycles. `list` succeeded. Almanac guide/receivers remained operational.

Disconnect cleared revision to −1 and all counts to zero. The existing binding
could not open stale knowledge without a world/player. Normal Quit Game and
server `stop` exited 0. Neither client-only class was loaded on the dedicated
server. See `present/flow-start.log`, `flow-reload.log`, `flow-finish.log`, individual
`ui-*.log` records, launcher logs and the preserved disposable world.

## Build, audit and resource stability

Vintner `clean build --warning-mode all` passed against the fresh Commons artifact.
Main, client and GameTest code compiled. `check` ran the release audit; a subsequent
explicit `auditReleaseAssets` also passed with unchanged counts:

- 1,580 JSON files.
- 169 recipes.
- 159 public wood-family blocks.
- 24 wood-preserving grapevine states.

No datagen execution was required by the established acceptance workflow.
`VintnerDataGenerator` registers no providers. All thirteen authored Commons
resources remained byte-identical to the accepted baseline and packaged copies;
the clean build's audit and repeated explicit audit were stable. No generated
resource churn occurred. This does not claim a separate nonempty datagen run.

## Final package inspection

`build/libs/vintner-1.3.1.jar` SHA-256:

`b81ff83a63fa62f3871f3603b9de024c3498e5331949dd6edbeb680bee856113`

Inspection after clean build confirmed required Commons `0.1.0-dev` metadata,
the registration helper, exactly four category/nine entry resources identical to
source, and the exact Soil Health related ID. No required Tilth/Reeve dependency,
embedded Commons/Tilth classes, nested Commons/Tilth JAR, GameTest implementation,
GameTest production entrypoint or temporary QA exists. Almanac, Archive and Desk
classes/resources remain. No unexpected package changes occurred; the artifact
checksum is identical to the accepted starting artifact.

Commons remains a separate artifact with no Vintner/Tilth specialist classes or
resources. Both dependency repositories remain unchanged. `package.json` records
the final checksums/check results.

## Content sanity review

All nine articles were reread and checked against the current relevant source:
Almanac use/guide/plot registration, vine/cutting/compost behavior, site evaluation,
press and fermentation capacities/batch handling, ageing vessels, cellar/storage
elapsed-time handling, quality/appraisal, and Archive snapshot persistence.
No factual correction was needed. No planned feature is claimed live; no live
reading is fabricated as static content. Market guidance remains an estimate,
not a guaranteed offer. Active cultivar wording is compatible with the active
roster and retained legacy values. Cosmetic wood remains distinct from functional
ageing treatment. Commons does not replace the specialist interfaces. The optional
soil link is conceptual reference navigation, not shared item/soil effects.

## Commands and evidence handling

Meaningful commands/checks, in addition to focused source reads:

- All repository `git status --short --branch`, `rev-parse HEAD`, remote checks,
  Vintner remote comparison, version/metadata/dependency inspection and diff checks.
- Commons `./gradlew clean build --warning-mode all`; preserve all 117 XML cases.
- Tilth `./gradlew jar test --rerun-tasks -PcommonsJar=...`, then full
  `test --rerun-tasks -I <external-init> -PcommonsJar=...` supplying test-JVM
  `tilth.vintnerJar` to the real artifact. Preserve both sets of XML.
- Vintner `./gradlew runGameTest -PcommonsJar=...`, then the same with
  `-PsereneSeasonsTest`; preserve each XML, log and run directory separately.
- Each scenario: `runReleaseServer` and `runClient` with external run-directory/
  debugger configuration and `-PcommonsJar`; combined scenario additionally
  supplied the real Tilth JAR through external `-PqaTilth` wiring.
- External Java/JDI callback/state probes: server module/presentation checks,
  Controls binding, shared-key dispatch, category/article/related controls,
  Back/Escape/Close, reload snapshots, item-use/Almanac pages, receiver inspection,
  normal Disconnect, outside-world binding guard and Quit Game.
- Vanilla server `reload`, `list`, disposable-fixture Almanac item placement,
  and `stop`. Both client/server pairs exited 0.
- Controlled external `missingCommonsProbe`: JavaExec's normal server classpath
  minus only Commons, invoking Fabric KnotServer headlessly. Expected process
  exit 1, before Vintner initialization, with exact message:

  `Mod 'Vintner' (vintner) 1.3.1 requires version 0.1.0-dev of zenithgb_library, which is missing!`

- Vintner `clean build --warning-mode all -PcommonsJar=...`, then
  `auditReleaseAssets -PcommonsJar=...`; ZIP metadata/class/resource/source-byte
  checks, resource hashes, final unstaged/staged `git diff --check`.

The missing-dependency nonzero exit is the required negative test. The two
pagination-probe errors and initial combined Quick Play refusal are described
above. No automated regression test, clean build, audit or final runtime
acceptance assertion failed.

Temporary external probe sources/classes, Python runners, Gradle init scripts
and command files were removed before staging. Logs, XML, summaries and disposable
worlds remain as evidence; no temporary QA was copied into source or JARs. Only
ignored audit-generated Python bytecode was removed from the repository. The
sole committed change is this acceptance document.

## Warnings, limits and deferred work

- Build: 59 existing mock-player deprecation/removal warnings; no compile failure.
- Tests: Tilth's initial explicit fixture skip was resolved by the full 124/124
  run. No final skipped/failing test remains in the acceptance matrix.
- GameTests: existing empty client-resource classpath notice and fresh
  `server.properties`/EULA setup messages; both full suites pass.
- Serene Seasons: development refmap and untranslated-tag notices; 170/170 pass.
- Server: deliberate localhost offline-mode warning; operational commands,
  reload and shutdown pass. No public/authenticated server acceptance is claimed.
- Client/networking: unauthenticated profile-key HTTP 401/Realms notices and
  native shader attribute warnings. Combined Quick Play initially preceded
  readiness; normal connection and actual synchronization then passed.
- Content: no errors or changes. Compost cross-link remains intentionally absent.
- Visual QA: actual synchronized screen/widget callbacks were exercised, not
  physical keyboard/mouse use, layout screenshots or final visual polish.
- Environment: disposable worlds only; no existing player world was modified.
- Architecture: the test-only package bridge remains coupled to the pinned
  Commons API. An open screen intentionally retains its immutable snapshot until
  reopened after reload. No gameplay state or ownership moved into Commons.

Manual Archive/Desk UI tours, authenticated multiplayer, a full three-mod gameplay
suite, gameplay interoperability/shared compost effects, search, icons,
player-specific conditions/discovery, Almanac retirement, final visual polish and
Reeve integration are not claimed. Existing standalone Vintner gameplay gates and
full Tilth tests passed; the combined acceptance targets knowledge integration.

Deferred: Phase 7 Reeve; Phase 8 interoperability only when justified; search,
icons, discovery, polish, optional future Almanac/Archive contextual links and
physical/menu Commons access. No downstream phase begins here.

All required current Phase 6 integration contracts have end-to-end acceptance
evidence. Recommend accepting Phase 6E and marking Phase 6 complete.
