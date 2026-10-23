package com.sblorftz.timer.ui

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import com.sblorftz.timer.R

object Palette {
    val Gold = Color(0xFFD4AF37)
    val GoldLight = Color(0xFFF3D77A)
    val GoldDark = Color(0xFF8A6D1C)
    val Cream = Color(0xFFF4EFE3)
    val Muted = Color(0xFF9AA69F)
    val Muted2 = Color(0xFF7F8B84)
    val Red = Color(0xFFE5484D)
    val Felt = Color(0xFF13261D)
    val FeltDark = Color(0xFF0A120E)
    val Night = Color(0xFF050706)
    val Amber = Color(0xFF2A1D06)
    val AmberDark = Color(0xFF120C03)
    val AmberNight = Color(0xFF060402)
    val ChipPurple = Color(0xFF7B3FB3)
    val ChipBlack = Color(0xFF1B1B1B)

    val ClockBrush = Brush.verticalGradient(listOf(Color.White, Color(0xFFE9E1CC)))
    val ClockAlertBrush = Brush.verticalGradient(listOf(Color(0xFFFF8A80), Red))
    val BarBrush = Brush.horizontalGradient(listOf(GoldDark, Gold, GoldLight))
}

val Display = FontFamily(
    Font(R.font.bigshoulders_600, FontWeight.SemiBold),
    Font(R.font.bigshoulders_800, FontWeight.ExtraBold),
    Font(R.font.bigshoulders_900, FontWeight.Black),
)

val Body = FontFamily(
    Font(R.font.inter_400, FontWeight.Normal),
    Font(R.font.inter_500, FontWeight.Medium),
    Font(R.font.inter_600, FontWeight.SemiBold),
    Font(R.font.inter_700, FontWeight.Bold),
)

/** Testo senza padding del font: i numeri giganti si allineano come nel mockup. */
fun tight(
    family: FontFamily,
    size: TextUnit,
    weight: FontWeight,
    color: Color = Palette.Cream,
    lineHeight: Float = 0.9f,
    letterSpacing: TextUnit = TextUnit.Unspecified,
) = TextStyle(
    fontFamily = family,
    fontSize = size,
    fontWeight = weight,
    color = color,
    letterSpacing = letterSpacing,
    lineHeight = size * lineHeight,
    fontFeatureSettings = "tnum",
    platformStyle = PlatformTextStyle(includeFontPadding = false),
    lineHeightStyle = LineHeightStyle(LineHeightStyle.Alignment.Center, LineHeightStyle.Trim.Both),
)

fun label(size: Int, color: Color = Palette.Muted2, spacing: Float = 0.4f, weight: FontWeight = FontWeight.SemiBold) =
    TextStyle(
        fontFamily = Body,
        fontSize = size.sp,
        fontWeight = weight,
        color = color,
        letterSpacing = (size * spacing).sp,
    )
