# Cartio — Engineering & UX Review

**Author:** Senior Kotlin/Android engineer (with input from a UI/UX designer)
**Date:** 2026-04-28
**Scope:** Whole codebase under `app/src/main` and supporting modules. No third‑party libraries are proposed; every recommendation can be implemented with what is already in the project (AndroidX, Compose, Hilt, Room, Kotlin Coroutines).
**Status of the app at review time:** FEAT-001 through FEAT-011 are merged on `master`. Build green, unit tests passing.

---

## 0. TL;DR

Cartio is in a healthy place for a v0.x product. The architecture is recognisably "modern Android": Compose + Hilt + Room + StateFlow with a feature‑first layout that mostly respects the layered MVVM + Repository contract documented in `CLAUDE.md`. The codebase is small enough that issues are still cheap to fix; that is exactly the time to fix them. Nothing here is dramatic — there are no hair‑on‑fire bugs — but there are a handful of architectural seams that are starting to slip, a few coroutine/lifecycle hazards that will burn the team in production, and several UX details that, individually small, add up to a noticeably less polished experience than the design intent suggests.

The single most valuable change you can make right now is to **stop using `GlobalScope` in `CartioApp` and structure startup work around `Application.applicationScope` you control yourself.** Everything else is incremental.

---

## 1. Engineering Review

### 1.1 What's done well

Before the criticism, things genuinely worth keeping:

- **Feature-first packaging** is consistent and the boundaries are real. Each feature has a `data/`, `domain/`, `ui/` triad. New developers can onboard one feature at a time without spelunking the rest.
- **Repository abstraction is honoured.** `ShoppingListRepository`, `ShoppingListItemRepository`, `ExpenseRepository`, `BillingRepository`, `AdsRepository` are interfaces, with `*Impl` classes bound via Hilt `@Binds`. This is the foundation ADR-004 calls for. Fakes in tests are simple to write.
- **Use cases are honest.** They aren't trivial pass-throughs — `AddItemToListUseCase` (`app/src/main/java/com/minicore/cartio/features/shopping/domain/AddItemToListUseCase.kt:9`) genuinely orchestrates three collaborators (product lookup/upsert, item upsert, list timestamp touch). That is the right use of the layer.
- **`SavedStateHandle` is used for nav arguments**, e.g. `RegisterExpensesViewModel:46`, `ShoppingListDetailViewModel:43`. ViewModels are free of `Bundle` plumbing in screens.
- **Schemas are exported** (`app/schemas/.../1.json,2.json,3.json`) and `MIGRATION_2_3` is a real migration. This is grown-up Room hygiene.
- **`@HiltViewModel` + `hiltViewModel()` + `collectAsStateWithLifecycle()`** is used throughout — this is the recommended trio and avoids the most common Compose recomposition mistakes.

The bones are good. The rest of this section is about hardening them.

---

### 1.2 Critical concerns (fix before next release)

#### 1.2.1 `GlobalScope` in `CartioApp` is a real problem
**Location:** `app/src/main/java/com/minicore/cartio/CartioApp.kt:26`

```kotlin
GlobalScope.launch(Dispatchers.Main) {
    runCatching { startupInitializer.initialize(applicationContext) }
        .onFailure { Log.e(TAG, "Startup initialization failed", it) }
}
```

`GlobalScope` is unstructured. The job is not cancelled when the process is shutting down, not tied to anything testable, and any future suspending caller of `startupInitializer` will inherit a scope with `EmptyCoroutineContext + Dispatchers.Main`. The `@OptIn(DelicateCoroutinesApi::class)` warning at line 26 is the language telling you exactly this.

**Fix (no library required):**

```kotlin
class CartioApp : Application() {
    val applicationScope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    override fun onCreate() {
        super.onCreate()
        MobileAds.initialize(this)
        applicationScope.launch {
            runCatching { startupInitializer.initialize(applicationContext) }
                .onFailure { Log.e(TAG, "Startup initialization failed", it) }
        }
    }

    override fun onTerminate() {
        applicationScope.cancel()
        super.onTerminate()
    }
}
```

