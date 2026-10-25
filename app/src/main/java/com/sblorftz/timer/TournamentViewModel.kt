package com.sblorftz.timer

import android.app.Application
import android.os.SystemClock
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

enum class HoldAction { Previous, Next, Plus, Minus }

class TournamentViewModel(app: Application) : AndroidViewModel(app) {
    private val store = Store(app)
    private val sounds = Sounds(app)

    var structure by mutableStateOf(store.loadStructure())
        private set
    var hasSavedSession by mutableStateOf(store.loadSession() != null)
        private set

    var levelIndex by mutableIntStateOf(0)
        private set
    var remainingMs by mutableLongStateOf(0L)
        private set
    var playedMs by mutableLongStateOf(0L)
        private set
    var running by mutableStateOf(false)
        private set
    /** Ultimo livello arrivato a zero: il torneo è a fine struttura. */
    var finished by mutableStateOf(false)
        private set

    // Undo bidirezionale: cambiando livello a mano si ricorda dove si era,
    // e tornandoci si ritrova il tempo lasciato.
    private var checkpointIndex: Int? = null
    private var checkpointRemainingMs: Long? = null

    var holdAction by mutableStateOf<HoldAction?>(null)
        private set
    var holdProgress by mutableFloatStateOf(0f)
        private set
    private var holdJob: Job? = null

    /** Istante (elapsedRealtime) dell'ultimo cambio livello, per l'animazione; 0 = nessuno. */
    var levelChangedAt by mutableLongStateOf(0L)
        private set
    /** Livello da cui si è arrivati con l'ultimo cambio (per i rulli dei bui). */
    var previousLevel: Level? = null
        private set
    /** Istante in cui il torneo è finito, per l'animazione; 0 = non finito. */
    var finishedAt by mutableLongStateOf(0L)
        private set

    /** Istante in cui parte la scia dorata del cambio livello; 0 = nessuna. */
    var sweepAt by mutableLongStateOf(0L)
        private set
    /** Il boom di fine livello è già partito in anticipo. */
    private var soundLed = false

    private var lastTick = 0L
    private var lastSavedSecond = -1L

    val level: Level get() = structure[levelIndex]
    val nextLevel: Level? get() = structure.getOrNull(levelIndex + 1)

    /** Millisecondi alla prossima pausa cambio fiches, null se non ce ne sono più. */
    val msToChipChange: Long?
        get() {
            val next = (levelIndex + 1 until structure.size).firstOrNull { structure[it].isChipChange }
                ?: return null
            return remainingMs + (levelIndex + 1 until next).sumOf { structure[it].durationMs }
        }

    /** Millisecondi alla fine dell'ultimo livello. */
    val msToEnd: Long
        get() = remainingMs + (levelIndex + 1 until structure.size).sumOf { structure[it].durationMs }

    init {
        // Torneo salvato: si carica subito (in pausa), così struttura e home
        // sanno a che livello si è arrivati anche prima di riprenderlo.
        if (hasSavedSession) resumeSavedTournament()
        viewModelScope.launch {
            while (isActive) {
                delay(nextTickMs())
                tick()
            }
        }
    }

    private fun tick() {
        val now = SystemClock.elapsedRealtime()
        val delta = now - lastTick
        lastTick = now
        if (!running) return

        val before = remainingMs
        val after = (before - delta).coerceAtLeast(0)
        remainingMs = after
        playedMs += delta

        if (before > 60_000 && after <= 60_000) sounds.oneMinute()
        if (before > 10_000 && after <= 10_000) sounds.tenSeconds()
        val last = levelIndex == structure.lastIndex
        // Il boom parte in anticipo così il colpo cade sul cambio; la scia passa il centro sul cambio.
        if (before > SOUND_LEAD_MS && after <= SOUND_LEAD_MS) {
            soundLed = true
            if (last) sounds.levelEnd() else sounds.levelChange()
        }
        if (before > SWEEP_LEAD_MS && after <= SWEEP_LEAD_MS) sweepAt = now

        if (after == 0L) {
            if (!soundLed) {
                if (last) sounds.levelEnd(now = true) else sounds.levelChange(now = true)
                sweepAt = now
            }
            soundLed = false
            if (!last) {
                markLevelChange()
                levelIndex++
                remainingMs = level.durationMs
            } else {
                running = false
                finished = true
                finishedAt = now
            }
            save()
        } else if (after / 1000 != lastSavedSecond) {
            save()
        }
    }

    /** Tick ogni 100 ms, ma si sveglia giusto in tempo per boom, scia e cambio livello. */
    private fun nextTickMs(): Long {
        if (!running) return 100
        val r = remainingMs
        val next = listOf(r - SOUND_LEAD_MS, r - SWEEP_LEAD_MS, r).filter { it > 0 }.minOrNull() ?: 1
        return next.coerceIn(1, 100)
    }

    fun startNewTournament() {
        levelIndex = 0
        remainingMs = level.durationMs
        playedMs = 0
        checkpointIndex = null
        checkpointRemainingMs = null
        finished = false
        resumeClock()
        save()
    }

