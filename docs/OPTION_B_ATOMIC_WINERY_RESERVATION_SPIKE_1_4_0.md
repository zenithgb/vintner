# Vintner 1.4.0 — Option B atomic winery reservation spike

Verdict: **BLOCKED — OPTION B UNSAFE; PRODUCT DECISION REQUIRED**.

Classification: `OPTION_B_UNSAFE` for the 1.4 release track under the required
all-or-nothing contract. A compact marker can be selected by Savanna villages,
but the tested layout provides no reserved full-winery volume, and the
available placement lifecycle has no atomic commit or rollback across the
large template. This is a safety decision about the proposed 1.4 integration,
not a proof that no different worldgen system could ever place these builds.
The spike stopped before any full winery placement, production hook, or RC.

## Starting state and preserved sources

- Isolated worktree: `/Users/zachariaheverson/Developer/Minecraft/vintner-worktrees/1.4.0-five-winery-integration`.
- Branch `phase/1.4.0-five-winery-integration`, HEAD
  `857476c7f3ff69e0c15b1e27cc83bfa51b343b4e`.
- Starting status was uncommitted: modified generator, auditor, red GameTest,
  four runtime NBTs; untracked four immutable source NBTs and two blocker
  reports. No reset, stash, clean, or commit occurred.
- Byte-exact source SHA-256: Desert
  `7be4b217c7f987829e1b2a2eeabdc87bbf83054984daa2db56610eb1f712d805`,
  Savanna
  `60348dbd4b0a5c4443029af827e3f7806d1a22783f3b4f19bf5c29ce2bf1b80b`,
  Snowy
  `1fdfc017cca2ea97318bb61b06296972ea3ebeef9208222f69430ff048e00549`,
  Taiga
  `f3a924c609b69c36e9a0b627478585402fc2f7537eaadfc6b6f02ad91a9f2a9a`.
- The release-consolidation baseline and dirty canonical checkout were not
  changed.

## Minecraft 26.3 lifecycle findings

Inspection used the locally resolved Minecraft 26.3 client/server classes in
`~/.gradle/caches/fabric-loom/26.3/minecraft-client.jar` and the Vintner
source. The relevant classes are `JigsawPlacement$Placer`,
`StructurePiecesBuilder`, `StructureStart`, `PoolElementStructurePiece`,
`StructureTemplate`, `WorldGenRegion`, and `ChunkPyramid`.

1. `JigsawPlacement` selects pool elements and assembles a piece list using
   bounding-box collision shapes. Accepted pieces enter that list before
   children are processed. The existing Vintner mixin observes this list to
   enforce one winery per village at selection time.
2. At structure-start creation, the piece list is saved as `Children`.
   Reload resolves each piece through the registered `StructurePieceType`.
   A custom B1 piece could save template ID, origin, rotation, and decision
   data, but registration does not change placement timing.
3. `StructureStart.placeInChunk` calls `postProcess` only for pieces whose
   bounding boxes intersect the currently generated chunk. A normal pool
   piece delegates to its element's `place`; the boolean return is discarded.
4. `StructureTemplate.placeInWorld` processes blocks and calls `setBlock`
   incrementally. A rejected/failed later block does not roll back earlier
   writes. A processor can filter or change individual blocks; it is not a
   transaction or whole-village reservation point.
5. At the `FEATURES` generation step, `WorldGenRegion` uses a one-chunk
   block-write radius and rejects writes outside its write zone. A full winery
   spans multiple chunks. Placing it from one invocation would require
   neighboring-chunk writes inside that radius plus a proof of ordering,
   persistence, and completion. That proof was not established. Normal
   per-chunk placement cannot provide the requested zero-visible-block abort.
6. Layout can inspect proposed structure pieces and generator height
   predictions, but not the final neighboring-chunk block states that the
   requested protected-block/terrain preflight needs. At block-placement
   time, the structure start and its marker are already persisted; there is
   no supported hook here to remove a failed reservation and retry a later
   village candidate atomically.

## Mechanism comparison

