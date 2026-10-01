# Five player-built village wineries — integration blocker

Status: **BLOCKED before release-candidate replacement**. The source exports
were confirmed by Zach and copied byte-exact into `structure_sources/` in the
isolated `phase/1.4.0-five-winery-integration` worktree. The four runtime NBTs
are deterministic player-derived candidates, not an accepted release change.
The existing Plains integration and exact RC `9149278b342a60f702f1069f11bdf37132069daf3a7b91ca4484cf45fb475ba6`
remain the accepted baseline.

| Culture | Authoritative exact-client world | Source SHA-256 | Source/runtime size | Blocks | Press | Runtime connector |
| --- | --- | --- | --- | ---: | --- | --- |
| Plains | Existing `structure_sources/village/plains/vineyard_tier1.nbt` | `7914e27f3bce456181324a23c459c8159ce26c1b8d227d588f99b3b8986e8c94` | 17×11×11 / 16×11×11 | 2,057 source | Oak `(7,1,7)` runtime | `(8,2,0)` north, dirt path; accepted one-column terrain trim |
| Desert | `New World (4)` | `7be4b217c7f987829e1b2a2eeabdc87bbf83054984daa2db56610eb1f712d805` | 13×10×13 | 1,690 | Jungle `(9,2,3)` east | `(6,0,0)` north, cut sandstone |
| Savanna | `New World (5)` | `60348dbd4b0a5c4443029af827e3f7806d1a22783f3b4f19bf5c29ce2bf1b80b` | 22×8×18 | 3,168 | Acacia `(15,3,3)` north | `(7,0,0)` north, dirt path |
| Snowy | `New World (6)` | `1fdfc017cca2ea97318bb61b06296972ea3ebeef9208222f69430ff048e00549` | 13×16×10 | 2,080 | Spruce `(8,0,7)` north | `(2,0,9)` south, snowy=false grass block |
| Taiga | `New World (8)` | `f3a924c609b69c36e9a0b627478585402fc2f7537eaadfc6b6f02ad91a9f2a9a` | 23×11×15 | 3,795 | Spruce `(16,3,11)` south | `(12,1,0)` north, dirt path |

The four confirmed exports reside under
`/Users/zachariaheverson/Library/Application Support/ModrinthApp/profiles/Vintner 10E3B Exact Client/saves/<world>/generated/vintner/structure/village/<culture>/vineyard_tier1.nbt`.
The initially named Showcase export paths were incorrect. Each source has zero
entities, one Grape Press, vineyard content, zero Composters and no jigsaw,
structure, barrier or command block. The runtime transform adds one jigsaw at
the listed boundary block; its `final_state` restores the exact authored block.
All other block states and block-entity NBT are unchanged. The variants retain
their distinct player-built architecture and have not been homogenized.

## Focused evidence

- Python deterministic-generation and five-structure audits: pass. All four
  rotations remain within NBT bounds; the new runtime GameTests verified the
  Grape Press, one rack, one door, a vine and the connector through each of the
  four real placement rotations for every new culture (4/4 tests passed).
- Java/GameTest compilation passed. The pool test passed with the existing
  Plains first-choice policy and the other cultures' existing weight 24.
  Vanilla houses-pool effective weights are 72 Desert, 81 Savanna, 68 Snowy
  and 76 Taiga; after the two specialist entries and winery they are 98, 107,
  94 and 102 respectively. Each winery entry is injected once.
- The **vanilla JigsawStructure generation path** was called for 100 start
  locations per culture in the disposable GameTest world. The final
  discoverability assertion failed: Desert 18/100, Savanna 0/100, Snowy 22/100,
  Taiga 0/100. No start contained two wineries. This isolates jigsaw selection
  and box-placement behavior; it is not a rendered natural-world or biome/terrain
  acceptance pass. The initial 25-start samples were Desert 7, Savanna 0,
  Snowy 7, Taiga 0.
- Temporary diagnostic runs, restored afterward, showed Savanna/Taiga at 0/100
  even with the previous 15×10×13 interim NBT, first-choice candidate ordering,
  and alternate street-edge connectors. The test's candidate instrumentation
  counted 3,507 Savanna and 5,163 Taiga offered house-pool slots in one
  100-start run, with zero placements. A connector-only 13×10×13 air box placed
  in 88/100 Savanna and 99/100 Taiga starts under the same diagnostic ordering;
  a connector-only 14×10×13 or 15×10×13 box placed in 0/100. These controls
  identify bounding-box placement rejection rather than a missing pool entry,
  connector parsing failure or insufficient weight. No diagnostic ordering
  change remains in production code.
- The smallest rectangle containing non-terrain authored blocks is 21×17 for
  Savanna and 23×15 for Taiga. Trimming only external air/terrain margins cannot
  approach the fitting 13-block control without removing visible architecture,
  vineyard rows or work areas. The sources and current runtime candidates are
  not to be shrunk to meet that control.

## Stop condition and remaining gates

The Savanna and Taiga structures need a culture-specific larger-lot jigsaw
integration or another empirically demonstrated placement approach. That is a
broader world-generation design decision than connector normalization or unused
margin trimming. The brief directs work to stop here rather than redesign the
player builds or silently change village selection rules.

The focused discoverability GameTest remains red as a reviewable guard. Terrain
placement, all-culture workstation pathing/profession and villager-type tests,
reload/parallel-generation checks, full standard and Serene Seasons suites,
exact-JAR server/client, distribution update, and human visual acceptance have
not run. No new release candidate was built, installed or frozen. No Commons,
Tilth, Reeve, shared checkout, existing release evidence or Showcase world was
modified; nothing was pushed, merged, tagged, uploaded or published.
