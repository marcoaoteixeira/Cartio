package com.minicore.cartio.features.expenses.ui

import androidx.lifecycle.SavedStateHandle
import com.minicore.cartio.core.database.entity.MeasureUnit
import com.minicore.cartio.core.time.Clock
import com.minicore.cartio.features.expenses.data.FakeExpenseRepository
import com.minicore.cartio.features.shopping.data.ShoppingList
import com.minicore.cartio.features.shopping.data.ShoppingListItem
import com.minicore.cartio.features.shopping.data.ShoppingListItemRepository
import com.minicore.cartio.features.shopping.data.ShoppingListRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
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
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class RegisterExpensesViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    @Before fun setUp() = Dispatchers.setMain(testDispatcher)
    @After fun tearDown() = Dispatchers.resetMain()

    private val expenseRepository = FakeExpenseRepository()

    private fun item(id: Long, name: String, qty: Int, checked: Boolean) =
        ShoppingListItem(id, 1L, id * 10, name, qty, checked, null)

    private fun createViewModel(items: List<ShoppingListItem> = emptyList()): RegisterExpensesViewModel {
        val itemsFlow = MutableStateFlow(items)
        return RegisterExpensesViewModel(
            savedStateHandle = SavedStateHandle(mapOf("listId" to 1L)),
            itemRepository = FakeItemRepo(itemsFlow),
            listRepository = FakeListRepo(),
            recordExpenses = expenseRepository,
            clock = Clock { 1_000L }
        )
    }

    @Test
    fun `only checked items appear as rows`() = runTest {
        val vm = createViewModel(
            items = listOf(
                item(1L, "Milk", 2, checked = true),
                item(2L, "Eggs", 1, checked = false),
                item(3L, "Bread", 3, checked = true)
            )
        )
        advanceUntilIdle()
        val rows = vm.uiState.value.rows
        assertEquals(2, rows.size)
        assertEquals(setOf("Milk", "Bread"), rows.map { it.productName }.toSet())
    }

    @Test
    fun `updatePrice recomputes total correctly`() = runTest {
        val vm = createViewModel(listOf(item(1L, "Milk", 3, checked = true)))
        advanceUntilIdle()

        vm.updatePrice(1L, "2.00")

        assertEquals(6.0, vm.uiState.value.total, 0.001)
    }

    @Test
    fun `onRecord skips rows with blank price and saves only priced rows`() = runTest {
        val vm = createViewModel(
            listOf(
                item(1L, "Milk", 2, checked = true),
                item(2L, "Eggs", 1, checked = true)
            )
        )
        val received = mutableListOf<RegisterExpensesEvent>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            vm.events.collect { received.add(it) }
        }
        advanceUntilIdle()
        vm.updatePrice(1L, "1.50")

        vm.onRecord()
        advanceUntilIdle()

        assertEquals(listOf(RegisterExpensesEvent.SavedAndUp), received)
    }

    @Test
    fun `onRecord skips rows with zero price and emits no event`() = runTest {
        val vm = createViewModel(listOf(item(1L, "Milk", 2, checked = true)))
        val received = mutableListOf<RegisterExpensesEvent>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            vm.events.collect { received.add(it) }
        }
        advanceUntilIdle()
        vm.updatePrice(1L, "0.00")

        vm.onRecord()
        advanceUntilIdle()

        assertTrue(received.isEmpty())
    }

    @Test
    fun `onRecord with valid price emits SavedAndUp event`() = runTest {
        val vm = createViewModel(listOf(item(1L, "Milk", 2, checked = true)))
        val received = mutableListOf<RegisterExpensesEvent>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            vm.events.collect { received.add(it) }
        }
        advanceUntilIdle()
        vm.updatePrice(1L, "1.99")

        vm.onRecord()
        advanceUntilIdle()

        assertEquals(listOf(RegisterExpensesEvent.SavedAndUp), received)
    }

    @Test
    fun `onRecord persists only items with positive price`() = runTest {
        val vm = createViewModel(
            listOf(
                item(1L, "Milk", 2, checked = true),
                item(2L, "Eggs", 3, checked = true)
            )
        )
        advanceUntilIdle()
        vm.updatePrice(1L, "1.50")

        vm.onRecord()
        advanceUntilIdle()

        val saved = expenseRepository.getRecordsSince(0L).first()
        assertEquals(1, saved.size)
        assertEquals("Milk", saved[0].productName)
    }

    @Test
    fun `updateMeasureUnit changes unit for correct row`() = runTest {
        val vm = createViewModel(listOf(item(1L, "Milk", 1, checked = true)))
        advanceUntilIdle()

        vm.updateMeasureUnit(1L, MeasureUnit.Kilogram)

        assertEquals(MeasureUnit.Kilogram, vm.uiState.value.rows.first().measureUnit)
    }
}

private class FakeItemRepo(private val flow: MutableStateFlow<List<ShoppingListItem>>) : ShoppingListItemRepository {
    override fun getItemsForList(listId: Long): Flow<List<ShoppingListItem>> = flow
    override suspend fun findActiveItemByProduct(listId: Long, productId: Long) = null
    override suspend fun insertItem(listId: Long, productId: Long) {}
    override suspend fun updateQuantity(itemId: Long, quantity: Int) {}
    override suspend fun checkItem(itemId: Long, checked: Boolean) {}
    override suspend fun deleteItem(itemId: Long) {}
}

private class FakeListRepo : ShoppingListRepository {
    override fun getShoppingLists(): Flow<List<ShoppingList>> = flowOf(emptyList())
    override fun getShoppingListById(id: Long): Flow<ShoppingList?> =
        flowOf(ShoppingList(id, "Test List", 0L, 0L))
    override suspend fun createShoppingList(name: String): Long = 0L
    override suspend fun renameShoppingList(id: Long, name: String) {}
    override suspend fun deleteShoppingList(id: Long) {}
    override suspend fun touchUpdatedAt(id: Long) {}
}
