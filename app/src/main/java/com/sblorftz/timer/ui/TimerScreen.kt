package com.sblorftz.timer.ui

import android.view.KeyEvent
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.ui.graphics.Shadow
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.Text
import com.sblorftz.timer.HoldAction
import com.sblorftz.timer.Level
import com.sblorftz.timer.TournamentViewModel
import com.sblorftz.timer.blindLevelCount
import com.sblorftz.timer.displayNumber
import com.sblorftz.timer.formatChips
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.delay

private const val CLOCK_HEIGHT = 380

private enum class TimerDialog { Menu, Exit, NewTournament }

@Composable
fun TimerScreen(
    vm: TournamentViewModel,
    onOpenStructure: () -> Unit,
    onExit: () -> Unit,
) {
    var dialog by remember { mutableStateOf<TimerDialog?>(null) }
    val focus = remember { FocusRequester() }

    LaunchedEffect(dialog) {
        if (dialog == null) runCatching { focus.requestFocus() }
    }

    BackHandler {
        dialog = if (dialog == null) TimerDialog.Exit else null
    }

    val level = vm.level
    TableBackground(warm = level.isChipChange) {
        Box(
            Modifier
                .fillMaxSize()
                .focusRequester(focus)
                .focusable()
                .onPreviewKeyEvent { e ->
                    if (dialog != null) return@onPreviewKeyEvent false
                    val code = e.nativeKeyEvent.keyCode
                    val down = e.type == KeyEventType.KeyDown
                    val up = e.type == KeyEventType.KeyUp
                    val first = down && e.nativeKeyEvent.repeatCount == 0
                    when (code) {
                        KeyEvent.KEYCODE_DPAD_CENTER, KeyEvent.KEYCODE_ENTER, KeyEvent.KEYCODE_NUMPAD_ENTER,
                        KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE, KeyEvent.KEYCODE_MEDIA_PLAY, KeyEvent.KEYCODE_MEDIA_PAUSE,
                        -> {
                            if (first) vm.togglePause()
                            true
                        }
                        KeyEvent.KEYCODE_DPAD_LEFT, KeyEvent.KEYCODE_MEDIA_REWIND -> {
                            if (first) vm.startHold(HoldAction.Previous)
                            if (up) vm.cancelHold()
                            true
                        }
                        KeyEvent.KEYCODE_DPAD_RIGHT, KeyEvent.KEYCODE_MEDIA_FAST_FORWARD -> {
                            if (first) vm.startHold(HoldAction.Next)
                            if (up) vm.cancelHold()
                            true
                        }
                        KeyEvent.KEYCODE_DPAD_UP -> {
                            if (down) vm.adjustMinute(up = true)
                            true
                        }
                        KeyEvent.KEYCODE_DPAD_DOWN -> {
                            if (down) vm.adjustMinute(up = false)
                            true
                        }
                        KeyEvent.KEYCODE_MENU -> {
                            if (first) dialog = TimerDialog.Menu
                            true
                        }
                        else -> false
                    }
                },
        ) {
            val alert = rememberAlert(vm)
            val dim = rememberPauseDim(vm)
            val change = rememberSince(vm.levelChangedAt, 1_200)
            AlertVignette(alert.red * (0.35f + 0.65f * alert.beat))
            TopBar(vm)
            if (level.isChipChange) ChipChangeCenter(vm, alert, dim, change) else BlindsCenter(vm, alert, dim, change)
            BottomBar(vm)
            if (dim > 0f) Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.45f * dim)))
            PauseBadge(vm)
            HintOrHold(vm)
            LevelChangeFlash(change)
            if (vm.finished) {
                FinishOverlay(vm.finishedAt, "${formatDuration(vm.playedMs)} di gioco · ${vm.structure.blindLevelCount()} livelli")
                TopBar(vm)
                HintOrHold(vm)
            }
        }

        when (dialog) {
            TimerDialog.Menu -> Overlay(
                title = "Menu",
                vertical = true,
                actions = listOf(
                    DialogAction(if (vm.running) "Continua" else "Riprendi") {
                        if (!vm.running) vm.togglePause()
                        dialog = null
                    },
                    DialogAction("Ricomincia livello") {
                        vm.resetLevel()
                        dialog = null
                    },
                    DialogAction("Struttura") {
                        dialog = null
                        onOpenStructure()
                    },
                    DialogAction("Nuovo torneo") { dialog = TimerDialog.NewTournament },
                    DialogAction("Esci") { dialog = TimerDialog.Exit },
                ),
            )
            TimerDialog.Exit -> Overlay(
                title = "Uscire dal torneo?",
                subtitle = "Resta salvato: lo riprendi dalla schermata iniziale.",
                actions = listOf(
                    DialogAction("Continua") { dialog = null },
                    DialogAction("Esci", danger = true) {
                        vm.leaveTimer()
                        onExit()
                    },
                ),
            )
            TimerDialog.NewTournament -> Overlay(
                title = "Ricominciare da capo?",
                subtitle = "Il torneo in corso si perde e si riparte dal livello 1.",
                actions = listOf(
                    DialogAction("Annulla") { dialog = null },
                    DialogAction("Ricomincia", danger = true) {
                        vm.startNewTournament()
                        dialog = null
                    },
                ),
            )
            null -> Unit
        }
    }
}

