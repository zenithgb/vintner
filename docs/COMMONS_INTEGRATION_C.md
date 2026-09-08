# Commons Integration D1 — Optional Vintner → Tilth reference

This phase adds one knowledge-only relation. It does not complete Phase 6D or
introduce shared soil, compost, crop, trade or other gameplay behavior.

## Baselines

- Vintner: `feat/1.4.0-vineyard-management`, clean starting commit
  `19d221074b15ae12e8eaac778e4626908334a957`, version `1.3.1`.
- Commons: clean `main`, `be3ae97b4db6a05c2718daec21186bbbb0e213f6`, no remotes.
- Tilth: clean `phase/4c-tilth-knowledge`,
  `e6bf3134a9a74c3358687b8984f971ad1a6f107a`.
- Commons was rebuilt cleanly; artifact SHA-256
  `10a6e01ddb6ed2d9b57c93da40ed3db6c1fa991fa6e088256adc2ec267008ef4`.
- Real Tilth artifact `zenithgb-farming-0.0.1-dev.jar`, SHA-256
  `d14dd41961d8cb03b9834379fa4e17593345e44defc276bff8178d9621b9c1f1`.

Neither read-only repository was modified. Both use the same Commons artifact.
Temporary external QA wiring supplies Tilth only to the present scenario; no
Tilth dependency or imports are added to Vintner's build or production code.

## Editorial decision

The complete final `vintner:vineyard_sites` related array is:

```json
["vintner:grapevines", "zenithgb_farming:soil/soil_health"]
```

The Soil Health target is a real accepted Tilth resource owned by
`zenithgb_farming:tilth`. Its article explains Tilth farmland state, mature
barley/bean harvests and its current limitations. Vintner's `TerroirEvaluator`,
Soil Probe and Almanac survey evaluate vineyard soil, terrain and climate.
These are related reference topics, not the same runtime soil system. Commons
shows the target's Tilth module context. Existing article prose is unchanged.

Grapevines → Compost is **deferred**. Source inspection confirms Vintner compost
converts suitable ground to vineyard soil, including the trellis/vine tending
path. Tilth's accepted Compost article instead describes a command-supplied
item that improves vanilla farmland's separate soil record, does not work on
dirt and does not accelerate growth. An unqualified related link beside Vintner's
compost instructions could imply interchangeable items or shared vineyard
effects. No such interoperability exists or is introduced here.

The other eight Vintner articles and every category/member order are unchanged.
No Crop Rotation link is added: perennial vineyard guidance does not establish
an annual field-crop rotation relationship. Barley/Beans commodity links and
reciprocal Tilth edits are also outside this phase.

All five expected Tilth target IDs were verified at their canonical resource
paths. No IDs were guessed, renamed or synthesized. No self/duplicate references,
Reeve IDs or `requires_modules` conditions were added.

## Validation design

The existing contribution GameTest uses the actual Commons resource loader and
assembler with isolated test catalogues and real registered modules. Its Vintner
4/9 and ordering assertions remain exact. It now asserts the complete optional
related array, exhaustive internal resolution, no self/duplicate references,
and no conditions. The same test runs both without Tilth and with its actual
production JAR supplied externally. Expected assembled counts are 1/4/9 absent
and 2/8/15 present; the resolved target must retain its canonical Tilth ID, title
and owner. No parser/assembler is duplicated and no global registry is reset.

Client acceptance additionally exercises the actual synchronized Commons screen:
absent targets must not create controls; present targets must open Soil Health
with Tilth context and Back must restore Vineyard Sites. This runtime check
covers UI resolution rather than manually recreating Commons filtering in a test.

## Evidence

Local evidence directory: `/tmp/vintner-phase6d1-9LwJa8`. Separate logs, XML and
disposable worlds preserve each scenario without overwriting prior phases.

