# FEAT-020 — Data Safety Form Mapping

## Objective

Produce a reference document mapping Cartio's actual data behavior to Google Play Console's Data Safety form, so the form can be filled out accurately — not generically — before submission.

## Research basis

- Google's own AdMob/Play data disclosure page (`developers.google.com/admob/android/privacy/play-data-disclosure`, covering the Mobile Ads SDK version this app uses, 25.5.0) lists what the SDK itself collects and shares automatically: IP address (for approximate location), user product interactions, diagnostics, and device/account identifiers (advertising ID, app set ID).
- No equivalent Google page exists for Play Billing specifically — billing is a first-party Google Play service, not a third-party SDK with its own data disclosure page.
- Verified by code audit (not assumed) that Cartio **does not persist any purchase data locally**: `BillingClientWrapper` (`features/monetization/data/`) holds entitlement state only in an in-memory `StateFlow`, re-populated each session via `queryPurchasesAsync()`. No `SharedPreferences`, `DataStore`, or Room table stores a purchase token, product ID, or purchase history. (`AdFrequencyStore`'s `SharedPreferences` usage is unrelated — it only stores ad-frequency-cap timestamps, no personal or purchase data.)
- Verified Room schema (`core/database`) holds no personal-information fields — shopping lists, items, and expense records only.
- Verified (from the FEAT-016 dependency audit) no analytics or crash-reporting SDK exists in the dependency graph beyond AdMob.

## Deliverable

`docs/play-store/data-safety.md` — a row-by-row mapping developers can read directly against Play Console's Data Safety form sections (Data collection and security, Data types, individual category questions).

## Decisions

| Decision | Choice |
|---|---|
| Scope | Document only what's verifiable from the code + Google's own SDK disclosures — no speculative categories |
| Billing data | Declared as NOT collected by Cartio itself, with the reasoning spelled out (Google Play Billing processes and stores purchases server-side; the app never persists purchase data) |
| Android Auto Backup | Noted as OS-level user backup, not developer-side third-party sharing — explained so the form isn't over-declared |
| Format | Markdown, mirrors Play Console's own form structure so it can be filled out top-to-bottom |
