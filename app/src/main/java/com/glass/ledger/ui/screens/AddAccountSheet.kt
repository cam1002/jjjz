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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
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
import com.glass.ledger.data.AccountType
import com.glass.ledger.data.IconCatalog
import com.glass.ledger.ui.MainViewModel
import com.glass.ledger.ui.components.AccountIcon
import com.glass.ledger.ui.components.GlassCornerPill
import com.glass.ledger.ui.components.clickableNoRipple
import com.glass.ledger.ui.components.liquidGlass
import com.glass.ledger.ui.theme.InkPrimary
import com.glass.ledger.ui.theme.InkSecondary

/**
 * 新增资产账户：名称、类型（资金/信用）、内置图标选择（微信/支付宝/各大银行等）、
 * 自定义底色、初始余额、信用额度。
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AddAccountSheet(vm: MainViewModel, onDismiss: () -> Unit) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var name by remember { mutableStateOf("") }
    var type by remember { mutableStateOf(AccountType.FUND) }
    var iconId by remember { mutableStateOf("card") }
    var color by remember { mutableStateOf(IconCatalog.palette.first()) }
    var balanceText by remember { mutableStateOf("") }
    var limitText by remember { mutableStateOf("") }

    val balance = balanceText.toDoubleOrNull() ?: 0.0
    val limit = limitText.toDoubleOrNull()
    val canSave = name.isNotBlank() && (type == AccountType.FUND || limit != null)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color(0xE6F4F6FF),
        dragHandle = null
    ) {
        Column(Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 18.dp)) {
            Text("新增账户", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = InkPrimary)
            Spacer(Modifier.height(14.dp))

            // 名称
            LabeledField(value = name, hint = "账户名称（如：招商银行）") { name = it }

            Spacer(Modifier.height(12.dp))

            // 类型
            Row(
                Modifier.fillMaxWidth().liquidGlass(GlassCornerPill, alpha = 0.5f, elevation = 2.dp).padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                TypeToggle("资金账户（储蓄）", type == AccountType.FUND, Modifier.weight(1f)) { type = AccountType.FUND }
                TypeToggle("信用账户（透支）", type == AccountType.CREDIT, Modifier.weight(1f)) { type = AccountType.CREDIT }
            }

            Spacer(Modifier.height(14.dp))

            // 图标选择
            Text("选择图标", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = InkPrimary)
            Spacer(Modifier.height(8.dp))
            FlowRow(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                IconCatalog.all.forEach { icon ->
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
            }

            Spacer(Modifier.height(14.dp))

            // 自定义底色
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

            Spacer(Modifier.height(14.dp))

            // 余额 / 额度
            if (type == AccountType.FUND) {
                LabeledField(value = balanceText, hint = "当前余额（元）", numeric = true) { balanceText = it }
            } else {
                LabeledField(value = balanceText, hint = "已用额度（元，欠款）", numeric = true) { balanceText = it }
                Spacer(Modifier.height(10.dp))
                LabeledField(value = limitText, hint = "总额度（元）", numeric = true) { limitText = it }
            }

            Spacer(Modifier.height(20.dp))
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
            Spacer(Modifier.height(10.dp))
        }
    }
}

@Composable
private fun LabeledField(
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

@Composable
private fun TypeToggle(label: String, selected: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Text(
        label,
        fontSize = 14.sp,
        fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
        color = if (selected) InkPrimary else InkSecondary,
        textAlign = TextAlign.Center,
        modifier = modifier
            .then(
                if (selected) Modifier.liquidGlass(GlassCornerPill, alpha = 0.7f, elevation = 2.dp, highlight = false)
                else Modifier
            )
            .clickableNoRipple(onClick)
            .padding(vertical = 9.dp)
    )
}
