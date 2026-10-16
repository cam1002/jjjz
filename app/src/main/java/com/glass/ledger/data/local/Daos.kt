package com.glass.ledger.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.glass.ledger.data.AccountEntity
import com.glass.ledger.data.CategoryEntity
import com.glass.ledger.data.CategorySum
import com.glass.ledger.data.RecordEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AccountDao {
    @Query("SELECT * FROM accounts ORDER BY sortOrder ASC, id ASC")
    fun observeAll(): Flow<List<AccountEntity>>

    /**
     * 资产总额 = 资金 + 充值 + 投资 + **负债类里余额为正的那部分**。
     * 债务账户的「借出」（别人欠我的）存的是正数，那是应收、是资产，不能算负债；
     * 信用卡偶尔会存出正余额（多还的溢缴款），同理。
     * 这两个查询的条件互斥且覆盖所有账户，所以
     * 「资产总额 + 负债总额 = 所有账户余额之和」这条恒等式不会破（净资产页就靠它）。
     */
    @Query("SELECT COALESCE(SUM(balance), 0) FROM accounts WHERE type IN ('FUND','RECHARGE','INVEST') OR balance > 0")
    fun observeAssetTotal(): Flow<Double>

    /** 负债总额（负数）= 信用 + 债务里余额为负的那部分（信用卡欠款、借入） */
    @Query("SELECT COALESCE(SUM(balance), 0) FROM accounts WHERE type IN ('CREDIT','DEBT') AND balance < 0")
    fun observeLiabilityTotal(): Flow<Double>

    @Insert
    suspend fun insert(account: AccountEntity): Long

    @Update
    suspend fun update(account: AccountEntity)

    @Delete
    suspend fun delete(account: AccountEntity)

    @Query("SELECT * FROM accounts WHERE id = :id")
    suspend fun getById(id: Long): AccountEntity?

    @Query("UPDATE accounts SET balance = balance + :delta WHERE id = :id")
    suspend fun adjustBalance(id: Long, delta: Double)
}

@Dao
interface CategoryDao {
    @Query("SELECT * FROM categories WHERE isExpense = 1 ORDER BY sortOrder ASC")
    fun observeExpenseCategories(): Flow<List<CategoryEntity>>

    @Query("SELECT * FROM categories WHERE isExpense = 0 ORDER BY sortOrder ASC")
    fun observeIncomeCategories(): Flow<List<CategoryEntity>>

    @Query("SELECT * FROM categories")
    suspend fun getAll(): List<CategoryEntity>

    @Insert
    suspend fun insert(category: CategoryEntity): Long
}

@Dao
interface RecordDao {
    @Query(
        "SELECT * FROM records WHERE dateEpochDay BETWEEN :start AND :end " +
            "ORDER BY dateEpochDay DESC, id DESC"
    )
    fun observeBetween(start: Long, end: Long): Flow<List<RecordEntity>>

    /** 已退款（refunded = 1）的流水不计入统计：它的金额已经回补进账户余额 */
    @Query("SELECT COALESCE(SUM(amount), 0) FROM records WHERE type = 'EXPENSE' AND refunded = 0 AND dateEpochDay BETWEEN :start AND :end")
    fun observeExpenseTotal(start: Long, end: Long): Flow<Double>

    @Query("SELECT COALESCE(SUM(amount), 0) FROM records WHERE type = 'INCOME' AND refunded = 0 AND dateEpochDay BETWEEN :start AND :end")
    fun observeIncomeTotal(start: Long, end: Long): Flow<Double>

    @Query(
        "SELECT categoryId, SUM(amount) AS total FROM records " +
            "WHERE type = 'EXPENSE' AND refunded = 0 AND dateEpochDay BETWEEN :start AND :end " +
            "GROUP BY categoryId ORDER BY total DESC"
    )
    fun observeExpenseByCategory(start: Long, end: Long): Flow<List<CategorySum>>

    /**
     * 删除账户时把该账户下的流水「解绑」：accountId 置 0，流水本身保留。
     * records 表没有对 accounts 的外键约束，所以 0 这个不存在的 id 是合法值，
     * UI 侧取不到账户时沿用 MainViewModel.toView 里原来的「已删除账户」兜底名。
     */
    @Query("UPDATE records SET accountId = 0 WHERE accountId = :accountId")
    suspend fun detachAccount(accountId: Long)

    /** 账单详情页：观察单条流水，编辑/退款/删除后界面自动刷新 */
    @Query("SELECT * FROM records WHERE id = :id")
    fun observeById(id: Long): Flow<RecordEntity?>

    /** 账户详情聚合用：同样排除已退款流水，保证月末余额推导与实际余额一致 */
    @Query("SELECT * FROM records WHERE accountId = :accountId AND refunded = 0 ORDER BY dateEpochDay ASC, id ASC")
    fun observeByAccount(accountId: Long): Flow<List<RecordEntity>>

    @Insert
    suspend fun insert(record: RecordEntity): Long

    @Update
    suspend fun update(record: RecordEntity)

    @Query("DELETE FROM records WHERE id = :id")
    suspend fun deleteById(id: Long)
}
