package com.glass.ledger.ui.base

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * 玻璃记账设计令牌（Base 层，全站唯一来源）。
 *
 * 约定：新界面不允许写死圆角、投影、透明度、品牌渐变，一律用这里的 token。
 * 想整体调风格（更透/更实、更圆、阴影更重），只改这一个文件。
 */
object GlassTokens {

    /** 全局玻璃清晰度：0 = 完全按请求透明度（通透、易干扰文字），1 = 完全不透明。 */
    const val CLARITY: Float = 0.55f

    /** 请求透明度 -> 实际绘制透明度：整体抬到可读区间，同时保留面板之间的层次差。 */
    fun surfaceAlpha(requested: Float): Float =
        (requested + (1f - requested) * CLARITY).coerceIn(0f, 0.97f)

    /** 圆角档位 */
    object Corner {
        /** 大面板 / 主卡 */
        val Card: Shape = RoundedCornerShape(30.dp)
        /** 列表卡、输入卡、区块卡 */
        val Field: Shape = RoundedCornerShape(22.dp)
        /** 分类格、按钮 */
        val Chip: Shape = RoundedCornerShape(16.dp)
        /** 图标格、小标签 */
        val Small: Shape = RoundedCornerShape(14.dp)
        /** 行内极小标签（账户小胶囊） */
        val Tag: Shape = RoundedCornerShape(8.dp)
        /** 胶囊 */
        val Pill: Shape = RoundedCornerShape(percent = 50)
        /** 圆形按钮 */
        val Circle: Shape = CircleShape
        /** 弹窗面板 */
        val Dialog: Shape = RoundedCornerShape(28.dp)
    }

    /** 投影档位 */
    object Elevation {
        val Flat: Dp = 0.dp
        val Chip: Dp = 2.dp
        val Field: Dp = 3.dp
        val Card: Dp = 5.dp
        val Section: Dp = 12.dp
        /** 悬浮底栏 */
        val Bar: Dp = 16.dp
        val Overlay: Dp = 18.dp
    }

    /**
     * 面板透明度档位（写入 liquidGlass 的 alpha，最终值由 [surfaceAlpha] 决定）。
     * 选档规则：越靠近内容主体，档位越高。
     */
    object Surface {
        /** 未选中的小 pill */
        const val Ghost: Float = 0.28f
        /** chips / 分段器 / 输入卡 */
        const val Chip: Float = 0.5f
        /** 列表行、图标格、顶栏圆钮 */
        const val Card: Float = 0.6f
        /** 底部导航栏 */
        const val Bar: Float = 0.68f
        /** 选中态（分段器滑块、选中 chip） */
        const val Selected: Float = 0.75f
        /** 弹窗面板 */
        const val Panel: Float = 0.85f
    }

    /** 间距 */
    object Space {
        /** 页面左右留白 */
        val Page: Dp = 20.dp
        /** 卡片间间距 */
        val Gap: Dp = 12.dp
        /** 组内间距 */
        val Row: Dp = 8.dp
        /** 区块标题到内容 */
        val SectionTop: Dp = 16.dp
        /** 底部收尾留白 */
        val Bottom: Dp = 30.dp
    }

    /** 字号 */
    object Type {
        const val PageTitle: Int = 20
        const val DialogTitle: Int = 18
        const val SectionHeader: Int = 14
        const val Body: Int = 15
        const val Caption: Int = 12
        const val Micro: Int = 10
    }

    /** 全屏页 / 详情页底色（自上而下） */
    val PageBackground: List<Color> = listOf(Color(0xFFEFF2FC), Color(0xFFE4E9FA))

    /** 弹窗面板底色（自带轻微玻璃渐变） */
    val PanelBackground: List<Color> = listOf(Color(0xF6FFFFFF), Color(0xF2EDF1FE))

    /** 品牌主色与主按钮渐变 */
    val BrandStart = Color(0xFF6C7CFF)
    val BrandEnd = Color(0xFF5B6EF5)
    val BrandBrush: Brush = Brush.linearGradient(listOf(BrandStart, BrandEnd))

    /** 品牌浅底（文字型小胶囊背景） */
    val BrandSoft = Color(0xFFE8ECFF)

    /** 选中态文字主色 */
    val AccentText = Color(0xFF3D4CC8)
}
