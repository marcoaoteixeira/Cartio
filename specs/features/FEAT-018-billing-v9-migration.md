# FEAT-018 — Play Billing Library 8 → 9 Migration

## Objective

Move off Play Billing Library 8.0.0 (`com.android.billingclient:billing-ktx`) to 9.1.0 before Google's enforcement deadline, without regressing the existing "Buy Me A Coffee" ad-removal purchase flow.

## Finding — scope is smaller than initially assumed

This item was deferred out of FEAT-016 on the assumption that PBL9 requires real application-code migration (per Google's generic migration guide, which documents several removed legacy APIs: `SkuDetails`, `SkuDetailsParams`, `querySkuDetailsAsync`, `QueryPurchaseHistoryParams`, the no-arg `enablePendingPurchases()`, `getSkuDetailsList`/`setSkuDetailsList`).

Auditing this codebase's actual usage (`features/monetization/data/`, `di/MonetizationModule.kt`) shows **none of the removed APIs are used anywhere**:

- `BillingClientWrapper` already calls `queryPurchasesAsync(QueryPurchasesParams, ...)` (typed overload, not the removed `(String, ...)` form).
- `ProductDetailsResponseListener.onProductDetailsResponse` already uses the `QueryProductDetailsResult` signature, not a raw list.
- Purchase flow already uses `ProductDetails` + `BillingFlowParams.ProductDetailsParams` — no `SkuDetails` anywhere.
- `MonetizationModule.provideBillingClient` already calls `enablePendingPurchases(PendingPurchasesParams.newBuilder().enableOneTimeProducts().build())` — the typed overload, not the removed no-arg form.
- No `querySkuDetailsAsync`, `queryPurchaseHistoryAsync`, or `consumeAsync` calls exist.
- No code branches explicitly on `BillingResponseCode.ERROR` in a way the v9 `ERROR`→`BILLING_UNAVAILABLE` reclassification (for a blocked Play Store) would silently break.

So this is a **version-coordinate bump**, verified safe by inspection, not a rewrite.

## Target

| Dependency | Current | Target |
|---|---|---|
| `com.android.billingclient:billing-ktx` | 8.0.0 | 9.1.0 (verified latest stable via Google Maven metadata) |

## Behavior changes to be aware of (no code change required, but worth knowing)

- `launchBillingFlow()`'s `BillingResult` can now carry new sub-response codes (`PAYMENT_DECLINED_DUE_TO_INSUFFICIENT_FUNDS`, `USER_INELIGIBLE`, `NO_APPLICABLE_SUB_RESPONSE_CODE`). `BillingClientWrapper.launchPurchase` doesn't inspect the result code today (fire-and-forget past `launchBillingFlow` call) — unaffected, no regression, but a future UX improvement could surface these to the user.
- A blocked Play Store (e.g. OEM kids mode) now reports `BILLING_UNAVAILABLE` instead of `ERROR`. No code depends on the old value.
- `DeveloperProvidedBillingDetails.getLinkUri()` nullability change — not used in this codebase (that class relates to subscription installment flows; Cartio only sells a one-time in-app product).

## Verification

- `./gradlew assembleDebug`, `./gradlew assembleRelease`, `./gradlew test`, `./gradlew lint` all pass.
- Existing `BillingClientWrapperTest` (via `FakeBillingClientFacade`) continues to pass unmodified — it fakes the facade interface, not the SDK, so it's insulated from the SDK version.
