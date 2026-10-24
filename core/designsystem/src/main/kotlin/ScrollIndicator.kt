package com.novelforge.app.ui

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.unit.dp

/** A quiet position cue; native scrolling and accessibility remain owned by LazyColumn. */
@Composable
fun Modifier.scrollIndicator(state: LazyListState): Modifier {
    val color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = .45f)
    return drawWithContent {
        drawContent()
        val layout = state.layoutInfo
        val total = layout.totalItemsCount
        val visible = layout.visibleItemsInfo.size
        if (total > visible && visible > 0) {
            val first = layout.visibleItemsInfo.first()
            val fraction = state.firstVisibleItemIndex + state.firstVisibleItemScrollOffset.toFloat() / first.size.coerceAtLeast(1)
            val thumb = (size.height * visible / total.toFloat()).coerceIn(24.dp.toPx(), size.height)
            val top = ((size.height - thumb) * fraction / (total - visible).coerceAtLeast(1)).coerceIn(0f, size.height - thumb)
            drawRoundRect(color, Offset(size.width - 3.dp.toPx(), top), Size(2.dp.toPx(), thumb), CornerRadius(2.dp.toPx()))
        }
    }
}
