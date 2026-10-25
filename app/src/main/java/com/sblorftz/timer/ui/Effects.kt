package com.sblorftz.timer.ui

import android.os.SystemClock
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.GraphicsLayerScope
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.unit.Dp
import kotlinx.coroutines.delay
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.TextStyle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameMillis
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.Text
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin

// Animazioni portate dai mock ~/tmp/sblorftz/mock/anim/*.html (stessi tempi ed easing).

/** Fase 0..1 di [p] tra [from] e [to], con easing. */
fun phase(p: Float, from: Float, to: Float, easing: Easing = LinearEasing) =
    easing.transform(((p - from) / (to - from)).coerceIn(0f, 1f))

val EaseOutCubic = Easing { x -> 1f - (1f - x).pow(3) }

fun backEasing(c1: Float = 1.70158f) = Easing { x ->
    val c3 = c1 + 1f
    1f + c3 * (x - 1f).pow(3) + c1 * (x - 1f).pow(2)
}

val BounceEasing = Easing { t ->
    val n = 7.5625f
    val d = 2.75f
    var x = t
    when {
        x < 1f / d -> n * x * x
        x < 2f / d -> { x -= 1.5f / d; n * x * x + 0.75f }
        x < 2.5f / d -> { x -= 2.25f / d; n * x * x + 0.9375f }
        else -> { x -= 2.625f / d; n * x * x + 0.984375f }
    }
}

/** Avanzamento 0..1 di un'animazione partita all'istante [startedAt] (elapsedRealtime); 1 se già finita o mai partita. */
@Composable
fun rememberSince(startedAt: Long, durationMs: Int): State<Float> {
    fun now() = if (startedAt == 0L) 1f else ((SystemClock.elapsedRealtime() - startedAt) / durationMs.toFloat()).coerceIn(0f, 1f)
    val p = remember(startedAt) { mutableFloatStateOf(now()) }
    LaunchedEffect(startedAt) {
        while (p.floatValue < 1f) {
            withFrameMillis { }
            p.floatValue = now()
        }
    }
    return p
}

/** Millisecondi dalla prima composizione, aggiornati a ogni frame fino a [untilMs]. */
@Composable
fun rememberFrameTime(untilMs: Long = Long.MAX_VALUE): State<Long> = produceState(0L) {
    val start = withFrameMillis { it }
    while (value < untilMs) withFrameMillis { value = it - start }
}

// ---------------------------------------------------------------- cambio livello

/** Flash bianco + scia dorata obliqua che attraversa lo schermo (p = 0..1 su 1,2 s). */
@Composable
fun LevelChangeFlash(progress: () -> Float) {
    Canvas(Modifier.fillMaxSize()) {
        val p = progress()
        if (p >= 1f) return@Canvas
        val flash = 0.45f * phase(p, 0f, 0.05f) * (1f - phase(p, 0.05f, 0.5f, EaseOutCubic))
        if (flash > 0f) drawRect(Color.White.copy(alpha = flash))
        val sweepAlpha = phase(p, 0f, 0.12f) * (1f - phase(p, 0.62f, 0.75f))
        if (sweepAlpha <= 0f) return@Canvas
        val x = -700f + 2800f * phase(p, 0f, 0.75f, CubicBezierEasing(0.65f, 0f, 0.35f, 1f))
        val w = 520f
        rotate(18f, pivot = Offset(x + w / 2, size.height / 2)) {
            drawRect(
                Brush.horizontalGradient(
                    0f to Color.Transparent,
                    0.3f to Palette.GoldLight.copy(alpha = 0.10f),
                    0.5f to Color(0xFFFFF0BE).copy(alpha = 0.42f),
                    0.7f to Palette.GoldLight.copy(alpha = 0.10f),
                    1f to Color.Transparent,
                    startX = x,
                    endX = x + w,
                ),
                topLeft = Offset(x, -400f),
                size = Size(w, size.height + 800f),
                alpha = sweepAlpha,
            )
        }
    }
}

private val SlotEasing = CubicBezierEasing(0.3f, 1.25f, 0.55f, 1f)

/** Taglia solo sopra e sotto: in larghezza i numeri del rullo possono uscire dal box. */
private val RowClip = GenericShape { size, _ -> addRect(Rect(-2000f, 0f, size.width + 2000f, size.height)) }

