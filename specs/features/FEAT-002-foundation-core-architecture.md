# FEAT-002 — Foundation & Core Architecture

**Status:** In Progress  
**Branch:** `feature/CARTIO-002-foundation-core-architecture`

---

## Goal

Establish the full architectural foundation and deliver the Dashboard (Shopping Lists) screen connected to a real Room database. Milestone: "App boots with persistent empty shopping list."

## Requirements

- Hilt DI configured and wired throughout the app.
- Room database with all 4 domain entities: `Product`, `ShoppingList`, `ShoppingListItem`, `PriceHistory`.
- Repository pattern with `LocalDataSource` + `Repository` interfaces (ADR-004 — two data sources from day one).
- Navigation Compose with 3 routes: Shopping (home), Reports, Settings.
- Dashboard Screen shows shopping lists from Room; displays empty state when database is empty.
- No add/edit functionality — that is Epic 2.
- `CartioTheme` applied app-wide using the prototype color palette.
- Bottom navigation bar visible on all 3 screens.

## Decisions

- **Navigation structure**: 3 tabs (Shopping, Reports, Settings) — follows the roadmap and ADR-013. The prototype's 4-tab layout (Home/Lists/Favorites/Profile) is NOT followed.
- **Dashboard data**: Connected to Room — not static/hardcoded.
- **Hilt**: Introduced in this feature (ADR-001).
- **Annotation processing**: KSP (not kapt) — compatible with AGP 9.2.0.
- **Bottom nav placement**: Lifted to `MainActivity` `Scaffold` so all 3 screens share the nav bar.
- **Reports/Settings**: Placeholder screens ("Coming Soon") — real implementation is future epics.
- **Icons**: `Icons.Rounded.ShoppingCart` used as placeholder for Reports tab. `BarChart` (material-icons-extended) deferred to avoid adding a 17MB dependency for a single icon.
- **Domain model**: Separate plain data classes from Room entities. Mapper extension functions bridge them.
- **FK strategy**: `ShoppingListItem → ShoppingList`: CASCADE. `ShoppingListItem → Product`: RESTRICT. `PriceHistory → Product`: CASCADE. All FK columns indexed.

## Out of Scope

- Creating, editing, or deleting shopping lists (Epic 2).
- Product catalog (Epic 3).
- Price tracking (Epic 4).
- Google Drive sync (Epic 6).
- Barcode scanning (Epic 7).
- Functional search bar (visual only for now).
- Real Reports or Settings screens.
