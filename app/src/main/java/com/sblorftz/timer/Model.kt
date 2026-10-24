package com.sblorftz.timer

import java.util.Locale
import kotlinx.serialization.Serializable

/**
 * Un livello della struttura. Small blind a 0 = pausa per il cambio fiches
 * (100 → 500): non ha bui e non conta nella numerazione dei livelli.
 */
@Serializable
data class Level(
    val smallBlind: Int,
    val bigBlind: Int,
    val minutes: Int,
) {
    val isChipChange: Boolean get() = smallBlind == 0
    val durationMs: Long get() = minutes * 60_000L

    companion object {
        fun chipChange(minutes: Int = 5) = Level(0, 0, minutes)
        fun blinds(smallBlind: Int, minutes: Int) = Level(smallBlind, smallBlind * 2, minutes)
    }
}

/** Stato del torneo salvato a ogni secondo, per riprenderlo dopo un riavvio. */
@Serializable
data class Session(
    val levelIndex: Int,
    val remainingMs: Long,
    val playedMs: Long,
    val checkpointIndex: Int? = null,
    val checkpointRemainingMs: Long? = null,
)

val DefaultStructure: List<Level> = listOf(
    Level.blinds(100, 15),
    Level.blinds(200, 15),
    Level.blinds(300, 15),
    Level.blinds(400, 15),
    Level.blinds(500, 15),
    Level.blinds(600, 10),
    Level.blinds(700, 10),
    Level.blinds(800, 10),
    Level.blinds(900, 10),
    Level.chipChange(5),
    Level.blinds(1000, 10),
    Level.blinds(2000, 10),
    Level.blinds(3000, 10),
    Level.blinds(4000, 10),
    Level.blinds(5000, 10),
    Level.blinds(10000, 10),
    Level.blinds(15000, 10),
    Level.blinds(20000, 10),
    Level.blinds(25000, 10),
)

/** Numero del livello visto dai giocatori: i cambi fiches non contano. */
fun List<Level>.displayNumber(index: Int): Int =
    take(index + 1).count { !it.isChipChange }

fun List<Level>.blindLevelCount(): Int = count { !it.isChipChange }

/** Passo "intelligente" per modificare lo small blind col telecomando. */
fun blindStep(value: Int, up: Boolean): Int {
    val ref = if (up) value else value - 1
    return when {
        ref < 1_000 -> 100
        ref < 5_000 -> 500
        ref < 20_000 -> 1_000
        else -> 5_000
    }
}

/** 1000 → "1.000", come si legge in Italia. */
fun formatChips(value: Int): String =
    String.format(Locale.US, "%,d", value).replace(',', '.')
