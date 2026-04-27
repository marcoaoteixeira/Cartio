# Google Ads + Google Play Billing (Ad-Free Entitlement) Architecture Plan

## Overview

This document consolidates the implementation plan for adding Google Ads to an Android app using Kotlin, Clean Architecture, MVVM, AdMob, and Google Play Billing for an ad-free supporter entitlement.

---

# Goals

## Advertising Requirements

- Show an ad when the user accesses a particular screen (example: shopping cart)
- Ad display should never block the user longer than 5–10 seconds (ideally 2–3 seconds timeout)
- Preload ads before navigation to avoid latency
- Add frequency capping to avoid degrading UX

## Monetization Requirement

Provide users a way to support the developer and remove ads permanently.

Recommended approach:

- Use Google Play Billing
- Sell a non-consumable managed product:

```text
remove_ads_support
```

Purchasing this product grants an **ad-free entitlement**.

---

# Recommended Ad Format

## Use Interstitial Ads

Show an interstitial when entering the cart screen, only when:

- User does not have ad-free entitlement
- Ad is preloaded and available
- Frequency cap allows showing it
- Timeout has not been exceeded

Avoid:

- Showing ads every cart visit
- Loading ads only when user opens cart
- Blocking navigation while waiting indefinitely

---

# AdMob Setup

## Add SDK

```kotlin
implementation("com.google.android.gms:play-services-ads:23.x.x")
```

## Manifest

```xml
<meta-data
 android:name="com.google.android.gms.ads.APPLICATION_ID"
 android:value="ca-app-pub-xxxxxxxx~yyyyyyyy"/>
```

## Initialize

```kotlin
class App : Application() {

   override fun onCreate() {
      super.onCreate()
      MobileAds.initialize(this)
   }
}
```

---

# Clean Architecture Structure

```text
presentation/
 ├── cart/
 │    ├── CartViewModel
 │    └── CartUiState
 │
 └── settings/
      └── SupportDeveloperViewModel


domain/
 ├── ads/
 │    ├── AdsRepository
 │    ├── ShowCartAdUseCase
 │    ├── PreloadCartAdUseCase
 │    └── ShouldShowAdUseCase
 │
 └── billing/
      ├── BillingRepository
      ├── HasAdFreeEntitlementUseCase
      └── PurchaseSupportUseCase


data/
 ├── ads/
 │    ├── AdMobDataSource
 │    ├── AdsRepositoryImpl
 │    └── AdFrequencyStore
 │
 └── billing/
      ├── BillingClientWrapper
      ├── BillingRepositoryImpl
      └── PurchaseVerifier
```

---

# Ads Domain

## Ads Repository

```kotlin
interface AdsRepository {

    suspend fun preloadCartInterstitial()

    suspend fun isAdAvailable(): Boolean

    suspend fun showCartInterstitial(
        activity: Activity
    ): AdResult

    suspend fun shouldShowAd(): Boolean

    suspend fun markAdShown()
}
```

---

## Ad Result Model

```kotlin
sealed class AdResult {
    data object Shown : AdResult()
    data object NotAvailable : AdResult()
    data object TimedOut : AdResult()
    data object DisabledForPremium : AdResult()
}
```

---

## AdMob Data Source

```kotlin
class AdMobDataSource {

   private var interstitial: InterstitialAd? = null

   suspend fun preload() {
      suspendCancellableCoroutine { continuation ->

         InterstitialAd.load(
            context,
            AD_UNIT,
            AdRequest.Builder().build(),
            object : InterstitialAdLoadCallback() {

               override fun onAdLoaded(
                   ad: InterstitialAd
               ) {
                  interstitial = ad
                  continuation.resume(Unit)
               }
            }
         )
      }
   }

   fun hasAd() = interstitial != null

   suspend fun show(
      activity: Activity
   ): AdResult {

      val ad = interstitial
          ?: return AdResult.NotAvailable

      return suspendCancellableCoroutine {

          ad.fullScreenContentCallback =
             object : FullScreenContentCallback() {

                override fun onAdDismissedFullScreenContent() {
                    interstitial = null
                    it.resume(AdResult.Shown)
                }
             }

          ad.show(activity)
      }
   }
}
```

