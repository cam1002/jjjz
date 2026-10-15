package com.glass.ledger.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.Locale
import kotlin.math.abs

/**
 * 月度余额趋势图（账户详情页专用，自研玻璃风格）：
 * - Catmull-Rom 平滑三次贝塞尔曲线
 * - 曲线下方渐变面积填充
 * - 虚线网格 + 发光数据点（白芯描边）
 * - 高亮月份：竖直虚线导引 + 悬浮玻璃气泡显示余额
 * - 底部月份刻度
 */
@Composable
fun BalanceLineChart(
    values: List<Double>,
    labels: List<String>,
    highlightIndex: Int,
    accent: Color,
    modifier: Modifier = Modifier
) {
    // 原生文字画笔（Canvas 内绘制刻度与气泡）
    val labelPaint = remember {
        android.graphics.Paint().apply {
            isAntiAlias = true
            textAlign = android.graphics.Paint.Align.CENTER
        }
    }
    val tipPaint = remember {
        android.graphics.Paint().apply {
            isAntiAlias = true
            textAlign = android.graphics.Paint.Align.CENTER
        }
    }

    Canvas(modifier.fillMaxWidth().height(212.dp)) {
        if (values.size < 2) return@Canvas

        val padStart = 16.dp.toPx()
        val padEnd = 16.dp.toPx()
        val top = 44.dp.toPx()
        val bottom = size.height - 30.dp.toPx()
        val usableW = size.width - padStart - padEnd
        val stepX = usableW / (values.size - 1)

        val minV = values.min()
        val maxV = values.max()
        val margin = ((maxV - minV) * 0.15).coerceAtLeast(abs(minV) * 0.05 + 1.0)
        val lo = minV - margin
        val hi = maxV + margin
        val range = (hi - lo).coerceAtLeast(1.0)

        fun xAt(i: Int) = padStart + stepX * i
        fun yAt(v: Double) = (bottom - (v - lo) / range * (bottom - top)).toFloat()

        val pts = values.mapIndexed { i, v -> Offset(xAt(i), yAt(v)) }

        // ---- 虚线网格（4 条） ----
        val gridPaint = Color.White.copy(alpha = 0.65f)
        repeat(4) { g ->
            val y = top + (bottom - top) * g / 3f
            drawLine(
                color = gridPaint,
                start = Offset(padStart, y),
                end = Offset(size.width - padEnd, y),
                strokeWidth = 1.dp.toPx(),
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(4f, 9f), 0f)
            )
        }

        // ---- 平滑曲线（Catmull-Rom 转三次贝塞尔），向传入 Path 追加 ----
        fun buildCurve(path: Path) {
            path.moveTo(pts.first().x, pts.first().y)
            for (i in 0 until pts.size - 1) {
                val p0 = pts.getOrElse(i - 1) { pts[i] }
                val p1 = pts[i]
                val p2 = pts[i + 1]
                val p3 = pts.getOrElse(i + 2) { p2 }
                path.cubicTo(
                    p1.x + (p2.x - p0.x) / 6f, p1.y + (p2.y - p0.y) / 6f,
                    p2.x - (p3.x - p1.x) / 6f, p2.y - (p3.y - p1.y) / 6f,
                    p2.x, p2.y
                )
            }
        }
        val line = Path().apply { buildCurve(this) }

        // ---- 面积填充：曲线 + 封底 ----
        val area = Path().apply {
            buildCurve(this)
            lineTo(pts.last().x, bottom)
            lineTo(pts.first().x, bottom)
            close()
        }
        drawPath(
            path = area,
            brush = Brush.verticalGradient(
                colors = listOf(accent.copy(alpha = 0.32f), accent.copy(alpha = 0.02f)),
                startY = top,
                endY = bottom
            )
        )

        // ---- 渐变描边主线 ----
        drawPath(
            path = line,
            brush = Brush.horizontalGradient(
                colors = listOf(accent.copy(alpha = 0.55f), accent, accent.copy(alpha = 0.8f)),
                startX = padStart,
                endX = size.width - padEnd
            ),
            style = Stroke(width = 2.6.dp.toPx(), cap = StrokeCap.Round)
        )

        // ---- 高亮点：竖直虚线导引 ----
        if (highlightIndex in pts.indices) {
            val hp = pts[highlightIndex]
            drawLine(
                color = accent.copy(alpha = 0.35f),
                start = Offset(hp.x, hp.y),
                end = Offset(hp.x, bottom),
                strokeWidth = 1.dp.toPx(),
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(5f, 7f), 0f)
            )
        }

        // ---- 数据点：外发光 + 白芯描边 ----
        pts.forEachIndexed { i, p ->
            val sel = i == highlightIndex
            drawCircle(color = accent.copy(alpha = 0.15f), radius = (if (sel) 11.dp else 7.dp).toPx(), center = p)
            drawCircle(color = Color.White, radius = (if (sel) 4.8.dp else 3.4.dp).toPx(), center = p)
            drawCircle(
                color = accent,
                radius = (if (sel) 4.8.dp else 3.4.dp).toPx(),
                center = p,
                style = Stroke(width = (if (sel) 2.2.dp else 1.5.dp).toPx())
            )
        }

        // ---- 月份刻度 ----
        labelPaint.textSize = 10.sp.toPx()
        pts.forEachIndexed { i, p ->
            if (i >= labels.size) return@forEachIndexed
            val sel = i == highlightIndex
            labelPaint.color = android.graphics.Color.parseColor(if (sel) "#1C1E2A" else "#8A8FA3")
            labelPaint.isFakeBoldText = sel
            drawContext.canvas.nativeCanvas.drawText(
                labels[i],
                p.x.coerceIn(padStart + 8.dp.toPx(), size.width - padEnd - 8.dp.toPx()),
                size.height - 6.dp.toPx(),
                labelPaint
            )
        }

        // ---- 高亮悬浮玻璃气泡 ----
        if (highlightIndex in pts.indices) {
            val hp = pts[highlightIndex]
            val tipText = String.format(Locale.US, "%,.2f", values[highlightIndex])
            tipPaint.textSize = 11.5.sp.toPx()
            tipPaint.color = android.graphics.Color.parseColor("#1C1E2A")
            val textW = tipPaint.measureText(tipText)
            val chipW = textW + 26.dp.toPx()
            val chipH = 30.dp.toPx()
            var chipX = hp.x - chipW / 2f
            chipX = chipX.coerceIn(4.dp.toPx(), size.width - chipW - 4.dp.toPx())
            var chipY = hp.y - chipH - 14.dp.toPx()
            if (chipY < 2.dp.toPx()) chipY = hp.y + 14.dp.toPx()

            // 阴影感：底层稍深
            drawRoundRect(
                color = Color(0xFF33406F).copy(alpha = 0.10f),
                topLeft = Offset(chipX, chipY + 2.dp.toPx()),
                size = Size(chipW, chipH),
                cornerRadius = CornerRadius(chipH / 2f)
            )
            drawRoundRect(
                color = Color.White.copy(alpha = 0.94f),
                topLeft = Offset(chipX, chipY),
                size = Size(chipW, chipH),
                cornerRadius = CornerRadius(chipH / 2f)
            )
            drawRoundRect(
                color = accent.copy(alpha = 0.55f),
                topLeft = Offset(chipX, chipY),
                size = Size(chipW, chipH),
                cornerRadius = CornerRadius(chipH / 2f),
                style = Stroke(width = 1.2.dp.toPx())
            )
            drawContext.canvas.nativeCanvas.drawText(
                tipText,
                chipX + chipW / 2f,
                chipY + chipH / 2f - (tipPaint.ascent() + tipPaint.descent()) / 2f,
                tipPaint
            )
        }
    }
}
