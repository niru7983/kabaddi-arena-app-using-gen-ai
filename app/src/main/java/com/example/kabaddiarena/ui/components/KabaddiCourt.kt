package com.example.kabaddiarena.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.example.kabaddiarena.data.local.entity.MatchEventEntity

import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import android.graphics.Paint
import android.graphics.Typeface
import androidx.compose.ui.graphics.toArgb

@Composable
fun KabaddiCourt(
    events: List<MatchEventEntity>,
    onCoordSelected: (Float, Float) -> Unit,
    modifier: Modifier = Modifier,
    homeTeam: String = "",
    opponentTeam: String = ""
) {
    BoxWithConstraints(
        modifier = modifier
            .aspectRatio(0.8f) // Vertical court
            .background(Color(0xFF8D6E63))
    ) {
        val maxWidthPx = with(LocalDensity.current) { maxWidth.toPx() }
        val maxHeightPx = with(LocalDensity.current) { maxHeight.toPx() }

        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTapGestures { offset ->
                        val xPercent = offset.x / maxWidthPx
                        val yPercent = offset.y / maxHeightPx
                        onCoordSelected(xPercent, yPercent)
                    }
                }
        ) {
            val width = size.width
            val height = size.height

            // Draw Court Outlines
            drawRect(
                color = Color.White,
                style = Stroke(width = 2.dp.toPx())
            )

            // Mid Line
            drawLine(
                color = Color.White,
                start = Offset(0f, height / 2),
                end = Offset(width, height / 2),
                strokeWidth = 3.dp.toPx()
            )

            // Baulk Lines
            drawLine(
                color = Color.Yellow,
                start = Offset(0f, height * 0.35f),
                end = Offset(width, height * 0.35f),
                strokeWidth = 2.dp.toPx()
            )
            drawLine(
                color = Color.Yellow,
                start = Offset(0f, height * 0.65f),
                end = Offset(width, height * 0.65f),
                strokeWidth = 2.dp.toPx()
            )

            // Bonus Lines
            drawLine(
                color = Color.White,
                start = Offset(0f, height * 0.25f),
                end = Offset(width, height * 0.25f),
                strokeWidth = 1.dp.toPx()
            )
            drawLine(
                color = Color.White,
                start = Offset(0f, height * 0.75f),
                end = Offset(width, height * 0.75f),
                strokeWidth = 1.dp.toPx()
            )

            // Team Names in Halves
            drawIntoCanvas { canvas ->
                val paint = Paint().apply {
                    color = Color.White.copy(alpha = 0.3f).toArgb()
                    textSize = 40.dp.toPx()
                    typeface = Typeface.DEFAULT_BOLD
                    textAlign = Paint.Align.CENTER
                }
                
                // Opponent Team (Top Half)
                canvas.nativeCanvas.drawText(
                    opponentTeam.uppercase(),
                    width / 2,
                    height * 0.25f,
                    paint
                )
                
                // Home Team (Bottom Half)
                canvas.nativeCanvas.drawText(
                    homeTeam.uppercase(),
                    width / 2,
                    height * 0.75f,
                    paint
                )
            }

            // Draw Event Heatmap Dots
            events.forEach { event ->
                if (event.x != null && event.y != null) {
                    drawCircle(
                        color = when (event.result) {
                            "FAILURE" -> Color.Red
                            "SUCCESS", "TOUCH", "BONUS", "SUPER_TACKLE" -> Color.Green
                            "EMPTY" -> Color.Gray
                            else -> Color.Gray
                        }.copy(alpha = 0.6f),
                        radius = 6.dp.toPx(),
                        center = Offset(event.x * width, event.y * height)
                    )
                }
            }
        }
    }
}