- Commons clean build/full suite: PASS, 117 tests.
- Tilth read-only module/content tests: PASS, 7 tests; production JAR built.
- Focused contribution GameTest, Tilth absent: PASS, 1/1, zero failures/skips.
- Same focused GameTest with real Tilth artifact: PASS, 1/1, zero failures/skips.
- Full standard GameTests: PASS, 170/170, zero failures/skips.
- Full Serene Seasons GameTests: PASS, 170/170, zero failures/skips.
- Clean build and explicit release audit: PASS. Counts remain 1,580 JSON files,
  169 recipes, 159 public wood-family blocks, 24 wood-preserving grapevine states.
- Final JAR: source-identical 4/9 knowledge resources; only the approved related
  array changes. Commons remains required and external; Tilth is not required or
  embedded. No Commons classes, GameTests, test entrypoint or QA are packaged.
  Almanac, Archive, Desk and module registration helper remain present.
- Connected runtime without Tilth: PASS. Details below.
- Connected runtime with Tilth: PASS. Details below.

### Runtime A: Tilth absent

The dedicated server and actual connected client used Commons + Vintner only.
Server registry inspection showed exactly `vintner:vintner`, name `Vintner`,
version `1.3.1`. The synchronized presentation contained 1 module / 4 categories /
9 entries at revision 1, retaining both structural Vineyard Sites related IDs.
The real article's scrolled related controls contained Grapevines but no Soil
Health button. Grapevines opened and Back restored Vineyard Sites.

Normal Controls callbacks assigned the existing Commons binding to K; the
existing keybinding dispatch opened the actual Commons screen from received
state. No Commons screen, packet or snapshot was constructed by QA. External
JDI probes invoked existing input/widget callbacks on the render thread and
read state; this is runtime callback evidence, not direct visual acceptance.

Vanilla reload advanced server/client revision 1 → 2 with exact unchanged 4/9
counts. The open screen retained its old snapshot; close/reopen used the current
snapshot by object identity. The absent-control and internal-link checks passed
again. The server's `list` command remained operational.

Normal Almanac item use opened its six-page book; Next/Previous and Escape worked.
The existing Almanac and Estate Desk receivers were present. Normal Disconnect
cleared level/player and Commons to revision −1 with counts 0/0/0. Normal Quit
Game and server `stop` both exited 0. No Tilth dependency refusal occurred.
Evidence is under `absent/`, including 43 `ui-*.log` files, both launcher logs,
`receivers.log`, server result and the disposable world.

### Runtime B: real Tilth present

The same Commons artifact ran with the actual Vintner and Tilth production
resources/artifact. Exactly two registered modules were observed:
`vintner:vintner` / Vintner / `1.3.1` and `zenithgb_farming:tilth` / Tilth /
`0.0.1-dev`. Vintner retained 4 categories / 9 entries; Tilth contributed 4 / 6.
The actual flattened presentation contained 2 modules / 8 categories / 15 entries.
Commons interleaves categories by order, then ID:

1. `vintner:getting_started`
2. `zenithgb_farming:getting_started`
3. `vintner:viticulture`
4. `zenithgb_farming:soil`
5. `vintner:winemaking`
6. `zenithgb_farming:crops`
7. `vintner:cellaring_quality`
8. `zenithgb_farming:crop_rotation`

Vintner's relative category/member order is unchanged. Using the existing
Commons binding after normal Controls remapping, Vineyard Sites showed exactly
one active, visible Soil Health related control. Activating it produced:

`Zenithgb Commons, Soil Health, From Tilth (zenithgb_farming:tilth); Entry zenithgb_farming:soil/soil_health`

Back restored `vintner:vineyard_sites` with Vintner context. The existing
Grapevines link and Back also worked. Vanilla reload produced revision 2 and
unchanged 2/8/15 counts, without duplicate registration or controls. Closing and
reopening used the new snapshot by identity; the cross-module and internal
navigation checks passed again. Numeric debugger IDs across attachments are
not treated as identity evidence; comparisons were within each attachment.

