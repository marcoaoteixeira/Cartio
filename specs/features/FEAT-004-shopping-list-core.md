# FEAT-004 — Shopping List Core (Epic 2)

## Overview

Delivers the full shopping list workflow: create lists from the dashboard, navigate to a detail screen, add/remove/check items with quantity steppers, swipe gestures, and rename lists.

## User Stories

- As a user, I can tap the FAB on the dashboard to create a new named list.
- As a user, I can tap a list card to open its detail screen.
- As a user, I can add items to a list by typing a name in the bottom input bar.
- As a user, I can increment or decrement an item's quantity using the stepper.
- As a user, when an item's quantity reaches 1, the minus button becomes a red trash icon to confirm intent before deletion.
- As a user, I can swipe left on an item to reveal a DELETE action.
- As a user, I can swipe right on an item to reveal a CHECK action.
- As a user, I can tap the circle checkbox on an item row to toggle its checked state.
- As a user, checked items appear in a "DONE · N" section with strikethrough text.
- As a user, I can tap the pencil icon in the detail header to rename the list.
- As a user, the dashboard card subtitle shows item count and last-updated date ("3 items · Updated Apr 25").
- As a user, the detail header shows a progress bar indicating how many items are picked up.

## Out of Scope (Epic 2)

- Units (kg, L, pcs, etc.) — deferred to a later feature
- Product search / catalog browsing — deferred
- Price tracking — deferred
- Barcode scanning — deferred

## Agreed Decisions

| Decision | Choice | Reason |
|---|---|---|
| Swipe UX | Full swipe-to-reveal (SwipeToDismissBox) | Matches prototype; no extra library (M3 already included) |
| Card subtitle | "N items · Updated MMM d" | Shows freshness + count in one line |
| Units | None in this epic | Prototype doesn't show unit selector |
| Rename list | Yes, pencil icon → dialog | Small addition, completes the detail screen |
| Item quantity type | Int in UI, Float in DB | Prototype shows integers; DB keeps Float for future compatibility |
| Product identity | Find-or-create by name (case-insensitive) | No barcode yet; name is the only identifier in Epic 2 |

## Data Layer Changes

### DAO additions

**ShoppingListDao:**
- `getAllWithItemCount(): Flow<List<ShoppingListWithCount>>` — LEFT JOIN with COUNT
- `getByIdFlow(id: Long): Flow<ShoppingListEntity?>` — reactive single-list query
- `update(entity: ShoppingListEntity)` — for rename + updatedAt

**ShoppingListItemDao:**
- `getByListWithProduct(listId: Long): Flow<List<ShoppingListItemWithProduct>>` — @Transaction + @Relation
- `update(item: ShoppingListItemEntity)` — for check/qty changes
- `deleteById(id: Long)` — for delete

**ProductDao:**
- `getByName(name: String): ProductEntity?` — case-insensitive lookup (COLLATE NOCASE)

### New domain model

```
ShoppingListItem(
  id: Long,
  listId: Long,
  productId: Long,
  productName: String,
  quantity: Int,
  checked: Boolean,
  note: String?
)
```

### New use case

`AddItemToListUseCase`: find-or-create product by name → insert item → update list timestamp.

## Navigation

New route: `shopping_list/{listId}` (Long argument). Accessed by tapping a list card on the dashboard.

## UI States

### ShoppingListDetailUiState

```
listName: String
activeItems: List<ShoppingListItem>   // checked=false, ordered by id ASC
checkedItems: List<ShoppingListItem>  // checked=true
isLoading: Boolean
```

### Detail screen layout

- Orange TopAppBar: back arrow, editable title + pencil, "X of Y picked up" subtitle, search + kebab, yellow progress bar at bottom
- LazyColumn: active items → "DONE · N" divider → checked items
- Bottom pill bar: cart icon + "Add item..." text field + circular "+" button

### Item row

- Swipe left → red DELETE background → delete on full swipe
- Swipe right → green CHECK background → toggle checked on full swipe
- Circle checkbox (tap to toggle)
- Item name (strikethrough + gray when checked)
- Quantity stepper: `[−][qty][+]` — minus becomes red trash when qty=1 (active items only)

## Test Plan

- `ShoppingListDetailViewModelTest`: initial load, addItem, checkItem, updateQuantity, deleteItem, renameList
- `ShoppingListViewModelTest` additions: createShoppingList emits navigable ID
