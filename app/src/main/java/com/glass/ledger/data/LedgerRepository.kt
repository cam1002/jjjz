package com.glass.ledger.data

import androidx.room.withTransaction
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
    fun observeAssetTotal(): Flow<Double> = accountDao.observeAssetTotal()
    fun observeLiabilityTotal(): Flow<Double> = accountDao.observeLiabilityTotal()

    suspend fun addAccount(
        name: String,
        iconId: String,
        colorArgb: Int,
        type: AccountType,
        initialBalance: Double,
        creditLimit: Double?,
        note: String = ""
    ) {
        val order = System.currentTimeMillis().toInt()
        // 负债类记负数（欠别人的）；债务账户的「借出」记正数（别人欠我的 = 应收 = 资产），
        // 符号规则统一在 signedBalanceOf 里，修改账户弹窗走的是同一套。
        val signed = signedBalanceOf(type, iconId, initialBalance)
        accountDao.insert(
            AccountEntity(
                name = name,
                iconId = iconId,
                colorArgb = colorArgb,
                type = type,
                balance = signed,
                creditLimit = creditLimit,
                sortOrder = order,
                note = note.trim()
            )
        )
    }

    suspend fun updateAccount(account: AccountEntity) = accountDao.update(account)

    /**
     * 删除账户：**不连带删流水**，只把这些流水和该账户解绑（accountId 置 0）。
     * 账单是既成事实，删一个账户不该把历史消费记录一起抹掉；解绑后它们仍照常出现在记账页
     * 和统计里，账户一栏沿用原来的「已删除账户」。账户自己的余额随账户一起消失，
     * 不做任何回补（钱本来就不在别的账户里）。
     * 两步放同一个事务里，避免出现「解绑了但账户还在」的中间状态。
     */
    suspend fun deleteAccount(account: AccountEntity) {
        db.withTransaction {
            recordDao.detachAccount(account.id)
            accountDao.delete(account)
        }
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

    /** 某账户的全部流水（详情页聚合用） */
    fun observeRecordsByAccount(accountId: Long): Flow<List<RecordEntity>> =
        recordDao.observeByAccount(accountId)

    /** 单条流水（账单详情页用；被删除后流出发 null） */
    fun observeRecord(id: Long): Flow<RecordEntity?> = recordDao.observeById(id)

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

    /**
     * 退款：给这笔流水打上「已退款」标记，并把金额原路回补到它的账户
     * （支出退回、收入扣回）。同一步里做两件事，用事务保证不会只成功一半。
     *
     * 标记后这条流水不再计入统计聚合（月概览 / 饼图 / 账户详情月度流入流出），
     * 因为钱已经回到余额里了，再算一次支出就会和余额对不上。
     */
    suspend fun refundRecord(record: RecordEntity) {
        if (record.refunded) return
        db.withTransaction {
            val back = if (record.type == RecordType.EXPENSE) record.amount else -record.amount
            accountDao.adjustBalance(record.accountId, back)
            recordDao.update(record.copy(refunded = true))
        }
    }

    private suspend fun applyBalance(record: RecordEntity, delta: Int) {
        // 已退款的流水：当初的 ±金额已经被 refundRecord 回补过，净额为 0。
        // 所以修改或删除它都不再动余额，否则会把退款的那笔钱重复算一遍。
        if (record.refunded) return
        val signed = when (record.type) {
            RecordType.EXPENSE -> -record.amount
            RecordType.INCOME -> record.amount
        } * delta
        accountDao.adjustBalance(record.accountId, signed)
    }
}
