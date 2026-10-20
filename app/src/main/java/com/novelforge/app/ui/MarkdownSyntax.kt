package com.novelforge.app.ui

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.*

class MarkdownSyntax(private val accent: Color) : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val styled = buildAnnotatedString {
            append(text)
            Regex("(?m)^#{1,6} .+$|^>.*$|\\[\\[[^]]+]]").findAll(text.text).forEach {
                addStyle(SpanStyle(color = accent), it.range.first, it.range.last + 1)
            }
            Regex("\\*\\*[^*]+\\*\\*").findAll(text.text).forEach {
                addStyle(SpanStyle(fontWeight = FontWeight.Bold), it.range.first, it.range.last + 1)
            }
            Regex("(?<!\\*)\\*[^*]+\\*(?!\\*)").findAll(text.text).forEach {
                addStyle(SpanStyle(fontStyle = FontStyle.Italic), it.range.first, it.range.last + 1)
            }
        }
        return TransformedText(styled, OffsetMapping.Identity)
    }
}
