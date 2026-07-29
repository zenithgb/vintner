# Changelog

All notable changes to Vintner are documented in this file.

## [Unreleased]

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

### Changed

- Bottle age now adjusts wine benefit duration; peak bottles are strongest,
  while spoiled bottles cause nausea.
- Stored bottles now catch up on elapsed world time after their chunk unloads,
  while still respecting the rack's current cellar conditions.
- Identified must and wine batches can no longer be silently blended with a
  different identified batch.

### Fixed

- Cellar humidity now requires actual adjacent water instead of incorrectly
  treating dry neighboring blocks as humid.
- The Vintner's Almanac recipe now unlocks after obtaining either grape variety.

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
