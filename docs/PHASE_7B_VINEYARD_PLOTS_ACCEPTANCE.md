# Phase 7B — Named Vineyard Plots

Named plots reuse the Vintner's Almanac and its existing land survey. This
keeps plot definition physical and visible in the vineyard without adding a
detached map menu.

## Registering a plot

1. Register an estate at a Vintage Archive.
2. Optionally rename an Almanac in an anvil to a custom plot name, such as
   `Upper Slope` or `River Field`. An unnamed Almanac assigns `Vineyard Plot N`.
3. Sneak-use the Almanac on one corner of suitable vineyard land. A concise
   prompt confirms the first corner and explains the next step.
4. Sneak-use the same Almanac on the opposite corner. The rectangular plot is
   registered and the corner bookmark is cleared.
5. Plots are limited to 32 by 32 blocks and 16 plots per estate. Reusing an
   existing name updates that plot while preserving its original creation day.

## Reading a plot

- Use the Almanac normally on land inside a registered plot.
- The live report includes area, vine count, red/white mix, average vine age,
  soil, climate, current health, projected yield, and projected quality.
- Use the Almanac in the air to see the estate's total plot count, registered
  area, current instructions, and any unfinished first corner.
- Yield and quality are explicitly projections. Historical harvest figures
  belong to the physical Estate Ledger in Phase 7C.

## Manual checks

1. Mark a small empty plot and confirm it reports zero vines.
2. Plant red and white vines inside it and confirm the count and variety mix.
3. Place vines just outside the boundary and confirm they are not counted.
4. Update the same plot name with new corners and confirm the boundary changes
   without increasing the estate plot count.
5. Try a boundary larger than 32 blocks on either axis and confirm rejection.
6. Create and inspect plots at different elevations and near chunk borders.
7. Exit and reload the world, then confirm the estate and named plots remain.
8. Repeat with an unnamed Almanac and confirm the two corners create
   `Vineyard Plot 1` without requiring an anvil.
