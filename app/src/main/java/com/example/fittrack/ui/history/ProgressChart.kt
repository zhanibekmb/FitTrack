package com.example.fittrack.ui.history

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import com.example.fittrack.R
import java.util.Locale

/** Simple chart without a third-party chart library: one point per completed workout. */
@Composable
fun ProgressChart(values: List<Double>) {
    val accent = MaterialTheme.colorScheme.primary
    val grid = MaterialTheme.colorScheme.outlineVariant
    if (values.isEmpty()) {
        Text(stringResource(R.string.no_chart_results))
        return
    }
    val highest = (values.maxOrNull() ?: 0.0).coerceAtLeast(minimumValue = 1.0)
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            stringResource(R.string.chart_caption, values.maxOrNull() ?: 0.0),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        val description = stringResource(R.string.chart_description,
            values.joinToString(", ") { String.format(Locale.ENGLISH, "%.1f", it) })
        Canvas(Modifier.fillMaxWidth().height(190.dp).semantics { contentDescription = description }) {
            val left = 16.dp.toPx()
            val right = size.width - 16.dp.toPx()
            val top = 14.dp.toPx()
            val bottom = size.height - 14.dp.toPx()
            val maxY = highest * 1.15
            repeat(times = 4) { index ->
                val y = top + (bottom - top) * index / 3f
                drawLine(color = grid, start = Offset(x = left, y), end = Offset(x = right, y))
            }
            val points = values.mapIndexed { index, weight ->
                val x = if (values.size == 1) (left + right) / 2f
                else left + (right - left) * index / values.lastIndex
                val y = bottom - (bottom - top) * (weight / maxY).toFloat()
                Offset(x, y)
            }
            for (i in 0 until points.lastIndex) {
                drawLine(
                    color = accent,
                    start = points[i],
                    end = points[i + 1],
                    strokeWidth = 3.dp.toPx(),
                    cap = StrokeCap.Round
                )
            }
            points.forEach { pt ->
                drawCircle(color = accent, radius = 5.dp.toPx(), center = pt)
            }
        }
    }
}
