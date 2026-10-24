package com.sblorftz.timer

import android.os.Bundle
import android.os.Process
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import com.sblorftz.timer.ui.EditorScreen
import com.sblorftz.timer.ui.HomeScreen
import com.sblorftz.timer.ui.TimerScreen

private enum class Screen { Home, Timer, Editor }

class MainActivity : ComponentActivity() {
    private val vm: TournamentViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            var screen by rememberSaveable { mutableStateOf(Screen.Home) }
            // Da dove si è aperta la struttura: ci si torna premendo Fatto/Indietro.
            var editorReturn by rememberSaveable { mutableStateOf(Screen.Home) }

            // Schermo sempre acceso finché si è al tavolo o il tempo sta scorrendo.
            val keepOn = screen == Screen.Timer || vm.running
            LaunchedEffect(keepOn) {
                if (keepOn) {
                    window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                } else {
                    window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                }
            }

            DesignCanvas {
                when (screen) {
                    Screen.Home -> HomeScreen(
                        vm = vm,
                        onResume = {
                            vm.resumeSavedTournament()
                            screen = Screen.Timer
                        },
                        onNew = {
                            vm.startNewTournament()
                            screen = Screen.Timer
                        },
                        onStructure = {
                            editorReturn = Screen.Home
                            screen = Screen.Editor
                        },
                        onExit = {
                            exitRequested = true
                            finishAndRemoveTask()
                        },
                    )
                    Screen.Timer -> TimerScreen(
                        vm = vm,
                        onOpenStructure = {
                            editorReturn = Screen.Timer
                            screen = Screen.Editor
                        },
                        onExit = { screen = Screen.Home },
                    )
                    Screen.Editor -> EditorScreen(vm = vm, onDone = { screen = editorReturn })
                }
            }
        }
    }

    private var exitRequested = false

    override fun onDestroy() {
        super.onDestroy()
        // "Esci" dalla home: chiude anche il processo, così alla riapertura riparte l'intro
        // (HomeIntro.played e il controllo aggiornamenti vivono nel processo).
        if (exitRequested && isFinishing) Process.killProcess(Process.myPid())
    }

    override fun onStop() {
        super.onStop()
        // Se la TV si spegne o si cambia app il torneo si ferma: meglio una pausa
        // in più che livelli che scorrono senza nessuno al tavolo.
        vm.pause()
    }
}

/**
 * Tutta l'interfaccia è disegnata su una tela di 1920×1080: 1 dp = 1 pixel
 * del mockup, qualunque sia la densità della TV.
 */
@Composable
private fun DesignCanvas(content: @Composable () -> Unit) {
    BoxWithConstraints(Modifier.fillMaxSize()) {
        val current = LocalDensity.current
        val widthPx = with(current) { maxWidth.toPx() }
        val scale = widthPx / 1920f
        CompositionLocalProvider(LocalDensity provides Density(scale, fontScale = 1f)) {
            content()
        }
    }
}
