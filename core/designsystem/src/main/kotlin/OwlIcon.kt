package com.novelforge.app.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp

@Composable
fun OwlIcon(modifier: Modifier = Modifier) {
    val color = MaterialTheme.colorScheme.onSurface
    Canvas(modifier.size(26.dp)) {
        val w = size.width; val h = size.height
        val stroke = Stroke(w * .065f)
        drawPath(Path().apply {
            moveTo(w*.15f,h*.12f);lineTo(w*.33f,h*.25f);quadraticBezierTo(w*.5f,h*.18f,w*.67f,h*.25f);lineTo(w*.85f,h*.12f)
            lineTo(w*.88f,h*.62f);cubicTo(w*.85f,h*.98f,w*.15f,h*.98f,w*.12f,h*.62f);close()
        },color,style=stroke)
        listOf(.34f,.66f).forEach { x -> drawCircle(color,w*.155f,Offset(w*x,h*.47f),style=stroke);drawCircle(color,w*.045f,Offset(w*x,h*.47f)) }
        drawPath(Path().apply { moveTo(w*.44f,h*.66f);lineTo(w*.56f,h*.66f);lineTo(w*.5f,h*.75f);close() },color)
    }
}
