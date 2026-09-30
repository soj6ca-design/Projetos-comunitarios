package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = BellaPrimaryDark,
    onPrimary = Color(0xFF530937),
    primaryContainer = Color(0xFF6B1D4B),
    onPrimaryContainer = Color(0xFFFFD8E6),
    secondary = BellaSecondaryDark,
    onSecondary = Color(0xFF4A1930),
    secondaryContainer = Color(0xFF632F47),
    onSecondaryContainer = Color(0xFFFFD9E5),
    tertiary = BellaGoldLight,
    onTertiary = Color(0xFF382900),
    tertiaryContainer = Color(0xFF553F00),
    onTertiaryContainer = Color(0xFFFFF0C2),
    background = BellaDarkBackground,
    onBackground = BellaDarkOnSurface,
    surface = BellaDarkSurface,
    onSurface = BellaDarkOnSurface,
    surfaceVariant = BellaDarkSurfaceVariant,
    onSurfaceVariant = BellaDarkOnSurface,
    outline = BellaDarkOutline
)

private val LightColorScheme = lightColorScheme(
    primary = BellaPrimary,
    onPrimary = BellaOnPrimary,
    primaryContainer = BellaPrimaryContainer,
    onPrimaryContainer = BellaOnPrimaryContainer,
    secondary = BellaSecondary,
    onSecondary = Color.White,
    secondaryContainer = BellaSecondaryContainer,
    onSecondaryContainer = BellaOnSecondaryContainer,
    tertiary = BellaGold,
    onTertiary = Color.White,
    tertiaryContainer = BellaGoldContainer,
    onTertiaryContainer = BellaOnGoldContainer,
    background = BellaBackground,
    onBackground = BellaOnSurface,
    surface = BellaSurface,
    onSurface = BellaOnSurface,
    surfaceVariant = BellaSurfaceVariant,
    onSurfaceVariant = BellaOnSurface,
    outline = BellaOutline
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Use our signature Studio Bella colors by default
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
