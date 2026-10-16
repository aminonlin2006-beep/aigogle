package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection

private val LightColorScheme = lightColorScheme(
    primary = AgriGreen,
    onPrimary = Color.White,
    primaryContainer = AgriGreenContainer,
    onPrimaryContainer = AgriOnGreenContainer,
    secondary = AmberHarvest,
    onSecondary = Color.White,
    secondaryContainer = AmberContainer,
    onSecondaryContainer = AmberOnContainer,
    background = AgriLightBackground,
    onBackground = Color(0xFF191D1A),
    surface = AgriLightSurface,
    onSurface = Color(0xFF191D1A),
    surfaceVariant = AgriLightSurfaceVariant,
    onSurfaceVariant = Color(0xFF424940),
    outline = Color(0xFF72796F),
    error = Color(0xFFBA1A1A),
    onError = Color.White
)

private val DarkColorScheme = darkColorScheme(
    primary = AgriGreenLight,
    onPrimary = Color(0xFF00390F),
    primaryContainer = AgriGreenDark,
    onPrimaryContainer = AgriGreenContainer,
    secondary = AmberHarvestLight,
    onSecondary = Color(0xFF4E1A00),
    secondaryContainer = Color(0xFF6B2600),
    onSecondaryContainer = AmberContainer,
    background = AgriDarkBackground,
    onBackground = Color(0xFFE1E3DF),
    surface = AgriDarkSurface,
    onSurface = Color(0xFFE1E3DF),
    surfaceVariant = AgriDarkSurfaceVariant,
    onSurfaceVariant = Color(0xFFC1C9BE),
    outline = Color(0xFF8B9389),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005)
)

@Composable
fun AgriManagerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    isLargeFont: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val typography = createAgriTypography(isLargeFont)

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = typography,
            content = content
        )
    }
}
