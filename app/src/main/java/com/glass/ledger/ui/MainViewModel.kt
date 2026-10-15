package com.glass.ledger.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.glass.ledger.GlassLedgerApp
import com.glass.ledger.data.AccountEntity
import com.glass.ledger.data.AccountType
import com.glass.ledger.data.LedgerRepository
import com.glass.ledger.data.RecordEntity
import com.glass.ledger.data.RecordType
import com.glass.ledger.data.RecordView
import com.glass.ledger.ui.theme.ExpenseRed
import com.glass.ledger.ui.theme.IncomeGreen
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth

data class DayGroup(
    val date: LocalDate,
    val expense: Double,
    val records: List<RecordView>
)

data class StatsSlice(
    val name: String,
    val iconKey: String,
    val colorArgb: Int,
    val total: Double
)

data class StatsUi(
    val slices: List<StatsSlice>,
    val totalExpense: Double,
    val totalIncome: Double
)

enum class StatsPeriod { WEEK, MONTH }

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class MainViewModel(private val repo: LedgerRepository) : ViewModel() {

    val selectedMonth = MutableStateFlow(YearMonth.now())
    val statsPeriod = MutableStateFlow(StatsPeriod.MONTH)

    val accounts: StateFlow<List<AccountEntity>> = repo.observeAccounts()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val fundTotal: StateFlow<Double> = repo.observeFundTotal()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0.0)

    val creditTotal: StateFlow<Double> = repo.observeCreditTotal()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0.0)

    val expenseCategories = repo.observeExpenseCategories()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val incomeCategories = repo.observeIncomeCategories()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    /** 所选月份的流水（带分类/账户冗余信息） */
    val monthRecords: StateFlow<List<DayGroup>> = selectedMonth
        .flatMapLatest { ym ->
            val (s, e) = ym.range()
            combine(
                repo.observeRecordsBetween(s, e),
                repo.observeAccounts(),
                combine(repo.observeExpenseCategories(), repo.observeIncomeCategories()) { a, b -> a + b }
            ) { records, accounts, cats ->
                val accMap = accounts.associateBy { it.id }
                val catMap = cats.associateBy { it.id }
                records
                    .map { r ->
                        val a = accMap[r.accountId]
                        val c = catMap[r.categoryId]
                        RecordView(
                            record = r,
                            categoryName = c?.name ?: "未知",
                            categoryIconKey = c?.iconKey ?: "more",
                            categoryColor = c?.colorArgb ?: 0xFF78909C.toInt(),
                            accountName = a?.name ?: "已删除账户",
                            accountIconId = a?.iconId ?: "card",
                            accountColor = a?.colorArgb ?: 0xFF5B6EF5.toInt()
                        )
                    }
                    .groupBy { LocalDate.ofEpochDay(it.record.dateEpochDay) }
                    .toSortedMap(compareByDescending { it })
                    .map { (date, list) ->
                        DayGroup(
                            date = date,
                            expense = list.filter { it.record.type == RecordType.EXPENSE }.sumOf { it.record.amount },
                            records = list
                        )
                    }
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    /** 所选月份 支出/收入 合计 */
    val monthSummary: StateFlow<Pair<Double, Double>> = selectedMonth
        .flatMapLatest { ym ->
            val (s, e) = ym.range()
            combine(repo.observeExpenseTotal(s, e), repo.observeIncomeTotal(s, e)) { exp, inc -> exp to inc }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0.0 to 0.0)

    /** 统计页：本周 / 本月 */
    val stats: StateFlow<StatsUi> = statsPeriod
        .flatMapLatest { period ->
            val (s, e) = when (period) {
                StatsPeriod.WEEK -> weekRange()
                StatsPeriod.MONTH -> YearMonth.now().range()
            }
            combine(
                repo.observeExpenseByCategory(s, e),
                combine(repo.observeExpenseCategories(), repo.observeIncomeCategories()) { a, b -> a + b },
                repo.observeExpenseTotal(s, e),
                repo.observeIncomeTotal(s, e)
            ) { sums, cats, exp, inc ->
                val catMap = cats.associateBy { it.id }
                StatsUi(
                    slices = sums.map { sm ->
                        val c = catMap[sm.categoryId]
                        StatsSlice(
                            name = c?.name ?: "未知",
                            iconKey = c?.iconKey ?: "more",
                            colorArgb = c?.colorArgb ?: 0xFF78909C.toInt(),
                            total = sm.total
                        )
                    },
                    totalExpense = exp,
                    totalIncome = inc
                )
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), StatsUi(emptyList(), 0.0, 0.0))

    fun shiftMonth(deltaMonths: Long) = selectedMonth.update { it.plusMonths(deltaMonths) }

    fun addRecord(
        type: RecordType,
        amount: Double,
        categoryId: Long,
        accountId: Long,
        date: LocalDate,
        note: String
    ) = viewModelScope.launch {
        repo.addRecord(
            RecordEntity(
                type = type,
                amount = amount,
                categoryId = categoryId,
                accountId = accountId,
                note = note,
                dateEpochDay = date.toEpochDay()
            )
        )
    }

    fun deleteRecord(record: RecordEntity) = viewModelScope.launch { repo.deleteRecord(record) }

    fun addAccount(
        name: String,
        iconId: String,
        colorArgb: Int,
        type: AccountType,
        initialBalance: Double,
        creditLimit: Double?
    ) = viewModelScope.launch {
        repo.addAccount(name, iconId, colorArgb, type, initialBalance, creditLimit)
    }

    fun deleteAccount(account: AccountEntity) = viewModelScope.launch { repo.deleteAccount(account) }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as GlassLedgerApp
                MainViewModel(app.repository)
            }
        }
    }
}

/** 饼图切片颜色兜底：按类别色，缺失时用主色循环 */
val FALLBACK_COLORS = listOf(ExpenseRed, IncomeGreen, androidx.compose.ui.graphics.Color(0xFF5B6EF5))
