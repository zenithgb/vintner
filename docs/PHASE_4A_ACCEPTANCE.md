# Phase 4A acceptance — Vineyard Site Surveying

This vertical slice begins Phase 4 by making vineyard placement inspectable and
meaningful without changing Vintner's existing 0–60 vineyard-quality budget.

## Implemented scope

- Climate reports estimate temperature band, rainfall, humidity, frost risk,
  heat stress, seasonal variation, and growing-season length from the biome,
  elevation, nearby water, and terrain shape.
- Soil reports derive clay, limestone, chalk, gravel, sand, loam, volcanic, or
  alluvial profiles from nearby vanilla blocks and expose drainage, fertility,
  water retention, heat retention, root depth, and mineral character.
- Terrain reports evaluate elevation, slope, aspect, sunlight, nearby water,
  wind exposure, frost pockets, and simple terracing.
- The Soil Probe gives a concise soil report and has 128 uses.
- The Vintner's Almanac gives a complete site report when used on land,
  trellises, or grapevines.
- Sneak-using an empty hand on a grapevine now gives a concise prediction and
  points players toward the inspection tools for detail.
- Terroir supplies up to 28 vineyard-quality points while the established vine,
  yield, ripeness, and harvest-weather inputs retain the other 32 points.

## Manual test gate

1. Craft a Soil Probe from one copper ingot and two sticks.
2. Probe dirt, sand, gravel, clay, calcite, dripstone, tuff, and mud; confirm
   each reports the expected soil family and consumes durability in Survival.
3. Compare the same soil beside water and away from water; sedimentary ground
   near water should report alluvial conditions.
4. Use the Almanac on flat ground, a south-facing slope, a north-facing slope,
   a hilltop, a hollow, and a terrace; confirm the terrain summaries differ.
5. Compare a cold, temperate, hot, wet, and dry biome; confirm climate bands,
   growing-season length, frost risk, heat stress, and site potential differ.
6. Use the Almanac on the upper and lower halves of one grapevine; both should
   report the same root site.
7. Sneak-use an empty hand on a vine; confirm the compact quality line appears
   instead of the full diagnostic wall of text.
8. Harvest otherwise identical mature vines on strong and weak sites; confirm
   the resulting grape quality differs and remains within the existing quality
   tiers.
9. Confirm the Read the Land advancement triggers only once.

Phase 4A is accepted after the automated suite passes and these comparisons are
visually verified in the development client.
