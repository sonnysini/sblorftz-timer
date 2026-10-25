package com.sblorftz.timer.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.toSize
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.launch

private const val GLIDE_MS = 150
private val GlideEase = CubicBezierEasing(0.33f, 1f, 0.68f, 1f)

/**
 * Riquadro di focus che scivola da un elemento all'altro in 150 ms, senza molla.
 * Lo disegna l'elemento che ha il focus, partendo spostato dove stava il riquadro e
 * tornando su di sé: così scorre insieme alla lista e non resta mai indietro.
 */
@Stable
class Glide {
    internal var owner: GlideTarget? = null
}

@Composable
fun rememberGlide(): Glide = remember { Glide() }

@Composable
fun rememberGlideTarget(): GlideTarget = remember { GlideTarget() }

class GlideTarget {
    internal var coords: LayoutCoordinates? = null
    internal var focused by mutableStateOf(false)
    /** 0 = riquadro appena partito, 1 = arrivato: per cambiare colore al testo quando ci è sotto. */
    val arrived = Animatable(1f)
    val dx = Animatable(0f)
    val dy = Animatable(0f)
    val dw = Animatable(0f)
    val dh = Animatable(0f)

    /** Dove si vede il riquadro adesso, in coordinate dello schermo. */
    internal fun visualRect(): Rect? {
        val c = coords?.takeIf { it.isAttached } ?: return null
        val p = c.positionInRoot()
        val s = c.size.toSize()
        return Rect(Offset(p.x + dx.value, p.y + dy.value), Size(s.width + dw.value, s.height + dh.value))
    }
}

fun Modifier.glide(
    glide: Glide,
    color: Color = Palette.Gold,
    fill: Float = 0.2f,
    border: Dp = 3.dp,
    radius: Dp = 12.dp,
    target: GlideTarget? = null,
): Modifier = composed {
    val t = target ?: remember { GlideTarget() }
    val scope = rememberCoroutineScope()
    onGloballyPositioned { t.coords = it }
        .onFocusChanged { st ->
            if (st.hasFocus && !t.focused) {
                val from = glide.owner?.takeIf { it !== t }?.visualRect()
                glide.owner = t
                val me = t.coords?.takeIf { it.isAttached }
                // UNDISPATCHED: lo spostamento iniziale è già pronto al primo frame (niente lampo sulla cella).
                scope.launch(start = CoroutineStart.UNDISPATCHED) {
                    if (from == null || me == null) {
                        t.dx.snapTo(0f); t.dy.snapTo(0f); t.dw.snapTo(0f); t.dh.snapTo(0f); t.arrived.snapTo(1f)
                        return@launch
                    }
                    val p = me.positionInRoot()
                    val s = me.size.toSize()
                    t.dx.snapTo(from.left - p.x)
                    t.dy.snapTo(from.top - p.y)
                    t.dw.snapTo(from.width - s.width)
                    t.dh.snapTo(from.height - s.height)
                    t.arrived.snapTo(0f)
                    val spec = tween<Float>(GLIDE_MS, easing = GlideEase)
                    launch { t.dx.animateTo(0f, spec) }
                    launch { t.dy.animateTo(0f, spec) }
                    launch { t.dw.animateTo(0f, spec) }
                    launch { t.dh.animateTo(0f, spec) }
                    launch { t.arrived.animateTo(1f, spec) }
                }
            }
            t.focused = st.hasFocus
        }
        // I valori animati si leggono solo qui: ridisegno, nessuna ricomposizione.
        .drawBehind {
            if (!t.focused) return@drawBehind
            val tl = Offset(t.dx.value, t.dy.value)
            val sz = Size(size.width + t.dw.value, size.height + t.dh.value)
            val cr = CornerRadius(radius.toPx())
            drawRoundRect(color.copy(alpha = fill), tl, sz, cr)
            if (border > 0.dp) {
                val bw = border.toPx()
                drawRoundRect(color, tl + Offset(bw / 2, bw / 2), Size(sz.width - bw, sz.height - bw), cr, style = Stroke(bw))
            }
        }
}
