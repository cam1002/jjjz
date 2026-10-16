package com.glass.ledger.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.glass.ledger.data.AccountEntity
import com.glass.ledger.data.AccountType
import com.glass.ledger.data.IconCatalog
import com.glass.ledger.data.NO_ACCOUNT_ICON
import com.glass.ledger.data.NO_ACCOUNT_ID
import com.glass.ledger.data.RecordType
import com.glass.ledger.data.RecordView
import com.glass.ledger.ui.MainViewModel
import com.glass.ledger.ui.base.GlassPageDialog
import com.glass.ledger.ui.base.GlassPrimaryButton
import com.glass.ledger.ui.base.GlassTokens
import com.glass.ledger.ui.components.AccountIcon
import com.glass.ledger.ui.components.CategoryIcon
import com.glass.ledger.ui.components.clickableNoRipple
import com.glass.ledger.ui.components.liquidGlass
import com.glass.ledger.ui.theme.InkPrimary
import com.glass.ledger.ui.theme.InkSecondary

/**
 * 记一笔（全屏页）：支出/收入切换、账户、金额、分类、日期、备注。
 *
 * 账户这一栏按截图做成"一个胶囊 + 抽屉选择器"：切换按钮下面只显示当前账户一个胶囊，
 * 默认是上次用过的账户（[MainViewModel.lastAccountId]），点它弹 [AccountPickerSheet]。
 * 备注原来是挤在日期旁边的一个小输入框，现在挪到日期下面单独一行。
 *
 * presetAccountId 非 0 时预选该账户（从账户详情页进入）。
 * editing 非空时是"修改这笔"：全部字段回填，保存走 [MainViewModel.saveRecordEdit]（余额按新旧值重放）。
 *
 * 骨架、顶栏、状态栏、主按钮全部来自 Base 层（GlassPageDialog / GlassPrimaryButton / GlassTokens）。
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AddRecordSheet(
    vm: MainViewModel,
    presetAccountId: Long = 0L,
    editing: RecordView? = null,
    onDismiss: () -> Unit
) {
    val accounts = vm.accounts.collectAsStateWithLifecycle().value
    val accountsLoaded = vm.accountsLoaded.collectAsStateWithLifecycle().value
    val expenseCats = vm.expenseCategories.collectAsStateWithLifecycle().value
    val incomeCats = vm.incomeCategories.collectAsStateWithLifecycle().value

    val origin = editing?.record
    var type by remember { mutableStateOf(origin?.type ?: RecordType.EXPENSE) }
    var amountText by remember { mutableStateOf(origin?.amount?.let(::amountToText) ?: "") }
    var categoryId by remember { mutableStateOf(origin?.categoryId ?: 0L) }
    /** 0 = 还没定下来（账户列表还在加载），此时保存按钮是灰的 */
    var accountId by remember { mutableStateOf(origin?.accountId ?: presetAccountId) }
    var dateTime by remember {
        mutableStateOf(
            origin?.let {
                java.time.LocalDate.ofEpochDay(it.dateEpochDay)
                    .atStartOfDay()
                    .plusSeconds(it.timeOfDay.toLong())
            } ?: java.time.LocalDateTime.now()
        )
    }
    var note by remember { mutableStateOf(origin?.note ?: "") }
    var showDatePicker by remember { mutableStateOf(false) }
    var showAccountPicker by remember { mutableStateOf(false) }
    var accountResolved by remember { mutableStateOf(false) }

    // 默认账户要等 Room 把列表读出来才能定：上次那个还在就用上次的，
    // 被删了就退到第一个，一个账户都没有就只能记到「不计入账户」。
    // 「不计入账户」是哨兵 id，账户列表里查不到，所以要先认它，否则会被当成失效的账户丢掉。
    LaunchedEffect(accounts, accountsLoaded, accountResolved) {
        if (accountResolved || !accountsLoaded) return@LaunchedEffect
        if (accountId == 0L) {
            accountId = when {
                vm.lastAccountId == NO_ACCOUNT_ID -> NO_ACCOUNT_ID
                accounts.any { it.id == vm.lastAccountId } -> vm.lastAccountId
                accounts.isNotEmpty() -> accounts.first().id
                else -> NO_ACCOUNT_ID
            }
        }
        accountResolved = true
    }

    val cats = if (type == RecordType.EXPENSE) expenseCats else incomeCats
    val amount = amountText.toDoubleOrNull() ?: 0.0
    val canSave = amount > 0 && categoryId != 0L && accountId != 0L

    GlassPageDialog(
        onDismiss = onDismiss,
        title = if (editing == null) "记一笔" else "编辑记录",
        subtitle = editing?.let { "修改后账户余额会自动重算" }
    ) {
        // ---- 支出 / 收入 ----
        Row(
            Modifier
                .fillMaxWidth()
                .liquidGlass(
                    shape = GlassTokens.Corner.Pill,
                    alpha = GlassTokens.Surface.Chip,
                    elevation = GlassTokens.Elevation.Chip
                )
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            TypeChip("支出", type == RecordType.EXPENSE, Modifier.weight(1f)) {
                type = RecordType.EXPENSE; categoryId = 0L
            }
            TypeChip("收入", type == RecordType.INCOME, Modifier.weight(1f)) {
                type = RecordType.INCOME; categoryId = 0L
            }
        }

        Spacer(Modifier.height(GlassTokens.Space.SectionTop))

        // ---- 当前账户（一个胶囊，点开是选择账户抽屉） ----
        AccountPill(
            account = displayAccountOf(accountId, accounts),
            onClick = { showAccountPicker = true }
        )

        Spacer(Modifier.height(GlassTokens.Space.SectionTop))

        // ---- 金额 ----
        Row(
            Modifier
                .fillMaxWidth()
                .liquidGlass(
                    shape = GlassTokens.Corner.Field,
                    alpha = GlassTokens.Surface.Chip,
                    elevation = GlassTokens.Elevation.Field
                )
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("¥ ", fontSize = 26.sp, fontWeight = FontWeight.Bold, color = InkPrimary)
            BasicTextField(
                value = amountText,
                onValueChange = { input ->
                    if (input.matches(Regex("\\d{0,8}(\\.\\d{0,2})?"))) amountText = input
                },
                singleLine = true,
                textStyle = TextStyle(fontSize = 26.sp, fontWeight = FontWeight.Bold, color = InkPrimary),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.weight(1f)
            )
            if (amountText.isNotEmpty()) {
                Text(
                    "清除",
                    fontSize = 13.sp,
                    color = InkSecondary,
                    modifier = Modifier.clickableNoRipple { amountText = "" }
                )
            }
        }

        Spacer(Modifier.height(GlassTokens.Space.SectionTop))
        SectionHeader("分类")
        Spacer(Modifier.height(GlassTokens.Space.Row))
        FlowRow(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            cats.forEach { c ->
                val sel = c.id == categoryId
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .then(
                            if (sel) Modifier.liquidGlass(
                                shape = GlassTokens.Corner.Chip,
                                alpha = GlassTokens.Surface.Card,
                                elevation = GlassTokens.Elevation.Chip
                            ) else Modifier.padding(4.dp)
                        )
                        .clickableNoRipple { categoryId = c.id }
                        .padding(horizontal = 8.dp, vertical = 6.dp)
                ) {
                    CategoryIcon(c.iconKey, c.colorArgb, size = 34.dp)
                    Text(
                        c.name,
                        fontSize = 11.sp,
                        color = if (sel) InkPrimary else InkSecondary,
                        fontWeight = if (sel) FontWeight.SemiBold else FontWeight.Normal,
                        modifier = Modifier.padding(top = 3.dp)
                    )
                }
            }
        }

        Spacer(Modifier.height(GlassTokens.Space.SectionTop))

        // ---- 日期（单独一行，旁边不再放备注输入框） ----
        Row(
            Modifier
                .liquidGlass(
                    shape = GlassTokens.Corner.Pill,
                    alpha = GlassTokens.Surface.Chip,
                    elevation = GlassTokens.Elevation.Chip
                )
                .clickableNoRipple { showDatePicker = true }
                .padding(horizontal = 14.dp, vertical = 9.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("日期", fontSize = 13.sp, color = InkSecondary)
            Spacer(Modifier.width(8.dp))
            Text(
                "${dateTime.year}年${dateTime.monthValue}月${dateTime.dayOfMonth}日 " +
                    "%02d:%02d".format(dateTime.hour, dateTime.minute),
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = InkPrimary
            )
        }

        Spacer(Modifier.height(GlassTokens.Space.Row))

        // ---- 备注（日期下面一整行） ----
        BasicTextField(
            value = note,
            onValueChange = { input -> note = input.takeIf { it.length <= 30 } ?: note },
            singleLine = true,
            textStyle = TextStyle(fontSize = 14.sp, color = InkPrimary),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
            modifier = Modifier
                .fillMaxWidth()
                .liquidGlass(
                    shape = GlassTokens.Corner.Pill,
                    alpha = GlassTokens.Surface.Chip,
                    elevation = GlassTokens.Elevation.Flat,
                    highlight = false
                )
                .padding(horizontal = 14.dp, vertical = 12.dp),
            decorationBox = { inner ->
                Box(Modifier.fillMaxWidth()) {
                    if (note.isEmpty()) {
                        Text("添加备注", fontSize = 14.sp, color = InkSecondary.copy(alpha = 0.8f))
                    }
                    inner()
                }
            }
        )

        Spacer(Modifier.height(24.dp))
        GlassPrimaryButton(
            text = "保存",
            enabled = canSave,
            onClick = {
                if (accountId != 0L) vm.lastAccountId = accountId
                if (editing != null) {
                    vm.saveRecordEdit(editing.record, type, amount, categoryId, accountId, dateTime, note.trim())
                } else {
                    vm.addRecord(type, amount, categoryId, accountId, dateTime, note.trim())
                }
                onDismiss()
            }
        )
        Spacer(Modifier.height(GlassTokens.Space.Bottom))
    }

    if (showDatePicker) {
        DateTimeWheelDialog(
            initial = dateTime,
            onDismiss = { showDatePicker = false },
            onConfirm = { picked ->
                dateTime = picked
                showDatePicker = false
            }
        )
    }

    if (showAccountPicker) {
        AccountPickerSheet(
            accounts = accounts,
            selectedId = accountId,
            onDismiss = { showAccountPicker = false },
            onSelect = { picked ->
                accountId = picked
                vm.lastAccountId = picked
                showAccountPicker = false
            }
        )
    }
}

