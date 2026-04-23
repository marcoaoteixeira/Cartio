# Architecture Decision Record (ADR) — Cartio Android App

Status: Published
Date: 2026-04-23

## Scope
Personal-use shopping list Android application intended both as:
- A learning project
- A production-worthy utility for daily market trips

This favors pragmatic engineering over enterprise overengineering.

---

# ADR-001: Primary Technology Stack

## Decision
Use:
- Kotlin
- Jetpack Compose
- MVVM
- Room
- Coroutines + Flow
- Hilt

## Status
Accepted

## Rationale
Chosen because it is the common modern Android-native stack, scales well, and supports:
- Fast MVP development
- Offline-first architecture
- Future sync support
- Barcode/scanning integration later

## Consequences
Positive:
- Mature Android ecosystem
- Maintainable and testable architecture
- Native performance

Tradeoffs:
- Android-only
- Slight upfront complexity versus ultra-minimal app structures

---

# ADR-002: Architectural Style

## Decision
Use layered MVVM + Repository pattern.

Layers:

Presentation
- Compose screens
- ViewModels
- StateFlow

Domain (lightweight)
- Use cases only for business logic worth isolating

Data
- Local data source (Room)
- Future sync source (Google Drive)
- Repository as abstraction over both

## Status
Accepted

## Rationale
Because sync is planned, repository abstraction should exist from day one.
Without that, retrofitting sync later creates technical debt.

---

# ADR-003: Data Model

## Decision
Support multiple shopping lists.

Entities:

ShoppingList
- id
- name
- createdAt
- updatedAt
- syncMetadata (future)

ShoppingItem
- id
- shoppingListId
- name
- quantity (optional)
- unit (optional, informational)
- estimatedPrice (optional)
- checked
- barcodeData (optional future)
- createdAt

## Notes
Units are intentionally optional.
Examples:
- 2 kg apples
- 1 milk
- bread (no quantity)

## Consequences
Supports flexibility without forcing data entry burden.

---

# ADR-004: Offline-First With Optional Sync

## Decision
Local database is source of truth.

Sync is optional and user-controlled.
Sync target (future): Google Drive.

## Proposed Sync Model
Likely simplest approach:
- Persist lists locally in Room
- Export/sync serialized list data to user Google Drive
- User controls sync manually or optionally auto-sync

## Why Not Full Backend
For a personal shopping app:
- Lower operational cost
- No server maintenance
- Privacy-friendly
- Fits project goals

## Architectural Constraint
Repository interfaces should be written now to support:
- LocalDataSource
- SyncDataSource

This is a deliberate anti-technical-debt decision.

---

# ADR-005: Single-Screen UX

## Decision
Single-screen application.
No full navigation stack for MVP.
Avoid Jetpack Navigation initially.

Primary screen contains:
- Active shopping list selector
- Item list
- Add/edit interactions via modal/dialog/bottom sheet
- Check-off interactions
- Price display
- Scan action entry point

## Rationale
Navigation would be overengineering here.
Single-screen Compose state is sufficient.

Status: Accepted

---

# ADR-006: State Management

## Decision
Use:
- ViewModel
- StateFlow
- Unidirectional data flow

Screen state should model:
- Selected list
- Items
- Checked/unchecked state
- Totals (optional running total from prices)
- Sync state

---

# ADR-007: Feature Scope for v1

## Included in v1
Core:
- Multiple lists
- Optional quantities + units
- Item prices
- Running estimated cart total

Stretch (v1 or v1.1)
- Barcode or label scanning

## Scan Feature Interpretation
Initial practical version:
Barcode scanning using camera.
Possible implementation later:
- ML Kit barcode scanning
- Store tag OCR (more ambitious later)

Decision:
Start with barcode scanning, keep OCR label scanning as future enhancement.

Rationale:
Barcode scanning is common, reliable, and significantly lower complexity.

---

# ADR-008: Testing Strategy

## Decision
Pragmatic coverage only.
Use:
- ViewModel unit tests
- Repository tests
- Light DAO tests

No extensive UI test suite required.

Reason:
Learning + practical project, not portfolio signaling exercise.

---

# ADR-009: Package Structure

## Decision
Feature-first package structure.

```text
app/
 ├── features/
 │    └── shopping/
 │         ├── ui/
 │         ├── domain/
 │         ├── data/
 │
 ├── core/
 │    ├── database/
 │    ├── sync/
 │    └── scanner/
 │
 └── di/
```

## Why
Feature grouping scales better than technical-layer grouping.
Especially if scan and sync features grow.

---

# ADR-010: Future-Proof Decisions (Intentional Hooks)

Reserved extension points:
- Google Drive sync adapter
- Barcode scanner module
- OCR scanner module
- Price history later
- Potential pantry/inventory extension later

These are explicitly planned seams, not speculative architecture.

---

# Notable Product Philosophy Decisions

We intentionally optimize for:
- Fast grocery usage
- Minimal friction entering items
- Offline reliability over cloud dependence
- Optional enrichment (prices, units, scan)

We intentionally do NOT optimize for:
- Multi-user collaboration initially
- Real-time sync
- Enterprise-scale clean architecture purity

---

