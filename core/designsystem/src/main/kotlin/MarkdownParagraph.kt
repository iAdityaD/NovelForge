package com.novelforge.app.ui
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.*
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun MarkdownParagraph(source: String, size: Float, serif: Boolean, onLink: (Long) -> Unit) {
    val heading = source.startsWith("#")
    val quote = source.startsWith(">")
    val text =
        source
            .replace(Regex("^#{1,6}\\s+"), "")
            .replace(Regex("(?m)^- \\[ \\] "), "☐ ")
            .replace(Regex("(?m)^- \\[x\\] "), "☑ ")
            .replace(Regex("(?m)^- "), "• ")
            .replace(Regex("(?m)^> ?"), "")
            .replace("[!NOTE]", "NOTE")
    val accent = MaterialTheme.colorScheme.primary
    val annotated = buildAnnotatedString {
        val regex = Regex("\\[\\[(\\d+)\\|([^]]+)]]|\\*\\*(.+?)\\*\\*|\\*([^*]+)\\*")
        var start = 0
        regex.findAll(text).forEach { m ->
            append(text.substring(start, m.range.first))
            when {
                m.groupValues[1].isNotBlank() -> {
                    pushStringAnnotation("entry", m.groupValues[1])
                    withStyle(SpanStyle(color = accent, fontWeight = FontWeight.Medium)) {
                        append(m.groupValues[2])
                    }
                    pop()
                }
                m.groupValues[3].isNotBlank() ->
                    withStyle(SpanStyle(fontWeight = FontWeight.Bold)) { append(m.groupValues[3]) }
                else ->
                    withStyle(SpanStyle(fontStyle = FontStyle.Italic)) { append(m.groupValues[4]) }
            }
            start = m.range.last + 1
        }
        append(text.substring(start))
    }
    ClickableText(
        modifier =
            if (quote)
                Modifier.fillMaxWidth()
                    .background(
                        MaterialTheme.colorScheme.primaryContainer,
                        androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
                    )
                    .padding(16.dp)
            else Modifier,
        text = annotated,
        style =
            TextStyle(
                color = MaterialTheme.colorScheme.onBackground,
                fontFamily = if (serif) FontFamily.Serif else FontFamily.SansSerif,
                fontSize = (if (heading) size + 5 else size).sp,
                lineHeight = (size * 1.75f).sp,
                fontWeight = if (heading) FontWeight.Medium else FontWeight.Normal,
            ),
        onClick = { offset ->
            annotated
                .getStringAnnotations("entry", offset, offset)
                .firstOrNull()
                ?.item
                ?.toLongOrNull()
                ?.let(onLink)
        },
    )
}
