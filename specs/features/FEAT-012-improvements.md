# FEAT-012 — Improvements (Engineering & UX Hardening)

## Objective

Consolidate ~40 engineering and UX findings from the review document (`docs/cartio_app_review.md`) into a single hardening pass on the existing codebase. No new product capability is introduced; every change is bug-prevention, accessibility compliance, code quality, or visual polish. Every change uses libraries already in the project (AndroidX, Compose, Hilt, Room, Coroutines).

## In-Scope

| Section of review | Items |
|---|---|
| §1.2 Critical | 1.2.1 → 1.2.7 (all) |
| §1.3 Architectural | 1.3.3 → 1.3.9. **1.3.7 modified**: return `MeasureUnit.Piece` instead of throwing. |
| §1.4 Code-quality nits | 15 entries |
| §1.5 Testability | 5 entries |
| §1.6 Performance | 3 entries |
| §2.1 UX strengths | Documented (no code change) |
| §2.2 UX issues | 18 entries (severity High → Low) |
| §2.3 Accessibility | 4 entries |
| §2.4 Design system direction | Lean custom (decision D2) |

## Out of Scope (per review §4)

- Gradle module split.
- MVI migration.
- Type-safe Compose Navigation destinations.
- Library replacement of `monetaryInputRegex`.
- Analytics / crash reporting.
- New product surface (Reports period selector, currency preference, etc.).

## Decisions

| # | Decision | Choice |
|---|---|---|
| D1 | Quantity type (1.3.6) | Migrate DB column to `INTEGER NOT NULL DEFAULT 1`. Drop the `Float?` shim and `roundToInt().coerceAtLeast(1)`. |
| D2 | Design system direction (2.4) | Lean **custom**. Extract brand components to `core/ui/components/`; document tokens. |
| D3 | Save confirmation (2.2 §16) | Replace toast with parent-screen Snackbar via Channel events. |
| D4 | Splash duration (2.2 §11) | Replace fixed `delay(2000)` with `installSplashScreen().setKeepOnScreenCondition`. Min visible 600 ms. |
| D5 | One-shot events (1.2.6) | `Channel<Event>(BUFFERED)` → `events: Flow<Event>` exposed by VM, consumed once in screen. |
| D6 | DB migration shape (D1) | Single 3 → 4 migration: ADD `quantity_int INTEGER`, copy `CAST(quantity AS INTEGER)`, table-rebuild to drop old column + add composite index. Schema 4.json exported. |
| D7 | Currency format (1.4 #7, 2.2 §18) | `NumberFormat.getCurrencyInstance(Locale.US)` via `core/format/CurrencyFormat.kt`. Single helper so v1.1 can flip locale. |
| D8 | Touch-target strategy (1.2.5, 2.2 §1) | Wrap interactive `Box` in `IconButton`; `CircleCheckbox` uses `Modifier.toggleable(role = Role.Checkbox)` on a 48 dp parent. |
| D9 | Price decimal places (1.4 #10) | **2 decimal places** — matches `%.2f` formatter and how prices render. |

## User-Visible Changes

- Touch targets ≥ 48 dp on all interactive icons.
- Swipe-delete on shopping list items shows an "Undo" Snackbar for ~5 s.
- Recording expenses confirms via Snackbar on the parent screen (not a toast).
- Splash dismisses as soon as the dashboard renders (≥ 600 ms minimum).
- Dashboard list cards show a fill-ratio indicator instead of a static dot.
- Search bar hidden until ≥ 5 lists.
- Done section in detail screen is collapsible.
- Register Expenses CTA promoted from kebab-only to a primary footer action when checked items exist.
- Drawer "Cartio" wordmark uses Autolova font for brand consistency.
- Dark-mode body text contrast bumped to WCAG AA.

## Non-Visible Changes

- `GlobalScope` removed; injected `ApplicationScope`.
- Billing entitlement refreshed on `ON_RESUME`.
- `findActiveItemByProduct` + insert wrapped in a single transactional DAO method.
- `Clock` interface injected for time-sensitive logic.
- DI split into `DatabaseModule` / `RepositoryModule` / `UseCaseModule`.
- `ShoppingList` / `ShoppingListItem` moved from `data/` to `domain/`.
- DB migrated 3 → 4 (quantity Int + composite index).
- `MeasureUnit` Room converter falls back to `Piece` instead of crashing.
- `BillingClientWrapper` uses `BillingClientFacade` interface; cancellation safe.
- `RecordExpensesUseCase` promoted from a Hilt-provided lambda to a real class.

## Verification

- `./gradlew clean assembleDebug :app:lintDebug :app:testDebugUnitTest` — all green.
- `./gradlew :app:connectedDebugAndroidTest` — `Migration3to4Test` and `ShoppingListItemDaoTransactionTest` pass.
- TalkBack pass on detail screen.
- Manual upgrade test: pre-FEAT-012 install → upgrade → no crash, quantities preserved as Int.
