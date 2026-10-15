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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.glass.ledger.data.AccountType
import com.glass.ledger.data.AccountTypes
import com.glass.ledger.data.IconCatalog
import com.glass.ledger.ui.MainViewModel
import com.glass.ledger.ui.components.AccountIcon
import com.glass.ledger.ui.components.DialogEdgeToEdge
import com.glass.ledger.ui.components.GlassBackdrop
import com.glass.ledger.ui.components.GlassCloseButton
import com.glass.ledger.ui.components.GlassCornerPill
import com.glass.ledger.ui.components.clickableNoRipple
import com.glass.ledger.ui.components.liquidGlass
import com.glass.ledger.ui.theme.InkPrimary
import com.glass.ledger.ui.theme.InkSecondary

/**
 * 新增账户第二步（全屏页）：开户表单（类型已在上一步选定）。
 * 布局：选择图标（池按类型过滤，末位固定「其他」，图标=底色+账户名首字）
 * -> 自定义底色 -> 输入框集中一块（名称 + 金额字段，同一张玻璃卡）。
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AddAccountSheet(
    vm: MainViewModel,
    type: AccountType,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var iconId by remember { mutableStateOf(defaultIconOf(type)) }
    var color by remember { mutableStateOf(IconCatalog.palette.first()) }
    var balanceText by remember { mutableStateOf("") }
    var limitText by remember { mutableStateOf("") }

    val balance = balanceText.toDoubleOrNull() ?: 0.0
    val limit = limitText.toDoubleOrNull()
    val canSave = name.isNotBlank() && (type != AccountType.CREDIT || limit != null)

    // 图标池按账户类型区分（「其他」单独追加在末位，不在目录里）
    val availableIcons = remember(type) { IconCatalog.forType(type) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        )
    ) {
        DialogEdgeToEdge(dimAmount = 0f)
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
                    .navigationBarsPadding()
                    .imePadding()
            ) {
                // ---- 顶栏：关闭 / 标题+说明 / 类型胶囊 ----
                Row(
                    Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    GlassCloseButton(onDismiss)
                    Spacer(Modifier.size(14.dp))
                    Column(Modifier.weight(1f)) {
                        Text("新增账户", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = InkPrimary)
                        Text(
                            AccountTypes.desc(type),
                            fontSize = 12.sp,
                            color = InkSecondary,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                    Text(
                        AccountTypes.label(type),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF3D4CC8),
                        modifier = Modifier
                            .liquidGlass(GlassCornerPill, alpha = 0.55f, elevation = 2.dp)
                            .padding(horizontal = 12.dp, vertical = 5.dp)
                    )
                }

                Column(
                    Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 20.dp)
                ) {
                    Spacer(Modifier.height(6.dp))

                    // ---- 图标选择（末位固定「其他」） ----
                    Text(
                        "选择图标（${AccountTypes.label(type)}可用）",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = InkPrimary
                    )
                    Spacer(Modifier.height(8.dp))
                    FlowRow(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        availableIcons.forEach { icon ->
                            val sel = icon.id == iconId
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier
                                    .then(
                                        if (sel) Modifier.liquidGlass(RoundedCornerShape(14.dp), alpha = 0.6f, elevation = 2.dp)
                                        else Modifier
                                    )
                                    .clickableNoRipple { iconId = icon.id }
                                    .padding(6.dp)
                            ) {
                                AccountIcon(icon.id, size = 36.dp)
                                Text(
                                    icon.label,
                                    fontSize = 10.sp,
                                    maxLines = 1,
                                    color = if (sel) InkPrimary else InkSecondary,
                                    modifier = Modifier.padding(top = 3.dp)
                                )
                            }
                        }
                        // 固定末位：其他（底色块 + 账户名首字）
                        val selOther = iconId == IconCatalog.OTHER_ID
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .then(
                                    if (selOther) Modifier.liquidGlass(RoundedCornerShape(14.dp), alpha = 0.6f, elevation = 2.dp)
                                    else Modifier
                                )
                                .clickableNoRipple { iconId = IconCatalog.OTHER_ID }
                                .padding(6.dp)
                        ) {
                            AccountIcon(
                                IconCatalog.OTHER_ID,
                                size = 36.dp,
                                name = name,
                                colorArgb = color
                            )
                            Text(
                                "其他",
                                fontSize = 10.sp,
                                maxLines = 1,
                                color = if (selOther) InkPrimary else InkSecondary,
                                modifier = Modifier.padding(top = 3.dp)
                            )
                        }
                    }

                    Spacer(Modifier.height(18.dp))

                    // ---- 自定义底色 ----
                    Text("自定义底色", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = InkPrimary)
                    Spacer(Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        IconCatalog.palette.forEach { c ->
                            val sel = c == color
                            Box(
                                Modifier
                                    .size(if (sel) 30.dp else 26.dp)
                                    .clip(CircleShape)
                                    .background(Color(c))
                                    .border(if (sel) 2.dp else 0.dp, Color.White.copy(alpha = 0.9f), CircleShape)
                                    .clickableNoRipple { color = c }
                            )
                        }
                    }

                    Spacer(Modifier.height(18.dp))

                    // ---- 输入框集中一块（图标下方，同一张玻璃卡） ----
                    Column(
                        Modifier
                            .fillMaxWidth()
                            .liquidGlass(RoundedCornerShape(22.dp), alpha = 0.5f, elevation = 4.dp)
                            .padding(horizontal = 16.dp)
                    ) {
                        GroupedField("账户名称", value = name, hint = nameHintOf(type)) { name = it }

                        when (type) {
                            AccountType.CREDIT -> {
                                FieldDivider()
                                GroupedField("已用额度", value = balanceText, hint = "欠款金额（元）", numeric = true) { balanceText = it }
                                FieldDivider()
                                GroupedField("总额度", value = limitText, hint = "总额度（元）", numeric = true) { limitText = it }
                            }
                            AccountType.DEBT -> {
                                FieldDivider()
                                GroupedField("欠债金额", value = balanceText, hint = "欠债金额（元）", numeric = true) { balanceText = it }
                            }
                            else -> {
                                FieldDivider()
                                GroupedField("余额", value = balanceText, hint = balanceHintOf(type), numeric = true) { balanceText = it }
                            }
                        }
                    }

                    Spacer(Modifier.height(24.dp))
                    Text(
                        "保存账户",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (canSave) Color.White else InkSecondary,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                            .then(
                                if (canSave) {
                                    Modifier
                                        .shadow(8.dp, GlassCornerPill)
                                        .background(Brush.linearGradient(listOf(Color(0xFF6C7CFF), Color(0xFF5B6EF5))), GlassCornerPill)
                                } else {
                                    Modifier.liquidGlass(GlassCornerPill, alpha = 0.4f, elevation = 2.dp)
                                }
                            )
                            .clickableNoRipple {
                                if (canSave) {
                                    vm.addAccount(
                                        name = name.trim(),
                                        iconId = iconId,
                                        colorArgb = color,
                                        type = type,
                                        initialBalance = balance,
                                        creditLimit = if (type == AccountType.CREDIT) limit else null
                                    )
                                    onDismiss()
                                }
                            }
                            .padding(vertical = 14.dp)
                    )
                    Spacer(Modifier.height(30.dp))
                }
            }
        }
    }
}

/** 分组输入卡内的分隔线 */
@Composable
private fun FieldDivider() {
    Box(
        Modifier
            .fillMaxWidth()
            .height(0.8.dp)
            .background(Color.White.copy(alpha = 0.75f))
    )
}

