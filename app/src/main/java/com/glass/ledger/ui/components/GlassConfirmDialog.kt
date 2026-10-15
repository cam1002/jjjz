package com.glass.ledger.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.glass.ledger.ui.base.GlassDialogPanel
import com.glass.ledger.ui.base.GlassTokens
import com.glass.ledger.ui.theme.ExpenseRed
import com.glass.ledger.ui.theme.InkPrimary
import com.glass.ledger.ui.theme.InkSecondary

/**
 * 应用内统一的确认弹窗（删除账户 / 删除记录）。
 * 外壳复用 Base 层 [GlassDialogPanel]，只保留"正文 + 双按钮"这部分差异内容。
 */
@Composable
fun GlassConfirmDialog(
    title: String,
    body: String,
    confirmLabel: String = "删除",
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    GlassDialogPanel(
        title = title,
        onDismiss = null,
        divider = false
    ) {
        Text(
            body,
            fontSize = 13.5.sp,
            color = InkSecondary,
            lineHeight = 20.sp
        )
        Spacer(Modifier.height(20.dp))
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            GlassDialogButton(
                label = "取消",
                modifier = Modifier.weight(1f),
                onClick = onDismiss
            )
            GlassDialogButton(
                label = confirmLabel,
                modifier = Modifier.weight(1f),
                danger = true,
                onClick = onConfirm
            )
        }
    }
}

/** 确认弹窗里的按钮：普通态走统一玻璃面板，危险态走红色渐变 */
@Composable
private fun GlassDialogButton(
    label: String,
    modifier: Modifier = Modifier,
    danger: Boolean = false,
    onClick: () -> Unit
) {
    Column(
        modifier
            .clip(GlassTokens.Corner.Chip)
            .then(
                if (danger) {
                    Modifier.background(
                        Brush.verticalGradient(listOf(Color(0xFFFF7A66), ExpenseRed)),
                        GlassTokens.Corner.Chip
                    )
                } else {
                    Modifier.liquidGlass(
                        shape = GlassTokens.Corner.Chip,
                        alpha = GlassTokens.Surface.Chip,
                        elevation = GlassTokens.Elevation.Chip
                    )
                }
            )
            .clickableNoRipple(onClick = onClick)
            .padding(vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            label,
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold,
            color = if (danger) Color.White else InkPrimary,
            textAlign = TextAlign.Center
        )
    }
}
