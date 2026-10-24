package com.sblorftz.timer.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.Text
import com.sblorftz.timer.BuildConfig
import com.sblorftz.timer.TournamentViewModel
import com.sblorftz.timer.Updater
import kotlinx.coroutines.launch
import com.sblorftz.timer.blindLevelCount

// Misure della fiche e spazi del titolo (dp = px del mock 1920×1080), tarati sulla Fire TV.
private const val CHIP_D = 160
private const val CHIP_Y = 0
private const val WATCH_GAP = 4
private const val BUTTONS_GAP = 30

@Composable
fun HomeScreen(
    vm: TournamentViewModel,
    onResume: () -> Unit,
    onNew: () -> Unit,
    onStructure: () -> Unit,
    onExit: () -> Unit,
) {
    var confirmNew by remember { mutableStateOf(false) }
    var confirmExit by remember { mutableStateOf(false) }
    var update by remember { mutableStateOf<Updater.Update?>(null) }
    // null = non sta scaricando; -1 = errore; 0..1 = avanzamento.
    var download by remember { mutableStateOf<Float?>(null) }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val first = remember { FocusRequester() }
    val overlayOpen = confirmNew || confirmExit || update != null
    LaunchedEffect(overlayOpen) {
        if (!overlayOpen) runCatching { first.requestFocus() }
    }
    // Indietro dalla home = chiudere davvero l'app (alla riapertura riparte l'intro).
    BackHandler(enabled = !overlayOpen) { confirmExit = true }
    // Solo dalla home e una volta per avvio: mai in mezzo a un torneo.
    LaunchedEffect(Unit) {
        if (!Updater.checkedThisLaunch) {
            Updater.checkedThisLaunch = true
            update = Updater.check()
        }
    }

    // Intro (una volta per avvio): carte a ventaglio, lettere che cadono, la fiche che diventa la O,
    // cronometro con il badge V2 che si schianta, bottoni che salgono.
    val intro = remember { !HomeIntro.played }
    val introTime = if (intro) rememberFrameTime(untilMs = 5_600) else remember { mutableStateOf(10_000L) }
    LaunchedEffect(Unit) { HomeIntro.played = true }
    val t = { introTime.value.toFloat() }
    val v2 = remember { V2Anchor() }
    val shake = Modifier.graphicsLayer { v2Shake(t()).let { translationX = it.x; translationY = it.y } }

    TableBackground {
        FanCards(t, shake.fillMaxWidth().padding(top = 110.dp))
        Column(
            shake
                .fillMaxSize()
                .padding(top = 170.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                "SBLORFTZ".forEachIndexed { i, ch ->
                    if (ch == 'O') {
                        // Al posto della O la fiche oro, che arriva dopo le lettere.
                        IntroChip(t, Modifier.padding(end = 8.dp).offset(y = CHIP_Y.dp).size(CHIP_D.dp))
                        return@forEachIndexed
                    }
                    Text(
                        ch.toString(),
                        style = tight(Display, 190.sp, FontWeight.Black, Palette.Gold, letterSpacing = 30.sp),
                        modifier = Modifier.graphicsLayer {
                            compositingStrategy = CompositingStrategy.Offscreen
                            val drop = phase(t(), 1300f + i * 70, 1750f + i * 70, backEasing())
                            alpha = phase(t(), 1300f + i * 70, 1500f + i * 70)
                            translationY = -90f * (1f - drop)
                        },
                    )
                }
            }
            Spacer(Modifier.height(WATCH_GAP.dp))
            IntroStopwatch(t, v2)
            Spacer(Modifier.height(BUTTONS_GAP.dp))

            Column(
                verticalArrangement = Arrangement.spacedBy(22.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                fun rise(i: Int) = Modifier.graphicsLayer {
                    compositingStrategy = CompositingStrategy.Offscreen
                    val a = phase(t(), 4200f + i * 120, 4700f + i * 120, EaseOutCubic)
                    alpha = a
                    translationY = 50f * (1f - a)
                }
                if (vm.hasSavedSession) {
                    GoldButton("Riprendi torneo", onResume, rise(0).focusRequester(first))
                    GoldButton("Nuovo torneo", { confirmNew = true }, rise(1))
                } else {
                    GoldButton(
                        "Avvia torneo", onNew, rise(0).focusRequester(first),
                        subtitle = "${vm.structure.blindLevelCount()} livelli · ${formatDuration(vm.structure.sumOf { it.durationMs })}",
                    )
                }
                GoldButton("Struttura", onStructure, rise(2))
            }
        }
        V2Impact(t, v2)
        Box(Modifier.fillMaxSize().padding(bottom = 48.dp), contentAlignment = Alignment.BottomCenter) {
            Text("v${Updater.displayVersion(BuildConfig.VERSION_NAME)}", style = label(20, Palette.Muted2.copy(alpha = 0.6f), 0.2f, FontWeight.Medium))
        }

        if (confirmExit) {
            Overlay(
                title = "Vuoi uscire?",
                subtitle = "Sblorftz Timer si chiude del tutto.",
                actions = listOf(
                    DialogAction("Annulla") { confirmExit = false },
                    DialogAction("Esci", danger = true) { onExit() },
                ),
            )
        }

        if (confirmNew) {
            Overlay(
                title = "Nuovo torneo?",
                subtitle = "Quello salvato si perde e si riparte dal livello 1.",
                actions = listOf(
                    DialogAction("Annulla") { confirmNew = false },
                    DialogAction("Nuovo torneo", danger = true) {
                        confirmNew = false
                        onNew()
                    },
                ),
            )
        }

        update?.let { u ->
            val d = download
            when {
                d == null -> Overlay(
                    title = "Aggiornamento ${Updater.displayVersion(u.versionName)}",
                    subtitle = u.notes.ifBlank { "È uscita una nuova versione di Sblorftz Timer." },
                    actions = listOf(
                        DialogAction("Aggiorna") {
                            download = 0f
                            scope.launch {
                                runCatching { Updater.downloadAndInstall(context, u) { download = it } }
                                    .onSuccess { update = null; download = null }
                                    .onFailure { download = -1f }
                            }
                        },
                        DialogAction("Più tardi") { update = null },
                    ),
                )
                d < 0f -> Overlay(
                    title = "Download non riuscito",
                    subtitle = "Controlla la connessione e riprova.",
                    actions = listOf(
                        DialogAction("Riprova") { download = null },
                        DialogAction("Più tardi") { update = null; download = null },
                    ),
                )
                else -> Overlay(
                    title = "Scarico ${Updater.displayVersion(u.versionName)}…",
                    subtitle = "${(d * 100).toInt()}%",
                    actions = emptyList(),
                )
            }
        }
    }
}
