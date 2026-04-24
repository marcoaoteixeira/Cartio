package com.nameless.cartio.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.nameless.cartio.core.database.entity.PriceHistoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PriceHistoryDao {
    @Query("SELECT * FROM price_history WHERE productId = :productId ORDER BY recordedAt DESC")
    fun getByProduct(productId: Long): Flow<List<PriceHistoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entry: PriceHistoryEntity): Long
}
