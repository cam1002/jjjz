package com.glass.ledger.ui.base

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.glass.ledger.ui.components.clickableNoRipple
import com.glass.ledger.ui.components.liquidGlass
import com.glass.ledger.ui.theme.InkPrimary

/**
 * 底部抽屉骨架（Base 层）：从屏幕底下升起的面板，承载"选一个东西"这类短列表
 * （选择账户、以后还有选择分类），比再开一个全屏页轻得多。
 * 面板底色、标题字号、关闭钮与 [GlassDialogPanel] 同源，只是贴底、上面两个圆角。
 *
 * 实现要点：仍然用 `fillScreen = true` 把弹窗窗口铺满整屏（原因见 [GlassDialogWindow]），
 * 然后在满屏 Box 里用 `Alignment.BottomCenter` 把面板压到底部 ——
 * 让窗口自己 wrap_content 是贴不到底的，系统会把它垂直居中。
 *
 * @param maxScreenFraction 面板最高占屏幕的比例。内容超出时列表自己滚动；
 *                          高度上限用 [heightIn] 而不是 weight，
 *                          因为 weight 在 wrap_content 的 Column 里会塌成 0（这条踩过）。
 */
@Composable
fun GlassBottomSheet(
    title: String,
    onDismiss: () -> Unit,
    maxScreenFraction: Float = 0.74f,
    content: @Composable ColumnScope.() -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        )
    ) {
        GlassDialogWindow(dimAmount = 0.35f, fillScreen = true)
        val screenHeight = LocalConfiguration.current.screenHeightDp.dp
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.BottomCenter) {
            // Dialog 的内容是新建的，所以一个 LaunchedEffect 就能让首帧从下面滑进来
            var shown by remember { mutableStateOf(false) }
            LaunchedEffect(Unit) { shown = true }
            AnimatedVisibility(
                visible = shown,
                enter = slideInVertically(
                    animationSpec = tween(240),
                    initialOffsetY = { it }
                ) + fadeIn(animationSpec = tween(240))
            ) {
                Column(
                    Modifier
                        .fillMaxWidth()
                        .heightIn(max = screenHeight * maxScreenFraction)
                        .shadow(GlassTokens.Elevation.Overlay, GlassTokens.Corner.BottomSheet)
                        .clip(GlassTokens.Corner.BottomSheet)
                        .background(Brush.verticalGradient(GlassTokens.PanelBackground))
                ) {
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .padding(start = 22.dp, end = 16.dp, top = 18.dp, bottom = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            title,
                            fontSize = GlassTokens.Type.DialogTitle.sp,
                            fontWeight = FontWeight.Bold,
                            color = InkPrimary,
                            modifier = Modifier.weight(1f)
                        )
                        GlassIconButton(onClick = onDismiss, size = 34.dp) {
                            Icon(
                                Icons.Filled.Close,
                                contentDescription = "关闭",
                                tint = InkPrimary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(Color(0x1A5A5F73))
                    )
                    Column(
                        Modifier
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState())
                            .navigationBarsPadding()
                            .padding(horizontal = GlassTokens.Space.Page)
                    ) { content() }
                }
            }
        }
    }
}

/**
 * 抽屉里的图标行：左图标 + 标题 + 右对齐的值，行尾一条细线（最后一行不给线）。
 * 选中态用 [GlassTokens.Surface.Selected] 玻璃底 + 半粗字，参考截图里当前项的处理。
 */
@Composable
fun GlassSheetRow(
    title: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    leading: @Composable () -> Unit,
    trailing: String? = null,
    trailingColor: Color = InkPrimary,
    divider: Boolean = true
) {
    Column(modifier.fillMaxWidth()) {
        Row(
            Modifier
                .fillMaxWidth()
                .then(
                    if (selected) Modifier.liquidGlass(
                        shape = GlassTokens.Corner.Chip,
                        alpha = GlassTokens.Surface.Selected,
                        elevation = GlassTokens.Elevation.Chip
                    ) else Modifier
                )
                .clickableNoRipple(onClick)
                .padding(horizontal = 8.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            leading()
            Spacer(Modifier.size(12.dp))
            Text(
                title,
                fontSize = 16.sp,
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                color = InkPrimary,
                modifier = Modifier.weight(1f)
            )
            if (trailing != null) {
                Text(trailing, fontSize = 16.sp, fontWeight = FontWeight.Medium, color = trailingColor)
            }
        }
        if (divider) {
            Box(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp)
                    .height(0.8.dp)
                    .background(Color(0x145A5F73))
            )
        }
    }
}
