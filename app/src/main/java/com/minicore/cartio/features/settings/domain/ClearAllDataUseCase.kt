package com.minicore.cartio.features.settings.domain

import androidx.room.withTransaction
import com.minicore.cartio.core.database.CartioDatabase
import com.minicore.cartio.core.database.dao.ProductDao
import com.minicore.cartio.core.database.dao.ShoppingListDao
import com.minicore.cartio.features.expenses.data.ExpenseRecordDao
import javax.inject.Inject

class ClearAllDataUseCase @Inject constructor(
    private val database: CartioDatabase,
    private val listDao: ShoppingListDao,
    private val productDao: ProductDao,
    private val expenseRecordDao: ExpenseRecordDao
) : ClearAllData {
    override suspend fun invoke() {
        // Single transaction so a failure mid-wipe rolls back everything.
        database.withTransaction {
            listDao.clearAll()
            productDao.clearAll()
            expenseRecordDao.clearAll()
        }
    }
}
