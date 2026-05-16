package com.calc.glass.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkScheme = darkColorScheme(
    primary = Color(0xFFE8E8FF),
    onPrimary = Color.White,
    background = Color(0xFF0E0B1A),
    onBackground = Color.White,
    surface = Color(0xFF15102A),
    onSurface = Color.White
)

private val LightScheme = lightColorScheme(
    primary = Color(0xFF26214B),
    onPrimary = Color.White,
    background = Color(0xFFEFEAFE),
    onBackground = Color(0xFF1A1233),
    surface = Color(0xFFF7F4FF),
    onSurface = Color(0xFF1A1233)
)

@Composable
fun GlassCalculatorTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkScheme else LightScheme,
        content = content
    )
}
