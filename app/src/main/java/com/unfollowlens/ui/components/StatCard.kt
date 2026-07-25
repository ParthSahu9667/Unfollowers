package com.unfollowlens.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.unfollowlens.ui.theme.AccentPrimary
import com.unfollowlens.ui.theme.TextPrimary
import com.unfollowlens.ui.theme.TextSecondary

/**
 * Dashboard stat card — glass card with a large animated number,
 * label text, and optional delta indicator.
 *
 * @param label Category label (e.g. "Followers")
 * @param value The stat number to display
 * @param delta Change since last snapshot (e.g. +12 or -5), null if no previous data
 * @param accentColor Category color for the value number
 * @param onClick Jump to the corresponding filtered list
 */
@Composable
fun StatCard(
    label: String,
    value: Int,
    modifier: Modifier = Modifier,
    delta: Int? = null,
    accentColor: Color = AccentPrimary,
    onClick: (() -> Unit)? = null
) {
    NeuCard(
        modifier = modifier,
        onClick = onClick,
        accentColor = accentColor,
        cornerRadius = 24.dp
    ) {
        Column(
            verticalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = TextSecondary
            )

            // Animated counter
            AnimatedCounter(
                targetValue = value,
                color = accentColor,
                modifier = Modifier.padding(top = 8.dp)
            )

            // Delta indicator
            if (delta != null && delta != 0) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = 6.dp)
                ) {
                    val deltaText = if (delta > 0) "+$delta" else "$delta"
                    val deltaColor = if (delta > 0) {
                        Color(0xFF3DDC97) // mint for positive
                    } else {
                        Color(0xFFFF6B6B) // coral for negative
                    }
                    Text(
                        text = deltaText,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = deltaColor
                    )
                    Text(
                        text = " since last",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary
                    )
                }
            }
        }
    }
}
