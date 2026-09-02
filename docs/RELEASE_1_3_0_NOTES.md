# Vintner 1.3.0 — Wine at the Table

Vintner 1.3.0 brings finished wine out of the cellar and onto the table. Wine
bottles can now be placed in the world, opened gradually, shared through a
four-cup Tasting Service, and recovered without losing their identity.

## Added

- Placeable red, white, and aged wine bottles.
- Four persistent servings per bottle, with visible partial-fill states.
- Tasting Services with four independently selectable cups.
- Tasting Service variants for all twelve supported wood families.
- Linen recolouring with all sixteen vanilla dyes.
- `A Proper Pour` and `To Good Company` advancements.
- Almanac reporting for remaining bottle and service servings.

## Changed

- Directly drinking a partial bottle now scales its effects to the remaining
  contents.
- Tasting Service pours apply one-quarter of the source bottle's normal wine
  effects; four servings equal one full bottle.
- Placed and stored bottles now share one consistent visual language across
  racks, crates, cellar shelves, and tasting cabinets.
- Empty bottles can be recovered after the final serving.
- Normal tooltips remain concise while detailed wine information stays in the
  Vintner's Almanac.

## Persistence and multiplayer

- Remaining servings, vintage, batch, bottle number, provenance, quality,
  ageing, and storage history survive placement, retrieval, save/reload, and
  chunk unloading.
- Bottle placement, pouring, individual cup selection, and the final serving
  are server-authoritative and protected against duplicate interactions.
- Existing bottles without serving data safely begin with four servings.

## Compatibility

- Minecraft 26.2
- Fabric Loader 0.19.3
- Fabric API 0.155.2+26.2
- Required on both client and server

Vintner remains under active development. Mechanics, balance, models, and
compatibility may continue to evolve in future releases.
