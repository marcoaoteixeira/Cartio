# FEAT-010 Implementation Plan — Buy Me A Coffee

## Package Layout

```
features/monetization/
 ├── data/
 │    ├── AdFrequencyStore.kt
 │    ├── AdMobDataSource.kt
 │    ├── AdsRepositoryImpl.kt
 │    ├── BillingClientWrapper.kt
 │    └── BillingRepositoryImpl.kt
 ├── domain/
 │    ├── AdResult.kt
 │    ├── AdsRepository.kt
 │    ├── BillingRepository.kt
 │    ├── HasAdFreeEntitlementUseCase.kt
 │    ├── ShowDetailAdUseCase.kt
 │    └── ShouldShowAdUseCase.kt
 └── MonetizationConfig.kt
di/
 └── MonetizationModule.kt
```

## Steps

### 1. Dependencies
- `libs.versions.toml`: add `googleAds = "24.4.0"`, `playBilling = "8.0.0"`, library aliases
- `app/build.gradle.kts`: add `libs.google.ads`, `libs.play.billing`

### 2. Manifest + strings
- `AndroidManifest.xml`: add `INTERNET` permission, AdMob `APPLICATION_ID` meta-data
- `strings.xml`: add `admob_app_id` (test), `settings_support_purchased_title`, `settings_support_purchased_body`

### 3. MonetizationConfig
- Single object with all constants: `ADMOB_INTERSTITIAL_UNIT_ID`, `REMOVE_ADS_PRODUCT_ID`, `FREQUENCY_CAP_MINUTES = 30L`, `AD_TIMEOUT_MS = 3_000L`

### 4. CartioApp
- Add `MobileAds.initialize(this)` in `onCreate()`
- Inject `AppStartupInitializer`; call in `onCreate()` via `GlobalScope.launch`

### 5. AppStartupInitializer
- New singleton: calls `billingRepository.connect()`, `billingRepository.refreshEntitlements()`, `adsRepository.preload(context)`

### 6. Domain
- `AdResult`: sealed class with `Shown`, `NotAvailable`, `TimedOut`, `SkippedForEntitlement`
- `AdsRepository`: interface (`preload`, `isAdAvailable`, `showInterstitial`, `shouldShowAd`, `markAdShown`)
- `BillingRepository`: interface (`adFreeEntitlement: Flow<Boolean>`, `connect`, `refreshEntitlements`, `launchRemoveAdsPurchase`)
- `ShouldShowAdUseCase`: check billing first → check frequency
- `ShowDetailAdUseCase`: if `shouldShowAd()` → call `adsRepo.showInterstitial(activity)`; mark shown on `Shown` result
- `HasAdFreeEntitlementUseCase`: return `billingRepo.adFreeEntitlement`

### 7. Data
- `AdFrequencyStore(context)`: SharedPreferences; `shouldShowAd()` = last timestamp > 30 min ago; `markShown()` = save now
- `AdMobDataSource()`: preload via `suspendCancellableCoroutine`; show via `suspendCancellableCoroutine + FullScreenContentCallback`
- `AdsRepositoryImpl`: delegates; `showInterstitial` wrapped in `withTimeoutOrNull(AD_TIMEOUT_MS)`
- `BillingClientWrapper(context)`: `MutableStateFlow<Boolean>` for entitlement; `connect()` via coroutine; `refreshEntitlements()` = queryPurchasesAsync INAPP, check product + PURCHASED state, auto-acknowledge; `launchPurchase(activity)` = launchBillingFlow
- `BillingRepositoryImpl`: delegates to wrapper

### 8. Hilt module
- `MonetizationModule`: `@Singleton` for `AdFrequencyStore`, `AdMobDataSource`, `BillingClientWrapper`; interface bindings for `AdsRepository`, `BillingRepository`; use-case providers

### 9. ShoppingListDetailViewModel
- Inject `ShowDetailAdUseCase`
- Add `fun onScreenEntered(activity: Activity)` launching `showDetailAd(activity)`

### 10. ShoppingListDetailScreen
- Add `val activity = LocalContext.current as Activity`
- Add `LaunchedEffect(Unit) { viewModel.onScreenEntered(activity) }`

### 11. SettingsViewModel
- Inject `HasAdFreeEntitlementUseCase`, `BillingRepository`
- Expose `val adFreeEntitlement: StateFlow<Boolean>` via `stateIn`
- Add `fun onBuyRemoveAdsClicked(activity: Activity)`

### 12. SettingsScreen
- Collect `adFreeEntitlement`
- If true: show "Ad-free — Thank you!" card instead of PromoCard
- If false: PromoCard with `onBuyClick = { viewModel.onBuyRemoveAdsClicked(activity) }`

### 13. Tests (TDD first)
- `ShouldShowAdUseCaseTest`: premium → false; free + cap active → false; free + cap elapsed → true
- `ShowDetailAdUseCaseTest`: premium → SkippedForEntitlement; ad timeout → TimedOut; normal → Shown; markAdShown called on Shown
- `AdFrequencyStoreTest`: never shown → true; within cap → false; after cap → true
- `SettingsViewModelTest`: add entitlement state tests, buy click delegates to billing repo

## Verification

```bash
./gradlew assembleDebug
./gradlew :app:testDebugUnitTest
```

Manual: open detail screen → test interstitial shows; dismiss → screen loads; reopen within 30 min → no ad; Settings buy → billing flow launches; purchase → PromoCard replaced.
