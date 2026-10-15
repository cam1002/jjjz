package com.glass.ledger.ui.base

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.glass.ledger.ui.components.clickableNoRipple
import com.glass.ledger.ui.components.liquidGlass
import com.glass.ledger.ui.theme.InkPrimary
import com.glass.ledger.ui.theme.InkSecondary

/**
 * 居中玻璃弹窗面板骨架（Base 层）：
 * Dialog + 系统栏适配 + 统一圆角玻璃卡 + 标题行（可选关闭钮）+ 分隔线。
 * 内容通过 [content] 塞进面板，已经带好左右间距。
 */
@Composable
fun GlassDialogPanel(
    title: String,
    onDismiss: (() -> Unit)? = null,
    dimAmount: Float = 0.35f,
    divider: Boolean = true,
    content: @Composable ColumnScope.() -> Unit
) {
    Dialog(
        onDismissRequest = { onDismiss?.invoke() },
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        )
    ) {
        GlassDialogWindow(dimAmount = dimAmount)
        Column(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = GlassTokens.Space.Page)
                .shadow(GlassTokens.Elevation.Overlay, GlassTokens.Corner.Dialog)
                .clip(GlassTokens.Corner.Dialog)
                .background(Brush.verticalGradient(GlassTokens.PanelBackground))
                .padding(start = 22.dp, end = 22.dp, top = 20.dp, bottom = 16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    title,
                    fontSize = GlassTokens.Type.DialogTitle.sp,
                    fontWeight = FontWeight.Bold,
                    color = InkPrimary,
                    modifier = Modifier.weight(1f)
                )
                if (onDismiss != null) {
                    Spacer(Modifier.size(10.dp))
                    GlassIconButton(onClick = onDismiss) {
                        Icon(
                            Icons.Filled.Close,
                            contentDescription = "关闭",
                            tint = InkPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
            if (divider) {
                Spacer(Modifier.height(12.dp))
                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(Color(0x225A5F73))
                )
                Spacer(Modifier.height(6.dp))
            } else {
                Spacer(Modifier.height(12.dp))
            }
            content()
        }
    }
}

/**
 * 全屏页式弹窗（Base 层）：新增账户、记一笔这类"占满屏幕的表单页"都用它。
 * 顶栏是关闭钮 + 标题（+ 副标题 / 右侧自定义胶囊），内容自动滚动并处理键盘避让。
 */
@Composable
fun GlassPageDialog(
    onDismiss: () -> Unit,
    title: String,
    subtitle: String? = null,
    trailing: @Composable () -> Unit = {},
    scroll: Boolean = true,
    content: @Composable ColumnScope.() -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        )
    ) {
        GlassDialogWindow(dimAmount = 0f)
        GlassPage(
            title = title,
            subtitle = subtitle,
            onClose = onDismiss,
            trailing = trailing,
            scroll = scroll
        ) { content() }
    }
}

/** 弹窗里的文字选项行：标题 + 说明，点击整行（账户类型选择这类列表用） */
@Composable
fun GlassDialogOption(
    label: String,
    desc: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Column(
        modifier
            .fillMaxWidth()
            .clip(GlassTokens.Corner.Chip)
            .clickableNoRipple(onClick)
            .padding(vertical = 11.dp, horizontal = 6.dp)
    ) {
        Text(label, fontSize = 16.sp, fontWeight = FontWeight.Medium, color = InkPrimary)
        Text(
            desc,
            fontSize = 13.sp,
            color = InkSecondary,
            modifier = Modifier.padding(top = 3.dp)
        )
    }
}

/**
 * 主操作按钮（保存 / 确定）：品牌蓝紫渐变胶囊，禁用态自动降级为玻璃面板。
 * 所有界面主按钮统一用它，别再各自写渐变色。
 */
@Composable
fun GlassPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    brush: Brush = GlassTokens.BrandBrush
) {
    Text(
        text,
        fontSize = 16.sp,
        fontWeight = FontWeight.Bold,
        color = if (enabled) Color.White else InkSecondary,
        textAlign = TextAlign.Center,
        modifier = modifier
            .fillMaxWidth()
            .then(
                if (enabled) {
                    Modifier
                        .shadow(8.dp, GlassTokens.Corner.Pill)
                        .background(brush, GlassTokens.Corner.Pill)
                } else {
                    Modifier.liquidGlass(
                        shape = GlassTokens.Corner.Pill,
                        alpha = GlassTokens.Surface.Chip,
                        elevation = GlassTokens.Elevation.Chip
                    )
                }
            )
            .clickableNoRipple { if (enabled) onClick() }
            .padding(vertical = 14.dp)
    )
}

/** 面板内分隔线 */
@Composable
fun GlassFieldDivider(modifier: Modifier = Modifier) {
    Box(
        modifier
            .fillMaxWidth()
            .height(0.8.dp)
            .background(Color.White.copy(alpha = 0.75f))
    )
}
