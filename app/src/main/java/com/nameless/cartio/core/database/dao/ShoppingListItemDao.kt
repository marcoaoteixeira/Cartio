package com.nameless.cartio.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.nameless.cartio.core.database.entity.ShoppingListItemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ShoppingListItemDao {
    @Query("SELECT * FROM shopping_list_items WHERE shoppingListId = :listId")
    fun getByList(listId: Long): Flow<List<ShoppingListItemEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: ShoppingListItemEntity): Long
}