@Composable
private fun TopBar(vm: TournamentViewModel) {
    // niente java.time: su Android 7 (Fire TV Stick 4K) non esiste
    val clockFormat = remember { SimpleDateFormat("HH:mm", Locale.ITALY) }
    val clock by produceState(clockFormat.format(Date())) {
        while (true) {
            value = clockFormat.format(Date())
            delay(1_000)
        }
    }
    Row(
        Modifier
            .fillMaxWidth()
            .padding(start = 96.dp, end = 96.dp, top = 56.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Brand()
        Row(
            horizontalArrangement = Arrangement.spacedBy(56.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            val structure = vm.structure
            if (vm.level.isChipChange) {
                MetaText("Pausa ", "cambio fiches", "")
            } else {
                MetaText("Livello ", "${structure.displayNumber(vm.levelIndex)}", " di ${structure.blindLevelCount()}")
            }
            MetaText("In gioco da ", formatDuration(vm.playedMs), "")
            Text(clock, style = label(40, Palette.Cream, 0f, FontWeight.Bold))
        }
    }
}

@Composable
private fun MetaText(before: String, bold: String, after: String) {
    Text(
        buildAnnotatedString {
            append(before)
            withStyle(SpanStyle(color = Palette.Cream, fontWeight = FontWeight.SemiBold)) { append(bold) }
            append(after)
        },
        style = label(26, Palette.Muted, 0f, FontWeight.Medium),
    )
}

/** Ultimi 10 secondi: [red] 0..1 (entra a 00:10), [beat] 1→0 a ogni secondo. */
private class AlertFx(val red: Float, val beat: Float)

@Composable
private fun rememberAlert(vm: TournamentViewModel): AlertFx {
    val seconds = ((vm.remainingMs + 999) / 1000).toInt()
    val alert = vm.running && seconds <= 10
    val red by animateFloatAsState(if (alert) 1f else 0f, tween(if (alert) 700 else 300), label = "red")
    val beat = remember { Animatable(0f) }
    LaunchedEffect(seconds, alert) {
        if (alert) {
            beat.snapTo(1f)
            beat.animateTo(0f, tween(400, easing = EaseOutCubic))
        } else {
            beat.snapTo(0f)
        }
    }
    return AlertFx(red, beat.value)
}

/** 0 = in gioco, 1 = in pausa (tavolo scurito). */
@Composable
private fun rememberPauseDim(vm: TournamentViewModel): Float {
    val dim by animateFloatAsState(
        if (!vm.running && !vm.finished) 1f else 0f,
        tween(600, easing = EaseOutCubic),
        label = "dim",
    )
    return dim
}

@Composable
private fun Clock(vm: TournamentViewModel, alert: AlertFx, dim: Float, change: Float) {
    val seconds = ((vm.remainingMs + 999) / 1000).toInt()
    // dopo un cambio livello l'orologio nuovo entra con un piccolo rimbalzo
    val pop = if (change < 1f) 1.1f - 0.1f * phase(change, 0f, 0.375f, backEasing()) else 1f
    // Il box del font è più alto delle cifre: lo si stringe all'altezza vera
    // dei numeri, così il resto della colonna resta dove sta nel mockup.
    Box(
        Modifier
            .height(CLOCK_HEIGHT.dp)
            .wrapContentHeight(align = Alignment.CenterVertically, unbounded = true),
    ) {
        Text(
            "%02d:%02d".format(seconds / 60, seconds % 60),
            style = tight(Display, 420.sp, FontWeight.Black).copy(
                brush = if (alert.red > 0f) alertClockBrush(alert.red) else Palette.ClockBrush,
                shadow = if (alert.red > 0f) {
                    Shadow(Color(0xFFFF3C28).copy(alpha = 0.55f * alert.red * (0.4f + 0.6f * alert.beat)), blurRadius = 30f + 40f * alert.beat)
                } else {
                    null
                },
            ),
            modifier = Modifier
                .scale(pop * (1f + (0.03f + 0.05f * alert.red) * alert.beat))
                .alpha(1f - 0.65f * dim),
        )
    }
}

@Composable
private fun ProgressBar(vm: TournamentViewModel) {
    val total = vm.level.durationMs.coerceAtLeast(1)
    val done = 1f - vm.remainingMs / total.toFloat()
    val lastMinute = vm.remainingMs <= 60_000
    Box(
        Modifier
            .width(1100.dp)
            .height(10.dp)
            .clip(RoundedCornerShape(5.dp))
            .background(Color.White.copy(alpha = 0.08f))
            .drawBehind {
                drawRoundRect(
                    brush = if (lastMinute) Brush.horizontalGradient(listOf(Palette.GoldDark, Palette.Red)) else Palette.BarBrush,
                    size = size.copy(width = size.width * done.coerceIn(0f, 1f)),
                    cornerRadius = CornerRadius(size.height / 2),
                )
            },
    )
}

@Composable
private fun BlindsCenter(vm: TournamentViewModel, alert: AlertFx, dim: Float, change: Float) {
    val level = vm.level
    Box(Modifier.fillMaxSize()) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(top = 126.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                "LIVELLO ${vm.structure.displayNumber(vm.levelIndex)}",
                style = label(30, Palette.Gold, 0.5f),
            )
            Spacer(Modifier.height(10.dp))
            Clock(vm, alert, dim, change)
            Spacer(Modifier.height(26.dp))
            ProgressBar(vm)
            Spacer(Modifier.height(40.dp))
            // dopo un cambio livello i bui girano come i rulli di una slot (piccolo poi grande)
            val prev = vm.previousLevel?.takeIf { change < 1f && !it.isChipChange }
            val style = tight(Display, 150.sp, FontWeight.ExtraBold, Palette.Gold, lineHeight = 1f)
            Row(Modifier.alpha(1f - 0.5f * dim), verticalAlignment = Alignment.CenterVertically) {
                SlotNumber(
                    prev?.let { formatChips(it.smallBlind) }, fakeChips(level.smallBlind), formatChips(level.smallBlind),
                    phase(change, 0.04f, 0.83f), style, alignEnd = true,
                )
                Text(" / ", style = style.copy(color = Palette.Gold.copy(alpha = 0.45f), fontWeight = FontWeight.SemiBold))
                SlotNumber(
                    prev?.let { formatChips(it.bigBlind) }, fakeChips(level.bigBlind), formatChips(level.bigBlind),
                    phase(change, 0.17f, 0.96f), style, alignEnd = false,
                )
            }
            Spacer(Modifier.height(4.dp))
            Text("PICCOLO · GRANDE BUIO", style = label(24, Palette.Muted2, 0.42f))
        }
    }
}

