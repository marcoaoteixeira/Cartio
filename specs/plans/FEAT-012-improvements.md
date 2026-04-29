# Plan: FEAT-012 — Improvements (Engineering & UX Hardening)

## Implementation Order (commits, in dependency order)

### Phase A — Foundation: scopes, clocks, events, qty migration
1. `ApplicationScope` (Hilt-provided, `SupervisorJob() + Dispatchers.Main.immediate`); replace `GlobalScope` in `CartioApp`.
2. Move `billingRepository.connect()` and `adsRepository.preload()` off `Dispatchers.Main`.
3. `core/time/Clock.kt` + `SystemClock`. Inject into use cases / repos / view models that read `System.currentTimeMillis()`.
4. `Channel<Event>` events pattern. Migrate `_listDeleted`, `_createdListId`, `saved` to `events: Flow<Event>`.
5. DB migration 3 → 4: `quantity Float? → INTEGER NOT NULL DEFAULT 1` + composite index `(shoppingListId, productId, checked)`. Schema 4.json exported. `MigrationTestHelper`-based test.

### Phase B — Data layer correctness
1. `@Transaction` `addOrIncrement(listId, productId)` DAO method; `AddItemToListUseCase` calls it.
2. `MeasureUnit` converter returns `MeasureUnit.Piece` on unknown abbreviation.
3. `BillingClientWrapper`: `invokeOnCancellation` + guard `continuation.resume` with `isActive`.
4. Paginated `getAllWithItemCount(limit, offset)` overload (no UI rewire yet).
5. Replace `if (BuildConfig.DEBUG) fallbackToDestructiveMigration(...)` with `AppConfig.DROP_DB_ON_MIGRATION_FAILURE` flag.

### Phase C — DI restructure & domain placement
1. Split `DatabaseModule` → `DatabaseModule` / `RepositoryModule` / `UseCaseModule`.
2. `RecordExpensesUseCaseImpl` as a real class; `@Binds` in `UseCaseModule`.
3. Move `ShoppingList.kt` and `ShoppingListItem.kt` from `features/shopping/data/` to `features/shopping/domain/`. Update imports.

### Phase D — Lifecycle & resume hooks
1. `MainActivity` `LifecycleEventObserver` calls `billingRepository.refreshEntitlements()` on `ON_RESUME`.
2. WAL safety comment in `ReportsViewModel.init`.
3. Memoise the sorted list in `ShoppingListDetailViewModel.uiState`.

### Phase E — Testability seams
1. Drop `Activity` parameter from `ShoppingListDetailViewModel.onScreenEntered`. VM exposes `shouldShowDetailAd()`; screen calls the use case with the activity.
2. `BackupPreferences` exposes `Flow<Boolean>` in addition to property.
3. Typed `Set<String>` wrapper in `InMemorySharedPreferences` test helper.
4. `BillingClientFacade` interface; `BillingClientWrapper` takes it via constructor; `FakeBillingClientFacade` + `BillingClientWrapperTest` (happy path).

### Phase F — Code-quality cleanup
All 15 nits from review §1.4 minus #11 (folded into A5/B) and #13 (no-op). Includes:
- Replace `"%.2f".format(...)` calls with `CurrencyFormat` helper (D7).
- Localise hard-coded English strings (`Save`, `Cancel`, `Reports`, `Cartio`, "Empty list").
- `<plurals>` for `item / items`.
- Replace `LocalContext.current as Activity` with `LocalActivity.current` / `Context.findActivity()` extension.
- `Migrations.kt` exposes `ALL_MIGRATIONS = arrayOf(MIGRATION_2_3, MIGRATION_3_4)`.
- Combine two `.first()` calls in `RegisterExpensesViewModel.init` into a single `combine(...).first()`.
- `monetaryInputRegex` confirmed at 2 decimal places (D9).
- Delete unused `reports_coming_soon` string.

### Phase G — UI/UX polish (review §2.2)
G1 Touch targets + a11y · G2 Undo deletes · G3 Price normalisation on blur · G4 Surface "Register expenses" CTA · G5 List density cue · G6 Hide search until ≥ 5 lists · G7 Collapsible DONE · G8 TopAppBar surface differentiation · G9 Progress bar thickness · G10 Splash hand-off · G11 Drawer subtitle weight · G12 Autolova in drawer · G13 Empty-list anchor · G14 Snackbar after Record · G15 Dark-mode contrast.

### Phase H — Design system extraction
- New package `core/ui/components/` containing `CircleCheckbox`, `QuantityStepper`, `AddItemBar`, `PromoCard`, `PurchasedCard`, `BrandTopAppBar`.
- New package `core/ui/tokens/` for `Spacing`, `CornerRadius`, `Elevation`.

### Phase I — Wrap-up
1. Run `/code-reviewer`; fix 🔴.
2. Mark FEAT-012 Done in `specs/README.md`.
3. Push.

## Key Migration (3 → 4)

```sql
-- copy old data into a new table with the desired schema
CREATE TABLE shopping_list_items_new (
    id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
    shoppingListId INTEGER NOT NULL,
    productId INTEGER NOT NULL,
    quantity INTEGER NOT NULL DEFAULT 1,
    checked INTEGER NOT NULL DEFAULT 0,
    note TEXT,
    FOREIGN KEY (shoppingListId) REFERENCES shopping_lists(id) ON DELETE CASCADE,
    FOREIGN KEY (productId) REFERENCES products(id) ON DELETE RESTRICT
);
INSERT INTO shopping_list_items_new (id, shoppingListId, productId, quantity, checked, note)
SELECT id, shoppingListId, productId,
       COALESCE(CAST(quantity AS INTEGER), 1),
       checked, note
FROM shopping_list_items;
DROP TABLE shopping_list_items;
ALTER TABLE shopping_list_items_new RENAME TO shopping_list_items;
CREATE INDEX index_shopping_list_items_shoppingListId ON shopping_list_items(shoppingListId);
CREATE INDEX index_shopping_list_items_productId ON shopping_list_items(productId);
CREATE INDEX index_shopping_list_items_shoppingListId_productId_checked
    ON shopping_list_items(shoppingListId, productId, checked);
```

## Edge Cases Considered

- App killed mid-migration → SQLite atomic; partial state impossible.
- User on debug build with destructive migration enabled → opt-in flag, not on by default.
- TalkBack on `CircleCheckbox` → reads as "Checkbox, checked / not checked" via `Role.Checkbox`.
- Splash with cold start vs warm start → `setKeepOnScreenCondition` keeps splash up until first frame; minimum 600 ms via timestamp gate.
- Channel events with subscriber loss (e.g. config change) → `Channel.BUFFERED` capacity prevents drops; `repeatOnLifecycle(STARTED)` re-subscribes.
- Snackbar undo after delete: store the `ShoppingListItem` in event payload; `restoreItem` re-inserts via existing repo path. Item gets a new id; UI re-binds via Flow.
- Old build with `quantity = 1.5` upgraded to FEAT-012 → migrated to 1 (rounds down via `CAST(... AS INTEGER)`).
