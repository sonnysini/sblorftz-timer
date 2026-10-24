package com.sblorftz.timer.ui

import android.view.KeyEvent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.Text
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.common.BitMatrix
import com.google.zxing.qrcode.QRCodeWriter
import com.sblorftz.timer.TournamentViewModel
import com.sblorftz.timer.remote.StructureServer
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * QR per modificare la struttura dal telefono. Il server resta acceso finché
 * questa finestra è aperta; ogni salvataggio dal telefono arriva subito in tabella.
 */
@Composable
fun PhoneEditor(vm: TournamentViewModel, onClose: () -> Unit) {
    var savedAt by remember { mutableStateOf<String?>(null) }
    val server = remember {
        StructureServer { levels ->
            vm.updateStructure(levels)
            savedAt = SimpleDateFormat("HH:mm:ss", Locale.ITALY).format(Date())
        }
    }
    val url = remember { server.start() }
    DisposableEffect(Unit) { onDispose { server.stop() } }

    // Il telefono vede sempre la struttura attuale, anche se cambiata dal telecomando.
    LaunchedEffect(vm.structure, vm.levelIndex, vm.hasSavedSession) {
        server.levels = vm.structure
        server.currentIndex = if (vm.hasSavedSession) vm.levelIndex else null
    }

    val close = remember { FocusRequester() }
    LaunchedEffect(Unit) { runCatching { close.requestFocus() } }

    Box(
        Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.78f))
            // Un solo bottone: le frecce non devono portare il focus sulla tabella sotto.
            .onPreviewKeyEvent { e ->
                when (e.nativeKeyEvent.keyCode) {
                    KeyEvent.KEYCODE_DPAD_UP, KeyEvent.KEYCODE_DPAD_DOWN,
                    KeyEvent.KEYCODE_DPAD_LEFT, KeyEvent.KEYCODE_DPAD_RIGHT -> true
                    else -> false
                }
            },
        contentAlignment = Alignment.Center,
    ) {
        Row(
            Modifier
                .background(Palette.FeltDark, RoundedCornerShape(24.dp))
                .border(1.dp, Palette.Gold.copy(alpha = 0.3f), RoundedCornerShape(24.dp))
                .padding(56.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (url != null) {
                val matrix = remember(url) { qrMatrix(url) }
                Box(
                    Modifier.size(440.dp).background(Color.White, RoundedCornerShape(18.dp)).padding(24.dp),
                ) { QrCode(matrix) }
                Spacer(Modifier.width(64.dp))
            }
            Column(Modifier.width(720.dp)) {
                Text("Modifica da telefono", style = tight(Display, 72.sp, FontWeight.ExtraBold, lineHeight = 1.05f))
                Spacer(Modifier.height(28.dp))
                if (url == null) {
                    Text(
                        "La TV non è collegata alla rete di casa. Collegala alla wifi e riprova.",
                        style = label(28, Palette.Muted, 0.01f, FontWeight.Medium),
                    )
                } else {
                    Step("1", "Collega il telefono alla stessa wifi della TV")
                    Spacer(Modifier.height(16.dp))
                    Step("2", "Inquadra il QR con la fotocamera")
                    Spacer(Modifier.height(16.dp))
                    Step("3", "Modifica i livelli e premi Salva sulla TV")
                    Spacer(Modifier.height(28.dp))
                    Text(
                        url.substringBefore("/?"),
                        style = label(24, Palette.Muted2, 0.02f, FontWeight.Medium),
                    )
                    Spacer(Modifier.height(20.dp))
                    Text(
                        savedAt?.let { "✓  Struttura salvata dal telefono alle $it" } ?: " ",
                        style = label(26, Palette.GoldLight, 0.02f, FontWeight.SemiBold),
                    )
                }
                Spacer(Modifier.height(40.dp))
                GoldButton("Chiudi", onClose, modifier = Modifier.focusRequester(close), width = 300)
            }
        }
    }
}

@Composable
private fun Step(number: String, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            Modifier.size(48.dp).border(2.dp, Palette.Gold, RoundedCornerShape(24.dp)),
            contentAlignment = Alignment.Center,
        ) {
            Text(number, style = tight(Display, 28.sp, FontWeight.ExtraBold, Palette.Gold, lineHeight = 1f))
        }
        Spacer(Modifier.width(20.dp))
        Text(text, style = label(28, Palette.Cream, 0.01f, FontWeight.Medium))
    }
}

private fun qrMatrix(text: String): BitMatrix =
    QRCodeWriter().encode(text, BarcodeFormat.QR_CODE, 0, 0, mapOf(EncodeHintType.MARGIN to 0))

@Composable
private fun QrCode(matrix: BitMatrix) {
    Canvas(Modifier.fillMaxSize()) {
        val cell = size.width / matrix.width
        for (y in 0 until matrix.height) {
            for (x in 0 until matrix.width) {
                if (matrix[x, y]) {
                    // +0.5 px: niente righine bianche fra i moduli per l'arrotondamento.
                    drawRect(Color.Black, Offset(x * cell, y * cell), Size(cell + 0.5f, cell + 0.5f))
                }
            }
        }
    }
}
