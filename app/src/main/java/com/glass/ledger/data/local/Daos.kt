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

    @Query("SELECT COALESCE(SUM(balance), 0) FROM accounts WHERE type = 'FUND'")
    fun observeFundTotal(): Flow<Double>

    @Query("SELECT COALESCE(SUM(balance), 0) FROM accounts WHERE type = 'CREDIT'")
    fun observeCreditTotal(): Flow<Double>

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

    @Query("SELECT COALESCE(SUM(amount), 0) FROM records WHERE type = 'EXPENSE' AND dateEpochDay BETWEEN :start AND :end")
    fun observeExpenseTotal(start: Long, end: Long): Flow<Double>

    @Query("SELECT COALESCE(SUM(amount), 0) FROM records WHERE type = 'INCOME' AND dateEpochDay BETWEEN :start AND :end")
    fun observeIncomeTotal(start: Long, end: Long): Flow<Double>

    @Query(
        "SELECT categoryId, SUM(amount) AS total FROM records " +
            "WHERE type = 'EXPENSE' AND dateEpochDay BETWEEN :start AND :end " +
            "GROUP BY categoryId ORDER BY total DESC"
    )
    fun observeExpenseByCategory(start: Long, end: Long): Flow<List<CategorySum>>

    @Query("SELECT * FROM records WHERE accountId = :accountId")
    suspend fun getByAccount(accountId: Long): List<RecordEntity>

    @Insert
    suspend fun insert(record: RecordEntity): Long

    @Update
    suspend fun update(record: RecordEntity)

    @Query("DELETE FROM records WHERE id = :id")
    suspend fun deleteById(id: Long)
}
