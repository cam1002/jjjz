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
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.glass.ledger.data.IconCatalog
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
            .liquidGlass(GlassCornerLarge)
            .padding(contentPadding)
    ) { content() }
}

/** 账户图标：内置银行/APP 矢量图 */
@Composable
fun AccountIcon(iconId: String, size: Dp = 40.dp, modifier: Modifier = Modifier) {
    Image(
        painter = painterResource(IconCatalog.resOf(iconId)),
        contentDescription = IconCatalog.labelOf(iconId),
        modifier = modifier.size(size).clip(RoundedCornerShape(size / 3.2f))
    )
}

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
            .liquidGlass(GlassCornerPill, alpha = 0.5f, elevation = 4.dp)
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        options.forEach { (value, label) ->
            val isSel = value == selected
            Box(
                Modifier
                    .then(if (isSel) Modifier.liquidGlass(GlassCornerPill, alpha = 0.75f, elevation = 2.dp, highlight = false) else Modifier)
                    .padding(horizontal = 22.dp, vertical = 8.dp)
                    .clickableNoRipple { onSelect(value) },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    label,
                    color = if (isSel) Color(0xFF3D4CC8) else InkSecondary,
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
