# Connected estate workstations

Open `connected_estate_workstations.bbmodel` in Blockbench to inspect and edit
the three oak modules at their actual 16-pixel block spacing. The archive,
management desk and map table have separate named groups. Vanilla textures
are embedded for an immediately usable local preview. This is a free-model
assembly because a three-block row exceeds a single Java block export's bounds.

The source of truth for runtime geometry is
`scripts/estate_workstation_geometry.py`, reused by the wood-family, estate-desk
and surveyor-table generators. The common desktop is Y=10.25..11.5,
Z=1..15; its standalone X=.5..15.5 inset is filled at connected ends. Front
moulding, rear rail/crown and leg/foot/stretcher positions are shared. Cabinet
storage and stationery remain specific to each workstation.

After a Blockbench design adjustment, transfer the intended dimensions back to
the shared geometry or the family accessory generator, then regenerate in this
order (the wood-family pass also normalizes shared loot/advancement resources):

```sh
python3 scripts/generate_estate_desk_assets.py
python3 scripts/generate_surveyors_map_table_assets.py
python3 scripts/generate_wood_variants.py
python3 scripts/audit_release_assets.py
python3 scripts/generate_workstation_blockbench.py /path/to/minecraft-client.jar
```

Do not export the complete assembly over an individual runtime block model.
Connection blockstates and gameplay/data are independent of this visual source.
The preview shows the default crafted surfaces; optional maps, blotter colours,
and ledger overlays still come from their existing runtime generators.
