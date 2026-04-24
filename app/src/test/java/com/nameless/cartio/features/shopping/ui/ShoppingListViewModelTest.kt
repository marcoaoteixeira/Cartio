package com.nameless.cartio.features.shopping.ui

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.nameless.cartio.features.shopping.data.ShoppingList
import com.nameless.cartio.features.shopping.data.ShoppingListRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ShoppingListViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state is empty list`() = runTest {
        val fakeRepository = FakeShoppingListRepository(emptyList())
        val viewModel = ShoppingListViewModel(fakeRepository)

        advanceUntilIdle()

        assertEquals(emptyList<ShoppingList>(), viewModel.shoppingLists.value)
    }

    @Test
    fun `emits lists from repository`() = runTest {
        val lists = listOf(
            ShoppingList(1L, "Groceries", 1000L, 2000L),
            ShoppingList(2L, "Hardware", 1500L, 2500L)
        )
        val fakeRepository = FakeShoppingListRepository(lists)
        val viewModel = ShoppingListViewModel(fakeRepository)

        // Subscribe to trigger WhileSubscribed upstream collection
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.shoppingLists.collect {}
        }
        advanceUntilIdle()

        assertEquals(lists, viewModel.shoppingLists.value)
    }
}

private class FakeShoppingListRepository(
    private val lists: List<ShoppingList>
) : ShoppingListRepository {
    override fun getShoppingLists() = flowOf(lists)
}
