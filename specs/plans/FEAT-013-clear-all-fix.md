# Plan: FEAT-013 — Clear All Data fully wipes the database

## Implementation Order

1. Add `@Query("DELETE FROM expense_records") suspend fun clearAll()` to `ExpenseRecordDao`.
2. Update `ClearAllDataUseCase`:
   - Inject `CartioDatabase`, `ShoppingListDao`, `ProductDao`, `ExpenseRecordDao`.
   - Wrap calls in `db.withTransaction { ... }`.
3. Remove unused price-history scaffolding (per ADR-017):
   - Delete `PriceHistoryEntity.kt` and `PriceHistoryDao.kt`.
   - Drop the entity from `CartioDatabase`'s `@Database` set; drop the `priceHistoryDao()` accessor.
   - Bump DB version 4 → 5 and add `MIGRATION_4_5 { DROP TABLE IF EXISTS price_history }`.
4. Hilt: no `providePriceHistoryDao` (it never existed in production); existing bindings cover the use case's new DAO list.
5. Integration test `ClearAllDataUseCaseTest` (`app/src/androidTest/`, in-memory Room):
   - Happy path: seed every table, run `invoke()`, assert all tables empty.
   - Idempotency: invoking on an empty DB twice is a no-op.
   - Rollback: when one DAO's `clearAll()` throws, every prior wipe inside the transaction is rolled back. (Implemented by delegating one DAO via `by` and overriding `clearAll` to throw.)

   *Why integration, not unit:* `withTransaction` is a Room extension that requires a real `RoomDatabase`. Mocking it would either bypass the transaction (proving nothing) or require introducing a `TransactionRunner` seam — premature for a 4-line use case with one caller.
6. Add `androidTestImplementation(libs.kotlinx.coroutines.test)` to `app/build.gradle.kts` (was missing — pre-existing `ShoppingListDaoTest` also depended on it but the dep was never wired).
7. Update CLAUDE.md domain model + roadmap-phases sections; add ADR-017 to `docs/cartio_app_adr.md`; trim price-history mentions from `docs/cartio_app_implementation_roadmap.md` and `docs/cartio_app_settings_screen_breakdown.md`.

## Edge Cases

- **Active Flow subscribers** (Reports, dashboard): Room's invalidation tracker re-emits empty lists after the transaction commits. No nav or process restart needed.
- **Background work touching the DB** (e.g. ad-frequency prefs): unaffected — those use SharedPreferences, not Room.
- **Existing devices on schema v4**: `MIGRATION_4_5` runs once and drops the empty `price_history` table. No data loss because nothing ever wrote to it.
- **Repeat wipe** (user taps twice): idempotent — DELETE on an empty table is a no-op.

## Files Modified / Created

| File | Change |
|---|---|
| `features/expenses/data/ExpenseRecordDao.kt` | Add `clearAll()` |
| `features/settings/domain/ClearAllDataUseCase.kt` | Inject 3 DAOs + database; `withTransaction` wrap |
| `core/database/CartioDatabase.kt` | Remove `PriceHistoryEntity` + accessor; version 4→5 |
| `core/database/migrations/Migrations.kt` | Add `MIGRATION_4_5` |
| `core/database/dao/PriceHistoryDao.kt` | **Deleted** |
| `core/database/entity/PriceHistoryEntity.kt` | **Deleted** |
| `app/src/androidTest/.../ClearAllDataUseCaseTest.kt` | New integration test (in-memory Room) |
| `app/build.gradle.kts` | Add `androidTestImplementation(libs.kotlinx.coroutines.test)` |
| `app/schemas/.../5.json` | Auto-emitted by Room (4 entities, no price_history) |
| Drive-by: 4 res XML files | Move `<?xml ?>` prolog to line 1 (was breaking build) |
| Drive-by: `androidTest/.../ShoppingListDaoTest.kt` | Replace stale `dao.getAll()` with `getAllWithItemCount()` |
| `CLAUDE.md` | Domain model + roadmap sections updated |
| `docs/cartio_app_adr.md` | Add ADR-017 |
| `docs/cartio_app_implementation_roadmap.md` | Remove price-tracking epic and refs |
| `docs/cartio_app_settings_screen_breakdown.md` | Replace price-history bullet with products/expenses |
| `specs/features/FEAT-013-clear-all-fix.md` | This spec |
| `specs/plans/FEAT-013-clear-all-fix.md` | This plan |
| `specs/README.md` | Add FEAT-013 row |

## Verification

```bash
./gradlew connectedAndroidTest               # ClearAllDataUseCaseTest passes (needs device/emulator)
./gradlew clean assembleDebug                # compiles
./gradlew test                               # unit suite green
```

Manual:
1. Create lists, items, record expenses (Reports shows totals).
2. Settings → Clear all data → Delete All.
3. Dashboard renders empty state. Reports renders empty state. No restart.
4. Re-open the app — nothing comes back.