Prefer `Dispatchers.Main.immediate` so that work that doesn't actually need to bounce off the main looper doesn't pay the cost. Inject `applicationScope` via Hilt where appropriate (e.g. for `BillingClientWrapper.refreshEntitlements`).

#### 1.2.2 The startup initializer blocks the main thread
**Location:** `CartioApp.kt:26` + `AppStartupInitializer.kt:12`

`MobileAds.initialize` and `BillingClient.startConnection` both perform IPC over Binder. Calling them on `Dispatchers.Main` is permitted by the SDK but every millisecond is one off the first frame budget. Move billing connection and ad preload to `Dispatchers.Default` (or `IO`) and switch to `Main` only where the SDK requires. With AdMob this matters less (the call is immediate-returning), but Billing's `startConnection` schedules a callback that runs on a binder thread anyway — there is no benefit to forcing `Main`.

#### 1.2.3 `findActiveItemByProduct` can race with insert
**Location:** `app/src/main/java/com/minicore/cartio/features/shopping/domain/AddItemToListUseCase.kt:15`

The flow is read‑modify‑write across two suspend calls without a transaction:

```kotlin
val activeItem = itemRepository.findActiveItemByProduct(listId, productId)
if (activeItem != null) {
    itemRepository.updateQuantity(activeItem.first, activeItem.second + 1)
} else {
    itemRepository.insertItem(listId, productId)
}
```

If the user double-taps "Add" fast enough (or two add operations run from autofill + IME action together), you'll get two rows for the same product instead of one row with quantity 2. Wrap the lookup + decision + update in a `@Transaction`-annotated DAO method, or in a single coroutine that holds a `Mutex` keyed by `(listId, productId)`. The DB-level transaction is the cheaper fix.

#### 1.2.4 Billing entitlement state is held in a singleton wrapper but not refreshed on resume
**Location:** `BillingClientWrapper.kt:29` + `AppStartupInitializer.kt:13`

`refreshEntitlements()` is called once at process start. If the user buys ad-free on another device, or returns hours after a cached purchase state, the entitlement flow will not update until process restart. Add a `LifecycleEventObserver` on `MainActivity`'s `ON_RESUME` that calls `BillingRepository.refreshEntitlements()` (Play recommends this).

#### 1.2.5 Compose `onClick` on `Box` with `clickable {}` instead of `IconButton`
**Locations:** `MainActivity.kt:148` (rename pencil), `ShoppingListDetailScreen.kt:425` (`CircleCheckbox`), `ShoppingListDetailScreen.kt:459` and `:489` (quantity stepper buttons), and `RegisterExpensesScreen.kt` `Box`/`clickable` patterns.

These targets are around 24‑28 dp. The Material guideline (and Play Store accessibility scanner) requires **48 dp minimum touch target**. The visual icon can stay small — wrap them in `IconButton` (which expands the ripple/touch area to 48 dp) or use `Modifier.minimumInteractiveComponentSize()`. Doing this also restores the standard Material ripple, which is currently absent from these tap targets.

#### 1.2.6 `LaunchedEffect(uiState.saved) { ... onNavigateUp() }` is fragile
**Location:** `RegisterExpensesScreen.kt:55`

If the user backgrounds the app between save success and navigation, the LaunchedEffect block survives recomposition but the state `saved=true` will fire `onNavigateUp` again on relaunch. This is the classic "navigation as state" footgun. Use a one-shot `Channel<Unit>` exposed as a `Flow` from the ViewModel (consumed via `collect` in a `LaunchedEffect`) so the event is consumed once. The same pattern applies to `_listDeleted` in `ShoppingListDetailViewModel:50` and `_createdListId` in `ShoppingListViewModel:38`.

A minimal pattern, no third-party library:

```kotlin
private val _events = Channel<Event>(Channel.BUFFERED)
val events: Flow<Event> = _events.receiveAsFlow()

// Screen
LaunchedEffect(Unit) { viewModel.events.collect { handle(it) } }
```

#### 1.2.7 Database is dropped in debug builds
**Location:** `DatabaseModule.kt:81`

```kotlin
.apply { if (BuildConfig.DEBUG) fallbackToDestructiveMigration(dropAllTables = true) }
```

