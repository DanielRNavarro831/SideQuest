package com.dnavarro.sidequest.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily

object Parchment {
    val Paper = Color(0xFFF3E4C1)
    val PaperShade = Color(0xFFE6D0A0)
    val PaperEdge = Color(0xFFC9A66B)
    val Ink = Color(0xFF3B2A1A)
    val InkFaded = Color(0xFF7A6248)
    val Wood = Color(0xFF7A4A24)
    val WoodLight = Color(0xFFB07A45)
    val WoodDark = Color(0xFF4A2C14)
    val Backdrop = Color(0xFF2B1D12)
    val Crimson = Color(0xFF9B1C1C)
    val Gold = Color(0xFFB8860B)
    val XpGreen = Color(0xFF4F7A28)
}

private val ScrollColors = lightColorScheme(
    primary = Parchment.Wood,
    onPrimary = Parchment.Paper,
    secondary = Parchment.Gold,
    onSecondary = Parchment.Ink,
    background = Parchment.Paper,
    onBackground = Parchment.Ink,
    surface = Parchment.Paper,
    onSurface = Parchment.Ink,
    surfaceVariant = Parchment.PaperShade,
    onSurfaceVariant = Parchment.InkFaded,
    surfaceContainerHigh = Parchment.Paper,
    outline = Parchment.PaperEdge,
    error = Parchment.Crimson,
    onError = Parchment.Paper,
)

private fun Typography.withFamily(family: FontFamily) = Typography(
    displayLarge = displayLarge.copy(fontFamily = family),
    displayMedium = displayMedium.copy(fontFamily = family),
    displaySmall = displaySmall.copy(fontFamily = family),
    headlineLarge = headlineLarge.copy(fontFamily = family),
    headlineMedium = headlineMedium.copy(fontFamily = family),
    headlineSmall = headlineSmall.copy(fontFamily = family),
    titleLarge = titleLarge.copy(fontFamily = family),
    titleMedium = titleMedium.copy(fontFamily = family),
    titleSmall = titleSmall.copy(fontFamily = family),
    bodyLarge = bodyLarge.copy(fontFamily = family),
    bodyMedium = bodyMedium.copy(fontFamily = family),
    bodySmall = bodySmall.copy(fontFamily = family),
    labelLarge = labelLarge.copy(fontFamily = family),
    labelMedium = labelMedium.copy(fontFamily = family),
    labelSmall = labelSmall.copy(fontFamily = family),
)

private val ScrollTypography = Typography().withFamily(FontFamily.Serif)

@Composable
fun SideQuestTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = ScrollColors, typography = ScrollTypography, content = content)
}
