package com.glass.ledger.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.glass.ledger.data.IconCatalog
import com.glass.ledger.ui.base.GlassIconButton
import com.glass.ledger.ui.base.GlassTokens
import com.glass.ledger.ui.blendTowardsWhite
import com.glass.ledger.ui.theme.InkPrimary
import com.glass.ledger.ui.theme.InkSecondary

/** 大玻璃卡片容器 */
@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    contentPadding: Dp = 20.dp,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier
            .fillMaxWidth()
            .liquidGlass(
                shape = GlassTokens.Corner.Card,
                alpha = GlassTokens.Surface.Card,
                elevation = GlassTokens.Elevation.Section
            )
            .padding(contentPadding)
    ) { content() }
}

/**
 * 账户图标。三种情况：
 * 1）目录里有内置矢量（微信、支付宝、四大行…）→ 直接画矢量；
 * 2）「其他」或只有品牌色、暂时还没有 logo 的银行（resId = 0）→ 立体色块 + 账户名首字；
 * 3）未知 id → 回落到通用银行卡图标（走分支 1）。
 */
@Composable
fun AccountIcon(
    iconId: String,
    size: Dp = 40.dp,
    modifier: Modifier = Modifier,
    name: String = "",
    colorArgb: Int = 0xFF5B6EF5.toInt()
) {
    val resId = IconCatalog.resOf(iconId)
    if (iconId == IconCatalog.OTHER_ID || resId == 0) {
        val base = Color(colorArgb)
        Box(
            modifier
                .size(size)
                .clip(RoundedCornerShape(size / 3.2f))
                .background(
                    Brush.linearGradient(listOf(base.blendTowardsWhite(0.24f), base, base.shade(0.78f)))
                )
                // 顶部高光，和同批立体渐变图标保持同一质感
                .background(
                    Brush.verticalGradient(0f to Color(0x30FFFFFF), 0.55f to Color(0x00FFFFFF))
                ),
            contentAlignment = Alignment.Center
        ) {
            val letter = name.trim().firstOrNull()?.toString() ?: "其"
            val letterSize = with(androidx.compose.ui.platform.LocalDensity.current) { (size / 2.2f).toSp() }
            Text(
                letter,
                color = Color.White,
                fontSize = letterSize,
                fontWeight = FontWeight.Bold,
                maxLines = 1
            )
        }
    } else {
        Image(
            painter = painterResource(resId),
            contentDescription = IconCatalog.labelOf(iconId),
            modifier = modifier.size(size).clip(RoundedCornerShape(size / 3.2f))
        )
    }
}

/** 按系数压暗（保留透明度），用于立体底板的暗端 */
private fun Color.shade(f: Float) = Color(red * f, green * f, blue * f, alpha)

/** 分类图标：圆形浅色底 + 主题色 Material 图标 */
@Composable
fun CategoryIcon(
    iconKey: String,
    colorArgb: Int,
    size: Dp = 40.dp,
    modifier: Modifier = Modifier
) {
    val color = Color(colorArgb)
    Box(
        modifier.size(size).clip(CircleShape).background(color.copy(alpha = 0.16f)),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            CategoryIcons.of(iconKey),
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(size * 0.52f)
        )
    }
}

/** 区块标题 */
@Composable
fun SectionTitle(text: String, modifier: Modifier = Modifier) {
    Text(
        text,
        modifier.padding(vertical = 10.dp),
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold
    )
}

/** 玻璃分段选择器 */
@Composable
fun <T> GlassSegmented(
    options: List<Pair<T, String>>,
    selected: T,
    onSelect: (T) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier
            .liquidGlass(
                shape = GlassTokens.Corner.Pill,
                alpha = GlassTokens.Surface.Chip,
                elevation = GlassTokens.Elevation.Field
            )
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        options.forEach { (value, label) ->
            val isSel = value == selected
            Box(
                Modifier
                    .then(
                        if (isSel) Modifier.liquidGlass(
                            shape = GlassTokens.Corner.Pill,
                            alpha = GlassTokens.Surface.Selected,
                            elevation = GlassTokens.Elevation.Chip,
                            highlight = false
                        ) else Modifier
                    )
                    .padding(horizontal = 22.dp, vertical = 8.dp)
                    .clickableNoRipple { onSelect(value) },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    label,
                    color = if (isSel) GlassTokens.AccentText else InkSecondary,
                    fontSize = 14.sp,
                    fontWeight = if (isSel) FontWeight.SemiBold else FontWeight.Normal
                )
            }
        }
    }
}

/** 无边框点击 */
@Composable
fun Modifier.clickableNoRipple(onClick: () -> Unit): Modifier = this.then(
    Modifier.clickable(
        interactionSource = remember { MutableInteractionSource() },
        indication = null,
        onClick = onClick
    )
)

/** 全屏页左上角玻璃关闭按钮（转发到 Base 层 GlassIconButton） */
@Composable
fun GlassCloseButton(onDismiss: () -> Unit, modifier: Modifier = Modifier) {
    GlassIconButton(onClick = onDismiss, modifier = modifier) {
        Icon(
            Icons.Filled.Close,
            contentDescription = "关闭",
            tint = InkPrimary,
            modifier = Modifier.size(20.dp)
        )
    }
}
