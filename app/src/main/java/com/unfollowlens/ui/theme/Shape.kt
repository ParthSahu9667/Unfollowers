package com.unfollowlens.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

val AppShapes = Shapes(
    // Chips, small buttons
    small = RoundedCornerShape(12.dp),
    // Buttons, inputs
    medium = RoundedCornerShape(16.dp),
    // Cards — soft, no sharp corners anywhere
    large = RoundedCornerShape(24.dp),
    // Sheets, elevated panels
    extraLarge = RoundedCornerShape(28.dp)
)

// Explicit named radii for custom composables
object CornerRadius {
    val Card = 24.dp
    val CardLarge = 28.dp
    val Chip = 16.dp
    val Button = 16.dp
    val Sheet = 28.dp
    val Avatar = 50.dp  // fully round
}
