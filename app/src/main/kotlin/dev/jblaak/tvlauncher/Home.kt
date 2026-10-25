package dev.jblaak.tvlauncher

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview

@Composable
internal fun Home() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF101224)),
    )
}

@Preview(device = "id:tv_1080p")
@Composable
internal fun HomePreview() {
    Home()
}
