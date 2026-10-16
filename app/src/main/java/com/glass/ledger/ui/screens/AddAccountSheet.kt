package com.glass.ledger.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.glass.ledger.data.AccountType
import com.glass.ledger.data.AccountTypes
import com.glass.ledger.data.BankCatalog
import com.glass.ledger.data.BORROW_IN_ICON
import com.glass.ledger.data.IconCatalog
import com.glass.ledger.data.LEND_OUT_ICON
import com.glass.ledger.ui.MainViewModel
import com.glass.ledger.ui.base.GlassFieldDivider
import com.glass.ledger.ui.base.GlassPageDialog
import com.glass.ledger.ui.base.GlassPrimaryButton
import com.glass.ledger.ui.base.GlassTokens
import com.glass.ledger.ui.components.AccountIcon
import com.glass.ledger.ui.components.clickableNoRipple
import com.glass.ledger.ui.components.liquidGlass
import com.glass.ledger.ui.theme.InkPrimary
import com.glass.ledger.ui.theme.InkSecondary

/** 资金账户第一格的固定 id：还没选银行时它就是「储蓄卡」，选完变成具体银行 */
private const val DEBIT_KEY = "debitcard"

/** 信用账户第一格的固定 id：同上，未选银行时是「信用卡」 */
private const val CREDIT_KEY = "creditcard"

/** 信用卡账户名后缀：选中「工商银行」自动填「工商银行信用卡」 */
private const val CREDIT_SUFFIX = "信用卡"

