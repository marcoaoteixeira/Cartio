package com.nameless.cartio

import android.app.Application
import com.google.android.gms.ads.MobileAds
import com.nameless.cartio.features.monetization.AppStartupInitializer
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltAndroidApp
class CartioApp : Application() {

    @Inject lateinit var startupInitializer: AppStartupInitializer

    override fun onCreate() {
        super.onCreate()
        MobileAds.initialize(this)
        GlobalScope.launch { startupInitializer.initialize(applicationContext) }
    }
}
