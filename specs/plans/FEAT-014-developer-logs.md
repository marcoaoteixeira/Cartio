# Plan: FEAT-014 — Developer Log Screen

## Implementation Order

1. **`core/logging/LogEntry.kt`** — `LogLevel` enum (DEBUG, INFO, WARN, ERROR) + `LogEntry(timestamp, level, tag, message)` data class.
2. **`core/logging/AppLogger.kt`** — Kotlin `object`. `MutableStateFlow<List<LogEntry>>` backed buffer, capped at 500. Public `d/i/w/e(tag, msg, throwable?)` — each forwards to `android.util.Log` and appends to buffer. `clear()` resets.
3. **Tests (write first — TDD):**
   - `AppLoggerTest.kt` (`test/`) — ring-buffer cap, `clear()`, entry field correctness.
   - `DevLogsViewModelTest.kt` (`test/`) — entries StateFlow emits after log calls; emits empty after `clearLogs()`.
4. **Migrate existing Log calls** — `CartioApp.kt` and `BillingClientWrapper.kt`: `Log.e` → `AppLogger.e`.
5. **`features/devlogs/ui/DevLogsViewModel.kt`** — `@HiltViewModel`. Exposes `entries: StateFlow<List<LogEntry>>` via `AppLogger.entries.stateIn(...)`. `clearLogs()` delegates to `AppLogger.clear()`.
6. **`features/devlogs/ui/DevLogsScreen.kt`** — Scaffold + primary TopAppBar with back arrow + trailing clear icon. `LazyColumn` of entries. Monospace font, color-coded by level. Auto-scroll to bottom on new entry. Empty state.
7. **`navigation/CartioDestinations.kt`** — add `data object DevLogs : CartioDestinations("dev_logs")`.
8. **`navigation/CartioNavGraph.kt`** — add `composable(CartioDestinations.DevLogs.route) { DevLogsScreen(...) }`; update `SettingsScreen` call to pass `onNavigateToDeveloperLogs`.
9. **`features/settings/ui/SettingsScreen.kt`**:
   - Add `onNavigateToDeveloperLogs: () -> Unit = {}` param.
   - Add `var tapCount by rememberSaveable { mutableIntStateOf(0) }`.
   - Make the "made with care" `Text` clickable (no ripple) — count taps; on 10 reset + navigate.
   - Increase the trailing `Spacer` from `8.dp` → `24.dp`.
10. **Spec/plan/README** — this spec, this plan, update `specs/README.md` FEAT-014 row to Done.

## Edge Cases

- **Buffer overflow**: `takeLast(500)` on every append — oldest entry is silently dropped.
- **Rotation during counting**: `rememberSaveable` preserves `tapCount` across config changes.
- **Rapid taps**: `StateFlow.update` is thread-safe; no race condition on concurrent log calls.
- **Empty log on first open**: empty-state composable shown; no crash from `scrollToItem(-1)` — guarded by `isNotEmpty()` check.
- **Throwable formatting**: `throwable?.stackTraceToString()` appended to message string before storage.

## Files Modified / Created

| File | Change |
|---|---|
| `core/logging/LogEntry.kt` | New |
| `core/logging/AppLogger.kt` | New |
| `features/devlogs/ui/DevLogsViewModel.kt` | New |
| `features/devlogs/ui/DevLogsScreen.kt` | New |
| `navigation/CartioDestinations.kt` | Add `DevLogs` |
| `navigation/CartioNavGraph.kt` | Wire new screen + update Settings call |
| `features/settings/ui/SettingsScreen.kt` | Easter egg + spacing |
| `CartioApp.kt` | `Log.e` → `AppLogger.e` |
| `features/monetization/data/BillingClientWrapper.kt` | Log calls → AppLogger |
| `test/.../AppLoggerTest.kt` | New unit test |
| `test/.../DevLogsViewModelTest.kt` | New unit test |
| `specs/features/FEAT-014-developer-logs.md` | This spec |
| `specs/plans/FEAT-014-developer-logs.md` | This plan |
| `specs/README.md` | Add FEAT-014 row |

## Verification

```bash
./gradlew test              # AppLoggerTest + DevLogsViewModelTest pass
./gradlew assembleDebug     # compiles clean
```

Manual on device:
1. Tap "Made with care" 9× — nothing happens, Play Store not opened.
2. Tap 10th time — Developer Logs screen opens.
3. Startup logs visible, newest at bottom, monospace font.
4. Tap trash — list clears, empty state shown.
5. Back arrow — returns to Settings.
6. Rotate mid-count — count is preserved.
