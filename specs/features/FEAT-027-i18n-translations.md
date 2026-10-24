# FEAT-027 — App Translations (en-US, pt-BR, es-ES, it-IT, fr-FR)

## Objective

Make Cartio's UI readable in the user's own language. Ship translated string
resources for Portuguese (Brazil), Spanish (Spain), Italian and French,
alongside the existing English baseline, so the OS serves the matching
language automatically based on device locale.

## User Stories

1. A user with their device language set to Portuguese, Spanish, Italian or
   French opens Cartio and sees the whole UI (list screens, settings,
   scan-tag flow, dialogs, snackbars) in that language.
2. A user on any other device language continues to see English (the default
   `values/strings.xml`), unchanged from today.
3. A user with an unsupported language set to a region variant (e.g.
   `pt-PT`, `es-MX`) still gets the closest supported language resource
   (Android's resource resolution falls back `pt-PT` → `pt`, etc.) rather than
   English, since no region-qualified variant is being shipped yet.
4. A user can open Settings → Language and pick a language explicitly
   (System default, English, Português, Español, Italiano, Français),
   overriding the device locale for Cartio only — other apps are unaffected.
   The choice persists across app restarts.

## Requirements

- Translate all `translatable` strings in `app/src/main/res/values/strings.xml`
  (105 entries, 1 `<plurals>`) into `values-pt/strings.xml`,
  `values-es/strings.xml`, `values-it/strings.xml`, `values-fr/strings.xml`.
  Language-only qualifiers (no region), since no second dialect of any of
  these four is planned — word choice targets the requested dialects
  (pt-BR, es-ES, it-IT, fr-FR) directly.
- `admob_app_id` / `admob_unit_id` stay `translatable="false"` and are **not**
  duplicated into the new files (Android falls back to default `values/` for
  any string missing from a locale file — no need to re-list non-translatable
  entries).
- Fix the one pre-existing hardcoded literal (`ShoppingListScreen.kt:434`,
  `Text("Delete")`) to use `stringResource(R.string.action_delete)` before
  translating — otherwise that menu item can never be translated.
- `<plurals name="shopping_list_item_count">` ports its `one`/`other`
  quantity categories as-is into all four new locales — pt, es, it and fr
  all use the same two-category plural rule as English, so no category
  restructuring is needed, only text.
- In-app language picker lives in Settings, under a new "Language" section:
  a tappable row (current language as subtitle) opens a dialog listing
  System default + the five supported languages as radio options.
- Picker is implemented with `androidx.appcompat`'s per-app language API
  (`AppCompatDelegate.setApplicationLocales`), already a project dependency
  (1.8.0) — no new library needed. This API persists the choice itself
  (via platform `LocaleManager` on API 33+, AppCompat's own storage below
  that) and survives process death / app restart without any custom
  SharedPreferences code.
- Each language's own name in the picker is shown as its endonym (e.g.
  "Português", "Español") and is **not** translated per locale —
  `translatable="false"`, same string in every `values-xx/strings.xml` —
  matching how Android/iOS system language pickers conventionally work, so
  users can always recognize their language regardless of the app's
  current display language.
- No new permissions. One new runtime dependency already present
  (`androidx.appcompat`, already declared, just newly *used*). New DI
  binding (`AppLanguagePreferences`) following the project's existing
  `BackupPreferences` interface+impl+Hilt-binding pattern.
- `./gradlew lint` must come back clean on `MissingTranslation` and
  `ExtraTranslation` (Android Lint's built-in resource-parity checks) for all
  five locales — this is the acceptance gate instead of a new automated test,
  since there's no runtime logic to unit test.

## Out of scope

- Store listing translations (Play Console title/short description/full
  description/screenshots) — separate system, not `strings.xml`, not covered
  here.
- Any additional languages beyond pt, es, it, fr.
- RTL layout support (none of the four shipped languages are RTL).
- Translation management platform (Crowdin/Lokalise) — revisit only if the
  app grows past this language count or gains outside contributors.

## Translation mechanism

Translations for this MVP are produced directly by Claude (in this session)
from the English source strings, with shopping/grocery-app context in mind,
and written straight into the four new `values-xx/strings.xml` files. No new
infra, no cost, no build-time dependency. Rationale and alternatives
considered are recorded in the Decisions table below.

## Decisions

| Decision | Choice |
|---|---|
| Translation mechanism | Claude translates `strings.xml` directly, now — no Play Console translation service (that targets store listing text, not in-app resources), no Cloud Translation API (overkill plumbing for ~105 static strings), no third-party TMS yet (revisit if scale grows) |
| Resource qualifiers | Language-only (`values-pt`, `values-es`, `values-it`, `values-fr`), no region qualifier — each language ships one dialect only |
| In-app language override | Yes — Settings → Language picker, via `AppCompatDelegate.setApplicationLocales` (already-present `androidx.appcompat` dependency, no new library) |
| Non-translatable strings (AdMob IDs) | Left only in default `values/`, not duplicated |
| Plural categories | Unchanged (`one`/`other`) across all four locales |
| Acceptance check | `./gradlew lint` clean on `MissingTranslation`/`ExtraTranslation` |
