package com.glass.ledger.ui.base

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.glass.ledger.ui.components.GlassBackdrop
import com.glass.ledger.ui.components.clickableNoRipple
import com.glass.ledger.ui.components.liquidGlass
import com.glass.ledger.ui.theme.InkPrimary
import com.glass.ledger.ui.theme.InkSecondary

/**
 * 品牌渐变实心操作钮（右下角 FAB、资产页的「+」）：
 * 统一用 [GlassTokens.BrandBrush] 渐变 + 白色描边 + 投影，别再各自写渐变色。
 */
@Composable
fun GlassBrandButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    shape: Shape = GlassTokens.Corner.Chip,
    elevation: Dp = GlassTokens.Elevation.Section,
    content: @Composable () -> Unit
) {
    Box(
        modifier
            .shadow(elevation, shape)
            .background(GlassTokens.BrandBrush, shape)
            .border(1.2.dp, Color.White.copy(alpha = 0.68f), shape)
            .clickableNoRipple(onClick),
        contentAlignment = Alignment.Center
    ) { content() }
}

/** 40dp 圆形玻璃按钮：顶栏返回/关闭等统一用它 */
@Composable
fun GlassIconButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Box(
        modifier
            .size(40.dp)
            .liquidGlass(
                shape = GlassTokens.Corner.Circle,
                alpha = GlassTokens.Surface.Card,
                elevation = GlassTokens.Elevation.Field
            )
            .clickableNoRipple(onClick),
        contentAlignment = Alignment.Center
    ) { content() }
}

/**
 * 统一顶栏：玻璃圆钮（返回或关闭）+ 标题 + 可选副标题 + 右侧自定义操作。
 * 所有全屏页、全屏弹窗都用它，保证标题字号、留白、按钮尺寸一致。
 */
@Composable
fun GlassTopBar(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    onBack: (() -> Unit)? = null,
    onClose: (() -> Unit)? = null,
    trailing: @Composable () -> Unit = {}
) {
    Row(
        modifier
            .fillMaxWidth()
            .padding(horizontal = GlassTokens.Space.Page, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val click = onBack ?: onClose
        if (click != null) {
            GlassIconButton(onClick = click) {
                Icon(
                    if (onBack != null) Icons.AutoMirrored.Filled.ArrowBack else Icons.Filled.Close,
                    contentDescription = if (onBack != null) "返回" else "关闭",
                    tint = InkPrimary,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(Modifier.size(14.dp))
        }
        Column(Modifier.weight(1f)) {
            Text(
                title,
                fontSize = GlassTokens.Type.PageTitle.sp,
                fontWeight = FontWeight.Bold,
                color = InkPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (subtitle != null) {
                Text(
                    subtitle,
                    fontSize = GlassTokens.Type.Caption.sp,
                    color = InkSecondary,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
        }
        trailing()
    }
}

/**
 * 全屏页骨架（Base 层）：页面底色渐变 + 彩色光斑 + 系统栏留白 + 统一顶栏 + 左右留白。
 *
 * 一级 Tab 页的背景由 MainActivity 统一提供；二级页、覆盖层、以及所有新的
 * 全屏界面都应该用 [GlassPage]（全屏弹窗用 [GlassPageDialog]），
 * 这样状态栏、背景、顶栏、留白天然一致。
 *
 * @param scroll true = 内容纵向滚动（普通表单/详情）；
 *               false = 内容自己负责滚动（内部放 LazyColumn 时用，避免无限高测量）。
 */
@Composable
fun GlassPage(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    onBack: (() -> Unit)? = null,
    onClose: (() -> Unit)? = null,
    trailing: @Composable () -> Unit = {},
    scroll: Boolean = true,
    horizontalPadding: Dp = GlassTokens.Space.Page,
    content: @Composable ColumnScope.() -> Unit
) {
    Box(
        modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(GlassTokens.PageBackground))
    ) {
        GlassBackdrop()
        Column(
            Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .imePadding()
        ) {
            GlassTopBar(
                title = title,
                subtitle = subtitle,
                onBack = onBack,
                onClose = onClose,
                trailing = trailing
            )
            Column(
                Modifier
                    .fillMaxWidth()
                    .then(if (scroll) Modifier.weight(1f).verticalScroll(rememberScrollState()) else Modifier)
                    .padding(horizontal = horizontalPadding)
            ) { content() }
        }
    }
}
