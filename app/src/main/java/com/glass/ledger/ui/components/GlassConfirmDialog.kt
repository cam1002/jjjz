package com.glass.ledger.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.glass.ledger.ui.theme.ExpenseRed
import com.glass.ledger.ui.theme.InkPrimary
import com.glass.ledger.ui.theme.InkSecondary

/**
 * 应用内统一的玻璃确认弹窗（删除账户 / 删除记录）。
 * 替代 Material AlertDialog，避免顶部状态栏出现灰色遮罩，
 * 并与整体液态玻璃视觉保持一致。
 */
@Composable
fun GlassConfirmDialog(
    title: String,
    body: String,
    confirmLabel: String = "删除",
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false)
    ) {
        DialogEdgeToEdge()
        Column(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 44.dp)
                .shadow(18.dp, RoundedCornerShape(24.dp))
                .clip(RoundedCornerShape(24.dp))
                .background(Brush.verticalGradient(listOf(Color(0xF2FFFFFF), Color(0xECEDF0FB))))
                .padding(start = 22.dp, end = 22.dp, top = 22.dp, bottom = 18.dp)
        ) {
            Text(
                title,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = InkPrimary
            )
            Spacer(Modifier.height(8.dp))
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
}

@Composable
private fun GlassDialogButton(
    label: String,
    modifier: Modifier = Modifier,
    danger: Boolean = false,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(16.dp)
    val background = if (danger) {
        Brush.verticalGradient(listOf(Color(0xFFFF7A66), ExpenseRed))
    } else {
        Brush.verticalGradient(listOf(Color(0xB3FFFFFF), Color(0x99E6EAF7)))
    }
    Column(
        modifier
            .clip(shape)
            .background(background)
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
