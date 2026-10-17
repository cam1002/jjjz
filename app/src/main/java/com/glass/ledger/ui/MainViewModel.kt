package com.glass.ledger.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.glass.ledger.GlassLedgerApp
import com.glass.ledger.data.AccountEntity
import com.glass.ledger.data.AccountType
import com.glass.ledger.data.CategoryEntity
import com.glass.ledger.data.IconCatalog
import com.glass.ledger.data.LedgerRepository
import com.glass.ledger.data.NO_ACCOUNT_ICON
import com.glass.ledger.data.NO_ACCOUNT_ID
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
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
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

/**
 * 主题模式偏好（设置页「通用」）。
 *
 * 注意：**目前只是把用户的选择存下来，还没有接入配色**。整套玻璃配色（GlassTokens、
 * theme/Color.kt 的底色光斑、白玻璃 + 深墨字）是按浅色写死的，深色要再配一套并逐屏核对，
 * 这一版按约定先不放功能，所以在这里改 `GlassLedgerTheme` 之前，选哪档界面都不变。
 */
enum class ThemeMode(val label: String) {
    SYSTEM("跟随系统"),
    LIGHT("浅色"),
    DARK("深色")
}

/** 账户详情页：单月统计 */
data class MonthStat(
    val month: Int,
    val label: String,
    val income: Double,
    val expense: Double,
    val endBalance: Double
)

