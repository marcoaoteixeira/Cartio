# FEAT-011 — Register Expenses + Reports

## Objective

Allow users to record what they spent on a shopping trip and see a 30-day spending summary broken down by the top items bought.

## User Stories

1. User opens the ⋮ menu on a Shopping List Detail screen and taps "Register expenses".
2. A new screen shows only the items in the "Done" section (checked items) of that list.
3. For each item, the user sets a unit price and optionally changes the measure unit (default: pcs). Quantity is displayed as a read-only label.
4. A running TOTAL updates automatically as prices are entered (unitPrice × quantity).
5. Tapping "Record" persists all rows where unitPrice > 0, shows a 2-second toast "Expenses recorded!", then returns to the Shopping List Detail screen.
6. The Reports screen shows total spending over the last 30 days and the top 10 items by total spend.

## Requirements

- Only checked items from the current list appear in the Register Expenses screen.
- No +/− quantity buttons on the Register Expenses screen — quantity is read-only.
- Price field uses `$` prefix; no country-specific currency symbol.
- Total = unitPrice × quantity per row, summed across all rows.
- Records with no price entered (blank or zero) are not persisted.
- A new database table `expense_records` stores expense snapshots independently of shopping lists and products, so data survives item/list deletion.
- Records older than 30 days are purged automatically when the Reports screen is opened.
- Reports: total spent in last 30 days + top 10 items by total spend (with purchase count).

## Decisions

| Decision | Choice |
|---|---|
| New entity vs reuse PriceHistoryEntity | New `ExpenseRecordEntity` |
| Fields stored | productName (snapshot), quantity, unitPrice, measureUnit, recordedAt |
| productId / shoppingListId | Omitted — reports aggregate by name only |
| Retention policy | Purge records > 30 days on Reports screen open |
| Currency display | `$` prefix only, no locale formatting |
