# Vintner 1.4.0 — large winery placement architecture finding

Verdict: **BLOCKED — LARGE WINERY REQUIRES DIFFERENT PLACEMENT ARCHITECTURE**.

The ordinary village houses-pool, multi-piece jigsaw design fails the required
all-or-nothing acceptance gate. The Savanna prototype produced incomplete
winery starts even though its private pools loaded and its child connectors
were compatible. The temporary runtime pieces and first-choice experiment
were restored; the imported player sources and existing integration work remain
intact. No candidate supersedes the current 1.4.0 RC.

## Scope and starting state

- Worktree: `/Users/zachariaheverson/Developer/Minecraft/vintner-worktrees/1.4.0-five-winery-integration`.
- Branch: `phase/1.4.0-five-winery-integration`; base HEAD `857476c7f3ff69e0c15b1e27cc83bfa51b343b4e`.
- Starting work was uncommitted: structure generator and audit changes, red
  jigsaw discoverability GameTest, four runtime NBTs, four immutable imported
  source NBTs, and the five-winery blocker report. This status was preserved.
- The dirty canonical checkout and release consolidation worktree were not
  changed.

The other imported sources were also rehashed and preserved: Desert
`7be4b217c7f987829e1b2a2eeabdc87bbf83054984daa2db56610eb1f712d805`
and Snowy
`1fdfc017cca2ea97318bb61b06296972ea3ebeef9208222f69430ff048e00549`.

## Immutable source identities and decomposition

| Culture | Source SHA-256 | Source size | Candidate anchor | Child 1 | Child 2 |
| --- | --- | --- | --- | --- | --- |
| Savanna | `60348dbd4b0a5c4443029af827e3f7806d1a22783f3b4f19bf5c29ce2bf1b80b` | 22×8×18 | x9–21, z0–8; 13×8×9 | x0–8, z0–17; 9×8×18 vineyard and path | x9–21, z9–17; 13×8×9 rear building |
| Taiga | `f3a924c609b69c36e9a0b627478585402fc2f7537eaadfc6b6f02ad91a9f2a9a` | 23×11×15 | x12–22, z0–7; 11×11×8 | x0–11, z0–14; 12×11×15 vineyard and path | x12–22, z8–14; 11×11×7 rear building |

The Savanna's vineyard rows are west at x1, x3, and x5, with the long path at
x7, the main building at x9–21, door at `(13,3,6)`, Acacia Press at
`(15,3,3)`, and rear work/storage through z17. The candidate cut kept the
press and front door with the anchor; the vineyard and rear building were
separate children. Its initial village connector was on the north edge of the
front building at `(15,0,0)`. This was an experimental street relationship,
not an accepted final entrance decision: the authored path starts at `(7,0,0)`.

The Taiga has a north approach along x12, west vineyard rows at x2, x4, x6,
and x8, an eastern timber/stone winery at x14–22, lower storage/cellar, a
rear work area, and Spruce Press at `(16,3,11)`. The proposed anchor contained
the north path and front building, while the rear child would own the press.
No Taiga runtime split was activated because Savanna met the stop condition.

## Exact block partition proof

The read-only partition check compared every source position's complete
palette state and optional block-entity NBT against the union of the proposed
regions. The regions were disjoint and covered every source position:

| Culture | Source blocks | Block entities | Region block counts | Source and recombined signature | Result |
| --- | ---: | ---: | --- | --- | --- |
| Savanna | 3,168 | 11 | 936 + 1,296 + 936 | `22b96fa1a9c7a69af8b95626e3fceb5c0355dbfbd8f061ba7aa4656eda42a4e5` | Equal; no missing, extra, or duplicate positions |
| Taiga | 3,795 | 30 | 968 + 1,980 + 847 | `b476d6127f15d917978638f6da8a243fef7f53d616b60db537b8999ecaa1f7fd` | Equal; no missing, extra, or duplicate positions |

This proves coordinate ownership and source-state reconstruction before
technical jigsaw substitution. It does **not** prove Minecraft will place all
regions, rotate them as one assembly, or preserve them on terrain.

## Savanna runtime prototype and 100-start evidence

The temporary prototype used one normal Savanna houses-pool anchor
(`vintner:village/savanna/vineyard`) and two weight-one private pools:
`vintner:village/savanna/vineyard_west` and
`vintner:village/savanna/vineyard_rear`. Each pool contained exactly its one
matching child template, with rigid projection and `minecraft:empty` fallback.
There were no random architectural alternatives or loops. The original
source blocks occupied each region except connector substitutions whose
`final_state` restored the original state. The prototype used default
selection/placement priority on child jigsaws.

