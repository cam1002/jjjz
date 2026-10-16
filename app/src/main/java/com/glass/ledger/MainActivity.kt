package com.glass.ledger

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.glass.ledger.ui.MainViewModel
import com.glass.ledger.ui.base.GlassBrandButton
import com.glass.ledger.ui.base.GlassTokens
import com.glass.ledger.ui.components.GlassBackdrop
import com.glass.ledger.ui.components.GlassBottomBar
import com.glass.ledger.ui.components.Tab
import com.glass.ledger.ui.screens.AccountDetailScreen
import com.glass.ledger.ui.screens.AccountTypePickerDialog
import com.glass.ledger.ui.screens.AddAccountSheet
import com.glass.ledger.ui.screens.AddRecordSheet
import com.glass.ledger.ui.screens.AssetsScreen
import com.glass.ledger.ui.screens.LedgerScreen
import com.glass.ledger.ui.screens.RecordDetailScreen
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
    var showTypePicker by remember { mutableStateOf(false) }
    var pickedType by remember { mutableStateOf<com.glass.ledger.data.AccountType?>(null) }
    var presetAccountId by remember { mutableStateOf(0L) }
    var editingRecord by remember { mutableStateOf<com.glass.ledger.data.RecordView?>(null) }
    val detail by vm.accountDetail.collectAsStateWithLifecycle()
    val recordDetail by vm.recordDetail.collectAsStateWithLifecycle()

    Box(Modifier.fillMaxSize()) {
        // 液态玻璃背景光斑
        GlassBackdrop()

        val topPadding = PaddingValues(top = 56.dp)
        when (tab) {
            Tab.LEDGER -> LedgerScreen(vm, contentPadding = topPadding)
            Tab.ASSETS -> AssetsScreen(
                vm,
                contentPadding = topPadding,
                onAddAccount = { showTypePicker = true }
            )
            Tab.STATS -> StatsScreen(vm, contentPadding = topPadding)
        }

        // 悬浮记账按钮（右下角，底栏之上）：仅在记账页且没有任何全屏覆盖层时显示
        val fabShape = RoundedCornerShape(22.dp)
        AnimatedVisibility(
            visible = tab == Tab.LEDGER && detail == null && recordDetail == null,
            enter = fadeIn() + scaleIn(initialScale = 0.6f),
            exit = fadeOut() + scaleOut(targetScale = 0.6f),
            modifier = Modifier.align(Alignment.BottomEnd)
        ) {
            GlassBrandButton(
                onClick = { showAddRecord = true },
                modifier = Modifier
                    .navigationBarsPadding()
                    .padding(end = 24.dp, bottom = 96.dp)
                    .size(60.dp),
                shape = fabShape,
                elevation = GlassTokens.Elevation.Section
            ) {
                Icon(Icons.Filled.Add, contentDescription = "记一笔", tint = Color.White, modifier = Modifier.size(30.dp))
            }
        }

        // 悬浮玻璃底部 Tab：记账 / 资产 / 统计（对齐到屏幕底部）
        GlassBottomBar(
            selected = tab,
            onSelect = { tab = it },
            modifier = Modifier.align(Alignment.BottomCenter)
        )

        // 账户详情：全屏覆盖层（点击资产页账户打开）
        if (detail != null) {
            AccountDetailScreen(
                vm = vm,
                onBack = { vm.closeAccountDetail() },
                onAddRecord = {
                    presetAccountId = detail?.account?.id ?: 0L
                    showAddRecord = true
                }
            )
        }

        // 账单详情（收据页）：全屏覆盖层（点击记账页某笔流水打开）
        recordDetail?.let { d ->
            RecordDetailScreen(
                vm = vm,
                d = d,
                onBack = { vm.closeRecordDetail() },
                onEdit = {
                    editingRecord = it
                    vm.closeRecordDetail()
                    showAddRecord = true
                }
            )
        }
    }

    if (showAddRecord) {
        AddRecordSheet(
            vm,
            presetAccountId = presetAccountId,
            editing = editingRecord,
            onDismiss = {
                showAddRecord = false
                presetAccountId = 0L
                editingRecord = null
            }
        )
    }
    // 新增账户两步：先选类型，再填表单
    if (showTypePicker) {
        AccountTypePickerDialog(
            onDismiss = { showTypePicker = false },
            onPick = { type ->
                pickedType = type
                showTypePicker = false
            }
        )
    }
    pickedType?.let { type ->
        AddAccountSheet(vm, type = type, onDismiss = { pickedType = null })
    }
}
