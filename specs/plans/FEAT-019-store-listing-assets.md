# Plan: FEAT-019 — Store Listing Assets

## Implementation Order

1. Create `docs/play-store/` directory.
2. Copy the existing hi-res icon there for convenience (`docs/play-store/icon-512.png`) — source stays at `app/src/main/ic_cartio-playstore.png`, this is just a copy for one-stop Play Console upload.
3. Build the feature graphic: a standalone HTML page sized exactly 1024×500, styled to match `CartioSplashScreen` (Autolova wordmark, yellow wave, cart icon on white), rendered to PNG via headless Chrome at the exact viewport size. Save as `docs/play-store/feature-graphic.png`.
4. Boot the `Pixel_5` AVD, install and launch a debug build, navigate to and screenshot each of the 5 screens listed in the spec. Save as `docs/play-store/screenshot-01-lists.png` through `screenshot-05-settings.png`.
5. Write `docs/play-store/listing-copy.md` with the short/full description text from the spec, so it's copy-pasteable into Play Console without re-deriving it.
6. `specs/README.md`: add FEAT-019 row.

No TDD step — this feature produces static assets and copy, not application logic.

## Files Created

| File | Purpose |
|---|---|
| `docs/play-store/icon-512.png` | Hi-res icon, copied from existing source |
| `docs/play-store/feature-graphic.png` | 1024×500 feature graphic |
| `docs/play-store/screenshot-01-lists.png` … `-05-settings.png` | Phone screenshots |
| `docs/play-store/listing-copy.md` | Short + full description text |
| `specs/features/FEAT-019-store-listing-assets.md` | This spec |
| `specs/plans/FEAT-019-store-listing-assets.md` | This plan |
| `specs/README.md` | Add FEAT-019 row |

## Verification

- Open each PNG and confirm exact pixel dimensions match Play Console requirements (icon 512×512, feature graphic 1024×500, screenshots within 320–3840px bounds).
- Screenshots show real app UI, not placeholder/lorem-ipsum content — verify by eye before committing.
