package com.glass.ledger.data

import com.glass.ledger.data.local.AppDatabase
import kotlinx.coroutines.flow.Flow

/**
 * 本地仓库：所有读写都走 Room（SQLite），不涉及任何后端。
 */
class LedgerRepository(private val db: AppDatabase) {

    private val accountDao = db.accountDao()
    private val categoryDao = db.categoryDao()
    private val recordDao = db.recordDao()

    // ---------- 账户 ----------
    fun observeAccounts(): Flow<List<AccountEntity>> = accountDao.observeAll()
    fun observeFundTotal(): Flow<Double> = accountDao.observeFundTotal()
    fun observeCreditTotal(): Flow<Double> = accountDao.observeCreditTotal()

    suspend fun addAccount(
        name: String,
        iconId: String,
        colorArgb: Int,
        type: AccountType,
        initialBalance: Double,
        creditLimit: Double?
    ) {
        val order = System.currentTimeMillis().toInt()
        accountDao.insert(
            AccountEntity(
                name = name,
                iconId = iconId,
                colorArgb = colorArgb,
                type = type,
                balance = if (type == AccountType.CREDIT) -initialBalance.coerceAtLeast(0.0) else initialBalance,
                creditLimit = creditLimit,
                sortOrder = order
            )
        )
    }

    suspend fun updateAccount(account: AccountEntity) = accountDao.update(account)

    suspend fun deleteAccount(account: AccountEntity) {
        // 级联删除该账户下的流水（本地数据，简单可靠）
        recordDao.getByAccount(account.id).forEach { r ->
            recordDao.deleteById(r.id)
        }
        accountDao.delete(account)
    }

    // ---------- 分类 ----------
    fun observeExpenseCategories(): Flow<List<CategoryEntity>> = categoryDao.observeExpenseCategories()
    fun observeIncomeCategories(): Flow<List<CategoryEntity>> = categoryDao.observeIncomeCategories()
    suspend fun getAllCategories(): List<CategoryEntity> = categoryDao.getAll()

    // ---------- 流水 ----------
    fun observeRecordsBetween(start: Long, end: Long): Flow<List<RecordEntity>> =
        recordDao.observeBetween(start, end)

    fun observeExpenseTotal(start: Long, end: Long): Flow<Double> =
        recordDao.observeExpenseTotal(start, end)

    fun observeIncomeTotal(start: Long, end: Long): Flow<Double> =
        recordDao.observeIncomeTotal(start, end)

    fun observeExpenseByCategory(start: Long, end: Long): Flow<List<CategorySum>> =
        recordDao.observeExpenseByCategory(start, end)

    /** 记一笔并同步账户余额 */
    suspend fun addRecord(record: RecordEntity) {
        val id = recordDao.insert(record)
        applyBalance(record.copy(id = id), delta = 1)
    }

    suspend fun deleteRecord(record: RecordEntity) {
        applyBalance(record, delta = -1)
        recordDao.deleteById(record.id)
    }

    suspend fun updateRecord(old: RecordEntity, new: RecordEntity) {
        applyBalance(old, delta = -1)
        recordDao.update(new)
        applyBalance(new, delta = 1)
    }

    private suspend fun applyBalance(record: RecordEntity, delta: Int) {
        val signed = when (record.type) {
            RecordType.EXPENSE -> -record.amount
            RecordType.INCOME -> record.amount
        } * delta
        accountDao.adjustBalance(record.accountId, signed)
    }
}
