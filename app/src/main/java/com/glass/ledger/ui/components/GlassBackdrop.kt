package com.glass.ledger.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.glass.ledger.ui.theme.LavenderBlob
import com.glass.ledger.ui.theme.MintBlob
import com.glass.ledger.ui.theme.PeachBlob
import com.glass.ledger.ui.theme.PeriwinkleBlob

/**
 * 液态玻璃的"景"：柔和渐变底色 + 大尺寸径向彩色光斑。
 * 玻璃面板浮在光斑之上，产生 iOS 26/27 液态玻璃的折射质感。
 *
 * 清晰度取向：底色整体提亮、光斑浓度下调，并在最后叠一层淡白纱。
 * 背景对比度降低后，透过玻璃面板看到的干扰变少，文字与数字更清晰。
 */
@Composable
fun GlassBackdrop(modifier: Modifier = Modifier) {
    Canvas(
        modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFFE1E7FB), Color(0xFFEFF2FD), Color(0xFFEEE8F8))
                )
            )
    ) {
        val w = size.width
        val h = size.height
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(PeriwinkleBlob.copy(alpha = 0.42f), Color.Transparent),
                center = Offset(w * 0.85f, h * 0.08f),
                radius = w * 0.75f
            )
        )
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(LavenderBlob.copy(alpha = 0.34f), Color.Transparent),
                center = Offset(w * 0.1f, h * 0.32f),
                radius = w * 0.6f
            )
        )
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(PeachBlob.copy(alpha = 0.28f), Color.Transparent),
                center = Offset(w * 0.95f, h * 0.62f),
                radius = w * 0.55f
            )
        )
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(MintBlob.copy(alpha = 0.26f), Color.Transparent),
                center = Offset(w * 0.15f, h * 0.9f),
                radius = w * 0.6f
            )
        )
        // 顶部强光晕，模拟玻璃后的光源
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color.White.copy(alpha = 0.34f), Color.Transparent),
                center = Offset(w * 0.5f, h * 0.05f),
                radius = w * 0.6f
            )
        )
        // 白纱：把整屏亮度抬起来，玻璃面板内的深色文字对比度更稳
        drawRect(color = Color.White.copy(alpha = 0.2f))
    }
}
