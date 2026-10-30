package com.viraj022.queens.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val QueensColorScheme = lightColorScheme(
    primary = QueenGold,
    onPrimary = Color.White,
    secondary = TextSecondary,
    onSecondary = Color.White,
    background = CreamBackground,
    onBackground = TextPrimary,
    surface = Color.White,
    onSurface = TextPrimary,
    error = ConflictCoral,
    onError = TextPrimary
)

@Composable
fun QueensTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = QueensColorScheme,
        typography = QueensTypography,
        content = content
    )
}
