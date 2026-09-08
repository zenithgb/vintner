# Commons Integration A — required dependency and module registration

Phase 6A integrates Vintner as a Commons consumer without contributing knowledge
or moving gameplay. Phase 6B and the rest of Phase 6 are not implemented here.

## Baselines and artifact

Verified on 2026-09-08 before editing:

- Vintner: clean `feat/1.4.0-vineyard-management` at
  `2e7163347b58de60fb775300116ded4f857fed55`, ten commits ahead of
  `origin/feat/1.4.0-vineyard-management`; origin remains
  `https://github.com/zenithgb/vintner.git`.
- Commons: clean `main` at `be3ae97b4db6a05c2718daec21186bbbb0e213f6`, no remotes.
- Minecraft 26.2, Java 25, Loader 0.19.3, Fabric API 0.158.0+26.2,
  Gradle 9.5.1; Vintner's existing Loom 1.17-SNAPSHOT resolves to 1.17.20,
  matching Commons. No toolchain changes.
- Vintner remains **1.3.1**. Commons remains **0.1.0-dev**.

Commons was freshly rebuilt with `./gradlew clean build`. Its full suite passed
117/117 with no failures, errors or skips. No Commons source or tests changed.
The separate artifact used throughout validation is:

`/Users/zachariaheverson/Developer/Minecraft/zenithgb-commons/build/libs/zenithgb-commons-0.1.0-dev.jar`

SHA-256:
`10a6e01ddb6ed2d9b57c93da40ed3db6c1fa991fa6e088256adc2ec267008ef4`.
Its Fabric metadata identifies `zenithgb_library` version `0.1.0-dev` and requires
Fabric API `>=0.158.0+26.2`.

## Consumer contract

Every Vintner Gradle invocation now requires an explicit existing `commonsJar`
file. `implementation files(...)` supplies the external Fabric mod to compilation
and development runtime under the existing Loom 26.2 configuration. There is no
`include`, shading, copying, nested artifact, new repository or publication of
Commons. Missing/invalid paths fail configuration with an actionable property
message. Production Fabric metadata independently requires exactly
`"zenithgb_library": "0.1.0-dev"`; future versions are not promised.

Example (substitute the location of the freshly built artifact):

```sh
./gradlew -PcommonsJar=/absolute/path/to/zenithgb-commons-0.1.0-dev.jar clean build
./gradlew -PcommonsJar=/absolute/path/to/zenithgb-commons-0.1.0-dev.jar runGameTest
./gradlew -PcommonsJar=/absolute/path/to/zenithgb-commons-0.1.0-dev.jar runGameTest -PsereneSeasonsTest
```

`Vintner.onInitialize` calls package-private `VintnerCommons.initialize` once.
The helper registers a `ZenithgbModule` directly in Commons' `ModuleRegistry`:

| Field | Value |
| --- | --- |
| ID | `vintner:vintner` |
| Display name | `Vintner` |
| Version | FabricLoader's actual Vintner runtime metadata, currently `1.3.1` |

No hard-coded version, client dependency, duplicate suppression or registry reset
is added. Registration precedes Vintner gameplay initialization and the first
server resource reload. Commons' initialization log may initially report zero
consumers when its initializer runs first; the later registry probes establish
the initialized consumer state.

Commons owns shared knowledge infrastructure and presentation; Vintner owns wine.
All cultivation, plots, estates, wine processing, scoring, vintages, consumption,
reputation, village roles, persistence and networking remain Vintner-owned.
Almanac content/opening/packets and Vintage Archives are unchanged. No Vintner
knowledge entries/categories, access path, keybinding, command, UI, packet or
cross-mod gameplay API is added. Tilth, Reeve and other specialist mods are neither
required nor included. A registered module with no articles is intentional.

## Automated and packaging evidence

Evidence is preserved outside the checkout in `/tmp/vintner-phase6a-JTV47f`.
Each GameTest invocation has a separate XML, full log, summary and copied run
directory. These temporary evidence paths are not runtime requirements.

| Check | Result |
| --- | --- |
| Commons clean build and full tests | PASS, 117/117 |
| Exact `commonsModuleUsesRuntimeIdentityExactlyOnce` | PASS, 1/1 |
| Full standard GameTests | PASS, 169/169 |
| Full Serene Seasons GameTests | PASS, 169/169 |
| Vintner clean build, common/client/GameTest compilation | PASS |
| Audit through `check`, plus explicit audit task | PASS |
| Audit counts | 1,567 JSON; 169 recipes; 159 public wood-family blocks; 24 wood-preserving grapevine states |

