package com.glass.ledger.ui.screens

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.glass.ledger.data.AccountEntity
import com.glass.ledger.data.IconCatalog
import com.glass.ledger.data.NO_ACCOUNT_ICON
import com.glass.ledger.data.NO_ACCOUNT_ID
import com.glass.ledger.ui.base.GlassBottomSheet
import com.glass.ledger.ui.base.GlassSheetRow
import com.glass.ledger.ui.base.GlassTokens
import com.glass.ledger.ui.components.AccountIcon
import com.glass.ledger.ui.moneyShort
import com.glass.ledger.ui.theme.ExpenseRed
import com.glass.ledger.ui.theme.InkPrimary
import com.glass.ledger.ui.theme.InkSecondary

/**
 * 记一笔里点账户胶囊弹出的「选择账户」抽屉（按截图 1 的排布）：
 * 第一行固定是「不计入账户」（灰色铅笔图标、右边不给余额），往下每个账户一行：
 * 图标 + 名称 + 右对齐余额，负数用红色，行与行之间一条细线。
 *
 * 账户不按收支类型过滤：截图里支出、收入、转账共用同一份账户列表，
 * 而且把钱从一张卡转到另一张卡本来就该能选任意账户。
 */
@Composable
fun AccountPickerSheet(
    accounts: List<AccountEntity>,
    selectedId: Long,
    onSelect: (Long) -> Unit,
    onDismiss: () -> Unit
) {
    GlassBottomSheet(title = "选择账户", onDismiss = onDismiss) {
        Spacer(Modifier.height(6.dp))
        // 「不计入账户」永远在第一位：这笔照常记账、照常进统计，只是不动任何账户余额
        GlassSheetRow(
            title = "不计入账户",
            selected = selectedId == NO_ACCOUNT_ID,
            onClick = { onSelect(NO_ACCOUNT_ID) },
            leading = {
                AccountIcon(
                    NO_ACCOUNT_ICON,
                    size = 36.dp,
                    name = "不",
                    colorArgb = IconCatalog.colorOf(NO_ACCOUNT_ICON)
                )
            }
        )
        Spacer(Modifier.height(4.dp))
        accounts.forEachIndexed { index, acc ->
            GlassSheetRow(
                title = acc.name,
                selected = acc.id == selectedId,
                onClick = { onSelect(acc.id) },
                leading = { AccountIcon(acc.iconId, size = 36.dp, name = acc.name, colorArgb = acc.colorArgb) },
                trailing = acc.balance.moneyShort(),
                trailingColor = if (acc.balance < 0) ExpenseRed else InkPrimary,
                divider = index != accounts.lastIndex
            )
        }
        Spacer(Modifier.height(GlassTokens.Space.Gap))
        Text(
            "选「不计入账户」时这笔照常记账，只是不改动任何账户的余额",
            fontSize = GlassTokens.Type.Caption.sp,
            color = InkSecondary
        )
        Spacer(Modifier.height(GlassTokens.Space.Bottom))
    }
}
