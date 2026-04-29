package com.minicore.cartio.core.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.minicore.cartio.core.database.entity.ShoppingListEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ShoppingListDao {
    @Query("SELECT sl.id, sl.name, sl.createdAt, sl.updatedAt, COUNT(sli.id) AS itemCount, COUNT(CASE WHEN sli.checked = 1 THEN 1 END) AS checkedCount FROM shopping_lists sl LEFT JOIN shopping_list_items sli ON sli.shoppingListId = sl.id GROUP BY sl.id ORDER BY sl.updatedAt DESC")
    fun getAllWithItemCount(): Flow<List<ShoppingListWithCount>>

    @Query("SELECT sl.id, sl.name, sl.createdAt, sl.updatedAt, COUNT(sli.id) AS itemCount, COUNT(CASE WHEN sli.checked = 1 THEN 1 END) AS checkedCount FROM shopping_lists sl LEFT JOIN shopping_list_items sli ON sli.shoppingListId = sl.id GROUP BY sl.id ORDER BY sl.updatedAt DESC LIMIT :limit OFFSET :offset")
    fun getAllWithItemCountPaged(limit: Int, offset: Int): Flow<List<ShoppingListWithCount>>

    @Query("SELECT * FROM shopping_lists WHERE id = :id")
    fun getByIdFlow(id: Long): Flow<ShoppingListEntity?>

    @Query("SELECT * FROM shopping_lists WHERE id = :id")
    suspend fun getById(id: Long): ShoppingListEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(shoppingList: ShoppingListEntity): Long

    @Query("UPDATE shopping_lists SET name = :name, updatedAt = :updatedAt WHERE id = :id")
    suspend fun updateName(id: Long, name: String, updatedAt: Long)

    @Query("UPDATE shopping_lists SET updatedAt = :updatedAt WHERE id = :id")
    suspend fun updateTimestamp(id: Long, updatedAt: Long)

    @Delete
    suspend fun delete(shoppingList: ShoppingListEntity)

    @Query("DELETE FROM shopping_lists WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM shopping_lists")
    suspend fun clearAll()
}
