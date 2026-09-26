package com.example.ui.chat

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp

enum class ChatWallpaper(val displayName: String, val bgColor: Color, val incomingBubbleColor: Color, val outgoingBubbleColor: Color) {
    WHATSAPP_DOODLE(
        displayName = "Classic WhatsApp",
        bgColor = Color(0xFF0B141A),
        incomingBubbleColor = Color(0xFF1F2C34),
        outgoingBubbleColor = Color(0xFF005C4B)
    ),
    EMERALD_DEEP(
        displayName = "Emerald Deep",
        bgColor = Color(0xFF051D18),
        incomingBubbleColor = Color(0xFF122E28),
        outgoingBubbleColor = Color(0xFF007A5E)
    ),
    MIDNIGHT_AMOLED(
        displayName = "Midnight AMOLED",
        bgColor = Color(0xFF000000),
        incomingBubbleColor = Color(0xFF18181B),
        outgoingBubbleColor = Color(0xFF14532D)
    ),
    CYBER_NAVY(
        displayName = "Cyber Navy",
        bgColor = Color(0xFF0A192F),
        incomingBubbleColor = Color(0xFF172A45),
        outgoingBubbleColor = Color(0xFF1E3A8A)
    ),
    DESERT_WARMTH(
        displayName = "Desert Warmth",
        bgColor = Color(0xFF1C1917),
        incomingBubbleColor = Color(0xFF292524),
        outgoingBubbleColor = Color(0xFF78350F)
    )
}

@Composable
fun ChatBackgroundCanvas(
    wallpaper: ChatWallpaper,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(wallpaper.bgColor)
    ) {
        // Subtle doodle geometry pattern for WhatsApp vibe
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height
            val patternColor = Color(0x08FFFFFF)
            val strokeWidth = 1.dp.toPx()

            val step = 80.dp.toPx()
            var y = 0f
            while (y < height) {
                var x = 0f
                while (x < width) {
                    // Small subtle chat doodle shapes (speech bubble, lock, dots)
                    drawCircle(
                        color = patternColor,
                        radius = 2.5.dp.toPx(),
                        center = Offset(x + 20f, y + 20f)
                    )
                    drawCircle(
                        color = patternColor,
                        radius = 1.5.dp.toPx(),
                        center = Offset(x + 55f, y + 45f)
                    )
                    // Mini doodle rectangle
                    drawRect(
                        color = patternColor,
                        topLeft = Offset(x + 35f, y + 15f),
                        size = androidx.compose.ui.geometry.Size(12.dp.toPx(), 8.dp.toPx()),
                        style = Stroke(strokeWidth)
                    )
                    x += step
                }
                y += step
            }
        }
    }
}
