package com.minicore.cartio.features.shopping.data

import com.minicore.cartio.core.database.dao.ShoppingListWithCount
import com.minicore.cartio.core.database.entity.ShoppingListEntity
import org.junit.Assert.assertEquals
import org.junit.Test

class ShoppingListMapperTest {

    @Test
    fun `ShoppingListEntity toDomain maps all fields with zero counts`() {
        val entity = ShoppingListEntity(id = 1L, name = "Groceries", createdAt = 1000L, updatedAt = 2000L)
        val domain = entity.toDomain()
        assertEquals(1L, domain.id)
        assertEquals("Groceries", domain.name)
        assertEquals(1000L, domain.createdAt)
        assertEquals(2000L, domain.updatedAt)
        assertEquals(0, domain.itemCount)
        assertEquals(0, domain.checkedCount)
    }

    @Test
    fun `ShoppingListWithCount toDomain maps all fields including counts`() {
        val withCount = ShoppingListWithCount(
            id = 2L, name = "Hardware", createdAt = 500L, updatedAt = 1500L,
            itemCount = 5, checkedCount = 3
        )
        val domain = withCount.toDomain()
        assertEquals(2L, domain.id)
        assertEquals("Hardware", domain.name)
        assertEquals(500L, domain.createdAt)
        assertEquals(1500L, domain.updatedAt)
        assertEquals(5, domain.itemCount)
        assertEquals(3, domain.checkedCount)
    }

    @Test
    fun `ShoppingListWithCount toDomain handles zero counts`() {
        val withCount = ShoppingListWithCount(
            id = 3L, name = "Empty", createdAt = 0L, updatedAt = 0L,
            itemCount = 0, checkedCount = 0
        )
        val domain = withCount.toDomain()
        assertEquals(0, domain.itemCount)
        assertEquals(0, domain.checkedCount)
    }
}
