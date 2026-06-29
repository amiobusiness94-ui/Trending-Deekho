package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val PremiumDarkColorScheme = darkColorScheme(
    primary = RoyalPurple,
    onPrimary = Color.White,
    secondary = TechBlue,
    onSecondary = Color.White,
    tertiary = GrowthGreen,
    onTertiary = Color.White,
    background = SlateDark,
    onBackground = TextPrimary,
    surface = SlateCard,
    onSurface = TextPrimary,
    outline = SlateMuted,
    surfaceVariant = SlateCard,
    onSurfaceVariant = TextSecondary
)

@Composable
fun MyApplicationTheme(
    content: @Composable () -> Unit
) {
    // Force Premium Dark OTT-style theme for all users to match the 'Premium Dark' branding guideline
    MaterialTheme(
        colorScheme = PremiumDarkColorScheme,
        typography = Typography,
        content = content
    )
}
