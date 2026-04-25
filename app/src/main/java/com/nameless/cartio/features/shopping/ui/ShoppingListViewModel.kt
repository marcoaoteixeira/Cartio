package com.nameless.cartio.features.shopping.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nameless.cartio.features.shopping.data.ShoppingList
import com.nameless.cartio.features.shopping.data.ShoppingListRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ShoppingListViewModel @Inject constructor(
    private val repository: ShoppingListRepository
) : ViewModel() {

    val shoppingLists: StateFlow<List<ShoppingList>> = repository
        .getShoppingLists()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000L), emptyList())

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

    fun deleteShoppingList(id: Long) {
        viewModelScope.launch { repository.deleteShoppingList(id) }
    }
}
