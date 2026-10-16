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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
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
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.roundToInt

/**
 * 数字滚轮（iOS 风格）：上下滑动选择，松手自动吸附到整格，中间高亮。
 * 用于年月日时分日期选择器。
 *
 * [selectedIndex] 是**受控**的外部值：外部改了它（例如点「当前日期」一次性回填年月日时分），
 * 滚轮自己滚过去；手势滑动结束后用 [onSelected] 把值报回外部。
 * 两个方向靠 [lastEmitted] + [programmaticScroll] 互斥，所以不会出现
 * "外部已经是现在、滚轮还停在旧位置"或者"滚轮把旧位置又塞回外部"的来回打架。
 *
 * 另外注意 `rememberLazyListState` 只认首次组合的初始索引，之后传新值进来一律忽略，
 * 所以程序化赋值必须显式 scrollToItem。
 */
@Composable
fun NumberWheel(
    values: List<Int>,
    selectedIndex: Int,
    modifier: Modifier = Modifier,
    showTwoDigits: Boolean = false,
    onSelected: (Int) -> Unit
) {
    if (values.isEmpty()) return
    val itemHeight = 40.dp
    val itemHeightPx = with(LocalDensity.current) { itemHeight.toPx() }
    val listState = rememberLazyListState(
        initialFirstVisibleItemIndex = selectedIndex.coerceIn(0, values.lastIndex)
    )

    /** 当前居中的项索引（含滑动中的四舍五入） */
    fun centeredIndex(): Int =
        (listState.firstVisibleItemIndex +
            (listState.firstVisibleItemScrollOffset / itemHeightPx).roundToInt())
            .coerceIn(0, values.lastIndex)

    /**
     * 滚轮当前居中的索引，供高亮用。
     * 注意别叫 `selectedIndex`：那会遮蔽同名参数，"跟随外部赋值"的 effect 会变成自己和自己判等。
     */
    val centered by remember { derivedStateOf { centeredIndex() } }
    val scope = rememberCoroutineScope()

    // 程序化滚动标记：滚动期间不接受手势上报，避免覆盖外部刚写进去的值
    var programmaticScroll by remember { mutableStateOf(false) }
    // 上一次报给外部的索引；只有它和居中索引不一致时才回调，天然断开"赋值 -> 上报 -> 再赋值"的循环
    var lastEmitted by remember { mutableStateOf(selectedIndex.coerceIn(0, values.lastIndex)) }

    // 1) 外部把值改了（「当前日期」一次性回填）就把滚轮滚过去。
    //    这里不能用 isScrollInProgress 当闸门：手滑动/吸附动画期间它可能一直为 true，
    //    那样外部值已经更新、滚轮却一次都不动，就是之前那个 bug。
    LaunchedEffect(selectedIndex, values.size) {
        val target = selectedIndex.coerceIn(0, values.lastIndex)
        if (centered != target) {
            programmaticScroll = true
            listState.animateScrollToItem(target)
            lastEmitted = target
            programmaticScroll = false
        }
    }

    // 2) 用户手势停下之后：吸附到整格 + 把值报回外部
    LaunchedEffect(listState, values.size) {
        snapshotFlow { listState.isScrollInProgress }
            .distinctUntilChanged()
            .filter { !it }
            .collect {
                if (programmaticScroll) return@collect
                val idx = centeredIndex()
                if (abs(listState.firstVisibleItemScrollOffset) > 1 ||
                    listState.firstVisibleItemIndex != idx
                ) {
                    listState.animateScrollToItem(idx)
                }
                if (idx != lastEmitted) {
                    lastEmitted = idx
                    onSelected(idx)
                }
            }
    }

    // 3) 列表长度变化（如换月后天数变少）时把滚轮夹回合法位置
    LaunchedEffect(values.size) {
        val target = listState.firstVisibleItemIndex.coerceAtMost(values.lastIndex)
        listState.scrollToItem(target)
        lastEmitted = target
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
                val sel = i == centered
                Box(
                    Modifier
                        .height(itemHeight)
                        .fillMaxWidth()
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {
                            scope.launch {
                                programmaticScroll = true
                                listState.animateScrollToItem(i)
                                lastEmitted = i
                                programmaticScroll = false
                            }
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
