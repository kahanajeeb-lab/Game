package com.example.ui.call

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlin.random.Random

data class HeartParticle(
    val id: Long,
    val startXOffset: Float,
    val color: Color,
    val size: Float
)

@Composable
fun HeartReactionStream(
    hearts: List<HeartParticle>,
    onParticleFinished: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxSize()) {
        hearts.forEach { heart ->
            SingleFloatingHeart(
                particle = heart,
                onFinished = { onParticleFinished(heart.id) }
            )
        }
    }
}

@Composable
private fun SingleFloatingHeart(
    particle: HeartParticle,
    onFinished: () -> Unit
) {
    val offsetY = remember { Animatable(0f) }
    val alpha = remember { Animatable(1f) }
    val scale = remember { Animatable(0.4f) }

    LaunchedEffect(particle.id) {
        // Expand scale quickly
        scale.animateTo(1.2f, tween(300))
        scale.animateTo(particle.size, tween(400))

        // Float upwards
        offsetY.animateTo(
            targetValue = -750f,
            animationSpec = tween(durationMillis = 2200, easing = LinearEasing)
        )
    }

    LaunchedEffect(particle.id) {
        // Fade out near the top
        alpha.animateTo(1f, tween(1200))
        alpha.animateTo(0f, tween(1000))
        onFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize(),
        contentAlignment = Alignment.BottomEnd
    ) {
        Icon(
            imageVector = Icons.Default.Favorite,
            contentDescription = "Floating Heart",
            tint = particle.color,
            modifier = Modifier
                .offset {
                    IntOffset(
                        x = (-60 + particle.startXOffset).toInt(),
                        y = (-140 + offsetY.value).toInt()
                    )
                }
                .size((32 * scale.value).dp)
                .alpha(alpha.value)
        )
    }
}