The new GameTest inspects the real initialized registry on a server: exactly one
Vintner namespace descriptor, exact ID/name and version equal to actual Fabric
metadata. It does not manually register/reset anything. All 168 pre-existing
tests/assertions remain unchanged. Bytecode inspection of the helper confirms
only common/server-safe references.

`build/libs/vintner-1.3.1.jar` inspection passed: required dependency and helper
present; no Commons classes/nested JAR, GameTest implementation/entrypoint,
temporary QA, knowledge resources or Tilth/Reeve dependency. Compared with the
preserved pre-Phase-6P2 artifact, every retained entry except `Vintner.class` and
`fabric.mod.json` is byte-identical. Almanac and Vintage Archives remain present.
The separate Commons JAR contains no Vintner classes or resources.

## Runtime evidence

Dedicated server and client use external, disposable directories under the
evidence directory, with the freshly built Commons JAR on their runtime classpath.
Neither Tilth nor Reeve is installed. Temporary Gradle init scripts and read-only
JDI probes were kept outside the checkout and removed after validation; no debug
command or production probe exists.

- Dedicated server reached readiness. Both mods initialized; contribution loading
  and vanilla `/reload` each loaded zero entries/categories and assembled zero
  categories without errors. `list` succeeded.
- `server-before-reload.txt` and `server-after-reload.txt` both report
  `moduleCount=1`, ID `vintner:vintner`, name `Vintner`, version `1.3.1`.
  Reload retains the expected descriptor values and does not duplicate registration.
- The localhost client connected as `VintnerDev`. Read-only JVM inspection found
  a `ClientLevel`, `LocalPlayer`, and no open screen: normal play. Both existing
  Vintner receivers (`almanac_report`, `estate_desk`) were registered. Server
  commands successfully gave grapes, the Almanac and an Estate Management Desk.
  This is runtime/registry evidence, not a manual gameplay or visual inspection.
- The client registry also contains exactly the one expected descriptor.
  Commons' own client initializer ran. Its client state received revision **2**
  with **one module descriptor and zero categories**, rather than merely retaining
  its initial unsynchronized empty state. The Commons access keybinding exists
  exactly once; Vintner adds no access path.
- Direct empty-state UI opening was not exercised: the available UI automation
  does not expose the Java game window. The synchronized zero-category payload
  and Commons binding are verified; visual layout/opening remains unverified.

The missing-dependency launch used an external JavaExec probe with the normal
server classpath minus exactly the Commons JAR, invoking Fabric's `KnotServer`
headlessly. Fabric refused launch with process exit **1** before Vintner
initialization:

```text
Mod 'Vintner' (vintner) 1.3.1 requires version 0.1.0-dev of zenithgb_library, which is missing!
```

`missing-headless.log` preserves the complete rejection. Omitting `commonsJar`
also fails Gradle configuration with the explicit path instruction
(`missing-property.log`). The real Commons artifact was never removed or edited.

Logs of QA harness corrections are preserved, not counted as product failures: changing
Loom's finalized run classpath was rejected, then a standalone probe needed the
direct Fabric entrypoint; its initial non-headless error dialog was closed before
the successful headless refusal check. The first client options file lacked a
data version, so vanilla's legacy keybinding migration rejected its modern key
syntax and showed onboarding. The disposable generated version-4903 options file
was corrected and the client relaunched successfully. No production fix was needed.

Existing mock-player deprecation warnings, development missing-client-resource
path/refmap notices and offline development-account authentication notices do not
prevent the passing gates. The final client did not repeat the options-load error.

The client remained in normal play across two live snapshots; a 20-second JFR
sample also captured `GameRenderer.renderLevel`. Server `stop` saved all dimensions
and exited **0**. The QA client was deliberately terminated after server shutdown
(Java exit 143 / Gradle exit 1), not counted as a crash. Its earlier onboarding run
was likewise deliberately terminated for the external profile correction.

Final JAR inspection and `git diff --check` passed. Only the seven scoped files
(build, production metadata/initializer/helper, one GameTest, README and this
document) changed. No generated files or Python bytecode are included. Commons
remains clean at the accepted baseline with the same artifact checksum.

**Phase 6A PASS** for the required dependency/module-registration scope, with the
explicit visual UI limitation above. One local commit, `build: require Zenithgb
Commons`, records this phase; nothing is pushed. Phase 6 remains incomplete.

## Deferred work

See [Commons Almanac audit](COMMONS_ALMANAC_AUDIT.md) for Phase 6B's source-based
surface classifications and proposed first static content set. It implements no migration.

Phase 6B must audit/classify the existing Almanac and knowledge surfaces before
any migration. No knowledge migration, Vintage Archives migration, Tilth/Reeve
link, interoperability or release acceptance is implied by this phase.
