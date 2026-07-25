package com.unfollowlens.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.unfollowlens.ui.theme.BgSurface
import com.unfollowlens.ui.theme.BgSurfaceElevated

@Composable
fun ShimmerEffect(modifier: Modifier = Modifier, height: Dp = 60.dp, cornerRadius: Dp = 20.dp) {
    val transition = rememberInfiniteTransition(label = "shimmer")
    val translateAnim by transition.animateFloat(
        initialValue = -300f, targetValue = 1000f,
        animationSpec = infiniteRepeatable(tween(1200, easing = LinearEasing), RepeatMode.Restart),
        label = "shimmerTranslate"
    )
    val brush = Brush.linearGradient(
        colors = listOf(BgSurface, BgSurfaceElevated, BgSurface),
        start = Offset(translateAnim, 0f),
        end = Offset(translateAnim + 300f, 0f)
    )
    Box(modifier.fillMaxWidth().height(height).clip(RoundedCornerShape(cornerRadius)).background(brush))
}

@Composable
fun ShimmerList(itemCount: Int = 6, modifier: Modifier = Modifier) {
    Column(modifier.padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        repeat(itemCount) { ShimmerEffect(height = 72.dp) }
    }
}
