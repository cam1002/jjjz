package com.glass.ledger.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/** 饼图切片 */
data class PieSlice(
    val label: String,
    val color: Color,
    val value: Double
)

/**
 * 圆形比例图（环形）。数据变化时带扫描动画。
 */
@Composable
fun PieChart(
    slices: List<PieSlice>,
    modifier: Modifier = Modifier,
    centerTitle: String = "支出",
    centerValue: String = ""
) {
    val total = slices.sumOf { it.value }
    val progress by animateFloatAsState(
        targetValue = if (total > 0) 1f else 0f,
        animationSpec = tween(durationMillis = 800),
        label = "pieProgress"
    )
    Box(modifier, contentAlignment = Alignment.Center) {
        Canvas(Modifier.fillMaxSize().padding(8.dp)) {
            val gap = if (slices.size > 1) 3f else 0f
            val ring = size.minDimension / 2f
            val strokePx = ring * 0.42f
            var startAngle = -90f
            if (total <= 0) {
                drawEmptyRing(center = Offset(ring, ring), radius = ring - strokePx / 2 - 8, thickness = strokePx)
            } else {
                slices.forEach { slice ->
                    val frac = (slice.value / total).toFloat()
                    val sweep = (frac * 360f * progress - gap).coerceAtLeast(0f)
                    drawArc(
                        color = slice.color,
                        startAngle = startAngle + gap / 2,
                        sweepAngle = sweep,
                        useCenter = false,
                        topLeft = Offset(strokePx / 2 + 8, strokePx / 2 + 8),
                        size = Size(
                            (ring - strokePx / 2 - 8) * 2f,
                            (ring - strokePx / 2 - 8) * 2f
                        ),
                        style = Stroke(width = strokePx, cap = StrokeCap.Butt)
                    )
                    startAngle += frac * 360f * progress
                }
            }
        }
        if (total > 0) {
            Box(contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(centerTitle, style = TextStyle(fontSize = 11.sp, color = Color(0xFF5A5F73)))
                    Text(
                        centerValue,
                        style = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1C1E2A))
                    )
                }
            }
        }
    }
}

private fun DrawScope.drawEmptyRing(center: Offset, radius: Float, thickness: Float) {
    drawCircle(
        color = Color.White.copy(alpha = 0.5f),
        radius = radius,
        center = center,
        style = Stroke(width = thickness)
    )
}
