package com.unfollowlens.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.unfollowlens.ui.theme.BgSurface
import com.unfollowlens.ui.theme.NeuDarkShadow
import com.unfollowlens.ui.theme.NeuLightShadow
import com.unfollowlens.ui.theme.StrokeHairline
import kotlinx.coroutines.launch

/**
 * Neumorphic card — the core surface component of Liquid Obsidian.
 *
 * Features dual-shadow elevation (light top-left, dark bottom-right)
 * to simulate extrusion from the same dark slab material.
 * Includes spring-physics press animation and haptic feedback.
 *
 * @param accentColor Optional colored left-edge accent strip
 */
@Composable
fun NeuCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    accentColor: Color? = null,
    cornerRadius: Dp = 24.dp,
    backgroundColor: Color = BgSurface,
    content: @Composable BoxScope.() -> Unit
) {
    val scale = remember { Animatable(1f) }
    val scope = rememberCoroutineScope()
    val haptic = LocalHapticFeedback.current
    val shape = RoundedCornerShape(cornerRadius)

    Box(
        modifier = modifier
            .graphicsLayer {
                scaleX = scale.value
                scaleY = scale.value
            }
            .drawBehind {
                // Light shadow — top-left highlight
                drawRoundRect(
                    color = NeuLightShadow,
                    topLeft = Offset(-4.dp.toPx(), -4.dp.toPx()),
                    size = size.copy(
                        width = size.width + 2.dp.toPx(),
                        height = size.height + 2.dp.toPx()
                    ),
                    cornerRadius = CornerRadius(cornerRadius.toPx())
                )
                // Dark shadow — bottom-right depth
                drawRoundRect(
                    color = NeuDarkShadow,
                    topLeft = Offset(4.dp.toPx(), 4.dp.toPx()),
                    size = size.copy(
                        width = size.width + 2.dp.toPx(),
                        height = size.height + 2.dp.toPx()
                    ),
                    cornerRadius = CornerRadius(cornerRadius.toPx())
                )
            }
            .clip(shape)
            .background(backgroundColor)
            .border(1.dp, StrokeHairline, shape)
            .then(
                if (accentColor != null) {
                    Modifier.drawBehind {
                        drawRoundRect(
                            color = accentColor,
                            topLeft = Offset(0f, 8.dp.toPx()),
                            size = androidx.compose.ui.geometry.Size(
                                3.dp.toPx(),
                                size.height - 16.dp.toPx()
                            ),
                            cornerRadius = CornerRadius(2.dp.toPx())
                        )
                    }
                } else Modifier
            )
            .then(
                if (onClick != null) {
                    Modifier.pointerInput(Unit) {
                        detectTapGestures(
                            onPress = {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                scope.launch {
                                    scale.animateTo(
                                        0.96f,
                                        spring(
                                            dampingRatio = 0.5f,
                                            stiffness = Spring.StiffnessMedium
                                        )
                                    )
                                }
                                tryAwaitRelease()
                                scope.launch {
                                    scale.animateTo(
                                        1f,
                                        spring(
                                            dampingRatio = 0.5f,
                                            stiffness = Spring.StiffnessMedium
                                        )
                                    )
                                }
                            },
                            onTap = { onClick() }
                        )
                    }
                } else Modifier
            )
            .padding(16.dp),
        content = content
    )
}
