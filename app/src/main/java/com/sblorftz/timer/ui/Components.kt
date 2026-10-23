package com.sblorftz.timer.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.Border
import androidx.tv.material3.ClickableSurfaceDefaults
import androidx.tv.material3.Surface
import androidx.tv.material3.Text

/** Panno verde (o ambra per il cambio fiches) con luce al centro del tavolo. */
@Composable
fun TableBackground(warm: Boolean = false, content: @Composable BoxScope.() -> Unit) {
    val colors = if (warm) {
        listOf(Palette.Amber, Palette.AmberDark, Palette.AmberNight)
    } else {
        listOf(Palette.Felt, Palette.FeltDark, Palette.Night)
    }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .drawBehind {
                drawRect(colors.last())
                drawRect(
                    Brush.radialGradient(
                        0f to colors[0],
                        0.55f to colors[1],
                        1f to colors[2],
                        center = Offset(size.width / 2, size.height * 0.45f),
                        radius = size.width * 0.62f,
                    ),
                )
            },
        content = content,
    )
}

@Composable
fun Brand(modifier: Modifier = Modifier) {
    Row(modifier, verticalAlignment = Alignment.Bottom) {
        Text("SBLORFTZ", style = tight(Display, 34.sp, FontWeight.ExtraBold, Palette.Gold, letterSpacing = 11.sp))
        Spacer(Modifier.width(18.dp))
        Text(
            "TIMER",
            style = tight(Display, 26.sp, FontWeight.ExtraBold, Palette.Cream.copy(alpha = 0.35f), letterSpacing = 5.sp),
        )
    }
}

/** Tasto del telecomando disegnato come nelle istruzioni in basso. */
@Composable
fun Key(text: String) {
    Box(
        Modifier
            .border(1.dp, Palette.Cream.copy(alpha = 0.25f), RoundedCornerShape(8.dp))
            .padding(horizontal = 12.dp, vertical = 3.dp),
    ) {
        Text(text, style = label(22, Palette.Cream.copy(alpha = 0.5f), 0.02f, FontWeight.Medium))
    }
}

@Composable
fun Hints(vararg items: Pair<String, String>) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        items.forEachIndexed { i, (key, what) ->
            if (i > 0) Spacer(Modifier.width(40.dp))
            Key(key)
            Spacer(Modifier.width(12.dp))
            Text(what, style = label(22, Palette.Cream.copy(alpha = 0.28f), 0.08f, FontWeight.Normal))
        }
    }
}

/** Bottone da telecomando: contorno oro, pieno oro quando ha il focus. */
@Composable
fun GoldButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    danger: Boolean = false,
    width: Int? = 560,
    compact: Boolean = false,
) {
    val accent = if (danger) Palette.Red else Palette.Gold
    Surface(
        onClick = onClick,
        modifier = if (width != null) modifier.width(width.dp) else modifier,
        shape = ClickableSurfaceDefaults.shape(RoundedCornerShape(14.dp)),
        colors = ClickableSurfaceDefaults.colors(
            containerColor = Color.White.copy(alpha = 0.03f),
            contentColor = Palette.Cream,
            focusedContainerColor = accent,
            focusedContentColor = Palette.FeltDark,
            pressedContainerColor = accent,
            pressedContentColor = Palette.FeltDark,
        ),
        border = ClickableSurfaceDefaults.border(
            border = Border(BorderStroke(2.dp, accent.copy(alpha = 0.35f)), shape = RoundedCornerShape(14.dp)),
            focusedBorder = Border(BorderStroke(2.dp, accent), shape = RoundedCornerShape(14.dp)),
        ),
        scale = ClickableSurfaceDefaults.scale(focusedScale = 1.06f),
    ) {
        Column(
            Modifier
                .padding(horizontal = if (compact) 28.dp else 36.dp, vertical = if (compact) 14.dp else 22.dp)
                .align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text.uppercase(),
                style = label(if (compact) 22 else 28, androidx.tv.material3.LocalContentColor.current, 0.18f, FontWeight.Bold),
                maxLines = 1,
                textAlign = TextAlign.Center,
            )
            if (subtitle != null) {
                Spacer(Modifier.height(6.dp))
                Text(
                    subtitle,
                    style = label(22, androidx.tv.material3.LocalContentColor.current.copy(alpha = 0.7f), 0.02f, FontWeight.Medium),
                )
            }
        }
    }
}

data class DialogAction(val text: String, val danger: Boolean = false, val onClick: () -> Unit)

/** Finestra di conferma sopra lo schermo: il primo bottone prende il focus. */
@Composable
fun Overlay(
    title: String,
    subtitle: String? = null,
    actions: List<DialogAction>,
    vertical: Boolean = false,
) {
    val first = remember { FocusRequester() }
    LaunchedEffect(Unit) { runCatching { first.requestFocus() } }
    Box(
        Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.72f)),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            Modifier
                .background(Palette.FeltDark, RoundedCornerShape(24.dp))
                .border(1.dp, Palette.Gold.copy(alpha = 0.3f), RoundedCornerShape(24.dp))
                .padding(horizontal = 72.dp, vertical = 56.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(title, style = tight(Display, 64.sp, FontWeight.ExtraBold, Palette.Cream, lineHeight = 1.1f), textAlign = TextAlign.Center)
            if (subtitle != null) {
                Spacer(Modifier.height(16.dp))
                Text(subtitle, style = label(24, Palette.Muted, 0.02f, FontWeight.Medium), textAlign = TextAlign.Center)
            }
            Spacer(Modifier.height(44.dp))
            if (vertical) {
                Column(verticalArrangement = Arrangement.spacedBy(18.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    actions.forEachIndexed { i, a ->
                        GoldButton(
                            a.text, a.onClick,
                            modifier = if (i == 0) Modifier.focusRequester(first) else Modifier,
                            danger = a.danger,
                            width = 520,
                        )
                    }
                }
            } else {
                Row(horizontalArrangement = Arrangement.spacedBy(28.dp)) {
                    actions.forEachIndexed { i, a ->
                        GoldButton(
                            a.text, a.onClick,
                            modifier = if (i == 0) Modifier.focusRequester(first) else Modifier,
                            danger = a.danger,
                            width = 300,
                        )
                    }
                }
            }
        }
    }
}
