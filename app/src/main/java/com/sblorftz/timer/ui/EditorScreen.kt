package com.sblorftz.timer.ui

import android.view.KeyEvent
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.Border
import androidx.tv.material3.ClickableSurfaceDefaults
import androidx.tv.material3.LocalContentColor
import androidx.tv.material3.Surface
import androidx.tv.material3.Text
import com.sblorftz.timer.DefaultStructure
import com.sblorftz.timer.Level
import com.sblorftz.timer.TournamentViewModel
import com.sblorftz.timer.blindLevelCount
import com.sblorftz.timer.blindStep
import com.sblorftz.timer.displayNumber
import com.sblorftz.timer.formatChips

private const val TABLE_WIDTH = 1480
private const val BLINDS_WIDTH = 720
private const val MINUTES_WIDTH = 412

private enum class Field { Blinds, Minutes }

private sealed interface EditorDialog {
    data class Menu(val index: Int) : EditorDialog
    data class Delete(val index: Int) : EditorDialog
    data object Restore : EditorDialog
}

@Composable
fun EditorScreen(vm: TournamentViewModel, onDone: () -> Unit) {
    var editing by remember { mutableStateOf<Pair<Int, Field>?>(null) }
    var dialog by remember { mutableStateOf<EditorDialog?>(null) }
    var focusRow by remember { mutableStateOf<Int?>(0) }
    val listState = rememberLazyListState()
    val structure = vm.structure

    // La struttura si salva a ogni modifica: Indietro è l'unico modo di uscire.
    BackHandler {
        when {
            dialog != null -> dialog = null
            editing != null -> editing = null
            else -> onDone()
        }
    }

    fun update(index: Int, level: Level) {
        vm.updateStructure(structure.toMutableList().also { it[index] = level })
    }

    fun append() {
        val last = structure.lastOrNull { !it.isChipChange }
        vm.updateStructure(structure + Level.blinds((last?.smallBlind ?: 50) * 2, last?.minutes ?: 15))
        focusRow = null
    }

    fun insertBelow(index: Int, level: Level) {
        vm.updateStructure(structure.toMutableList().also { it.add(index + 1, level) })
        dialog = null
        editing = null
        focusRow = index + 1
    }

    // Dopo "+ Aggiungi livello" la riga nuova resta in vista, col focus sulla riga tratteggiata.
    LaunchedEffect(structure.size) {
        if (focusRow == null) listState.animateScrollToItem(structure.size)
    }

    TableBackground {
        Box(Modifier.fillMaxSize().padding(top = 56.dp, bottom = 48.dp), contentAlignment = Alignment.TopCenter) {
        Column(Modifier.width(TABLE_WIDTH.dp).fillMaxHeight()) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
                Column(Modifier.weight(1f)) {
                    Brand()
                    Spacer(Modifier.height(28.dp))
                    Text("Struttura", style = tight(Display, 96.sp, FontWeight.Black, lineHeight = 1f), maxLines = 1)
                    Spacer(Modifier.height(14.dp))
                    Text(
                        "${structure.blindLevelCount()} livelli · ${formatDuration(structure.sumOf { it.durationMs })} in tutto · si salva da sola",
                        style = label(24, Palette.Muted, 0.02f, FontWeight.Medium),
                    )
                }
                PhonePanel(vm)
            }

            Spacer(Modifier.height(40.dp))
            Row(Modifier.padding(horizontal = 24.dp)) {
                Text("LIVELLO", style = label(20), modifier = Modifier.width(260.dp))
                Text("PICCOLO / GRANDE BUIO", style = label(20), modifier = Modifier.width((BLINDS_WIDTH + 40).dp))
                Text("DURATA", style = label(20), modifier = Modifier.width(MINUTES_WIDTH.dp))
            }
            Spacer(Modifier.height(12.dp))

            LazyColumn(
                state = listState,
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 24.dp, top = 6.dp),
                modifier = Modifier.fillMaxWidth().weight(1f),
            ) {
                itemsIndexed(structure) { index, level ->
                    val requester = remember { FocusRequester() }
                    LaunchedEffect(focusRow, structure.size) {
                        if (focusRow == index) {
                            runCatching { requester.requestFocus() }
                            focusRow = -1
                        }
                    }
                    LevelRow(
                        level = level,
                        number = if (level.isChipChange) null else structure.displayNumber(index),
                        current = vm.hasSavedSession && vm.levelIndex == index,
                        editing = editing?.takeIf { it.first == index }?.second,
                        firstCell = requester,
                        onEdit = { field -> editing = if (editing == index to field) null else index to field },
                        onChange = { update(index, it) },
                        onMenu = {
                            editing = null
                            dialog = EditorDialog.Menu(index)
                        },
                    )
                }
                item(key = "add") { AddRow(::append) }
            }

            Spacer(Modifier.height(20.dp))
            Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                Hints("OK" to "modifica", "☰" to "opzioni livello", "INDIETRO" to "salva ed esci")
            }
        }
        }

        when (val d = dialog) {
            is EditorDialog.Menu -> {
                val level = structure[d.index]
                Overlay(
                    title = if (level.isChipChange) "Cambio fiches" else "Livello ${structure.displayNumber(d.index)}",
                    subtitle = if (level.isChipChange) "${level.minutes} min"
                    else "${formatChips(level.smallBlind)} / ${formatChips(level.bigBlind)} · ${level.minutes} min",
                    vertical = true,
                    buttonWidth = 680,
                    actions = listOfNotNull(
                        DialogAction("Nuovo livello sotto") {
                            val base = structure.subList(0, d.index + 1).lastOrNull { !it.isChipChange }
                            val sb = base?.let { it.smallBlind + blindStep(it.smallBlind, true) } ?: 100
                            insertBelow(d.index, Level.blinds(sb, base?.minutes ?: 15))
                        },
                        DialogAction("Cambio fiches sotto") { insertBelow(d.index, Level.chipChange()) },
                        if (structure.size > 1) DialogAction("Elimina", danger = true) { dialog = EditorDialog.Delete(d.index) } else null,
                        DialogAction("Ripristina predefinita") { dialog = EditorDialog.Restore },
                    ),
                )
            }
            is EditorDialog.Delete -> {
                val level = structure[d.index]
                Overlay(
                    title = if (level.isChipChange) "Eliminare il cambio fiches?"
                    else "Eliminare il livello ${structure.displayNumber(d.index)}?",
                    subtitle = if (level.isChipChange) "${level.minutes} min"
                    else "${formatChips(level.smallBlind)} / ${formatChips(level.bigBlind)} · ${level.minutes} min",
                    actions = listOf(
                        DialogAction("Annulla") { dialog = null },
                        DialogAction("Elimina", danger = true) {
                            vm.updateStructure(structure.toMutableList().also { it.removeAt(d.index) })
                            dialog = null
                            editing = null
                            focusRow = d.index.coerceAtMost(structure.size - 2)
                        },
                    ),
                )
            }
            EditorDialog.Restore -> Overlay(
                title = "Ripristinare la predefinita?",
                subtitle = "La struttura attuale viene sostituita dai 19 livelli originali.",
                actions = listOf(
                    DialogAction("Annulla") { dialog = null },
                    DialogAction("Ripristina", danger = true) {
                        vm.updateStructure(DefaultStructure)
                        dialog = null
                        editing = null
                        focusRow = 0
                    },
                ),
            )
            null -> Unit
        }
    }
}

