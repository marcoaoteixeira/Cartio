# FEAT-021 — Feature Graphic Rebuild

## Objective

Replace the FEAT-019 feature graphic, which used hand-drawn placeholder branding (generic font wordmark, improvised cart icon), with one built directly from the app's real brand assets.

## What changed

- Wordmark: now the exact vector logotype from `branding/splash_screen.svg` (the hand-drawn "Cartio" script glyph with its highlight/shading subpaths), not a generic italic serif font.
- Icon: now the actual hi-res Play Store icon (`docs/play-store/icon-512.png`, same file used for the icon listing asset), not an improvised SVG cart glyph.
- Sparkle accents: small rotated tick marks flanking the icon, echoing the sparkle decoration in the real splash screen SVG (same brand yellow, `#fdc604`).
- Wave footer: redrawn for the 1024×500 landscape aspect (the original SVG's wave was authored for a 135×300 portrait canvas and can't be reused verbatim without distortion), using the exact two brand hex colors from the SVG (`#fdc604`, `#fddb6f`) rather than approximated ones.

## Decisions

| Decision | Choice |
|---|---|
| Source of truth | `branding/splash_screen.svg` for wordmark path data and exact brand colors; `docs/play-store/icon-512.png` for the icon artwork |
| Wave | Redrawn, not reused verbatim — portrait-authored wave geometry doesn't translate to a landscape banner without distortion; colors matched exactly instead |
| Build method | Same as FEAT-019: standalone HTML at exact 1024×500, rendered via headless Chrome (`--headless=new --force-device-scale-factor=1`), HTML source discarded after render |
