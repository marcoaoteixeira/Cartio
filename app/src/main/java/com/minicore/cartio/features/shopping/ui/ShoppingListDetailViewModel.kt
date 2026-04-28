package com.minicore.cartio.features.shopping.ui

import android.app.Activity
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.minicore.cartio.features.monetization.domain.ShowDetailAdUseCase
import com.minicore.cartio.features.shopping.data.ShoppingListItem
import com.minicore.cartio.features.shopping.data.ShoppingListItemRepository
import com.minicore.cartio.features.shopping.data.ShoppingListRepository
import com.minicore.cartio.features.shopping.domain.AddItemToList
import com.minicore.cartio.navigation.CartioDestinations
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class SortOrder { DEFAULT, ALPHA_ASC, ALPHA_DESC }

data class ShoppingListDetailUiState(
    val listId: Long = 0L,
    val listName: String = "",
    val activeItems: List<ShoppingListItem> = emptyList(),
    val checkedItems: List<ShoppingListItem> = emptyList(),
    val sortOrder: SortOrder = SortOrder.DEFAULT,
    val isLoading: Boolean = true
)

@HiltViewModel
class ShoppingListDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val listRepository: ShoppingListRepository,
    private val itemRepository: ShoppingListItemRepository,
    private val addItemToList: AddItemToList,
    private val showDetailAd: ShowDetailAdUseCase
) : ViewModel() {

    private val listId: Long = checkNotNull(savedStateHandle[CartioDestinations.ShoppingListDetail.ARG_LIST_ID]) {
        "ShoppingListDetailViewModel requires a ${CartioDestinations.ShoppingListDetail.ARG_LIST_ID} navigation argument"
    }

    private val _sortOrder = MutableStateFlow(SortOrder.DEFAULT)

    private val _listDeleted = MutableStateFlow(false)
    val listDeleted: StateFlow<Boolean> = _listDeleted.asStateFlow()

    val uiState: StateFlow<ShoppingListDetailUiState> = combine(
        listRepository.getShoppingListById(listId),
        itemRepository.getItemsForList(listId),
        _sortOrder
    ) { list, items, sortOrder ->
        val sorted = when (sortOrder) {
            SortOrder.DEFAULT -> items
            SortOrder.ALPHA_ASC -> items.sortedBy { it.productName.lowercase() }
            SortOrder.ALPHA_DESC -> items.sortedByDescending { it.productName.lowercase() }
        }
        ShoppingListDetailUiState(
            listId = listId,
            listName = list?.name ?: "",
            activeItems = sorted.filter { !it.checked },
            checkedItems = sorted.filter { it.checked },
            sortOrder = sortOrder,
            isLoading = false
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000L), ShoppingListDetailUiState())

    fun onScreenEntered(activity: Activity) {
        viewModelScope.launch { showDetailAd(activity) }
    }

    fun toggleSort() {
        _sortOrder.value = when (_sortOrder.value) {
            SortOrder.DEFAULT -> SortOrder.ALPHA_ASC
            SortOrder.ALPHA_ASC -> SortOrder.ALPHA_DESC
            SortOrder.ALPHA_DESC -> SortOrder.DEFAULT
        }
    }

    fun addItem(name: String) {
        viewModelScope.launch { addItemToList(listId, name) }
    }

    fun checkItem(itemId: Long, checked: Boolean) {
        viewModelScope.launch { itemRepository.checkItem(itemId, checked) }
    }

    fun updateQuantity(itemId: Long, quantity: Int) {
        viewModelScope.launch { itemRepository.updateQuantity(itemId, quantity) }
    }

    fun deleteItem(itemId: Long) {
        viewModelScope.launch { itemRepository.deleteItem(itemId) }
    }

    fun renameList(name: String) {
        viewModelScope.launch { listRepository.renameShoppingList(listId, name) }
    }

    fun deleteList() {
        viewModelScope.launch {
            listRepository.deleteShoppingList(listId)
            _listDeleted.value = true
        }
    }
}
