# Phase 8C — Regional Markets

Wine inspection now derives a local market from the player's real climate and
terrain. The Vintner's Almanac compares the held bottle with that regional
buyer profile and reports local demand and a local potential value.

## Region rules

- cold and cool climates use cold-region demand;
- temperate or warm sites beside water use coastal demand;
- high-elevation and steep inland sites use mining-settlement demand;
- ordinary lowland sites use agricultural-village demand.

Climate, water distance, elevation, and slope all come from the existing live
terroir evaluator. Moving to a different landscape can therefore change the
same bottle's local outlook without rewriting its metadata.

## Manual acceptance

1. Inspect a good young white wine beside water in a temperate or warm biome.
   The Almanac should report a coastal settlement and a positive demand
   premium.
2. Inspect the same bottle in a high inland location. The local market should
   change to a mining settlement and the white wine premium should fall.
3. Inspect a red wine in the high inland location. Mining demand should value
   it more highly than the equivalent white wine.
4. Inspect a bottle in a cold or cool biome. Cold-region demand should take
   priority even if the site is mountainous or beside water.

## Deliberately deferred

The local value is an Almanac market signal, not yet a guaranteed merchant
offer. Persisted settlement economies, named trade partners, contracts, trade
routes, stock levels, and time-varying market events remain later Phase 8
milestones.
