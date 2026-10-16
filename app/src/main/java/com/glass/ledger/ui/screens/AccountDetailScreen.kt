package com.glass.ledger.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.glass.ledger.data.AccountEntity
import com.glass.ledger.data.AccountType
import com.glass.ledger.data.AccountTypes
import com.glass.ledger.data.isPayableAccount
import com.glass.ledger.data.signedBalanceOf
import com.glass.ledger.ui.MainViewModel
import com.glass.ledger.ui.base.GlassDialogPanel
import com.glass.ledger.ui.base.GlassPage
import com.glass.ledger.ui.base.GlassPrimaryButton
import com.glass.ledger.ui.base.GlassTokens
import com.glass.ledger.ui.components.AccountIcon
import com.glass.ledger.ui.components.BalanceLineChart
import com.glass.ledger.ui.components.GlassCard
import com.glass.ledger.ui.components.clickableNoRipple
import com.glass.ledger.ui.components.liquidGlass
import com.glass.ledger.ui.blendTowardsWhite
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
 *
 * 页面骨架、顶栏、状态栏、主按钮全部来自 Base 层。
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

    GlassPage(
        title = acc.name,
        onBack = onBack,
        scroll = false,
        trailing = {
            Text(
                "修改账户",
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = GlassTokens.AccentText,
                modifier = Modifier
                    .clickableNoRipple { showEdit = true }
                    .padding(start = 10.dp)
            )
        }
    ) {
        Box(Modifier.fillMaxWidth().weight(1f)) {
            LazyColumn(
                contentPadding = PaddingValues(bottom = 120.dp, top = GlassTokens.Space.Gap),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // ---- 余额主卡 ----
                item {
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .shadow(GlassTokens.Elevation.Overlay, GlassTokens.Corner.Card)
                            .background(
                                Brush.linearGradient(
                                    listOf(
                                        accent.copy(alpha = 0.92f),
                                        accent.copy(alpha = 0.78f).blendTowardsWhite(0.15f)
                                    )
                                ),
                                GlassTokens.Corner.Card
                            )
                            .border(1.dp, Color.White.copy(alpha = 0.55f), GlassTokens.Corner.Card)
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
                        Column(Modifier.padding(GlassTokens.Space.Page)) {
                            Text(
                                AccountTypes.label(acc.type) + " · 当前余额",
                                fontSize = GlassTokens.Type.Caption.sp,
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
                                    fontSize = GlassTokens.Type.Caption.sp,
                                    color = Color.White.copy(alpha = 0.85f),
                                    modifier = Modifier.padding(top = 8.dp)
                                )
                            }
                            // 开户时填的备注，没填就不占位
                            if (acc.note.isNotBlank()) {
                                Text(
                                    acc.note,
                                    fontSize = GlassTokens.Type.Caption.sp,
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
                                Text("流入", fontSize = GlassTokens.Type.Caption.sp, color = InkSecondary)
                                Text(
                                    "+${d.yearIncome.money()}",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = IncomeGreen,
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                            }
                            Box(
                                Modifier
                                    .width(1.dp)
                                    .height(40.dp)
                                    .background(Color.White.copy(alpha = 0.7f))
                            )
                            Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("流出", fontSize = GlassTokens.Type.Caption.sp, color = InkSecondary)
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
                        Text(
                            "月度余额走势",
                            fontSize = GlassTokens.Type.Body.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = InkPrimary
                        )
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
                        Text("时间", fontSize = GlassTokens.Type.Caption.sp, color = InkSecondary, modifier = Modifier.weight(1.1f))
                        Text("流入", fontSize = GlassTokens.Type.Caption.sp, color = InkSecondary, modifier = Modifier.weight(1f), textAlign = TextAlign.End)
                        Text("流出", fontSize = GlassTokens.Type.Caption.sp, color = InkSecondary, modifier = Modifier.weight(1f), textAlign = TextAlign.End)
                        Text("余额", fontSize = GlassTokens.Type.Caption.sp, color = InkSecondary, modifier = Modifier.weight(1.2f), textAlign = TextAlign.End)
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
                            Box(
                                Modifier
                                    .fillMaxWidth()
                                    .height(0.8.dp)
                                    .background(Color.White.copy(alpha = 0.6f))
                            )
                            Row(
                                Modifier.fillMaxWidth().padding(horizontal = 6.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(m.label, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = InkPrimary, modifier = Modifier.weight(1.1f))
                                Text(
                                    if (m.income > 0) "+${m.income.money()}" else "-",
                                    fontSize = 13.sp,
                                    color = if (m.income > 0) IncomeGreen else InkSecondary.copy(alpha = 0.72f),
                                    modifier = Modifier.weight(1f),
                                    textAlign = TextAlign.End
                                )
                                Text(
                                    if (m.expense > 0) "-${m.expense.money()}" else "-",
                                    fontSize = 13.sp,
                                    color = if (m.expense > 0) ExpenseRed else InkSecondary.copy(alpha = 0.72f),
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

            // ---- 底部“记一笔” ----
            GlassPrimaryButton(
                text = "记一笔",
                onClick = onAddRecord,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(horizontal = 44.dp, vertical = 20.dp)
            )
        }
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

@Composable
private fun YearArrow(symbol: String, enabled: Boolean, onClick: () -> Unit) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(34.dp)
            .then(
                if (enabled) Modifier.liquidGlass(
                    shape = GlassTokens.Corner.Circle,
                    alpha = GlassTokens.Surface.Chip,
                    elevation = GlassTokens.Elevation.Chip
                ) else Modifier
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

/**
 * 修改账户：名称 + 余额（负债类账户输入欠款金额，保存时取负），信用卡还可改额度，
 * 备注和开户表单一样能改（开户时填的备注要能改，不然只能删了重建）。
 * 正负号一律走 [signedBalanceOf] / [isPayableAccount]，和开户表单同一套规则：
 * 债务账户里的「借出」是别人欠我的（应收），不翻符号，提示语也单独一档。
 * 唯一例外是**溢缴款**（欠债类账户余额为正 = 钱还多了），它按资产算，回填和保存都不翻符号，
 * 否则"打开就保存"会把 +66 悄悄变成 -66。
 */
@Composable
private fun EditAccountDialog(
    account: AccountEntity,
    onDismiss: () -> Unit,
    onSave: (AccountEntity) -> Unit
) {
    val payable = isPayableAccount(account.type, account.iconId)
    /**
     * 溢缴款：欠债类账户（信用卡、借入）余额居然是正数 —— 钱还多了，多出来的部分是存在那边的
     * 资产（资产总额那条 SQL 就是靠 `balance > 0` 把它捞进去的）。
     * 这种账户不能按"欠款"口径回填再翻符号，否则字段什么都不动、只点一下保存就会把 +66 变成 -66，
     * 净资产凭空少 132。所以：回填原值、按正数存回去，只让它改大小，不改方向。
     */
    val overPaid = payable && account.balance > 0
    var name by remember { mutableStateOf(account.name) }
    var balanceText by remember {
        mutableStateOf(
            if (payable && !overPaid) abs(account.balance).toString() else account.balance.toString()
        )
    }
    var limitText by remember { mutableStateOf(account.creditLimit?.toString() ?: "") }
    var noteText by remember { mutableStateOf(account.note) }

    val balanceHint = when {
        overPaid -> "溢缴款（元）"
        account.type == AccountType.DEBT && !payable -> "借出金额（元）"
        payable -> "欠款金额（元）"
        else -> "当前余额（元）"
    }

    val canSave = name.isNotBlank() && balanceText.toDoubleOrNull() != null

    GlassDialogPanel(title = "修改账户", onDismiss = null, divider = false) {
        Spacer(Modifier.height(GlassTokens.Space.SectionTop))
        EditField(value = name, hint = "账户名称") { name = it }

        Spacer(Modifier.height(10.dp))
        EditField(
            value = balanceText,
            hint = balanceHint,
            numeric = true
        ) { balanceText = it }

        if (account.type == AccountType.CREDIT) {
            Spacer(Modifier.height(10.dp))
            EditField(value = limitText, hint = "总额度（元）", numeric = true) { limitText = it }
        }

        Spacer(Modifier.height(10.dp))
        EditField(value = noteText, hint = "备注（可选）") { noteText = it }

        Spacer(Modifier.height(20.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(
                "取消",
                fontSize = 15.sp,
                color = InkSecondary,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .weight(1f)
                    .liquidGlass(
                        shape = GlassTokens.Corner.Pill,
                        alpha = GlassTokens.Surface.Chip,
                        elevation = GlassTokens.Elevation.Chip
                    )
                    .clickableNoRipple(onDismiss)
                    .padding(vertical = 12.dp)
            )
            GlassPrimaryButton(
                text = "保存",
                enabled = canSave,
                modifier = Modifier.weight(1f),
                onClick = {
                    val input = balanceText.toDoubleOrNull() ?: 0.0
                    // 溢缴款保持正数（见上面 overPaid 的注释），其余情况一律由图标决定方向
                    val newBalance = if (overPaid) input.coerceAtLeast(0.0)
                    else signedBalanceOf(account.type, account.iconId, input)
                    val newLimit = if (account.type == AccountType.CREDIT)
                        limitText.toDoubleOrNull() ?: account.creditLimit
                    else account.creditLimit
                    onSave(account.copy(
                        name = name.trim(),
                        balance = newBalance,
                        creditLimit = newLimit,
                        note = noteText.trim()
                    ))
                }
            )
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
        textStyle = TextStyle(fontSize = GlassTokens.Type.Body.sp, color = InkPrimary),
        keyboardOptions = KeyboardOptions(keyboardType = if (numeric) KeyboardType.Decimal else KeyboardType.Text),
        modifier = Modifier.fillMaxWidth(),
        decorationBox = { inner ->
            Box(
                Modifier
                    .fillMaxWidth()
                    .liquidGlass(
                        shape = GlassTokens.Corner.Chip,
                        alpha = GlassTokens.Surface.Chip,
                        elevation = GlassTokens.Elevation.Chip
                    )
                    .padding(horizontal = 16.dp, vertical = 13.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                if (value.isEmpty()) {
                    Text(hint, fontSize = GlassTokens.Type.Body.sp, color = InkSecondary.copy(alpha = 0.7f))
                }
                inner()
            }
        }
    )
}
