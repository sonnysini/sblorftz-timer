package com.sblorftz.timer.ui

import android.view.KeyEvent
import androidx.activity.compose.BackHandler
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
            TopBar(vm)
            if (level.isChipChange) ChipChangeCenter(vm) else BlindsCenter(vm)
            BottomBar(vm)
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

@Composable
private fun Clock(vm: TournamentViewModel) {
    val seconds = ((vm.remainingMs + 999) / 1000).toInt()
    val alert = vm.running && seconds <= 10
    val pulse = rememberInfiniteTransition(label = "pulse")
    val beat by pulse.animateFloat(
        initialValue = 1f,
        targetValue = 1.06f,
        animationSpec = infiniteRepeatable(tween(500), RepeatMode.Reverse),
        label = "beat",
    )
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
                brush = if (alert) Palette.ClockAlertBrush else Palette.ClockBrush,
            ),
            modifier = Modifier
                .scale(if (alert) beat else 1f)
                .alpha(if (vm.running || vm.finished) 1f else 0.35f),
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
private fun BlindsCenter(vm: TournamentViewModel) {
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
            Clock(vm)
            Spacer(Modifier.height(26.dp))
            ProgressBar(vm)
            Spacer(Modifier.height(40.dp))
            Box(Modifier.alpha(if (vm.running || vm.finished) 1f else 0.5f)) {
                Text(
                    buildAnnotatedString {
                        append(formatChips(level.smallBlind))
                        withStyle(SpanStyle(color = Palette.Gold.copy(alpha = 0.45f), fontWeight = FontWeight.SemiBold)) {
                            append(" / ")
                        }
                        append(formatChips(level.bigBlind))
                    },
                    style = tight(Display, 150.sp, FontWeight.ExtraBold, Palette.Gold, lineHeight = 1f),
                )
            }
            Spacer(Modifier.height(4.dp))
            Text("PICCOLO · GRANDE BUIO", style = label(24, Palette.Muted2, 0.42f))
        }
        PauseBadge(vm)
        HintOrHold(vm)
    }
}

@Composable
private fun ChipChangeCenter(vm: TournamentViewModel) {
    Box(Modifier.fillMaxSize()) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(top = 126.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text("CAMBIO FICHES", style = label(30, Palette.GoldLight, 0.5f))
            Spacer(Modifier.height(10.dp))
            Clock(vm)
            Spacer(Modifier.height(26.dp))
            ProgressBar(vm)
            Spacer(Modifier.height(50.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(60.dp),
            ) {
                Chip("100", Palette.ChipBlack)
                Text("× 5", style = tight(Display, 96.sp, FontWeight.ExtraBold))
                Text("→", style = tight(Display, 96.sp, FontWeight.ExtraBold, Palette.Gold))
                Chip("500", Palette.ChipPurple)
            }
            Spacer(Modifier.height(28.dp))
            Text("LE FICHES DA 100 ESCONO DAL TAVOLO", style = label(24, Palette.Muted2, 0.42f))
        }
        PauseBadge(vm)
        HintOrHold(vm)
    }
}

@Composable
private fun Chip(value: String, color: Color) {
    Box(
        Modifier
            .size(120.dp)
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
    if (vm.running) return
    Box(Modifier.fillMaxWidth().offset(y = 376.dp), contentAlignment = Alignment.TopCenter) {
        Box(
            Modifier
                .drawBehind {
                    val radius = size.width * 0.7f
                    drawCircle(
                        brush = Brush.radialGradient(
                            listOf(Palette.Gold.copy(alpha = 0.35f), Color.Transparent),
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
                if (vm.finished) "FINE" else "PAUSA",
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
