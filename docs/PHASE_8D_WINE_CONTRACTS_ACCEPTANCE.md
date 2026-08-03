# Phase 8D — Wine Contracts and Trade Correspondence

The estate desk now supports bounded, persistent wine orders when a Trade
Correspondence Board is installed within two blocks. The board turns the
previously advisory regional market into a physical estate workflow without
putting another wall of information in chat or on bottle tooltips.

## Contract rules

- each eight-day offer cycle contains three orders from the estate's regional
  trade partner;
- only one order can be active at a time;
- every order names a wine style, minimum quality, minimum bottle age, bottle
  count, expiry day, and emerald payment;
- dispatching a matching bottle through the board advances the order by one;
- a mismatched bottle is rejected without being consumed;
- completing an order pays the player, records the event in the estate ledger,
  and contributes to estate reputation;
- offers, acceptance, progress, completion, and expiry persist with the world.

## Physical module

The Trade Correspondence Board is available in all twelve supported wood
families. It is a freestanding writing bureau with filed letters and wax seals,
not a placeholder full cube. Placing it within two horizontal blocks and one
vertical block of an Estate Management Desk unlocks the desk's Contracts tab.

## Manual acceptance

1. Open an Estate Management Desk without a nearby board. The Contracts tab
   should explain that the correspondence module is missing and should not
   manufacture offers.
2. Place any wood variant of the Trade Correspondence Board within two blocks
   of the desk. Reopen the desk and confirm that three regional orders appear.
3. Accept one order. Its card should show as active and the other Accept buttons
   should become unavailable.
4. Use a bottle with the wrong style, insufficient quality, insufficient age,
   or another known producer on the board. It should remain in hand and the
   order should not advance.
5. Dispatch matching bottles one at a time. The board should show progress and
   consume exactly one bottle per successful dispatch.
6. Dispatch the final bottle. Confirm the emerald payment, completion sound,
   estate-ledger entry, and reputation contribution.
7. Save and reload midway through another order. Acceptance and delivered
   bottle count should survive the reload.
8. Leave an offered or active order past its displayed expiry day. It should
   expire, and the next eight-day cycle should provide a fresh bounded set.
9. Place and rotate several wood variants. Check the legs, sloped writing
   ledge, pigeonholes, letters, seals, item icon, particles, and axe mining.

## Deliberately deferred

Contracts currently use stable regional trade partners and fixed cycle rules.
Physical caravans, route travel time, settlement stock simulation, negotiated
contracts, fluctuating events, and multiplayer estate permissions remain later
Phase 8 milestones.