/**
 * Numero che gira come un rullo di slot da [old] a [new] passando dai valori [fake].
 * [p] = 0..1; con [old] null o p = 1 resta fermo su [new]. La larghezza è sempre quella di [new].
 */
@Composable
fun SlotNumber(old: String?, fake: List<String>, new: String, p: () -> Float, style: TextStyle, alignEnd: Boolean) {
    val rowH = 150
    val rolling = old != null
    Box(
        Modifier
            .height(rowH.dp)
            .graphicsLayer { clip = rolling; shape = RowClip },
        contentAlignment = Alignment.Center,
    ) {
        Text(new, style = style, modifier = Modifier.alpha(if (rolling) 0f else 1f))
        if (rolling) {
            val values = listOf(old!!) + fake + new
            val steps = values.size - 1
            Box(
                Modifier
                    .matchParentSize()
                    .wrapContentSize(align = if (alignEnd) Alignment.TopEnd else Alignment.TopStart, unbounded = true),
            ) {
                Column(
                    Modifier.graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen; translationY = -SlotEasing.transform(p()) * steps * rowH },
                    horizontalAlignment = if (alignEnd) Alignment.End else Alignment.Start,
                ) {
                    values.forEach { v ->
                        Box(Modifier.height(rowH.dp), contentAlignment = Alignment.Center) { Text(v, style = style) }
                    }
                }
            }
        }
    }
}

// ---------------------------------------------------------------- ultimi 10 secondi

/** Bordo dello schermo che si arrossa a tempo col battito. */
@Composable
fun AlertVignette(alpha: () -> Float) {
    Canvas(Modifier.fillMaxSize().graphicsLayer { this.alpha = alpha() }) {
        drawRect(
            Brush.radialGradient(
                0.45f to Color.Transparent,
                1f to Color(0xFFC81E14).copy(alpha = 0.55f),
                center = Offset(size.width / 2, size.height * 0.45f),
                radius = size.width * 0.6f,
            ),
        )
    }
}

fun alertClockBrush(red: Float) = Brush.verticalGradient(
    listOf(
        lerp(Color.White, Color(0xFFFF7864), red),
        lerp(Color(0xFFE9E1CC), Color(0xFFE6281E), red),
    ),
)

// ---------------------------------------------------------------- fine torneo

private class RainChip(val x: Float, val size: Float, val speed: Float, val offset: Float, val spin: Float, val color: Color, val delay: Float)

private val rainChips: List<RainChip> = run {
    var seed = 7L
    fun rnd(): Float { seed = (seed * 16807) % 2147483647; return seed / 2147483647f }
    val colors = listOf(Palette.ChipBlack, Palette.ChipPurple, Color(0xFFB3261E), Color(0xFF1F7A3F), Color(0xFF1F4FA8))
    List(38) { i ->
        val s = 50f + rnd() * 60f
        RainChip(rnd() * 1880f, s, 260f + s * 4f, rnd() * 1300f, (rnd() - 0.5f) * 400f, colors[i % 5], rnd() * 900f)
    }
}

private fun DrawScope.chip(center: Offset, radius: Float, color: Color, angle: Float, alpha: Float) {
    drawCircle(Color.Black.copy(alpha = 0.5f * alpha), radius, center + Offset(0f, 5f))
    drawCircle(color, radius, center, alpha = alpha)
    val stroke = radius * 0.15f
    rotate(angle, center) {
        drawCircle(
            Color.White.copy(alpha = 0.85f * alpha), radius - stroke / 2, center,
            style = Stroke(stroke, pathEffect = PathEffect.dashPathEffect(floatArrayOf(radius * 0.37f, radius * 0.27f))),
        )
    }
}

