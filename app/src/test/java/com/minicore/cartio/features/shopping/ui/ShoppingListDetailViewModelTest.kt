package com.minicore.cartio.features.shopping.ui

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.SavedStateHandle
import com.minicore.cartio.features.shopping.data.ShoppingListItemRepository
import com.minicore.cartio.features.shopping.data.ShoppingListRepository
import com.minicore.cartio.features.shopping.domain.ShoppingList
import com.minicore.cartio.features.shopping.domain.ShoppingListItem
import com.minicore.cartio.features.shopping.domain.AddItemToList
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
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
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ShoppingListDetailViewModelTest {

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

    private fun createViewModel(
        list: ShoppingList = ShoppingList(1L, "Test List", 1000L, 2000L),
        items: MutableStateFlow<List<ShoppingListItem>> = MutableStateFlow(emptyList()),
        fakeItemRepo: FakeShoppingListItemRepository = FakeShoppingListItemRepository(items)
    ) = ShoppingListDetailViewModel(
        savedStateHandle = SavedStateHandle(mapOf("listId" to 1L)),
        listRepository = FakeDetailShoppingListRepository(list),
        itemRepository = fakeItemRepo,
        addItemToList = AddItemToList { _, _ -> }
    )

    @Test
    fun `initial state is loading`() {
        val viewModel = createViewModel()
        assertTrue(viewModel.uiState.value.isLoading)
    }

    @Test
    fun `loads list name after subscription`() = runTest {
        val viewModel = createViewModel()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect {}
        }
        advanceUntilIdle()
        assertEquals("Test List", viewModel.uiState.value.listName)
    }

    @Test
    fun `splits items into active and checked`() = runTest {
        val itemsFlow = MutableStateFlow(
            listOf(
                ShoppingListItem(1L, 1L, 10L, "Eggs", 2, false, null),
                ShoppingListItem(2L, 1L, 11L, "Milk", 1, true, null)
            )
        )
        val viewModel = createViewModel(items = itemsFlow)
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect {}
        }
        advanceUntilIdle()
        assertEquals(1, viewModel.uiState.value.activeItems.size)
        assertEquals(1, viewModel.uiState.value.checkedItems.size)
        assertEquals("Eggs", viewModel.uiState.value.activeItems.first().productName)
        assertEquals("Milk", viewModel.uiState.value.checkedItems.first().productName)
    }

    @Test
    fun `empty list shows no items`() = runTest {
        val viewModel = createViewModel()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect {}
        }
        advanceUntilIdle()
        assertTrue(viewModel.uiState.value.activeItems.isEmpty())
        assertTrue(viewModel.uiState.value.checkedItems.isEmpty())
    }

    @Test
    fun `checkItem delegates to repository`() = runTest {
        val fakeItemRepo = FakeShoppingListItemRepository(MutableStateFlow(emptyList()))
        val viewModel = createViewModel(fakeItemRepo = fakeItemRepo)
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect {}
        }

        viewModel.checkItem(itemId = 5L, checked = true)
        advanceUntilIdle()

        assertEquals(listOf(5L to true), fakeItemRepo.checkedItems)
    }

    @Test
    fun `updateQuantity delegates to repository`() = runTest {
        val fakeItemRepo = FakeShoppingListItemRepository(MutableStateFlow(emptyList()))
        val viewModel = createViewModel(fakeItemRepo = fakeItemRepo)
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect {}
        }

        viewModel.updateQuantity(itemId = 3L, quantity = 5)
        advanceUntilIdle()

        assertEquals(listOf(3L to 5), fakeItemRepo.updatedQuantities)
    }

    @Test
    fun `deleteItem delegates to repository`() = runTest {
        val fakeItemRepo = FakeShoppingListItemRepository(MutableStateFlow(emptyList()))
        val viewModel = createViewModel(fakeItemRepo = fakeItemRepo)
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect {}
        }

        viewModel.deleteItem(itemId = 7L)
        advanceUntilIdle()

        assertEquals(listOf(7L), fakeItemRepo.deletedIds)
    }

    @Test
    fun `renameList delegates to repository`() = runTest {
        val fakeListRepo = FakeDetailShoppingListRepository(ShoppingList(1L, "Old Name", 1000L, 2000L))
        val viewModel = ShoppingListDetailViewModel(
            savedStateHandle = SavedStateHandle(mapOf("listId" to 1L)),
            listRepository = fakeListRepo,
            itemRepository = FakeShoppingListItemRepository(MutableStateFlow(emptyList())),
            addItemToList = AddItemToList { _, _ -> }
        )
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect {}
        }

        viewModel.renameList("New Name")
        advanceUntilIdle()

        assertEquals(listOf(1L to "New Name"), fakeListRepo.renamedLists)
    }

    @Test
    fun `isLoading is false after data arrives`() = runTest {
        val viewModel = createViewModel()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect {}
        }
        advanceUntilIdle()
        assertFalse(viewModel.uiState.value.isLoading)
    }
}

private class FakeDetailShoppingListRepository(
    private val list: ShoppingList
) : ShoppingListRepository {
    val renamedLists = mutableListOf<Pair<Long, String>>()

    override fun getShoppingLists() = flowOf(listOf(list))
    override fun getShoppingListsPaged(limit: Int, offset: Int) = flowOf(listOf(list))
    override fun getShoppingListById(id: Long) = flowOf(list.takeIf { it.id == id })
    override suspend fun createShoppingList(name: String) = 0L
    override suspend fun renameShoppingList(id: Long, name: String) { renamedLists.add(id to name) }
    override suspend fun deleteShoppingList(id: Long) {}
    override suspend fun touchUpdatedAt(id: Long) {}
}

private class FakeShoppingListItemRepository(
    private val itemsFlow: MutableStateFlow<List<ShoppingListItem>>
) : ShoppingListItemRepository {
    val checkedItems = mutableListOf<Pair<Long, Boolean>>()
    val updatedQuantities = mutableListOf<Pair<Long, Int>>()
    val deletedIds = mutableListOf<Long>()

    override fun getItemsForList(listId: Long) = itemsFlow
    override suspend fun findActiveItemByProduct(listId: Long, productId: Long): Pair<Long, Int>? = null
    override suspend fun insertItem(listId: Long, productId: Long) {}
    override suspend fun addOrIncrement(listId: Long, productId: Long) {}
    override suspend fun updateQuantity(itemId: Long, quantity: Int) { updatedQuantities.add(itemId to quantity) }
    override suspend fun checkItem(itemId: Long, checked: Boolean) { checkedItems.add(itemId to checked) }
    override suspend fun deleteItem(itemId: Long) { deletedIds.add(itemId) }
}
