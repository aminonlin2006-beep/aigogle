package com.example.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

fun createAgriTypography(isLarge: Boolean = false): Typography {
    val scale = if (isLarge) 1.2f else 1.0f

    return Typography(
        displayLarge = TextStyle(
            fontFamily = FontFamily.Default,
            fontWeight = FontWeight.Bold,
            fontSize = (32 * scale).sp,
            lineHeight = (40 * scale).sp
        ),
        headlineLarge = TextStyle(
            fontFamily = FontFamily.Default,
            fontWeight = FontWeight.Bold,
            fontSize = (28 * scale).sp,
            lineHeight = (36 * scale).sp
        ),
        headlineMedium = TextStyle(
            fontFamily = FontFamily.Default,
            fontWeight = FontWeight.SemiBold,
            fontSize = (24 * scale).sp,
            lineHeight = (32 * scale).sp
        ),
        titleLarge = TextStyle(
            fontFamily = FontFamily.Default,
            fontWeight = FontWeight.Bold,
            fontSize = (20 * scale).sp,
            lineHeight = (28 * scale).sp
        ),
        titleMedium = TextStyle(
            fontFamily = FontFamily.Default,
            fontWeight = FontWeight.SemiBold,
            fontSize = (16 * scale).sp,
            lineHeight = (24 * scale).sp
        ),
        bodyLarge = TextStyle(
            fontFamily = FontFamily.Default,
            fontWeight = FontWeight.Normal,
            fontSize = (16 * scale).sp,
            lineHeight = (24 * scale).sp
        ),
        bodyMedium = TextStyle(
            fontFamily = FontFamily.Default,
            fontWeight = FontWeight.Normal,
            fontSize = (14 * scale).sp,
            lineHeight = (20 * scale).sp
        ),
        labelLarge = TextStyle(
            fontFamily = FontFamily.Default,
            fontWeight = FontWeight.Medium,
            fontSize = (14 * scale).sp,
            lineHeight = (20 * scale).sp
        ),
        labelMedium = TextStyle(
            fontFamily = FontFamily.Default,
            fontWeight = FontWeight.Medium,
            fontSize = (12 * scale).sp,
            lineHeight = (16 * scale).sp
        )
    )
}

val Typography = createAgriTypography(false)
