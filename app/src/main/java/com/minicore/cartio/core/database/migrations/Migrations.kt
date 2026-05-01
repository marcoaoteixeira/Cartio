package com.minicore.cartio.core.database.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS expense_records (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                productName TEXT NOT NULL,
                quantity INTEGER NOT NULL,
                unitPrice REAL NOT NULL,
                measureUnit TEXT NOT NULL,
                recordedAt INTEGER NOT NULL
            )
            """.trimIndent()
        )
    }
}

/**
 * Schema 4 changes:
 * 1. shopping_list_items.quantity: REAL nullable → INTEGER NOT NULL DEFAULT 1
 *    (rounds historic floats; null becomes 1).
 * 2. New composite index on (shoppingListId, productId, checked) to speed up
 *    findActiveByProduct.
 * SQLite cannot ALTER COLUMN type, so we rebuild the table.
 */
val MIGRATION_3_4 = object : Migration(3, 4) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE shopping_list_items_new (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                shoppingListId INTEGER NOT NULL,
                productId INTEGER NOT NULL,
                quantity INTEGER NOT NULL DEFAULT 1,
                checked INTEGER NOT NULL DEFAULT 0,
                note TEXT,
                FOREIGN KEY (shoppingListId) REFERENCES shopping_lists(id) ON UPDATE NO ACTION ON DELETE CASCADE,
                FOREIGN KEY (productId) REFERENCES products(id) ON UPDATE NO ACTION ON DELETE RESTRICT
            )
            """.trimIndent()
        )
        db.execSQL(
            """
            INSERT INTO shopping_list_items_new (id, shoppingListId, productId, quantity, checked, note)
            SELECT id, shoppingListId, productId,
                   COALESCE(CAST(quantity AS INTEGER), 1),
                   checked, note
            FROM shopping_list_items
            """.trimIndent()
        )
        db.execSQL("DROP TABLE shopping_list_items")
        db.execSQL("ALTER TABLE shopping_list_items_new RENAME TO shopping_list_items")
        db.execSQL("CREATE INDEX index_shopping_list_items_shoppingListId ON shopping_list_items(shoppingListId)")
        db.execSQL("CREATE INDEX index_shopping_list_items_productId ON shopping_list_items(productId)")
        db.execSQL(
            """
            CREATE INDEX index_shopping_list_items_shoppingListId_productId_checked
                ON shopping_list_items(shoppingListId, productId, checked)
            """.trimIndent()
        )
    }
}

/**
 * Schema 5 changes:
 * Drop the unused `price_history` table. Price tracking was scaffolded ahead
 * of an MVP feature that is no longer planned (see ADR-017).
 */
val MIGRATION_4_5 = object : Migration(4, 5) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("DROP TABLE IF EXISTS price_history")
    }
}

val ALL_MIGRATIONS = arrayOf(MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5)
