# FEAT-026 — Scan Market Tag to Add Item

## Objective

Let a user point the phone camera at a market shelf tag, OCR the text on-device, and use the recognized product name to add a new item to the active shopping list — without typing.

## User Stories

1. User is in a Shopping List Detail screen, sees an item they need that isn't on the list yet.
2. User taps the camera icon in the add-item bar — it replaces the leading shopping-cart icon (not an extra icon beside the "+" button, to avoid crowding).
3. A scan screen opens with a camera preview and a capture button.
4. User aims at the shelf tag and taps capture — a single photo is taken.
5. The photo is OCR'd on-device (ML Kit Text Recognition). Camera permission must already be granted, or the user is prompted first.
6. Recognized text blocks are shown as a tappable list (e.g. brand, product name, price, unit price may each be a separate block).
7. User taps the block that is the product name — it's written into the (now reopened) AddItemBar text field, editable before confirming.
8. User taps "+" to add, same as manual entry today. No new domain logic — reuses `ShoppingListDetailViewModel.addItem()`.
9. If OCR finds no usable text (blur, glare, bad angle), scan screen shows "No text found" with Retry and "Enter manually" (dismisses back to the list).

## Requirements

- Single-shot capture only — no continuous/live frame analysis.
- ML Kit Text Recognition, **bundled** variant (`com.google.mlkit:text-recognition`) — works fully offline, no network call, consistent with offline-first architecture.
- Captured image is held in memory only for the OCR call, then discarded immediately. Never written to disk, never uploaded anywhere.
- Runtime `CAMERA` permission requested on first use of the scan entry point; on denial, show rationale and let user dismiss back to manual entry — never block the rest of the app.
- Scan entry point (camera icon) and the whole feature gated behind a local feature flag (per CLAUDE.md: Scanner module must be flag-gated), default **off** until feature is verified stable.
- Recognized text blocks list is scrollable and capped at a reasonable count (e.g. first 20 blocks) to avoid UI blowup on a tag with dense text (ingredients list, etc).
- Auto-add is an internal A/B flag (`AppConfig.SCAN_AUTO_ADD_ENABLED`, default **off**): off means a recognized block only pre-fills the editable AddItemBar for manual confirm (original behavior); on means the selected block is added to the list immediately, skipping the pre-fill/confirm step.
- Camera unavailable/bind failure (emulator without camera, hardware busy) is caught and surfaces the same "Enter manually" fallback, not a crash.

## Out of scope

- Barcode scanning (separate, already-planned roadmap item using ML Kit barcode module — different ML Kit API, own future FEAT).
- Price-history / price extraction from the tag — FEAT-026 only extracts the product *name*; price parsing is not attempted.
- Any cloud OCR fallback — on-device only.

## Architecture

- `core/scanner/` (new): `RecognizeTextFromImageUseCase` wraps `TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)`. Thin but keeps the OCR call testable and consistent with the project's use-case layer.
- `features/shopping/ui/ScanTagScreen.kt` (new): CameraX `ImageCapture` preview + capture button + result/candidate-block list + error/empty states. Own `ScanTagViewModel` (states: Idle, Capturing, Processing, Result(blocks), Error, PermissionDenied).
- `ShoppingListDetailScreen.kt`: `AddItemBar`'s leading `Icons.Rounded.ShoppingCart` is replaced with `Icons.Rounded.CameraAlt` (tappable, navigates to `ScanTagScreen`) when the feature flag is on; falls back to the current cart icon (non-interactive) when the flag is off. On result, returns selected text and pre-fills `itemInputText`.
- Feature flag checked at the call site that shows the camera icon (simple local toggle per CLAUDE.md, no new DI complexity needed).

## Decisions

| Decision | Choice |
|---|---|
| Capture mode | Single-shot photo, not live/continuous OCR |
| ML Kit variant | Bundled (`com.google.mlkit:text-recognition`), offline-first, accepts APK size increase |
| Auto-add on recognition | A/B flag, default off: off routes through editable AddItemBar + manual confirm, on adds immediately |
| Image retention | None — in-memory only, discarded right after OCR completes |
| Permission denial handling | Show rationale, fall back to manual entry, never block the app |
| Feature flag | Yes, default off, gates the camera entry point in `AddItemBar` |
| Barcode scanning | Explicitly out of scope — separate roadmap item |

## Data Safety Form follow-up

FEAT-026 adds the `CAMERA` permission. `docs/play-store/data-safety.md` (FEAT-020) must be updated: camera is used on-device only for OCR, no image or extracted text is collected, transmitted, or stored — should map to "not collected" with that reasoning spelled out, same pattern FEAT-020 already uses for billing data.
