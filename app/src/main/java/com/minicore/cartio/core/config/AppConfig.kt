package com.minicore.cartio.core.config

object AppConfig {
    const val PLAY_STORE_URL =
        "https://play.google.com/store/apps/details?id=com.minicore.cartio"

    /**
     * When true, a missing migration will silently drop and recreate the database.
     * Off by default — flip to true locally only while iterating on schema changes.
     * Must never be true on a release build.
     */
    const val DROP_DB_ON_MIGRATION_FAILURE = false
}
