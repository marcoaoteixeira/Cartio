package com.nameless.cartio.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.nameless.cartio.core.database.entity.ShoppingListItemEntity
import com.nameless.cartio.core.database.entity.ShoppingListItemWithProduct
import kotlinx.coroutines.flow.Flow

@Dao
interface ShoppingListItemDao {
    @Transaction
    @Query("SELECT * FROM shopping_list_items WHERE shoppingListId = :listId ORDER BY checked ASC, id ASC")
    fun getByListWithProduct(listId: Long): Flow<List<ShoppingListItemWithProduct>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: ShoppingListItemEntity): Long

    @Query("UPDATE shopping_list_items SET quantity = :quantity WHERE id = :id")
    suspend fun updateQuantity(id: Long, quantity: Float)

    @Query("UPDATE shopping_list_items SET checked = :checked WHERE id = :id")
    suspend fun updateChecked(id: Long, checked: Boolean)

    @Query("DELETE FROM shopping_list_items WHERE id = :id")
    suspend fun deleteById(id: Long)
}