The test server confirmed both private pools had size one, both templates
loaded at the expected dimensions, and each exposed one jigsaw connector.
Minecraft's `JigsawBlock.canAttach` returned `true` for both intended
anchor-child pairs.

The equivalent village-jigsaw-start probe used 100 Savanna starts with the
real `minecraft:village_savanna` structure path and the existing deterministic
sample positions. It inspects generated piece lists; it is **not** a fresh
natural-village or physical terrain-placement test.

| Probe | Valid starts | Anchor present | Complete three-piece assemblies | Incomplete anchors | Duplicates |
| --- | ---: | ---: | ---: | ---: | ---: |
| Normal house-pool candidate order | 100 | 55 | 0 | 55 | 0 |
| Savanna anchor offered first, as a diagnostic | 100 | 89 | 0 | 89 | 0 |

The focused GameTest run was expected to fail: its existing four-culture
discoverability gate still found Taiga at 0/100. More importantly, Savanna
failed the stricter completeness gate in both experimental modes. The test
reported no child pieces for the accepted Savanna anchors. First-choice
ordering increased anchors and did not produce a complete winery. It was
restored after the probe; no weight was changed.

The exact reason each child was rejected was not isolated among village
recursion depth, occupied collision volume, and local placement constraints.
That distinction cannot change this acceptance result. In Minecraft 26.3's
`JigsawPlacement$Placer.tryPlacingChildren`, a chosen piece is added to the
pieces list before its child state is queued, while rejected child candidates
are skipped without rolling back the parent. Ordinary jigsaw pools have no
transactional required-child contract. Selection or placement priority can
change order but cannot make required child placement atomic or override a
collision. The observed 55/55 and 89/89 incomplete anchors establish the
actual failure mode for this prototype.

The focused server logs are retained in this worktree's ignored
`build/large-winery-prototype/` directory as `probe.log`,
`probe-prioritized.log`, and `probe-attach.log`. The temporary Savanna runtime
was restored byte-for-byte to its pre-prototype SHA-256
`75fa1889d371681e5a329c6d9b3900d5dfd6012e8fa0caf2f838c7abecb96706`;
the four experimental child templates/pool files were removed. The source
generator's `--check`, the five-winery structure audit, and `git diff --check`
all passed after restoration. `gametestClasses` also passed on the restored
sources, and Gradle removed the temporary child resources from its copied
output.

## Acceptance boundary and alternatives

The brief explicitly stops work when a successful start can contain a partial
winery. Consequently, Taiga's 100-start runtime prototype, 25 fresh natural
villages per culture, four rotations, flat/one-step/two-step terrain and seam
tests, Winemaker POI/profession/save-reload checks, generation performance
sampling, five-culture profession tests, standard and Serene GameTest matrices,
clean build, release audits, exact-JAR runtime, and new RC freeze were **not
run**. The logical Taiga split is unvalidated at runtime. No visual or gameplay
acceptance is inferred.

**Option B — dedicated Vintner placement reservation.** A small village-facing
piece could reserve a winery envelope and place the immutable full template as
one deterministic operation after the jigsaw decision. This would need a
separate design proof for bounding-box reservation before neighboring pieces,
chunk-safe and server-deterministic placement, persistence/reload, rotation,
terrain alignment, and collision rejection that leaves no anchor. It is more
complex than vanilla pools and was not implemented.

**Option C — village-adjacent specialist structure.** Generate the winery near
the matching village through a separate structure path. This avoids treating
the full winery as a village house slot but changes village association,
spacing, and visual approach behavior. It is a larger product/worldgen change
and needs separate approval. It was not implemented.

The current release candidate remains
`/Users/zachariaheverson/Developer/Minecraft/vintner-release-evidence/1.4.0-plains-winery-spawn-investigation-20260924/candidate/vintner-1.4.0.jar`,
SHA-256
`9149278b342a60f702f1069f11bdf37132069daf3a7b91ca4484cf45fb475ba6`.
Desert and Snowy source/runtime work remains intact, including the previously
passing focused import, connector, and rotation evidence; it was not rerun
under this failed Savanna experiment. No commit, push, merge, tag, upload,
publication, or release was made.

**Recommended next action:** authorize a separately scoped Option B design
spike that proves an all-or-nothing reservation and placement contract before
changing the 1.4.0 release candidate.
