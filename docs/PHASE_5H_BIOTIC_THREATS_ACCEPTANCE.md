# Phase 5H — Biotic Vineyard Threats Acceptance

Vintner now models two readable, seasonal vineyard pressures without adding
random crop destruction or background maintenance timers.

## Threats and counters

- **Pest pressure** affects mature, unmanaged canopies during summer.
  Selecting a yield-management strategy with shears represents canopy work
  and prevents the penalty.
- **Bird pressure** affects ripe grapes during autumn. Vineyard Netting protects
  the crop and remains installed until the player removes it.
- Weather, soil, frost, drought, heat, mildew, and rot retain priority when a
  more urgent environmental problem is present.
- Resistant cultivars receive their documented health bonus when the relevant
  threat is active.

The Almanac reports the current prioritized threat, its advice, and whether
bird netting is installed. The ordinary item tooltip remains concise.

## Vineyard netting

- Craft two Vineyard Netting from a two-by-two square of string.
- Use netting on either half of a mature, two-block trained vine to install it.
- Use shears on the upper canopy to recover the netting in Survival.
- The lower trunk remains the shears target for yield-management selection.
- Breaking the vine removes its stored management data, including netting.

Netting is stored against the root position rather than added to every
grapevine blockstate, preserving the existing wood-variant and connected-wire
model system.

## Manual acceptance

- [ ] Vineyard Netting appears in the Vintner creative tab with a valid icon.
- [ ] Its two-by-two string recipe produces two netting items.
- [ ] A young or untrained vine rejects netting with a readable message.
- [ ] A mature two-block vine accepts netting from either the trunk or canopy.
- [ ] Reapplying netting is rejected and does not consume another item.
- [ ] The Almanac changes from `Bird netting: not installed` to `installed`.
- [ ] In autumn, an unnetted ripe vine reports Bird Pressure and recommends
  installing netting.
- [ ] The same vine reports healthy conditions once netted, provided no more
  urgent environmental threat is active.
- [ ] In summer, a mature unmanaged vine reports Pest Pressure.
- [ ] Selecting a yield strategy on the lower trunk clears Pest Pressure.
- [ ] Shearing the upper canopy removes and returns the netting in Survival.
- [ ] Removing netting uses one point of shears durability.
- [ ] A named cutting, rootstock, and yield strategy remain unchanged when
  netting is installed or removed.
- [ ] Breaking the vine and planting a new vine in the same position does not
  inherit the previous vine's netting.
