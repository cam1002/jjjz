package com.glass.ledger.ui.screens

import androidx.compose.runtime.Composable
import com.glass.ledger.data.AccountType
import com.glass.ledger.data.AccountTypes
import com.glass.ledger.ui.base.GlassDialogOption
import com.glass.ledger.ui.base.GlassDialogPanel

/**
 * 新增账户第一步：选择账户类型。
 * 面板壳（Dialog + 系统栏 + 玻璃卡 + 标题行 + 关闭钮）与选项行全部复用 Base 层，
 * 因此和站内其它弹窗的观感完全一致。
 */
@Composable
fun AccountTypePickerDialog(
    onDismiss: () -> Unit,
    onPick: (AccountType) -> Unit
) {
    GlassDialogPanel(
        title = "选择账户类型",
        onDismiss = onDismiss
    ) {
        AccountTypes.all.forEach { (type, label, desc) ->
            GlassDialogOption(
                label = label,
                desc = desc,
                onClick = { onPick(type) }
            )
        }
    }
}
