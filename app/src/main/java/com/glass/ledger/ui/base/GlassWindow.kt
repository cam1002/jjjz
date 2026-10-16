package com.glass.ledger.ui.base

import android.graphics.Color as AndroidColor
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.view.View
import android.view.Window
import android.view.WindowManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.window.DialogWindowProvider
import androidx.core.view.WindowCompat

/**
 * 系统栏风格的唯一实现（Base 层）。
 * Activity 与所有 Dialog 都走 [style]，因此任何界面打开后状态栏表现完全一致：
 * 背景透明、图标深色（浅色玻璃配色）、不叠加系统强制对比度蒙层。
 *
 * @param dimAmount 为 null 时不动窗口背景与蒙层（Activity 用）；
 *                  传值时把弹窗窗口背景设为透明并按该浓度压暗背后（Dialog 用）。
 */
object GlassWindow {

    fun style(window: Window, view: View, dimAmount: Float?) {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        if (dimAmount != null) {
            window.setBackgroundDrawable(ColorDrawable(AndroidColor.TRANSPARENT))
            window.setDimAmount(dimAmount)
            @Suppress("DEPRECATION")
            window.statusBarColor = AndroidColor.TRANSPARENT
            @Suppress("DEPRECATION")
            window.navigationBarColor = AndroidColor.TRANSPARENT
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // 状态栏/导航栏对比度蒙层：Android 15（API 35）把这两个 setter 标了废弃，
            // 但官方文档明确写了"deprecated but still have an effect on Android 15"，
            // 也没有给出替代 API —— 不关掉的话透明状态栏上会浮一层系统自算的灰色 scrim，
            // 直接破坏玻璃质感。所以这里保留调用，只压掉告警。
            @Suppress("DEPRECATION")
            window.isStatusBarContrastEnforced = false
            window.isNavigationBarContrastEnforced = false
        }
        // 框架版 WindowInsetsController 只有 setter，统一用 androidx Compat
        val controller = WindowCompat.getInsetsController(window, view)
        controller.isAppearanceLightStatusBars = true
        controller.isAppearanceLightNavigationBars = true
    }
}

/**
 * 在 Dialog 内容区顶部调用一次，把弹窗自己的 Window 适配成 App 风格。
 *
 * @param dimAmount 全屏页传 0f（不压暗）；居中面板保留淡蒙层 0.35f。
 * @param fillScreen true = 让弹窗窗口铺满整块屏幕（含状态栏、刘海区域）。全屏表单页必须传：
 *   Compose 的 Dialog 在 `usePlatformDefaultWidth = false` 时按"整块屏幕高度"测量内容，
 *   再把窗口尺寸设成内容的测量结果；而系统默认还会把这个窗口整体压到状态栏下方
 *   （Android 15+ 由 LayoutParams 的 fitInsetsTypes 决定，实测状态栏 142px 就是这么凭空多出来的）。
 *   两者一叠加，可见区域就比内容矮一个状态栏，底部固定按钮会被顶出屏幕。
 *   关掉避让后窗口从 (0,0) 铺开，测量高度 = 可见高度，页面渐变也能通到状态栏底下，
 *   状态栏/导航栏的留白交回给 [GlassPage] 的 statusBarsPadding + navigationBarsPadding 处理。
 *   居中面板不要传，它要靠 wrap_content 才能垂直居中。
 */
@Composable
fun GlassDialogWindow(dimAmount: Float = 0.35f, fillScreen: Boolean = false) {
    val view = LocalView.current
    val window = (view.parent as? DialogWindowProvider)?.window
    SideEffect {
        val w = window ?: return@SideEffect
        GlassWindow.style(w, view, dimAmount)
        if (fillScreen) {
            val lp = w.attributes
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                lp.layoutInDisplayCutoutMode =
                    WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_ALWAYS
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.VANILLA_ICE_CREAM) {
                // Android 15+：不让窗口主动避让系统栏，否则它会被整个压到状态栏下面，
                // 而 Compose 仍按整屏高度测量，底部就多出正好一个状态栏的高度。
                lp.setFitInsetsTypes(0)
                lp.setFitInsetsSides(0)
            }
            w.attributes = lp
        }
    }
}
