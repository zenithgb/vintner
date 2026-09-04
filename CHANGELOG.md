# Changelog

All notable changes to Vintner are documented in this file.

## [Unreleased]

### Added

- Eight focused grape cultivars with distinct names, cutting and fruit icons,
  planted-vine palettes, site preferences, yields, resistance, wine styles,
  benefits, and ageing potential.
- Vine age, grafting, nursery propagation, strategic rootstocks, vineyard
  netting, and balanced, quality-focused, or high-yield management modes.
- Seasonal vineyard weather and pressure from drought, frost, heat, rot,
  mildew, pests, birds, and nutrient imbalance, with physical counterplay.
- Estate registration, named vineyard plots, a persistent activity ledger,
  physical facility recognition, reputation progression, and irrigation
  reporting.
- Estate Management Desks and Surveyor's Map Tables for all twelve wood
  families, with a server-authored management screen and interactive atlas.
- Read-only regional market and buyer guidance for planning future vintages.

### Changed

- The Vintner's Almanac now provides contextual vineyard, estate, appraisal,
  and market reports while the Estate Management Desk supplies the broader
  fixed-location overview.
- Vineyard and management recipes participate in the complete recipe-book and
  release-asset validation gates.

## [1.3.0] - 2026-09-02

### Added

- Placeable red, white, and aged wine bottles that retain their vintage,
  quality, provenance, age, bottle number, and remaining contents.
- Four persistent servings per bottle with visible partial-fill states.
- Four-cup Tasting Services with independently selectable servings, variants
  for all twelve supported wood families, and all sixteen vanilla linen dyes.
- `A Proper Pour` and `To Good Company` advancements for serving and sharing
  wine.
- Almanac reporting for remaining bottle and Tasting Service servings.

### Changed

- Directly drinking a partial bottle now scales its effects to the remaining
  contents.
- Tasting Service pours apply one-quarter of the bottle's normal effects, so
  four servings equal one full bottle.
- Placed bottles and bottles displayed in racks, crates, cellar shelves, and
  tasting cabinets now share a consistent visual language.
- Empty bottles can be recovered after the final serving.
- Detailed serving and provenance information remains in the Almanac while
  normal bottle tooltips stay concise.

### Fixed

- Individual Tasting Service cups can be selected and consumed in any order
  from every horizontal facing without emptying a neighboring cup.
- Remaining servings and exact cup state persist through placement,
  retrieval, save/reload, and chunk unloading.
- Server-authoritative final-serving handling prevents duplicate pours or
  drinks during competing interactions.

## [1.2.0] - 2026-08-12

### Added

- Persistent wine provenance from pressing through fermentation, barrel
  ageing, and bottling, including grape variety, batch day, winery origin, and
  producer without preventing grapes from different vines from stacking.
- Almanac reports for provenance, bottling day, and a concise hold, drink-now,
  past-peak, or spoiled recommendation.
- Vintage Archives that catalogue up to sixteen unique wine batches without
  consuming their bottles, update existing batch records when rescanned, and
  retain their catalogue when moved in Survival, and support comparator output.
- Vintage Archive variants, recipes, loot tables, item presentation, and
  recipe unlocks for all twelve supported wood families.
- Chestnut and neutral ageing barrels plus an eight-bottle large cask, each
  with distinct capacity, speed, quality, style affinity, and tasting character.
- Barrel stands, single-batch Labelled Cellar Shelves, and mixed-vintage
  Tasting Cabinets in all twelve supported wood families.
- Persistent wine style and estate identity, fuller body-aware tasting notes,
  estimated trade value, and cellar prestige in Almanac reports.
- Cellar evaluation for temperature stability and nearby machinery
  disturbance in addition to shelter, depth, light, humidity, and heat.

### Changed

- Wine quality now also controls pairing strength, low-quality fault risk,
  estimated value, and future settlement-prestige contribution.
- Labelled shelves and tasting cabinets age their stored bottles, expose exact
  visual fill states, retain metadata on retrieval and save/load, and provide
  comparator output.

## [1.1.0] - 2026-08-07

### Added

- Stable wine batch identities that survive pressing, fermentation, barrel
  aging, bottling, storage, and save/reload cycles.
- Numbered bottles within each fermentation or aging batch, with extraction
  order preserved across barrel save/reload cycles.
