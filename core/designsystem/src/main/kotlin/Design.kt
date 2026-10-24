package com.novelforge.app.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.*
import com.novelforge.core.model.Book
import kotlin.math.*

val Pine = Color(0xFF7357E8)
val Gold = Color(0xFF7254CE)
val Paper = Color(0xFFF6F7FB)
val Ink = Color(0xFF1B1C24)
val Muted = Color(0xFF616574)
val Line = Color(0xFFE5E6EE)
private val Light =
    lightColorScheme(
        primary = Pine,
        onPrimary = Color.White,
        primaryContainer = Color(0xFFEAE4FF),
        onPrimaryContainer = Color(0xFF402690),
        secondary = Gold,
        secondaryContainer = Color(0xFFEAE4FF),
        onSecondaryContainer = Color(0xFF402690),
        background = Paper,
        onBackground = Ink,
        surface = Color.White,
        onSurface = Ink,
        surfaceVariant = Color(0xFFEEF0F7),
        onSurfaceVariant = Muted,
        outlineVariant = Line,
    )
private val Dark =
    darkColorScheme(
        primary = Color(0xFFB99AF7),
        onPrimary = Color(0xFF211045),
        primaryContainer = Color(0xFF302744),
        onPrimaryContainer = Color(0xFFE4D9FF),
        secondary = Color(0xFFB99AF7),
        secondaryContainer = Color(0xFF302744),
        onSecondaryContainer = Color(0xFFE4D9FF),
        background = Color(0xFF17171C),
        onBackground = Color(0xFFE8E5ED),
        surface = Color(0xFF24242C),
        onSurface = Color(0xFFE8E5ED),
        surfaceVariant = Color(0xFF2D2D36),
        onSurfaceVariant = Color(0xFFA5A2AE),
        outlineVariant = Color(0xFF383842),
    )

@Composable
fun ForgeTheme(dark: Boolean, content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (dark) Dark else Light,
        typography =
            Typography(
                displaySmall =
                    androidx.compose.ui.text.TextStyle(
                        fontFamily = FontFamily.SansSerif,
                        fontSize = 30.sp,
                        lineHeight = 42.sp,
                    ),
                headlineLarge =
                    androidx.compose.ui.text.TextStyle(
                        fontFamily = FontFamily.SansSerif,
                        fontSize = 30.sp,
                        lineHeight = 39.sp,
                    ),
                headlineMedium =
                    androidx.compose.ui.text.TextStyle(
                        fontFamily = FontFamily.SansSerif,
                        fontSize = 24.sp,
                        lineHeight = 34.sp,
                    ),
                titleLarge =
                    androidx.compose.ui.text.TextStyle(
                        fontFamily = FontFamily.SansSerif,
                        fontSize = 20.sp,
                        lineHeight = 29.sp,
                    ),
                titleMedium =
                    androidx.compose.ui.text.TextStyle(
                        fontSize = 16.sp,
                        lineHeight = 22.sp,
                        fontWeight = FontWeight.Medium,
                    ),
                bodyLarge =
                    androidx.compose.ui.text.TextStyle(fontSize = 16.sp, lineHeight = 25.sp),
                bodyMedium =
                    androidx.compose.ui.text.TextStyle(fontSize = 14.sp, lineHeight = 21.sp),
                labelSmall =
                    androidx.compose.ui.text.TextStyle(
                        fontSize = 10.sp,
                        lineHeight = 15.sp,
                        letterSpacing = .8.sp,
                        fontWeight = FontWeight.Medium,
                    ),
            ),
        content = content,
    )
}

@Composable
fun Eyebrow(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.secondary,
) {
    Text(text.uppercase(), modifier, style = MaterialTheme.typography.labelSmall, color = color)
}

@Composable
fun SectionTitle(
    title: String,
    subtitle: String? = null,
    action: String? = null,
    onAction: () -> Unit = {},
) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleLarge)
            subtitle?.let {
                Text(
                    it,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        if (action != null) TextButton(onClick = onAction) { Text(action) }
    }
}

@Composable
fun ActionIcon(icon: ImageVector, label: String, onClick: () -> Unit, enabled: Boolean = true) {
    IconButton(onClick = onClick, enabled = enabled) { Icon(icon, label, Modifier.size(22.dp)) }
}