/** 账户详情页整体数据 */
data class AccountDetailUi(
    val account: AccountEntity,
    val year: Int,
    val months: List<MonthStat>,
    val yearIncome: Double,
    val yearExpense: Double
)

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class MainViewModel(
    private val repo: LedgerRepository,
    private val prefs: android.content.SharedPreferences,
    /** 本机首次安装应用的日期（epoch day），由 Factory 从包信息里取 */
    private val installEpochDay: Long
) : ViewModel() {

    val selectedMonth = MutableStateFlow(YearMonth.now())
    val statsPeriod = MutableStateFlow(StatsPeriod.MONTH)

    /**
     * 账户列表读到过没有。用来区分"还在加载"和"一个账户都没有"：
     * 记一笔要靠它决定什么时候该把默认账户定下来，不然空列表的那一帧会被误判成没有账户。
     */
    val accountsLoaded = MutableStateFlow(false)

    val accounts: StateFlow<List<AccountEntity>> = repo.observeAccounts()
        .onEach { accountsLoaded.value = true }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    /**
     * 记一笔的默认账户 = 上次选的那个（存 SharedPreferences，杀进程也记得）。
     * 0 = 还没选过；-1（[NO_ACCOUNT_ID]）= 上次选的是「不计入账户」，这也是个合法记忆。
     */
    var lastAccountId: Long
        get() = prefs.getLong(KEY_LAST_ACCOUNT, 0L)
        set(value) = prefs.edit().putLong(KEY_LAST_ACCOUNT, value).apply()

    /**
     * 主题模式偏好（设置页）。存下来是给以后接深色配色用的，
     * **当前版本选了不会改变界面**，原因见 [ThemeMode] 的说明。
     */
    var themeMode: ThemeMode
        get() = prefs.getString(KEY_THEME_MODE, null)
            ?.let { saved -> ThemeMode.entries.firstOrNull { it.name == saved } }
            ?: ThemeMode.SYSTEM
        set(value) = prefs.edit().putString(KEY_THEME_MODE, value.name).apply()

    /**
     * 坚持记账天数：从今天数到首次安装那天，**安装当天算第 1 天**。
     * 用系统的 firstInstallTime 而不是自己记时间戳 —— 它天然就是"这个应用装进来多久"，
     * Studio 反复 Run（覆盖安装）不会重置，只有卸载重装才归零。
     */
    val daysTracked: Int
        get() = (LocalDate.now().toEpochDay() - installEpochDay).coerceAtLeast(0L).toInt() + 1

    /** 设置页「总账单数」：全库流水条数（含已退款，口径见 RecordDao.observeTotalCount） */
    val recordCount: StateFlow<Int> = repo.observeRecordCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)

    /** 资产总额（资金+充值+投资） */
    val assetTotal: StateFlow<Double> = repo.observeAssetTotal()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0.0)

    /** 负债总额（信用+债务，负数） */
    val liabilityTotal: StateFlow<Double> = repo.observeLiabilityTotal()
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
                    .map { r -> r.toView(accMap, catMap) }
                    .groupBy { LocalDate.ofEpochDay(it.record.dateEpochDay) }
                    .toSortedMap(compareByDescending { it })
                    .map { (date, list) ->
                        DayGroup(
                            date = date,
                            // 已退款的不计入当日合计（与月概览口径一致）
                            expense = list
                                .filter { it.record.type == RecordType.EXPENSE && !it.record.refunded }
                                .sumOf { it.record.amount },
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

    // ---------- 账户详情 ----------
    val detailAccountId = MutableStateFlow<Long?>(null)
    val detailYear = MutableStateFlow(YearMonth.now().year)

    fun openAccountDetail(id: Long) {
        detailYear.value = YearMonth.now().year
        detailAccountId.value = id
    }

    fun closeAccountDetail() { detailAccountId.value = null }

    fun shiftDetailYear(delta: Int) = detailYear.update { it + delta }

    fun updateAccount(account: AccountEntity) = viewModelScope.launch {
        repo.updateAccount(account)
    }

    /** 详情页聚合：某账户某年 12 个月的流入/流出/月末余额 */
    val accountDetail: StateFlow<AccountDetailUi?> =
        combine(detailAccountId, detailYear, repo.observeAccounts()) { id, year, accounts ->
            id?.let { accountId -> accounts.firstOrNull { it.id == accountId }?.let { accountId to it } }
        }
            .flatMapLatest { pair ->
                if (pair == null) kotlinx.coroutines.flow.flowOf(null)
                else repo.observeRecordsByAccount(pair.first).map { records -> pair to records }
            }
            .map { data ->
                if (data == null) return@map null
                val pair = data.first
                val account = pair.second
                val records = data.second
                val year = detailYear.value
                val today = LocalDate.now()
                val signed: (RecordEntity) -> Double = { r ->
                    if (r.type == RecordType.INCOME) r.amount else -r.amount
                }
                val months = (1..12).map { m ->
                    val start = YearMonth.of(year, m).atDay(1).toEpochDay()
                    val end = YearMonth.of(year, m).atEndOfMonth().toEpochDay()
                    val inMonth = records.filter { it.dateEpochDay in start..end }
                    val afterMonth = records.filter { it.dateEpochDay > end }.sumOf { signed(it) }
                    MonthStat(
                        month = m,
                        label = if (year == today.year && m == today.monthValue) "本月" else "${m}月",
                        income = inMonth.filter { it.type == RecordType.INCOME }.sumOf { it.amount },
                        expense = inMonth.filter { it.type == RecordType.EXPENSE }.sumOf { it.amount },
                        // 月末余额 = 当前余额 − 该月末之后发生的净流水
                        endBalance = account.balance - afterMonth
                    )
                }
                AccountDetailUi(
                    account = account,
                    year = year,
                    months = months,
                    yearIncome = months.sumOf { it.income },
                    yearExpense = months.sumOf { it.expense }
                )
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    fun addRecord(
        type: RecordType,
        amount: Double,
        categoryId: Long,
        accountId: Long,
        dateTime: java.time.LocalDateTime,
        note: String
    ) = viewModelScope.launch {
        repo.addRecord(
            RecordEntity(
                type = type,
                amount = amount,
                categoryId = categoryId,
                accountId = accountId,
                note = note,
                dateEpochDay = dateTime.toLocalDate().toEpochDay(),
                timeOfDay = dateTime.hour * 3600 + dateTime.minute * 60
            )
        )
    }

    fun deleteRecord(record: RecordEntity) = viewModelScope.launch { repo.deleteRecord(record) }

    fun updateRecord(old: RecordEntity, new: RecordEntity) = viewModelScope.launch {
        repo.updateRecord(old, new)
    }

    /** 退款：标记该笔已退款并把金额回补到它的账户（统计同步剔除） */
    fun refundRecord(record: RecordEntity) = viewModelScope.launch { repo.refundRecord(record) }

    /** 详情页「修改」：余额按新旧账户/金额自动重放 */
    fun saveRecordEdit(
        old: RecordEntity,
        type: RecordType,
        amount: Double,
        categoryId: Long,
        accountId: Long,
        dateTime: java.time.LocalDateTime,
        note: String
    ) = viewModelScope.launch {
        repo.updateRecord(
            old,
            old.copy(
                type = type,
                amount = amount,
                categoryId = categoryId,
                accountId = accountId,
                note = note,
                dateEpochDay = dateTime.toLocalDate().toEpochDay(),
                timeOfDay = dateTime.hour * 3600 + dateTime.minute * 60
            )
        )
    }

    // ---------- 账单详情（收据页） ----------
    private val detailRecordId = MutableStateFlow<Long?>(null)

    fun openRecordDetail(id: Long) { detailRecordId.value = id }
    fun closeRecordDetail() { detailRecordId.value = null }

    /**
     * 详情页数据：直接观察这一条流水，所以修改 / 退款 / 在别处删除都会让界面自动刷新，
     * 记录被删掉时流出发 null，界面随之关闭。
     */
    val recordDetail: StateFlow<RecordView?> = detailRecordId
        .flatMapLatest { id ->
            if (id == null) flowOf(null)
            else combine(
                repo.observeRecord(id),
                repo.observeAccounts(),
                combine(repo.observeExpenseCategories(), repo.observeIncomeCategories()) { a, b -> a + b }
            ) { record, accounts, cats ->
                if (record == null) null
                else record.toView(accounts.associateBy { it.id }, cats.associateBy { it.id })
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    fun addAccount(
        name: String,
        iconId: String,
        colorArgb: Int,
        type: AccountType,
        initialBalance: Double,
        creditLimit: Double?,
        note: String = ""
    ) = viewModelScope.launch {
        repo.addAccount(name, iconId, colorArgb, type, initialBalance, creditLimit, note)
    }

    fun deleteAccount(account: AccountEntity) = viewModelScope.launch { repo.deleteAccount(account) }

    companion object {
        private const val PREFS_NAME = "glass_ledger_prefs"
        private const val KEY_LAST_ACCOUNT = "lastAccountId"
        private const val KEY_THEME_MODE = "themeMode"

        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as GlassLedgerApp
                // 首选项而不是数据库表：这只是个界面偏好，不该跟着备份/导出走
                MainViewModel(
                    app.repository,
                    app.getSharedPreferences(PREFS_NAME, android.content.Context.MODE_PRIVATE),
                    installEpochDay(app)
                )
            }
        }

        /**
         * 首次安装日期。先换算成默认时区的本地日期再取 epoch day，
         * 否则 UTC 与本地差几小时会在边界上算错一天；拿不到就退回今天，
         * 最坏情况是显示 1 天，不会崩。
         */
        private fun installEpochDay(app: android.content.Context): Long = runCatching {
            val info = app.packageManager.getPackageInfo(app.packageName, 0)
            java.time.Instant.ofEpochMilli(info.firstInstallTime)
                .atZone(java.time.ZoneId.systemDefault())
                .toLocalDate()
                .toEpochDay()
        }.getOrDefault(LocalDate.now().toEpochDay())
    }
}

/** 饼图切片颜色兜底：按类别色，缺失时用主色循环 */
val FALLBACK_COLORS = listOf(ExpenseRed, IncomeGreen, androidx.compose.ui.graphics.Color(0xFF5B6EF5))

/** 流水 + 冗余的分类/账户信息 -> UI 视图模型（列表页与详情页共用） */
private fun RecordEntity.toView(
    accMap: Map<Long, AccountEntity>,
    catMap: Map<Long, CategoryEntity>
): RecordView {
    val a = accMap[accountId]
    val c = catMap[categoryId]
    // 记一笔时选了「不计入账户」的流水：accountId = -1，查不到账户，但显示上必须和
    // "账户被删了"（accountId = 0）区分开，前者是有意为之，后者是历史遗留。
    val detached = accountId == NO_ACCOUNT_ID
    return RecordView(
        record = this,
        categoryName = c?.name ?: "未知",
        categoryIconKey = c?.iconKey ?: "more",
        categoryColor = c?.colorArgb ?: 0xFF78909C.toInt(),
        accountName = if (detached) "不计入账户" else a?.name ?: "已删除账户",
        accountIconId = when {
            detached -> NO_ACCOUNT_ICON
            else -> a?.iconId ?: "card"
        },
        accountColor = when {
            detached -> IconCatalog.colorOf(NO_ACCOUNT_ICON)
            else -> a?.colorArgb ?: 0xFF5B6EF5.toInt()
        }
    )
}
