# Trellis model sources

These Blockbench projects are geometry-review companions for the deterministic
trellis generator in `scripts/generate_wood_variants.py`. The generator is the
runtime source of truth; `scripts/audit_blockbench_models.py` rejects geometry
drift between these review files and the generated oak post/wire models.

The tested runtime geometry uses:

- a centered post from `[7, 0, 7]` to `[9, 16, 9]`;
- four wire elements at Y 4, 7, 10, and 13;
- wire depth centered on Z 8, from 7.5 to 8.5.

The review projects intentionally omit embedded vanilla texture bytes. Inspect
the geometry in Blockbench, but do not export these files directly over runtime
assets. The generator binds wire faces to `minecraft:block/anvil`, particles to
`minecraft:block/oak_planks`, and wood-family posts/braces to their family
textures without duplicating vanilla PNGs in the repository.
