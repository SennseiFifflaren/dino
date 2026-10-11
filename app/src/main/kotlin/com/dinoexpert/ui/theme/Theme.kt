package com.dinoexpert.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

val Forest = Color(0xFF14634D)
val Ink = Color(0xFF183D35)
val Sand = Color(0xFFFFF8E8)
val Turquoise = Color(0xFF006B70)
val CorrectGreen = Color(0xFF185C36)
val RetryRust = Color(0xFF854016)

private val colors = lightColorScheme(
    primary = Forest, onPrimary = Color.White,
    primaryContainer = Color(0xFFD7EFDC), onPrimaryContainer = Ink,
    secondary = Turquoise, onSecondary = Color.White,
    secondaryContainer = Color(0xFFD5F0EC), onSecondaryContainer = Ink,
    tertiary = Color(0xFF855021), tertiaryContainer = Color(0xFFFFE4AE),
    background = Sand, onBackground = Ink,
    surface = Color(0xFFFFFCF4), onSurface = Ink,
    surfaceVariant = Color(0xFFE9EEDF), onSurfaceVariant = Color(0xFF40584D),
    outline = Color(0xFF627B6F), error = RetryRust,
)
private val typography = Typography(
    displaySmall = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.ExtraBold, fontSize = 36.sp, lineHeight = 42.sp),
    headlineMedium = TextStyle(fontWeight = FontWeight.Bold, fontSize = 28.sp, lineHeight = 36.sp),
    titleLarge = TextStyle(fontWeight = FontWeight.Bold, fontSize = 22.sp, lineHeight = 30.sp),
    bodyLarge = TextStyle(fontSize = 18.sp, lineHeight = 27.sp),
    bodyMedium = TextStyle(fontSize = 16.sp, lineHeight = 24.sp),
    labelLarge = TextStyle(fontWeight = FontWeight.Bold, fontSize = 16.sp, lineHeight = 22.sp),
)

@Composable
fun DinoTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = colors,
        typography = typography,
        shapes = Shapes(medium = RoundedCornerShape(20.dp), large = RoundedCornerShape(28.dp)),
        content = content,
    )
}
