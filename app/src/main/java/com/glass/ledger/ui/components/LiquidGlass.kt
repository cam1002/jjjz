package com.glass.ledger.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * iOS 液态玻璃风格 Modifier：
 * 半透明白色渐变表面 + 高光描边 + 柔和投影。
 * 配合 [GlassBackdrop] 的彩色光斑背景形成"透而不透"的折射感。
 */
fun Modifier.liquidGlass(
    shape: Shape = RoundedCornerShape(28.dp),
    alpha: Float = 0.62f,
    elevation: Dp = 12.dp,
    highlight: Boolean = true
): Modifier {
    var m: Modifier = this
        .shadow(
            elevation = elevation,
            shape = shape,
            clip = false,
            ambientColor = Color.White.copy(alpha = 0.35f),
            spotColor = Color(0x4D334090)
        )
        .background(
            brush = Brush.linearGradient(
                colors = listOf(
                    Color.White.copy(alpha = alpha),
                    Color.White.copy(alpha = alpha * 0.55f),
                    Color(0xFFEEF1FF).copy(alpha = alpha * 0.75f)
                ),
                start = Offset.Zero,
                end = Offset.Infinite
            ),
            shape = shape
        )
    if (highlight) {
        m = m.border(width = 1.2.dp, color = Color.White.copy(alpha = 0.85f), shape = shape)
    }
    return m
}

/** 常用圆角 */
val GlassCornerLarge = RoundedCornerShape(30.dp)
val GlassCornerMedium = RoundedCornerShape(22.dp)
val GlassCornerSmall = RoundedCornerShape(14.dp)
val GlassCornerPill = RoundedCornerShape(percent = 50)
