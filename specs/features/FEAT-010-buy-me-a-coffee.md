# FEAT-010 — Buy Me A Coffee

## Summary

Add Google AdMob interstitial ads and Google Play Billing to Cartio. Free users see an interstitial ad when opening the Shopping List Detail screen (frequency-capped to once per 30 minutes). Users can purchase the `remove_ads_support` in-app product once to permanently remove all ads.

## Goals

- Monetize free users via AdMob interstitial ads without degrading UX
- Provide a one-time purchase path to remove ads forever
- Entitlement derives from Play Billing purchase state — not a local stored flag

## Requirements

### Ads
- Show an AdMob interstitial when the user opens Shopping List Detail (free users only)
- Preload the ad at app startup so it is ready on first navigation
- Cap frequency to at most once per 30 minutes
- If the ad is not ready or takes longer than 3 seconds, proceed to the screen immediately
- Users with the ad-free entitlement never see ads

### Billing
- Sell non-consumable managed product: `remove_ads_support`
- Launch the billing flow from the PromoCard "Buy coffee" button in Settings
- After purchase, PromoCard is replaced with a "Thank you" state
- Entitlement is refreshed at app startup by querying BillingClient

### Constraints
- All AdMob and Play product IDs are named constants — no hardcoded production IDs in source control at this stage (test IDs used)
- SharedPreferences for frequency cap (consistent with existing BackupPreferences pattern)
- Clean Architecture: all ad/billing logic lives in `features/monetization/`; UI layers never touch SDK classes directly

## Out of Scope

- Rewarded ads
- Server-side purchase verification
- Firebase Remote Config for ad tuning
- Multiple supporter tiers

## Test IDs (replace before Play Store release)

| Constant | Test Value |
|---|---|
| `ADMOB_APP_ID` | `ca-app-pub-3940256099942544~3347511713` |
| `ADMOB_INTERSTITIAL_UNIT_ID` | `ca-app-pub-3940256099942544/1033173712` |
| `REMOVE_ADS_PRODUCT_ID` | `remove_ads_support` |
