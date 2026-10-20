package com.novelforge.app.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.novelforge.app.data.EntrySummary
import kotlin.math.*

@Composable
fun ConnectionMap(entries: List<EntrySummary>, links: List<Pair<Long, Long>>) {
    val connected = links.flatMap { listOf(it.first, it.second) }.toSet()
    val nodes = entries.filter { it.id in connected }.take(40)
    val ink = MaterialTheme.colorScheme.primary
    val line = MaterialTheme.colorScheme.outlineVariant
    if (nodes.isEmpty()) return
    Column {
        Canvas(
            Modifier.fillMaxWidth().height(210.dp).semantics {
                contentDescription =
                    "${connected.size} connected pages, ${links.size} links. Page titles and navigation follow below."
            }
        ) {
            val radius = min(size.width, size.height) * .39f
            val points =
                nodes
                    .mapIndexed { i, e ->
                        val angle = 2 * PI * i / nodes.size - PI / 2
                        e.id to
                            Offset(
                                size.width / 2 + (cos(angle) * radius).toFloat(),
                                size.height / 2 + (sin(angle) * radius).toFloat(),
                            )
                    }
                    .toMap()
            links.forEach { (a, b) ->
                val start = points[a]
                val end = points[b]
                if (start != null && end != null) drawLine(line, start, end, 2.dp.toPx())
            }
            points.values.forEach { drawCircle(ink, 6.dp.toPx(), it) }
        }
        Text(
            "${connected.size} connected pages · ${links.size} links",
            style = MaterialTheme.typography.bodySmall,
        )
        if (connected.size > 40)
            Text(
                "Map shows the first 40 connected pages. All links are listed below.",
                style = MaterialTheme.typography.bodySmall,
            )
    }
}
