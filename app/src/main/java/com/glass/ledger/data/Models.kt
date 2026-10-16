package com.glass.ledger.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/** 账户类型：资金 / 信用 / 充值 / 投资 / 债务 */
enum class AccountType { FUND, CREDIT, RECHARGE, INVEST, DEBT }

/** 各账户类型的展示文案（新增账户第一步的选择面板使用） */
object AccountTypes {
    val all: List<Triple<AccountType, String, String>> = listOf(
        Triple(AccountType.FUND, "资金账户", "现金、储蓄卡、网络支付等"),
        Triple(AccountType.CREDIT, "信用账户", "花呗、白条、信用卡等"),
        Triple(AccountType.RECHARGE, "充值账户", "交通卡、饭卡、会员卡等"),
        Triple(AccountType.INVEST, "投资账户", "股票、基金等"),
        Triple(AccountType.DEBT, "债务账户", "借入、借出等")
    )
    fun label(type: AccountType) = all.first { it.first == type }.second
    fun desc(type: AccountType) = all.first { it.first == type }.third
}

/** 流水类型：支出 / 收入 */
enum class RecordType { EXPENSE, INCOME }

/**
 * 记一笔里「不计入账户」用的 accountId 哨兵值。
 * 选它 = 这笔流水照常记账、照常进统计，但**不动任何账户的余额**
 * （`adjustBalance` 拿 -1 去 UPDATE 命中不到行，天然就是空操作）。
 *
 * 为什么是 -1 而不是 0：0 已经被"删除账户后解绑的流水"占用了，
 * 两者在界面上要分开显示（一个是「不计入账户」，一个是「已删除账户」），不能混成一个值。
 */
const val NO_ACCOUNT_ID = -1L

/** 债务账户里「借出」图标的 id：它决定这笔债务记正还是记负，见 [signedBalanceOf] */
const val LEND_OUT_ICON = "lend_out"

/** 债务账户里「借入」图标的 id */
const val BORROW_IN_ICON = "borrow_in"

/** 「不计入账户」这一行用的图标 id（灰色铅笔），见 IconCatalog */
const val NO_ACCOUNT_ICON = "no_account"

/**
 * 这个账户是「欠别人的钱」吗？决定余额记正还是记负、输入框提示语怎么写、资产页组头怎么汇总。
 *
 * 信用账户（信用卡/花呗/白条）一律是欠款 → 是。
 * 债务账户看图标分方向：借出 = 别人欠我的（应收）→ 不是；借入和其它 → 是。
 */
fun isPayableAccount(type: AccountType, iconId: String): Boolean =
    type == AccountType.CREDIT || (type == AccountType.DEBT && iconId != LEND_OUT_ICON)

/**
 * 把用户在开户/修改账户里输入的正数金额转成实际存进库的余额。
 * 负债类记负数（欠钱），借出记正数（别人欠我的，算资产），其余类型原样。
 */
fun signedBalanceOf(type: AccountType, iconId: String, input: Double): Double {
    if (type != AccountType.CREDIT && type != AccountType.DEBT) return input
    val v = input.coerceAtLeast(0.0)
    return if (isPayableAccount(type, iconId)) -v else v
}

/** 资产账户：银行卡、微信、支付宝、花呗等 */
@Entity(tableName = "accounts")
data class AccountEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    /** 图标 id，见 IconCatalog */
    val iconId: String,
    /** 自定义底色（ARGB Int） */
    val colorArgb: Int,
    val type: AccountType,
    /**
     * 当前余额。负数 = 欠别人的钱（信用账户、债务账户的借入），
     * 债务账户的「借出」是正数（别人欠我的，算资产），符号规则见 [signedBalanceOf]。
     */
    val balance: Double,
    /** 信用账户额度，资金账户为 null */
    val creditLimit: Double? = null,
    val sortOrder: Int = 0,
    /**
     * 备注（开户时选填）。
     * defaultValue 要写成 SQL 表达式 `''`（两个单引号），不是空串：
     * 迁移用的是 ALTER TABLE ... DEFAULT ''，Room 校验时会拿 PRAGMA table_info
     * 返回的 dflt_value（就是 `''` 这两个字符）来比对，写成 "" 会报
     * "Migration didn't properly handle: accounts"。
     */
    @ColumnInfo(defaultValue = "''")
    val note: String = ""
)

/** 收支分类 */
@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    /** Material 图标 key，见 CategoryIcons */
    val iconKey: String,
    val colorArgb: Int,
    val isExpense: Boolean,
    val sortOrder: Int = 0
)

/** 一笔流水 */
@Entity(tableName = "records")
data class RecordEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val type: RecordType,
    /** 金额，恒为正数 */
    val amount: Double,
    val categoryId: Long,
    val accountId: Long,
    val note: String = "",
    /** LocalDate.toEpochDay() */
    val dateEpochDay: Long,
    /** 当日秒数（时/分），LocalDateTime 的时间部分，0 = 00:00 */
    val timeOfDay: Int = 0,
    /**
     * 是否已退款。true 表示这笔流水已经回补到账户余额，
     * 因此所有统计聚合（月概览、饼图、账户详情月度流水）都会排除它，
     * 只有账单列表和详情页还能看到它。
     *
     * defaultValue 必须写：v2 -> v3 迁移用的是 ALTER TABLE ... DEFAULT 0，
     * Room 校验迁移时会比对列默认值，不声明就会报 "Migration didn't properly handle"。
     */
    @ColumnInfo(defaultValue = "0")
    val refunded: Boolean = false
)

/** 分类聚合结果（饼图数据） */
data class CategorySum(
    val categoryId: Long,
    val total: Double
)

/** 带关联名称的流水视图（UI 用） */
data class RecordView(
    val record: RecordEntity,
    val categoryName: String,
    val categoryIconKey: String,
    val categoryColor: Int,
    val accountName: String,
    val accountIconId: String,
    val accountColor: Int
)