/** 金额 -> 输入框初值：整数不带小数点（219 而不是 219.0） */
private fun amountToText(v: Double): String =
    if (v % 1.0 == 0.0) v.toLong().toString() else v.toString()

/** 区块小标题（统一走 Base 层字号/字重） */
@Composable
private fun SectionHeader(text: String) {
    Text(
        text,
        fontSize = GlassTokens.Type.SectionHeader.sp,
        fontWeight = FontWeight.SemiBold,
        color = InkPrimary
    )
}

/**
 * 当前账户 -> 胶囊上要显示的账户（图标 + 名字）。
 * 「不计入账户」用哨兵 id 现造一个假账户塞进去，这样胶囊不用为它单独写一套显示逻辑；
 * id 传 0（还没加载出默认值）时返回 null，胶囊显示「选择账户」。
 */
private fun displayAccountOf(accountId: Long, accounts: List<AccountEntity>): AccountEntity? = when {
    accountId == NO_ACCOUNT_ID -> AccountEntity(
        id = NO_ACCOUNT_ID,
        name = "不计入账户",
        iconId = NO_ACCOUNT_ICON,
        colorArgb = IconCatalog.colorOf(NO_ACCOUNT_ICON),
        type = AccountType.FUND,
        balance = 0.0
    )
    else -> accounts.firstOrNull { it.id == accountId }
}

