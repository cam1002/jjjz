package com.glass.ledger.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.glass.ledger.data.RecordEntity
import com.glass.ledger.data.RecordType
import com.glass.ledger.data.RecordView
import com.glass.ledger.ui.MainViewModel
import com.glass.ledger.ui.cn
import com.glass.ledger.ui.cnDate
import com.glass.ledger.ui.cnLabel
import com.glass.ledger.ui.base.GlassTokens
import com.glass.ledger.ui.components.AccountIcon
import com.glass.ledger.ui.components.CategoryIcon
import com.glass.ledger.ui.components.GlassCard
import com.glass.ledger.ui.components.GlassConfirmDialog
import com.glass.ledger.ui.components.clickableNoRipple
import com.glass.ledger.ui.components.liquidGlass
import com.glass.ledger.ui.money
import com.glass.ledger.ui.theme.ExpenseRed
import com.glass.ledger.ui.theme.IncomeGreen
import com.glass.ledger.ui.theme.InkPrimary
import com.glass.ledger.ui.theme.InkSecondary
import java.time.YearMonth

@Composable
fun LedgerScreen(vm: MainViewModel, contentPadding: PaddingValues) {
    val month = vm.selectedMonth.collectAsStateWithLifecycle().value
    val groups = vm.monthRecords.collectAsStateWithLifecycle().value
    val (expense, income) = vm.monthSummary.collectAsStateWithLifecycle().value

    var deleting by remember { mutableStateOf<RecordEntity?>(null) }

    Column(
        Modifier
            .fillMaxSize()
            .padding(contentPadding)
            .padding(horizontal = 20.dp)
    ) {
        // 顶部标题 + 月份切换
        Row(
            Modifier
                .fillMaxWidth()
                .padding(top = 14.dp, bottom = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "收支",
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = InkPrimary,
                modifier = Modifier.weight(1f)
            )
            MonthNavigator(month = month, onShift = { vm.shiftMonth(it) })
        }

        // 当月概览
        GlassCard {
            Text("当月支出", style = MaterialTheme.typography.bodyMedium, color = InkSecondary)
            Text(
                "¥${expense.money()}",
                fontSize = 36.sp,
                fontWeight = FontWeight.Bold,
                color = InkPrimary,
                modifier = Modifier.padding(top = 4.dp)
            )
            Spacer(Modifier.height(14.dp))
            Row(Modifier.fillMaxWidth()) {
                SummaryItem("收入", income, IncomeGreen, Modifier.weight(1f))
                SummaryItem("结余", income - expense, if (income - expense >= 0) IncomeGreen else ExpenseRed, Modifier.weight(1f))
            }
        }

        Spacer(Modifier.height(16.dp))
        Text(
            if (groups.isEmpty()) "近期账单" else "${month.cn()}账单",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = InkPrimary,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        if (groups.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(bottom = 120.dp), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("暂无相关数据", color = InkSecondary, fontSize = 16.sp)
                    Text("点击右下角 + 记一笔", color = InkSecondary.copy(alpha = 0.7f), fontSize = 13.sp, modifier = Modifier.padding(top = 6.dp))
                }
            }
        } else {
            LazyColumn(
                Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 150.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                groups.forEach { group ->
                    item {
                        Row(
                            Modifier.padding(top = 8.dp, bottom = 2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("${group.date.cnDate()} ${group.date.cnLabel()}", fontSize = 13.sp, color = InkSecondary)
                            Text("  |  ", fontSize = 13.sp, color = InkSecondary.copy(alpha = 0.6f))
                            Text("支 ${group.expense.money()}", fontSize = 13.sp, color = ExpenseRed)
                        }
                    }
                    items(group.records, key = { it.record.id }) { rv ->
                        RecordRow(rv, onLongPress = { deleting = rv.record })
                    }
                }
            }
        }
    }

    deleting?.let { rec ->
        GlassConfirmDialog(
            title = "删除这笔记录？",
            body = "删除后账户余额将同步回退。",
            onDismiss = { deleting = null },
            onConfirm = { vm.deleteRecord(rec); deleting = null }
        )
    }
}

@Composable
private fun MonthNavigator(month: YearMonth, onShift: (Long) -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        modifier = Modifier.liquidGlass(
            shape = GlassTokens.Corner.Pill,
            alpha = GlassTokens.Surface.Chip,
            elevation = GlassTokens.Elevation.Field
        )
    ) {
        Icon(
            Icons.AutoMirrored.Filled.KeyboardArrowLeft, "上一月",
            tint = InkPrimary,
            modifier = Modifier.size(30.dp).clickableNoRipple { onShift(-1) }.padding(3.dp)
        )
        Text(
            month.cn(),
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold,
            color = InkPrimary
        )
        Icon(
            Icons.AutoMirrored.Filled.KeyboardArrowRight, "下一月",
            tint = InkPrimary,
            modifier = Modifier.size(30.dp).clickableNoRipple { onShift(1) }.padding(3.dp)
        )
    }
}

@Composable
private fun SummaryItem(label: String, value: Double, color: Color, modifier: Modifier = Modifier) {
    Column(modifier) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(label, fontSize = 13.sp, color = InkSecondary)
        }
        Text(
            "¥${value.money()}",
            fontSize = 17.sp,
            fontWeight = FontWeight.SemiBold,
            color = InkPrimary,
            modifier = Modifier.padding(top = 2.dp)
        )
    }
}

@Composable
private fun RecordRow(rv: RecordView, onLongPress: () -> Unit) {
    val isExpense = rv.record.type == RecordType.EXPENSE
    Row(
        Modifier
            .fillMaxWidth()
            .liquidGlass(
                shape = GlassTokens.Corner.Field,
                alpha = GlassTokens.Surface.Card,
                elevation = GlassTokens.Elevation.Card
            )
            .clickableNoRipple(onLongPress)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        CategoryIcon(rv.categoryIconKey, rv.categoryColor)
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(rv.categoryName, fontSize = 15.sp, fontWeight = FontWeight.Medium, color = InkPrimary)
            val timeStr = if (rv.record.timeOfDay > 0)
                "%02d:%02d".format(rv.record.timeOfDay / 3600, (rv.record.timeOfDay % 3600) / 60)
            else ""
            val sub = listOf(rv.record.note, timeStr).filter { it.isNotBlank() }.joinToString(" · ")
            if (sub.isNotBlank()) {
                Text(sub, fontSize = 12.sp, color = InkSecondary, maxLines = 1)
            }
        }
        Column(horizontalAlignment = Alignment.End) {
            Text(
                (if (isExpense) "-" else "+") + rv.record.amount.money(),
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (isExpense) ExpenseRed else IncomeGreen
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(top = 3.dp)
                    .liquidGlass(
                        shape = GlassTokens.Corner.Tag,
                        alpha = GlassTokens.Surface.Chip,
                        elevation = GlassTokens.Elevation.Flat,
                        highlight = false
                    )
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                AccountIcon(rv.accountIconId, size = 12.dp, name = rv.accountName, colorArgb = rv.accountColor)
                Spacer(Modifier.width(4.dp))
                Text(rv.accountName, fontSize = 10.sp, color = InkSecondary)
            }
        }
    }
}
