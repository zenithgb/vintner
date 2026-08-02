# Phase 5A — Persistent Vine Age Acceptance

This slice begins `1.4.0 — Vineyard Expansion` by separating a vine's
long-lived root age from its short ripening cycle.

## Automated coverage

- Planting dates persist per grapevine root position in world saved data.
- Existing-world vines initialize safely when first observed.
- Age progresses through new, young, mature, old, and ancient stages.
- Young vines favor yield; old and ancient vines favor concentration.
- Vine age stays inside the existing 60-point vineyard-quality budget.
- Removing the vine clears its planting record; normal harvest and pruning do
  not reset it.

## Manual acceptance

1. Plant a cutting on a bare two-block trellis and inspect it with the
   Vintner's Almanac. It should read `New planting` and show zero days.
2. Use `/time add 192000` (eight Minecraft days), then inspect again. It
   should read `Young` without needing to replant or reload the chunk.
3. Save and quit, reload the world, and inspect the same vine. Its day count
   and stage must be preserved.
4. Grow and harvest the vine. The permanent age must remain unchanged while
   the normal growth stage returns to fruit set.
5. Compare young and old/ancient harvests over several cycles. Young vines
   should average more grapes; older vines should contribute more quality.
6. Remove the vine while preserving its trellis, plant a fresh cutting, and
   confirm the replacement begins at `New planting`.

No new tooltip lines are added. Detailed age and tradeoff information belongs
in the Almanac inspection.
