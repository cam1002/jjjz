package com.glass.ledger.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

/**
 * 数字滚轮（iOS 风格）：上下滑动选择，松手自动吸附到整格，中间高亮。
 * 用于年月日时分日期选择器。
 */
@Composable
fun NumberWheel(
    values: List<Int>,
    initialIndex: Int,
    modifier: Modifier = Modifier,
    showTwoDigits: Boolean = false,
    onSelected: (Int) -> Unit
) {
    if (values.isEmpty()) return
    val itemHeight = 40.dp
    val itemHeightPx = with(LocalDensity.current) { itemHeight.toPx() }
    val listState = rememberLazyListState(
        initialFirstVisibleItemIndex = initialIndex.coerceIn(0, values.lastIndex)
    )

    /** 当前居中的项索引（含滑动中的四舍五入） */
    fun centeredIndex(): Int =
        (listState.firstVisibleItemIndex +
            (listState.firstVisibleItemScrollOffset / itemHeightPx).roundToInt())
            .coerceIn(0, values.lastIndex)

    val selectedIndex by remember { derivedStateOf { centeredIndex() } }
    val scope = rememberCoroutineScope()

    // 松手后吸附到整格并回调
    LaunchedEffect(listState, values.size) {
        snapshotFlow {
            if (listState.isScrollInProgress) null else centeredIndex()
        }
            .filterNotNull()
            .distinctUntilChanged()
            .collect { idx ->
                listState.animateScrollToItem(idx)
                onSelected(idx)
            }
    }

    // 列表长度变化（如换月后天数变少）时把滚轮夹回合法位置
    LaunchedEffect(values.size) {
        val target = listState.firstVisibleItemIndex.coerceAtMost(values.lastIndex)
        listState.scrollToItem(target)
    }

    Box(modifier.height(itemHeight * 5)) {
        // 中间高亮条
        Box(
            Modifier
                .fillMaxWidth()
                .height(itemHeight)
                .align(Alignment.Center)
                .clip(RoundedCornerShape(12.dp))
                .background(Color.White.copy(alpha = 0.82f))
        )
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(vertical = itemHeight * 2),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            itemsIndexed(values) { i, v ->
                val sel = i == selectedIndex
                Box(
                    Modifier
                        .height(itemHeight)
                        .fillMaxWidth()
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {
                            scope.launch { listState.animateScrollToItem(i) }
                            onSelected(i)
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        if (showTwoDigits) "%02d".format(v) else v.toString(),
                        fontSize = 18.sp,
                        fontWeight = if (sel) FontWeight.Bold else FontWeight.Normal,
                        color = if (sel) Color(0xFF1C1E2A) else Color(0xFF9AA0B5)
                    )
                }
            }
        }
    }
}
