package com.glass.ledger.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.glass.ledger.data.AccountEntity
import com.glass.ledger.data.AccountType
import com.glass.ledger.data.AccountTypes
import com.glass.ledger.ui.MainViewModel
import com.glass.ledger.ui.components.AccountIcon
import com.glass.ledger.ui.components.BalanceLineChart
import com.glass.ledger.ui.components.DialogEdgeToEdge
import com.glass.ledger.ui.components.GlassBackdrop
import com.glass.ledger.ui.components.GlassCard
import com.glass.ledger.ui.components.GlassCornerLarge
import com.glass.ledger.ui.components.GlassCornerPill
import com.glass.ledger.ui.components.clickableNoRipple
import com.glass.ledger.ui.components.liquidGlass
import com.glass.ledger.ui.money
import com.glass.ledger.ui.theme.ExpenseRed
import com.glass.ledger.ui.theme.IncomeGreen
import com.glass.ledger.ui.theme.InkPrimary
import com.glass.ledger.ui.theme.InkSecondary
import java.time.YearMonth
import kotlin.math.abs

/**
 * 账户详情页：顶部返回/账户名/修改账户，余额主卡（账户底色渐变 + 图标水印），
 * 年份切换，流入/流出年度合计，月度余额趋势图（自研玻璃曲线风格），
 * 月度账单表格（时间/流入/流出/余额），底部“记一笔”。
 */
