package com.glass.ledger.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.glass.ledger.ui.theme.ExpenseRed
import kotlin.math.roundToInt
import kotlinx.coroutines.launch

/** 删除钮的宽度（高度跟着行走，两端半圆，所以这个值只决定中间那段直边有多长） */
private val REVEAL_WIDTH = 96.dp

/** 量到行高之前的兜底高度，见 [onSizeChanged] */
private val FALLBACK_HEIGHT = 68.dp

/** 拉开/收回的动画时长 */
private const val ANIM_MS = 180

/**
 * 左滑显示删除按钮的行容器（资产页的账户行用的就是它）。
 *
 * 交互按系统列表的习惯来：手指横向拖动时整行跟着走，松手时按「过半」决定停在
 * 露出按钮的位置还是弹回去。删除钮**高度和账户行完全一样**（用 [onSizeChanged] 实测行高，
 * 不写死），两端收成半圆（`CircleShape` 作用在非正方形上就是胶囊形），红渐变 + 白色
 * 垃圾桶 + 「删除」，形状语言和 Base 层的圆形玻璃钮一致。
 *
 * 两个容易踩的点：
 *
 * 1. 删除钮只在行真的拉开后才组装。玻璃行本身是半透明的，底下常驻一个红色块会直接透出来，
 *    每一行金额左边都会浮出一团淡红，看着像整页都坏了。
 * 2. 行拉开时点行本身只负责把它收回去（盖一层透明点击层），不会把点击透传给行自己的 onClick。
 *
 * 同一列表里只允许一行是拉开的：[opened] 由父级保管（谁拉开就把 key 记在谁身上），
 * 别的行 [opened] 变 false 时这里动画收回。删除完成、或父级换行时同理。
 */
@Composable
fun SwipeDeleteRow(
    opened: Boolean,
    onOpenedChange: (Boolean) -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val density = LocalDensity.current
    /** 实测行高（px）：删除钮的高，也是整行最多能滑出去的距离 */
    var rowHeightPx by remember { mutableFloatStateOf(0f) }
    val revealPx = with(density) { REVEAL_WIDTH.toPx() }
    /** 行的水平偏移：0 = 关着，-revealPx = 完全拉开 */
    val offset = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(opened) {
        if (!opened && offset.value != 0f) offset.animateTo(0f, tween(ANIM_MS))
    }

    /** 是否已经拉开一点（含拖动中）；没拉开时压根不绘制删除钮，见 KDoc 第 1 点 */
    val revealed = offset.value < -0.5f

    Box(
        modifier
            .fillMaxWidth()
            .onSizeChanged { rowHeightPx = it.height.toFloat() }
    ) {
        if (revealed) {
            val height = with(density) {
                (if (rowHeightPx > 0f) rowHeightPx else FALLBACK_HEIGHT.toPx()).toDp()
            }
            Row(
                Modifier
                    .align(Alignment.CenterEnd)
                    .size(REVEAL_WIDTH, height)
                    .clip(CircleShape)
                    .background(Brush.horizontalGradient(listOf(Color(0xFFFF7A66), ExpenseRed)))
                    .clickableNoRipple(onDelete),
                horizontalArrangement = Arrangement.spacedBy(7.dp, Alignment.CenterHorizontally),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Filled.Delete,
                    contentDescription = "删除",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    "删除",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
            }
        }

        Box(
            Modifier
                .fillMaxWidth()
                .offset { IntOffset(offset.value.roundToInt(), 0) }
                .pointerInput(revealPx) {
                    detectHorizontalDragGestures(
                        onDragEnd = {
                            val open = offset.value < -revealPx / 2
                            onOpenedChange(open)
                            scope.launch {
                                offset.animateTo(if (open) -revealPx else 0f, tween(ANIM_MS))
                            }
                        },
                        onDragCancel = {
                            onOpenedChange(false)
                            scope.launch { offset.animateTo(0f, tween(ANIM_MS)) }
                        }
                    ) { change, dragX ->
                        change.consume()
                        val next = (offset.value + dragX).coerceIn(-revealPx, 0f)
                        scope.launch { offset.snapTo(next) }
                    }
                }
        ) {
            content()
            if (revealed) {
                Box(
                    Modifier
                        .matchParentSize()
                        .clickableNoRipple { onOpenedChange(false) }
                )
            }
        }
    }
}
