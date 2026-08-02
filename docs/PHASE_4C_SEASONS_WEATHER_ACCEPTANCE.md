# Phase 4C — Native seasons and vineyard weather

## Player-facing behaviour

- Vintner now has a four-season vineyard calendar: spring, summer, autumn,
  and winter.
- A season lasts 8 Minecraft days by default. World owners can change it with
  `/gamerule vintner:season_length_days <1-96>`.
- Spring growth is faster, summer growth is normal, autumn growth is slower,
  and vines are dormant in winter. Dormancy never removes or damages a vine.
- Each 128-by-128-block vineyard region receives a stable seasonal outlook.
  Possible outcomes are settled weather, ideal conditions, cool ripening,
  late frost, heatwave, heavy rain, drought, and hail risk.
- Weather affects the harvest-weather portion of grape quality. It never
  destroys a vineyard, replacing destructive simulation with recoverable
  vintage variation.
- Glass placed two to six blocks above a vine counts as protected cultivation.
  It permits slow winter growth and shelters the crop from late frost, hail,
  and heavy rain. It does not remove heatwave or drought pressure.
- Use the Vintner's Almanac on vineyard land or a grapevine to see the current
  season, vintage year, day within the season, local outlook, and its current
  harvest-quality contribution.

## Automated acceptance

- Calendar boundaries respect the configurable season length.
- Four seasons roll into a new vintage year.
- Winter dormancy blocks growth without mutating vine blocks.
- Protected vines can grow slowly in winter and mitigate only shelter-relevant
  weather events.
- Seasonal outlooks are deterministic for the same world, region, year, and
  season.
- All weather outcomes stay within the existing 0–7 harvest-weather budget,
  retaining the established 60-point maximum vineyard contribution.

## Compatibility boundary

This milestone is the native fallback requested by the roadmap and has no hard
dependency on another season mod. A Serene Seasons adapter remains optional and
must only be enabled when a compatible Minecraft 26.2 API is available; Vintner
continues to run standalone meanwhile.
