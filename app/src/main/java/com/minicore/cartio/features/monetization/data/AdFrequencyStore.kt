package com.minicore.cartio.features.monetization.data

import android.content.SharedPreferences
import com.minicore.cartio.core.time.Clock
import com.minicore.cartio.features.monetization.MonetizationConfig
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class AdFrequencyStore @Inject constructor(
    private val prefs: SharedPreferences,
    private val clock: Clock
) {

    suspend fun shouldShowAd(): Boolean {
        val lastShown = prefs.getLong(KEY_LAST_SHOWN, 0L)
        if (lastShown == 0L) return true
        val elapsed = clock.now() - lastShown
        return elapsed >= TimeUnit.MINUTES.toMillis(MonetizationConfig.FREQUENCY_CAP_MINUTES)
    }

    suspend fun markShown() {
        prefs.edit().putLong(KEY_LAST_SHOWN, clock.now()).apply()
    }

    companion object {
        const val PREFS_FILE = "ad_frequency_prefs"
        private const val KEY_LAST_SHOWN = "last_ad_shown_ms"
    }
}
