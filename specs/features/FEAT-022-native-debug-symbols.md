# FEAT-022 — Upload Native Debug Symbols

## Objective

Resolve Play Console's warning: *"This App Bundle contains native code, and you've not uploaded debug symbols."* Make future native crashes/ANRs symbolicated in Play Console's reports instead of showing raw memory addresses.

## Root cause

`app/build.gradle.kts`'s `release` build type has no `ndk { debugSymbolLevel = ... }` set. Several dependencies (`play-services-ads`, `android.billingclient`, Room's SQLite bindings) bundle native `.so` libraries. Without `debugSymbolLevel` configured, AGP strips symbols from the release AAB and never produces the separate native-debug-symbols file Play Console needs to decode native stack traces — hence the warning, confirmed by Google's own linked doc (`developer.android.com/studio/build/shrink-code#native-crash-support`).

## Fix

Set `ndk { debugSymbolLevel = "FULL" }` inside the `release` build type. This makes AGP package a native-debug-symbols zip alongside the AAB at build time; Play App Signing picks it up automatically on upload (via Android App Bundle's embedded symbol table) — no manual upload step needed for future releases built this way.

`FULL` (vs `SYMBOL_TABLE`) keeps full debugging info (local variable names, inlined function info), useful for actually reading stack traces during development; `SYMBOL_TABLE` is a smaller, function-name-only alternative. Given Cartio is a small app, the size difference is negligible — `FULL` is the better default for debuggability.

## Decisions

| Decision | Choice |
|---|---|
| Symbol level | `FULL` — full debugging fidelity, size cost negligible for this app |
| Scope | `release` build type only — debug builds aren't distributed via Play Console, no need to symbolicate those |
