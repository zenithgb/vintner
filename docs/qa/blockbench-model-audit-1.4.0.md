# Vintner 1.4.0 Blockbench and model audit

Status: automated audit complete; human visual acceptance remains pending.

Baseline: `phase/1.4.0-poplar` at
`9426a9c7a4471ef59d466116c5491f744beecb99` (Minecraft 26.3,
13 wood families, including Poplar).

## Scope and method

This is the authoritative shipping-model inventory for Vintner 1.4.0. It
traces registered content through blockstate/item selection, generated model
JSON, renderers, maintainable sources and collision ownership. The inventory
contains 28 meaningful model families represented by 907 model JSON files
(692 block and 215 item models) and five maintained Blockbench projects.

The audit used the following source rules:

- Nursery Bed: `.bbmodel` is authoritative and exports deterministically.
- Estate workstations: shared geometry Python is authoritative; the generated
  connected Blockbench assembly is the textured three-block review artifact.
- Trellis and all large wood/state matrices: deterministic generators are
  authoritative; Blockbench files are checked review companions.
- Simple cubes, parent-only models and sprites do not gain redundant
  `.bbmodel` files.

Classification totals before remediation: A 0, B 2, C 0, D 0, E 19, F 7.

Classification totals after remediation: A 1, B 0, C 0, D 0, E 20, F 7.

The two B findings were source-hardening defects, not visible shipping-model
defects: the Nursery README incorrectly described a populated item source, and
the trellis README described the review companions as independently
exportable runtime sources. Both are corrected and now enforced by
`scripts/audit_blockbench_models.py`.

## Authoritative inventory

Abbreviations: BE = block-entity renderer; W = wood families; shape ownership
names the Java class when the visible model is not a full-cube default.

