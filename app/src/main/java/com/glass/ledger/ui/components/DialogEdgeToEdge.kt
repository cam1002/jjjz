package com.glass.ledger.ui.components

import android.graphics.Color as AndroidColor
import android.graphics.drawable.ColorDrawable
import android.os.Build
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.window.DialogWindowProvider
import androidx.core.view.WindowCompat

/**
 * Dialog 窗口状态栏/导航栏适配：
 * 去掉默认灰色底与强制对比度蒙层，让玻璃背景延伸到系统栏，
 * 并统一使用深色图标（App 整体是浅色玻璃风）。
 * 在 Dialog 内容区顶部调用一次即可。
 *
 * @param dimAmount 窗口后方蒙层浓度：全屏页传 0f，居中面板保留淡蒙层。
 */
@Composable
fun DialogEdgeToEdge(dimAmount: Float = 0.35f) {
    val view = LocalView.current
    val window = (view.parent as? DialogWindowProvider)?.window
    SideEffect {
        val w = window ?: return@SideEffect
        w.setBackgroundDrawable(ColorDrawable(AndroidColor.TRANSPARENT))
        w.setDimAmount(dimAmount)
        @Suppress("DEPRECATION")
        w.statusBarColor = AndroidColor.TRANSPARENT
        @Suppress("DEPRECATION")
        w.navigationBarColor = AndroidColor.TRANSPARENT
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            w.isStatusBarContrastEnforced = false
            w.isNavigationBarContrastEnforced = false
        }
        // 深色图标：框架版 WindowInsetsController 只有 setter（Kotlin 合成属性需要 getter），
        // 这里统一用 androidx 的 Compat，内部会按 API 等级自动回落到 systemUiVisibility。
        val controller = WindowCompat.getInsetsController(w, view)
        controller.isAppearanceLightStatusBars = true
        controller.isAppearanceLightNavigationBars = true
    }
}
