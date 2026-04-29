package com.minicore.cartio.features.expenses.ui

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.minicore.cartio.core.database.entity.MeasureUnit
import com.minicore.cartio.core.time.Clock
import com.minicore.cartio.features.expenses.domain.ExpenseRecord
import com.minicore.cartio.features.expenses.domain.RecordExpensesUseCase
import com.minicore.cartio.features.shopping.data.ShoppingListItemRepository
import com.minicore.cartio.features.shopping.data.ShoppingListRepository
import com.minicore.cartio.navigation.CartioDestinations
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
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
    val isSaving: Boolean = false
)

@HiltViewModel
class RegisterExpensesViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val itemRepository: ShoppingListItemRepository,
    private val listRepository: ShoppingListRepository,
    private val recordExpenses: RecordExpensesUseCase,
    private val clock: Clock
) : ViewModel() {

    private val listId: Long = checkNotNull(savedStateHandle[CartioDestinations.RegisterExpenses.ARG_LIST_ID])

    private val _uiState = MutableStateFlow(RegisterExpensesUiState())
    val uiState: StateFlow<RegisterExpensesUiState> = _uiState.asStateFlow()

    private val _events = Channel<RegisterExpensesEvent>(Channel.BUFFERED)
    val events: Flow<RegisterExpensesEvent> = _events.receiveAsFlow()

    init {
        viewModelScope.launch {
            runCatching {
                // Combine list + items into a single first() so the screen
                // doesn't flicker when one stream lands before the other.
                val (list, items) = combine(
                    listRepository.getShoppingListById(listId),
                    itemRepository.getItemsForList(listId)
                ) { l, i -> l to i }.first()

                val checkedItems = items.filter { it.checked }
                    .map { item ->
                        ExpenseRowState(
                            itemId = item.id,
                            productName = item.productName,
                            quantity = item.quantity
                        )
                    }
                _uiState.value = RegisterExpensesUiState(
                    listName = list?.name ?: "",
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
        val now = clock.now()
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
            _uiState.value = _uiState.value.copy(isSaving = false)
            _events.send(RegisterExpensesEvent.SavedAndUp)
        }
    }

    private fun computeTotal(rows: List<ExpenseRowState>): Double =
        rows.sumOf { (it.unitPrice.toDoubleOrNull() ?: 0.0) * it.quantity }

    private fun positivePrice(raw: String): Boolean = (raw.toDoubleOrNull() ?: 0.0) > 0.0
}
