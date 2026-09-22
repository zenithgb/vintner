# Vintner 1.4.0: Minecraft 26.3 Poplar integration

Date: 2026-09-22
Status: implementation and automated acceptance passed locally; no publication authorized.

## Baseline and isolation

This phase starts from the accepted Minecraft 26.3 and exact Commons 0.1.2
commit `f59f0cd9564ab7ffb77ef284fb2d67ea32922fdb` in an isolated
`phase/1.4.0-poplar` worktree. The user's active Vintner checkout and its
tracked and untracked model work were not edited, reset, stashed or cleaned.

The supported family order is Oak, Spruce, Birch, Jungle, Acacia, Dark Oak,
Mangrove, Cherry, Pale Oak, Poplar, Bamboo, Crimson and Warped. This retains
the established order of all existing identifiers while placing Poplar with
the overworld woods immediately before Bamboo. Registry identifiers are
unchanged; Poplar adds only `poplar_` identifiers.

## Covered families

Poplar is generated for every complete public Vintner wood family:

- Trellis;
- Grape Press;
- Fermentation Barrel;
- Aging Barrel;
- Wine Rack;
- Wine Crate;
- Wine Basket;
- Vintage Archive;
- Barrel Stand;
- Labelled Cellar Shelf;
- Tasting Cabinet;
- Tasting Service;
- Estate Management Desk;
- Surveyor's Map Table.

The non-item Red and White Grapevine support blocks also receive Poplar so
vine placement preserves the selected trellis wood. All 14 public recipes use
Minecraft's `poplar_planks`; models use the vanilla `poplar_planks`,
`poplar_log` and `poplar_log_top` textures.

Intentional exclusions are Vineyard Soil, Nursery Bed, Grape Bowl, the placed
Wine Bottle and fixed-material tools/items because they are not wood-family
content. Toasted Aging Barrel, Seasoned Aging Barrel and Cellar Cask retain
their existing compatibility IDs and remain treatment states obtained by
upgrading an ordinary wood-family Aging Barrel; separate Poplar-prefixed
specialist variants would duplicate that mechanic and were not added.

## Generation and models

`WoodVariant` remains the registration and Creative-order source of truth.
`scripts/generate_wood_variants.py` remains the resource-family source of
truth. The Estate Desk, Surveyor's Map Table, Tasting Service and serving-decor
generators consume the same ordered `WOODS` table, so Poplar inherits the
accepted geometry and changes only its texture mapping.

No Blockbench geometry was redesigned. The representative custom families
were checked through the existing release audits: connected Desk, Map Table
and Archive envelopes and connection pieces; all Tasting Service states; the
Wine Basket approved pose/contact contract; trellis connection and slope
models; and storage base/display separation. The Surveyor's Map Table helper
was aligned with the canonical recipe-advancement and loot schema after the
first regeneration exposed stale output. Two subsequent complete generation
passes produced the identical source/resource SHA-256
`6be12ce3158fcb7720c601fc44c9619412cf60673353b2e43fe7ee1d458e9bce`.

## Exact resource delta

| Metric | Before | After | Change |
|---|---:|---:|---:|
| Supported woods | 12 | 13 | +1 |
| Public full-family blocks/items | 168 | 182 | +14 |
| Red/white grapevine support states | 24 | 26 | +2 |
| Recipes | 182 | 196 | +14 |
| Recipe advancement helpers | 182 | 196 | +14 |
| All JSON resources | 1,915 | 2,025 | +110 |
| Blockstates | 199 | 215 | +16 |
| Model JSON | 869 | 907 | +38 |
| Vintner texture files | 100 | 100 | 0 |
| Loot tables | 177 | 191 | +14 |

The release audit's historical metric changes from 159 to 172 because it
counts 13 of the public wood families plus the three retained specialist
aging-vessel IDs; Wine Baskets are validated separately by the serving-decor
audit. No new Vintner texture is required because Poplar uses Minecraft 26.3's
vanilla textures.

## Validation

- common, client and GameTest compilation: PASS;
- all resource JSON parsing: PASS;
- deterministic generators, two consecutive passes: PASS;
- release audit: 2,025 JSON files, 196 recipes, 172 historical-metric public
  blocks and 26 grapevine states;
- Nursery Bed model audit: PASS and unchanged;
- serving-decor pose/contact audit: PASS across 13 woods;
- focused Creative semantic-order test: 1/1 PASS after replacing stale literal
  12-family totals with cardinality-derived expectations;
- standard Minecraft 26.3 GameTests: 194/194 PASS;
- Serene Seasons/GlitchCore GameTests: 194/194 PASS.

Final clean-build, exact-JAR server/client smoke, checksum and package evidence
are recorded in the external Poplar phase evidence directory so the committed
source remains the immutable candidate under test.

## Compatibility

No existing block/item identifier, component, block-entity format, network
payload or saved-data schema changed. Existing non-Poplar content is generated
from the same inputs and remained byte-stable. Poplar registrations and assets
are additive, so no Vintner save migration is required. Commons remains the
external exact 0.1.2 artifact and is not modified or embedded.

This phase establishes the Minecraft 26.3 Poplar model baseline only. It does
not begin the comprehensive Blockbench redesign/audit.
