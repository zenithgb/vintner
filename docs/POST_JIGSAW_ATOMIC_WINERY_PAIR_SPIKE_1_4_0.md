# Vintner 1.4.0 post-jigsaw atomic winery-pair spike

Verdict: **BLOCKED — POST-LAYOUT PAIRING NOT SAFE**. The layout transaction,
piece persistence, and Winemaker acquisition worked. A naturally generated
Taiga vineyard lost authored soil/vine blocks in one sampled village, including
after a full-footprint generator-height preflight. The required complete-piece
natural-generation gate did not pass. Do not make a release candidate from this
uncommitted worktree.

## Scope and starting state

- Isolated worktree: `/Users/zachariaheverson/Developer/Minecraft/vintner-worktrees/1.4.0-five-winery-integration`.
- Branch and base HEAD: `phase/1.4.0-five-winery-integration`, `857476c7f3ff69e0c15b1e27cc83bfa51b343b4e`.
- Starting worktree already had modified structure auditor/generator/GameTests,
  four combined runtime NBTs, four split-source directories, and four untracked
  integration/spike reports. Those sources/reports were preserved. The dirty
  canonical checkout was not modified.
- No push, merge, tag, upload, publication, dependency-project change, or
  Vintner 1.5 work occurred.

## Hook and transaction

`StructureGenerateMixin` redirects `Structure.generate`'s call to
`StructurePiecesBuilder.build()`. At this point `JigsawPlacement` has completed
the village piece list, but `PiecesContainer`/`StructureStart` has not been
created and no structure blocks have been placed. The hook is limited to the
Savanna and Taiga village structure keys. It creates two vanilla
`PoolElementStructurePiece` objects with `SinglePoolElement` templates, keeps
both local during search, and adds both to the builder only after both pass.
The pieces persist in `StructureStart.Children` with their template IDs,
origins, bounding boxes and rotations. There is no global pair registry,
deferred world write, or ordinary houses-pool dependency.

Search sorts at most 64 completed village street pieces by bounding box and
tests four sides each. The authored road-edge coordinate is transformed by a
culture-specific rotation. A candidate must be within the start's eight-chunk
reference range and must have **no XZ bounding-box overlap** with any vanilla
piece or the other half. A one-block adjacency to a street is allowed. The
terrain check uses generator surface/ocean-floor heights, rejects water and
more than two blocks of predicted footprint variation. The later experiment
queried every footprint column; that still missed the Taiga failure below.

## Byte-exact sources and probe templates

| Culture | Runtime/source template | SHA-256 | Dimensions |
| --- | --- | --- | --- |
| Savanna winery | `vintner:village/savanna/winery_tier1` | `7f50357028418cdd2599164fdf40d6ebbd625748832b7147f37128bcca1fa91a` | 15×8×18 |
| Savanna field | `vintner:village/savanna/vineyard_field_tier1` | `39733138bb05eed5f9b48e0028a0a2587b7b5bdefa147c5afd0b9cb4c5d2b799` | 7×3×18 |
| Taiga winery | `vintner:village/taiga/winery_tier1` | `8198a9905ceb011c55a7546087ca33ef74b6bdd19d74877d8504e8db189583fb` | 11×11×15 |
| Taiga field | `vintner:village/taiga/vineyard_field_tier1` | `b9ce1de6fdbf215960f310b59664e8b223111bab62eac98bc976ff69b95e45c3` | 11×3×13 |

The four split runtime NBTs are byte-identical to their maintained
`structure_sources` counterparts. Diagnostic log lines print IDs, source
hashes, sizes, considered candidates, rejection counts, chosen origins and
rotations, and commit status. The old combined Savanna/Taiga templates were
not used by this pair probe.

## Results

- Savanna 100-start GameTest: 100 valid starts; 98 pairs; winery-only 0;
  field-only 0; duplicates 0; serialized/reloaded pairs 98; exceptions 0.