| Mechanism | Narrowest plausible hook | Persistence | Blocking issue |
| --- | --- | --- | --- |
| B1 custom Vintner piece | Replace a chosen marker in the village piece list before start serialization | Registered piece type with template ID, origin, rotation, and decision | Full physical preflight is later than start serialization; `postProcess` is chunk-driven, and no rollback spans the template. |
| B2 marker plus deferred placement | Marker pool entry, then a Vintner placement hook during or after worldgen | Marker/start plus additional completion state | A late hook has no reserved full volume; rejected marker can suppress later valid selection; deferred/tick repair or neighbor writes would violate the brief without a new safety proof. |
| B3 custom pool element/factory | Custom element codec in the village houses pool | Element codec plus piece save data | A compact collision box does not reserve the full authored footprint; a full collision box recreates the 0/100 placement blocker. It inherits B1/B2's placement timing and adds codec risk. |

No production Option B mechanism was selected. A safe candidate would need a
bounded layout-time reservation for all authored visible blocks, a
street-only compatible overlap policy, later protected-block and terrain
preflight before any visible write, deterministic one-per-village retry, and
a single completion/rollback contract across all affected chunks. The
inspected hooks do not jointly supply these properties without intrusive
jigsaw collision-state changes and cross-chunk placement behavior. Those
changes exceed this release spike's safety boundary.

## Savanna marker-only layout control

The reversible diagnostic replaced only the *experimental runtime copy* of
the Savanna template with a 3×8×3 marker region around the existing authored
north path connector. It kept a second temporary copy of the complete runtime
template solely for calculating the intended rotated full bounding box. The
Savanna candidate was offered first as an experimental control. The test
generated 100 deterministic starts through the real
`minecraft:village_savanna` structure-start path; it did not place visible
blocks in a fresh natural world.

| Metric | Result |
| --- | ---: |
| Valid village starts | 100 |
| Marker selections | 98 |
| Duplicate selected markers under the existing one-per rule | 0 |
| Full winery boxes with no other piece-box intersection | 0 |
| Full boxes intersecting at least one other piece box | 98 |
| Boxes without a house or town-center box intersection | 12 |
| Complete full wineries placed | Not attempted |
| Partial full wineries placed | Not attempted |
| Generation exceptions observed in the layout probe | 0 |

The 12 less-constrained candidates still intersected streets, terminators,
lamps, or feature pieces; two examples intersected only a Savanna straight
street's box. These are **box** intersections, not proof that actual authored
blocks collide. It would be incorrect to reject all 98 based only on those
boxes. Equally, allowing them without transformed block-level checks could
overwrite village roads or leave later village placement to overwrite the
winery. The study therefore found a candidate-selection signal but did not
prove a safe placement policy.

The diagnostic logs are retained in this worktree's ignored
`build/option-b-spike/` directory. The existing red four-culture GameTest
reported its expected failure because unsplit Taiga remains 0/100; that
failure is not an Option B placement result.

## Stop boundary and final state

No full-placement prototype was activated. The proposed atomicity guarantee,
accepted/rejected preflight counts, visible-block-before-abort count,
completion signature, save/restart, POI/profession, rotations, terrain,
25 natural Savanna villages, Taiga prototype, one-per-village retry after
rejection, parallel/reload behavior, full standard and Serene GameTests,
clean build, exact-JAR runtime, and new candidate were **not** reached.
Absence of partial wineries in this selection-only probe is not an
atomicity pass.

The temporary marker, complete-template copy, first-choice source change,
and GameTest diagnostics were restored or removed. The Savanna runtime SHA-256
again equals its pre-spike value
`75fa1889d371681e5a329c6d9b3900d5dfd6012e8fa0caf2f838c7abecb96706`.
`gametestClasses`, generator `--check`, the five-winery structure audit, and
`git diff --check` pass on restored source; copied prototype assets are absent
from Gradle output. Desert, Snowy, Plains, and the Taiga source/runtime are
intact. No commit, push, merge, tag, upload, or publication occurred.

The current authoritative RC remains
`/Users/zachariaheverson/Developer/Minecraft/vintner-release-evidence/1.4.0-plains-winery-spawn-investigation-20260924/candidate/vintner-1.4.0.jar`,
SHA-256
`9149278b342a60f702f1069f11bdf37132069daf3a7b91ca4484cf45fb475ba6`.

**Next action:** Zach/ChatGPT makes the product decision between retaining
the smaller interim Savanna/Taiga wineries for the 1.4 release while preserving
the player-authored builds for later worldgen work, or separately authorizing
Option C village-adjacent placement. Neither alternative was implemented.
