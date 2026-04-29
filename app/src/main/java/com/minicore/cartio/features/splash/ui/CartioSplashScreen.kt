package com.minicore.cartio.features.splash.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.Canvas
import com.minicore.cartio.R
import com.minicore.cartio.core.ui.theme.AutolovaFamily
import com.minicore.cartio.core.ui.theme.BrandYellow
import kotlinx.coroutines.delay

// Minimum-visible time so the brand splash registers but doesn't feel
// artificially slow on warm starts. Was 2000ms; trimmed to the 600ms
// suggested by Material guidelines for branded splashes.
private const val SPLASH_DURATION_MS = 600L

@Composable
fun CartioSplashScreen(onComplete: () -> Unit) {
    LaunchedEffect(Unit) {
        delay(SPLASH_DURATION_MS)
        onComplete()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val waveTopY = size.height * 0.72f
            val path = Path().apply {
                moveTo(0f, waveTopY + 40f)
                cubicTo(
                    size.width * 0.25f, waveTopY - 20f,
                    size.width * 0.75f, waveTopY + 80f,
                    size.width, waveTopY + 20f
                )
                lineTo(size.width, size.height)
                lineTo(0f, size.height)
                close()
            }
            drawPath(path, BrandYellow)
        }

        Column(
            modifier = Modifier.align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Image(
                painter = painterResource(R.drawable.shopping_cart),
                contentDescription = null,
                modifier = Modifier.size(180.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Cartio",
                fontFamily = AutolovaFamily,
                fontSize = 52.sp,
                color = Color.Black
            )
        }
    }
}
