# FEAT-019 — Store Listing Assets

## Objective

Produce the assets Play Console requires for the store listing: hi-res icon (already exists), feature graphic, phone screenshots, and short/full description copy.

## Status of each required asset

| Asset | Requirement | Status |
|---|---|---|
| Hi-res icon | 512×512 PNG, 32-bit with alpha | **Already exists** — `app/src/main/ic_cartio-playstore.png`, verified 512×512 |
| Feature graphic | 1024×500 PNG/JPG, no alpha | New — built from the same branding as the splash screen (Autolova wordmark, yellow wave, cart icon) for visual consistency |
| Phone screenshots | Min 2, max 8, each dimension 320–3840px, 16:9 or 9:16 | New — captured from a real debug build running on a Pixel 5 emulator (API per AVD default), not mockups |
| Short description | ≤ 80 characters | New — drafted below |
| Full description | ≤ 4000 characters | New — drafted below |

## Copy

**Short description** (character count verified ≤80):

> Shopping lists and expense tracking — simple, fast, works offline.

**Full description:**

> Cartio is a no-fuss shopping list app that also tracks what you actually spend.
>
> MAKE LISTS THAT WORK THE WAY YOU SHOP
> Create as many shopping lists as you need, add items in seconds, and check them off as you go. Rename, reorder, and sort lists the way that fits your routine.
>
> TRACK WHAT YOU SPEND
> When you check an item off, log what you paid for it — price, quantity, and unit. Cartio turns that into a running expense history so you can see where your money actually goes, list by list.
>
> SEE YOUR SPENDING, NOT JUST YOUR LIST
> The Reports screen rolls up your logged expenses so your shopping habits are visible, not just your to-do list.
>
> WORKS OFFLINE, STAYS ON YOUR DEVICE
> Cartio needs no account and no internet connection to work. Your lists and expense history live in a local database on your device — nothing is uploaded anywhere, because there's no server to upload it to.
>
> FREE, WITH AN OPTIONAL THANK-YOU
> Cartio is free to use, supported by occasional ads. If you'd like to remove them, a one-time "Buy Me A Coffee" purchase is available in Settings — entirely optional.
>
> No account. No clutter. Just lists that work, and a clear picture of what you're spending.

## Screenshot plan

Captured on the `Pixel_5` AVD, debug build (test ads, so no live ad impressions in the screenshots). Order matches a natural first-look flow:

1. Shopping list home (list of lists)
2. A list opened — items with checkboxes, add-item bar
3. Register Expenses dialog/screen (price/quantity/unit entry) — the feature that differentiates Cartio from a plain checklist app
4. Reports screen (spending rollup)
5. Settings screen (shows offline-first / no-account positioning, ad-free purchase)

5 screenshots satisfies the "min 2, max 8" requirement with room to spare, while showing every major screen once.

## Decisions

| Decision | Choice |
|---|---|
| Where assets live in the repo | `docs/play-store/` (new directory) — keeps them versioned and discoverable, mirrors how `docs/keystore/` is organized (data, not code) |
| Screenshot source | Real running debug build on an emulator, not generated/mocked images — accuracy over speed |
| Feature graphic tool | Built as an HTML page at the exact 1024×500 canvas size and rendered to PNG, reusing the splash screen's established visual language rather than inventing new branding |
