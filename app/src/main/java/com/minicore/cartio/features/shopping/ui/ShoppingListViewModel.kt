package com.minicore.cartio.features.shopping.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.minicore.cartio.features.shopping.data.ShoppingList
import com.minicore.cartio.features.shopping.data.ShoppingListRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class DashboardSort { RECENT, ALPHA_ASC, ALPHA_DESC }

@HiltViewModel
class ShoppingListViewModel @Inject constructor(
    private val repository: ShoppingListRepository
) : ViewModel() {

    private val _dashboardSort = MutableStateFlow(DashboardSort.RECENT)
    val dashboardSort: StateFlow<DashboardSort> = _dashboardSort.asStateFlow()

    val shoppingLists: StateFlow<List<ShoppingList>> = combine(
        repository.getShoppingLists(),
        _dashboardSort
    ) { lists, sort ->
        when (sort) {
            DashboardSort.RECENT -> lists
            DashboardSort.ALPHA_ASC -> lists.sortedBy { it.name.lowercase() }
            DashboardSort.ALPHA_DESC -> lists.sortedByDescending { it.name.lowercase() }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000L), emptyList())

    private val _createdListId = MutableStateFlow<Long?>(null)
    val createdListId: StateFlow<Long?> = _createdListId.asStateFlow()

    fun createShoppingList(name: String) {
        viewModelScope.launch {
            val id = repository.createShoppingList(name)
            _createdListId.value = id
        }
    }

    fun onNavigationHandled() {
        _createdListId.value = null
    }

    fun toggleDashboardSort() {
        _dashboardSort.value = when (_dashboardSort.value) {
            DashboardSort.RECENT -> DashboardSort.ALPHA_ASC
            DashboardSort.ALPHA_ASC -> DashboardSort.ALPHA_DESC
            DashboardSort.ALPHA_DESC -> DashboardSort.RECENT
        }
    }

    fun deleteShoppingList(id: Long) {
        viewModelScope.launch { repository.deleteShoppingList(id) }
    }
}
