package com.sblorftz.timer.ui

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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
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
 * Riquadro "Usa lo smartphone" in alto a destra nella Struttura. Il server resta
 * acceso finché la schermata è aperta; ogni salvataggio dal telefono arriva subito in tabella.
 */
@Composable
fun PhonePanel(vm: TournamentViewModel, modifier: Modifier = Modifier) {
    var savedAt by remember { mutableStateOf<String?>(null) }
    val server = remember {
        StructureServer { levels ->
            vm.updateStructure(levels)
            savedAt = SimpleDateFormat("HH:mm", Locale.ITALY).format(Date())
        }
    }
    val url = remember { server.start() }
    DisposableEffect(Unit) { onDispose { server.stop() } }

    // Il telefono vede sempre la struttura attuale, anche se cambiata dal telecomando.
    LaunchedEffect(vm.structure, vm.levelIndex, vm.hasSavedSession) {
        server.levels = vm.structure
        server.currentIndex = if (vm.hasSavedSession) vm.levelIndex else null
    }

    Row(
        modifier
            .background(Color.White.copy(alpha = 0.03f), RoundedCornerShape(22.dp))
            .border(1.dp, Palette.Gold.copy(alpha = 0.25f), RoundedCornerShape(22.dp))
            .padding(start = 30.dp, end = 22.dp, top = 20.dp, bottom = 20.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.width(300.dp)) {
            Text("Usa lo smartphone", style = tight(Display, 38.sp, FontWeight.ExtraBold, Palette.Gold, lineHeight = 1.05f))
            Spacer(Modifier.height(10.dp))
            Text(
                when {
                    url == null -> "Collega la TV alla wifi di casa per modificare la struttura dal telefono."
                    savedAt != null -> "✓  Salvata dal telefono alle $savedAt"
                    else -> "Inquadra il QR code e modifica la struttura dal telefono."
                },
                style = label(20, if (savedAt != null) Palette.GoldLight else Palette.Muted, 0.01f, FontWeight.Medium),
            )
        }
        if (url != null) {
            Spacer(Modifier.width(28.dp))
            val matrix = remember(url) { qrMatrix(url) }
            Box(Modifier.size(170.dp).background(Color.White, RoundedCornerShape(12.dp)).padding(10.dp)) {
                QrCode(matrix)
            }
        }
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