/**
 * 新增账户第二步（全屏弹窗）：开户表单。
 *
 * 资金/信用/充值/借贷/投资的图标格是写死的，且资金和信用的第一格能点进「选择银行」全屏列表：
 * 资金 = 储蓄卡 / 微信 / 支付宝 / 现金 / 其他（按截图），
 * 信用 = 信用卡 / 花呗 / 白条 / 其他，
 * 充值 = 饭卡 / 交通卡 / 会员卡 / 其他（预付费卡就这三类，不再混进钱包、银行卡），
 * 借贷 = 借入 / 借出 / 其他（借入存负数，见 [com.glass.ledger.data.signedBalanceOf]），
 * 投资 = 股票 / 基金。
 * 选完银行后那一格的图标换成这家银行的 logo、底色取它的品牌色，
 * 账户名自动填银行全称 —— 信用账户还要在银行名后面补「信用卡」三个字（工商银行信用卡），
 * 因为光写「工商银行」分不清这是储蓄卡还是信用卡。
 * 资金账户字段只有 名称 / 余额 / 备注，其它类型随类型变化（信用卡多两栏额度），
 * 主按钮固定在底部（内容区自己滚动）。
 *
 * 两条老约定不变：底色一律取图标品牌色（[IconCatalog.colorOf]）；
 * 名称没被手动改过时，选图标就自动填图标名。
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AddAccountSheet(
    vm: MainViewModel,
    type: AccountType,
    onDismiss: () -> Unit
) {
    val slots = remember(type) { iconSlotsOf(type) }
    /** 会跳「选择银行」的那一格（资金=储蓄卡、信用=信用卡），图标池类型没有 */
    val bankSlot = slots.firstOrNull { it.pickBank }

    var selectedKey by remember { mutableStateOf(slots.first().key) }
    /** 选银行槽当前选中的银行图标（未选 = 槽位自己，即「储蓄卡」/「信用卡」） */
    var bankIconId by remember { mutableStateOf(bankSlot?.key ?: slots.first().key) }
    /** 名称是否被手动编辑过（清空输入框算"没编辑过"，选图标会重新自动填） */
    var nameEdited by remember { mutableStateOf(false) }
    var name by remember { mutableStateOf(IconCatalog.nameOf(slots.first().key).orEmpty()) }
    var balanceText by remember { mutableStateOf("") }
    var limitText by remember { mutableStateOf("") }
    var noteText by remember { mutableStateOf("") }
    var showBanks by remember { mutableStateOf(false) }

    val currentIconId = if (selectedKey == bankSlot?.key) bankIconId else selectedKey
    // 底色跟随图标品牌色，「其他」回落到默认蓝紫
    val color = IconCatalog.colorOf(currentIconId)

    val balance = balanceText.toDoubleOrNull() ?: 0.0
    val limit = limitText.toDoubleOrNull()
    val canSave = name.isNotBlank() && (type != AccountType.CREDIT || limit != null)

    // 选银行时用返回箭头回表单，其余情况关闭整个弹窗
    val dismissOrBack = { if (showBanks) showBanks = false else onDismiss() }

    GlassPageDialog(
        onDismiss = dismissOrBack,
        title = if (showBanks) "选择银行" else "添加${AccountTypes.label(type)}",
        onBack = if (showBanks) ({ showBanks = false }) else null,
        scroll = false
    ) {
        if (showBanks) {
            BankPickerList(
                selectedId = if (selectedKey == bankSlot?.key) bankIconId else null,
                onPick = { bank ->
                    bankIconId = bank.id
                    selectedKey = bankSlot?.key ?: bank.id
                    if (!nameEdited) name = accountNameOf(bank.id, type)
                    showBanks = false
                },
                modifier = Modifier.fillMaxWidth().weight(1f)
            )
        } else {
            Column(
                Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
            ) {
                Spacer(Modifier.height(6.dp))

                // ---- 图标（同一张玻璃卡：标题 + 五格） ----
                Column(
                    Modifier
                        .fillMaxWidth()
                        .liquidGlass(
                            shape = GlassTokens.Corner.Field,
                            alpha = GlassTokens.Surface.Chip,
                            elevation = GlassTokens.Elevation.Field
                        )
                        .padding(16.dp)
                ) {
                    Text(
                        "图标",
                        fontSize = GlassTokens.Type.SectionHeader.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = InkPrimary
                    )
                    if (bankSlot != null) {
                        Text(
                            if (type == AccountType.CREDIT)
                                "点信用卡可选具体银行，名称自动填「××银行信用卡」"
                            else "点储蓄卡可选具体银行，图标和名称会跟着换",
                            fontSize = GlassTokens.Type.Caption.sp,
                            color = InkSecondary,
                            modifier = Modifier.padding(top = 3.dp)
                        )
                    }
                    if (type == AccountType.DEBT) {
                        Text(
                            "借入 = 你欠别人的，记为负数并从总资产里扣掉；借出 = 别人欠你的，算进资产",
                            fontSize = GlassTokens.Type.Caption.sp,
                            color = InkSecondary,
                            modifier = Modifier.padding(top = 3.dp)
                        )
                    }
                    Spacer(Modifier.height(14.dp))
                    FlowRow(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        slots.forEach { slot ->
                            val iconId = if (slot.pickBank) bankIconId else slot.key
                            IconTile(
                                iconId = iconId,
                                label = if (slot.pickBank) IconCatalog.labelOf(bankIconId) else slot.label,
                                colorArgb = IconCatalog.colorOf(iconId),
                                selected = selectedKey == slot.key,
                                onClick = {
                                    selectedKey = slot.key
                                    if (!nameEdited) {
                                        name = accountNameOf(iconId, type)
                                    }
                                    if (slot.pickBank) showBanks = true
                                }
                            )
                        }
                    }
                }

                Spacer(Modifier.height(14.dp))

                // ---- 输入框集中一块（同一张玻璃卡，值右对齐） ----
                Column(
                    Modifier
                        .fillMaxWidth()
                        .liquidGlass(
                            shape = GlassTokens.Corner.Field,
                            alpha = GlassTokens.Surface.Chip,
                            elevation = GlassTokens.Elevation.Field
                        )
                        .padding(horizontal = 16.dp)
                ) {
                    GroupedField(
                        label = "名称",
                        value = name,
                        hint = nameHintOf(type),
                        onValueChange = {
                            name = it
                            // 手动改过就不再自动填；把输入框清空算回到"未编辑"状态
                            nameEdited = it.isNotBlank()
                        }
                    )

                    when (type) {
                        AccountType.CREDIT -> {
                            GlassFieldDivider()
                            GroupedField("已用额度", value = balanceText, hint = "0.00", numeric = true) { balanceText = it }
                            GlassFieldDivider()
                            GroupedField("总额度", value = limitText, hint = "0.00", numeric = true) { limitText = it }
                        }
                        AccountType.DEBT -> {
                            GlassFieldDivider()
                            // 三个格子对应三种说法，输入的都是正数：借入会被记成负数（见 signedBalanceOf）
                            GroupedField(
                                label = debtFieldLabelOf(currentIconId),
                                value = balanceText,
                                hint = "0.00",
                                numeric = true
                            ) { balanceText = it }
                        }
                        else -> {
                            GlassFieldDivider()
                            GroupedField("余额", value = balanceText, hint = "0.00", numeric = true) { balanceText = it }
                        }
                    }

                    GlassFieldDivider()
                    GroupedField("备注", value = noteText, hint = "请输入备注") { noteText = it }
                }

                Spacer(Modifier.height(GlassTokens.Space.Bottom))
            }

            Spacer(Modifier.height(16.dp))
            GlassPrimaryButton(
                text = "保存",
                enabled = canSave,
                onClick = {
                    vm.addAccount(
                        name = name.trim(),
                        iconId = currentIconId,
                        colorArgb = color,
                        type = type,
                        initialBalance = balance,
                        creditLimit = if (type == AccountType.CREDIT) limit else null,
                        note = noteText
                    )
                    onDismiss()
                }
            )
            Spacer(Modifier.height(GlassTokens.Space.Gap))
        }
    }
}

