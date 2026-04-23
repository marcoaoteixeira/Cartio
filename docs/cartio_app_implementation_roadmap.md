# Implementation Roadmap — Cartio Android App

Status: Published Roadmap
Planning Style: Common product engineering approach (Epics → Milestones → MVP slices)

---

# Product Goal
Deliver a usable personal grocery intelligence app in thin vertical slices, always keeping the app runnable.

Guiding principle:
Build in feature slices, not technical layers.

Each slice should produce something installable and testable.

---

# Delivery Strategy
I’d split this into 3 horizons:

- Phase 1 — MVP Core Utility
- Phase 2 — Intelligence Features
- Phase 3 — Sync + Advanced Features

This avoids trying to build sync, scanning, and reporting too early.

---

# EPIC 1 — Foundation & Core Architecture

Goal:
Create project skeleton and domain foundations.

## Stories
- Initialize Android project
- Configure Kotlin + Compose + Room + Hilt
- Set up package structure
- Define domain entities:
  - Product
  - ShoppingList
  - ShoppingListItem
  - PriceHistory
- Create Room schema and DAOs
- Repository interfaces

## Milestone 1
"App boots with persistent empty shopping list"

Deliverable:
User can open app and create first list.

---

# EPIC 2 — Shopping List Core (MVP Slice 1)

Goal:
Core shopping workflow.

## Stories
- Create multiple lists
- Add product to list
- Edit/remove items
- Check/uncheck items
- Optional quantity + units
- Running item count

## MVP Slice
User can use the app for real grocery trips.

## Milestone 2
"Replace paper shopping list"

Release Candidate:
v0.1

---

# EPIC 3 — Product Catalog Model (MVP Slice 2)

Goal
Introduce reusable products.

## Stories
- Product search when adding items
- Reuse previously entered products
- Create product if missing
- Last-used product suggestions

## Nice stretch
Simple “frequently bought” section.

## Milestone 3
"Products become reusable domain objects"

Release Candidate:
v0.2

---

# EPIC 4 — Price Tracking (MVP Slice 3)

Goal
Introduce grocery intelligence.

## Stories
- Record item price
- Save price history entries
- Show last-paid price while adding item
- Show cart estimated total

## Optional stories
- Price changed indicator
- Cheapest observed price badge

## Milestone 4
"App tracks shopping spend"

Release Candidate:
v0.3 (Strong MVP)

This is where I’d call MVP complete.

---

# MVP Boundary (Stop Here Before More Features)

Includes:
✅ Multiple lists
✅ Reusable products
✅ Prices + history
✅ Offline persistence

Excludes:
❌ Sync
❌ Scanning
❌ Reports

This is deliberate.
Strong teams freeze MVP boundaries.

---

# EPIC 5 — Reports Module

Goal
Add insight features.

## Stories
- Product price trend report
- Price evolution history view
- Monthly spend summary
- Product detail report screen

## Suggested first report
Start simple:
Single product timeline view.

## Milestone 5
"App provides shopping insights"

Release Candidate:
v0.4

---

# EPIC 6 — Google Drive Sync

Goal
Optional user-controlled sync.

## Stories
- Google sign-in
- Export data as JSON
- Push to Drive
- Pull from Drive
- Timestamp conflict handling
- Manual sync button

## Defer auto-sync
Do not do background sync first.
Manual sync first.

That reduces complexity massively.

## Milestone 6
"Lists survive device loss"

Release Candidate:
v0.5

---

# EPIC 7 — Barcode Scanning

Goal
Accelerate product entry.

## Stories
- Camera permission flow
- Barcode scanning
- Match barcode to product
- Create product from scan

Future extension:
Store tag OCR investigation.

## Milestone 7
"Scan products into lists"

Release Candidate:
v0.6

---

# Suggested Delivery Order (Important)

Do NOT build by component order.
Build by user value order:

1. Lists
2. Products
3. Prices
4. Reports
5. Sync
6. Scanning

I would specifically put scanning after sync.
Most people reverse that.
I wouldn’t.
Sync adds more durable value.

---

# Technical Milestones (Cross-Cutting)

## Milestone A — Architecture Hardening
After MVP:
- Repository cleanup
- Error handling model
- Logging
- Backup testing

---

## Milestone B — Performance Review
When lists hit large product counts:
- Lazy loading review
- Query indexing
- Room optimization

Especially price history queries.

---

# Suggested Use Cases Backlog

Core domain use cases:

Priority 1
- CreateShoppingList
- AddProductToList
- CheckOffItem
- RecordProductPrice

Priority 2
- GetPriceTrend
- GetRecurringProducts
- SyncListsToDrive

Priority 3
- ScanBarcodeToProduct

Implement only as needed.
Do not generate all upfront.

---

# Proposed Sprint Plan (If Solo Developer)

## Sprint 1 (1–2 weeks)
Foundation + persistence

Goal:
Milestone 1

---

## Sprint 2
Core shopping flows

Goal:
Milestone 2

---

## Sprint 3
Product reuse model

Goal:
Milestone 3

---

## Sprint 4
Price history + totals

Goal:
Milestone 4 (MVP)

---

## Sprint 5+
Reports
Sync
Scanning

---

# Risks To Manage Early

## Risk 1
Overmodeling too early.

Mitigation:
Keep domain simple until price history lands.

---

## Risk 2
Sync complexity explosion.

Mitigation:
Manual sync only first.
JSON only first.

---

## Risk 3
Barcode feature creep.

Mitigation:
Treat OCR as research spike, not committed scope.

---

# Product Backlog Candidates (Later)
Not roadmap commitments.
Just parking lot.

- Pantry mode
- Smart repeat reminders
- Store comparisons
- Budget alerts
- Shared household lists

---

# Architecture Decision I’d Make For Implementation
I would likely use feature flags (even simple local toggles) for:
- Reports module
- Sync module
- Scanning module

Reason:
Keeps unfinished work isolated.
Very useful even in solo projects.

---

# If I Were Defining “Version 1.0”
I’d call this 1.0:

Must Have:
- Core shopping lists
- Product model
- Price history
- Product trend report
- Drive sync

Barcode scanning can be 1.1.

That’s a strong first public-quality release.

---

# What I Would Build First (Tomorrow)
Very first coding slice:

Vertical slice #1:
- Create list
- Add product
- Check item complete
- Persist locally

Nothing else.

That can be built in a weekend.

And that is exactly where I would start.

---

# One question before we could refine this further
Do you want this roadmap optimized more toward:

A) Solo side-project realism (leanest route)
B) Portfolio-quality engineering discipline
C) Production-grade architecture as if shipping to Play Store

I can tune the roadmap very differently depending on which of those you want.

