package com.glass.ledger.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.glass.ledger.data.AccountType
import com.glass.ledger.data.AccountTypes
import com.glass.ledger.ui.components.DialogEdgeToEdge

/**
 * 新增账户第一步：选择账户类型。
 * 布局对齐参考截图（标题 + 关闭 + 分隔线 + 类型名/说明列表），
 * 视觉使用当前 APP 的液态玻璃风格。
 */
@Composable
fun AccountTypePickerDialog(
    onDismiss: () -> Unit,
    onPick: (AccountType) -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false)
    ) {
        DialogEdgeToEdge()
        Column(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .clip(RoundedCornerShape(28.dp))
                .background(
                    Brush.verticalGradient(
                        listOf(Color(0xE6FFFFFF), Color(0xE0EEF1FE))
                    )
                )
                .padding(start = 22.dp, end = 22.dp, top = 20.dp, bottom = 12.dp)
        ) {
            // 标题行
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "选择账户类型",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1C1E2A),
                    modifier = Modifier.weight(1f)
                )
                Box(
                    Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(Color(0x33FFFFFF))
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = onDismiss
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text("×", fontSize = 16.sp, color = Color(0xFF7A7F95))
                }
            }
            Spacer(Modifier.height(12.dp))
            // 分隔线
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(Color(0x225A5F73))
            )
            Spacer(Modifier.height(6.dp))

            AccountTypes.all.forEach { (type, label, desc) ->
                Column(
                    Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { onPick(type) }
                        .padding(vertical = 11.dp, horizontal = 6.dp)
                ) {
                    Text(label, fontSize = 16.sp, fontWeight = FontWeight.Medium, color = Color(0xFF1C1E2A))
                    Text(
                        desc,
                        fontSize = 13.sp,
                        color = Color(0xFF8A8FA5),
                        modifier = Modifier.padding(top = 3.dp)
                    )
                }
            }
        }
    }
}
