package com.sblorftz.timer

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaMetadataRetriever
import android.media.MediaPlayer
import android.media.SoundPool
import android.os.Handler
import android.os.Looper

/**
 * Suoni del torneo. Gli annunci vocali sono fatti a pezzi messi in fila:
 *  - a 1 minuto: "tra un minuto" + "cambio buio" / "cambio fiches";
 *  - al cambio: boom con dentro "cambio buio" / "cambio fiches", poi piccolo e grande buio.
 * I numeri sono file `vox_n_<valore>` in res/raw (senza silenzio in coda): se un numero
 * manca l'annuncio si ferma lì, senza errori.
 * Durante il cambio fiches: tic-tac a ogni secondo con un ronzio basso sotto, e il rumore
 * delle fiches quando la pila si forma nell'animazione.
 */
class Sounds(private val context: Context) {
    private val pool = SoundPool.Builder()
        .setMaxStreams(6)
        .setAudioAttributes(
            AudioAttributes.Builder()
                // MEDIA: segue il volume che si regola col telecomando della TV.
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build(),
        )
        .build()
    private val handler = Handler(Looper.getMainLooper())

    private class Clip(val id: Int, val durationMs: Long)

    private fun clip(res: Int) = Clip(pool.load(context, res, 1), durationOf(res))

    private val traUnMinuto = clip(R.raw.vox_tra_un_minuto)
    private val cambioBuio = clip(R.raw.vox_cambio_buio)
    private val cambioFiches = clip(R.raw.vox_cambio_fiches)
    private val tenSeconds = clip(R.raw.ten_seconds)
    // Boom dell'intro con la voce dentro. Le versioni normali hanno 500 ms di rincorsa prima
    // del colpo e partono in anticipo; le "now" partono dal colpo, per i cambi a mano.
    private val levelChange = clip(R.raw.level_change)
    private val levelChangeNow = clip(R.raw.level_change_now)
    private val chipChange = clip(R.raw.level_change_fiches)
    private val chipChangeNow = clip(R.raw.level_change_fiches_now)
    private val levelEnd = clip(R.raw.level_end)
    private val levelEndNow = clip(R.raw.level_end_now)
    private val tic = clip(R.raw.tic)
    private val tac = clip(R.raw.tac)
    private val piles = listOf(clip(R.raw.fiches_pila_1), clip(R.raw.fiches_pila_2))
    private var nextPile = 0
    // Pila che frena (pausa) e che riparte da ferma (ripresa).
    private val pauseClip = clip(R.raw.fiches_pausa)
    private val resumeClip = clip(R.raw.fiches_ripresa)
    private var pauseStream = 0
    private val numbers = HashMap<Int, Clip?>()

    // Il ronzio dura 20 s in loop: troppo lungo per SoundPool (limite ~1 MB decodificato).
    private val drone: MediaPlayer? = runCatching {
        MediaPlayer.create(
            context,
            R.raw.ronzio_fiches,
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build(),
            0,
        )?.apply { isLooping = true }
    }.getOrNull()

    /** "Tra un minuto" + "cambio buio"/"cambio fiches": i pezzi hanno già la pausa dell'originale. */
    fun oneMinute(toChipChange: Boolean) =
        announce(0, listOf(traUnMinuto, if (toChipChange) cambioFiches else cambioBuio), gapsMs = listOf(0L))

    fun tenSeconds() = announce(0, listOf(tenSeconds))

    /** Tic-tac del cambio fiches: si alternano secondo per secondo. */
    fun secondTick(even: Boolean) = play(if (even) tic else tac)

    /** Le fiche da 100 arrivano in pila: le due prese si alternano. */
    fun chipPile() {
        play(piles[nextPile])
        nextPile = (nextPile + 1) % piles.size
    }

    /** Pausa / ripresa dal telecomando: premendo a raffica suona solo l'ultimo. */
    fun pauseToggle(paused: Boolean) {
        if (pauseStream != 0) pool.stop(pauseStream)
        pauseStream = play(if (paused) pauseClip else resumeClip)
    }

    /** Ronzio sotto il tic-tac: acceso solo col cambio fiches in corso. */
    fun drone(on: Boolean) {
        val d = drone ?: return
        if (on && !d.isPlaying) d.start() else if (!on && d.isPlaying) d.pause()
    }

    /** Boom + voce del nuovo livello; dopo la voce, i bui (se ci sono i file dei numeri). */
    fun levelChange(to: Level, now: Boolean = false) {
        cancelQueued()
        val boom = when {
            to.isChipChange -> if (now) chipChangeNow else chipChange
            else -> if (now) levelChangeNow else levelChange
        }
        val cut = if (now) NOW_CUT_MS else 0
        val blinds = if (to.isChipChange) emptyList() else listOfNotNull(number(to.smallBlind), number(to.bigBlind))
        announce(0, listOf(boom))
        // Si dicono solo se ci sono tutti e due: mai "cambio buio: cento" senza il grande.
        if (blinds.size == 2) announce(BOOM_VOICE_END_MS - cut + GAP_VOICE_MS, blinds, gapsMs = listOf(GAP_NUMBERS_MS))
    }

    fun levelEnd(now: Boolean = false) = announce(0, listOf(if (now) levelEndNow else levelEnd))

    /** Zittisce gli annunci in coda (es. pausa o salto di livello). */
    fun cancelQueued() = handler.removeCallbacksAndMessages(null)

    /** Suona [clips] in fila partendo tra [startMs]; tra un pezzo e l'altro la pausa di [gapsMs] (l'ultima si ripete). */
    private fun announce(startMs: Long, clips: List<Clip>, gapsMs: List<Long> = listOf(0L)) {
        var at = startMs
        clips.forEachIndexed { i, c ->
            if (at <= 0) play(c) else handler.postDelayed({ play(c) }, at)
            at += c.durationMs + gapsMs[minOf(i, gapsMs.lastIndex)]
        }
    }

    private fun number(value: Int): Clip? = numbers.getOrPut(value) {
        @Suppress("DiscouragedApi")
        val res = context.resources.getIdentifier("vox_n_$value", "raw", context.packageName)
        if (res == 0) null else clip(res)
    }

    private fun durationOf(res: Int): Long = runCatching {
        context.resources.openRawResourceFd(res).use { fd ->
            MediaMetadataRetriever().run {
                try {
                    setDataSource(fd.fileDescriptor, fd.startOffset, fd.length)
                    extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLong()
                } finally {
                    release()
                }
            }
        }
    }.getOrNull() ?: 0L

    private fun play(c: Clip): Int = pool.play(c.id, 1f, 1f, 1, 0, 1f)

    fun release() {
        cancelQueued()
        drone?.release()
        pool.release()
    }

    private companion object {
        /** Nel boom la voce ("cambio buio" da one_minute a 1130 ms) parte a 1250 ms e l'ultima parola finisce a ~2180 ms. */
        const val BOOM_VOICE_END_MS = 2_180L
        /** Le versioni "now" sono il boom senza i primi 440 ms. */
        const val NOW_CUT_MS = 440L
        const val GAP_VOICE_MS = 250L
        const val GAP_NUMBERS_MS = 150L
    }
}
