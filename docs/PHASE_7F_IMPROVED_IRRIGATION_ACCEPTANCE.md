# Phase 7F — Improved Irrigation

Named vineyard plots now measure the real water-channel coverage of their
root vines. This turns the existing drought-protection mechanic into a visible
physical estate upgrade without adding a detached purchase menu.

## Recognition rule

A plot reports **Improved irrigation ready** when:

- it contains at least four root vines; and
- at least 75% of those vines are within the existing four-block channel
  radius.

The status is live rather than permanently unlocked. Removing or diverting
the water removes the status on the next inspection, and the vines also lose
the existing drought mitigation.

## Manual acceptance

1. Register a named plot containing at least four vines.
2. Sneak-use the Vintner's Almanac on a vine in that plot.
3. Confirm the plot report shows the irrigated vine count, total vine count,
   and percentage.
4. Add vanilla water channels within four blocks of at least 75% of the root
   vines.
5. Inspect again and confirm `Improved irrigation ready` appears.
6. Remove the channels and inspect again. Coverage should fall and the upgrade
   should return to its incomplete state.

## Deliberately deferred

Pumps, tanks, drip lines, and automated channels remain Phase 14 systems. This
milestone recognizes the physical irrigation players can already build and
does not counterfeit those later automation features.
