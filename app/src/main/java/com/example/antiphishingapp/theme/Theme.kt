// ui/theme/Theme.kt
package com.example.antiphishingapp.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

// Dark 색상 세트
private val DarkColorScheme = darkColorScheme(
    primary = Primary700,    // Primary 색상: 좀 더 어두운 색상
    secondary = Grayscale300, // Secondary 색상: 연한 그레이
    tertiary = Grayscale500   // Tertiary 색상: 그레이 중간 색상
)

// Light 색상 세트
private val LightColorScheme = lightColorScheme(
    primary = Primary700,    // Primary 색상: 좀 더 어두운 색상
    secondary = Primary300,  // Secondary 색상: 연한 보라색
    tertiary = Grayscale300   // Tertiary 색상: 그레이 중간 색상
)

@Composable
fun AntiPhishingAppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }
    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography,  // Type.kt에서 Typography만 가져옴
        content = content
    )
}