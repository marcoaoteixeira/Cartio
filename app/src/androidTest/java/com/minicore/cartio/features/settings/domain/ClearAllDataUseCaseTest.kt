package com.minicore.cartio.features.settings.domain

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.minicore.cartio.core.database.CartioDatabase
import com.minicore.cartio.core.database.entity.MeasureUnit
import com.minicore.cartio.core.database.entity.ProductEntity
import com.minicore.cartio.core.database.entity.ShoppingListEntity
import com.minicore.cartio.core.database.entity.ShoppingListItemEntity
import com.minicore.cartio.features.expenses.data.ExpenseRecordDao
import com.minicore.cartio.features.expenses.data.ExpenseRecordEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ClearAllDataUseCaseTest {

    private lateinit var db: CartioDatabase
    private lateinit var useCase: ClearAllDataUseCase

    @Before
    fun setUp() {
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            CartioDatabase::class.java
        ).allowMainThreadQueries().build()
        useCase = ClearAllDataUseCase(
            database = db,
            listDao = db.shoppingListDao(),
            productDao = db.productDao(),
            expenseRecordDao = db.expenseRecordDao()
        )
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun invoke_wipesEveryTable() = runTest {
        val listId = db.shoppingListDao().insert(
            ShoppingListEntity(name = "Groceries", createdAt = 1L, updatedAt = 1L)
        )
        val productId = db.productDao().insert(
            ProductEntity(name = "Milk", createdAt = 1L)
        )
        db.shoppingListItemDao().insert(
            ShoppingListItemEntity(
                shoppingListId = listId,
                productId = productId,
                quantity = 1,
                checked = false
            )
        )
        db.expenseRecordDao().insertAll(
            listOf(
                ExpenseRecordEntity(
                    productName = "Milk",
                    quantity = 1,
                    unitPrice = 2.50,
                    measureUnit = MeasureUnit.Piece,
                    recordedAt = 1L
                )
            )
        )

        useCase()

        assertTrue(db.shoppingListDao().getAllWithItemCount().first().isEmpty())
        assertTrue(db.productDao().getAll().first().isEmpty())
        assertTrue(db.shoppingListItemDao().getByListWithProduct(listId).first().isEmpty())
        assertEquals(
            emptyList<ExpenseRecordEntity>(),
            db.expenseRecordDao().getRecordsSince(0L).first()
        )
    }

    @Test
    fun invoke_onEmptyDatabase_isIdempotent() = runTest {
        useCase()
        useCase()
        assertTrue(db.shoppingListDao().getAllWithItemCount().first().isEmpty())
    }

    @Test
    fun invoke_whenAnyDaoThrows_rollsBackEntireWipe() = runTest {
        db.shoppingListDao().insert(
            ShoppingListEntity(name = "Keep", createdAt = 1L, updatedAt = 1L)
        )
        db.productDao().insert(
            ProductEntity(name = "Keep", createdAt = 1L)
        )

        val explodingExpenseDao = object : ExpenseRecordDao by db.expenseRecordDao() {
            override suspend fun clearAll(): Unit = error("boom")
        }
        val failingUseCase = ClearAllDataUseCase(
            database = db,
            listDao = db.shoppingListDao(),
            productDao = db.productDao(),
            expenseRecordDao = explodingExpenseDao
        )

        try {
            failingUseCase()
            fail("Expected use case to propagate the DAO failure")
        } catch (_: IllegalStateException) {
            // expected
        }

        assertEquals(1, db.shoppingListDao().getAllWithItemCount().first().size)
        assertEquals(1, db.productDao().getAll().first().size)
    }
}
