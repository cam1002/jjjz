package com.glass.ledger.ui.base

import android.graphics.Color as AndroidColor
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.view.View
import android.view.Window
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
 * @param dimAmount 全屏页传 0f（不压暗，让页面渐变顶到状态栏）；居中面板保留淡蒙层 0.35f。
 */
@Composable
fun GlassDialogWindow(dimAmount: Float = 0.35f) {
    val view = LocalView.current
    val window = (view.parent as? DialogWindowProvider)?.window
    SideEffect {
        val w = window ?: return@SideEffect
        GlassWindow.style(w, view, dimAmount)
    }
}
