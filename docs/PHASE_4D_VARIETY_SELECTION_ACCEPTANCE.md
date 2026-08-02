# Phase 4D — Site-led grape variety selection

## Player-facing behaviour

- The Soil Probe and Vintner's Almanac now compare red- and white-grape fit
  when surveying vineyard land.
- Reports name the recommended variety and show a readable fit score out of
  100 for both choices.
- Red grapes favour warmer sites with strong sun, drainage, mineral character,
  heat retention, and low frost risk.
- White grapes favour cooler sites with balanced drainage, water retention,
  root depth, limited heat stress, and shelter from excessive wind.
- The planted variety now influences the vineyard contribution to grape
  quality. Choosing the better-suited variety helps a site express its
  potential, while a less suitable choice remains viable rather than becoming
  unusable.
- Ordinary item tooltips remain concise; detailed site guidance stays in the
  field instruments.
- Inspecting a vine with the Almanac gives one prioritized management action:
  prepare its soil, irrigate during drought, protect it from damaging weather,
  wait for ripeness, or harvest. It avoids another wall of simultaneous tips.

## Balance boundaries

- Variety fit adjusts only the existing terroir contribution. It does not add
  a new quality category or exceed the established 28-point terroir cap.
- Raw site quality remains the dominant factor: 75 percent of the adjusted
  site score comes from the surveyed site and 25 percent from variety fit.
- Recommendations are deterministic for the same climate, soil, and terrain.

## Automated acceptance

- A warm, sunny volcanic test site recommends red grapes.
- A cooler version of the same site recommends white grapes.
- The recommended red variety earns more vineyard points than white on the
  warm test site.
- Management advice prioritizes soil preparation before weather mitigation and
  selects irrigation, shelter, or harvest guidance for the relevant state.
