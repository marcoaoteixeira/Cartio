package com.minicore.cartio.core.database.dao

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.minicore.cartio.core.database.CartioDatabase
import com.minicore.cartio.core.database.entity.ShoppingListEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ShoppingListDaoTest {

    private lateinit var db: CartioDatabase
    private lateinit var dao: ShoppingListDao

    @Before
    fun setUp() {
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            CartioDatabase::class.java
        ).allowMainThreadQueries().build()
        dao = db.shoppingListDao()
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun emptyDatabaseReturnsEmptyList() = runTest {
        val result = dao.getAll().first()
        assertEquals(emptyList<ShoppingListEntity>(), result)
    }

    @Test
    fun insertAndRetrieveShoppingList() = runTest {
        val entity = ShoppingListEntity(name = "Groceries", createdAt = 1000L, updatedAt = 2000L)
        val id = dao.insert(entity)

        val retrieved = dao.getById(id)
        assertEquals("Groceries", retrieved?.name)
    }

    @Test
    fun deleteShoppingList() = runTest {
        val entity = ShoppingListEntity(name = "Temp", createdAt = 1000L, updatedAt = 1000L)
        val id = dao.insert(entity)
        val inserted = dao.getById(id)!!

        dao.delete(inserted)

        assertNull(dao.getById(id))
    }
}