@Composable
fun AccountDetailScreen(
    vm: MainViewModel,
    onBack: () -> Unit,
    onAddRecord: () -> Unit
) {
    val detail by vm.accountDetail.collectAsStateWithLifecycle()
    var showEdit by remember { mutableStateOf(false) }

    val d = detail ?: return
    val acc = d.account
    val accent = Color(acc.colorArgb)
    val thisYear = YearMonth.now().year

    Box(
        Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(Color(0xFFEFF2FC), Color(0xFFE4E9FA))))
    ) {
        GlassBackdrop()

        Column(
            Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(horizontal = 20.dp)
        ) {
            // ---- 顶栏：返回 / 账户名 / 修改账户 ----
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 8.dp)) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(40.dp)
                        .liquidGlass(CircleShape, alpha = 0.6f, elevation = 3.dp)
                        .clickableNoRipple(onBack)
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "返回",
                        tint = InkPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(Modifier.width(12.dp))
                Text(
                    acc.name,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = InkPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    "修改账户",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF4A5BE0),
                    modifier = Modifier
                        .clickableNoRipple { showEdit = true }
                        .padding(start = 10.dp, top = 4.dp, bottom = 4.dp)
                )
            }

            Spacer(Modifier.height(12.dp))

            LazyColumn(contentPadding = PaddingValues(bottom = 120.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                // ---- 余额主卡 ----
                item {
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .shadow(16.dp, GlassCornerLarge)
                            .background(
                                Brush.linearGradient(
                                    listOf(
                                        accent.copy(alpha = 0.92f),
                                        accent.copy(alpha = 0.78f).blendTowardsWhite(0.15f)
                                    )
                                ),
                                GlassCornerLarge
                            )
                            .border(1.dp, Color.White.copy(alpha = 0.4f), GlassCornerLarge)
                    ) {
                        // 图标水印
                        AccountIcon(
                            acc.iconId,
                            size = 100.dp,
                            name = acc.name,
                            colorArgb = acc.colorArgb,
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(end = 12.dp, bottom = 12.dp)
                                .graphicsLayer { alpha = 0.25f }
                        )
                        Column(Modifier.padding(20.dp)) {
                            Text(
                                AccountTypes.label(acc.type) + " · 当前余额",
                                fontSize = 12.sp,
                                color = Color.White.copy(alpha = 0.85f)
                            )
                            Text(
                                acc.balance.money(),
                                fontSize = 34.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                            if (acc.type == AccountType.CREDIT && acc.creditLimit != null) {
                                Text(
                                    "总额度 ${acc.creditLimit.money()} · 可用 ${(acc.creditLimit + acc.balance).money()}",
                                    fontSize = 12.sp,
                                    color = Color.White.copy(alpha = 0.85f),
                                    modifier = Modifier.padding(top = 8.dp)
                                )
                            }
                        }
                    }
                }

                // ---- 年份切换 ----
                item {
                    Row(
                        Modifier.fillMaxWidth().padding(top = 2.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        YearArrow("‹", enabled = true) { vm.shiftDetailYear(-1) }
                        Text(
                            "${d.year}年",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = InkPrimary,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.width(96.dp)
                        )
                        YearArrow("›", enabled = d.year < thisYear) { vm.shiftDetailYear(1) }
                    }
                    Text(
                        "${d.year}-01-01 - ${d.year}-12-31",
                        fontSize = 11.sp,
                        color = InkSecondary,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth().padding(top = 4.dp)
                    )
                }

                // ---- 流入 / 流出 年度合计 ----
                item {
                    GlassCard(contentPadding = 18.dp) {
                        Row(Modifier.fillMaxWidth()) {
                            Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("流入", fontSize = 12.sp, color = InkSecondary)
                                Text(
                                    "+${d.yearIncome.money()}",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = IncomeGreen,
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                            }
                            Box(Modifier.width(1.dp).height(40.dp).background(Color.White.copy(alpha = 0.7f)))
                            Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("流出", fontSize = 12.sp, color = InkSecondary)
                                Text(
                                    "-${d.yearExpense.money()}",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = ExpenseRed,
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                            }
                        }
                    }
                }

                // ---- 月度余额趋势图 ----
                item {
                    GlassCard {
                        Text("月度余额走势", fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = InkPrimary)
                        Spacer(Modifier.height(6.dp))
                        BalanceLineChart(
                            values = d.months.map { it.endBalance },
                            labels = d.months.map { it.label },
                            highlightIndex = d.months.indexOfFirst { it.label == "本月" },
                            accent = accent
                        )
                    }
                }

                // ---- 月度账单表格 ----
                val rows = d.months
                    .filter { it.income > 0 || it.expense > 0 }
                    .sortedByDescending { it.month }
                item {
                    Row(
                        Modifier.fillMaxWidth().padding(horizontal = 6.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("时间", fontSize = 12.sp, color = InkSecondary, modifier = Modifier.weight(1.1f))
                        Text("流入", fontSize = 12.sp, color = InkSecondary, modifier = Modifier.weight(1f), textAlign = TextAlign.End)
                        Text("流出", fontSize = 12.sp, color = InkSecondary, modifier = Modifier.weight(1f), textAlign = TextAlign.End)
                        Text("余额", fontSize = 12.sp, color = InkSecondary, modifier = Modifier.weight(1.2f), textAlign = TextAlign.End)
                    }
                }
                if (rows.isEmpty()) {
                    item {
                        Text(
                            "今年还没有这个账户的账单",
                            fontSize = 13.sp,
                            color = InkSecondary.copy(alpha = 0.8f),
                            modifier = Modifier.fillMaxWidth().padding(vertical = 18.dp),
                            textAlign = TextAlign.Center
                        )
                    }
                } else {
                    items(rows, key = { "m${it.month}" }) { m ->
                        Column {
                            Box(Modifier.fillMaxWidth().height(0.8.dp).background(Color.White.copy(alpha = 0.6f)))
                            Row(
                                Modifier.fillMaxWidth().padding(horizontal = 6.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(m.label, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = InkPrimary, modifier = Modifier.weight(1.1f))
                                Text(
                                    if (m.income > 0) "+${m.income.money()}" else "-",
                                    fontSize = 13.sp,
                                    color = if (m.income > 0) IncomeGreen else InkSecondary.copy(alpha = 0.5f),
                                    modifier = Modifier.weight(1f),
                                    textAlign = TextAlign.End
                                )
                                Text(
                                    if (m.expense > 0) "-${m.expense.money()}" else "-",
                                    fontSize = 13.sp,
                                    color = if (m.expense > 0) ExpenseRed else InkSecondary.copy(alpha = 0.5f),
                                    modifier = Modifier.weight(1f),
                                    textAlign = TextAlign.End
                                )
                                Text(
                                    m.endBalance.money(),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (m.endBalance < 0) ExpenseRed else InkPrimary,
                                    modifier = Modifier.weight(1.2f),
                                    textAlign = TextAlign.End
                                )
                            }
                        }
                    }
                }
            }
        }

        // ---- 底部“记一笔” ----
        val pillShape = RoundedCornerShape(26.dp)
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(bottom = 20.dp)
                .fillMaxWidth()
                .padding(horizontal = 44.dp)
                .shadow(12.dp, pillShape)
                .background(Brush.linearGradient(listOf(Color(0xFF6C7CFF), Color(0xFF5B6EF5))), pillShape)
                .border(1.2.dp, Color.White.copy(alpha = 0.65f), pillShape)
                .clickableNoRipple(onAddRecord)
        ) {
            Text("记一笔", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White, modifier = Modifier.padding(vertical = 14.dp))
        }

        // ---- 修改账户弹窗 ----
        if (showEdit) {
            EditAccountDialog(
                account = acc,
                onDismiss = { showEdit = false },
                onSave = { updated ->
                    vm.updateAccount(updated)
                    showEdit = false
                }
            )
        }
    }
}