This is helpful while developing, but will silently nuke developer data when a migration accidentally fails to apply. Recommend gating on a separate flag (`AppConfig.DROP_DB_ON_MIGRATION_FAILURE`) so the team can opt out per-developer, and never enable it on `release`. Also, for the Auto Backup feature (FEAT-008) this means restored data could be wiped if a debug build is run after restore — surprising and irreversible.

---

### 1.3 Architectural concerns

#### 1.3.1 Two parallel naming systems in `features/shopping/data/`
**Location:** `app/src/main/java/com/minicore/cartio/features/shopping/data/`

```
LocalProductDataSource.kt
ProductLocalDataSource.kt
LocalShoppingListDataSource.kt
ShoppingListLocalDataSource.kt
LocalShoppingListItemDataSource.kt
ShoppingListItemLocalDataSource.kt
```

Each pair is "interface vs implementation" but the naming convention is inconsistent — some have the interface as `XLocalDataSource` and impl as `LocalX`, others the inverse. New contributors will guess wrong. Pick one convention (Google's modern guidance is interface `LocalDataSource`, implementation `LocalDataSourceImpl`; older sample code uses `Local*DataSource` for the interface and `Local*DataSourceImpl` for the impl) and rename the outliers in a single PR. This is cheap now, expensive later.

#### 1.3.2 `SyncDataSource` abstraction promised by ADR-004 is not yet present
The repository interfaces today only know about a `LocalDataSource`. ADR-004 explicitly says repositories must support both `LocalDataSource` and `SyncDataSource` from day one. Today they don't — `ShoppingListRepositoryImpl:9` only takes `ShoppingListLocalDataSource`. Two options:

1. Treat ADR-004 as aspirational (most likely the right call until Drive sync is on the roadmap) and **update the ADR** so future-you doesn't think it's already wired.
2. Add a no-op `RemoteDataSource` interface plus a `NoopRemoteDataSource` impl now, so repository signatures don't churn when sync arrives.

I'd take option 1 unless sync is < 2 sprints away.

#### 1.3.3 Domain models live next to data classes in `data/`
**Location:** `features/shopping/data/ShoppingList.kt`, `features/shopping/data/ShoppingListItem.kt`

`ShoppingList` and `ShoppingListItem` are domain models (no Room annotations, plain Kotlin), but they sit in `data/`. Move them to `features/shopping/domain/` to match `ExpenseRecord.kt` (`features/expenses/domain/ExpenseRecord.kt`). This is the only inconsistency in the otherwise clean package layout.

#### 1.3.4 `DatabaseModule` is doing too much
**Location:** `DatabaseModule.kt`

This file binds: the database, four DAOs, three repositories, four data sources, two use cases, and a migration. It's 105 lines and growing. Split into:

- `DatabaseModule` — Room + DAOs + migrations.
- `RepositoryModule` — `@Binds` for repositories and data sources.
- `UseCaseModule` — `@Binds`/`@Provides` for use cases.

This is purely a readability win, but new features want a place to add their bindings; today the answer is "shove it in `DatabaseModule`," which ages poorly.

#### 1.3.5 `RecordExpensesUseCase` is provided as a lambda directly bridging the repository
**Location:** `DatabaseModule.kt:101`

```kotlin
@Provides
fun provideRecordExpenses(repo: ExpenseRepository): RecordExpensesUseCase =
    RecordExpensesUseCase { records -> repo.recordExpenses(records) }
```

This is fine **today** because the use case is a pass-through. But the moment recording an expense needs to also touch a `recordedAt` clock, post-process for analytics, or fan out to multiple stores, this lambda becomes a constraint that has to be unwound before the real logic can be added. Promote it to a real class (`RecordExpensesUseCaseImpl`) immediately — the cost is one file, the benefit is a stable extension point.

#### 1.3.6 Entity column types vs. domain types disagree on `quantity`
**Location:** `ShoppingListItemEntity.kt:34` (`quantity: Float?`) vs. `ShoppingListItem` domain model (Int) vs. UI quantity stepper (Int).

The DB stores `Float?`, but every layer above coerces it back to `Int` (e.g. `ShoppingListItemRepositoryImpl.kt:17`: `entity.quantity?.roundToInt()?.coerceAtLeast(1)`). Either:

- The Float storage was intended to support fractional quantities (e.g. "1.5 kg"); if so, the domain model and UI need to support it too.
- Or fractional quantities were never the intent; in which case migrate the column to `INTEGER NOT NULL DEFAULT 1` and remove the Float.

Right now the code lies twice in opposite directions. Pick one.

#### 1.3.7 `MeasureUnit` Room converter throws on unknown values
**Location:** `core/database/converters/CustomConverters.kt:13`

```kotlin
@TypeConverter
fun toMeasureUnit(abbreviation: String): MeasureUnit =
    MeasureUnit.fromAbbreviation(abbreviation)  // entries.first { ... } throws
```

If the schema is ever extended (e.g. a new `MeasureUnit.Pound("lb")` is added in a future release, then the user downgrades), reading old rows will crash with `NoSuchElementException`. Use `entries.firstOrNull { ... } ?: MeasureUnit.Piece` as a safe default. The cost is one extra `?:`; the benefit is a non-crashing app on schema mismatch.

#### 1.3.8 No cancellation propagation around suspending Billing callbacks
**Location:** `BillingClientWrapper.kt:46`, `:64`, `:88`

`suspendCancellableCoroutine` is used but the cancellation handler never cancels the underlying billing operation. If a coroutine is cancelled mid-`queryProductDetailsAsync`, the callback will still fire and call `continuation.resume(Unit)` — by then the continuation is already cancelled, which is at best a `CancellationException` swallowed by `suspendCancellableCoroutine`. Add `continuation.invokeOnCancellation { /* there's nothing to cancel on BillingClient, but log */ }` to make intent explicit, and gate `continuation.resume` calls behind `if (continuation.isActive)`.

#### 1.3.9 `getAllWithItemCount` query is a single GROUP BY without LIMIT
**Location:** `ShoppingListDao.kt:14`

It's fine for tens of lists. For a power user with hundreds, paginate. AndroidX Paging is overkill — add `LIMIT :limit OFFSET :offset` parameters and a count query, expose them through the repository. This is not urgent; flag it for v1.

---

### 1.4 Code-quality nits (worth a single cleanup PR)

| # | Location | Issue |
|---|---|---|
| 1 | `ShoppingListDetailScreen.kt:160` | `sortIcon` switch always returns the same icon; either rotate it or remove the `when`. |
| 2 | `ShoppingListScreen.kt:131` | Sort accessibility description hard-coded in English ("Sort A to Z") — should be a `stringResource`. |
| 3 | `ShoppingListDetailScreen.kt:637-642` | "Save" / "Cancel" hard-coded in English; use string resources. |
| 4 | `ShoppingListDetailScreen.kt:152-153` | "Empty list" / "%d of %d picked up" hard-coded; localize. |
| 5 | `ReportsScreen.kt:72` | `Text("Reports")` — should use `R.string.nav_item_reports`. |
| 6 | `ShoppingListScreen.kt:407-408` | `"item" / "items"` — use Android plurals (`<plurals>` resource). |
| 7 | `RegisterExpensesScreen.kt:179` | `"$%.2f".format(total)` — no locale. Use `NumberFormat.getCurrencyInstance` (no third-party library, in stdlib) once you decide whether to localize currency. |
| 8 | `strings.xml:63` | `reports_coming_soon` is an orphan string resource (Reports isn't "coming soon" anymore). Lint as unused, remove. |
| 9 | `MainActivity.kt:115` | `"Cartio"` and `MainActivity.kt:150` `"cartio · v..."` are hard-coded; localize via `R.string`. |
| 10 | `RegisterExpensesComponents.kt:97` | `monetaryInputRegex` hard-codes 2 decimal places, but the requirement was 3. The linter changed `{0,3}` → `{0,2}`. Memory says spec wanted up to 3 decimal places — confirm intent and fix. |
| 11 | `ShoppingListItemEntity.kt` | No `Index` on `(shoppingListId, productId, checked)` — `findActiveByProduct` does a partial index scan. Add a composite index. |
| 12 | `RegisterExpensesViewModel.kt:54-55` | Two `.first()` calls on different flows mean the screen flickers if data arrives non-atomically. Combine with `combine(...).first()`. |
| 13 | `CartioNavGraph.kt:26-65` | Boilerplate — extract a small DSL or just leave it; not worth the abstraction yet. |
| 14 | `ShoppingListDetailScreen.kt:91` | `LocalContext.current as Activity` — fragile (will crash inside a `Preview`). Use `LocalActivity` or guard with `LocalContext.current.findActivity()`. |
| 15 | `Migrations.kt:7` | The migration uses `Migration(2, 3)` as an anonymous object literal. Future migrations should follow the same pattern; consider a `Migrations.kt` containing all migrations as `val MIGRATION_X_Y = Migration(...)` plus an `ALL_MIGRATIONS` array exposed to `DatabaseModule`. |

---

### 1.5 Testability concerns

The test coverage is good for the size of the codebase, but a few patterns will become painful as features grow:

1. **Time is read directly from `System.currentTimeMillis()` everywhere** (`AddItemToListUseCase:20`, `ShoppingListRepositoryImpl:19`, `ReportsViewModel:27`, `RegisterExpensesViewModel:95`). `GetSpendingReportUseCase` does the right thing — `clock` is a parameter — but the rest of the codebase doesn't follow suit. Inject a `Clock` (you can write a 5-line `interface Clock { fun now(): Long }` and bind `SystemClock` in Hilt; no library required) and test time-sensitive logic deterministically.

2. **`ShoppingListDetailViewModel.onScreenEntered(activity)` takes an `Activity`.** This couples the ViewModel to the Android `Activity` class, which is the testability thing modern Android most wants to avoid. The reason is that `ShowDetailAdUseCase` needs an Activity for `interstitial.show(activity)`. Push the activity dependency into the screen — let the screen call `viewModel.shouldShowAd().also { if (it) showAdUseCase(activity) }`. The ViewModel decides; the screen executes.

3. **`SettingsViewModel` reads `BackupPreferences.isBackupEnabled` in its constructor**, which means the initial value is read on the main thread. Fine here (SharedPreferences is fast), but a pattern that scales badly. Prefer reading via a `Flow` exposed by the repository.

4. **`InMemorySharedPreferences` test helper uses `@Suppress("UNCHECKED_CAST")` on a `Set<String>` cast.** Acceptable, but a small wrapper that returns the typed set would remove the warning.

5. **There are no tests around `BillingClientWrapper`.** That's defensible (it's a thin Play Billing wrapper) but it is also the most likely class to silently break when Billing 8.x ships breaking changes. At minimum, a contract test that exercises the `connect → query → purchase` happy path against a fake `BillingClient` interface would catch SDK upgrade regressions. This requires extracting an `interface BillingClientFacade` so the wrapper takes it via constructor — currently it's instantiated via `BillingClient.newBuilder()` directly in the wrapper.

---

### 1.6 Performance concerns

- **`ShoppingListDetailViewModel.uiState`** combines three flows and does a sort + two filters per emission (`uiState` block at line 52-70). On a 100-item list with frequent toggles this is fine; on a 1000-item list it will start to hurt. Consider memoising the sorted list so a check toggle doesn't trigger a re-sort.
- **`SwipeToDismissBox`** with a large `LazyColumn` of items can cause measurement overhead because each row is its own dismiss-state. This is rarely a problem in practice but watch for it during dogfood.
- **`ReportsViewModel` collects two flows** — `purgeOlderThan` plus the report flow. A purge inside the ViewModel `init` is fired-and-forgotten without coordinating with the report query. With WAL mode (Room default) it's safe. Worth a comment explaining why.

---

## 2. UI/UX Review

> The following section captures the perspective of a designer who specialises in Android consumer apps. Their notes were transcribed and lightly edited for the document.

> *"Overall: warm, friendly, and immediately readable. The orange/cream palette is a confident choice and the cart icon system is doing real work. The product feels like a small, deliberate thing rather than a generic to-do app. That is rare. There are six places where small adjustments would make it feel substantially more polished, and one place where I think the team is fighting Material 3 instead of leaning on it."*

### 2.1 What's working well

- **The brand palette is doing 80% of the work.** `Primary = #E65100` against `Background = #FFF8F0` is warm without being shouty, and the contrast ratio is in spec for body text.
- **The "Shopping in progress" card on the dashboard** (`ShoppingListScreen.kt:211`) is the right pattern. It collapses what could have been a separate "Resume" tab into a single, persistent CTA. This is a good shortcut for the most common task.
- **The swipe-to-check / swipe-to-delete gestures with coloured backgrounds** (`ShoppingListDetailScreen.kt:315`) are fast, satisfying, and well-coloured. Green for check, red for delete is unambiguous.
- **The progress bar under the detail screen's top bar** (`ShoppingListDetailScreen.kt:225`) is a delightful detail. It gives the user a sense of "I'm 70% done shopping" without needing to count.
- **The promo card** (`SettingsComponents.kt:215`) is *not* a desperate IAP push. It's framed as "Buy me a coffee," which fits the indie tone of the app and makes the request feel earned.
- **Empty states have actual content.** `R.string.shopping_empty_subtitle` ("Tap + to create your first shopping list") tells the user what to do. Most apps do not do this.

### 2.2 Issues by severity

#### 🟥 Severity High

1. **Touch targets below 48 dp.** This is engineering and design's joint responsibility: the `CircleCheckbox` (24 dp), `QuantityStepper` buttons (28 dp), and the rename pencil (16 dp) are all too small for confident tapping during a real grocery trip — the user is moving, often holding a basket. **Recommendation:** keep visual size as-is, expand the tappable area to 48 dp via `IconButton` or `Modifier.minimumInteractiveComponentSize()`. This will also make the ripple feedback feel right.

2. **No undo on item delete.** Swipe to delete on a shopping list item (`ShoppingListDetailScreen.kt:309`) is irreversible. In a fast-add context this will bite users. **Recommendation:** show a `Snackbar` with "Undone" action for ~5 seconds after delete. The Snackbar is already in Material 3; no new dependency. The same applies to `deleteShoppingList` from the dashboard's `MoreVert` menu.

3. **The price-input mask allows leading-zero ambiguity.** Currently `005.50` is accepted (`monetaryInputRegex = ^\d*\.?\d{0,2}$`). For prices the user expects `5.50`. This is a small nit but causes "why does this look weird" reactions. **Recommendation:** trim leading zeros on blur, or render the value through the same `%.2f` formatter on field re-render.

4. **The "Register expenses" entry point is hidden behind a kebab menu** (`ShoppingListDetailScreen.kt:191`). Recording expenses is the highest-value action on this screen for the Reports feature to be useful. **Recommendation:** when the user has at least one checked item, surface a primary "Register" button as an extended FAB or persistent footer action. The kebab menu can keep the option for discoverability, but don't make it the only path.

#### 🟧 Severity Medium

5. **The dashboard has no visual difference between an empty list and a list of 50 items.** All cards render with the same height and density. **Recommendation:** add a small badge or progress dot indicating how full the list is. The data is already in `ShoppingListWithCount`. A 1-dp ring around the leading orange dot proportional to `checkedCount/itemCount` would do it.

6. **Search bar is always present, even when there are 0 lists.** `ShoppingListScreen.kt:180` renders the search field unconditionally, then shows the empty state below. **Recommendation:** hide the search bar until there are at least ~5 lists. For 0–4 lists, search is a distraction.

7. **The "DONE" section header (`ShoppingListDetailScreen.kt:507`) has no way to collapse**. With a long shopping trip, the checked items pile up and push the active items off screen. **Recommendation:** make the header tappable to collapse the section. State can live in `rememberSaveable` per-list.

8. **TopAppBar uses `MaterialTheme.colorScheme.primary` as container.** This is fine on the home screen but every screen uses it, so navigating from dashboard → list → expenses gives no chromatic differentiation. **Recommendation:** for "deep" screens (Register Expenses), use `surface` with elevation 2; reserve `primary` for the top-level destinations (Shopping, Reports, Settings). This is also closer to Material 3 guidance.

9. **The Reports summary card (`ReportsScreen.kt:137`)** is currency-only. There is no period selector ("last 7 / 30 / 90 days"). Users compare months, not days. **Recommendation:** even a 3-segment toggle would meaningfully expand the report's usefulness. The existing `GetSpendingReportUseCase` already takes `daysBack` as a Flow operator parameter — wiring is half done.

10. **The progress bar on the detail screen is 3 dp tall.** That's hairline-thin on a 6-inch phone. **Recommendation:** 4–6 dp with rounded caps; the user actually reads it then.

#### 🟨 Severity Low (polish)

11. **Splash screen is a fixed 2000 ms delay.** `CartioSplashScreen.kt:29` `SPLASH_DURATION_MS = 2000L`. This is artificially slow on warm starts. **Recommendation:** keep the visual but exit as soon as the first frame of the dashboard is ready, with a minimum visible time of ~600 ms. Use `installSplashScreen` (already imported in `MainActivity.kt:50`) with `setKeepOnScreenCondition` instead of an arbitrary delay.

12. **Drawer item subtitles are useful but heavy.** "Lists, items, prices" / "Spend & trends" / "Sync, data, about" — they earn their space but compete with the title for visual weight. **Recommendation:** reduce subtitle to `bodySmall` and increase opacity contrast (currently `outlineColor`).

13. **The "Cartio" wordmark in the drawer (`MainActivity.kt:115`) uses `headlineSmall` with system font.** The splash screen uses the custom `Autolova` font. Brand inconsistency. **Recommendation:** use Autolova in the drawer header too.

14. **Currency display is `$` only**, irrespective of locale. Per spec this is intentional for v1, but a US user travelling in Europe will record EUR purchases as USD. **Recommendation:** in v1.1, surface a "currency" preference under Settings → Data (already-existing section). Default to `$`, persist user choice.

15. **The "Empty list" state in `ShoppingListDetailScreen.kt:589` is centered vertically**. With the keyboard open (the `AddItemBar` has `imePadding`), the empty state jumps up dramatically. Users who tap into the input first will see the empty state become a small strip. **Recommendation:** anchor the empty state to the top half of the available space rather than centering.

16. **No visual feedback when "Record" is pressed in `RegisterExpensesScreen`.** The button disables, then a toast appears, then we navigate. Toast is the weakest possible confirmation; for a financial action, a momentary check-icon overlay or a short Snackbar before navigation reads as "we got it" — not "fire and forget." **Recommendation:** replace toast with a `SnackbarHostState.showSnackbar(...)` shown in the parent screen after `popBackStack`, or animate a check-circle inline before navigating.

17. **Dark mode uses identical orange `Primary = #FF8A65`** but the background is very dark (`#1C1412`). The contrast is fine for headings; for body text on `surfaceVariantDark` (#3A2E2A) the contrast ratio is ~3.5:1 — below WCAG AA. **Recommendation:** lift the on-surface text by 5–10% in dark mode.

18. **`%.2f.format(total)` is locale-insensitive.** A user with a German locale expects `12,50`, not `12.50`. Even if you keep `$` for v1, currency formatting should respect locale. Use `NumberFormat.getCurrencyInstance(Locale.getDefault()).apply { currency = Currency.getInstance("USD") }` once you decide on the policy.

### 2.3 Accessibility

- **Content descriptions are present on most icons** — good. A few buttons have `contentDescription = null` where the icon carries semantic weight. Audit.
- **`CircleCheckbox` has no role or `Modifier.semantics { role = Role.Checkbox; toggleableState = ... }`** — TalkBack reads it as a generic clickable element. Add `Modifier.toggleable(value = checked, role = Role.Checkbox, onValueChange = { onClick() })`.
- **Focus order on the "Register expenses" rows** is fine, but the IME action on the price field is not declared (default is "Done"). For a multi-row form, "Next" would let the user tab through prices without dismissing the keyboard between every row.
- **Dynamic type** is not currently honoured — the headline-sized total in Reports is hard-coded at 40.sp. Use `MaterialTheme.typography.displaySmall` or wrap in `Modifier.scaleByFont` (no library, just `LocalDensity.current.fontScale`).

### 2.4 One bigger picture concern

> *"You're using Material 3 but you're styling around it. The orange `TopAppBar` everywhere, the custom `CircleCheckbox`, the hand-rolled `QuantityStepper`, the bespoke `AddItemBar` rounded pill — these all look fine in isolation, but they're each a small fight against the system. If the team is happy maintaining a custom design language, double down on it (extract a `CartioComponents` module, document the tokens in `Color.kt` and `Type.kt`). If the team isn't, lean into M3 — let the `TopAppBar` sit on `surfaceContainer`, use `Checkbox` and `IconToggleButton` defaults, and skin via `colorScheme`. Right now the codebase is half-and-half, which is the most expensive place to be."*

---

## 3. Recommendations Summary (Prioritised)

If only three things get done in the next sprint, do these:

| # | Change | Why | Effort |
|---|---|---|---|
| 1 | Replace `GlobalScope` with an `applicationScope` on `CartioApp` and refresh billing entitlement on `ON_RESUME` | Correctness; future-proof; fixes a `DelicateCoroutinesApi` opt-in. | 0.5 day |
| 2 | Make all primary touch targets ≥ 48 dp via `IconButton` / `minimumInteractiveComponentSize` | Accessibility; Play Store will flag this | 0.5 day |
| 3 | Add Snackbar undo for item / list deletions | Highest UX win for lowest effort | 0.5 day |

If the team has another week:

4. Wrap `findActiveItemByProduct` + insert in a `@Transaction`. (1 hour)
5. Inject a `Clock` interface and migrate time-of-day reads. (0.5 day)
6. Localise hard-coded English strings (`Save`, `Cancel`, `Reports`, `Cartio`, etc.). (0.5 day)
7. Add Android `<plurals>` for `item / items`. (1 hour)
8. Decide and document: `quantity` is `Int` everywhere or `Float?` everywhere; pick one and migrate. (1 day with migration)
9. Replace single-event `StateFlow` (saved/listDeleted/createdListId) with `Channel`-based event flows. (0.5 day)
10. Add Reports period selector (7 / 30 / 90 days). (1 day; `GetSpendingReportUseCase` already takes the parameter)

If the team has another month:

11. Promote `RecordExpensesUseCase` from a lambda to a real class.
12. Split `DatabaseModule` into `DatabaseModule` / `RepositoryModule` / `UseCaseModule`.
13. Move `ShoppingList` and `ShoppingListItem` from `data/` to `domain/`.
14. Write a `BillingClientFacade` interface and a contract test for the wrapper.
15. Decide the M3-vs-custom design question and align the codebase.
16. Reconcile or update ADR-004 to reflect that `RemoteDataSource` is not yet wired.

---

## 4. What is explicitly *not* recommended

To avoid scope creep, these things came up but I am **not** recommending them right now:

- **Modularising into Gradle modules.** The codebase is 80 source files. Splitting into `:core`, `:feature-shopping`, `:feature-expenses`, etc. would add 20% build complexity for a benefit that doesn't materialise until the codebase is 5× larger. Reconsider when there are >300 files or >2 active developers.
- **Adopting MVI.** MVVM with StateFlow is working. MVI's main wins (single immutable state, action log) are not worth the boilerplate at this size.
- **Adding navigation type-safe args (Compose Navigation 2.8 destinations).** It's a great feature, but it requires KSP plumbing churn for one nav graph that has two arguments. Defer until the nav graph has 10+ destinations.
- **Replacing the in-house `monetaryInputRegex` with a library.** A 1-line regex doesn't justify a dependency.
- **Adding analytics or crash reporting.** Out of scope for this review.

---

## 5. Closing

Cartio is in the awkward but encouraging spot of being too small to need scaffolding but big enough that the patterns it commits to today will be load-bearing for the next year. Almost every recommendation above is about putting that load on the *right* beams. Nothing here suggests the project is on a bad trajectory; quite the opposite — most projects this size have far more rot to clean up.

The single most important meta-recommendation: **keep writing the feature specs and plans before the code.** The `specs/features/` and `specs/plans/` directories are an unusually disciplined practice for a codebase this young, and they are the reason this review could be written without spending half its budget on archaeology. Don't drop the habit when v1 ships.