# ADR-011: Pricing History Strategy

## Decision
Use historical last-paid pricing (Option C).

Each product can retain price history entries.

Possible model:

PriceHistory
- id
- itemName (or normalized product reference later)
- price
- store (optional future)
- recordedAt

## Behavior
When a user enters a new price:
- App remembers latest paid price
- Can show last-known price when re-adding item
- Enables trend analysis later

## Why
Provides useful intelligence with low complexity.
Also unlocks reporting features.

Status: Accepted

---

# ADR-012: Sync Strategy

## Decision
Use user-owned JSON file synchronization through Google Drive.

## Sync Model
- Room remains system of record
- Lists serialized to JSON
- Sync adapter pushes/pulls Drive file
- Conflict resolution initially simple:
    Last-write-wins + timestamp metadata

## Why
Best balance of:
- Simplicity
- Portability
- User data ownership
- No backend costs

Status: Accepted

---

# ADR-013: Navigation Evolution Strategy

## Decision
Start with single-screen-first UX, but introduce lightweight navigation architecture now.

Use Navigation Compose despite MVP being mostly single-screen.

Initial routes:
- Cartio (home)
- Reports
- Settings/Sync (future)

## Rationale
This is a notable correction to the earlier ADR.
Originally avoiding navigation was reasonable for MVP.
However your reporting feature changes the calculus.

The presence of:
- Product price history reports
- Sync settings
- Potential scan workflows

means navigation is no longer speculative.
It is foreseeable product scope.

Therefore introducing navigation early reduces future refactoring.

## Architectural Principle
Do not overengineer for hypothetical futures.
Do prepare for highly probable futures.

Reports now qualify as probable.

Status: Revised Accepted

---

# ADR-014: Reporting Feature (Future Planned)

## Decision
Include lightweight reporting feature in roadmap.

Examples:
- Price trend by product
- Estimated monthly spend trends
- Cheapest observed price over time
- Price variation by grocery/store (future)

This likely uses aggregated queries over PriceHistory.

Potential later extension:
Simple local charts.

This becomes a product differentiator, not just utility plumbing.

---

# Final Architectural Shape

Stack:
- Kotlin
- Compose
- Room
- MVVM
- Repository
- Hilt
- Navigation Compose

Feature Modules:
- Shopping
- Sync
- Scanner
- Reports

Data Characteristics:
- Offline-first
- Local source of truth
- Drive JSON sync
- Historical pricing

Product Identity:
Not just a shopping list anymore.
Lightweight personal grocery intelligence app.

---

# ADR-015: Product-Centric Domain Model

## Decision
Adopt normalized Product entity model (Option B).

Status: Accepted

## Core Domain Model

Product
- id
- name
- barcode (optional)
- defaultUnit (optional)
- createdAt

ShoppingList
- id
- name
- createdAt

ShoppingListItem
- id
- shoppingListId
- productId
- quantity (optional)
- checked
- note (optional)

PriceHistory
- id
- productId
- price
- store (optional)
- recordedAt

## Relationship Model

- One ShoppingList has many ShoppingListItems
- One ShoppingListItem references one Product
- One Product has many PriceHistory entries

This separates:
- Product identity
- List membership
- Price observations

That separation is intentional.

---

## Why This Was Chosen
Supports future features with much less technical debt:

Immediately enables:
- Historical price reports
- Barcode association
- Last-paid suggestions

Future enables:
- Store-specific pricing
- Product aliases/brands
- Pantry/inventory mode
- Product favorites
- Smart repeat suggestions
- Import from barcode catalogs later

This is a classic decision where normalization pays for itself.

---

## Consequences
Positive
- Better long-term extensibility
- Cleaner domain model
- Easier analytics/reporting queries

Tradeoffs
- Slightly more joins
- Slightly more modeling complexity for MVP

Accepted because future benefit outweighs added complexity.

---

# ADR-016: Product Identity Rules (Important)

## Decision
Product identity is not based solely on display name.
Use product IDs as canonical identity.

Names can change.
IDs should not.

This prevents:
- "Milk" and "Whole Milk" history collisions
- Broken reporting due to renamed items

This is especially important for barcode integration later.

---

# Revised Architectural Direction

Architecture has now moved from simple CRUD app into a small domain-driven product model.

This is still not full DDD.
But it uses a DDD-lite mindset where it adds clear value.

Likely aggregate roots:
- ShoppingList
- Product

That is a reasonable boundary.

---

# Implementation Note I Would Recommend
Because Product now exists, I would probably introduce lightweight use cases sooner than originally planned:

Examples:
- AddProductToList
- RecordProductPrice
- GetPriceTrend
- SyncListsToDrive

Originally I kept domain layer optional.
With Product as a richer domain object, it becomes more justified.

I would now elevate the domain layer from optional to recommended.

That is a meaningful ADR evolution.

---

# ADR Set Status
Architecture is now coherent enough to begin implementation.
Major foundational decisions are covered.

Remaining decisions can happen during discovery:
- OCR vs barcode strategy details
- Conflict resolution sophistication for sync
- Reporting visualization approach
- Store model introduction (if/when needed)

At this point I would consider the ADR baseline ready for project inception.

