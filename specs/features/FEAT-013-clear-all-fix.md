# FEAT-013 — Clear All Data fully wipes the database

## Objective

Make Settings → "Clear all data" actually clear all data. Today it only deletes shopping lists and products; recorded expenses survive, so opening Reports after a wipe still shows old totals.

## User Story

User taps Settings → Clear all data → Delete All. After confirming, *every* user-generated row across `shopping_lists`, `shopping_list_items`, `products` and `expense_records` is gone. Reports shows the empty state. Dashboard shows the empty state.

## Requirements

- The wipe is atomic: either every table is cleared or none are. A failure mid-wipe must not leave partial state.
- The DB instance is preserved — open `Flow`s on dashboard / reports / detail screens auto-emit empty results without process restart.
- Existing "Clear all data" UI surface, copy and confirmation dialog stay as-is.

## Decisions

| Decision | Choice |
|---|---|
| Wipe strategy | Clear all tables in one `withTransaction` block. Drop-and-recreate was rejected — it would force a process restart to rebuild the Hilt singleton and would break every active `Flow` subscriber. |
| Transaction scope | `androidx.room.withTransaction { ... }` (already on classpath via `room-ktx`). |
| DAOs touched | `ShoppingListDao.clearAll`, `ProductDao.clearAll`, `ExpenseRecordDao.clearAll` (new). `shopping_list_items` cascades from `shopping_lists` via FK so no separate call needed. |
| Price-history scope | **Removed entirely.** `PriceHistoryEntity`, `PriceHistoryDao`, and the `price_history` table are deleted in the same change. Schema bumped 4 → 5 with a `DROP TABLE` migration. Recorded as ADR-017. |
| Test surface | Integration test (in-memory Room) covering happy path, idempotency, and rollback on DAO failure. |
