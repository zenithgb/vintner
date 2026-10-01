# Vintner 1.4.0 — paired winery and vineyard placement spike

Verdict: **BLOCKED — PAIRED VILLAGE PLACEMENT NOT SAFE** under the brief's
independent-fit gate. Neither full Savanna split piece was selected in 100
ordinary village starts with the inherited or alternative street-facing
connector. A compact control made from each same source and connector was
selected in 98/100 starts. This isolates the failure to the full pieces'
ordinary jigsaw placement envelope in the sampled starts; it does not prove
that a different, separately designed village architecture could never work.
No pair logic, Taiga runtime activation, new candidate, or release work was
attempted after this stop condition.

## Starting state and source custody

- Worktree: `/Users/zachariaheverson/Developer/Minecraft/vintner-worktrees/1.4.0-five-winery-integration`.
- Branch: `phase/1.4.0-five-winery-integration`.
- HEAD: `857476c7f3ff69e0c15b1e27cc83bfa51b343b4e`.
- The existing generator, auditor, GameTest, four combined runtime NBT changes,
  four combined sources, and three blocker reports were uncommitted and were
  preserved. The dirty canonical checkout was not changed.
- Four new split exports were copied byte-exact into `structure_sources` after
  read-only inspection. The original combined sources were not altered.

| Culture and piece | Exact source export | Bytes | SHA-256 | Size | Palette states | Blocks | Block entities | Entities |
| --- | --- | ---: | --- | --- | ---: | ---: | ---: | ---: |
| Savanna winery | `Vintner 10E3B Exact Client/saves/New World (5)/generated/vintner/structure/village/savanna/winery_tier1.nbt` | 8,186 | `7f50357028418cdd2599164fdf40d6ebbd625748832b7147f37128bcca1fa91a` | 15×8×18 | 52 | 2,160 | 11 | 0 |
| Savanna field | `Vintner 10E3B Exact Client/saves/New World (5)/generated/vintner/structure/village/savanna/vineyard_field_tier1.nbt` | 1,639 | `39733138bb05eed5f9b48e0028a0a2587b7b5bdefa147c5afd0b9cb4c5d2b799` | 7×3×18 | 20 | 378 | 0 | 0 |
| Taiga winery | `Vintner 10E3B Exact Client/saves/New World (8)/generated/vintner/structure/village/taiga/winery_tier1.nbt` | 7,806 | `8198a9905ceb011c55a7546087ca33ef74b6bdd19d74877d8504e8db189583fb` | 11×11×15 | 66 | 1,815 | 30 | 0 |
| Taiga field | `Vintner 10E3B Exact Client/saves/New World (8)/generated/vintner/structure/village/taiga/vineyard_field_tier1.nbt` | 1,984 | `b9ce1de6fdbf215960f310b59664e8b223111bab62eac98bc976ff69b95e45c3` | 11×3×13 | 33 | 429 | 0 | 0 |

The absolute prefix for every source export is
`/Users/zachariaheverson/Library/Application Support/ModrinthApp/profiles/`.
All four NBTs decode and re-encode losslessly, have complete in-bounds block
volumes and Minecraft 26.3 DataVersion 5023, and contain zero entities. There
are no Composters, Structure Blocks, Jigsaws, Barriers, command/debug blocks,
or embedded villagers. Palette IDs are syntactically valid; the Savanna
temporary templates loaded in the 26.3 GameTest runtime without a structure
parse error. A complete registry/state validation of the Taiga split exports
was not run after the Savanna gate failed.

Savanna winery contains exactly one Acacia Grape Press at local `[8,3,3]`,
seven Acacia wine racks, two fermentation barrels, two stands, and one tasting
service. Savanna field contains 45 vineyard-soil blocks, 75 Acacia grapevine
blocks, and 15 trellis blocks, with no workstation. Taiga winery contains
exactly one Spruce Grape Press at `[4,3,11]`, plus its authored cellar and
service blocks. Taiga field contains 36 vineyard-soil blocks, 63 Spruce
grapevine blocks, and nine trellis blocks, with no workstation. The split
exports contain their own foundations/terrain. Savanna winery has 28 dirt-path
blocks; Savanna field has no path. Taiga winery has 15 path blocks and Taiga
field has two. Both field exports have air margins and no unrelated building.

## Comparison with immutable combined designs

- Savanna winery's 911 non-air blocks match the combined source exactly after
  translating it seven blocks in X, including all 13 Vintner blocks and the
  Grape Press. The field occupies the seven-column vineyard side. Its 216
  non-air blocks have 28 differences from the combined source at that origin:
  19 grapevine states and nine grapevine-versus-trellis choices. No combined
  non-air position lies outside the two split volumes.
