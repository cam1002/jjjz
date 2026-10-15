package com.glass.ledger.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.glass.ledger.data.RecordType
import com.glass.ledger.ui.MainViewModel
import com.glass.ledger.ui.components.AccountIcon
import com.glass.ledger.ui.components.CategoryIcon
import com.glass.ledger.ui.components.DialogEdgeToEdge
import com.glass.ledger.ui.components.GlassBackdrop
import com.glass.ledger.ui.components.GlassCloseButton
import com.glass.ledger.ui.components.GlassCornerMedium
import com.glass.ledger.ui.components.GlassCornerPill
import com.glass.ledger.ui.components.clickableNoRipple
import com.glass.ledger.ui.components.liquidGlass
import com.glass.ledger.ui.theme.InkPrimary
import com.glass.ledger.ui.theme.InkSecondary

/**
 * 记一笔（全屏页）：支出/收入切换、金额、分类、账户（支出账户可选）、日期、备注。
 * presetAccountId 非 0 时预选该账户（从账户详情页进入）。
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AddRecordSheet(vm: MainViewModel, presetAccountId: Long = 0L, onDismiss: () -> Unit) {
    val accounts = vm.accounts.collectAsStateWithLifecycle().value
    val expenseCats = vm.expenseCategories.collectAsStateWithLifecycle().value
    val incomeCats = vm.incomeCategories.collectAsStateWithLifecycle().value

    var type by remember { mutableStateOf(RecordType.EXPENSE) }
    var amountText by remember { mutableStateOf("") }
    var categoryId by remember { mutableStateOf(0L) }
    var accountId by remember { mutableStateOf(presetAccountId) }
    var dateTime by remember { mutableStateOf(java.time.LocalDateTime.now()) }
    var note by remember { mutableStateOf("") }
    var showPicker by remember { mutableStateOf(false) }

    val cats = if (type == RecordType.EXPENSE) expenseCats else incomeCats
    val amount = amountText.toDoubleOrNull() ?: 0.0
    val canSave = amount > 0 && categoryId != 0L && accountId != 0L

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
                // ---- 顶栏：关闭 / 标题 ----
                Row(
                    Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    GlassCloseButton(onDismiss)
                    Spacer(Modifier.size(14.dp))
                    Text("记一笔", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = InkPrimary)
                }

                Column(
                    Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 20.dp)
                ) {
                    // 支出 / 收入
                    Row(
                        Modifier.fillMaxWidth().liquidGlass(GlassCornerPill, alpha = 0.5f, elevation = 2.dp).padding(4.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        TypeChip("支出", type == RecordType.EXPENSE, Modifier.weight(1f)) {
                            type = RecordType.EXPENSE; categoryId = 0L
                        }
                        TypeChip("收入", type == RecordType.INCOME, Modifier.weight(1f)) {
                            type = RecordType.INCOME; categoryId = 0L
                        }
                    }

                    Spacer(Modifier.height(16.dp))

                    // 金额
                    Row(
                        Modifier.fillMaxWidth().liquidGlass(GlassCornerMedium, alpha = 0.5f, elevation = 3.dp)
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
                            Text("清除", fontSize = 13.sp, color = InkSecondary, modifier = Modifier.clickableNoRipple { amountText = "" })
                        }
                    }

                    Spacer(Modifier.height(16.dp))
                    Text("分类", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = InkPrimary)
                    Spacer(Modifier.height(8.dp))
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
                                        if (sel) Modifier.liquidGlass(RoundedCornerShape(16.dp), alpha = 0.6f, elevation = 2.dp)
                                        else Modifier.padding(4.dp)
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

                    Spacer(Modifier.height(16.dp))
                    Text(if (type == RecordType.EXPENSE) "支出账户" else "收入账户", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = InkPrimary)
                    Spacer(Modifier.height(8.dp))
                    FlowRow(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        accounts.forEach { a ->
                            val sel = a.id == accountId
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .then(
                                        if (sel) Modifier.liquidGlass(GlassCornerPill, alpha = 0.6f, elevation = 2.dp)
                                        else Modifier.liquidGlass(GlassCornerPill, alpha = 0.28f, elevation = 0.dp, highlight = false)
                                    )
                                    .clickableNoRipple { accountId = a.id }
                                    .padding(horizontal = 12.dp, vertical = 7.dp)
                            ) {
                                AccountIcon(a.iconId, size = 18.dp, name = a.name, colorArgb = a.colorArgb)
                                Spacer(Modifier.width(6.dp))
                                Text(a.name, fontSize = 13.sp, color = if (sel) InkPrimary else InkSecondary)
                            }
                        }
                    }

                    Spacer(Modifier.height(16.dp))
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        // 日期
                        Row(
                            Modifier.liquidGlass(GlassCornerPill, alpha = 0.45f, elevation = 2.dp)
                                .clickableNoRipple { showPicker = true }
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
                        // 备注
                        BasicTextField(
                            value = note,
                            onValueChange = { note = it.takeIf { s -> s.length <= 30 } ?: note },
                            singleLine = true,
                            textStyle = TextStyle(fontSize = 13.sp, color = InkPrimary),
                            modifier = Modifier.weight(1f, fill = false)
                                .liquidGlass(GlassCornerPill, alpha = 0.35f, elevation = 0.dp, highlight = false)
                                .padding(horizontal = 14.dp, vertical = 11.dp)
                        )
                    }

                    Spacer(Modifier.height(24.dp))
                    Text(
                        "保存",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (canSave) Color.White else InkSecondary,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                            .then(
                                if (canSave) {
                                    Modifier
                                        .shadow(8.dp, GlassCornerPill)
                                        .background(
                                            Brush.linearGradient(
                                                listOf(Color(0xFF6C7CFF), Color(0xFF5B6EF5))
                                            ),
                                            GlassCornerPill
                                        )
                                } else {
                                    Modifier.liquidGlass(GlassCornerPill, alpha = 0.4f, elevation = 2.dp)
                                }
                            )
                            .clickableNoRipple {
                                if (canSave) {
                                    vm.addRecord(type, amount, categoryId, accountId, dateTime, note.trim())
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

    if (showPicker) {
        DateTimeWheelDialog(
            initial = dateTime,
            onDismiss = { showPicker = false },
            onConfirm = { picked ->
                dateTime = picked
                showPicker = false
            }
        )
    }
}

@Composable
private fun TypeChip(label: String, selected: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Text(
        label,
        fontSize = 15.sp,
        fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
        color = if (selected) InkPrimary else InkSecondary,
        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
        modifier = modifier
            .then(
                if (selected) Modifier.liquidGlass(GlassCornerPill, alpha = 0.7f, elevation = 2.dp, highlight = false)
                else Modifier
            )
            .clickableNoRipple(onClick)
            .padding(vertical = 9.dp)
    )
}
