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

## Verified finding (post-fix)

Built `bundleRelease` and inspected the output. The fix runs correctly (`extractReleaseNativeDebugMetadata` task executes), but Cartio's only bundled native library — `libandroidx.graphics.path.so` (pulled in transitively, likely via Compose) — is shipped **pre-stripped** by its publisher (`file` confirms: `ELF ... stripped`, no DWARF debug sections). AGP's extraction step correctly finds nothing to package, so no `BUNDLE-METADATA/com.android.tools.build.debugsymbols/` entry appears in the AAB, and the Play Console warning may persist specifically for this one library.

This is a characteristic of the upstream artifact, not a fixable app-config issue — no setting here can produce symbols Google didn't publish for their own library. The fix is still correct and necessary: it will capture symbols for any native code that does ship them, now or in a future dependency.
