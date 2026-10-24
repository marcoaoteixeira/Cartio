# FEAT-027 — Implementation Plan

## Steps

1. **Fix hardcoded literal** — `ShoppingListScreen.kt:434`: replace
   `Text("Delete")` with `Text(stringResource(R.string.action_delete))`.
   Must land before translating, otherwise this menu item is permanently
   English.
2. **Confirm plural rule compatibility** — pt, es, it all use CLDR's
   standard two-category (`one`/`other`) plural rule, same as English, so
   `shopping_list_item_count` ports with only text changes, no category
   additions/removals.
3. **Create `app/src/main/res/values-pt/strings.xml`** — full translation of
   every `translatable` string (104 of 105 entries — excludes the two
   `translatable="false"` AdMob IDs), Brazilian Portuguese word choice,
   preserving all `%1$s`/`%d` format specifiers, `&amp;`/`&#8230;` entities,
   and the `<plurals>` block structure exactly.
4. **Create `app/src/main/res/values-es/strings.xml`** — same, Spain Spanish.
5. **Create `app/src/main/res/values-it/strings.xml`** — same, Italian.
5a. **Create `app/src/main/res/values-fr/strings.xml`** — same, French.
6. **Verify format-specifier parity** — every `%1$s`, `%2$d`, `%d` etc. in
   each translated string must match the position/type of its English
   source (a reordered sentence can still need `%2$s %1$d` — positional
   specifiers handle this; just confirm each file's specifiers are a set-match
   against the source string they translate).
7. **Run `./gradlew lint`** — confirm no `MissingTranslation` or
   `ExtraTranslation` warnings across `values-pt`, `values-es`, `values-it`,
   `values-fr`.
8. **Manual spot-check in emulator** — switch emulator system language
   (Settings → System → Languages) to pt-BR, es-ES, it-IT, fr-FR in turn; walk
   Shopping List, Detail, Settings, Scan-tag flow; confirm no English leaks
   through and no text overflow/truncation in buttons or dialogs (longer
   translated strings are the main visual risk).

## In-app language picker (added after initial translation pass)

9. **Add picker strings** (default `values/strings.xml`, translated into all
   four locales except the five language-name strings, which are
   `translatable="false"` endonyms shared across every locale):
   `settings_section_language`, `settings_language_title`,
   `settings_language_dialog_title`, `language_system_default`,
   `language_english`, `language_portuguese`, `language_spanish`,
   `language_italian`, `language_french`.
10. **`core/i18n/AppLanguage.kt`** — enum with a nullable BCP-47 `tag`
    (`null` = system default) and a `@StringRes labelRes`, plus a
    `fromTag(String?)` lookup used to reconstruct current selection from
    `AppCompatDelegate.getApplicationLocales()`.
11. **`features/settings/data/AppLanguagePreferences.kt`** — interface
    (`currentLanguage: StateFlow<AppLanguage>`, `setLanguage(AppLanguage)`)
    + `AppLanguagePreferencesImpl` wrapping `AppCompatDelegate`, following
    the existing `BackupPreferences`/`BackupPreferencesImpl` pattern so the
    ViewModel can be tested against a fake instead of the real Android API.
12. **`di/LanguageModule.kt`** — `@Binds` `AppLanguagePreferencesImpl` to
    `AppLanguagePreferences`, mirroring `BackupModule`.
13. **`SettingsViewModel`** — inject `AppLanguagePreferences`; expose
    `currentLanguage`, `showLanguageDialog: StateFlow<Boolean>`; add
    `requestLanguagePicker()`, `dismissLanguageDialog()`,
    `selectLanguage(AppLanguage)` (sets the preference and closes the
    dialog) — written test-first per the TDD requirement in CLAUDE.md,
    against a `FakeAppLanguagePreferences` added alongside the existing
    `FakeBackupPreferences` in `SettingsViewModelTest`.
14. **`SettingsScreen`/`SettingsComponents`** — new "Language" section
    (`SettingsListItem`, subtitle = current language's endonym, chevron,
    opens picker) + a dialog listing all `AppLanguage` entries as radio
    options, consistent with the existing `ClearDataDialog` dialog pattern.
15. **Manual verification** — picking a language in the emulator recreates
    the UI in that language immediately (AppCompat triggers recreation);
    confirm the choice survives `am force-stop` + relaunch, and that
    "System default" correctly reverts to the device locale.

## Edge cases (picker)

- **ComponentActivity, not AppCompatActivity — confirmed broken as
  documented-but-untrue.** `AppCompatDelegate.setApplicationLocales()` alone
  silently no-ops in this app: verified empirically (`adb shell cmd locale
  get-app-locales` stayed `[]` after calling it, no exception, no log). Root
  cause: Cartio has zero `AppCompatActivity` instances anywhere (MainActivity
  is a plain `ComponentActivity`), and AppCompatDelegate's static
  per-app-language path appears to depend on that machinery being
  initialized to actually drive the platform `LocaleManager`, even on API
  33+ where in theory it's a direct passthrough. **Fix applied**:
  `AppLanguagePreferencesImpl` now calls the platform
  `LocaleManager.setApplicationLocales()` directly via
  `@ApplicationContext` on API 33+ (`Build.VERSION_CODES.TIRAMISU`), and
  keeps `AppCompatDelegate` only as the API 29-32 fallback (untested on
  real <33 hardware in this session — the emulator used reports SDK 37 —
  so that branch is unverified in practice, only compiled and
  reasoned-through; flag for extra scrutiny the first time it needs to run
  on a real API 29-32 device).
- **Picking the already-selected language** — should be a no-op (dialog
  just closes), not force an unnecessary recreate.
- **"System default" after the device locale itself changes** — not
  re-tested here (out of scope); Android re-resolves `values-xx/` resources
  against the new device locale automatically whenever "System default" is
  selected, same as today's default behavior.

## Edge cases

- **Format-specifier mismatch** — a translator (human or AI) reordering a
  sentence can accidentally drop or duplicate a `%1$s`/`%d` token, which
  crashes at runtime (`MissingFormatArgumentException`) the first time that
  string is formatted, not at lint/build time for all cases — double-check
  by eye in step 6.
- **Plural-sensitive numbers in translated languages** — Portuguese,
  Spanish and Italian all treat "1" as the `one` category and everything
  else (including 0) as `other`, matching the existing English structure —
  no special-case needed, but worth confirming during the manual check in
  step 8 (list with 0 items, 1 item, 2+ items).
- **Text overflow** — translated strings (especially pt-BR) commonly run
  15-30% longer than English. Buttons/badges with fixed-width or
  single-line truncation (`promo_badge`, `shopping_in_progress_label`,
  `detail_swipe_check_label`/`detail_swipe_delete_label` — short all-caps
  labels) are the highest-risk spots; flag any visual clipping found in
  step 8 as a follow-up (layout fix), not a blocker for this translation
  PR, since fixing it is a UI-width concern separate from the translation
  itself — unless it's bad enough to be unusable, in which case fix inline.
- **`values-night` isn't a locale** — don't confuse Android's existing
  `values-night/` (dark theme colors) with a locale qualifier; translations
  are unrelated to that directory and must not touch it.
- **Device region ≠ shipped qualifier** — a device set to `pt-PT` or
  `es-MX` resolves to `values-pt`/`values-es` (language-only match), not
  English — confirmed working automatically by Android's resource
  resolution, no extra qualifier needed for this MVP.
