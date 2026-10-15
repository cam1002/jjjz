package com.glass.ledger.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.glass.ledger.data.AccountEntity
import com.glass.ledger.data.CategoryEntity
import com.glass.ledger.data.RecordEntity

@Database(
    entities = [AccountEntity::class, CategoryEntity::class, RecordEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun accountDao(): AccountDao
    abstract fun categoryDao(): CategoryDao
    abstract fun recordDao(): RecordDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun get(context: Context): AppDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: build(context).also { INSTANCE = it }
            }

        private fun build(context: Context): AppDatabase =
            Room.databaseBuilder(context.applicationContext, AppDatabase::class.java, "glass_ledger.db")
                .addCallback(SeedCallback)
                .build()

        /** 首次建库时写入默认分类与账户，数据全部保存在本地 */
        private val SeedCallback = object : Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                // 支出分类
                val expense = listOf(
                    Triple("餐饮", "restaurant", 0xFFFF7043.toInt()),
                    Triple("交通", "bus", 0xFF42A5F5.toInt()),
                    Triple("购物", "shopping", 0xFFEC407A.toInt()),
                    Triple("休闲娱乐", "theater", 0xFF7E57C2.toInt()),
                    Triple("居住", "home", 0xFF26A69A.toInt()),
                    Triple("医疗", "medical", 0xFFEF5350.toInt()),
                    Triple("教育", "education", 0xFF5C6BC0.toInt()),
                    Triple("通讯", "phone", 0xFF29B6F6.toInt()),
                    Triple("人情", "gift", 0xFFFFCA28.toInt()),
                    Triple("其他", "more", 0xFF78909C.toInt())
                )
                expense.forEachIndexed { i, (name, icon, color) ->
                    db.execSQL(
                        "INSERT INTO categories (name, iconKey, colorArgb, isExpense, sortOrder) VALUES (?,?,?,?,?)",
                        arrayOf(name, icon, color, 1, i)
                    )
                }
                // 收入分类
                val income = listOf(
                    Triple("工资", "salary", 0xFF66BB6A.toInt()),
                    Triple("奖金", "bonus", 0xFFFFA726.toInt()),
                    Triple("理财", "invest", 0xFF26C6DA.toInt()),
                    Triple("其他收入", "more", 0xFF9CCC65.toInt())
                )
                income.forEachIndexed { i, (name, icon, color) ->
                    db.execSQL(
                        "INSERT INTO categories (name, iconKey, colorArgb, isExpense, sortOrder) VALUES (?,?,?,?,?)",
                        arrayOf(name, icon, color, 0, i)
                    )
                }
                // 默认账户
                db.execSQL(
                    "INSERT INTO accounts (name, iconId, colorArgb, type, balance, creditLimit, sortOrder) VALUES (?,?,?,?,?,?,?)",
                    arrayOf("微信", "wechat", 0xFF2AAE67.toInt(), "FUND", 0.0, null as Double?, 0)
                )
                db.execSQL(
                    "INSERT INTO accounts (name, iconId, colorArgb, type, balance, creditLimit, sortOrder) VALUES (?,?,?,?,?,?,?)",
                    arrayOf("支付宝", "alipay", 0xFF1677FF.toInt(), "FUND", 0.0, null as Double?, 1)
                )
                db.execSQL(
                    "INSERT INTO accounts (name, iconId, colorArgb, type, balance, creditLimit, sortOrder) VALUES (?,?,?,?,?,?,?)",
                    arrayOf("现金", "cash", 0xFF34A853.toInt(), "FUND", 0.0, null as Double?, 2)
                )
            }
        }
    }
}
