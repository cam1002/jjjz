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
        Triple(AccountType.INVEST, "投资账户", "基金、股票等"),
        Triple(AccountType.DEBT, "债务账户", "借入、借出等")
    )
    fun label(type: AccountType) = all.first { it.first == type }.second
    fun desc(type: AccountType) = all.first { it.first == type }.third
}

/** 流水类型：支出 / 收入 */
enum class RecordType { EXPENSE, INCOME }

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
    /** 当前余额；信用账户为负数表示欠款 */
    val balance: Double,
    /** 信用账户额度，资金账户为 null */
    val creditLimit: Double? = null,
    val sortOrder: Int = 0
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
