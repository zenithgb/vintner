# Vintner 1.3.1 — Cellar Crafting Patch

Vintner 1.3.1 is a small recipe-balancing and validation update for the
cellar furniture introduced alongside the 1.3 release line.

## Changed

- Labelled Cellar Shelves now use paper instead of a non-craftable name tag.
- Cask Conversion Kits now accept any vanilla plank type instead of requiring
  spruce planks.
- Barrel Stand recipes now produce two stands across all twelve wood families.

## Quality assurance

- Expanded recipe-unlock GameTest coverage to every Vintner wood family and
  cellar furniture family, plus the Cooper's Mallet and all treatment kits.
- Expanded the release asset audit to validate all 138 recipes, their matching
  unlock advancements, outputs, counts, shaped patterns, and conflicting
  crafting inputs.
- Added missing completeness checks for Tasting Services and the Cooper's
  Mallet.

## Compatibility

- Minecraft 26.2
- Fabric Loader 0.19.3
- Fabric API 0.155.2+26.2
- Required on both client and server

This patch changes recipes only. Existing blocks, items, worlds, wine data,
and cellar inventories remain compatible.
