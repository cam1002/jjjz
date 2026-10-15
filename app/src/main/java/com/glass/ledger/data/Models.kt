package com.glass.ledger.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/** 账户类型：资金账户（借记）/ 信用账户（透支） */
enum class AccountType { FUND, CREDIT }

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
    val dateEpochDay: Long
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