@Composable
private fun YearArrow(symbol: String, enabled: Boolean, onClick: () -> Unit) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(34.dp)
            .then(
                if (enabled) Modifier.liquidGlass(CircleShape, alpha = 0.55f, elevation = 2.dp)
                else Modifier
            )
            .clickableNoRipple { if (enabled) onClick() }
    ) {
        Text(
            symbol,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = if (enabled) InkPrimary else InkSecondary.copy(alpha = 0.35f)
        )
    }
}

/** 向白色方向混合，用于卡片渐变亮端 */
private fun Color.blendTowardsWhite(t: Float): Color {
    fun mix(c: Float, w: Float) = c + (w - c) * t
    return Color(mix(red, 1f), mix(green, 1f), mix(blue, 1f), alpha)
}

/**
 * 修改账户：名称 + 余额（负债类账户输入欠款金额，保存时取负），信用卡还可改额度。
 */
@Composable
private fun EditAccountDialog(
    account: AccountEntity,
    onDismiss: () -> Unit,
    onSave: (AccountEntity) -> Unit
) {
    val isLiability = account.type == AccountType.CREDIT || account.type == AccountType.DEBT
    var name by remember { mutableStateOf(account.name) }
    var balanceText by remember {
        mutableStateOf(if (isLiability) abs(account.balance).toString() else account.balance.toString())
    }
    var limitText by remember { mutableStateOf(account.creditLimit?.toString() ?: "") }

    val canSave = name.isNotBlank() && balanceText.toDoubleOrNull() != null

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false)
    ) {
        DialogEdgeToEdge()
        Column(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 26.dp)
                .liquidGlass(GlassCornerLarge, alpha = 0.85f, elevation = 18.dp)
                .padding(22.dp)
        ) {
            Text("修改账户", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = InkPrimary)
            Spacer(Modifier.height(16.dp))

            EditField(value = name, hint = "账户名称") { name = it }

            Spacer(Modifier.height(10.dp))
            EditField(
                value = balanceText,
                hint = if (isLiability) "欠款金额（元）" else "当前余额（元）",
                numeric = true
            ) { balanceText = it }

            if (account.type == AccountType.CREDIT) {
                Spacer(Modifier.height(10.dp))
                EditField(value = limitText, hint = "总额度（元）", numeric = true) { limitText = it }
            }

            Spacer(Modifier.height(20.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    "取消",
                    fontSize = 15.sp,
                    color = InkSecondary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .weight(1f)
                        .liquidGlass(GlassCornerPill, alpha = 0.4f, elevation = 2.dp)
                        .clickableNoRipple(onDismiss)
                        .padding(vertical = 12.dp)
                )
                Text(
                    "保存",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (canSave) Color.White else InkSecondary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .weight(1f)
                        .then(
                            if (canSave) {
                                Modifier
                                    .shadow(8.dp, GlassCornerPill)
                                    .background(
                                        Brush.linearGradient(listOf(Color(0xFF6C7CFF), Color(0xFF5B6EF5))),
                                        GlassCornerPill
                                    )
                            } else {
                                Modifier.liquidGlass(GlassCornerPill, alpha = 0.4f, elevation = 2.dp)
                            }
                        )
                        .clickableNoRipple {
                            if (canSave) {
                                val input = balanceText.toDoubleOrNull() ?: 0.0
                                val newBalance = if (isLiability) -abs(input) else input
                                val newLimit = if (account.type == AccountType.CREDIT)
                                    limitText.toDoubleOrNull() ?: account.creditLimit
                                else account.creditLimit
                                onSave(
                                    account.copy(
                                        name = name.trim(),
                                        balance = newBalance,
                                        creditLimit = newLimit
                                    )
                                )
                            }
                        }
                        .padding(vertical = 12.dp)
                )
            }
        }
    }
}

@Composable
private fun EditField(
    value: String,
    hint: String,
    numeric: Boolean = false,
    onValueChange: (String) -> Unit
) {
    BasicTextField(
        value = value,
        onValueChange = { input ->
            if (!numeric || input.matches(Regex("\\d{0,8}(\\.\\d{0,2})?"))) onValueChange(input)
        },
        singleLine = true,
        textStyle = TextStyle(fontSize = 15.sp, color = InkPrimary),
        keyboardOptions = KeyboardOptions(keyboardType = if (numeric) KeyboardType.Decimal else KeyboardType.Text),
        decorationBox = { inner ->
            Box(
                Modifier.fillMaxWidth()
                    .liquidGlass(RoundedCornerShape(16.dp), alpha = 0.45f, elevation = 2.dp)
                    .padding(horizontal = 16.dp, vertical = 13.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                if (value.isEmpty()) {
                    Text(hint, fontSize = 15.sp, color = InkSecondary.copy(alpha = 0.7f))
                }
                inner()
            }
        }
    )
}
