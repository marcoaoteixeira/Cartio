# CLAUDE.md

## AI Behavior Rules

**Always:**
- Provide clear, concise, technically accurate responses
- Follow required output schemas and formats
- Ask for clarification only when essential
- Provide actionable and specific insights when asked

**Never:**
- Invent system behavior or hallucinate design details not provided
- Produce speculative or fictional logs
- Output code invalid in Kotlin/Android contexts (unless explicitly asked)
- Provide harmful instructions or security-sensitive details

## Build & Development Commands

```bash
# Build
./gradlew assembleDebug
./gradlew assembleRelease

# Run unit tests
./gradlew test

# Run a single unit test class
./gradlew test --tests "com.minicore.cartio.ClassName"

# Run instrumented tests (requires connected device/emulator)
./gradlew connectedAndroidTest

# Lint
./gradlew lint
```

**App ID:** `com.minicore.cartio` | **minSdk:** 29 | **targetSdk:** 36

---

## Architecture

Layered MVVM + Repository, feature-first package structure:

```
app/src/main/java/com/minicore/cartio/
 ├── features/
 │    └── shopping/
 │         ├── ui/         # Compose screens + ViewModels
 │         ├── domain/     # Use cases
 │         └── data/       # Room DAOs, local data source, repository impl
 ├── core/
 │    ├── database/        # Room database, shared DAOs
 │    ├── sync/            # Google Drive sync adapter (future)
 │    └── scanner/         # Barcode/ML Kit scanner (future)
 └── di/                   # Hilt modules
```

**Data flow:** Compose UI → ViewModel (StateFlow) → Use Cases → Repository → Room DAO

Navigation Compose is used from the start. Initial routes: Shopping (home), Reports, Settings/Sync.

---

## Domain Model

Three core entities with deliberate normalization (ADR-015, revised by ADR-017):

| Entity | Key fields |
|---|---|
| `Product` | id, name, barcode?, defaultUnit?, createdAt |
| `ShoppingList` | id, name, createdAt, updatedAt |
| `ShoppingListItem` | id, shoppingListId, productId, quantity?, checked, note? |

Plus `ExpenseRecord` (id, productName, quantity, unitPrice, measureUnit, recordedAt) — denormalized log used by Reports (FEAT-011).

**Product identity is ID-based, not name-based** (ADR-016). Names can change; IDs must not. This matters for future barcode integration and report integrity across renames.

Relationships: ShoppingList → many ShoppingListItems → each references one Product.

---

## Key Architectural Constraints

- **Repository interfaces must support two data sources** from day one: `LocalDataSource` (Room) and `SyncDataSource` (future Google Drive). Do not skip this abstraction — retrofitting it later is deliberate technical debt avoidance (ADR-004).
- **Domain use cases are recommended**, not optional. With Product as a richer domain object, use cases like `AddProductToList` and the spending-report aggregation justify the layer.
- **Offline-first**: Room is always the source of truth. Sync is optional and user-controlled (manual first, no background sync).
- **State management**: ViewModel + StateFlow + unidirectional data flow. Screen state models selected list, items, checked state, totals, and sync state.

---

## Build Setup Notes

**AGP 9.2.0 bundles Kotlin compilation** — do NOT apply `org.jetbrains.kotlin.android` separately; doing so causes an "extension 'kotlin' already registered" conflict. Only `compose-compiler` (`org.jetbrains.kotlin.plugin.compose`) needs to be applied explicitly. Use `buildFeatures { compose = true }` inside `android {}` (no `composeOptions` block — that was Kotlin 1.x only).

**Verified working dependency versions (AGP 9.2.0 + Gradle 9.4.1):**
- Hilt: `2.59.2` — minimum required for AGP 9.x (2.58 and earlier fail with "Android BaseExtension not found")
- KSP: `2.3.7` — new standalone versioning (no longer tied to Kotlin version like `2.1.20-1.0.32`)
- compose-compiler plugin: `2.3.0` — must match the Kotlin version bundled in Gradle 9.4.1
- `hilt-navigation-compose`: `1.2.0`, `room`: `2.7.1`, `navigation-compose`: `2.9.0`

---

## Testing Strategy (ADR-008)

Pragmatic coverage only: ViewModel unit tests, Repository tests, light DAO tests. No extensive UI test suite.

---

## Roadmap Phases

1. **MVP** (v0.1–v0.3): Lists → Product catalog → Expenses & Reports
2. **Sync + Scanning** (v0.4–v0.5): Google Drive JSON sync → Barcode scanning (ML Kit)

Price-history tracking was scoped out of the MVP (ADR-017). Reports run off the `expense_records` log instead.

Feature flags (simple local toggles) should gate Sync and Scanner modules to keep unfinished work isolated.

Full ADR history: `docs/cartio_app_adr.md` | Roadmap: `docs/cartio_app_implementation_roadmap.md`

## Feature Development Workflow

Follow these steps **in order**. Do not skip or reorder. Do not write code until step 7.

1. **Cleanup** — Switch to default branch and pull. If there is uncommited work, ask user for directions.
2. **Clarify** — Understand the task; ask questions when any requirement is unclear.
3. **Brainstorm** — Propose clean, organized solutions. Cover security, performance, and edge cases. Explain reasoning.
4. **Spec** — Create `specs/features/FEAT-XXX-<slug>.md` capturing agreed-upon requirements and decisions.
5. **Plan** — Create `specs/plans/FEAT-XXX-<slug>.md` with an ordered implementation plan and edge cases.
6. **Explain** — Describe the approach in plain language: what changes, why, in what order, and what edge cases are covered.
7. **Branch** — Propose branch name (format: `feature/CARTIO-FEAT_NUMBER-<slug>`), confirm with user, then create from `main`.
8. **Code** — Implement using Test-Driven Development (TDD): write failing tests first, then make them pass.
9. **Review** — Run `/code-reviewer` skill. Address every **🔴 Critical Issues** before proceeding.
10. **User Check** — Ask the user to review the code; do not continue until approved.
11. **Commit** — Use conventional commit format. Message **must** be ≤ 120 chars and include project name and feature number:
    ```
    feat(CARTIO-FEAT_NUMBER): concise description of what changed
    ```
12. **Close** — Update the feature status to `Done` in `specs/README.md`.
13. **Push** — Push changes.