# FEAT-017 — Privacy Policy

## Objective

Publish a privacy policy at a public URL, as required by Google Play Console before an app using ads and in-app billing can be submitted.

## Decisions

| Decision | Choice |
|---|---|
| Hosting | GitHub Pages, served from `docs/` on `master` |
| URL | `https://minicore-software.github.io/Cartio/privacy-policy.html` (updated 2026-10-20 after repo ownership transfer from `marcoaoteixeira` to `minicore-software`) |
| Contact email | `minicore.software@gmail.com` |
| Publisher name | Minicore Software |
| Format | Standalone static HTML (no build step, no JS dependency) |

## What the policy must cover (derived from actual app behavior, not assumed)

Verified against the current codebase before drafting — no data behavior is invented:

- **No account, no login.** The app has no sign-in flow of any kind.
- **Local-first storage.** Shopping lists, items, and expense records live in a local Room database (`core/database`). Nothing is transmitted to any server the developer controls — there is no backend.
- **Android Auto Backup.** `AndroidManifest.xml` sets `android:allowBackup="true"` with `dataExtractionRules`/`backup_rules`. The device's local database is included in the user's own Android device backup to their Google Account, managed entirely by Android OS / Google, not by the developer.
- **Advertising (AdMob).** `com.google.android.gms:play-services-ads` serves interstitial ads (`features/monetization`). Per Google's own disclosures, the Google Mobile Ads SDK collects device/advertising identifiers and usage data for ad serving and measurement. The developer does not receive or store any of this directly.
- **In-app purchase (Google Play Billing).** `com.android.billingclient:billing-ktx` handles the one-time "Buy Me A Coffee" ad-removal purchase (`cartio_buy_me_a_coffee_support`). Purchase processing, payment details, and billing history are handled entirely by Google Play; the app only receives a purchase/entitlement status.
- **No analytics or crash-reporting SDK** is present in the dependency graph (verified: no Firebase, no Crashlytics, no third-party analytics library).
- **Permissions.** Only `android.permission.INTERNET` is declared — required for ads and billing network calls.
- **Children.** Not directed at children; no age-gating needed since no personal data is collected directly by the app.
- **Future (not yet shipped, mention as "may introduce"):** optional, user-initiated Google Drive sync (ADR-004 scaffolding exists, feature not built) — if/when shipped, this document will need a revision describing what's uploaded and user control over it.

## Non-goals

This is not a lawyer-reviewed legal document — it's a good-faith, accurate disclosure sufficient for Play Console's requirement and for user transparency, matching exactly what the shipped code does. If the app's data behavior changes (e.g. Drive sync ships), this doc must be revised in the same PR as that feature.
