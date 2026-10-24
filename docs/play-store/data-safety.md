# Play Console Data Safety — Mapping for Cartio

Use this as a direct reference while filling out Play Console's **App content → Data safety** form. Every row below is traced to either Google's own AdMob SDK disclosure or a specific place in this codebase — nothing here is generic boilerplate.

Sources checked: Google's AdMob/Play data disclosure page for Mobile Ads SDK 25.5.0 (`com.google.android.gms:play-services-ads`, pinned in `gradle/libs.versions.toml`); code audit of `features/monetization/`, `core/database/`, and `core/scanner/` (FEAT-026).

---

## Step 1 — Does your app collect or share any required user data types?

**Yes.** The Google Mobile Ads SDK (AdMob), bundled for the interstitial ads that support the free version of Cartio, automatically collects and shares some data. Cartio's own code collects none.

## Step 2 — Data types to declare

| Data type | Collected? | Shared? | Optional? | Purpose | Source |
|---|---|---|---|---|---|
| Device or other IDs (advertising ID, app set ID) | Yes | Yes | Yes — user can disable ad ID collection / enable Limited Ads in device settings | Advertising or marketing, Analytics, Fraud prevention, security, and compliance | AdMob SDK (automatic) |
| Approximate location (derived from IP address) | Yes | Yes | Not user-controllable within the app | Advertising or marketing, Analytics, Fraud prevention, security, and compliance | AdMob SDK (automatic) |
| App activity → App interactions (ad views/clicks) | Yes | Yes | Not user-controllable within the app | Advertising or marketing, Analytics, Fraud prevention, security, and compliance | AdMob SDK (automatic) |
| App info and performance → Diagnostics | Yes | Yes | Not user-controllable within the app | Advertising or marketing, Analytics, Fraud prevention, security, and compliance | AdMob SDK (automatic) |

All four rows above come straight from Google's own disclosure for the Mobile Ads SDK — Cartio's code never touches this data directly; the SDK collects and transmits it to Google.

### Explicitly NOT collected (do not declare)

| Data type | Why not |
|---|---|
| Purchase history | Google Play Billing processes and stores purchase records on Google's servers. Cartio never persists a purchase token, product ID, or purchase state locally — `BillingClientWrapper` (`features/monetization/data/BillingClientWrapper.kt`) holds entitlement status only in an in-memory `StateFlow`, re-fetched via `queryPurchasesAsync()` each session. Nothing is written to `SharedPreferences`, `DataStore`, or Room. |
| Name, email address, phone number, physical address | No account system exists anywhere in the app (verified: no login/signup flow, no user-identity fields in the Room schema). |
| Financial info (beyond purchase, already excluded above) | Payment is handled entirely by Google Play Billing; the app never sees card/payment details. |
| Photos/videos (camera) | `CAMERA` permission (FEAT-026, "scan tag to add item") is used only to run on-device ML Kit text recognition on a single captured frame. The frame is decoded in memory (`RecognizeTextFromImageUseCase`, `core/scanner/`), passed to the on-device recognizer, and discarded immediately after — never written to disk, never uploaded, never leaves the device. |
| Health, fitness, messages, audio, files/docs, calendar, contacts | App has no permissions or code paths that touch any of these. |
| Web browsing history, search history | Not applicable — Cartio has no browser or search feature. |
| App activity → In-app search history, installed apps | Not collected — Cartio has no app-level analytics beyond what AdMob collects automatically. |

## Step 3 — Data security practices

| Question | Answer | Basis |
|---|---|---|
| Is all user data encrypted in transit? | Yes | AdMob SDK uses TLS (per Google's own disclosure page); Google Play Billing traffic is Google-managed and TLS-encrypted. |
| Do you provide a way for users to request data deletion? | Not applicable for Cartio's own data (no account/server exists to delete from) — local data can be erased any time via **Settings → Clear all data** (`FEAT-013`) or by uninstalling the app. Ad-related data is managed by the user through their Google Account's ad settings. | Verified: `settings_clear_data_title` wipes the local Room database. |
| Is the app's data handling independently reviewed against a security standard? | No | No such review exists; answer honestly as "No" unless one is obtained before submission. |

## Note on Android Auto Backup (not a Data Safety declaration)

`AndroidManifest.xml` sets `android:allowBackup="true"`, so a user's local database (shopping lists, items, expense records) can be included in **their own** Android device backup to **their own** Google Account — standard OS-level backup, not Cartio sharing data with a third party. This does not need to be declared as data sharing in the Data Safety form (same as the overwhelming majority of Android apps that leave Auto Backup on). It is already disclosed in plain language in `docs/privacy-policy.html` under "Android device backup."

## Children's policy note

Cartio is not directed at children per `docs/privacy-policy.html`. If Play Console asks whether the app is designed for children, answer **No** — there is no age-gating, child-directed content, or COPPA-relevant data collection.

---

**If the app's data behavior changes** (e.g. a future Google Drive sync feature ships, or a new SDK is added), this document and the Play Console form must both be updated in the same change — see `docs/privacy-policy.html`'s "Changes to this policy" section for the same commitment.
