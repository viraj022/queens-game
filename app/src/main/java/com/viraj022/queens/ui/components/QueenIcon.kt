package com.viraj022.queens.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import com.viraj022.queens.ui.theme.QueenGold

/**
 * Vector crown drawn with Compose Canvas — no emoji.
 */
@Composable
fun QueenIcon(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.fillMaxSize(0.62f)) {
        val w = size.width
        val h = size.height
        val stroke = (w * 0.06f).coerceAtLeast(2f)

        val crown = Path().apply {
            moveTo(w * 0.12f, h * 0.72f)
            lineTo(w * 0.12f, h * 0.42f)
            lineTo(w * 0.28f, h * 0.58f)
            lineTo(w * 0.50f, h * 0.22f)
            lineTo(w * 0.72f, h * 0.58f)
            lineTo(w * 0.88f, h * 0.42f)
            lineTo(w * 0.88f, h * 0.72f)
            close()
        }

        drawPath(path = crown, color = QueenGold, style = Fill)
        drawPath(
            path = crown,
            color = QueenGold.copy(alpha = 0.85f),
            style = Stroke(width = stroke, cap = StrokeCap.Round, join = StrokeJoin.Round)
        )

        drawRoundRect(
            color = QueenGold,
            topLeft = Offset(w * 0.10f, h * 0.70f),
            size = Size(w * 0.80f, h * 0.14f),
            cornerRadius = CornerRadius(h * 0.04f, h * 0.04f)
        )

        listOf(
            Offset(w * 0.12f, h * 0.40f),
            Offset(w * 0.50f, h * 0.20f),
            Offset(w * 0.88f, h * 0.40f)
        ).forEach { center ->
            drawCircle(
                color = QueenGold.copy(alpha = 0.95f),
                radius = w * 0.055f,
                center = center
            )
        }
    }
}