@Composable
fun SurfaceCard(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(16.dp))
            .padding(20.dp),
        content = content,
    )
}

@Composable
fun StatusPill(status: String) {
    val color =
        when (status) {
            "Final" -> MaterialTheme.colorScheme.primary
            "Revised" -> Color(0xFF6472A1)
            "In progress" -> MaterialTheme.colorScheme.secondary
            else -> MaterialTheme.colorScheme.onSurfaceVariant
        }
    Text(
        status,
        Modifier.clip(RoundedCornerShape(6.dp))
            .background(color.copy(alpha = .1f))
            .padding(horizontal = 8.dp, vertical = 4.dp),
        color = color,
        style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 0.sp),
    )
}

fun kindIcon(kind: String): ImageVector =
    when (kind) {
        "Character" -> Icons.Outlined.PersonOutline
        "Location" -> Icons.Outlined.Place
        "Arc" -> Icons.Outlined.Route
        "Plot thread" -> Icons.Outlined.AccountTree
        "Screenplay" -> Icons.Outlined.Movie
        "Backstory" -> Icons.Outlined.History
        "Worldbuilding" -> Icons.Outlined.Public
        "Research" -> Icons.Outlined.TravelExplore
        "Folder" -> Icons.Outlined.Folder
        "Chapter" -> Icons.Outlined.Description
        else -> Icons.Outlined.StickyNote2
    }

@Composable
fun EmptyState(
    title: String,
    description: String,
    icon: ImageVector = Icons.Outlined.AutoStories,
    action: String? = null,
    onAction: () -> Unit = {},
) {
    Column(
        Modifier.fillMaxWidth().padding(vertical = 36.dp, horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Box(
            Modifier.size(68.dp)
                .clip(RoundedCornerShape(22.dp))
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center,
        ) {
            Icon(icon, null, Modifier.size(30.dp), tint = MaterialTheme.colorScheme.primary)
        }
        Text(title, style = MaterialTheme.typography.titleLarge)
        Text(
            description,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
        )
        if (action != null) FilledTonalButton(onClick = onAction) { Text(action) }
    }
}

@Composable
fun BookCover(book: Book, modifier: Modifier = Modifier, compact: Boolean = false) {
    val accents = listOf(Color(0xFF7460C4), Color(0xFF337F96), Color(0xFFB16C50), Color(0xFF577D68))
    val accent = accents[book.cover.coerceIn(0, 3)]
    BoxWithConstraints(
        modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Brush.linearGradient(listOf(accent.copy(alpha = .7f), Color(0xFF202336))))
    ) {
        val coverWidth = maxWidth
        Canvas(Modifier.fillMaxSize()) {
            repeat(7) { i ->
                val x = size.width * (.3f + i * .16f)
                drawLine(
                    Color.White.copy(alpha = .09f),
                    Offset(x, 0f),
                    Offset(x - size.width * .8f, size.height),
                    size.width * .045f,
                )
            }
            drawCircle(
                accent.copy(alpha = .45f),
                size.width * .6f,
                Offset(size.width * .95f, size.height * .8f),
            )
        }
        Column(
            Modifier.fillMaxSize().padding(maxWidth * .1f),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Icon(
                Icons.Outlined.AutoStories,
                null,
                Modifier.size(if (compact) 16.dp else 22.dp),
                tint = Color.White.copy(alpha = .85f),
            )
            if (!compact)
                Column {
                    Text(
                        book.title.replace('\n', ' '),
                        fontSize = (coverWidth.value * .115f).coerceIn(12f, 23f).sp,
                        lineHeight = (coverWidth.value * .14f).coerceIn(16f, 28f).sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White,
                        maxLines = 4,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Spacer(Modifier.height(9.dp))
                    Text(
                        book.author.ifBlank { book.genre },
                        fontSize = 10.sp,
                        color = Color.White.copy(alpha = .75f),
                        maxLines = 1,
                    )
                }
            else
                Text(
                    book.title.take(1).uppercase(),
                    fontSize = (coverWidth.value * .3f).sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                )
        }
    }
}
