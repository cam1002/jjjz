package com.glass.ledger.ui.screens

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.Contrast
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.glass.ledger.ui.MainViewModel
import com.glass.ledger.ui.ThemeMode
import com.glass.ledger.ui.base.GlassTokens
import com.glass.ledger.ui.components.GlassCard
import com.glass.ledger.ui.components.GlyphTile
import com.glass.ledger.ui.components.clickableNoRipple
import com.glass.ledger.ui.components.liquidGlass
import com.glass.ledger.ui.theme.ExpenseRed
import com.glass.ledger.ui.theme.IncomeGreen
import com.glass.ledger.ui.theme.InkPrimary
import com.glass.ledger.ui.theme.InkSecondary

/** 统计卡里「坚持记账天数」那块的小色块颜色（截图是橙色日历） */
private val TileOrange = Color(0xFFF0913A)

/**
 * 反馈收件邮箱：**占位地址**，真要收信前换成自己的。
 * App 本身不联网、不接后端，这里是把系统邮件客户端拉起来代发。
 */
private const val FEEDBACK_MAIL = "wzc1002@qq.com"

/**
 * 设置页（底部第四个 Tab）。
 */
@Composable
fun SettingsScreen(vm: MainViewModel, contentPadding: PaddingValues) {
    val context = LocalContext.current
    val recordCount by vm.recordCount.collectAsStateWithLifecycle()
    // vm.themeMode 是 SharedPreferences 的普通读写、不是可观察状态，所以界面自己存一份镜像，
    // 切换时同时写回 prefs：杀进程重进还是这一档。
    var mode by remember { mutableStateOf(vm.themeMode) }

    Column(
        Modifier
            .fillMaxSize()
            .padding(contentPadding)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
    ) {
        Text(
            "设置",
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            color = InkPrimary,
            modifier = Modifier.padding(top = 14.dp, bottom = 10.dp)
        )

        // 统计卡：天数从系统记录的首次安装时间算，笔数是全库流水条数
        GlassCard {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                StatCell(
                    icon = Icons.Filled.CalendarMonth,
                    tint = TileOrange,
                    value = vm.daysTracked.toString(),
                    unit = "天",
                    label = "坚持记账天数",
                    modifier = Modifier.weight(1f)
                )
                // 两栏之间的细竖线（截图里有，这里用墨色压到很淡，不抢玻璃面）
                Box(
                    Modifier
                        .width(1.dp)
                        .height(40.dp)
                        .background(InkSecondary.copy(alpha = 0.14f))
                )
                StatCell(
                    icon = Icons.AutoMirrored.Filled.ReceiptLong,
                    tint = GlassTokens.BrandEnd,
                    value = recordCount.toString(),
                    unit = "笔",
                    label = "总账单数",
                    modifier = Modifier.weight(1f)
                )
            }
        }

//        SettingsGroup("账务") {
//            SettingsRow(
//                icon = Icons.AutoMirrored.Filled.MenuBook,
//                base = ExpenseRed,
//                title = "账本管理",
//                subtitle = "当前只有一个账本「日常」",
//                onClick = { context.toast("账本管理后续开放") }
//            )
//        }

        SettingsGroup("通用") {
            // 整行不响应点击：右侧那个三档开关才是控件
            SettingsRow(
                icon = Icons.Filled.WbSunny,
                base = GlassTokens.BrandEnd,
                title = "主题模式",
                subtitle = mode.label,
                trailing = {
                    ThemeModeToggle(selected = mode) { picked ->
                        mode = picked
                        vm.themeMode = picked
                    }
                }
            )
        }

        SettingsGroup("其他") {
            SettingsRow(
                icon = Icons.Filled.Forum,
                base = IncomeGreen,
                title = "意见与建议反馈",
                subtitle = "交给系统邮件客户端",
                onClick = { sendFeedbackMail(context) }
            )
        }

        // 给悬浮底栏让位
        Spacer(Modifier.height(120.dp))
    }
}

