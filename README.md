# Vintner

Vintner is a Fabric mod for Minecraft 26.2 focused on vineyards, grape
cultivation, winemaking, ageing, and wine quality.

## Vineyard loop

1. Acquire a red or white grape cutting from a farmer, wandering trader, or
   village house chest.
2. Craft oak trellises from four sticks and one iron chain. The recipe produces
   two trellises.
3. Build rows from two-block-high trellises. Adjacent trellises connect with
   fence-style wires.
4. Use the cutting on the lower trellis to plant a grapevine.
5. Shift-use compost on a trellis or grapevine to prepare suitable ground
   directly beneath it.
6. Let the vine grow into the upper trellis, then harvest its mature upper
   canopy.
7. Press the grapes into must, ferment the must, and age the resulting wine.

The Vintner advancement tab follows this same progression from the first
cutting through a finished aged bottle.

Harvested grapes are fruit, not planting material. Mature vines can be pruned
with shears to produce renewable cuttings; pruning returns the vine to its
previous growth stage so it can regrow.

## Winemaking

The grape press accepts one matching grape at a time and converts four grapes
into one bottle of must. Use the press with an empty hand to press a complete
batch, then use a glass bottle to collect the must.

The fermentation barrel holds four matching bottles of must. Fermentation
begins once the barrel is full and takes one minute; use an empty hand to
inspect progress or collect finished wine.

The aging barrel holds four matching bottles of wine. Aging begins once the
barrel is full, takes ninety seconds, records an ageing-quality contribution,
and produces aged wine.

Wine racks hold four bottles without opening an inventory screen. Bottles age
while stored, and the surrounding build determines cellar quality: sheltered,
dark, underground spaces with nearby water age wine best, while nearby heat
damages it. Stored bottles catch up on elapsed world time when their chunk is
loaded again. Wine racks, trellises, presses, both barrel types, and Vintage
Archives are available in every supported vanilla wood family.

Craft a Vintner's Almanac from a book and both grape varieties. Use it while
holding wine in the other hand to inspect the bottle's vintage, batch, tasting
profile, quality score and production breakdown, numbered place in its batch,
age, cumulative cellar history, batch provenance, bottling day, and whether
the bottle should be held or enjoyed now. Provenance is established when a
batch is pressed so grapes from matching vines remain stackable. Use the
Almanac directly on a wine rack to
inspect the cellar around that rack. Normal wine tooltips remain intentionally
compact. Cellar advancements guide players from laying down their first rack
to identifying a vintage and discovering ideal storage conditions.

Craft a Vintage Archive to build a physical record of a cellar collection.
Use a wine bottle on the Archive to catalogue its current metadata without
consuming it. Rescanning the same batch updates its entry rather than using
another slot. Use the Almanac on the Archive to cycle through its sixteen
records, or use an empty hand for a collection summary.

Trellises connect independently on all four horizontal faces. Vertically
stacked trellises form separate same-height rows; they do not create diagonal
wires between elevations. Shift-placement creates an isolated trellis that
does not connect to its neighbours.

Shift-use an established grapevine with an empty hand to inspect its vineyard
conditions and predicted wine quality.

Use the grape press, fermentation barrel, or aging barrel with an empty hand
to inspect its contents and progress. Machine interactions also report full,
incompatible, or unfinished batches in the action bar.

Wine quality is scored from 0–100 across vineyard conditions, pressing,
fermentation, barrel ageing, and bottle storage. The score produces six tiers:
Rough, Table, Good, Fine, Exceptional, and Legendary. Higher tiers last longer,
have stronger signature effects, and retain their character for longer in the
cellar. Ideal storage can gradually improve a strong bottle, while poor storage
progressively reduces its quality and can eventually spoil it.

Starter cuttings remain renewable and accessible:

- Farmers and wandering traders sell both varieties.
- Village house chests have a chance to contain one variety.
- Established mature vines can be pruned with shears.

All three winemaking machines support comparator output. The grape press
reports its stored must level. Fermentation and aging barrels progress from
signal strength 1 to 14 while working, output 15 when ready, and output 0 when
empty.

## Development

Build and verify the mod:

```bash
./gradlew clean build
./gradlew runGameTest
```

Launch the development client:

```bash
./gradlew runClient
```

Editable trellis geometry lives in `blockbench/trellis/`. Runtime models live
in `src/main/resources/assets/vintner/models/block/trellis/`.

The distributable JAR is written to `build/libs/`.

## License

MIT
