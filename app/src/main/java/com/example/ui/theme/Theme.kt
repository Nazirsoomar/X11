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
    primary = ElectricCyan,
    onPrimary = Color(0xFF00363D),
    primaryContainer = TelecomBlueContainer,
    onPrimaryContainer = Color(0xFFE0F7FA),
    secondary = TelecomBlue,
    onSecondary = Color(0xFF00344D),
    secondaryContainer = Color(0xFF004D73),
    onSecondaryContainer = Color(0xFFCBE6FF),
    tertiary = SuccessGreen,
    onTertiary = Color(0xFF003822),
    tertiaryContainer = SuccessGreenBg,
    onTertiaryContainer = Color(0xFFA7F3D0),
    error = DangerRed,
    errorContainer = DangerRedBg,
    onError = Color.White,
    background = TechDarkBg,
    onBackground = TextPrimary,
    surface = TechDarkSurface,
    onSurface = TextPrimary,
    surfaceVariant = TechDarkSurfaceVariant,
    onSurfaceVariant = TextSecondary,
    outline = BorderSubtle,
    outlineVariant = BorderAccent
)

private val LightColorScheme = lightColorScheme(
    primary = ElectricCyanDark,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE0F7FA),
    onPrimaryContainer = Color(0xFF002026),
    secondary = Color(0xFF0284C7),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFE0F2FE),
    onSecondaryContainer = Color(0xFF0369A1),
    tertiary = Color(0xFF059669),
    onTertiary = Color.White,
    background = Color(0xFFF8FAFC),
    onBackground = Color(0xFF0F172A),
    surface = Color.White,
    onSurface = Color(0xFF0F172A),
    surfaceVariant = Color(0xFFF1F5F9),
    onSurfaceVariant = Color(0xFF475569),
    outline = Color(0xFFCBD5E1),
    outlineVariant = Color(0xFF94A3B8)
)

@Composable
fun UnisocTunerTheme(
    darkTheme: Boolean = true, // Default to sleek tech dark theme for router & terminal operations
    dynamicColor: Boolean = false, // Keep high-contrast engineering colors
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
