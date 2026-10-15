package com.glass.ledger.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.glass.ledger.ui.base.GlassDialogPanel
import com.glass.ledger.ui.base.GlassPrimaryButton
import com.glass.ledger.ui.base.GlassTokens
import com.glass.ledger.ui.components.NumberWheel
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.YearMonth

/**
 * 滚轮式日期时间选择器：年 / 月 / 日 / 时 / 分，样式对齐参考截图。
 * 支持"当前日期"一键回填，确定后返回 LocalDateTime。
 */
@Composable
fun DateTimeWheelDialog(
    initial: LocalDateTime,
    onDismiss: () -> Unit,
    onConfirm: (LocalDateTime) -> Unit
) {
    var year by remember { mutableStateOf(initial.year) }
    var month by remember { mutableStateOf(initial.monthValue) }
    var day by remember { mutableStateOf(initial.dayOfMonth) }
    var hour by remember { mutableStateOf(initial.hour) }
    var minute by remember { mutableStateOf(initial.minute) }

    val years = remember { (2020..(LocalDate.now().year + 5)).toList() }
    val months = remember { (1..12).toList() }
    val daysInMonth = YearMonth.of(year, month).lengthOfMonth()
    val days = remember(daysInMonth) { (1..daysInMonth).toList() }
    val hours = remember { (0..23).toList() }
    val minutes = remember { (0..59).toList() }

    // 换月/换年后天数可能非法（如 31 日 -> 2 月）
    LaunchedEffect(daysInMonth) {
        if (day > daysInMonth) day = daysInMonth
    }

    GlassDialogPanel(
        title = "修改日期",
        onDismiss = onDismiss,
        divider = false
    ) {

            // 当前日期
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                Text(
                    "当前日期",
                    fontSize = 13.sp,
                    color = GlassTokens.BrandEnd,
                    modifier = Modifier
                        .clip(GlassTokens.Corner.Pill)
                        .background(GlassTokens.BrandSoft)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {
                            val now = LocalDateTime.now()
                            year = now.year; month = now.monthValue; day = now.dayOfMonth
                            hour = now.hour; minute = now.minute
                        }
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                )
            }

            Spacer(Modifier.height(10.dp))

            // 五个滚轮：年 月 日 时 分
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                NumberWheel(
                    values = years,
                    initialIndex = years.indexOf(year),
                    modifier = Modifier.weight(1.35f),
                    onSelected = { year = years[it] }
                )
                WheelSuffix("年")
                NumberWheel(
                    values = months,
                    initialIndex = month - 1,
                    modifier = Modifier.weight(0.9f),
                    showTwoDigits = false,
                    onSelected = { month = it + 1 }
                )
                WheelSuffix("月")
                NumberWheel(
                    values = days,
                    initialIndex = (day - 1).coerceAtMost(daysInMonth - 1),
                    modifier = Modifier.weight(0.9f),
                    onSelected = { day = it + 1 }
                )
                WheelSuffix("日")
                NumberWheel(
                    values = hours,
                    initialIndex = hour,
                    modifier = Modifier.weight(0.85f),
                    showTwoDigits = true,
                    onSelected = { hour = it }
                )
                NumberWheel(
                    values = minutes,
                    initialIndex = minute,
                    modifier = Modifier.weight(0.85f),
                    showTwoDigits = true,
                    onSelected = { minute = it }
                )
            }

            Spacer(Modifier.height(18.dp))

            GlassPrimaryButton(
                text = "确定",
                onClick = {
                    onConfirm(LocalDateTime.of(year, month, day.coerceAtMost(daysInMonth), hour, minute))
                },
                brush = Brush.linearGradient(listOf(Color(0xFF7FB3E8), Color(0xFF6C9FE0)))
            )
    }
}

@Composable
private fun WheelSuffix(text: String) {
    Text(
        text,
        fontSize = 13.sp,
        color = Color(0xFF6A7088),
        modifier = Modifier.padding(horizontal = 2.dp)
    )
}