---

## Frequency Capping

Use DataStore.

```kotlin
class AdFrequencyStore {

   suspend fun shouldShowAd(): Boolean {

      val lastShown = readTimestamp()

      return Duration.between(
          lastShown,
          Instant.now()
      ) > 30.minutes
   }

   suspend fun markShown() {
      saveTimestamp()
   }
}
```

---

## Ads Repository Implementation

```kotlin
class AdsRepositoryImpl(
   private val adMob: AdMobDataSource,
   private val frequencyStore: AdFrequencyStore
): AdsRepository {

   override suspend fun preloadCartInterstitial() {
      adMob.preload()
   }

   override suspend fun isAdAvailable() =
      adMob.hasAd()

   override suspend fun shouldShowAd() =
      frequencyStore.shouldShowAd()

   override suspend fun markAdShown() =
      frequencyStore.markShown()

   override suspend fun showCartInterstitial(
      activity: Activity
   ): AdResult {

      return withTimeoutOrNull(3000) {
         adMob.show(activity)
      } ?: AdResult.TimedOut
   }
}
```

---

# Billing as Source of Truth for Ad-Free Entitlement

## Important Principle

Do not store:

```kotlin
hasAdFree = true
```

Instead derive entitlement from ownership of:

```text
remove_ads_support
```

If user owns the product:

```text
user has ad-free entitlement
```

Benefits:

- survives reinstall
- survives device changes
- tied to Play account
- supports purchase restoration
- harder to tamper with

---

# Billing Repository

```kotlin
interface BillingRepository {

    val adFreeEntitlement: Flow<Boolean>

    suspend fun connect()

    suspend fun refreshEntitlements()

    suspend fun launchRemoveAdsPurchase(
        activity: Activity
    )
}
```

---

# Billing Implementation

## Wrapper

```kotlin
class BillingClientWrapper(
   private val context: Context
) {

   private val entitlementFlow =
      MutableStateFlow(false)

   private lateinit var billingClient: BillingClient

   fun adFreeEntitlement() =
      entitlementFlow.asStateFlow()
}
```

---

## Connect

```kotlin
suspend fun connect() {

   billingClient =
      BillingClient.newBuilder(context)
         .enablePendingPurchases()
         .setListener(::onPurchasesUpdated)
         .build()

   billingClient.startConnection(...)
}
```

---

## Query Purchases

```kotlin
suspend fun refreshEntitlements() {

   val purchases =
      billingClient.queryPurchasesAsync(
         QueryPurchasesParams.newBuilder()
             .setProductType(
                 BillingClient.ProductType.INAPP
             )
             .build()
      )

   entitlementFlow.value =
      purchases.purchasesList.any {
         it.products.contains(
            REMOVE_ADS_PRODUCT_ID
         ) &&
         it.purchaseState ==
            Purchase.PurchaseState.PURCHASED
      }
}
```

---

## Launch Purchase

```kotlin
suspend fun launchPurchase(
   activity: Activity
) {

   billingClient.launchBillingFlow(
      activity,
      billingParams
   )
}
```

---

## Acknowledge Purchases

```kotlin
private fun acknowledgeIfNeeded(
   purchase: Purchase
) {

   if (!purchase.isAcknowledged) {
      billingClient.acknowledgePurchase(...)
   }
}
```

Failure to acknowledge may cause automatic refund.

---

## Billing Repository Implementation

```kotlin
class BillingRepositoryImpl(
   private val billing: BillingClientWrapper
): BillingRepository {

   override val adFreeEntitlement =
      billing.adFreeEntitlement()

   override suspend fun connect() {
      billing.connect()
   }

   override suspend fun refreshEntitlements() {
      billing.refreshEntitlements()
   }

   override suspend fun launchRemoveAdsPurchase(
      activity: Activity
   ) {
      billing.launchPurchase(activity)
   }
}
```

