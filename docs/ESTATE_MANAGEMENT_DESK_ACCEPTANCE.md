# Estate Management Desk acceptance

The Estate Management Desk is the fixed, full-estate counterpart to the
portable Vintner's Almanac. It provides a fresh server-authored overview of an
estate without automating vineyard or cellar work.

## Scope

- Craftable, directional desk block with a bespoke writing-desk model.
- Read-only full-screen interface styled with wood, leather, parchment, ink,
  and brass.
- Overview, Vineyards, Cellar, Markets, Contracts, Ledger, and Map tabs.
- Live estate, vineyard, cellar, reputation, market, and ledger information.
- A nearby Surveyor's Map Table can hold up to nine compatible explored maps
  for the interactive Estate Atlas, including plot selection, pan, and zoom.
- Clear registration guidance when the player has not founded an estate.
- Contracts are an honest destination for the next contract milestone; this
  foundation does not simulate contracts that do not exist yet.

The desk does not replace physical presses, barrels, racks, archives, vineyard
plots, or the Almanac's portable inspection role.

## Manual acceptance

1. Craft the desk with green carpet, a book, a gold nugget, dark oak planks,
   and dark oak slabs.
2. Place it facing north, east, south, and west. Confirm the model, outline,
   collision, and breaking particles follow the block correctly.
3. Use the desk with an empty hand before founding an estate. Confirm the UI
   opens with registration guidance and does not flood chat.
4. Found and name an estate using the existing Almanac and Archive flow.
5. Reopen the desk and confirm all seven tabs are present and readable at the
   current GUI scale.
6. Confirm Overview shows the estate name, founding year, home region,
   reputation, vineyard area, harvest record, cellar count, market, and label.
7. Register multiple plots. Confirm Vineyards lists their dimensions, vines,
   varieties, health, projected yield and quality, and irrigation; test mouse
   wheel scrolling with enough entries to overflow the page.
8. Add and remove nearby aging barrels, barrel stands, storage, tasting
   cabinets, and archives. Reopen the desk and confirm Cellar reflects the
   current physical setup.
9. Move the desk between meaningfully different biomes and confirm Markets
   follows the biome-aware market classification rather than altitude alone.
10. Confirm Contracts clearly reports that there are no active contracts and
    does not invent rewards or progress.
11. Generate estate ledger activity and confirm the newest entries appear on
    Ledger without long chat output.
12. In multiplayer, confirm each player sees only their own estate snapshot.
13. Close and reopen the desk after changing the estate. Confirm the report is
    refreshed rather than cached.
14. Place a Surveyor's Map Table within two blocks, add compatible explored
    maps, and confirm Map displays the atlas. Select plots, pan, zoom, and
    verify that removing or moving the table removes its atlas on refresh.

## Release boundary

Automated asset, build, GameTest, and dedicated-server checks validate the
implementation. The visual layout, scaling, model silhouette, and multiplayer
ownership checks above remain a human in-game acceptance gate.
