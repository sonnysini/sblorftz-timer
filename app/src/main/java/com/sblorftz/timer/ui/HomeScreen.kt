package com.sblorftz.timer.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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

@Composable
fun HomeScreen(
    vm: TournamentViewModel,
    onResume: () -> Unit,
    onNew: () -> Unit,
    onStructure: () -> Unit,
) {
    var confirmNew by remember { mutableStateOf(false) }
    var update by remember { mutableStateOf<Updater.Update?>(null) }
    // null = non sta scaricando; -1 = errore; 0..1 = avanzamento.
    var download by remember { mutableStateOf<Float?>(null) }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val first = remember { FocusRequester() }
    val overlayOpen = confirmNew || update != null
    LaunchedEffect(overlayOpen) {
        if (!overlayOpen) runCatching { first.requestFocus() }
    }
    // Solo dalla home e una volta per avvio: mai in mezzo a un torneo.
    LaunchedEffect(Unit) {
        if (!Updater.checkedThisLaunch) {
            Updater.checkedThisLaunch = true
            update = Updater.check()
        }
    }

    TableBackground {
        Column(
            Modifier
                .fillMaxSize()
                .padding(top = 170.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                "SBLORFTZ",
                style = tight(Display, 190.sp, FontWeight.Black, Palette.Gold, letterSpacing = 30.sp),
            )
            Spacer(Modifier.height(18.dp))
            Text("TIMER  ·  ♠ ♥ ♣ ♦", style = label(30, Palette.Cream.copy(alpha = 0.45f), 0.45f))
            Spacer(Modifier.height(96.dp))

            Column(
                verticalArrangement = Arrangement.spacedBy(22.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                if (vm.hasSavedSession) {
                    GoldButton("Riprendi torneo", onResume, Modifier.focusRequester(first))
                    GoldButton("Nuovo torneo", { confirmNew = true })
                } else {
                    GoldButton(
                        "Avvia torneo", onNew, Modifier.focusRequester(first),
                        subtitle = "${vm.structure.blindLevelCount()} livelli · ${formatDuration(vm.structure.sumOf { it.durationMs })}",
                    )
                }
                GoldButton("Struttura", onStructure)
            }
        }
        Box(Modifier.fillMaxSize().padding(bottom = 48.dp), contentAlignment = Alignment.BottomCenter) {
            Text("v${BuildConfig.VERSION_NAME}", style = label(20, Palette.Muted2.copy(alpha = 0.6f), 0.2f, FontWeight.Medium))
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
                    title = "Aggiornamento ${u.versionName}",
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
                    title = "Scarico ${u.versionName}…",
                    subtitle = "${(d * 100).toInt()}%",
                    actions = emptyList(),
                )
            }
        }
    }
}
