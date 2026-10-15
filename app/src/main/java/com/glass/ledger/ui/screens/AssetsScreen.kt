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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.glass.ledger.data.AccountEntity
import com.glass.ledger.data.AccountType
import com.glass.ledger.ui.MainViewModel
import com.glass.ledger.ui.components.AccountIcon
import com.glass.ledger.ui.components.GlassCard
import com.glass.ledger.ui.components.GlassCornerMedium
import com.glass.ledger.ui.components.clickableNoRipple
import com.glass.ledger.ui.components.liquidGlass
import com.glass.ledger.ui.money
import com.glass.ledger.ui.theme.ExpenseRed
import com.glass.ledger.ui.theme.IncomeGreen
import com.glass.ledger.ui.theme.InkPrimary
import com.glass.ledger.ui.theme.InkSecondary

/**
 * 资产页：净资产总览 + 资金账户 + 信用账户。
 * 总资产 = 所有资金账户余额之和；信用账户余额为负数表示欠款。
 */
@Composable
fun AssetsScreen(
    vm: MainViewModel,
    contentPadding: PaddingValues,
    onAddAccount: () -> Unit
) {
    val accounts = vm.accounts.collectAsStateWithLifecycle().value
    val fundTotal = vm.fundTotal.collectAsStateWithLifecycle().value
    val creditTotal = vm.creditTotal.collectAsStateWithLifecycle().value

    val fundAccounts = accounts.filter { it.type == AccountType.FUND }
    val creditAccounts = accounts.filter { it.type == AccountType.CREDIT }

    var deleting by remember { mutableStateOf<AccountEntity?>(null) }

    Column(
        Modifier
            .fillMaxSize()
            .padding(contentPadding)
            .padding(horizontal = 20.dp)
    ) {
        Text(
            "资产",
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            color = InkPrimary,
            modifier = Modifier.fillMaxWidth().padding(top = 14.dp, bottom = 10.dp)
        )

        LazyColumn(contentPadding = PaddingValues(bottom = 150.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            item {
                // 净资产总览
                GlassCard {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text("净资产", style = MaterialTheme.typography.bodyMedium, color = InkSecondary)
                            Text(
                                (fundTotal + creditTotal).money(),
                                fontSize = 34.sp,
                                fontWeight = FontWeight.Bold,
                                color = InkPrimary,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                            Spacer(Modifier.height(10.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(18.dp)) {
                                Text(
                                    "资产总额  ${fundTotal.money()}",
                                    fontSize = 13.sp,
                                    color = InkSecondary
                                )
                                Text(
                                    "负债总额  ${(-creditTotal).money()}",
                                    fontSize = 13.sp,
                                    color = InkSecondary
                                )
                            }
                        }
                        // 添加账户按钮
                        Box_GlassPlus(onClick = onAddAccount)
                    }
                }
            }

            item {
                SectionRow(title = "资金账户", total = fundTotal)
            }
            items(fundAccounts, key = { "f${it.id}" }) { acc ->
                AccountRow(acc, onLongPress = { deleting = acc })
            }

            if (creditAccounts.isNotEmpty()) {
                item {
                    SectionRow(title = "信用账户", total = -creditTotal)
                }
                items(creditAccounts, key = { "c${it.id}" }) { acc ->
                    AccountRow(acc, onLongPress = { deleting = acc })
                }
            }

            item {
                Text(
                    "提示：长按账户可删除；数据全部保存在本机。",
                    fontSize = 12.sp,
                    color = InkSecondary.copy(alpha = 0.8f),
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
    }

    deleting?.let { acc ->
        AlertDialog(
            onDismissRequest = { deleting = null },
            title = { Text("删除「${acc.name}」？") },
            text = { Text("该账户下的所有流水记录将一并删除。") },
            confirmButton = {
                TextButton(onClick = { vm.deleteAccount(acc); deleting = null }) { Text("删除", color = ExpenseRed) }
            },
            dismissButton = { TextButton(onClick = { deleting = null }) { Text("取消") } }
        )
    }
}

@Composable
private fun Box_GlassPlus(onClick: () -> Unit) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(52.dp)
            .shadow(6.dp, RoundedCornerShape(16.dp))
            .background(
                androidx.compose.ui.graphics.Brush.linearGradient(
                    listOf(Color(0xFF6C7CFF), Color(0xFF5B6EF5))
                ),
                RoundedCornerShape(16.dp)
            )
            .border(1.dp, Color.White.copy(alpha = 0.6f), RoundedCornerShape(16.dp))
            .clickableNoRipple(onClick)
    ) {
        Icon(Icons.Filled.Add, contentDescription = "添加账户", tint = Color.White)
    }
}

@Composable
private fun SectionRow(title: String, total: Double) {
    Row(
        Modifier.fillMaxWidth().padding(top = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            title,
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold,
            color = InkPrimary,
            modifier = Modifier.weight(1f)
        )
        Text(total.money(), fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = InkPrimary)
    }
}

@Composable
private fun AccountRow(acc: AccountEntity, onLongPress: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .liquidGlass(GlassCornerMedium, alpha = 0.55f, elevation = 5.dp)
            .clickableNoRipple(onLongPress)
            .padding(horizontal = 14.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AccountIcon(acc.iconId, size = 40.dp)
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(acc.name, fontSize = 15.sp, fontWeight = FontWeight.Medium, color = InkPrimary)
            if (acc.type == AccountType.CREDIT && acc.creditLimit != null) {
                Text(
                    "可用: ${(acc.creditLimit + acc.balance).money()}",
                    fontSize = 11.sp,
                    color = InkSecondary,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
        }
        Text(
            acc.balance.money(),
            fontSize = 17.sp,
            fontWeight = FontWeight.SemiBold,
            color = if (acc.balance < 0) ExpenseRed else IncomeGreen
        )
    }
}