- Savanna 25 fresh naturally located villages, seed 1400: 14 pairs and 11
  unchanged starts; partial/duplicate 0. All 14 pairs physically placed:
  14 Acacia Grape Presses, 630 vineyard-soil blocks, 1,050 Acacia red vines,
  210 Acacia trellises; no missing piece chunks. Separation min/median/max
  15.3/35.3/74.0 blocks; winery-centre 29.5/49.9/82.1; field-centre
  23.3/59.4/74.0. Both halves had zero gap to a street bounding box.
- A naturally placed Acacia Grape Press was claimed by a Savanna Winemaker.
  `minecraft:job_site` pointed to `[8106,73,7800]`; profession and job site
  survived save, stop and restart. Two barrel stands were also POIs, so two
  villagers selected Cooper before a third selected Winemaker.
- Taiga 100-start GameTest, including after full-footprint terrain scan:
  100 valid starts; 100 pairs; winery-only 0; field-only 0; duplicates 0;
  serialized/reloaded pairs 100; exceptions 0.
- Taiga first 25 fresh natural villages, seed 1400: 22 pairs, three unchanged,
  no partial/duplicates. A subsequent fresh world with the full-footprint
  terrain check yielded 21 pairs, four unchanged, no partial/duplicates.
  Separation min/median/max 15.2/47.8/124.6; winery-centre
  20.2/52.0/86.6; field-centre 4.6/48.1/80.8. Both halves had zero street
  bounding-box gap. The 113–125 block separations are visual review cases.
- All 21 winery pieces in the second Taiga world physically placed one Spruce
  Grape Press and the expected 31 Vintner workstation/storage blocks each.
  Twenty fields had 36 vineyard-soil blocks each. The remaining field, at
  village `[-336,6832]`, bounding box `[-392,69,6783,-382,71,6795]`, had
  **29 rather than 36** soil blocks and **54 rather than 63** red vines.
  A strip contained `minecraft:cave_air`/ore where authored soil and vines
  should have survived. Its start still serialized both pieces, so the layout
  transaction alone did not detect the block-placement loss. No piece chunk
  was missing. This same location failed before and after the broader terrain
  preflight. This is a mandatory stop, not a passing terrain case.
- A naturally placed Spruce Grape Press was claimed by a Taiga Winemaker;
  its `minecraft:job_site` pointed to `[-9135,67,-17384]` and profession/job
  site persisted across save, stop and restart.
- All four rotations occurred across the two natural samples for each culture.
  Rotation identity and full piece bounding boxes survived serialization.
  Directional visual correctness, field foundations, and one/two-step terrain
  appearance remain unaccepted. The damaged Taiga field demonstrates a
  failed terrain/complete-piece gate.

## Release boundary

The current authoritative RC remains
`/Users/zachariaheverson/Developer/Minecraft/vintner-release-evidence/1.4.0-plains-winery-spawn-investigation-20260924/candidate/vintner-1.4.0.jar`
at SHA-256
`9149278b342a60f702f1069f11bdf37132069daf3a7b91ca4484cf45fb475ba6`.
No new candidate was built or frozen. Plains/Desert/Snowy final combined-culture
acceptance, complete structure audit, standard and Serene Seasons suites,
release audits, clean build, exact-JAR client/server matrix, human visuals,
two-player play, and public 1.3.1 world upgrade remain open for this spike.

## Reproducible evidence

All local probe artifacts are in the isolated worktree's ignored
`build/post-jigsaw-natural/` directory. Especially:

- `savanna-25-layout.log`, `savanna-25-blocks.log`;
- `taiga-100-footprint.log`;
- `taiga-25-footprint-layout.log`, `check_blocks_taiga_footprint.log`;
- `scan_25.py`, `scan_25_taiga_footprint.py`, `check_blocks.py`,
  `check_blocks_taiga_footprint.py`;
- disposable saved worlds in `build/release-server-run/` named
  `paired-savanna-diagnostic-1400`, `paired-taiga-diagnostic-1400`, and
  `paired-taiga-footprint-1400`.

The path forward is to diagnose the exact chunk/terrain overwrite sequence for
the damaged Taiga field within this bounded spike before attempting another
candidate policy. Do not merely raise village weights or treat a saved pair as
proof of physically complete placement.
