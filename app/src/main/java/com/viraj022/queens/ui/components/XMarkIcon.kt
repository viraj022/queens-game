package com.viraj022.queens.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.unit.dp
import com.viraj022.queens.ui.theme.XMarkSlate

@Composable
fun XMarkIcon(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.fillMaxSize(0.36f)) {
        val strokeWidth = 3.dp.toPx()
        drawLine(
            color = XMarkSlate,
            start = Offset(0f, 0f),
            end = Offset(size.width, size.height),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round
        )
        drawLine(
            color = XMarkSlate,
            start = Offset(size.width, 0f),
            end = Offset(0f, size.height),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round
        )
    }
}