/** Schermata di fine torneo: pioggia di fiche, trofeo che rimbalza, FINE e durata. */
@Composable
fun FinishOverlay(finishedAt: Long, subtitle: String) {
    // il tempo si legge solo in disegno e graphicsLayer: niente ricomposizione a ogni frame
    val time = rememberFrameTime()
    val start = remember(finishedAt) { if (finishedAt == 0L) 0L else SystemClock.elapsedRealtime() - finishedAt }
    fun now() = (time.value + start).toFloat()
    Box(Modifier.fillMaxSize()) {
        // sfondo e piatto scuro: statici, disegnati una volta
        Canvas(Modifier.fillMaxSize()) {
            drawRect(
                Brush.radialGradient(
                    0f to Palette.Amber, 0.55f to Palette.AmberDark, 1f to Palette.AmberNight,
                    center = Offset(size.width / 2, size.height * 0.45f), radius = size.width * 0.62f,
                ),
            )
        }
        // ogni fiche è un layer offscreen: il tratteggio si rasterizza una volta, poi si sposta e ruota
        for (c in rainChips) {
            Canvas(
                Modifier
                    .size((c.size + 8f).dp)
                    .graphicsLayer {
                        compositingStrategy = CompositingStrategy.Offscreen
                        val t = now()
                        alpha = if (t < c.delay) 0f else 0.55f + 0.45f * c.size / 110f
                        translationX = c.x + 30f * sin(t / 700f + c.offset)
                        translationY = -130f + ((c.offset + t * c.speed / 1000f) % 1300f)
                        rotationZ = c.spin * t / 1000f
                    },
            ) {
                chip(Offset(c.size / 2 + 4f, c.size / 2), c.size / 2, c.color, 0f, 1f)
            }
        }
        Canvas(Modifier.fillMaxSize()) {
            drawOval(
                Brush.radialGradient(
                    0.3f to Color(0xEB0A0702), 0.7f to Color.Transparent,
                    center = Offset(960f, 440f), radius = 500f,
                ),
                topLeft = Offset(460f, 120f), size = Size(1000f, 640f),
            )
        }
        // alone dorato che respira dietro il trofeo
        Canvas(
            Modifier
                .padding(start = 660.dp, top = 120.dp)
                .size(600.dp)
                .graphicsLayer {
                    compositingStrategy = CompositingStrategy.Offscreen
                    val t = now()
                    alpha = phase(t, 500f, 1000f) * (0.75f + 0.25f * sin(t / 400f))
                },
        ) {
            drawCircle(Brush.radialGradient(0f to Palette.GoldLight.copy(alpha = 0.35f), 0.65f to Color.Transparent))
        }
        Column(
            Modifier.fillMaxWidth().padding(top = 170.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Trophy(
                Modifier
                    .size(300.dp, 315.dp)
                    .graphicsLayer {
                        compositingStrategy = CompositingStrategy.Offscreen
                        val t = now()
                        val pop = phase(t, 200f, 1000f, backEasing(2f))
                        val s = 0.2f + 0.8f * pop
                        scaleX = s; scaleY = s
                        rotationZ = -12f * (1f - pop)
                        alpha = phase(t, 200f, 450f)
                    },
            )
            Spacer(Modifier.height(10.dp))
            Text(
                "FINE",
                style = tight(Display, 150.sp, FontWeight.Black, Palette.Gold, letterSpacing = 27.sp),
                modifier = Modifier.graphicsLayer {
                    compositingStrategy = CompositingStrategy.Offscreen
                    val a = phase(now(), 800f, 1300f, EaseOutCubic)
                    alpha = a
                    translationY = 40f * (1f - a)
                    scaleX = 1.6f - 0.6f * a
                },
            )
            Spacer(Modifier.height(14.dp))
            Text(
                subtitle,
                style = label(30, Color(0xFFCFC6B0), 0.12f, FontWeight.Medium),
                modifier = Modifier.graphicsLayer {
                    compositingStrategy = CompositingStrategy.Offscreen
                    val b = phase(now(), 1200f, 1700f, EaseOutCubic)
                    alpha = b
                    translationY = 30f * (1f - b)
                },
            )
        }
    }
}

@Composable
private fun Trophy(modifier: Modifier) {
    Canvas(modifier) {
        val k = size.width / 200f
        scale(k, k, pivot = Offset.Zero) {
            val gold = Brush.linearGradient(
                0f to Color(0xFFFFF0B0), 0.45f to Color(0xFFE2BC48), 1f to Color(0xFF8A6A14),
                start = Offset.Zero, end = Offset(200f, 210f),
            )
            val handleL = Path().apply { moveTo(52f, 38f); lineTo(24f, 38f); lineTo(24f, 58f); cubicTo(24f, 80f, 40f, 92f, 62f, 92f) }
            val handleR = Path().apply { moveTo(148f, 38f); lineTo(176f, 38f); lineTo(176f, 58f); cubicTo(176f, 80f, 160f, 92f, 138f, 92f) }
            drawPath(handleL, gold, style = Stroke(11f, cap = StrokeCap.Butt))
            drawPath(handleR, gold, style = Stroke(11f, cap = StrokeCap.Butt))
            val bowl = Path().apply {
                moveTo(45f, 22f); lineTo(155f, 22f); lineTo(155f, 70f)
                arcTo(Rect(45f, 15f, 155f, 125f), 0f, 180f, false)
                close()
            }
            drawPath(bowl, gold)
            drawRect(gold, Offset(89f, 122f), Size(22f, 36f))
            drawRoundRect(gold, Offset(62f, 156f), Size(76f, 16f), androidx.compose.ui.geometry.CornerRadius(4f))
            drawRoundRect(gold, Offset(46f, 172f), Size(108f, 24f), androidx.compose.ui.geometry.CornerRadius(6f))
            translate(100f, 70f) {
                val star = Path()
                for (i in 0 until 10) {
                    val r = if (i % 2 == 0) 20f else 8.5f
                    val ang = -PI / 2 + i * PI / 5
                    val px = (r * cos(ang)).toFloat()
                    val py = (r * sin(ang)).toFloat()
                    if (i == 0) star.moveTo(px, py) else star.lineTo(px, py)
                }
                star.close()
                drawPath(star, Color(0xFF7A5A0E))
            }
        }
    }
}

// ---------------------------------------------------------------- home

/** L'intro della home si vede una volta per avvio dell'app. */
object HomeIntro {
    var played = false
}

private val suits = listOf("♠" to Palette.Cream, "♥" to Color(0xFFD9443A), "♣" to Palette.Cream, "♦" to Color(0xFFD9443A))
private val fan = listOf(Triple(-34f, -330f, 60f), Triple(-12f, -120f, 10f), Triple(12f, 120f, 10f), Triple(34f, 330f, 60f))

private val CardShape = RoundedCornerShape(16.dp)

/** Quattro carte che salgono impilate e si aprono a ventaglio dietro al logo (t in ms). */
@Composable
fun FanCards(time: () -> Float, modifier: Modifier = Modifier) {
    Box(modifier, contentAlignment = Alignment.Center) {
        suits.forEachIndexed { i, (suit, color) ->
            val (r, x, y) = fan[i]
            Box(
                Modifier
                    .size(200.dp, 290.dp)
                    .graphicsLayer {
                        compositingStrategy = CompositingStrategy.Offscreen
                        val t = time()
                        val rise = phase(t, 150f + i * 60, 650f + i * 60, EaseOutCubic)
                        val open = phase(t, 800f + i * 70, 1500f + i * 70, backEasing(1.3f))
                        transformOrigin = androidx.compose.ui.graphics.TransformOrigin(0.5f, 1.2f)
                        translationX = x * open
                        translationY = y * open + 260f * (1f - rise)
                        rotationZ = r * open
                        alpha = rise * (1f - 0.72f * phase(t, 1700f, 2300f))
                    }
                    .clip(CardShape)
                    .background(Brush.linearGradient(listOf(Color(0xFF1C3328), Color(0xFF0C1813))))
                    .border(3.dp, Palette.Gold.copy(alpha = 0.55f), CardShape),
                contentAlignment = Alignment.Center,
            ) {
                Text(suit, style = label(120, color, 0f, FontWeight.Normal))
                Box(Modifier.fillMaxSize().padding(start = 16.dp, top = 12.dp), contentAlignment = Alignment.TopStart) {
                    Text(suit, style = label(34, color, 0f, FontWeight.Normal))
                }
                Box(
                    Modifier.fillMaxSize().padding(end = 16.dp, bottom = 12.dp).graphicsLayer { rotationZ = 180f },
                    contentAlignment = Alignment.TopStart,
                ) {
                    Text(suit, style = label(34, color, 0f, FontWeight.Normal))
                }
            }
        }
    }
}

// ---------------------------------------------------------------- badge V2 (intro home)

private const val V2_POP = 3200f
private const val V2_SLAM = 3800f
private const val V2_HIT = V2_SLAM + 240f
private const val V_W = 46f
private const val TWO_W = 40f
private const val V2_H = 58f

/** Dove sta il badge: lo legge il Canvas degli effetti per centrare lampo, anello e scintille sul 2. */
class V2Anchor {
    var coords: LayoutCoordinates? = null
    val twoCenter get() = Offset(V_W + TWO_W / 2, V2_H / 2)
}

/** Tremolio di logo e carte quando il 2 si schianta. */
fun v2Shake(t: Float): Offset {
    if (t < V2_HIT || t > V2_HIT + 340f) return Offset.Zero
    val k = (1f - phase(t, V2_HIT, V2_HIT + 340f)) * 18f
    return Offset(k * sin(t / 9f), k * cos(t / 7f))
}

/** Badge oro sotto TIMER, dentro il cronometro: prima la V, poi il 2 arriva enorme e si schianta dentro. */
@Composable
fun V2Badge(time: () -> Float, anchor: V2Anchor, modifier: Modifier = Modifier) {
    val shape = 10f
    Box(modifier.onGloballyPositioned { anchor.coords = it }) {
        Row(
            Modifier
                .graphicsLayer {
                    val t = time()
                    val pop = phase(t, V2_POP, V2_POP + 400f, backEasing(2.2f))
                    val land = phase(t, V2_HIT, V2_HIT + 460f, backEasing(3f))
                    val bump = if (t >= V2_HIT) 1f + 0.25f * sin(land * PI.toFloat()) * (1f - land) else 1f
                    alpha = phase(t, V2_POP, V2_POP + 120f)
                    scaleX = (0.4f + 0.6f * pop) * bump
                    scaleY = scaleX
                }
                .drawBehind {
                    val t = time()
                    val w = if (t >= V2_HIT) size.width else V_W
                    val r = androidx.compose.ui.geometry.CornerRadius(shape, shape)
                    drawRoundRect(
                        Brush.linearGradient(listOf(Color(0xFFF7DC7A), Palette.Gold, Color(0xFFA8841F)), end = Offset(w, size.height)),
                        size = Size(w, size.height),
                        cornerRadius = r,
                    )
                    val s = phase(t, V2_HIT + 660f, V2_HIT + 1160f, CubicBezierEasing(0.65f, 0f, 0.35f, 1f))
                    if (s > 0f && s < 1f) {
                        val x = -60f + (size.width + 60f) * s
                        clipPath(Path().apply { addRoundRect(androidx.compose.ui.geometry.RoundRect(Rect(Offset.Zero, size), r)) }) {
                            rotate(20f, pivot = Offset(x + 25f, size.height / 2)) {
                                drawRect(
                                    Brush.horizontalGradient(
                                        0f to Color.Transparent, 0.5f to Color.White.copy(alpha = 0.85f), 1f to Color.Transparent,
                                        startX = x, endX = x + 50f,
                                    ),
                                    topLeft = Offset(x, -20f),
                                    size = Size(50f, size.height + 40f),
                                )
                            }
                        }
                    }
                },
            verticalAlignment = Alignment.CenterVertically,
        ) {
            val glyph = tight(Display, 50.sp, FontWeight.Black, Palette.FeltDark, lineHeight = 1f)
            Box(Modifier.size(V_W.dp, V2_H.dp).padding(start = 12.dp, end = 1.dp), contentAlignment = Alignment.CenterEnd) { Text("V", style = glyph) }
            Box(Modifier.size(TWO_W.dp, V2_H.dp).padding(start = 1.dp, end = 10.dp), contentAlignment = Alignment.CenterStart) {
                // In volo: 2 oro chiaro gigante e ruotato; all'impatto diventa scuro sull'oro.
                Text(
                    "2",
                    style = glyph.copy(color = Palette.GoldLight),
                    modifier = Modifier.graphicsLayer {
                        val t = time()
                        val v = phase(t, V2_SLAM, V2_HIT) { it * it }
                        alpha = if (t >= V2_HIT) 0f else phase(t, V2_SLAM, V2_SLAM + 80f)
                        scaleX = 9f - 8f * v
                        scaleY = scaleX
                        rotationZ = -50f * (1f - v)
                    },
                )
                Text("2", style = glyph, modifier = Modifier.graphicsLayer { alpha = if (time() >= V2_HIT) 1f else 0f })
            }
        }
    }
}

private val sparks = List(22) { i -> (i / 22f * 2f * PI.toFloat() + (i % 3) * 0.2f) to (120f + (i * 53) % 200) }

/** Lampo, onda ad anello e scintille centrati sul 2 al momento dell'impatto. */
@Composable
fun V2Impact(time: () -> Float, anchor: V2Anchor) {
    val self = remember { V2Anchor() }
    Canvas(Modifier.fillMaxSize().onGloballyPositioned { self.coords = it }) {
        val t = time()
        if (t < V2_HIT || t > V2_HIT + 900f) return@Canvas
        val a = anchor.coords ?: return@Canvas
        val me = self.coords ?: return@Canvas
        val c = me.localPositionOf(a, anchor.twoCenter)
        val hit = phase(t, V2_HIT, V2_HIT + 660f)
        drawRect(
            Brush.radialGradient(
                0f to Color(0xFFFFE278).copy(alpha = 0.9f),
                0.3f to Palette.Gold.copy(alpha = 0.25f),
                0.65f to Color.Transparent,
                center = c,
                radius = 1300f,
            ),
            alpha = (1f - hit) * 0.9f,
        )
        val r = 20f + 900f * EaseOutCubic.transform(hit)
        drawCircle(Color(0xFFF7DC7A), r, c, alpha = 1f - hit, style = Stroke(8f - 6f * hit))
        val k = phase(t, V2_HIT, V2_HIT + 860f, EaseOutCubic)
        val fade = 1f - phase(t, V2_HIT + 460f, V2_HIT + 860f)
        sparks.forEach { (ang, dist) ->
            val p = Offset(c.x + cos(ang) * dist * k, c.y + sin(ang) * dist * k + 120f * k * k)
            drawCircle(Palette.Gold.copy(alpha = 0.5f * fade), 11f, p)
            drawCircle(Color(0xFFF7DC7A), 5f, p, alpha = fade)
        }
    }
}

// ---------------------------------------------------------------- fiche della O e cronometro (intro home)

private const val CHIP_FALL = 1850f
private const val CHIP_STOP = 2950f
private const val WATCH_IN = 2950f

private val EaseOutQuart = Easing { x -> 1f - (1f - x).pow(4) }

/** Fiche oro (stessa grafica del mock 13-home-v5): bordo a 8 tacche crema, centro con anello tratteggiato. */
private fun DrawScope.goldChip() {
    val d = size.minDimension
    val u = d / 230f
    val c = center
    val brush = Brush.linearGradient(listOf(Color(0xFFF7DC7A), Palette.Gold, Color(0xFFA8841F)), start = Offset.Zero, end = Offset(d, d))
    drawCircle(brush, 112f * u, c)
    val ring = 2f * PI.toFloat() * 97f * u / 16f
    drawCircle(
        Palette.Cream.copy(alpha = 0.9f), 97f * u, c,
        style = Stroke(24f * u, pathEffect = PathEffect.dashPathEffect(floatArrayOf(ring, ring))),
    )
    drawCircle(brush, 74f * u, c)
    drawCircle(Color(0xFF8A6A14), 74f * u, c, style = Stroke(4f * u))
    drawCircle(
        Palette.FeltDark.copy(alpha = 0.4f), 62f * u, c,
        style = Stroke(3f * u, pathEffect = PathEffect.dashPathEffect(floatArrayOf(5f * u, 7f * u))),
    )
}

/**
 * La O di SBLORFTZ: cade dall'alto in verticale, fa 5 giri sull'asse verticale che rallentano
 * fino a fermarsi dritta, poi un alone dorato che si spegne.
 */
@Composable
fun IntroChip(time: () -> Float, modifier: Modifier = Modifier) {
    Canvas(
        modifier.graphicsLayer {
            compositingStrategy = CompositingStrategy.Offscreen
            val t = time()
            alpha = phase(t, CHIP_FALL, CHIP_FALL + 100f)
            translationY = -900f * (1f - phase(t, CHIP_FALL, CHIP_FALL + 650f, EaseOutCubic))
            val k = phase(t, CHIP_FALL, CHIP_STOP, EaseOutQuart)
            val flip = cos((1f - k) * PI.toFloat() * 10f)
            scaleX = if (kotlin.math.abs(flip) < 0.06f) (if (flip < 0f) -0.06f else 0.06f) else flip
        },
    ) {
        val t = time()
        val glow = if (t >= CHIP_STOP) 1f - phase(t, CHIP_STOP, CHIP_STOP + 500f) else 0f
        if (glow > 0f) {
            drawCircle(
                Brush.radialGradient(
                    0.55f to Color(0xFFF7DC7A).copy(alpha = 0.95f * glow),
                    1f to Color.Transparent,
                    center = center, radius = size.minDimension * 0.75f,
                ),
                size.minDimension * 0.75f,
            )
        }
        goldChip()
    }
}

/** Cronometro tondo: TIMER sopra, badge V2 sotto. Entra con un pop dopo che la fiche si è fermata. */
@Composable
fun IntroStopwatch(time: () -> Float, anchor: V2Anchor, modifier: Modifier = Modifier) {
    Box(
        modifier
            .size(214.dp, 247.dp)
            .graphicsLayer {
                val t = time()
                val k = phase(t, WATCH_IN, WATCH_IN + 400f, backEasing(1.6f))
                alpha = phase(t, WATCH_IN, WATCH_IN + 200f)
                scaleX = 0.6f + 0.4f * k
                scaleY = scaleX
            }
            .drawBehind {
                val cream = Palette.Cream
                // corona e pulsante laterale
                drawRoundRect(cream, Offset(size.width / 2 - 17f, 0f), Size(34f, 14f), androidx.compose.ui.geometry.CornerRadius(4f))
                drawRect(cream, Offset(size.width / 2 - 7f, 12f), Size(14f, 14f))
                val c = Offset(size.width / 2, size.height - 107f)
                rotate(45f, pivot = Offset(c.x + 76f, c.y - 76f)) {
                    drawRoundRect(cream, Offset(c.x + 76f - 11f, c.y - 76f - 14f - 6f), Size(22f, 12f), androidx.compose.ui.geometry.CornerRadius(3f))
                }
                drawCircle(Color(0xFF0F1F18), 103.5f, c)
                drawCircle(cream, 103.5f, c, style = Stroke(7f))
                drawCircle(Palette.Gold.copy(alpha = 0.45f), 92f, c, style = Stroke(2f))
            },
        contentAlignment = Alignment.BottomCenter,
    ) {
        Column(
            Modifier.size(214.dp, 214.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = androidx.compose.foundation.layout.Arrangement.Center,
        ) {
            Text("TIMER", style = tight(Display, 50.sp, FontWeight.Black, Palette.Cream, letterSpacing = 5.sp, lineHeight = 1f))
            Spacer(Modifier.height(8.dp))
            V2Badge(time, anchor)
        }
    }
}

// ---------------------------------------------------------------- secondi a rulli

/**
 * Cifra dell'orologio che, quando cambia, scende: la vecchia esce in basso, la nuova cade dall'alto.
 * [delayMs] sfalsa le cifre (unità per prima, poi le altre a cascata). Il disegno di ogni frame
 * tocca solo translationY/alpha dei layer: niente ricomposizione né relayout del testo gigante.
 */
@Composable
fun RollingDigit(digit: Char, delayMs: Int, style: TextStyle, width: Dp, windowHeight: Dp, red: () -> Float) {
    var shown by remember { mutableStateOf(digit) }
    var old by remember { mutableStateOf<Char?>(null) }
    val p = remember { Animatable(1f) }
    LaunchedEffect(digit) {
        if (digit == shown) return@LaunchedEffect
        old = shown
        shown = digit
        p.snapTo(0f)
        delay(delayMs.toLong())
        p.animateTo(1f, tween(420, easing = backEasing(1.2f)))
        old = null
    }
    // Il box del testo è più alto delle cifre: si taglia a una finestra alta [windowHeight] al centro,
    // così la cifra che scende sparisce prima della barra e non ci passa sopra.
    // Larghezza fissa e cifra centrata: l'orologio non si allarga/stringe (né si ricentra) a ogni secondo.
    Box(
        Modifier.width(width).drawWithContent {
            val wh = windowHeight.toPx()
            clipRect(top = (size.height - wh) / 2, bottom = (size.height + wh) / 2) { this@drawWithContent.drawContent() }
        },
    ) {
        @Composable
        fun Layer(c: Char, layer: GraphicsLayerScope.(h: Float) -> Unit) = Box(
            Modifier.fillMaxWidth().graphicsLayer {
                compositingStrategy = CompositingStrategy.Offscreen
                layer(windowHeight.toPx())
            },
            contentAlignment = Alignment.Center,
        ) {
            Text("$c", style = style.copy(brush = Palette.ClockBrush), modifier = Modifier.graphicsLayer { alpha = 1f - red() })
            Text("$c", style = style.copy(brush = alertClockBrush(1f)), modifier = Modifier.graphicsLayer { alpha = red() })
        }
        old?.let { o ->
            Layer(o) { h ->
                translationY = p.value * h
                alpha = (1f - p.value).coerceIn(0f, 1f)
            }
        }
        Layer(shown) { h ->
            translationY = (p.value - 1f) * h
            alpha = (p.value * 1.4f).coerceIn(0f, 1f)
        }
    }
}
