package com.nameless.cartio.features.shopping.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nameless.cartio.features.shopping.data.ShoppingList
import com.nameless.cartio.features.shopping.data.ShoppingListRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class ShoppingListViewModel @Inject constructor(
    private val repository: ShoppingListRepository
) : ViewModel() {

    val shoppingLists: StateFlow<List<ShoppingList>> = repository
        .getShoppingLists()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000L), emptyList())
}
