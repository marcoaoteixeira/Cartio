# Plan: FEAT-017 — Privacy Policy

## Implementation Order

1. Write `docs/privacy-policy.html` — standalone static page, no external assets, matching the disclosures in the spec.
2. Add a minimal `docs/index.html` redirect/landing stub only if the repo has no existing Pages entrypoint (check first — avoid clobbering).
3. Instruct user to enable GitHub Pages in repo settings (Settings → Pages → Source: `Deploy from a branch`, Branch: `master`, Folder: `/docs`) — this is a GitHub repo *setting*, not a file, so it can't be done by a commit; flag it clearly rather than silently assuming it's on.
4. Add the live URL to `app/src/main/res/values/strings.xml` or `SettingsScreen` only if there's an existing "Privacy Policy" link slot — check first. If none exists, note it as a follow-up (out of scope here: adding a settings link is a UI change, this feature is just publishing the document).
5. `specs/README.md`: add FEAT-017 row.

## Files Modified/Created

| File | Change |
|---|---|
| `docs/privacy-policy.html` | New — the policy itself |
| `specs/features/FEAT-017-privacy-policy.md` | This spec |
| `specs/plans/FEAT-017-privacy-policy.md` | This plan |
| `specs/README.md` | Add FEAT-017 row |

## Verification

- Open the HTML file locally in a browser — renders cleanly, readable, no broken markup.
- After user enables GitHub Pages and merges, confirm `https://marcoaoteixeira.github.io/Cartio/privacy-policy.html` resolves (can't verify before merge — Pages only serves from `master`).

## Out of scope (flagged, not done here)

- Adding a "Privacy Policy" link inside the app's Settings screen — a UI change beyond "publish the document." Separate follow-up if wanted.
- Enabling GitHub Pages itself — a repo setting only the user (as repo owner) can toggle.
