package com.nameless.cartio

import android.app.Application
import android.util.Log
import com.google.android.gms.ads.MobileAds
import com.nameless.cartio.features.monetization.AppStartupInitializer
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltAndroidApp
class CartioApp : Application() {

    companion object {
        private const val TAG = "CartioApp"
    }

    @Inject lateinit var startupInitializer: AppStartupInitializer

    override fun onCreate() {
        super.onCreate()
        MobileAds.initialize(this)
        // Dispatchers.Main is required: AdMob and Play Billing both enforce main-thread access
        GlobalScope.launch(Dispatchers.Main) {
            runCatching { startupInitializer.initialize(applicationContext) }
                .onFailure { Log.e(TAG, "Startup initialization failed", it) }
        }
    }
}
