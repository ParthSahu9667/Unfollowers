package com.unfollowlens.ui.components

import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.unfollowlens.ui.theme.BgSurfaceElevated
import com.unfollowlens.ui.theme.GlassOverlay
import com.unfollowlens.ui.theme.StrokeHairline

/**
 * Frosted glass panel — uses Modifier.blur on API 31+ with a flat
 * semi-transparent fallback on older versions.
 *
 * Used for: top stat bar, bottom nav, modal sheets.
 */
@Composable
fun GlassPanel(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 24.dp,
    blurRadius: Dp = 20.dp,
    content: @Composable BoxScope.() -> Unit
) {
    val shape = RoundedCornerShape(cornerRadius)

    // API 31+ supports RenderEffect blur; older gets a flat fallback
    val glassModifier = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        modifier
            .clip(shape)
            .blur(blurRadius)
            .background(GlassOverlay)
            .border(1.dp, StrokeHairline, shape)
    } else {
        modifier
            .clip(shape)
            .background(BgSurfaceElevated.copy(alpha = 0.85f))
            .border(1.dp, StrokeHairline, shape)
    }

    Box(modifier = glassModifier, content = content)
}