/** 分组输入行：左侧字段名 + 右侧输入 */
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
        Text(label, fontSize = 14.sp, color = InkSecondary, modifier = Modifier.width(76.dp))
        BasicTextField(
            value = value,
            onValueChange = { input ->
                if (!numeric || input.matches(Regex("\\d{0,8}(\\.\\d{0,2})?"))) onValueChange(input)
            },
            singleLine = true,
            textStyle = TextStyle(fontSize = 15.sp, color = InkPrimary, fontWeight = FontWeight.Medium),
            keyboardOptions = KeyboardOptions(keyboardType = if (numeric) KeyboardType.Decimal else KeyboardType.Text),
            modifier = Modifier.weight(1f),
            decorationBox = { inner ->
                Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterStart) {
                    if (value.isEmpty()) {
                        Text(hint, fontSize = 14.sp, color = InkSecondary.copy(alpha = 0.6f))
                    }
                    inner()
                }
            }
        )
    }
}

private fun defaultIconOf(type: AccountType) = when (type) {
    AccountType.FUND -> "card"
    AccountType.CREDIT -> "baitiao"
    AccountType.RECHARGE -> "buscard"
    AccountType.INVEST -> "fund"
    AccountType.DEBT -> "debt"
}

private fun nameHintOf(type: AccountType) = when (type) {
    AccountType.FUND -> "如：招商银行储蓄卡"
    AccountType.CREDIT -> "如：招行信用卡"
    AccountType.RECHARGE -> "如：公交卡 / 公司饭卡"
    AccountType.INVEST -> "如：指数基金 / 股票账户"
    AccountType.DEBT -> "如：借小王 / 房贷"
}

private fun balanceHintOf(type: AccountType) = when (type) {
    AccountType.FUND -> "当前余额（元）"
    AccountType.RECHARGE -> "卡内余额（元）"
    AccountType.INVEST -> "当前市值（元）"
    else -> "当前余额（元）"
}
