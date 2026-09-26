package com.example.ui.call

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorMatrix

/**
 * 10 Natural White Glowing Skin Beauty Filters:
 * Specially formulated to provide soft, luminescent, porcelain, and radiant skin whitening
 * while preserving natural facial features and eye clarity.
 */
data class BeautyFilter(
    val id: String,
    val name: String,
    val tagLine: String,
    val primaryGlow: Color,
    val brightness: Float,       // Luminance boost
    val contrast: Float,         // Smooth contrast
    val whiteGlowTone: Float,    // White skin luminance saturation
    val warmthHue: Float,        // Subtle rose/ivory tint balance
    val isFavorite: Boolean = false
) {
    /**
     * Constructs a 4x5 ColorMatrix that transforms the camera stream into
     * glowing natural white radiant skin.
     */
    fun toColorMatrix(intensity: Float, smoothing: Float): ColorMatrix {
        val b = (brightness * intensity) * 255f
        val c = 1.0f + (contrast - 1.0f) * intensity
        val w = warmthHue * intensity
        val glow = whiteGlowTone * intensity

        // Luminescent white matrix calculation
        return ColorMatrix(
            floatArrayOf(
                c * (1f + glow * 0.15f), 0f, 0f, 0f, b + (w * 10f),
                0f, c * (1f + glow * 0.18f), 0f, 0f, b + (glow * 8f),
                0f, 0f, c * (1f + glow * 0.22f), 0f, b + (glow * 14f),
                0f, 0f, 0f, 1f, 0f
            )
        )
    }
}

object BeautyFilterPresets {
    val filters = listOf(
        BeautyFilter(
            id = "porcelain_glow",
            name = "Porcelain Glow",
            tagLine = "Ivory luminescent clarity with gentle whitening",
            primaryGlow = Color(0xFFFDFBF7),
            brightness = 0.14f,
            contrast = 1.05f,
            whiteGlowTone = 0.22f,
            warmthHue = 0.05f,
            isFavorite = true
        ),
        BeautyFilter(
            id = "pearl_radiance",
            name = "Pearl Radiance",
            tagLine = "Lustrous white pearl sheen with radiant highlights",
            primaryGlow = Color(0xFFF9F6F0),
            brightness = 0.18f,
            contrast = 1.08f,
            whiteGlowTone = 0.28f,
            warmthHue = 0.08f,
            isFavorite = true
        ),
        BeautyFilter(
            id = "ivory_smooth",
            name = "Ivory Smooth",
            tagLine = "Velvety soft-focus skin whitening & blemish blur",
            primaryGlow = Color(0xFFFFFFF0),
            brightness = 0.12f,
            contrast = 1.02f,
            whiteGlowTone = 0.20f,
            warmthHue = 0.04f,
            isFavorite = false
        ),
        BeautyFilter(
            id = "snow_white",
            name = "Snow White",
            tagLine = "Clean, crisp high-key alabaster brilliance",
            primaryGlow = Color(0xFFF0F8FF),
            brightness = 0.22f,
            contrast = 1.10f,
            whiteGlowTone = 0.32f,
            warmthHue = -0.02f,
            isFavorite = false
        ),
        BeautyFilter(
            id = "crystal_clear",
            name = "Crystal Clear",
            tagLine = "Translucent Korean glass-skin pure radiance",
            primaryGlow = Color(0xFFF5FFFF),
            brightness = 0.16f,
            contrast = 1.12f,
            whiteGlowTone = 0.25f,
            warmthHue = 0.02f,
            isFavorite = true
        ),
        BeautyFilter(
            id = "dewy_aura",
            name = "Dewy Aura",
            tagLine = "Ultra-hydrated, glistening fresh morning dew reflection",
            primaryGlow = Color(0xFFFAF0E6),
            brightness = 0.15f,
            contrast = 1.06f,
            whiteGlowTone = 0.24f,
            warmthHue = 0.10f,
            isFavorite = false
        ),
        BeautyFilter(
            id = "silk_cream",
            name = "Silk Cream",
            tagLine = "Warm creamy white undertone with satin diffuse glow",
            primaryGlow = Color(0xFFFFF8DC),
            brightness = 0.13f,
            contrast = 1.04f,
            whiteGlowTone = 0.21f,
            warmthHue = 0.12f,
            isFavorite = false
        ),
        BeautyFilter(
            id = "milky_blossom",
            name = "Milky Blossom",
            tagLine = "Pearlescent white touched with subtle petal vitality",
            primaryGlow = Color(0xFFFFF0F5),
            brightness = 0.17f,
            contrast = 1.07f,
            whiteGlowTone = 0.26f,
            warmthHue = 0.14f,
            isFavorite = true
        ),
        BeautyFilter(
            id = "opalescent_shimmer",
            name = "Opalescent Shimmer",
            tagLine = "Radiant soft-white prismatic glow reflecting ambient light",
            primaryGlow = Color(0xFFE6E6FA),
            brightness = 0.19f,
            contrast = 1.09f,
            whiteGlowTone = 0.29f,
            warmthHue = 0.06f,
            isFavorite = false
        ),
        BeautyFilter(
            id = "moonlit_radiance",
            name = "Moonlit Radiance",
            tagLine = "Cool ethereal silver-white luminescent night glow",
            primaryGlow = Color(0xFFE0EEEE),
            brightness = 0.20f,
            contrast = 1.11f,
            whiteGlowTone = 0.30f,
            warmthHue = -0.04f,
            isFavorite = false
        )
    )
}