/** 账户胶囊：图标 + 名称 + 一个小箭头，点它弹「选择账户」抽屉 */
@Composable
private fun AccountPill(account: AccountEntity?, onClick: () -> Unit) {
    Row(
        Modifier
            .liquidGlass(
                shape = GlassTokens.Corner.Pill,
                alpha = GlassTokens.Surface.Card,
                elevation = GlassTokens.Elevation.Chip
            )
            .clickableNoRipple(onClick)
            .padding(start = 10.dp, end = 12.dp, top = 7.dp, bottom = 7.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (account == null) {
            // 账户还在加载：先占个位，避免出现一个空白胶囊
            Text("选择账户", fontSize = 15.sp, color = InkSecondary)
        } else {
            AccountIcon(account.iconId, size = 26.dp, name = account.name, colorArgb = account.colorArgb)
            Spacer(Modifier.width(8.dp))
            Text(
                account.name,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                color = InkPrimary
            )
        }
        Icon(
            Icons.Filled.ExpandMore,
            contentDescription = "选择账户",
            tint = InkSecondary,
            modifier = Modifier.size(18.dp)
        )
    }
}

@Composable
private fun TypeChip(label: String, selected: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Text(
        label,
        fontSize = GlassTokens.Type.Body.sp,
        fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
        color = if (selected) InkPrimary else InkSecondary,
        textAlign = TextAlign.Center,
        modifier = modifier
            .then(
                if (selected) Modifier.liquidGlass(
                    shape = GlassTokens.Corner.Pill,
                    alpha = GlassTokens.Surface.Bar,
                    elevation = GlassTokens.Elevation.Chip,
                    highlight = false
                ) else Modifier
            )
            .clickableNoRipple(onClick)
            .padding(vertical = 9.dp)
    )
}
