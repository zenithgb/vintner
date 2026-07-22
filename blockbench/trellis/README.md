# Trellis model sources

These Blockbench projects are the editable geometry sources for the runtime
models in `src/main/resources/assets/vintner/models/block/trellis/`.

The tested runtime geometry uses:

- a centered post from `[7, 0, 7]` to `[9, 16, 9]`;
- four wire elements at Y 4, 7, 10, and 13;
- wire depth centered on Z 8, from 7.5 to 8.5.

After exporting `wire_level.json`, bind its face texture variable to
`minecraft:block/anvil` and its particle texture to
`minecraft:block/oak_planks`. Blockbench currently has no texture embedded in
the `.bbmodel`, so exporting without this normalization produces unresolved
`#missing` face textures.
