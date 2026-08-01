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

Ageing vessels begin only when full and produce wine with a recorded vessel
identity. Ordinary barrels hold four bottles and provide a balanced starting
profile. Toasted barrels work faster and favour bold red wine, seasoned
barrels preserve primary fruit character, and cellar casks hold eight bottles
for slower, gentler ageing.

The ordinary ageing barrel is available in every supported wood family, but
those wood choices are cosmetic and share the balanced standard profile. Once
that loop is familiar, any ordinary barrel can become one of three clearly
signposted specialist choices. Craft a reusable Cooper's Mallet and a
cooperage treatment kit, hold one in each hand, then use either item on an
empty placed barrel. The barrel keeps its original wood appearance, the kit
is consumed, and the mallet uses one of its 64 durability points:

- A Toasting Kit creates fast, bold, red-focused ageing with a little more
  risk.
- A Seasoning Kit creates slow, gentle, fruit-preserving ageing with very low
  risk.
- A Cask Conversion Kit creates an eight-bottle vessel for slow bulk
  maturation.

Sneak-using the mallet on an empty specialised barrel removes the treatment
and returns its kit. Breaking a specialised ordinary barrel also returns both
its wood-family barrel and the applied kit. The mallet can rotate Vintner
presses, barrels, stands, racks, crates, archives, shelves, and cabinets by
right-clicking them. Older standalone Toasted, Seasoned, and Cellar Cask
blocks remain loadable for existing worlds, but new cellars use this treatment
system.

These are parallel cellar strategies rather than simple power tiers. Use the
Vintner's Almanac directly on any placed ageing vessel to compare its role,
capacity, ageing time, wine affinity, risk, and recipe before committing a
batch.

Wine racks hold four bottles without opening an inventory screen. Bottles age
while stored, and the surrounding build determines cellar quality: sheltered,
dark, underground spaces with nearby water age wine best, while nearby heat
damages it. Stored bottles catch up on elapsed world time when their chunk is
loaded again. Wine racks, trellises, presses, both barrel types, and Vintage
Archives are available in every supported vanilla wood family.

Wine crates provide dense, UI-free cellar storage for up to sixteen mixed
bottles. Use a bottle on a crate to store it and use an empty hand to retrieve
the most recently stored bottle. Every bottle keeps its batch, numbering,
quality, provenance, and age metadata. Crated bottles continue ageing under
the surrounding cellar conditions, while the Almanac reports crate capacity
and cellar quality at a glance. Wine crates are available in every supported
vanilla wood family.

Barrel stands provide low-profile cellar staging. Labelled Cellar Shelves hold
eight bottles from one batch, making complete vintages easy to organise.
Tasting Cabinets hold eight mixed bottles for comparison. Both fixtures show
their exact bottle count, age bottles under the surrounding cellar conditions,
support comparator output, preserve metadata, and are available in every
supported wood family.

Craft a Vintner's Almanac from a book and both grape varieties. Use it while
holding wine in the other hand to inspect the bottle's vintage, batch, tasting
profile, body and finish, wine style, estate, quality score and production
breakdown, numbered place in its batch, age, cumulative cellar history, batch
provenance, ageing vessel, estimated trade value, cellar prestige, bottling
day, and whether the bottle should be held or enjoyed now. Provenance is established when a
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
have stronger signature effects and food pairings, carry higher value and
prestige, have fewer quality faults, and retain their character for longer in
the cellar. Cellars evaluate shelter, depth, darkness, humidity, nearby heat,
temperature stability, and machinery disturbance. Ideal storage can gradually
improve a strong bottle, while poor storage progressively reduces its quality
and can eventually spoil it.

Starter cuttings remain renewable and accessible:

- Farmers and wandering traders sell both varieties.
- Village house chests have a chance to contain one variety.
- Established mature vines can be pruned with shears.

All three winemaking machines support comparator output. The grape press
reports its stored must level. Fermentation and aging barrels progress from
signal strength 1 to 14 while working, output 15 when ready, and output 0 when
empty. Wine racks and wine crates output a signal proportional to the number
of stored bottles.

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