/** Tre valori finti che passano nel rullo prima di fermarsi su [value]. */
private fun fakeChips(value: Int) = listOf(1.8f, 0.6f, 1.4f).map { formatChips(((value * it) / 100).toInt().coerceAtLeast(1) * 100) }

@Composable
private fun ChipChangeCenter(vm: TournamentViewModel, alert: AlertFx, dim: Float, change: Float) {
    Box(Modifier.fillMaxSize()) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(top = 126.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text("CAMBIO FICHES", style = label(30, Palette.GoldLight, 0.5f))
            Spacer(Modifier.height(10.dp))
            Clock(vm, alert, dim, change)
            Spacer(Modifier.height(26.dp))
            ProgressBar(vm)
            Spacer(Modifier.height(20.dp))
            ChipMerge()
            Spacer(Modifier.height(10.dp))
            Text("LE FICHES DA 100 ESCONO DAL TAVOLO", style = label(24, Palette.Muted2, 0.42f))
        }
    }
}

private val SlideEasing = CubicBezierEasing(0.5f, 0f, 0.2f, 1f)
private val PopEasing = CubicBezierEasing(0.3f, 1.6f, 0.5f, 1f)
private val SqueezeEasing = CubicBezierEasing(0.42f, 0f, 1f, 1f)

/**
 * Animazione in loop da 4 s del cambio fiches: le 5 nere da 100 arrivano in pila (sfalsate),
 * la pila si schiaccia con un lampo dorato e ne esce la viola da 500 (mock fiches-anim.html).
 */
