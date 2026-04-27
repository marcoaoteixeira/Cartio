package com.nameless.cartio.features.expenses.ui

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nameless.cartio.core.database.entity.MeasureUnit
import com.nameless.cartio.features.expenses.domain.ExpenseRecord
import com.nameless.cartio.features.expenses.domain.RecordExpensesUseCase
import com.nameless.cartio.features.shopping.data.ShoppingListItemRepository
import com.nameless.cartio.features.shopping.data.ShoppingListRepository
import com.nameless.cartio.navigation.CartioDestinations
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ExpenseRowState(
    val itemId: Long,
    val productName: String,
    val quantity: Int,
    val measureUnit: MeasureUnit = MeasureUnit.Piece,
    val unitPrice: String = ""
)

data class RegisterExpensesUiState(
    val listName: String = "",
    val rows: List<ExpenseRowState> = emptyList(),
    val total: Double = 0.0,
    val hasValidPrices: Boolean = false,
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val saved: Boolean = false
)

@HiltViewModel
class RegisterExpensesViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val itemRepository: ShoppingListItemRepository,
    private val listRepository: ShoppingListRepository,
    private val recordExpenses: RecordExpensesUseCase
) : ViewModel() {

    private val listId: Long = checkNotNull(savedStateHandle[CartioDestinations.RegisterExpenses.ARG_LIST_ID])

    private val _uiState = MutableStateFlow(RegisterExpensesUiState())
    val uiState: StateFlow<RegisterExpensesUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            runCatching {
                val listName = listRepository.getShoppingListById(listId).first()?.name ?: ""
                val checkedItems = itemRepository.getItemsForList(listId).first()
                    .filter { it.checked }
                    .map { item ->
                        ExpenseRowState(
                            itemId = item.id,
                            productName = item.productName,
                            quantity = item.quantity
                        )
                    }
                _uiState.value = RegisterExpensesUiState(
                    listName = listName,
                    rows = checkedItems,
                    isLoading = false
                )
            }.onFailure {
                _uiState.value = _uiState.value.copy(isLoading = false)
            }
        }
    }

    fun updatePrice(itemId: Long, price: String) {
        val updated = _uiState.value.rows.map { row ->
            if (row.itemId == itemId) row.copy(unitPrice = price) else row
        }
        _uiState.value = _uiState.value.copy(
            rows = updated,
            total = computeTotal(updated),
            hasValidPrices = updated.any { positivePrice(it.unitPrice) }
        )
    }

    fun updateMeasureUnit(itemId: Long, unit: MeasureUnit) {
        val updated = _uiState.value.rows.map { row ->
            if (row.itemId == itemId) row.copy(measureUnit = unit) else row
        }
        _uiState.value = _uiState.value.copy(rows = updated)
    }

    fun onRecord() {
        if (_uiState.value.isSaving) return
        val now = System.currentTimeMillis()
        val records = _uiState.value.rows.mapNotNull { row ->
            if (!positivePrice(row.unitPrice)) return@mapNotNull null
            ExpenseRecord(
                productName = row.productName,
                quantity = row.quantity,
                unitPrice = row.unitPrice.toDouble(),
                measureUnit = row.measureUnit,
                recordedAt = now
            )
        }
        if (records.isEmpty()) return
        _uiState.value = _uiState.value.copy(isSaving = true)
        viewModelScope.launch {
            recordExpenses(records)
            _uiState.value = _uiState.value.copy(isSaving = false, saved = true)
        }
    }

    private fun computeTotal(rows: List<ExpenseRowState>): Double =
        rows.sumOf { (it.unitPrice.toDoubleOrNull() ?: 0.0) * it.quantity }

    private fun positivePrice(raw: String): Boolean = (raw.toDoubleOrNull() ?: 0.0) > 0.0
}
