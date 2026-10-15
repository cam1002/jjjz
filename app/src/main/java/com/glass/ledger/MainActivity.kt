package com.glass.ledger

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.glass.ledger.ui.MainViewModel
import com.glass.ledger.ui.components.GlassBackdrop
import com.glass.ledger.ui.components.GlassBottomBar
import com.glass.ledger.ui.components.Tab
import com.glass.ledger.ui.screens.AddAccountSheet
import com.glass.ledger.ui.screens.AddRecordSheet
import com.glass.ledger.ui.screens.AssetsScreen
import com.glass.ledger.ui.screens.LedgerScreen
import com.glass.ledger.ui.screens.StatsScreen
import com.glass.ledger.ui.theme.GlassLedgerTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            GlassLedgerTheme {
                GlassLedgerRoot()
            }
        }
    }
}

@Composable
fun GlassLedgerRoot(vm: MainViewModel = viewModel(factory = MainViewModel.Factory)) {
    var tab by remember { mutableStateOf(Tab.LEDGER) }
    var showAddRecord by remember { mutableStateOf(false) }
    var showAddAccount by remember { mutableStateOf(false) }

    Box(Modifier.fillMaxSize()) {
        // 液态玻璃背景光斑
        GlassBackdrop()

        val topPadding = PaddingValues(top = 56.dp)
        when (tab) {
            Tab.LEDGER -> LedgerScreen(vm, contentPadding = topPadding)
            Tab.ASSETS -> AssetsScreen(
                vm,
                contentPadding = topPadding,
                onAddAccount = { showAddAccount = true }
            )
            Tab.STATS -> StatsScreen(vm, contentPadding = topPadding)
        }

        // 悬浮记账按钮（右下角，底栏之上）
        val fabShape = RoundedCornerShape(22.dp)
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .navigationBarsPadding()
                .padding(end = 24.dp, bottom = 96.dp)
                .size(60.dp)
                .shadow(12.dp, fabShape)
                .background(Brush.linearGradient(listOf(Color(0xFF6C7CFF), Color(0xFF5B6EF5))), fabShape)
                .border(1.2.dp, Color.White.copy(alpha = 0.7f), fabShape)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) { showAddRecord = true }
        ) {
            Icon(Icons.Filled.Add, contentDescription = "记一笔", tint = Color.White, modifier = Modifier.size(30.dp))
        }

        // 悬浮玻璃底部 Tab：记账 / 资产 / 统计（对齐到屏幕底部）
        GlassBottomBar(
            selected = tab,
            onSelect = { tab = it },
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }

    if (showAddRecord) {
        AddRecordSheet(vm, onDismiss = { showAddRecord = false })
    }
    if (showAddAccount) {
        AddAccountSheet(vm, onDismiss = { showAddAccount = false })
    }
}