@Composable
private fun LevelRow(
    level: Level,
    number: Int?,
    current: Boolean,
    editing: Field?,
    firstCell: FocusRequester,
    onEdit: (Field) -> Unit,
    onChange: (Level) -> Unit,
    onMenu: () -> Unit,
) {
    Row(
        Modifier
            .padding(horizontal = 24.dp)
            // ☰ su una qualsiasi cella della riga apre le opzioni del livello.
            .onPreviewKeyEvent { e ->
                if (e.nativeKeyEvent.keyCode != KeyEvent.KEYCODE_MENU) return@onPreviewKeyEvent false
                if (e.type == KeyEventType.KeyDown && e.nativeKeyEvent.repeatCount == 0) onMenu()
                true
            },
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(Modifier.width(260.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(
                if (number != null) "$number" else "—",
                style = tight(Display, 44.sp, FontWeight.ExtraBold, if (current) Palette.Gold else Palette.Cream, lineHeight = 1f),
                modifier = Modifier.width(80.dp),
            )
            if (current) Text("IN CORSO", style = label(18, Palette.Gold, 0.25f))
        }

        if (level.isChipChange) {
            Cell(
                text = "CAMBIO FICHES",
                width = BLINDS_WIDTH,
                editing = false,
                modifier = Modifier.focusRequester(firstCell),
                warm = true,
                onClick = onMenu,
            )
        } else {
            Cell(
                text = "${formatChips(level.smallBlind)} / ${formatChips(level.bigBlind)}",
                width = BLINDS_WIDTH,
                editing = editing == Field.Blinds,
                modifier = Modifier.focusRequester(firstCell),
                onClick = { onEdit(Field.Blinds) },
                onStep = { up ->
                    val sb = (level.smallBlind + (if (up) 1 else -1) * blindStep(level.smallBlind, up))
                        .coerceIn(100, 1_000_000)
                    onChange(Level.blinds(sb, level.minutes))
                },
            )
        }
        Spacer(Modifier.width(40.dp))
        Cell(
            text = "${level.minutes} min",
            width = MINUTES_WIDTH,
            editing = editing == Field.Minutes,
            onClick = { onEdit(Field.Minutes) },
            onStep = { up ->
                onChange(level.copy(minutes = (level.minutes + if (up) 1 else -1).coerceIn(1, 120)))
            },
        )
    }
}

/** Ultima riga della tabella, tratteggiata: OK aggiunge un livello in fondo. */
@Composable
private fun AddRow(onClick: () -> Unit) {
    val shape = RoundedCornerShape(12.dp)
    Surface(
        onClick = onClick,
        modifier = Modifier
            .padding(start = (24 + 260).dp)
            .width((BLINDS_WIDTH + 40 + MINUTES_WIDTH).dp)
            .drawBehind {
                drawRoundRect(
                    color = Palette.Gold.copy(alpha = 0.35f),
                    cornerRadius = CornerRadius(12.dp.toPx()),
                    style = Stroke(width = 2.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(14f, 10f))),
                )
            },
        shape = ClickableSurfaceDefaults.shape(shape),
        colors = ClickableSurfaceDefaults.colors(
            containerColor = Color.Transparent,
            contentColor = Palette.Gold.copy(alpha = 0.8f),
            focusedContainerColor = Palette.Gold.copy(alpha = 0.22f),
            focusedContentColor = Palette.Cream,
        ),
        border = ClickableSurfaceDefaults.border(
            focusedBorder = Border(BorderStroke(3.dp, Palette.Gold), shape = shape),
        ),
        scale = ClickableSurfaceDefaults.scale(focusedScale = 1.02f),
    ) {
        Box(Modifier.fillMaxWidth().padding(vertical = 22.dp), contentAlignment = Alignment.Center) {
            Text("+  AGGIUNGI LIVELLO", style = label(24, LocalContentColor.current, 0.3f, FontWeight.Bold))
        }
    }
}