| # | Family | Registry IDs and state coverage | Source / renderer / item representation | W / shape / dependencies | Before -> after | Finding and action |
|---:|---|---|---|---|---|---|
| 1 | Trellis | 13 blocks; 21 multipart rules per wood; isolated, level, end, slope and junction presentations | `generate_wood_variants.py`; geometry review in `blockbench/trellis/trellis_single.bbmodel` and `wire_level.bbmodel`; block-model item | 13W; dynamic outline/collision in `TrellisBlock`; row connections, above/below and slope | B -> E | Geometry is consistent. Clarified generator authority and added source/runtime drift checks. |
| 2 | Red grapevines | 13 registered wood-preserving blocks; 53 multipart rules and 29 model targets per wood | `generate_wood_variants.py`; no BE; no BlockItem | 13W; `GrapevineBlock`; age, upper/lower, cultivar, row and slope state | E -> E | Healthy generated cross/trellis composition; young-wire and slope coverage already audited. |
| 3 | White grapevines | 13 registered wood-preserving blocks; 53 multipart rules and 29 model targets per wood | Same pipeline as red grapevines | 13W; `GrapevineBlock`; same dependencies | E -> E | Healthy and structurally matched to red family. |
| 4 | Cultivar grape/cutting presentations | 12 cultivars mapped through four registered base grape/cutting items; 24 generated item presentations | `generate_wood_variants.py`; generated item sprites/cross parents | n/a; item-only; cultivar component selects presentation | E -> E | Deterministic palette generation is appropriate; no 3D source justified. |
| 5 | Vineyard Soil | 1 block/item; one blockstate variant | simple block model and block-model item | n/a; normal full block; moist/soil behavior does not change model | F -> F | Simple cube is appropriate. |
| 6 | Nursery Bed | 1 block/item; frame plus 16 occupied mappings across four ages/four plant types | authoritative `nursery_bed.bbmodel` and empty `nursery_bed_item.bbmodel`; deterministic exporter | fixed oak; 16x5x16 shape in `NurseryBedBlock`; occupied, plant and age | B -> A | 58-element grouped source and empty 10-element item are correct. Fixed stale source guidance; existing exporter/audit remain authoritative. |
| 7 | Vineyard Netting | 1 item; installed state stored in vineyard saved data, not blockstate | generated flat sprite item; no world model/BE | n/a; no independent collision; saved-data-only visual dependency | F -> F | Item is readable. Installed vines are not visually distinct; a world visual would require new synchronized/render state and is deferred rather than hidden in model JSON. |
| 8 | Grape Bowl | 1 custom block item; 60 cultivar/serving states, five serving levels and eight palettes | `generate_serving_decor_assets.py`; item component selects exact model | fixed oak; `GrapeBowlBlock` 2.7..13.3 x 0..3.5; cultivar and servings | E -> E | One connected, varied grape bunch; closed bowl seams; stable eating order and floor contact are enforced. |
| 9 | Grape Press | 13 blocks/items; 16 multipart rules and seven targets per wood | `generate_wood_variants.py`; block-model items | 13W; default block interaction envelope; facing and fill/press state | E -> E | Handcrafted 13-element base remains proportionate and generator-authoritative. |
| 10 | Fermentation Barrel | 13 blocks/items; 17 multipart rules and five targets per wood | `generate_wood_variants.py`; block-model items | 13W; support shape in `FermentationBarrelBlock`; facing, must style and process status | E -> E | Cask form, hoops and status overlays are consistent. |
| 11 | Aging Barrel | 13 public base blocks/items; 32 multipart rules and eight targets per wood | `generate_wood_variants.py`; block-model items | 13W; support shape in `AgingBarrelBlock`; facing, vessel and fill state | E -> E | Base barrel is consistent across woods and Poplar. |
| 12 | Upgrade-derived aging forms | Toasted, Seasoned and Cellar Cask visual models selected by the base aging block; fixed compatibility IDs `chestnut_aging_barrel`, `neutral_aging_barrel`, `large_cask` | `generate_wood_variants.py`; no separate 13-family public registrations | derived textures/forms; `AgingBarrelBlock`; vessel component/state | E -> E | Intentional upgrade-derived structure preserved; no erroneous Poplar registry expansion. |
| 13 | Barrel Stand | 13 blocks/items; four facing bases per wood | `generate_wood_variants.py`; block-model item | 13W; `BarrelStandBlock`; facing | E -> E | 12-element support makes continuous barrel contact and remains decoratively useful. |
| 14 | Wine Rack | 13 blocks/items; four facing bases; four synchronized bottle slots | `generate_wood_variants.py`; `WineStorageRenderer`; block-model item | 13W; directional shape in `WineRackBlock`; facing and exact slot contents | E -> E | Frame and horizontal bottle centres are stable; renderer preserves exact bottle identity and targeting. |
| 15 | Wine Crate | 13 blocks/items; four facing bases; 16 synchronized bottle slots | `generate_wood_variants.py`; `WineStorageRenderer`; block-model item | 13W; low shape in `WineCrateBlock`; facing and exact slots | E -> E | 27-element crate and vertical 4x4 layout are aligned and stackable. |
| 16 | Labelled Cellar Shelf | 13 blocks/items; 72 multipart rules; eight synchronized bottle slots | `generate_wood_variants.py`; `WineStorageRenderer`; flattened base+glass item | 13W; `CellarCollectionBlock`; facing, glass and exact slots | E -> E | Shelf, labels, glass overlays and bottle centres are coherent. |
| 17 | Tasting Cabinet | 13 blocks/items; 72 multipart rules; eight synchronized bottle slots | `generate_wood_variants.py`; `WineStorageRenderer`; flattened base+glass item | 13W; `CellarCollectionBlock`; facing, glass and exact slots | E -> E | Cabinet silhouette and serving/storage presentation are healthy. |
| 18 | Vintage Archive | 13 blocks/items; 12 multipart rules and three targets per wood | shared `estate_workstation_geometry.py` plus `generate_wood_variants.py`; block-model item | 13W; directional shape in `VintageArchiveBlock`; facing and left/right connections | E -> E | 39-element cabinet/storage body shares the workstation profile and joins flush. |
| 19 | Wine Basket | 13 blocks/items; eight facing/occupancy states; four bottle appearances | `generate_serving_decor_assets.py`; `WineBasketRenderer`; item uses exact block model | 13W; custom shallow shape in `WineBasketBlock`; facing and exact stored stack | E -> E | Accepted 30-element basket is grounded and intersection-free; approved bottle pose has only two measured rim contacts. |
| 20 | Tasting Service | 13 blocks/items; 116 multipart rules and 29 target models per wood | `generate_tasting_service_models.py` and wood generator; state-driven block models | 13W; tray+bottle shape in `TastingServiceBlock`; facing, linen, bottle and four cups | E -> E | Full combinatorial family is deterministic; bottle/cup/tray contact and Pick Block behavior remain unchanged. |
| 21 | Wine Bottle family | one placed block without BlockItem; four registered wine items; 80 placed-state variants and 20 target models | canonical bottle generator in `generate_wood_variants.py`; items reuse full bottle models; storage/basket BEs select appearance | n/a; narrow `WineBottleBlock` shape; facing, fill, red/white and aged/ordinary | E -> E | Dark olive-green body is shared; burgundy, straw-gold and restrained gold accents preserve readable styles. |
| 22 | Estate Management Desk | 13 blocks/items; 80 multipart rules and 20 targets per wood | shared `estate_workstation_geometry.py`, desk generator, generated connected Blockbench assembly, live-map BE renderer | 13W; `EstateManagementDeskBlock`; facing, connection, blotter, ledger and map | E -> E | Resolved 29-element base uses the common 10.25..11.5 desktop; four connection profiles meet mixed neighbours flush. |
| 23 | Surveyor's Map Table | 13 blocks/items; 16 multipart rules and four targets per wood | shared workstation geometry plus table generator; connected Blockbench assembly | 13W; `SurveyorsMapTableBlock`; facing, connection and maps | E -> E | 25-element table shares height/supports with Desk/Archive and retains a distinct map surface. |
| 24 | Must and grape ingredient sprites | red/white must, base grapes, rootstock/resistant cuttings | generated/hand-authored flat sprite parents | n/a; item-only | F -> F | Flat items remain clearer than unnecessary 3D models. |
| 25 | Specialist tools | Soil Probe, Grafting Knife and Cooper's Mallet | flat generated item models and custom PNGs | n/a; item logic only | F -> F | GUI/held scale follows vanilla sprite transforms; 3D conversion is not justified. |
| 26 | Cooperage kits | Toasting, Seasoning and Cask Conversion kits | flat generated item models and custom PNGs | n/a; upgrade recipes/components | F -> F | Simple icons correctly communicate upgrade inputs. |
| 27 | Vintner Almanac | 1 item | flat generated item model and custom PNG | n/a; report/UI behavior only | F -> F | Book-like sprite is appropriate; no world geometry. |
| 28 | Compost | 1 item | flat generated item model and custom PNG | n/a; item behavior only | F -> F | Simple ingredient presentation is appropriate. |