/** 统计卡的一栏：浅色底板图标 + 大号数字带单位 + 灰色说明 */
@Composable
private fun StatCell(
    icon: ImageVector,
    tint: Color,
    value: String,
    unit: String,
    label: String,
    modifier: Modifier = Modifier
) {
    Row(modifier.padding(horizontal = 6.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(
            Modifier
                .size(38.dp)
                .clip(GlassTokens.Corner.Small)
                .background(tint.copy(alpha = 0.16f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(20.dp))
        }
        Column(Modifier.padding(start = 10.dp)) {
            // 数字和单位写在同一段富文本里：天然同一条基线，比两个 Text 硬对齐稳
            Text(
                buildAnnotatedString {
                    withStyle(
                        SpanStyle(fontSize = 24.sp, fontWeight = FontWeight.Bold, color = InkPrimary)
                    ) { append(value) }
                    withStyle(SpanStyle(fontSize = 13.sp, color = InkSecondary)) { append(" $unit") }
                }
            )
            Text(
                label,
                fontSize = GlassTokens.Type.Caption.sp,
                color = InkSecondary,
                modifier = Modifier.padding(top = 2.dp)
            )
        }
    }
}

/** 一个分组：小灰标题 + 一张玻璃列表卡 */
@Composable
private fun SettingsGroup(title: String, content: @Composable ColumnScope.() -> Unit) {
    Text(
        title,
        fontSize = GlassTokens.Type.SectionHeader.sp,
        color = InkSecondary,
        modifier = Modifier.padding(start = 6.dp, top = 22.dp, bottom = 8.dp)
    )
    Column(
        Modifier
            .fillMaxWidth()
            .liquidGlass(
                shape = GlassTokens.Corner.Field,
                alpha = GlassTokens.Surface.Card,
                elevation = GlassTokens.Elevation.Field
            )
    ) { content() }
}

/**
 * 设置行：彩色立体底板图标 + 标题（可带副标题）+ 右侧箭头或自定义控件。
 * [onClick] 为 null 时整行不响应点击（右侧控件自己处理），[trailing] 为 null 时画默认箭头。
 */
@Composable
private fun SettingsRow(
    icon: ImageVector,
    base: Color,
    title: String,
    subtitle: String? = null,
    trailing: (@Composable () -> Unit)? = null,
    onClick: (() -> Unit)? = null
) {
    val clickable = if (onClick != null) Modifier.clickableNoRipple(onClick) else Modifier
    Row(
        clickable
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 11.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        GlyphTile(icon = icon, base = base, size = 34.dp, contentDescription = title)
        Column(Modifier.weight(1f).padding(start = 12.dp)) {
            Text(title, fontSize = 16.sp, fontWeight = FontWeight.Medium, color = InkPrimary)
            if (subtitle != null) {
                Text(
                    subtitle,
                    fontSize = GlassTokens.Type.Caption.sp,
                    color = InkSecondary,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
        }
        if (trailing != null) trailing() else Chevron()
    }
}

@Composable
private fun Chevron() {
    Icon(
        Icons.AutoMirrored.Filled.KeyboardArrowRight,
        contentDescription = null,
        tint = InkSecondary.copy(alpha = 0.45f),
        modifier = Modifier.size(20.dp)
    )
}

/**
 * 主题模式三档开关（跟随系统 / 浅色 / 深色），样式沿用 [GlassSegmented] 的玻璃胶囊配方，
 * 只是把文字换成图标，好塞进行尾。
 */
@Composable
private fun ThemeModeToggle(selected: ThemeMode, onSelect: (ThemeMode) -> Unit) {
    Row(
        Modifier
            .liquidGlass(
                shape = GlassTokens.Corner.Pill,
                alpha = GlassTokens.Surface.Chip,
                elevation = GlassTokens.Elevation.Chip
            )
            .padding(3.dp),
        horizontalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        ThemeMode.entries.forEach { m ->
            val sel = m == selected
            Box(
                Modifier
                    .size(30.dp)
                    .then(
                        if (sel) Modifier.liquidGlass(
                            shape = GlassTokens.Corner.Circle,
                            alpha = GlassTokens.Surface.Selected,
                            elevation = GlassTokens.Elevation.Chip,
                            highlight = false
                        ) else Modifier
                    )
                    .clickableNoRipple { onSelect(m) },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    themeModeIcon(m),
                    contentDescription = m.label,
                    tint = if (sel) GlassTokens.AccentText else InkSecondary,
                    modifier = Modifier.size(15.dp)
                )
            }
        }
    }
}

private fun themeModeIcon(mode: ThemeMode): ImageVector = when (mode) {
    // 跟随系统用半明半暗的圆（BrightnessAuto 那个「A」字徽章在 15dp 上糊成一团，读不出来）
    ThemeMode.SYSTEM -> Icons.Filled.Contrast
    ThemeMode.LIGHT -> Icons.Filled.LightMode
    ThemeMode.DARK -> Icons.Filled.DarkMode
}

/**
 * 拉起系统邮件客户端写反馈。用 ACTION_SENDTO + mailto: 而不是 ACTION_SEND，
 * 这样只有邮件类应用会进选择器，不会跳去短信/社交 App。
 * 装了但没配账号时同样会抛 ActivityNotFoundException，所以统一 Toast 兜底。
 */
private fun sendFeedbackMail(context: Context) {
    val version = runCatching {
        context.packageManager.getPackageInfo(context.packageName, 0).versionName
    }.getOrNull() ?: "?"
    val intent = Intent(Intent.ACTION_SENDTO).apply {
        data = Uri.parse("mailto:")
        putExtra(Intent.EXTRA_EMAIL, arrayOf(FEEDBACK_MAIL))
        putExtra(Intent.EXTRA_SUBJECT, "简洁记账 意见反馈")
        // 末尾附一段本机信息方便远程定位问题，用户想删可以直接删掉
        putExtra(
            Intent.EXTRA_TEXT,
            "\n\n———\nApp 版本：$version\n" +
                "机型：${Build.MANUFACTURER} ${Build.MODEL}\n" +
                "系统：Android ${Build.VERSION.RELEASE}"
        )
    }
    try {
        context.startActivity(intent)
    } catch (_: ActivityNotFoundException) {
        context.toast("本机没有可用的邮件应用")
    }
}

private fun Context.toast(text: String) = Toast.makeText(this, text, Toast.LENGTH_SHORT).show()
