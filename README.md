# Vintner

Vintner is a Fabric mod for Minecraft 26.2 focused on vineyards, grape
cultivation, winemaking, ageing, and wine quality.

## Vineyard loop

1. Craft oak trellises from four sticks and one chain. The recipe produces two
   trellises.
2. Place trellises beside one another to create fence-style wire connections.
3. Use red or white grapes on a trellis to plant a grapevine.
4. Shift-use compost on a trellis or grapevine to prepare suitable soil
   directly beneath it.
5. Grow and harvest grapes, press them into must, ferment the must, and age the
   resulting wine.

Trellises connect independently on all four horizontal faces. Vertically
stacked trellises form separate same-height rows; they do not create diagonal
wires between elevations. Shift-placement creates an isolated trellis that
does not connect to its neighbours.

Shift-use an established grapevine with an empty hand to inspect its vineyard
conditions and predicted wine quality.

## Development

Build and verify the mod:

```bash
./gradlew clean build
```

Launch the development client:

```bash
./gradlew runClient
```

Editable trellis geometry lives in `blockbench/trellis/`. Runtime models live
in `src/main/resources/assets/vintner/models/block/trellis/`.

## License

MIT
