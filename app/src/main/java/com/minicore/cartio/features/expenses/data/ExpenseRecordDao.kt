package com.minicore.cartio.features.expenses.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ExpenseRecordDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(records: List<ExpenseRecordEntity>)

    @Query("SELECT * FROM expense_records WHERE recordedAt >= :since ORDER BY recordedAt DESC")
    fun getRecordsSince(since: Long): Flow<List<ExpenseRecordEntity>>

    @Query("DELETE FROM expense_records WHERE recordedAt < :before")
    suspend fun deleteOlderThan(before: Long)
}
