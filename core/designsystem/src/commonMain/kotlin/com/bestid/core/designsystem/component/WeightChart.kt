package com.bestid.core.designsystem.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.bestid.core.designsystem.preview.PreviewData
import com.bestid.core.designsystem.preview.PreviewSurface
import com.bestid.core.designsystem.theme.BestIdTheme
import kotlin.math.roundToLong

/** Punto del gráfico de peso. [label] es el mes ya formateado; [valueLabel], el peso formateado. */
@Immutable
data class WeightPoint(val label: String, val kg: Double, val valueLabel: String)

/**
 * Evolución del peso dibujada con Canvas: línea, área suave, último punto destacado y ejes
 * rotulados en kg (izquierda) y meses (abajo). [description] resume el gráfico para el lector de pantalla.
 */
@Composable
fun WeightChart(
    points: List<WeightPoint>,
    description: String,
    modifier: Modifier = Modifier,
) {
    val colors = BestIdTheme.colors
    val labelStyle = BestIdTheme.typography.label.copy(color = colors.muted)
    val measurer = rememberTextMeasurer()
    val chartHeight = BestIdTheme.sizes.chartHeight

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(chartHeight)
            .semantics { contentDescription = description },
    ) {
        if (points.isEmpty()) return@Canvas
        val minKg = points.minOf { it.kg }
        val maxKg = points.maxOf { it.kg }
        val span = (maxKg - minKg).takeIf { it > 0.0 } ?: 1.0
        val low = minKg - span * 0.2
        val high = maxKg + span * 0.2

        val maxLabel = measurer.measure(formatKg(high), labelStyle)
        val leftAxis = maxLabel.size.width + 8.dp.toPx()
        val bottomAxis = maxLabel.size.height + 8.dp.toPx()
        val plotWidth = size.width - leftAxis - 8.dp.toPx()
        val plotHeight = size.height - bottomAxis - 8.dp.toPx()
        val top = 8.dp.toPx()

        fun x(i: Int) = leftAxis + if (points.size == 1) plotWidth / 2 else plotWidth * i / (points.size - 1)
        fun y(kg: Double) = top + plotHeight * (1 - ((kg - low) / (high - low))).toFloat()

        // Líneas guía y rótulos del eje de kg
        listOf(low, (low + high) / 2, high).forEach { kg ->
            val yy = y(kg)
            drawLine(colors.line, Offset(leftAxis, yy), Offset(leftAxis + plotWidth, yy), strokeWidth = 1.dp.toPx())
            val text = measurer.measure(formatKg(kg), labelStyle)
            drawText(text, topLeft = Offset(0f, yy - text.size.height / 2))
        }

        // Área y línea
        val line = Path()
        points.forEachIndexed { i, p -> if (i == 0) line.moveTo(x(i), y(p.kg)) else line.lineTo(x(i), y(p.kg)) }
        val area = Path().apply {
            addPath(line)
            lineTo(x(points.lastIndex), top + plotHeight)
            lineTo(x(0), top + plotHeight)
            close()
        }
        drawPath(area, Brush.verticalGradient(listOf(colors.primarySoft, colors.surface), startY = top, endY = top + plotHeight))
        drawPath(line, colors.primary, style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round))

        // Último punto destacado
        val last = Offset(x(points.lastIndex), y(points.last().kg))
        drawCircle(colors.surface, radius = 7.dp.toPx(), center = last)
        drawCircle(colors.primary, radius = 5.dp.toPx(), center = last)

        // Rótulos de meses
        points.forEachIndexed { i, p ->
            val text = measurer.measure(p.label, labelStyle)
            drawText(text, topLeft = Offset(x(i) - text.size.width / 2, top + plotHeight + 4.dp.toPx()))
        }
    }
}

/** Formato de eje con coma decimal (es-PE): 4,2. */
internal fun formatKg(kg: Double): String {
    val tenths = (kg * 10).roundToLong()
    return "${tenths / 10},${kotlin.math.abs(tenths % 10)}"
}

@Preview
@Composable
private fun WeightChartPreview() = PreviewSurface {
    PlainCard {
        WeightChart(
            points = PreviewData.WEIGHT_POINTS.map { (m, kg) -> WeightPoint(m, kg, formatKg(kg)) },
            description = PreviewData.STAT_VALUE,
        )
    }
}

@Preview
@Composable
private fun WeightChartSinglePointPreview() = PreviewSurface {
    PlainCard {
        WeightChart(points = listOf(WeightPoint("nov", 4.2, "4,2")), description = PreviewData.STAT_VALUE)
    }
}
