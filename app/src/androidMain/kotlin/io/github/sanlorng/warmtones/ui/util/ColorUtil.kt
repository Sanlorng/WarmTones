package io.github.sanlorng.warmtones.ui.util

import androidx.compose.ui.graphics.Color
import kotlin.math.abs

fun String.toColor(): Color {
    val hash = this.hashCode()
    val random = kotlin.random.Random(hash)
    return Color(
        red = (abs(random.nextInt()) % 106 + 150) / 255f,
        green = (abs(random.nextInt()) % 106 + 150) / 255f,
        blue = (abs(random.nextInt()) % 106 + 150) / 255f,
    )
}