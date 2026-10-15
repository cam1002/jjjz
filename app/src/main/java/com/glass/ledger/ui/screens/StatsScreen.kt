package com.glass.ledger.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.glass.ledger.ui.MainViewModel
import com.glass.ledger.ui.StatsPeriod
import com.glass.ledger.ui.components.CategoryIcon
import com.glass.ledger.ui.components.GlassCard
import com.glass.ledger.ui.components.GlassSegmented
import com.glass.ledger.ui.components.PieChart
import com.glass.ledger.ui.components.PieSlice
import com.glass.ledger.ui.money
import com.glass.ledger.ui.theme.InkPrimary
import com.glass.ledger.ui.theme.InkSecondary
import java.time.LocalDate

/**
 * 统计页：当周 / 当月支出，按类别区分的圆形比例图 + 明细列表。
 */
@Composable
fun StatsScreen(vm: MainViewModel, contentPadding: PaddingValues) {
    val period by vm.statsPeriod.collectAsStateWithLifecycle()
    val stats by vm.stats.collectAsStateWithLifecycle()

    Column(
        Modifier
            .fillMaxSize()
            .padding(contentPadding)
            .padding(horizontal = 20.dp)
    ) {
        Row(
            Modifier.fillMaxWidth().padding(top = 14.dp, bottom = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "统计",
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = InkPrimary,
                modifier = Modifier.weight(1f)
            )
            GlassSegmented(
                options = listOf(StatsPeriod.WEEK to "当周", StatsPeriod.MONTH to "当月"),
                selected = period,
                onSelect = { vm.statsPeriod.value = it }
            )
        }

        LazyColumn(contentPadding = PaddingValues(bottom = 150.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            item {
                GlassCard {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        PieChart(
                            slices = stats.slices.map {
                                PieSlice(it.name, Color(it.colorArgb), it.total)
                            },
                            centerTitle = if (period == StatsPeriod.WEEK) "本周支出" else "本月支出",
                            centerValue = "¥${stats.totalExpense.money()}",
                            modifier = Modifier.size(150.dp)
                        )
                        Spacer(Modifier.width(10.dp))
                        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            StatLine("支出", stats.totalExpense, InkPrimary)
                            StatLine("收入", stats.totalIncome, InkPrimary)
                            StatLine(
                                "结余",
                                stats.totalIncome - stats.totalExpense,
                                if (stats.totalIncome - stats.totalExpense >= 0) Color(0xFF2FA36B) else Color(0xFFE5533B)
                            )
                            Text(
                                "周期：${periodLabel(period)}",
                                fontSize = 12.sp,
                                color = InkSecondary,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }
                }
            }

            item {
                Text(
                    "类别支出占比",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = InkPrimary
                )
            }

            if (stats.slices.isEmpty()) {
                item {
                    GlassCard {
                        Text(
                            "该周期暂无支出记录",
                            fontSize = 14.sp,
                            color = InkSecondary,
                            modifier = Modifier.fillMaxWidth().padding(vertical = 20.dp),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            } else {
                items(stats.slices, key = { it.name }) { slice ->
                    val pct = if (stats.totalExpense > 0) slice.total / stats.totalExpense else 0.0
                    Column(
                        Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(20.dp))
                            .background(Color.White.copy(alpha = 0.72f))
                            .padding(horizontal = 14.dp, vertical = 12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            CategoryIcon(slice.iconKey, slice.colorArgb, size = 34.dp)
                            Spacer(Modifier.width(10.dp))
                            Text(slice.name, fontSize = 15.sp, color = InkPrimary, modifier = Modifier.weight(1f))
                            Text("¥${slice.total.money()}", fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = InkPrimary)
                            Text(
                                "  ${(pct * 100).toInt()}%",
                                fontSize = 12.sp,
                                color = InkSecondary,
                                modifier = Modifier.width(46.dp)
                            )
                        }
                        Spacer(Modifier.height(8.dp))
                        // 占比条
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(Color.White.copy(alpha = 0.6f))
                        ) {
                            Spacer(
                                Modifier
                                    .fillMaxWidth(if (pct.isNaN()) 0f else pct.toFloat())
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp))
                                    .background(Color(slice.colorArgb))
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StatLine(label: String, value: Double, valueColor: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(label, fontSize = 13.sp, color = InkSecondary, modifier = Modifier.width(40.dp))
        Text("¥${value.money()}", fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = valueColor)
    }
}

private fun periodLabel(p: StatsPeriod): String {
    val today = LocalDate.now()
    return when (p) {
        StatsPeriod.WEEK -> {
            val (s, e) = com.glass.ledger.ui.weekRange(today)
            val sd = LocalDate.ofEpochDay(s)
            val ed = LocalDate.ofEpochDay(e)
            "${sd.monthValue}.${sd.dayOfMonth} - ${ed.monthValue}.${ed.dayOfMonth}"
        }
        StatsPeriod.MONTH -> "${today.year}年${today.monthValue}月"
    }
}
