package com.ghost.folio.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = LavenderMist,
    onPrimary = ChampionBlue,
    primaryContainer = SurfaceDark,
    onPrimaryContainer = LavenderMist,
    secondary = MutedDark,
    onSecondary = WhiteConvolvulus,
    background = ChampionBlue,
    onBackground = WhiteConvolvulus,
    surface = SurfaceDark,
    onSurface = WhiteConvolvulus,
    surfaceVariant = SurfaceDark,
    onSurfaceVariant = LavenderMist,
    outline = MutedDark
)

private val LightColorScheme = lightColorScheme(
    primary = RoyalInk,
    onPrimary = WhiteConvolvulus,
    primaryContainer = SurfaceLight,
    onPrimaryContainer = RoyalInk,
    secondary = MutedLight,
    onSecondary = ChampionBlue,
    background = WhiteConvolvulus,
    onBackground = ChampionBlue,
    surface = SurfaceLight,
    onSurface = ChampionBlue,
    surfaceVariant = SurfaceLight,
    onSurfaceVariant = RoyalInk,
    outline = MutedLight
)

@Composable
fun FolioTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = android.graphics.Color.TRANSPARENT
            window.navigationBarColor = android.graphics.Color.TRANSPARENT
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
            WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = Shapes,
        content = content
    )
}