@Composable
private fun ChipMerge() {
    val p by rememberInfiniteTransition(label = "chipMerge").animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(4_000, easing = LinearEasing), RepeatMode.Restart),
        label = "p",
    )
    Box(Modifier.width(900.dp).height(180.dp), contentAlignment = Alignment.Center) {
        val flashIn = phase(p, 0.53f, 0.58f)
        val flashOut = phase(p, 0.58f, 0.70f)
        Box(
            Modifier
                .size(240.dp)
                .graphicsLayer {
                    alpha = flashIn * (1f - flashOut)
                    scaleX = 0.3f + 0.7f * flashIn + 0.5f * flashOut
                    scaleY = scaleX
                }
                .background(
                    Brush.radialGradient(
                        0f to Color(0xF2FFECAA),
                        0.35f to Palette.Gold.copy(alpha = 0.55f),
                        0.7f to Color.Transparent,
                    ),
                    CircleShape,
                ),
        )
        for (i in 0 until 5) {
            val stagger = i * 0.035f
            val slide = phase(p, 0.12f + stagger, 0.32f + stagger, SlideEasing)
            val squeeze = phase(p, 0.50f, 0.58f, SqueezeEasing)
            Chip(
                "100",
                Palette.ChipBlack,
                Modifier.graphicsLayer {
                    alpha = phase(p, 0f, 0.06f) * (1f - phase(p, 0.56f, 0.58f))
                    translationX = (i - 2) * 170f * (1f - slide)
                    translationY = -i * 7f * slide * (1f - squeeze)
                    scaleX = 1f - 0.3f * squeeze
                    scaleY = scaleX
                },
            )
        }
        Chip(
            "500",
            Palette.ChipPurple,
            Modifier.graphicsLayer {
                alpha = phase(p, 0.56f, 0.68f) * (1f - phase(p, 0.92f, 1f))
                scaleX = 0.2f + 1.05f * phase(p, 0.56f, 0.68f, PopEasing)
                scaleY = scaleX
            },
        )
    }
}

@Composable
private fun Chip(value: String, color: Color, modifier: Modifier = Modifier) {
    Box(
        modifier
            .size(120.dp)
            .drawBehind {
                // spessore della fiche: serve a far leggere la pila
                drawCircle(Color.Black.copy(alpha = 0.55f), center = center + Offset(0f, 6.dp.toPx()))
            }
            .background(color, CircleShape)
            .drawBehind {
                val stroke = 12.dp.toPx()
                drawCircle(
                    color = Color.White.copy(alpha = 0.85f),
                    radius = size.minDimension / 2 - stroke / 2,
                    style = Stroke(stroke, pathEffect = PathEffect.dashPathEffect(floatArrayOf(22f, 16f))),
                )
            },
        contentAlignment = Alignment.Center,
    ) {
        Text(value, style = tight(Display, 40.sp, FontWeight.ExtraBold, Color.White))
    }
}

@Composable
private fun PauseBadge(vm: TournamentViewModel) {
    // Pausa: il badge cade dall'alto e rimbalza; alla ripresa si gonfia e sparisce.
    val paused = !vm.running && !vm.finished
    val drop = remember { Animatable(0f) }
    val out = remember { Animatable(if (paused) 0f else 1f) }
    LaunchedEffect(paused) {
        if (paused) {
            out.snapTo(0f)
            drop.snapTo(0f)
            drop.animateTo(1f, tween(900, easing = BounceEasing))
        } else {
            out.animateTo(1f, tween(300, easing = EaseOutCubic))
        }
    }
    if (out.value >= 1f) return
    val breath by rememberInfiniteTransition(label = "breath").animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(1_570), RepeatMode.Reverse),
        label = "b",
    )
    Box(Modifier.fillMaxWidth().offset(y = 376.dp), contentAlignment = Alignment.TopCenter) {
        Box(
            Modifier
                .graphicsLayer {
                    translationY = -750f * (1f - drop.value)
                    val s = 1f + 0.12f * out.value
                    scaleX = s
                    scaleY = s
                    alpha = 1f - out.value
                }
                .drawBehind {
                    val radius = size.width * (0.6f + 0.2f * breath)
                    drawCircle(
                        brush = Brush.radialGradient(
                            listOf(Palette.Gold.copy(alpha = 0.3f + 0.25f * breath), Color.Transparent),
                            center = center,
                            radius = radius,
                        ),
                        radius = radius,
                    )
                }
                .background(Palette.Gold, RoundedCornerShape(18.dp))
                .padding(start = 76.dp, end = 40.dp, top = 18.dp, bottom = 18.dp),
        ) {
            Text(
                "PAUSA",
                style = tight(Display, 120.sp, FontWeight.Black, Palette.FeltDark, letterSpacing = 36.sp),
            )
        }
    }
}

