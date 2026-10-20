package com.novelforge.app.ui

import android.graphics.Paint
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.*
import androidx.compose.ui.unit.dp
import com.novelforge.app.data.EntrySummary
import kotlin.math.*

@Composable
fun InteractiveConnections(
    entries: List<EntrySummary>,
    links: List<Pair<Long, Long>>,
    onOpen: (Long) -> Unit,
) {
    val ids = links.flatMap { listOf(it.first, it.second) }.toSet()
    val nodes = entries.filter { it.id in ids }.take(40)
    var scale by remember { mutableFloatStateOf(1f) }
    var pan by remember { mutableStateOf(Offset.Zero) }
    val positions = remember { mutableMapOf<Long, Offset>() }
    val accent = MaterialTheme.colorScheme.primary
    val line = MaterialTheme.colorScheme.outline
    val text = MaterialTheme.colorScheme.onSurface
    Column {
        Canvas(
            Modifier.fillMaxWidth()
                .height(300.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.surface)
                .semantics {
                    contentDescription =
                        "Interactive graph of ${nodes.size} files. Accessible link list follows."
                }
                .pointerInput(nodes) {
                    detectTransformGestures { _, delta, zoom, _ ->
                        scale = (scale * zoom).coerceIn(.5f, 3f)
                        pan += delta
                    }
                }
                .pointerInput(nodes, scale, pan) {
                    detectTapGestures { tap ->
                        positions.entries
                            .minByOrNull { (it.value - tap).getDistance() }
                            ?.takeIf { (it.value - tap).getDistance() < 32.dp.toPx() }
                            ?.let { onOpen(it.key) }
                    }
                }
        ) {
            positions.clear()
            val center = Offset(size.width / 2, size.height / 2) + pan
            val radius = min(size.width, size.height) * .32f * scale
            nodes.forEachIndexed { i, n ->
                val angle = 2 * PI * i / nodes.size - PI / 2
                positions[n.id] =
                    center +
                        Offset((cos(angle) * radius).toFloat(), (sin(angle) * radius).toFloat())
            }
            links.forEach { (a, b) ->
                val start = positions[a]
                val end = positions[b]
                if (start != null && end != null)
                    drawLine(line.copy(alpha = .5f), start, end, 1.5.dp.toPx())
            }
            nodes.forEach { n ->
                val p = positions.getValue(n.id)
                drawCircle(accent.copy(alpha = .15f), 17.dp.toPx(), p)
                drawCircle(accent, 7.dp.toPx(), p)
                drawContext.canvas.nativeCanvas.drawText(
                    n.title.take(16),
                    p.x,
                    p.y + 30.dp.toPx(),
                    Paint(Paint.ANTI_ALIAS_FLAG).apply {
                        color = text.toArgb()
                        textSize = 11.dp.toPx()
                        textAlign = Paint.Align.CENTER
                    },
                )
            }
        }
        Row {
            TextButton(
                onClick = {
                    scale = 1f
                    pan = Offset.Zero
                }
            ) {
                Text("Reset view")
            }
            Spacer(Modifier.weight(1f))
            Text(
                "${nodes.size} files · ${links.size} links",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(14.dp),
            )
        }
        if (ids.size > 40)
            Text(
                "Map shows the first 40 connected files. All references are listed below.",
                style = MaterialTheme.typography.bodySmall,
            )
    }
}
