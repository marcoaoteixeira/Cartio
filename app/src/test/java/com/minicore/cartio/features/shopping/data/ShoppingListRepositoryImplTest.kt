package com.minicore.cartio.features.shopping.data

import com.minicore.cartio.core.database.dao.ShoppingListWithCount
import com.minicore.cartio.core.database.entity.ShoppingListEntity
import com.minicore.cartio.core.time.Clock
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ShoppingListRepositoryImplTest {

    private val fixedTime = 9000L
    private val clock = Clock { fixedTime }

    private fun repository(dataSource: ShoppingListLocalDataSource) =
        ShoppingListRepositoryImpl(dataSource, clock)

    @Test
    fun `getShoppingLists maps entities from data source`() = runTest {
        val withCount = ShoppingListWithCount(1L, "Groceries", 1000L, 2000L, 3, 1)
        val repo = repository(FakeShoppingListLocalDataSource(lists = listOf(withCount)))

        val result = repo.getShoppingLists().first()

        assertEquals(1, result.size)
        assertEquals("Groceries", result[0].name)
        assertEquals(3, result[0].itemCount)
        assertEquals(1, result[0].checkedCount)
    }

    @Test
    fun `getShoppingLists returns empty list when source is empty`() = runTest {
        val repo = repository(FakeShoppingListLocalDataSource())

        val result = repo.getShoppingLists().first()

        assertEquals(emptyList<Any>(), result)
    }

    @Test
    fun `createShoppingList inserts entity with clock timestamp and returns id`() = runTest {
        val dataSource = FakeShoppingListLocalDataSource()
        val repo = repository(dataSource)

        val id = repo.createShoppingList("Weekly Shop")

        assertEquals(1L, id)
        val inserted = dataSource.inserted.first()
        assertEquals("Weekly Shop", inserted.name)
        assertEquals(fixedTime, inserted.createdAt)
        assertEquals(fixedTime, inserted.updatedAt)
    }

    @Test
    fun `getShoppingListById returns null when not found`() = runTest {
        val repo = repository(FakeShoppingListLocalDataSource())

        val result = repo.getShoppingListById(99L).first()

        assertNull(result)
    }

    @Test
    fun `renameShoppingList delegates to data source with clock timestamp`() = runTest {
        val dataSource = FakeShoppingListLocalDataSource()
        val repo = repository(dataSource)

        repo.renameShoppingList(1L, "Renamed")

        assertEquals(Triple(1L, "Renamed", fixedTime), dataSource.lastRename)
    }

    @Test
    fun `deleteShoppingList delegates id to data source`() = runTest {
        val dataSource = FakeShoppingListLocalDataSource()
        val repo = repository(dataSource)

        repo.deleteShoppingList(5L)

        assertEquals(5L, dataSource.lastDeleted)
    }
}

private class FakeShoppingListLocalDataSource(
    private val lists: List<ShoppingListWithCount> = emptyList()
) : ShoppingListLocalDataSource {
    val inserted = mutableListOf<ShoppingListEntity>()
    var lastRename: Triple<Long, String, Long>? = null
    var lastDeleted: Long? = null
    private var nextId = 1L

    override fun getShoppingListsWithCount(): Flow<List<ShoppingListWithCount>> = flowOf(lists)
    override fun getShoppingListsWithCountPaged(limit: Int, offset: Int): Flow<List<ShoppingListWithCount>> = flowOf(lists)
    override fun getShoppingListByIdFlow(id: Long): Flow<ShoppingListEntity?> =
        flowOf(lists.find { it.id == id }?.let { ShoppingListEntity(it.id, it.name, it.createdAt, it.updatedAt) })
    override suspend fun insert(entity: ShoppingListEntity): Long {
        inserted.add(entity)
        return nextId++
    }
    override suspend fun updateName(id: Long, name: String, updatedAt: Long) { lastRename = Triple(id, name, updatedAt) }
    override suspend fun updateTimestamp(id: Long, updatedAt: Long) {}
    override suspend fun deleteById(id: Long) { lastDeleted = id }
}
