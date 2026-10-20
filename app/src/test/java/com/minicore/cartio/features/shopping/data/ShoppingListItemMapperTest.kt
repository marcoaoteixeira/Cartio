package com.minicore.cartio.features.shopping.data

import com.minicore.cartio.core.database.entity.ProductEntity
import com.minicore.cartio.core.database.entity.ShoppingListItemEntity
import com.minicore.cartio.core.database.entity.ShoppingListItemWithProduct
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ShoppingListItemMapperTest {

    private val product = ProductEntity(id = 10L, name = "Milk", createdAt = 0L)

    @Test
    fun `toDomain maps all fields from embedded item and product`() {
        val entity = ShoppingListItemEntity(
            id = 1L, shoppingListId = 5L, productId = 10L,
            quantity = 2, checked = false, note = "organic"
        )
        val domain = ShoppingListItemWithProduct(item = entity, product = product).toDomain()

        assertEquals(1L, domain.id)
        assertEquals(5L, domain.listId)
        assertEquals(10L, domain.productId)
        assertEquals("Milk", domain.productName)
        assertEquals(2, domain.quantity)
        assertEquals(false, domain.checked)
        assertEquals("organic", domain.note)
    }

    @Test
    fun `toDomain maps checked item correctly`() {
        val entity = ShoppingListItemEntity(
            id = 2L, shoppingListId = 5L, productId = 10L,
            quantity = 1, checked = true, note = null
        )
        val domain = ShoppingListItemWithProduct(item = entity, product = product).toDomain()

        assertEquals(true, domain.checked)
        assertNull(domain.note)
    }

    @Test
    fun `toDomain uses product name from relation not item`() {
        val renamedProduct = product.copy(name = "Whole Milk")
        val entity = ShoppingListItemEntity(id = 3L, shoppingListId = 1L, productId = 10L)
        val domain = ShoppingListItemWithProduct(item = entity, product = renamedProduct).toDomain()

        assertEquals("Whole Milk", domain.productName)
    }
}
