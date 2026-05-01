# FEAT-014 — Developer Log Screen

## Objective

Give developers visibility into app behavior on a real device without ADB. A hidden screen — unlocked by tapping the "Made with care" footer 10 times — displays all in-memory logs collected during the current app session.

## User Story

As a developer testing on a real device, I tap the "Made with care" label at the bottom of Settings 10 times. A Developer Logs screen opens showing every log entry captured since app launch, formatted in monospace for easy reading. I can clear the log or go back to Settings with the standard back arrow.

## Requirements

- Tapping the "Made with care" label 10 times navigates to the Developer Logs screen. The tap count resets on entry (and survives rotation via `rememberSaveable`).
- The version row's existing Play Store link behavior is unchanged.
- The "Made with care" label gets extra bottom margin so it sits clear of the device bottom edge.
- `AppLogger` captures every log call (debug, info, warn, error) in an in-memory ring buffer (max 500 entries). It also forwards to `android.util.Log` so logcat continues to work.
- The log screen lists entries newest-at-bottom, auto-scrolling to the latest on update.
- Each entry is formatted: `[HH:mm:ss.SSS] D/TAG: message` in a monospace font.
- Color coding: ERROR = error color, WARN = amber, INFO/DEBUG = default text.
- A "Clear" action in the TopAppBar wipes the buffer.
- Empty state shows "No logs yet."
- Navigation follows the existing detail-screen pattern: primary-color TopAppBar, back arrow → `navigateUp()`.

## Decisions

| Decision | Choice |
|---|---|
| Easter egg trigger | "Made with care" footer label — less prominent than a functional row, suits the hidden nature |
| Logger design | Kotlin `object AppLogger` — no Hilt, accessible from anywhere without injection overhead |
| Buffer cap | 500 entries; oldest dropped on overflow |
| Font | `FontFamily.Monospace` (Compose built-in) — no extra font asset needed |
| Test surface | Unit tests for `AppLogger` ring-buffer logic + `DevLogsViewModel` state emission |
