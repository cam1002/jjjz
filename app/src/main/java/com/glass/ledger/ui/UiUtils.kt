package com.glass.ledger.ui

import androidx.compose.ui.graphics.Color
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale

/** 金额格式化：1,234.56 */
fun Double.money(): String = String.format(Locale.US, "%,.2f", this)

/**
 * 收据式金额：整数就不带小数（-219），有小数才保留两位（-219.50）。
 * 账单详情页用，列表/概览仍用 [money]。
 */
fun Double.moneyShort(): String =
    if (this % 1.0 == 0.0) String.format(Locale.US, "%,.0f", this) else money()

/** 向白色方向混合，用于卡片渐变亮端、分类色淡化 */
fun Color.blendTowardsWhite(t: Float): Color {
    fun mix(c: Float, w: Float) = c + (w - c) * t
    return Color(mix(red, 1f), mix(green, 1f), mix(blue, 1f), alpha)
}

fun YearMonth.range(): Pair<Long, Long> =
    atDay(1).toEpochDay() to atEndOfMonth().toEpochDay()

/** 本周一 ~ 本周日 */
fun weekRange(today: LocalDate = LocalDate.now()): Pair<Long, Long> {
    val mondayOffset = (today.dayOfWeek.value - 1).toLong()
    val monday = today.minusDays(mondayOffset)
    return monday.toEpochDay() to monday.plusDays(6).toEpochDay()
}

private val CN_MONTH = DateTimeFormatter.ofPattern("yyyy年M月", Locale.CHINA)
private val CN_DATE = DateTimeFormatter.ofPattern("M月d日", Locale.CHINA)
private val WEEK_NAMES = arrayOf("星期一", "星期二", "星期三", "星期四", "星期五", "星期六", "星期日")

fun YearMonth.cn(): String = format(CN_MONTH)

fun LocalDate.cnDate(): String = format(CN_DATE)

fun LocalDate.cnWeekday(): String = WEEK_NAMES[dayOfWeek.value - 1]

fun LocalDate.cnLabel(today: LocalDate = LocalDate.now()): String = when (this) {
    today -> "今天"
    today.minusDays(1) -> "昨天"
    else -> cnWeekday()
}