/** 图标格：立体图标 + 名称，选中时右上角一个品牌色对勾角标 */
@Composable
private fun IconTile(
    iconId: String,
    label: String,
    colorArgb: Int,
    selected: Boolean,
    onClick: () -> Unit
) {
    Column(
        Modifier
            .width(58.dp)
            .then(
                if (selected) {
                    Modifier.liquidGlass(
                        shape = GlassTokens.Corner.Small,
                        alpha = GlassTokens.Surface.Card,
                        elevation = GlassTokens.Elevation.Chip
                    )
                } else Modifier
            )
            .clickableNoRipple(onClick)
            .padding(vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box {
            AccountIcon(iconId, size = 44.dp, name = label, colorArgb = colorArgb)
            if (selected) {
                Box(
                    Modifier
                        .align(Alignment.TopEnd)
                        .offset(x = 3.dp, y = (-3).dp)
                        .size(17.dp)
                        .background(GlassTokens.BrandBrush, CircleShape)
                        .border(1.6.dp, Color.White.copy(alpha = 0.92f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Filled.Check,
                        contentDescription = "已选择",
                        tint = Color.White,
                        modifier = Modifier.size(11.dp)
                    )
                }
            }
        }
        Text(
            // 一格只有 58dp，六字以上的名字（广州农商银行…）去掉「银行」二字，带斜杠的通用图标
            // 取斜杠前那段；剩下的真放不下才换行，总之不出现省略号。
            when {
                label.contains('/') -> label.substringBefore('/')
                label.length > 5 && label.endsWith("银行") -> label.removeSuffix("银行")
                else -> label
            },
            fontSize = GlassTokens.Type.Caption.sp,
            maxLines = 2,
            textAlign = TextAlign.Center,
            overflow = TextOverflow.Ellipsis,
            color = if (selected) InkPrimary else InkSecondary,
            modifier = Modifier.padding(top = 5.dp)
        )
    }
}

/** 分组输入行：左侧字段名 + 右侧输入（值右对齐，和截图一致） */
@Composable
private fun GroupedField(
    label: String,
    value: String,
    hint: String,
    numeric: Boolean = false,
    onValueChange: (String) -> Unit
) {
    Row(
        Modifier.fillMaxWidth().padding(vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, fontSize = 15.sp, color = InkPrimary, modifier = Modifier.width(76.dp))
        BasicTextField(
            value = value,
            onValueChange = { input ->
                if (!numeric || input.matches(Regex("\\d{0,8}(\\.\\d{0,2})?"))) onValueChange(input)
            },
            singleLine = true,
            textStyle = TextStyle(
                fontSize = 15.sp,
                color = InkPrimary,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.End
            ),
            keyboardOptions = KeyboardOptions(keyboardType = if (numeric) KeyboardType.Decimal else KeyboardType.Text),
            modifier = Modifier.weight(1f),
            decorationBox = { inner ->
                Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterEnd) {
                    if (value.isEmpty()) {
                        Text(hint, fontSize = 15.sp, color = InkSecondary.copy(alpha = 0.75f))
                    }
                    inner()
                }
            }
        )
    }
}

/** 一个图标格位；pickBank = true 时点它进选银行列表（资金账户的储蓄卡、信用账户的信用卡） */
private data class IconSlot(val key: String, val label: String, val pickBank: Boolean = false)

/**
 * 图标格：五种账户类型全部写死（资金/信用的第一格可选银行），不再靠 [IconCatalog.forType] 过滤 ——
 * 那样会把不相干的图标混进池子。末尾的 else 只是将来新增类型时的兜底。
 */
private fun iconSlotsOf(type: AccountType): List<IconSlot> = when (type) {
    AccountType.FUND -> listOf(
        IconSlot(DEBIT_KEY, "储蓄卡", pickBank = true),
        IconSlot("wechat", "微信"),
        IconSlot("alipay", "支付宝"),
        IconSlot("cash", "现金"),
        IconSlot(IconCatalog.OTHER_ID, "其他")
    )
    AccountType.CREDIT -> listOf(
        IconSlot(CREDIT_KEY, "信用卡", pickBank = true),
        IconSlot("huabei", "花呗"),
        IconSlot("baitiao", "白条"),
        IconSlot(IconCatalog.OTHER_ID, "其他")
    )
    // 充值账户就四格，不给它走 forType 过滤：那样会把「钱包」「银行卡」也混进来，
    // 而充值账户就是饭卡/交通卡/会员卡这类预付费卡。
    AccountType.RECHARGE -> listOf(
        IconSlot("meal", "饭卡"),
        IconSlot("buscard", "交通卡"),
        IconSlot("member", "会员卡"),
        IconSlot(IconCatalog.OTHER_ID, "其他")
    )
    // 借贷只有三个格子：借入（欠别人的，记负数）、借出（别人欠我的，记正数）和「其他」。
    // 「其他」按欠债处理，因为借贷列表里剩下的一律是我还不出来的那部分。
    AccountType.DEBT -> listOf(
        IconSlot(BORROW_IN_ICON, "借入"),
        IconSlot(LEND_OUT_ICON, "借出"),
        IconSlot(IconCatalog.OTHER_ID, "其他")
    )
    // 投资就股票、基金两格，不给它加「其他」：想换名字直接改名称输入框就行。
    AccountType.INVEST -> listOf(
        IconSlot("stock", "股票"),
        IconSlot("fund", "基金")
    )
    else -> IconCatalog.forType(type).map { IconSlot(it.id, it.label) } +
        IconSlot(IconCatalog.OTHER_ID, "其他")
}

/**
 * 选中图标时自动填的账户名。
 * 银行图标（[BankCatalog] 里的 id）在信用账户要补后缀：工商银行 → 工商银行信用卡，
 * 只写「工商银行」在资产列表里分不清是储蓄卡还是信用卡；资金账户保持银行全称。
 * 非银行图标（花呗、微信…）直接用图标名。
 */
private fun accountNameOf(iconId: String, type: AccountType): String {
    val base = IconCatalog.nameOf(iconId) ?: return ""
    return if (type == AccountType.CREDIT && BankCatalog.isBank(iconId) && !base.endsWith(CREDIT_SUFFIX))
        base + CREDIT_SUFFIX
    else base
}

/**
 * 借贷账户金额那栏的字段名：跟着图标走，让「输入正数」这件事一眼看得懂。
 * 「其他」和借入同类（都是要还的），沿用「欠债金额」。
 */
private fun debtFieldLabelOf(iconId: String) = when (iconId) {
    LEND_OUT_ICON -> "借出金额"
    BORROW_IN_ICON -> "借入金额"
    else -> "欠债金额"
}

private fun nameHintOf(type: AccountType) = when (type) {
    AccountType.FUND -> "如：招商银行储蓄卡"
    AccountType.CREDIT -> "如：工商银行信用卡"
    AccountType.RECHARGE -> "如：公交卡 / 公司饭卡"
    AccountType.INVEST -> "如：指数基金 / 股票账户"
    AccountType.DEBT -> "如：借小王 / 房贷"
}