- Taiga winery's 879 non-air blocks match the combined source exactly after
  translating it 12 blocks in X, including all 31 Vintner blocks and the
  Grape Press. The field best aligns at combined offset `[0,1,2]`; 12 of its
  270 non-air blocks differ there: ten grapevine states, one
  grapevine-versus-trellis choice, and one path-versus-grass choice. Combined
  content omitted from the split volumes is mostly ground (180 dirt and 36
  grass blocks), plus one dirt-path block and one fence at the former join.
  No major building or vineyard area is missing. These minor authored
  differences were recorded, not automatically corrected.

## Layout architecture inspection

Minecraft 26.3 `JigsawPlacement$Placer.tryPlacingChildren` checks a candidate
bounding box against its mutable free-space shape, subtracts that space,
creates junctions, adds the piece to its list, and schedules descendants. The
current Vintner mixin observes shuffled house-pool candidates and the list
addition to enforce one combined winery per village. It does not have a
two-candidate precommit point. Intercepting the list addition alone would be
too late: the free-space shape and parent junction have already changed.

`JigsawPlacement` assembles the in-memory piece list before passing it to the
`StructurePiecesBuilder`. A post-layout Vintner helper could in principle
search accepted street connectors and add two independent pieces together
after both collision checks, with state scoped to one village-start callback.
That would require a separate, bounded candidate-location and connector
algorithm plus proof that both full pieces fit. It was not implemented or
classified safe, because the brief requires the independent ordinary-slot
controls to pass first. No piece-state transaction, serialization change,
global state, or block placement was added.

## Savanna independent controls and stop gate

Each control temporarily used the existing
`vintner:village/savanna/vineyard` pool entry and its established weight,
replacing only the experimental runtime NBT in this worktree. The new source
was never edited. The technical jigsaw replaced one boundary block and
restored that block through `final_state`. The existing GameTest generated
100 deterministic `minecraft:village_savanna` structure starts per run.

| Piece/connector | Full size | Full-piece selections / 100 | Compact 3×3×3 connector control / 100 |
| --- | --- | ---: | ---: |
| Winery north, inherited path at `[0,0,0]` | 15×8×18 | 0 | 98 |
| Winery west, boundary path at `[0,0,8]` | 15×8×18 | 0 | Not run |
| Winery south, boundary path at `[5,0,17]` | 15×8×18 | 0 | Not run |
| Field north, grass edge at `[3,0,0]` | 7×3×18 | 0 | 98 |
| Field east, grass edge at `[6,0,8]` | 7×3×18 | 0 | Not run |
| Field south, grass edge at `[3,0,17]` | 7×3×18 | 0 | Not run |

The compact controls kept the exact connector metadata and orientation of
their respective full north-facing pieces. They show that the pool injection,
connector contract, and 100-start probe route are functional. The full pieces
remain effectively 0/100 under three plausible street boundaries apiece.
The test did not instrument every candidate enumeration or collision branch,
so exact candidate-opportunity and rejection counts are **not measured**.
The 0/100 full versus 98/100 compact result supports bounding-box fit as the
placement blocker; it is not a block-level overlap measurement. The existing
four-culture GameTest exited nonzero as expected because it still demands
discoverable Savanna and Taiga combined wineries. No generation exceptions
were observed in these layout probes.

The independent-fit stop condition is met. No paired 100-start probe, partial
or duplicate pair count, 25 natural villages, separation statistics,
rotations, terrain fixtures, Winemaker POI/profession, save/restart, reload,
parallel generation, full GameTest matrices, clean build, exact-JAR runtime,
or new release candidate was attempted. Absence of pairs in a selection-only
probe is not evidence of atomic paired placement.

## Restoration and release authority

All temporary NBT variants and probe logs remain only in ignored
`build/paired-spike/`. The authored combined Savanna runtime resource and its
compiled copy were restored byte-exact to SHA-256
`75fa1889d371681e5a329c6d9b3900d5dfd6012e8fa0caf2f838c7abecb96706`.
`gametestClasses`, `generate_village_vineyards.py --check`, the existing
five-winery structure audit, and `git diff --check` pass on the restored
worktree. The split sources and this report are the only lasting additions
from this spike. No commit, push, merge, tag, upload, or publication occurred.

The authoritative Vintner 1.4 RC remains the Plains candidate at
`/Users/zachariaheverson/Developer/Minecraft/vintner-release-evidence/1.4.0-plains-winery-spawn-investigation-20260924/candidate/vintner-1.4.0.jar`,
SHA-256 `9149278b342a60f702f1069f11bdf37132069daf3a7b91ca4484cf45fb475ba6`.
No new five-culture manual checklist was created because there is no new
candidate. Existing candidate-specific manual acceptance still applies to
that older RC.

**Next action:** Zach/ChatGPT decides a separately scoped placement approach
for the split builds after reviewing this failed ordinary-slot fit evidence.
Do not silently activate Taiga or fall back to Option B or C.
