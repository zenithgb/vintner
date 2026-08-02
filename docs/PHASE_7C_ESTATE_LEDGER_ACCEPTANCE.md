# Phase 7C — Estate Ledger acceptance

Phase 7C gives each registered estate a persistent, bounded operating
history. The open book on the existing Vintage Archive is the physical
ledger; no additional workstation or menu is introduced.

## Manual acceptance

1. Found an estate with a renamed Vintner's Almanac on a Vintage Archive.
2. Register or update a named vineyard plot.
3. Plant a cutting, harvest mature grapes, and press a batch.
4. Bottle aged wine, place a bottle in a rack or crate, and record one in the
   Vintage Archive.
5. Sneak-use the Vintage Archive with an empty hand.

Expected:

- The five most recent events appear newest first.
- Repeated identical activity on the same day is aggregated into one entry.
- Wine entries retain their batch code and quality score.
- The best bottled or archived vintage appears above the recent history.
- The ledger survives leaving and reopening the world.
- Unregistered players are told to found an estate first.
- Normal empty-hand use still reports Vintage Archive capacity.
- Archive updates do not count as new archival events.

## Deliberate limits

- Each estate retains its 128 most recent entries.
- Only player-caused events are recorded; ambient growth and time passage do
  not spam the history.
- Sales and contracts are not listed until Vintner has real transaction and
  contract systems capable of producing those events.