## Cross-family results

- All 14 public wood families (Trellis plus 13 specialist block families) have
  identical resolved geometry, rotation, pivot, UV and item-display structure
  across the authoritative 13-wood order. Only texture mappings differ.
- Pale Oak, Poplar, Bamboo, Crimson and Warped wrappers resolve through the
  same geometry and explicitly bind their family plank/stem/log textures.
- The three estate modules share the same leg, foot, stretcher, desktop,
  moulding, rear rail and crown profile. Left/right connection pieces fill only
  the intended edge inset, so same-facing mixed-wood rows have no model gap.
- Rendered bottle layouts are synchronized per slot for racks, crates, shelves
  and cabinets. Model JSON does not duplicate renderer bottles.
- Collision remains gameplay-owned. No collision, capacity, targeting,
  registry, component, save or interaction logic changed in this audit.
- Specialist blocks use `noOcclusion`/custom shapes where required and do not
  opt into vanilla fence, wall or pane connection behavior.
- No C or D finding was created: every substantial retained model is either
  backed by a maintainable Blockbench source or is intentionally generated.

## Blockbench inspection record

Representative accepted models were opened in Blockbench, including the
30-element Poplar Wine Basket and resolved 29-element Poplar Estate Desk. The
connected 109-element workstation assembly was regenerated from the Minecraft
26.3 client texture archive and was byte-clean. Nursery and trellis sources
were reopened structurally by their audit pipeline. Standalone generated JSON
shows missing preview textures in Blockbench when it cannot locate vanilla
assets; packaged texture references are valid and the textured workstation
review assembly embeds the ten required preview textures.

## Deferred, non-blocking visual backlog

- Installed vineyard netting has no distinct world model because its state is
  stored outside the blockstate/render model. Adding one requires synchronized
  render state or a saved-state architecture change and is post-1.4 work.
- Subjective shader/no-shader appearance, hand feel and decorative adjacency
  remain human checks in the companion manual checklist.