- Deterministic tasting profiles and an opt-in Vintner's Almanac, keeping
  detailed wine information out of normal item tooltips.
- Bottle aging stages from young through peak and decline, with storage
  quality affecting aging speed and poor storage eventually spoiling wine.
- Cumulative bottle storage histories by cellar rating, summarized by the
  Vintner's Almanac.
- Cellar evaluation based on shelter, depth, light, nearby water, and heat.
- UI-free wine racks with four visible bottle positions, comparator output,
  safe bottle drops, and variants for all twelve supported wood families.
- Cellar progression for building a rack, inspecting a vintage, and discovering
  ideal storage conditions.
- A persistent 0–100 wine-quality profile with the six roadmap tiers: Rough,
  Table, Good, Fine, Exceptional, and Legendary.
- Inspectable vineyard, pressing, fermentation, ageing, and cellar quality
  contributions in the Vintner's Almanac.

### Changed

- Bottle age now adjusts wine benefit duration; peak bottles are strongest,
  while spoiled bottles cause nausea.
- Stored bottles now catch up on elapsed world time after their chunk unloads,
  while still respecting the rack's current cellar conditions.
- Identified must and wine batches can no longer be silently blended with a
  different identified batch.
- Fermentation barrels now carry a compact capped airlock, distinguishing them
  from copper-banded aging barrels without exceeding one block.
- Wine racks now use a lighter open-frame design with slimmer stored bottles.
- Numbered wines now show their bottle position as one compact tooltip line;
  detailed batch, tasting, age, and cellar data remains in the Almanac.
- Wine racks now align cleanly when placed side by side or stacked, with
  larger visible bottles.
- Wine quality now accumulates through the full production chain instead of
  being copied unchanged and automatically raised by one tier.
- Existing Common, Fine, and Exceptional bottle and barrel data migrates to
  stable scored profiles without losing its previous quality meaning.

### Fixed

- Fermentation and ageing now wait for a complete four-bottle batch before
  progress, active visuals, and processing effects begin.
- Wine rack inventory models now use Minecraft's standard angled block-item
  presentation, making every wood variant easier to identify.
- Cellar humidity now requires actual adjacent water instead of incorrectly
  treating dry neighboring blocks as humid.
- The Vintner's Almanac recipe now unlocks after obtaining either grape variety.
- Empty-hand wine-rack interactions now return the latest stored bottle.
- Breaking a filled wine rack in Creative mode now drops its bottles with their
  wine metadata intact.
- Trellis end braces are inset from the post to prevent coplanar face clipping.
- Trellis end braces now reach the ground cleanly, and young vines retain both
  halves of a two-trellis wire until their upper growth takes over.

## [1.0.1] - 2026-07-23

### Fixed

- Two-block trellis rows now render wires and end braces only on the correct
  level, without partial connections to one-block rows.
- Grapevines now use compact selection outlines while retaining the trellis
  structure for collision.
- Breaking either half of a grapevine removes only the vine, restores the
  supporting trellises, and drops the matching grape cutting.

## [1.0.0] - 2026-07-22

### Added

- Red and white grape cultivars with two-block growth, mature canopy variants,
  harvesting, regrowth, and renewable cuttings.
- Fence-style oak trellises with four-way connections, isolated placement,
  vertical rows, collision shapes, and automatic end braces.
- Prepared vineyard soil, compost preparation, and vineyard condition
  inspection.
- Grape vintage and quality evaluation based on vineyard conditions.
- A manual grape press with visible contents, batch validation, bottling, and
  comparator output.
- Fermentation and aging barrels with persistent batches, progress feedback,
  visual states, comparator output, and safe content drops.
- Red, white, and aged wines with quality-sensitive effects.
- Survival access to grape cuttings through farmer trades, wandering traders,
  village house loot, and mature-vine pruning.
- A complete Vintner advancement path from the first cutting to aged wine.
- Custom vineyard, grape, must, wine, machine, and creative-tab presentation.
- An automated Fabric GameTest suite covering trellises, vines, machines,
  metadata, loot, pruning, harvesting, and serialization.

### Fixed

- Winemaking machine model overlap, hoop flickering, world tearing, and item
  display scale.
- Trellis phantom geometry, obsolete slope models, stacked-row updates, and
  stale neighbor connections.
- Repeating advancement rewards and missing advancement notifications.
- Final grape batches now preserve their vintage and quality when pressed.
