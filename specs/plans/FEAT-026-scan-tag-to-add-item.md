# Plan: FEAT-026 — Scan Market Tag to Add Item

## Sample tag observations (`resources/product-tags/`)

Reviewed samples are electronic shelf labels (ESL), not printed paper — confirms design choices already in the spec:
- Product name is frequently **abbreviated/truncated** by the retailer (e.g. `MSSN CARB BAL FLR TRT 8CT`) — OCR will return the abbreviation, not a clean name. The editable AddItemBar pre-fill (not auto-add) is required, not optional polish.
- Tags are multi-block by nature: brand line, product-name line, size/weight, price (large digits + small cents), per-unit price, barcode digits, store codes, promo badges. No reliable heuristic picks "the name" block automatically — confirms the plan to show all recognized blocks and let the user tap the right one.
- Digital-display glare/reflection is visible in samples — worth a one-line UI hint ("avoid glare") in the scan screen, not a blocking requirement.
- These samples are useful as `androidTest`/manual fixtures for the OCR flow but are not product photos — don't wire them into production assets.

## Implementation Order

1. Gradle: add `com.google.mlkit:text-recognition:16.0.1` (bundled) to `app/build.gradle`.
2. `core/scanner/RecognizeTextFromImageUseCase` — wraps `TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)`, takes an `InputImage`, returns `List<String>` text blocks (suspend, wraps the Task-based ML Kit API via `await()`/`suspendCancellableCoroutine`).
3. `core/scanner/FeatureFlags` (or extend existing flag mechanism if one exists — check `core/` first) — `SCANNER_ENABLED` local toggle, default `false`.
4. `features/shopping/ui/ScanTagViewModel` — states: `Idle`, `PermissionDenied`, `Capturing`, `Processing`, `Result(blocks: List<String>)`, `Error`. Calls `RecognizeTextFromImageUseCase`, discards the captured bitmap/ImageProxy immediately after OCR completes (no disk write).
5. `features/shopping/ui/ScanTagScreen` — CameraX `PreviewView` + `ImageCapture` use case, capture button, permission rationale UI, result block list (capped ~20, scrollable), "No text found" + Retry/"Enter manually" states.
6. Navigation — add `ScanTag(listId)` destination, wire result back to `ShoppingListDetailScreen` (simplest: `NavController` saved-state handle or a shared `ScanResultViewModel` scoped to the nav graph — pick saved-state handle to keep it simple, matches existing nav patterns in the project).
7. `ShoppingListDetailScreen` / `AddItemBar` — when `SCANNER_ENABLED`, replace the leading `Icons.Rounded.ShoppingCart` with a tappable `Icons.Rounded.CameraAlt` that navigates to `ScanTagScreen` (no new icon added beside "+" — avoids crowding the bar); flag off keeps today's static cart icon. On returning from `ScanTagScreen` with a selected block, set `itemInputText` to it (user still taps "+" to confirm — no auto-add).
8. `AndroidManifest.xml` — add `<uses-permission android:name="android.permission.CAMERA"/>` and `<uses-feature android:name="android.hardware.camera" android:required="false"/>` (required false — app must work on camera-less devices, scanner entry simply unavailable).
9. Runtime permission request flow (Accompanist not in use elsewhere in this project — check; if not present, use `ActivityResultContracts.RequestPermission` directly, no new dependency).
10. String resources (`scan_title`, `scan_no_text_found`, `scan_permission_rationale`, `scan_retry`, `scan_enter_manually`, content descriptions).
11. `docs/play-store/data-safety.md` — add CAMERA permission row: "not collected", on-device OCR only, image discarded immediately after processing, reasoning spelled out same as existing billing-data row.
12. Tests — `ScanTagViewModelTest` (state transitions, empty-result handling, use case failure handling — fake `RecognizeTextFromImageUseCase`, no real camera/ML Kit needed), `RecognizeTextFromImageUseCaseTest` if feasible to fake the ML Kit client boundary, light manual verification against `resources/product-tags/` samples.

## Key Data Flow

```
AddItemBar leading icon: cart icon → camera icon (swap gated by SCANNER_ENABLED)
  → tap camera icon → navigate(ScanTag(listId))
  → ScanTagScreen
       ↓ request CAMERA permission if not granted
       ↓ CameraX preview + capture button
       ↓ user taps capture → ImageCapture.takePicture() → single frame
       ↓ ScanTagViewModel.processImage(frame)
            → RecognizeTextFromImageUseCase(InputImage.fromBitmap/fromMediaImage)
            → frame discarded immediately after call returns
       ↓ Result(blocks) shown as tappable list, or Error/"no text found" state
  → user taps a block → saved-state handle set → navigateUp()
  → ShoppingListDetailScreen reads saved-state handle → itemInputText = selected block
  → user edits if needed, taps "+" → existing viewModel.addItem() (unchanged)
```

## Edge Cases

- Permission denied → rationale shown, "Enter manually" dismisses to list, scan icon still visible to retry later (permission can be granted next tap)
- Permission permanently denied ("don't ask again") → rationale explains Settings path, same manual fallback
- No text detected (blur, glare, bad angle, camera-less device never gets here) → "No text found" + Retry/"Enter manually", no crash, no empty-string prefill
- Camera bind failure (emulator without camera, hardware busy) → caught, same manual fallback, no crash
- Dense text tag (ingredients list, nutrition facts accidentally scanned) → block list capped (~20) and scrollable, doesn't blow up layout
- User backgrounds app mid-capture / rotates device → CameraX lifecycle tied to composable lifecycle, no leaked ImageProxy (single-shot capture avoids the continuous-analysis leak pitfall entirely)
- Feature flag off → leading icon stays the current static shopping-cart icon, zero behavior change to existing AddItemBar
- Offline — works fully, bundled model requires no network call at any point
