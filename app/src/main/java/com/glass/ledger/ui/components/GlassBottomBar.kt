package com.glass.ledger.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.InsertChart
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.glass.ledger.ui.theme.InkSecondary

enum class Tab(val label: String, val icon: ImageVector) {
    LEDGER("记账", Icons.AutoMirrored.Filled.MenuBook),
    ASSETS("资产", Icons.Filled.AccountBalanceWallet),
    STATS("统计", Icons.Filled.InsertChart)
}

/** 悬浮液态玻璃底部 Tab 栏 */
@Composable
fun GlassBottomBar(
    selected: Tab,
    onSelect: (Tab) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 28.dp, vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier
                .liquidGlass(shape = GlassCornerPill, alpha = 0.68f, elevation = 16.dp)
                .height(64.dp)
                .padding(horizontal = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Tab.entries.forEach { tab ->
                TabItem(tab = tab, selected = tab == selected, onClick = { onSelect(tab) })
            }
        }
    }
}

@Composable
private fun RowScope.TabItem(tab: Tab, selected: Boolean, onClick: () -> Unit) {
    val contentColor by animateColorAsState(
        if (selected) Color(0xFF3D4CC8) else InkSecondary,
        label = "tabColor"
    )
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .weight(1f)
            .padding(vertical = 6.dp)
            .then(
                if (selected) {
                    Modifier.liquidGlass(
                        shape = RoundedCornerShape(18.dp),
                        alpha = 0.5f,
                        elevation = 2.dp,
                        highlight = false
                    )
                } else Modifier
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .padding(vertical = 6.dp)
    ) {
        Icon(tab.icon, contentDescription = tab.label, tint = contentColor, modifier = Modifier.size(22.dp))
        Text(
            tab.label,
            color = contentColor,
            style = androidx.compose.material3.MaterialTheme.typography.labelMedium,
            modifier = Modifier.padding(top = 2.dp)
        )
    }
}