---

# Use Cases

## Should Show Ad

```kotlin
class ShouldShowAdUseCase(
   private val adsRepo: AdsRepository,
   private val billingRepo: BillingRepository
) {

   suspend operator fun invoke(): Boolean {

      if (billingRepo.adFreeEntitlement.first())
         return false

      return adsRepo.shouldShowAd()
   }
}
```

---

## Show Cart Ad

```kotlin
class ShowCartAdUseCase(
    private val shouldShowAd: ShouldShowAdUseCase,
    private val adsRepo: AdsRepository
) {

   suspend operator fun invoke(
      activity: Activity
   ): AdResult {

      if (!shouldShowAd())
         return AdResult.DisabledForPremium

      return adsRepo.showCartInterstitial(activity)
   }
}
```

---

# Presentation Layer (MVVM)

## Cart ViewModel

```kotlin
class CartViewModel(
   private val showCartAd: ShowCartAdUseCase,
   private val preloadAd: PreloadCartAdUseCase
): ViewModel() {

   fun preloadAds() {
      viewModelScope.launch {
         preloadAd()
      }
   }

   fun onCartOpened(activity: Activity) {

      viewModelScope.launch {

         showCartAd(activity)

         loadCart()
      }
   }
}
```

UI never touches AdMob directly.

---

# Startup Flow

```kotlin
class AppStartupInitializer {

   suspend fun initialize() {

      billingRepository.connect()

      billingRepository.refreshEntitlements()

      adsRepository.preloadCartInterstitial()
   }
}
```

---

# Optional Cached Entitlement

Fast startup optimization:

```text
Cached entitlement = fast UX
Play Billing = source of truth
```

Use DataStore as temporary cache:

```kotlin
cached_entitlement=true
```

Then reconcile against Billing.

---

# Entitlement Modeling

Prefer:

```kotlin
sealed interface UserEntitlement {
   data object Free : UserEntitlement
   data object AdFreeSupporter : UserEntitlement
}
```

Avoid:

```kotlin
Boolean hasNoAds
```

More extensible for future supporter tiers.

---

# Optional Monetization Facade

Can simplify ViewModels further.

```kotlin
interface MonetizationFacade {

   suspend fun maybeShowCartAd(
      activity: Activity
   )

   suspend fun purchaseRemoveAds(
      activity: Activity
   )
}
```

---

# Dependency Injection (Hilt)

```kotlin
@Module
@InstallIn(SingletonComponent::class)
object AdsModule {

   @Provides
   fun provideAdsRepository(
      adMob: AdMobDataSource,
      store: AdFrequencyStore
   ): AdsRepository =
      AdsRepositoryImpl(adMob, store)
}
```

Repeat similarly for Billing.

---

# Testing Strategy

Use fakes:

```kotlin
FakeAdsRepository
FakeBillingRepository
```

Test:

- Premium users never see ads
- Timeout falls through to cart
- Frequency capping works
- Entitlement disables ads immediately after purchase

Most of this can be pure unit tests.

---

# Production Hardening (Recommended)

## Server-Side Verification

Better security:

```text
Purchase token
→ backend
→ verify with Google Play Developer API
→ return entitlement
```

Useful if revenue matters.

---

## Remote Config (Optional)

Use Firebase Remote Config for:

- ad frequency tuning
- global ad kill switch
- A/B tests
- timeout adjustments

---

# MVP Scope

## Phase 1

- AdMob interstitial ads
- Ad preload
- Timeout fallback
- Frequency capping
- Remove Ads purchase
- Billing-driven entitlement

## Phase 2

- Remote Config
- Server verification
- Mediation support
- Rewarded ads expansion

---

# Final Recommendation

Use:

- AdMob Interstitials for cart entry monetization
- Clean Architecture separation of Ads and Billing domains
- Google Play Billing purchases as the authoritative ad-free entitlement source
- Frequency caps + timeout protection for UX
- Optional backend verification if needed later

This scales cleanly while keeping monetization logic maintainable and testable.

