package com.nameless.cartio.core.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.nameless.cartio.core.database.entity.ShoppingListEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ShoppingListDao {
    @Query("SELECT * FROM shopping_lists ORDER BY updatedAt DESC")
    fun getAll(): Flow<List<ShoppingListEntity>>

    @Query("SELECT * FROM shopping_lists WHERE id = :id")
    suspend fun getById(id: Long): ShoppingListEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(shoppingList: ShoppingListEntity): Long

    @Delete
    suspend fun delete(shoppingList: ShoppingListEntity)
}
