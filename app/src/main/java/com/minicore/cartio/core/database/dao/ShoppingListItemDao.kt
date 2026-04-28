package com.minicore.cartio.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.minicore.cartio.core.database.entity.ShoppingListItemEntity
import com.minicore.cartio.core.database.entity.ShoppingListItemWithProduct
import kotlinx.coroutines.flow.Flow

@Dao
abstract class ShoppingListItemDao {
    @Transaction
    @Query("SELECT * FROM shopping_list_items WHERE shoppingListId = :listId ORDER BY checked ASC, id ASC")
    abstract fun getByListWithProduct(listId: Long): Flow<List<ShoppingListItemWithProduct>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insert(item: ShoppingListItemEntity): Long

    @Query("UPDATE shopping_list_items SET quantity = :quantity WHERE id = :id")
    abstract suspend fun updateQuantity(id: Long, quantity: Int)

    @Query("UPDATE shopping_list_items SET quantity = quantity + 1 WHERE id = :id")
    abstract suspend fun incrementQuantity(id: Long)

    @Query("UPDATE shopping_list_items SET checked = :checked WHERE id = :id")
    abstract suspend fun updateChecked(id: Long, checked: Boolean)

    @Query("SELECT * FROM shopping_list_items WHERE shoppingListId = :listId AND productId = :productId AND checked = 0 LIMIT 1")
    abstract suspend fun findActiveByProduct(listId: Long, productId: Long): ShoppingListItemEntity?

    @Query("DELETE FROM shopping_list_items WHERE id = :id")
    abstract suspend fun deleteById(id: Long)

    @Transaction
    open suspend fun addOrIncrement(listId: Long, productId: Long) {
        val existing = findActiveByProduct(listId, productId)
        if (existing != null) {
            incrementQuantity(existing.id)
        } else {
            insert(ShoppingListItemEntity(shoppingListId = listId, productId = productId, quantity = 1))
        }
    }
}
