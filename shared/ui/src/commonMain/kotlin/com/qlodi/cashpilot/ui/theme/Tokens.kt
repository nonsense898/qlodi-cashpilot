package com.qlodi.cashpilot.ui.theme

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp

val PillShape = RoundedCornerShape(999.dp)

/** Motion — пружинні специфікації (expressive-принцип). Spacing/Radii/Shapes — у Theme.kt. */
object Motion {
    fun <T> spatial() = spring<T>(dampingRatio = 0.82f, stiffness = Spring.StiffnessMediumLow)
    fun <T> fast() = tween<T>(220)
    fun <T> emphasized() = tween<T>(400)
}
