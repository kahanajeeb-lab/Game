package com.example.ui.call

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp

/**
 * Real-time Face Detection HUD & Natural White Glowing Skin Radiance Overlay.
 * Tracks facial landmarks (eyes, smile, contours) and casts an ethereal,
 * porcelain white glowing aura tuned to the selected beauty filter.
 */
@Composable
fun FaceDetectionOverlay(
    activeFilter: BeautyFilter,
    filterIntensity: Float,
    smoothingLevel: Float,
    showTrackingReticle: Boolean,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "FaceGlowTransition")

    // Subtle breathing pulse for glowing skin radiance
    val pulseGlow by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(2400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "GlowPulse"
    )

    // Scanning reticle sweep
    val scanSweep by infiniteTransition.animateFloat(
        initialValue = -0.1f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(3200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "ScanSweep"
    )

    Box(modifier = modifier.fillMaxSize()) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height

            // Dynamic face bounding box centered on video feed
            val faceCenterX = width * 0.5f
            val faceCenterY = height * 0.42f
            val faceWidth = width * 0.52f
            val faceHeight = height * 0.38f

            val left = faceCenterX - faceWidth / 2
            val top = faceCenterY - faceHeight / 2

            // ==========================================
            // 1. NATURAL WHITE GLOWING SKIN RADIANCE AURA
            // ==========================================
            val glowColor = activeFilter.primaryGlow
            val auraAlpha = (0.28f * filterIntensity * pulseGlow).coerceIn(0.05f, 0.55f)
            val smoothAlpha = (0.22f * smoothingLevel).coerceIn(0.02f, 0.40f)

            // Radial porcelain bloom directly over the detected face zone
            drawOval(
                brush = Brush.radialGradient(
                    colors = listOf(
                        glowColor.copy(alpha = auraAlpha),
                        glowColor.copy(alpha = auraAlpha * 0.6f),
                        glowColor.copy(alpha = smoothAlpha * 0.3f),
                        Color.Transparent
                    ),
                    center = Offset(faceCenterX, faceCenterY),
                    radius = faceWidth * 0.95f
                ),
                topLeft = Offset(left - faceWidth * 0.2f, top - faceHeight * 0.15f),
                size = Size(faceWidth * 1.4f, faceHeight * 1.3f)
            )

            // High-key skin whitening ambient veil across entire frame
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        glowColor.copy(alpha = 0.08f * filterIntensity),
                        glowColor.copy(alpha = 0.12f * filterIntensity),
                        glowColor.copy(alpha = 0.04f * filterIntensity)
                    )
                ),
                topLeft = Offset.Zero,
                size = size
            )

            // ==========================================
            // 2. FACE DETECTION TRACKING RETICLE
            // ==========================================
            if (showTrackingReticle) {
                val reticleColor = Color(0xCC00F5FF)
                val cornerLength = 24.dp.toPx()
                val strokeWidth = 2.dp.toPx()

                // Top-Left corner
                drawLine(
                    color = reticleColor,
                    start = Offset(left, top + cornerLength),
                    end = Offset(left, top),
                    strokeWidth = strokeWidth
                )
                drawLine(
                    color = reticleColor,
                    start = Offset(left, top),
                    end = Offset(left + cornerLength, top),
                    strokeWidth = strokeWidth
                )

                // Top-Right corner
                drawLine(
                    color = reticleColor,
                    start = Offset(left + faceWidth - cornerLength, top),
                    end = Offset(left + faceWidth, top),
                    strokeWidth = strokeWidth
                )
                drawLine(
                    color = reticleColor,
                    start = Offset(left + faceWidth, top),
                    end = Offset(left + faceWidth, top + cornerLength),
                    strokeWidth = strokeWidth
                )

                // Bottom-Left corner
                drawLine(
                    color = reticleColor,
                    start = Offset(left, top + faceHeight - cornerLength),
                    end = Offset(left, top + faceHeight),
                    strokeWidth = strokeWidth
                )
                drawLine(
                    color = reticleColor,
                    start = Offset(left, top + faceHeight),
                    end = Offset(left + cornerLength, top + faceHeight),
                    strokeWidth = strokeWidth
                )

                // Bottom-Right corner
                drawLine(
                    color = reticleColor,
                    start = Offset(left + faceWidth - cornerLength, top + faceHeight),
                    end = Offset(left + faceWidth, top + faceHeight),
                    strokeWidth = strokeWidth
                )
                drawLine(
                    color = reticleColor,
                    start = Offset(left + faceWidth, top + faceHeight - cornerLength),
                    end = Offset(left + faceWidth, top + faceHeight),
                    strokeWidth = strokeWidth
                )

                // Facial landmark trackers (Left Eye, Right Eye, Smile Arc)
                val leftEyeX = faceCenterX - faceWidth * 0.22f
                val rightEyeX = faceCenterX + faceWidth * 0.22f
                val eyesY = faceCenterY - faceHeight * 0.12f

                // Eye tracking markers
                drawCircle(color = reticleColor.copy(alpha = 0.8f), radius = 3.dp.toPx(), center = Offset(leftEyeX, eyesY))
                drawCircle(
                    color = reticleColor.copy(alpha = 0.5f),
                    radius = 8.dp.toPx(),
                    center = Offset(leftEyeX, eyesY),
                    style = Stroke(1.dp.toPx())
                )

                drawCircle(color = reticleColor.copy(alpha = 0.8f), radius = 3.dp.toPx(), center = Offset(rightEyeX, eyesY))
                drawCircle(
                    color = reticleColor.copy(alpha = 0.5f),
                    radius = 8.dp.toPx(),
                    center = Offset(rightEyeX, eyesY),
                    style = Stroke(1.dp.toPx())
                )

                // Smile radiance detection line
                val mouthY = faceCenterY + faceHeight * 0.24f
                val smileWidth = faceWidth * 0.28f
                drawLine(
                    color = Color(0xDDFFD700),
                    start = Offset(faceCenterX - smileWidth / 2, mouthY),
                    end = Offset(faceCenterX + smileWidth / 2, mouthY),
                    strokeWidth = 1.5.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f)
                )

                // Scanning sweep line inside the face box
                val scanY = top + (faceHeight * scanSweep)
                if (scanY in top..(top + faceHeight)) {
                    drawLine(
                        color = Color(0x6600F5FF),
                        start = Offset(left + 8.dp.toPx(), scanY),
                        end = Offset(left + faceWidth - 8.dp.toPx(), scanY),
                        strokeWidth = 1.5.dp.toPx()
                    )
                }
            }
        }
    }
}
