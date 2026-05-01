package com.minicore.cartio

import android.app.Application
import com.google.android.gms.ads.MobileAds
import com.minicore.cartio.core.logging.AppLogger
import com.minicore.cartio.di.ApplicationScope
import com.minicore.cartio.features.monetization.AppStartupInitializer
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltAndroidApp
class CartioApp : Application() {

    companion object {
        private const val TAG = "CartioApp"
    }

    @Inject lateinit var startupInitializer: AppStartupInitializer

    @Inject @ApplicationScope lateinit var applicationScope: CoroutineScope

    override fun onCreate() {
        super.onCreate()
        MobileAds.initialize(this)
        applicationScope.launch(Dispatchers.Default) {
            runCatching { startupInitializer.initialize(applicationContext) }
                .onFailure { AppLogger.e(TAG, "Startup initialization failed", it) }
        }
    }

    override fun onTerminate() {
        applicationScope.cancel()
        super.onTerminate()
    }
}
