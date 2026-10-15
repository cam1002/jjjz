package com.glass.ledger.ui

import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale

/** 金额格式化：1,234.56 */
fun Double.money(): String = String.format(Locale.US, "%,.2f", this)

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
