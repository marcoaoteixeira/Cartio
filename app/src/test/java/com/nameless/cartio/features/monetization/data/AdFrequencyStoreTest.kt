package com.nameless.cartio.features.monetization.data

import android.content.SharedPreferences
import com.nameless.cartio.features.monetization.MonetizationConfig
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.concurrent.TimeUnit

class AdFrequencyStoreTest {

    @Test
    fun `never shown returns true`() = runTest {
        val store = AdFrequencyStore(InMemorySharedPreferences())
        assertTrue(store.shouldShowAd())
    }

    @Test
    fun `shown just now returns false`() = runTest {
        val prefs = InMemorySharedPreferences()
        val now = 1_000_000L
        AdFrequencyStore(prefs, clock = { now }).markShown()
        assertFalse(AdFrequencyStore(prefs, clock = { now }).shouldShowAd())
    }

    @Test
    fun `shown within cap returns false`() = runTest {
        val prefs = InMemorySharedPreferences()
        val base = 1_000_000L
        AdFrequencyStore(prefs, clock = { base }).markShown()

        val withinCap = base + TimeUnit.MINUTES.toMillis(MonetizationConfig.FREQUENCY_CAP_MINUTES) - 1
        assertFalse(AdFrequencyStore(prefs, clock = { withinCap }).shouldShowAd())
    }

    @Test
    fun `shown at cap boundary returns true`() = runTest {
        val prefs = InMemorySharedPreferences()
        val base = 1_000_000L
        AdFrequencyStore(prefs, clock = { base }).markShown()

        val atCap = base + TimeUnit.MINUTES.toMillis(MonetizationConfig.FREQUENCY_CAP_MINUTES)
        assertTrue(AdFrequencyStore(prefs, clock = { atCap }).shouldShowAd())
    }
}