@Composable
private fun HintOrHold(vm: TournamentViewModel) {
    Box(
        Modifier
            .fillMaxSize()
            .padding(bottom = 178.dp),
        contentAlignment = Alignment.BottomCenter,
    ) {
        val hold = vm.holdAction
        if (hold != null) {
            HoldPill(hold, vm.holdProgress)
        } else if (vm.finished) {
            Hints("☰" to "menu")
        } else if (vm.running) {
            Hints("OK" to "pausa", "◀ ▶" to "tieni premuto per cambiare livello", "☰" to "menu")
        } else {
            Hints("OK" to "riprendi", "▲ ▼" to "±1 minuto", "☰" to "menu")
        }
    }
}

@Composable
private fun HoldPill(action: HoldAction, progress: Float) {
    val text = if (action == HoldAction.Next) "LIVELLO SUCCESSIVO  ▶" else "◀  LIVELLO PRECEDENTE"
    Box(
        Modifier
            .width(620.dp)
            .clip(RoundedCornerShape(30.dp))
            .background(Color.White.copy(alpha = 0.06f))
            .border(1.dp, Palette.Gold.copy(alpha = 0.5f), RoundedCornerShape(30.dp))
            .drawBehind {
                drawRect(
                    brush = Palette.BarBrush,
                    size = size.copy(width = size.width * progress),
                    alpha = 0.85f,
                )
            }
            .padding(vertical = 6.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(text, style = label(24, Palette.Cream, 0.25f, FontWeight.Bold))
    }
}

@Composable
private fun BottomBar(vm: TournamentViewModel) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.BottomCenter) {
        Row(
            Modifier
                .fillMaxWidth()
                .height(170.dp)
                .background(Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(alpha = 0.55f))))
                .drawBehind {
                    drawLine(Palette.Gold.copy(alpha = 0.18f), Offset.Zero, Offset(size.width, 0f), 1.dp.toPx())
                }
                .padding(horizontal = 96.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            val next = vm.nextLevel
            val current = vm.level
            when {
                next == null -> InfoCard("PROSSIMO", "Ultimo livello", null)
                current.isChipChange -> InfoCard("SI RIPARTE CON", blindsText(next), "${next.minutes} min")
                next.isChipChange -> InfoCard("PROSSIMO", "Cambio fiches", "${next.minutes} min")
                else -> InfoCard("PROSSIMO", blindsText(next), "${next.minutes} min")
            }

            val toChange = vm.msToChipChange
            if (current.isChipChange) {
                InfoCard(
                    "LIVELLO",
                    "${vm.structure.displayNumber(vm.levelIndex) + 1} di ${vm.structure.blindLevelCount()}",
                    null,
                    alignEnd = true,
                )
            } else if (toChange != null) {
                InfoCard("CAMBIO FICHES TRA", formatDuration(toChange), null, alignEnd = true, chip = true)
            } else {
                InfoCard("FINE STRUTTURA TRA", formatDuration(vm.msToEnd), null, alignEnd = true)
            }
        }
    }
}

private fun blindsText(level: Level) = "${formatChips(level.smallBlind)} / ${formatChips(level.bigBlind)}"

@Composable
private fun InfoCard(title: String, value: String, extra: String?, alignEnd: Boolean = false, chip: Boolean = false) {
    Column(horizontalAlignment = if (alignEnd) Alignment.End else Alignment.Start) {
        Text(title, style = label(20, Palette.Muted2, 0.35f))
        Spacer(Modifier.height(6.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (chip) {
                Box(
                    Modifier
                        .size(26.dp)
                        .drawBehind {
                            val stroke = 5.dp.toPx()
                            drawCircle(
                                Palette.Gold,
                                radius = size.minDimension / 2 - stroke / 2,
                                style = Stroke(stroke, pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 6f))),
                            )
                        },
                )
                Spacer(Modifier.width(12.dp))
            }
            Text(value, style = tight(Display, 58.sp, FontWeight.ExtraBold, lineHeight = 1.05f))
            if (extra != null) {
                Spacer(Modifier.width(14.dp))
                Text(extra, style = label(34, Palette.Muted, 0f, FontWeight.Medium))
            }
        }
    }
}

/** 52 min, 1 h 12 min. */
fun formatDuration(ms: Long): String {
    val totalMin = ms / 60_000
    val h = totalMin / 60
    val m = totalMin % 60
    return if (h > 0) "$h h %02d min".format(m) else "$m min"
}
