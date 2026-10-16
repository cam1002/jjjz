package com.glass.ledger.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.glass.ledger.data.RecordType
import com.glass.ledger.data.RecordView
import com.glass.ledger.ui.MainViewModel
import com.glass.ledger.ui.base.GlassIconButton
import com.glass.ledger.ui.base.GlassPage
import com.glass.ledger.ui.base.GlassTokens
import com.glass.ledger.ui.blendTowardsWhite
import com.glass.ledger.ui.components.AccountIcon
import com.glass.ledger.ui.components.DottedLeader
import com.glass.ledger.ui.components.GlassConfirmDialog
import com.glass.ledger.ui.components.ReceiptCategoryBadge
import com.glass.ledger.ui.components.ReceiptSurface
import com.glass.ledger.ui.moneyShort
import com.glass.ledger.ui.theme.ExpenseRed
import com.glass.ledger.ui.theme.IncomeGreen
import com.glass.ledger.ui.theme.InkPrimary
import com.glass.ledger.ui.theme.InkSecondary
import java.time.LocalDate
import java.time.format.DateTimeFormatter

private val RECEIPT_TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")

/** 账本名：目前只有一个账本，按需求只做展示 */
private const val LEDGER_NAME = "日常"

/**
 * 账单详情页（收据样式）：分类色通栏 + 扇贝撕边白纸 + 压边分类徽标 + 点线明细 + 三个圆形操作钮。
 *
 * 数据来自 [MainViewModel.recordDetail]，它直接观察这一条流水，
 * 所以退款、修改都会即时刷新；这条记录被删除时外层拿到 null，页面自动关闭。
 */
@Composable
fun RecordDetailScreen(
    vm: MainViewModel,
    d: RecordView,
    onBack: () -> Unit,
    onEdit: (RecordView) -> Unit
) {
    val r = d.record
    val accent = Color(d.categoryColor)
    val isExpense = r.type == RecordType.EXPENSE
    var confirmDelete by remember { mutableStateOf(false) }

    GlassPage(
        title = "",
        onBack = onBack,
        scroll = false,
        horizontalPadding = 14.dp,
        topBand = accent.blendTowardsWhite(0.74f),
        topBandHeight = 250.dp
    ) {
        Box(
            Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            ReceiptSurface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                badge = { ReceiptCategoryBadge(d.categoryIconKey, d.categoryColor) }
            ) {
                Text(
                    d.categoryName,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = accent,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    (if (isExpense) "-" else "+") + r.amount.moneyShort(),
                    fontSize = 36.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (r.refunded) InkSecondary else InkPrimary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
                if (r.refunded) {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "已退款",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = ExpenseRed,
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .clip(GlassTokens.Corner.Pill)
                            .background(ExpenseRed.copy(alpha = 0.1f))
                            .padding(horizontal = 12.dp, vertical = 4.dp)
                    )
                }
                Spacer(Modifier.height(16.dp))
                DottedLeader(Modifier.fillMaxWidth())
                Spacer(Modifier.height(2.dp))
                ReceiptLine("账户") {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        AccountIcon(d.accountIconId, size = 20.dp, name = d.accountName, colorArgb = d.accountColor)
                        Spacer(Modifier.width(7.dp))
                        Text(
                            d.accountName,
                            fontSize = 14.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(d.accountColor)
                        )
                    }
                }
                ReceiptLine("日期") {
                    Text(
                        LocalDate.ofEpochDay(r.dateEpochDay)
                            .atStartOfDay()
                            .plusSeconds(r.timeOfDay.toLong())
                            .format(RECEIPT_TIME),
                        fontSize = 14.5.sp,
                        color = InkPrimary
                    )
                }
                ReceiptLine("账本") {
                    Text(LEDGER_NAME, fontSize = 14.5.sp, color = InkPrimary)
                }
                if (r.note.isNotBlank()) {
                    ReceiptLine("备注", leader = false) {
                        Text(
                            r.note,
                            fontSize = 14.5.sp,
                            color = InkPrimary,
                            maxLines = 3,
                            textAlign = TextAlign.End,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
                Spacer(Modifier.height(2.dp))
                DottedLeader(Modifier.fillMaxWidth())
            }

            // 底部三个操作钮
            Row(
                Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(44.dp, Alignment.CenterHorizontally),
                verticalAlignment = Alignment.Bottom
            ) {
                ReceiptAction(
                    label = "退款",
                    icon = Icons.AutoMirrored.Filled.Undo,
                    tint = if (isExpense) IncomeGreen else GlassTokens.BrandEnd,
                    enabled = !r.refunded,
                    onClick = { vm.refundRecord(r) }
                )
                ReceiptAction("修改", Icons.Filled.Edit) { onEdit(d) }
                ReceiptAction("删除", Icons.Filled.Delete, tint = ExpenseRed) { confirmDelete = true }
            }
        }
    }

    if (confirmDelete) {
        GlassConfirmDialog(
            title = "删除这笔记录？",
            body = if (r.refunded)
                "这笔已退款、余额已回补，删除时不会再调整账户余额。"
            else
                "删除后账户余额将同步回退。",
            onDismiss = { confirmDelete = false },
            onConfirm = { vm.deleteRecord(r); onBack() }
        )
    }
}

/**
 * 明细行：空心圆点 + 标签 + 点线引导 + 右侧内容。
 * [leader] = false 时不给点线，内容占满剩余宽度（备注这类长文本用）。
 */
@Composable
private fun ReceiptLine(
    label: String,
    leader: Boolean = true,
    value: @Composable RowScope.() -> Unit
) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val rowScope = this
        Box(
            Modifier
                .size(9.dp)
                .clip(CircleShape)
                .border(1.4.dp, Color(0xFFAEB6CE), CircleShape)
        )
        Spacer(Modifier.width(9.dp))
        Text(label, fontSize = 14.sp, color = InkSecondary)
        Spacer(Modifier.width(10.dp))
        if (leader) {
            DottedLeader(
                Modifier
                    .weight(1f)
                    .padding(horizontal = 2.dp),
                color = Color(0xFFD6DBEA),
                dotSize = 2.2.dp,
                gapRatio = 2.8f
            )
            Spacer(Modifier.width(12.dp))
            value()
        } else {
            Box(Modifier.weight(1f), contentAlignment = Alignment.CenterEnd) {
                // value 需要 RowScope（里面的 Text 用了 weight/fillMaxWidth），把外层 Row 的作用域显式带进去
                with(rowScope) { value() }
            }
        }
    }
}

/** 收据页底部的圆形操作钮：白玻璃圆 + 下方文字标签 */
@Composable
private fun ReceiptAction(
    label: String,
    icon: ImageVector,
    tint: Color = InkPrimary,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        GlassIconButton(
            onClick = { if (enabled) onClick() },
            size = 56.dp
        ) {
            Icon(
                icon,
                contentDescription = label,
                tint = if (enabled) tint else InkSecondary.copy(alpha = 0.4f),
                modifier = Modifier.size(23.dp)
            )
        }
        Spacer(Modifier.height(7.dp))
        Text(
            label,
            fontSize = 12.5.sp,
            fontWeight = FontWeight.Medium,
            color = if (enabled) InkSecondary else InkSecondary.copy(alpha = 0.4f)
        )
    }
}