/**
 * Cella della tabella. OK entra in modifica, ▲▼ cambiano il valore,
 * OK o Indietro confermano. In modifica il focus non scappa dalla cella.
 */
@Composable
private fun Cell(
    text: String,
    width: Int,
    editing: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    onStep: ((Boolean) -> Unit)? = null,
    warm: Boolean = false,
) {
    val accent = Palette.Gold
    val shape = RoundedCornerShape(12.dp)
    Surface(
        onClick = onClick,
        modifier = modifier
            .width(width.dp)
            .onPreviewKeyEvent { e ->
                if (!editing || onStep == null) return@onPreviewKeyEvent false
                when (e.nativeKeyEvent.keyCode) {
                    KeyEvent.KEYCODE_DPAD_UP, KeyEvent.KEYCODE_DPAD_DOWN -> {
                        if (e.type == KeyEventType.KeyDown) onStep(e.nativeKeyEvent.keyCode == KeyEvent.KEYCODE_DPAD_UP)
                        true
                    }
                    KeyEvent.KEYCODE_DPAD_LEFT, KeyEvent.KEYCODE_DPAD_RIGHT -> true
                    else -> false
                }
            },
        shape = ClickableSurfaceDefaults.shape(shape),
        colors = ClickableSurfaceDefaults.colors(
            containerColor = if (editing) accent else Color.White.copy(alpha = 0.04f),
            contentColor = if (editing) Palette.FeltDark else if (warm) Palette.GoldLight else Palette.Cream,
            focusedContainerColor = if (editing) accent else accent.copy(alpha = 0.22f),
            focusedContentColor = if (editing) Palette.FeltDark else Palette.Cream,
        ),
        border = ClickableSurfaceDefaults.border(
            border = Border(BorderStroke(1.dp, Color.White.copy(alpha = 0.06f)), shape = shape),
            focusedBorder = Border(BorderStroke(3.dp, accent), shape = shape),
        ),
        scale = ClickableSurfaceDefaults.scale(focusedScale = 1.03f),
    ) {
        Box(
            Modifier
                .fillMaxWidth()
                .padding(vertical = 14.dp, horizontal = 24.dp),
            contentAlignment = Alignment.CenterStart,
        ) {
            Text(
                if (editing) "▲▼  $text" else text,
                style = tight(Display, 44.sp, FontWeight.ExtraBold, LocalContentColor.current, lineHeight = 1.1f),
            )
        }
    }
}