Almanac item use still opened its six-page book, Next/Previous and Escape worked,
and both `vintner:almanac_report` and `vintner:estate_desk` receivers remained
registered. Normal Disconnect cleared all Commons state to revision −1 and
0/0/0; client Quit Game and server `stop` both exited 0. No specialist state
migrated and no Tilth gameplay was invoked to make the link resolve.

Evidence is under `present/`: 47 `ui-*.log` files, launcher/runtime logs,
`receivers.log`, server result and the disposable world. `ui-017-click.log` and
`ui-031-click.log` capture the real cross-module opening before/after reload;
`ui-018-click.log` and `ui-032-click.log` capture restoration of Vintner context.

## Commands and final checks

All Vintner/Tilth Gradle invocations supplied the fresh Commons artifact through
the existing explicit `-PcommonsJar` property. Meaningful commands/checks:

- Repository status, branch/HEAD, remotes, version, metadata and source inspection.
- Commons `./gradlew clean build`: 117 passed, zero failed/skipped.
- Tilth `./gradlew jar test --tests 'com.zenithgb.library.knowledge.TilthKnowledgeContributionTest' --tests 'com.zenithgb.farming.TilthCommonsTest'`: 7 passed, zero failed/skipped. Its 4/6 packaged resources were compared to source; all expected target paths were verified.
- Vintner focused filter `vintner_gametest:vintner_game_tests_commons_knowledge_contribution_matches_approved_structure`, once absent and once with a temporary external init script adding the real Tilth JAR.
- Vintner full `runGameTest`, then `runGameTest -PsereneSeasonsTest`, then `clean build` and `auditReleaseAssets`.
- Separate `runReleaseServer` / `runClient` invocations per scenario, external JDWP callback/state probes, normal Controls/binding/navigation, vanilla `reload`, `list`, normal Disconnect/Quit and `stop`.
- ZIP metadata/class/resource inspection and source-byte equality; production
  Java/build/metadata diff checks; unstaged and staged `git diff --check`.

No meaningful build, test or runtime QA command failed in this phase. All
temporary probe programs, init wiring and command files were removed before
staging; only logs/reports/disposable-world evidence remain outside the repository.
Ignored audit-generated Python bytecode was removed. No generated-resource churn.

Vintner JAR SHA-256:
`b81ff83a63fa62f3871f3603b9de024c3498e5331949dd6edbeb680bee856113`.
Production metadata still requires Commons `0.1.0-dev`, with no Tilth dependency.
No production Java, build or metadata file changed. The only production edit is
the one related-array addition; the existing article body is byte-unchanged.

## Warnings and limits

- Existing GameTest mock-player APIs emit 59 deprecation/removal warnings.
- Development classpath notices and fresh GameTest properties/EULA setup
  messages remain. Serene Seasons/GlitchCore development refmap notices and
  untranslated-tag warnings did not fail compatibility tests.
- Disposable localhost servers deliberately used offline mode. Clients logged
  unauthenticated profile-key HTTP 401 and native shader warnings, while connected
  operation and normal exit passed.
- Screen/widget/state callbacks establish real synchronized UI behavior, not
  direct visual/physical-input polish or authenticated public-server acceptance.
- The test-only bridge is coupled to the pinned Commons loader API. Presence
  detection exists only in tests, never Vintner production code.
- Full Tilth gameplay tests and a full combined-mod GameTest suite were not run;
  targeted real-content tests and both connected scenarios cover this scope.
- Compost links remain deferred specifically to avoid implying shared item effects.

All required Phase 6D1 checks passed. Recommend accepting Phase 6D1 only.

## Boundaries

Commons remains required; Tilth remains external and optional. Only the JSON
related ID establishes the relationship. Vintner's Almanac, Vintage Archives,
Estate Desk, gameplay, saves, Java and networking remain unchanged. No article
is hidden merely because its optional related target is absent.

Phase 6D2 reciprocal links belong to a separate Tilth-side task. Later articles,
Almanac transition, Archive links, gameplay contracts, Reeve, search, icons,
discovery and visual polish remain deferred.
