package com.minicore.cartio.features.splash.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.minicore.cartio.R
import kotlinx.coroutines.delay

private const val SPLASH_DURATION_MS = 600L

@Composable
fun CartioSplashScreen(onComplete: () -> Unit) {
    LaunchedEffect(Unit) {
        delay(SPLASH_DURATION_MS)
        onComplete()
    }

    Image(
        painter = painterResource(R.drawable.splash_screen),
        contentDescription = null,
        modifier = Modifier.fillMaxSize(),
        contentScale = ContentScale.FillBounds
    )
}
