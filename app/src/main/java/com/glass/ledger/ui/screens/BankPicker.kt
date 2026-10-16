package com.glass.ledger.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.glass.ledger.data.Bank
import com.glass.ledger.data.BankCatalog
import com.glass.ledger.ui.base.GlassTokens
import com.glass.ledger.ui.components.AccountIcon
import com.glass.ledger.ui.components.clickableNoRipple
import com.glass.ledger.ui.components.liquidGlass
import com.glass.ledger.ui.theme.InkPrimary
import com.glass.ledger.ui.theme.InkSecondary
import kotlinx.coroutines.launch

/**
 * 选银行列表（开户表单点「储蓄卡」或「信用卡」那一格进入，两类共用这一份 [BankCatalog]）。
 *
 * 分组规则：[BankCatalog.common] 里的银行进「常用」段，其余按拼音首字母分段；
 * 右侧索引条点一下滚到对应段。列表画在一整块玻璃面板里，索引条浮在面板右外侧，
 * 和截图的层级一致。顶栏/背景由外层的全屏弹窗提供，这里只管列表本身。
 */
@Composable
fun BankPickerList(
    selectedId: String?,
    onPick: (Bank) -> Unit,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    // 扁平行：段标题 + 银行行，索引条靠它算出每个字母对应的下标
    val rows = remember {
        buildList {
            add(PickerRow.Header("常用"))
            BankCatalog.common.forEach { add(PickerRow.BankRow(it)) }
            BankCatalog.sections.forEach { (letter, banks) ->
                add(PickerRow.Header(letter))
                banks.forEach { add(PickerRow.BankRow(it)) }
            }
        }
    }
    val letterToIndex = remember(rows) {
        rows.mapIndexedNotNull { i, row -> (row as? PickerRow.Header)?.let { it.title to i } }
    }.toMap()

    Box(modifier) {
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .liquidGlass(
                    shape = GlassTokens.Corner.Card,
                    alpha = GlassTokens.Surface.Chip,
                    elevation = GlassTokens.Elevation.Field
                )
                .padding(horizontal = 8.dp, vertical = 6.dp),
            contentPadding = PaddingValues(bottom = GlassTokens.Space.Bottom),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            items(rows) { row ->
                when (row) {
                    is PickerRow.Header -> Text(
                        row.title,
                        fontSize = GlassTokens.Type.Caption.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = InkSecondary,
                        modifier = Modifier.padding(start = 12.dp, top = 12.dp, bottom = 4.dp)
                    )
                    is PickerRow.BankRow -> BankRowItem(
                        bank = row.bank,
                        selected = row.bank.id == selectedId,
                        onClick = { onPick(row.bank) }
                    )
                }
            }
        }

        // 右侧 A–Z 索引
        Column(
            Modifier
                .align(Alignment.CenterEnd)
                .width(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            BankCatalog.indexes.forEach { letter ->
                Text(
                    letter,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium,
                    color = InkSecondary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .clip(CircleShape)
                        .clickableNoRipple {
                            letterToIndex[letter]?.let { index ->
                                scope.launch { listState.animateScrollToItem(index) }
                            }
                        }
                        .padding(vertical = 2.4.dp)
                )
            }
        }
    }
}

private sealed interface PickerRow {
    data class Header(val title: String) : PickerRow
    data class BankRow(val bank: Bank) : PickerRow
}

@Composable
private fun BankRowItem(bank: Bank, selected: Boolean, onClick: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .then(
                if (selected) Modifier.liquidGlass(
                    shape = RoundedCornerShape(16.dp),
                    alpha = GlassTokens.Surface.Selected,
                    elevation = GlassTokens.Elevation.Chip,
                    highlight = false
                ) else Modifier
            )
            .clickableNoRipple(onClick)
            .padding(horizontal = 10.dp, vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AccountIcon(bank.id, size = 38.dp, name = bank.name, colorArgb = bank.colorArgb)
        Spacer(Modifier.width(12.dp))
        Text(
            bank.name,
            fontSize = GlassTokens.Type.Body.sp,
            fontWeight = FontWeight.Medium,
            color = if (selected) GlassTokens.AccentText else InkPrimary,
            modifier = Modifier.weight(1f)
        )
        if (selected) {
            Icon(
                Icons.Filled.Check,
                contentDescription = "已选择",
                tint = GlassTokens.AccentText,
                modifier = Modifier.size(18.dp)
            )
        }
        Spacer(Modifier.width(20.dp)) // 给右侧索引条让位
    }
}
