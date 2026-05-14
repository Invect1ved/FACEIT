package com.example.faceit.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp

/**
 * Простой линейный график эло по точкам (слева направо — от старых к новым значениям).
 */
@Composable
fun EloLineChart(
    points: List<Int>,
    modifier: Modifier = Modifier,
    chartHeightDp: Int = 180
) {
    val primary = MaterialTheme.colorScheme.primary
    val outline = MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)

    if (points.size < 2) return

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(chartHeightDp.dp)
    ) {
        val pad = 8.dp.toPx()
        val w = size.width - pad * 2
        val h = size.height - pad * 2
        val minE = points.minOrNull()!!.toFloat()
        val maxE = points.maxOrNull()!!.toFloat()
        val span = (maxE - minE).coerceAtLeast(1f)

        fun xAt(i: Int): Float = pad + w * i / (points.lastIndex).toFloat()
        fun yAt(v: Int): Float = pad + h * (1f - (v - minE) / span)

        drawLine(
            color = outline,
            start = Offset(pad, pad + h),
            end = Offset(pad + w, pad + h),
            strokeWidth = 1.dp.toPx()
        )

        val path = Path()
        points.forEachIndexed { i, v ->
            val x = xAt(i)
            val y = yAt(v)
            if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }
        drawPath(
            path = path,
            color = primary,
            style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
        )
        points.forEachIndexed { i, v ->
            drawCircle(
                color = primary,
                radius = 4.dp.toPx(),
                center = Offset(xAt(i), yAt(v))
            )
        }
    }
}
