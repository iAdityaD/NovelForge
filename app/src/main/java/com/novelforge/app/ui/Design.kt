package com.novelforge.app.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.*
import com.novelforge.app.data.Book
import kotlin.math.*

val Pine = Color(0xFF244C40)
val Gold = Color(0xFF86632D)
val Paper = Color(0xFFF7F5EF)
val Ink = Color(0xFF242E29)
val Muted = Color(0xFF5F6B62)
val Line = Color(0xFFE3E5DB)
private val Light = lightColorScheme(primary=Pine, onPrimary=Color.White, primaryContainer=Color(0xFFE3EBDF), onPrimaryContainer=Pine, secondary=Gold, background=Paper, onBackground=Ink, surface=Color(0xFFFFFEFA), onSurface=Ink, surfaceVariant=Color(0xFFEEEFE6), onSurfaceVariant=Muted, outline=Color(0xFFB8C0B5), outlineVariant=Line)
private val Dark = darkColorScheme(primary=Color(0xFFA6CBB7), onPrimary=Color(0xFF10372A), primaryContainer=Color(0xFF2B4538), onPrimaryContainer=Color(0xFFCCE5D2), secondary=Color(0xFFD9BA82), background=Color(0xFF151C18), onBackground=Color(0xFFE4E7DD), surface=Color(0xFF1E2822), onSurface=Color(0xFFE4E7DD), surfaceVariant=Color(0xFF2B352E), onSurfaceVariant=Color(0xFFAAB4A9), outlineVariant=Color(0xFF35433A))
@Composable fun ForgeTheme(dark: Boolean, content: @Composable () -> Unit) {
    MaterialTheme(colorScheme=if(dark) Dark else Light, typography=Typography(
        displaySmall=androidx.compose.ui.text.TextStyle(fontFamily=FontFamily.Serif,fontSize=36.sp,lineHeight=42.sp),
        headlineLarge=androidx.compose.ui.text.TextStyle(fontFamily=FontFamily.Serif,fontSize=32.sp,lineHeight=39.sp),
        headlineMedium=androidx.compose.ui.text.TextStyle(fontFamily=FontFamily.Serif,fontSize=27.sp,lineHeight=34.sp),
        titleLarge=androidx.compose.ui.text.TextStyle(fontFamily=FontFamily.Serif,fontSize=23.sp,lineHeight=29.sp),
        titleMedium=androidx.compose.ui.text.TextStyle(fontSize=16.sp,lineHeight=22.sp,fontWeight=FontWeight.Medium),
        bodyLarge=androidx.compose.ui.text.TextStyle(fontSize=16.sp,lineHeight=25.sp),
        bodyMedium=androidx.compose.ui.text.TextStyle(fontSize=14.sp,lineHeight=21.sp),
        labelSmall=androidx.compose.ui.text.TextStyle(fontSize=10.sp,lineHeight=15.sp,letterSpacing=1.3.sp,fontWeight=FontWeight.Medium)
    ), content=content)
}
@Composable fun Eyebrow(text: String, modifier: Modifier=Modifier, color: Color=MaterialTheme.colorScheme.secondary) { Text(text.uppercase(), modifier, style=MaterialTheme.typography.labelSmall, color=color) }
@Composable fun SectionTitle(title: String, subtitle: String?=null, action: String?=null, onAction: () -> Unit={}) {
    Row(Modifier.fillMaxWidth(), verticalAlignment=Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) { Text(title,style=MaterialTheme.typography.titleLarge); subtitle?.let { Text(it,style=MaterialTheme.typography.bodySmall,color=MaterialTheme.colorScheme.onSurfaceVariant) } }
        if(action!=null) TextButton(onClick=onAction) { Text(action) }
    }
}
@Composable fun ActionIcon(icon: ImageVector, label: String, onClick: () -> Unit, enabled: Boolean=true) { IconButton(onClick=onClick,enabled=enabled) { Icon(icon,label,Modifier.size(22.dp)) } }
@Composable fun SurfaceCard(modifier: Modifier=Modifier, content: @Composable ColumnScope.() -> Unit) {
    Column(modifier.clip(RoundedCornerShape(20.dp)).background(MaterialTheme.colorScheme.surface).border(1.dp,MaterialTheme.colorScheme.outlineVariant,RoundedCornerShape(20.dp)).padding(20.dp),content=content)
}
@Composable fun StatusPill(status: String) {
    val color = when(status) { "Final" -> MaterialTheme.colorScheme.primary; "Revised" -> Color(0xFF6472A1); "In progress" -> MaterialTheme.colorScheme.secondary; else -> MaterialTheme.colorScheme.onSurfaceVariant }
    Text(status,Modifier.clip(RoundedCornerShape(6.dp)).background(color.copy(alpha=.1f)).padding(horizontal=8.dp,vertical=4.dp),color=color,style=MaterialTheme.typography.labelSmall.copy(letterSpacing=0.sp))
}
fun kindIcon(kind: String): ImageVector = when(kind) { "Character" -> Icons.Outlined.PersonOutline; "Location" -> Icons.Outlined.Place; "Arc" -> Icons.Outlined.Route; "Plot thread" -> Icons.Outlined.AccountTree; "Screenplay" -> Icons.Outlined.Movie; "Backstory" -> Icons.Outlined.History; "Worldbuilding" -> Icons.Outlined.Public; "Research" -> Icons.Outlined.TravelExplore; "Chapter" -> Icons.Outlined.Description; else -> Icons.Outlined.StickyNote2 }
@Composable fun EmptyState(title: String, description: String, icon: ImageVector=Icons.Outlined.AutoStories, action: String?=null, onAction: () -> Unit={}) {
    Column(Modifier.fillMaxWidth().padding(vertical=36.dp,horizontal=24.dp),horizontalAlignment=Alignment.CenterHorizontally,verticalArrangement=Arrangement.spacedBy(14.dp)) {
        Box(Modifier.size(68.dp).clip(RoundedCornerShape(22.dp)).background(MaterialTheme.colorScheme.primaryContainer),contentAlignment=Alignment.Center) { Icon(icon,null,Modifier.size(30.dp),tint=MaterialTheme.colorScheme.primary) }
        Text(title,style=MaterialTheme.typography.titleLarge)
        Text(description,style=MaterialTheme.typography.bodyMedium,color=MaterialTheme.colorScheme.onSurfaceVariant,textAlign=androidx.compose.ui.text.style.TextAlign.Center)
        if(action!=null) FilledTonalButton(onClick=onAction) { Text(action) }
    }
}
@Composable fun BookCover(book: Book, modifier: Modifier=Modifier, compact: Boolean=false) {
    val palettes = listOf(listOf(Color(0xFF183D38),Color(0xFF44695B)),listOf(Color(0xFF29374F),Color(0xFF6F798D)),listOf(Color(0xFF704337),Color(0xFFA37C59)),listOf(Color(0xFF554766),Color(0xFF928096)))
    val palette = palettes[book.cover.coerceIn(0,3)]
    BoxWithConstraints(modifier.clip(RoundedCornerShape(10.dp)).background(Brush.verticalGradient(palette))) {
        val coverType = (maxWidth.value * .105f).coerceIn(8f,22f)
        Canvas(Modifier.fillMaxSize()) {
            val w=size.width; val h=size.height
            drawRect(Color.Black.copy(alpha=.15f),size=Size(w*.045f,h))
            drawLine(Color.White.copy(alpha=.15f),Offset(w*.06f,0f),Offset(w*.06f,h),1f)
            drawRect(Color(0xFFE7D6A8).copy(alpha=.35f),Offset(w*.13f,h*.08f),Size(w*.74f,h*.84f),style=Stroke(1f))
            drawCircle(Color(0xFFE8DBB1),w*.105f,Offset(w*.68f,h*.57f))
            drawCircle(palette[0].copy(alpha=.95f),w*.093f,Offset(w*.72f,h*.545f))
            repeat(20) { i -> val x=(sin(i*19.7)*.36+.52).toFloat()*w; val y=(cos(i*7.3)*.1+.55).toFloat()*h; drawCircle(Color(0xFFE7D6A8).copy(alpha=.6f),if(i%4==0)2.2f else 1.2f,Offset(x,y)) }
            repeat(3) { layer ->
                val path=Path().apply { moveTo(0f,h); lineTo(0f,h*(.71f+layer*.07f)); for(i in 0..12) { val x=w*i/12f; val y=h*(.68f+layer*.075f)+(sin(i*.8+layer)*h*.07).toFloat(); lineTo(x,y) }; lineTo(w,h); close() }
                drawPath(path,Color(0xFF071E1B).copy(alpha=.23f+layer*.16f))
            }
        }
        Column(Modifier.fillMaxSize().padding(horizontal=maxWidth*.12f,vertical=maxHeight*.1f),horizontalAlignment=Alignment.CenterHorizontally) {
            Text(book.title.replace('\n',' '),color=Color(0xFFF2EAD1),fontFamily=FontFamily.Serif,fontSize=coverType.sp,lineHeight=(coverType*1.25f).sp,textAlign=androidx.compose.ui.text.style.TextAlign.Center,maxLines=4,overflow=TextOverflow.Ellipsis)
            Spacer(Modifier.weight(1f))
            Text(book.author.ifBlank { "A NOVEL IN THE MAKING" }.uppercase(),color=Color(0xFFE4D6B8),fontSize=if(compact) 7.sp else 9.sp,letterSpacing=1.4.sp,maxLines=1,overflow=TextOverflow.Ellipsis)
        }
    }
}
