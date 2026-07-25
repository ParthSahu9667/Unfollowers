package com.unfollowlens.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

/**
 * Liquid Obsidian — dark-mode only color scheme.
 * Heavily re-themed from stock Material3 to match the premium
 * frosted-glass, neumorphic design language.
 */
private val LiquidObsidianColorScheme = darkColorScheme(
    primary = AccentPrimary,
    onPrimary = TextPrimary,
    primaryContainer = AccentPrimary.copy(alpha = 0.15f),
    onPrimaryContainer = AccentPrimary,

    secondary = AccentFan,
    onSecondary = TextPrimary,
    secondaryContainer = AccentFan.copy(alpha = 0.15f),
    onSecondaryContainer = AccentFan,

    tertiary = AccentMutual,
    onTertiary = TextPrimary,
    tertiaryContainer = AccentMutual.copy(alpha = 0.15f),
    onTertiaryContainer = AccentMutual,

    error = AccentNotBack,
    onError = TextPrimary,
    errorContainer = AccentNotBack.copy(alpha = 0.15f),
    onErrorContainer = AccentNotBack,

    background = BgBase,
    onBackground = TextPrimary,

    surface = BgSurface,
    onSurface = TextPrimary,
    surfaceVariant = BgSurfaceElevated,
    onSurfaceVariant = TextSecondary,

    outline = StrokeHairline,
    outlineVariant = StrokeHairline,

    inverseSurface = TextPrimary,
    inverseOnSurface = BgBase,
    inversePrimary = AccentPrimary,

    surfaceTint = AccentPrimary
)

@Composable
fun UnfollowLensTheme(content: @Composable () -> Unit) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = BgBase.toArgb()
            window.navigationBarColor = BgBase.toArgb()
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = false
                isAppearanceLightNavigationBars = false
            }
        }
    }

    MaterialTheme(
        colorScheme = LiquidObsidianColorScheme,
        typography = AppTypography,
        shapes = AppShapes,
        content = content
    )
}
