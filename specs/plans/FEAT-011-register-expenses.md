# Plan: FEAT-011 — Register Expenses + Reports

## Implementation Order

1. `ExpenseRecordEntity` + `ExpenseRecordDao` (new Room entity, table `expense_records`)
2. DB migration 2 → 3 + update `CartioDatabase`
3. `LocalExpenseDataSource`, `ExpenseRepository` interface, `ExpenseRepositoryImpl`
4. `ExpenseRecord` domain model + `RecordExpensesUseCase` fun interface
5. `DatabaseModule` — add DAO provider, bind repository + use case, add migration
6. Navigation — `CartioDestinations.RegisterExpenses`, wire `CartioNavGraph`
7. `ShoppingListDetailViewModel` — expose `listId` in `uiState`
8. `ShoppingListDetailScreen` — add `onNavigateToRegisterExpenses` param, wire stub
9. `RegisterExpensesViewModel` + UI state models
10. `RegisterExpensesScreen` + `RegisterExpensesComponents`
11. Reports domain — `SpendingReport`, `GetSpendingReportUseCase`
12. `ReportsViewModel` (init purge + report Flow)
13. `ReportsScreen` — replace placeholder
14. String resources
15. Tests — `RegisterExpensesViewModelTest`, `GetSpendingReportUseCaseTest`, `ReportsViewModelTest`

## Key Data Flow

```
ShoppingListDetail ⋮ menu
  → navigate(RegisterExpenses(listId))
  → RegisterExpensesScreen
       ↓ loads checkedItems via ShoppingListItemRepository
       ↓ user fills prices
  → onRecord() → RecordExpensesUseCase → ExpenseRepository → ExpenseRecordDao.insertAll
  → toast + navigateUp

Reports screen open
  → ReportsViewModel.init → expenseRepository.purgeOlderThan(now - 30 days)
  → GetSpendingReportUseCase(since = now - 30 days)
       ↓ Flow<List<ExpenseRecord>> → grouped by productName → SpendingReport
  → ReportsScreen renders summary card + top 10 list
```

## Entity Schema

```sql
CREATE TABLE expense_records (
    id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
    productName TEXT NOT NULL,
    quantity INTEGER NOT NULL,
    unitPrice REAL NOT NULL,
    measureUnit TEXT NOT NULL,
    recordedAt INTEGER NOT NULL
)
```

## Edge Cases

- No checked items → empty state in Register Expenses, "Record" disabled
- All prices left blank → "Record" saves nothing (filtered out)
- Product/list deleted after recording → expense data unaffected (no FK)
- Report with no data → empty state ("No expenses recorded yet")
- Purge on Reports open → fast single DELETE, no race with read Flow
