package com.glass.ledger.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

import com.glass.ledger.ui.base.GlassTokens

/**
 * 液态玻璃表面的唯一实现。
 * 尺寸、透明度、清晰度等参数全部来自 Base 层 [GlassTokens]；
 * 想整体调风格请改 GlassTokens，不要在这里或各页面写数值。
 */
fun Modifier.liquidGlass(
    shape: Shape = GlassTokens.Corner.Dialog,
    alpha: Float = GlassTokens.Surface.Card,
    elevation: Dp = GlassTokens.Elevation.Section,
    highlight: Boolean = true
): Modifier {
    val a = GlassTokens.surfaceAlpha(alpha)
    var m: Modifier = this
        .shadow(
            elevation = elevation,
            shape = shape,
            clip = false,
            ambientColor = Color.White.copy(alpha = 0.30f),
            spotColor = Color(0x59333F7A)
        )
        .background(
            brush = Brush.linearGradient(
                colors = listOf(
                    Color.White.copy(alpha = a),
                    Color(0xFFFAFBFF).copy(alpha = a * 0.94f),
                    Color(0xFFE9EDFB).copy(alpha = a * 0.97f)
                ),
                start = Offset.Zero,
                end = Offset.Infinite
            ),
            shape = shape
        )
    if (highlight) {
        m = m.border(width = 1.2.dp, color = Color.White.copy(alpha = 0.9f), shape = shape)
    }
    return m
}

/** 常用圆角（转发到 GlassTokens.Corner，保留旧名字兼容已有页面） */
val GlassCornerLarge = GlassTokens.Corner.Card
val GlassCornerMedium = GlassTokens.Corner.Field
val GlassCornerSmall = GlassTokens.Corner.Small
val GlassCornerPill = GlassTokens.Corner.Pill