    fun resumeSavedTournament() {
        val s = store.loadSession() ?: return startNewTournament()
        levelIndex = s.levelIndex.coerceIn(0, structure.lastIndex)
        remainingMs = s.remainingMs.coerceIn(0, level.durationMs)
        playedMs = s.playedMs
        checkpointIndex = s.checkpointIndex
        checkpointRemainingMs = s.checkpointRemainingMs
        finished = false
        // Si riparte in pausa: il tempo riprende quando il tavolo è pronto.
        running = false
    }

    fun togglePause() {
        if (finished) return
        if (running) {
            running = false
            save()
        } else {
            resumeClock()
        }
    }

    fun pause() {
        if (running) {
            running = false
            save()
        }
    }

    private fun resumeClock() {
        lastTick = SystemClock.elapsedRealtime()
        running = true
    }

    /** ±1 minuto sul livello in corso, senza superare la sua durata. */
    fun adjustMinute(up: Boolean) {
        val step = if (up) 60_000 else -60_000
        remainingMs = (remainingMs + step).coerceIn(1_000, level.durationMs)
        soundLed = false
        finished = false
        save()
    }

    fun resetLevel() {
        remainingMs = level.durationMs
        soundLed = false
        finished = false
        sounds.levelEnd(now = true)
        save()
    }

    fun startHold(action: HoldAction) {
        if (holdAction != null) return
        val possible = when (action) {
            HoldAction.Previous -> levelIndex > 0
            HoldAction.Next -> levelIndex < structure.lastIndex
            HoldAction.Plus, HoldAction.Minus -> true
        }
        if (!possible) return
        holdAction = action
        holdProgress = 0f
        holdJob = viewModelScope.launch {
            val start = SystemClock.elapsedRealtime()
            while (isActive) {
                val p = (SystemClock.elapsedRealtime() - start) / HOLD_MS.toFloat()
                holdProgress = p.coerceAtMost(1f)
                if (p >= 1f) {
                    clearHold()
                    when (action) {
                        HoldAction.Next -> changeLevel(1)
                        HoldAction.Previous -> changeLevel(-1)
                        HoldAction.Plus -> adjustMinute(up = true)
                        HoldAction.Minus -> adjustMinute(up = false)
                    }
                    break
                }
                delay(16)
            }
        }
    }

    fun cancelHold() {
        holdJob?.cancel()
        clearHold()
    }

    private fun clearHold() {
        holdJob = null
        holdAction = null
        holdProgress = 0f
    }

    private fun changeLevel(direction: Int) {
        val target = levelIndex + direction
        if (target !in structure.indices) return
        markLevelChange()
        val restore = checkpointIndex == target && checkpointRemainingMs != null
        if (restore) {
            levelIndex = target
            remainingMs = checkpointRemainingMs!!.coerceIn(0, level.durationMs)
            checkpointIndex = null
            checkpointRemainingMs = null
        } else {
            checkpointIndex = levelIndex
            checkpointRemainingMs = remainingMs
            levelIndex = target
            remainingMs = level.durationMs
        }
        soundLed = false
        finished = false
        sounds.levelChange(now = true)
        save()
    }

    private fun markLevelChange() {
        previousLevel = level
        levelChangedAt = SystemClock.elapsedRealtime()
        // Cambio a mano (o scia non partita): la scia parte adesso.
        if (sweepAt < levelChangedAt - 1_000) sweepAt = levelChangedAt
    }

    fun updateStructure(levels: List<Level>) {
        if (levels.isEmpty()) return
        structure = levels
        store.saveStructure(levels)
        // Il torneo in corso usa subito la struttura nuova.
        levelIndex = levelIndex.coerceIn(0, levels.lastIndex)
        remainingMs = remainingMs.coerceAtMost(level.durationMs)
        if (checkpointIndex?.let { it !in levels.indices } == true) {
            checkpointIndex = null
            checkpointRemainingMs = null
        }
        if (hasSavedSession) save()
    }

    fun endTournament() {
        running = false
        store.clearSession()
        hasSavedSession = false
    }

    /** Uscita alla home: il torneo resta salvato e si riprende da lì. */
    fun leaveTimer() {
        cancelHold()
        running = false
        save()
    }

    private fun save() {
        lastSavedSecond = remainingMs / 1000
        store.saveSession(
            Session(levelIndex, remainingMs, playedMs, checkpointIndex, checkpointRemainingMs),
        )
        hasSavedSession = true
    }

    override fun onCleared() {
        sounds.release()
    }

    private companion object {
        const val HOLD_MS = 1_000L
        /** Rincorsa del boom (500 ms) + ritardo audio HDMI (~80 ms, come per l'intro). */
        const val SOUND_LEAD_MS = 580L
        /** Metà della corsa della scia (950 ms, vedi SweepFlash): così passa il centro sul cambio. */
        const val SWEEP_LEAD_MS = 475L
    }
}
