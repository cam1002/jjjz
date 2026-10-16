package com.glass.ledger.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * 收据（小票）纸面：白色卡片 + 上下两条扇贝撕纸边。
 *
 * 形状是现场按宽度算出来的：顶边一串向上凸的半圆，底边一串向下凸的半圆，
 * 左右是直边（通栏贴在页面留白内）。先画一层浅黑影当纸的厚度，再在上面画纸面。
 *
 * @param topInset 纸面顶边到 Box 顶部的距离，给压边的圆形徽标（[badge]）留位置
 * @param badge    压在纸顶边中央的圆形徽标
 */
@Composable
fun ReceiptSurface(
    modifier: Modifier = Modifier,
    scallop: Dp = 9.dp,
    topInset: Dp = 34.dp,
    badgeSize: Dp = 62.dp,
    badge: @Composable (() -> Unit)? = null,
    contentPadding: PaddingValues = PaddingValues(start = 22.dp, end = 22.dp, top = 18.dp, bottom = 22.dp),
    content: @Composable ColumnScope.() -> Unit
) {
    val shadowOffset = 4.dp
    Box(modifier) {
        Canvas(Modifier.matchParentSize()) {
            val step = scallop.toPx() * 2.1f
            val topY = topInset.toPx()
            // 纸面最低点留出 shadowOffset，不然影子的下沿会被 Canvas 边界裁掉
            val bottomY = size.height - step / 2f - shadowOffset.toPx()
            // 影子（整体下移一点，先画）+ 纸面（后画，盖住影子的上沿）
            drawPath(
                path = receiptPath(size.width, topY, bottomY, step, shadowOffset.toPx()),
                color = Color(0x14000000)
            )
            drawPath(
                path = receiptPath(size.width, topY, bottomY, step, 0f),
                color = Color.White
            )
        }
        Column(
            Modifier
                // 有徽标时自动给"压在纸边上的那一半"留出高度
                .padding(top = topInset + (if (badge != null) badgeSize / 2 else 0.dp))
                .padding(contentPadding)
        ) { content() }
        if (badge != null) {
            Box(
                Modifier
                    .align(Alignment.TopCenter)
                    .offset(y = topInset - badgeSize / 2f)
                    .size(badgeSize),
                contentAlignment = Alignment.Center
            ) { badge() }
        }
    }
}

/**
 * 压在收据顶边中央的分类徽标：白色外环 + 分类色浅底 + 分类色图标。
 * 上半截落在顶部色带上，白环让它像是"从色带里抠出来"的（图里的效果）。
 * 尺寸与 [ReceiptSurface] 的 topInset / badgeSize 配套使用。
 */
@Composable
fun ReceiptCategoryBadge(
    iconKey: String,
    colorArgb: Int,
    size: Dp = 62.dp,
    ring: Dp = 6.dp,
    modifier: Modifier = Modifier
) {
    val color = Color(colorArgb)
    val inner = size - ring * 2
    Box(
        modifier.size(size).clip(CircleShape).background(Color.White),
        contentAlignment = Alignment.Center
    ) {
        Box(
            Modifier.size(inner).clip(CircleShape).background(color.copy(alpha = 0.17f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                CategoryIcons.of(iconKey),
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(inner * 0.5f)
            )
        }
    }
}

/**
 * 点线（收据里 label 与 value 之间的圆点连线，也能直接当分隔线用）。
 * 宽度由调用方给：通栏用 `Modifier.fillMaxWidth()`，行内弹性用 `Modifier.weight(1f)`。
 */
@Composable
fun DottedLeader(
    modifier: Modifier = Modifier,
    color: Color = Color(0xFFCBD2E4),
    dotSize: Dp = 2.6.dp,
    gapRatio: Float = 2.4f
) {
    Canvas(modifier.height(4.dp)) {
        val d = dotSize.toPx()
        val mid = size.height / 2f
        drawLine(
            color = color,
            start = Offset(d / 2f, mid),
            end = Offset(size.width - d / 2f, mid),
            strokeWidth = d,
            cap = StrokeCap.Round,
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(0.1f, d * gapRatio))
        )
    }
}

/**
 * 生成扇贝纸面路径。
 * 顶部：从左往右，每段一个向上凸的半圆（180° 起、扫 180°）。
 * 底部：从右往左，每段一个向下凸的半圆（0° 起、扫 180°）。
 */
private fun receiptPath(w: Float, topY: Float, bottomY: Float, step: Float, dy: Float): Path {
    val path = Path()
    path.moveTo(0f, topY + dy)
    var x = 0f
    while (x < w - 0.5f) {
        val seg = minOf(step, w - x)
        path.addArc(Rect(x, topY + dy - seg / 2f, x + seg, topY + dy + seg / 2f), 180f, 180f)
        x += seg
    }
    path.lineTo(w, bottomY + dy)
    var xr = w
    while (xr > 0.5f) {
        val seg = minOf(step, xr)
        val left = xr - seg
        path.addArc(Rect(left, bottomY + dy - seg / 2f, xr, bottomY + dy + seg / 2f), 0f, 180f)
        xr = left
    }
    path.close()
    return path
}
