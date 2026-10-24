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

    /**
     * Gates the camera-scan-to-add-item entry point (FEAT-026).
     * Off by default until the feature is verified stable.
     */
    const val SCANNER_ENABLED = false

    /**
     * A/B toggle (FEAT-026): when true, selecting a recognized text block adds the
     * item to the list immediately instead of pre-filling the AddItemBar for manual
     * confirmation. Off by default — matches the documented "no auto-add" decision
     * until the auto-add variant is validated.
     */
    const val SCAN_AUTO_ADD_ENABLED = false
}
