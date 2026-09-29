package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Calendar

@Database(
    entities = [
        ExpenseEntity::class,
        AssetEntity::class,
        CategoryEntity::class,
        MilestoneEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class FinTrackDatabase : RoomDatabase() {
    abstract fun finTrackDao(): FinTrackDao

    companion object {
        @Volatile
        private var INSTANCE: FinTrackDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): FinTrackDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    FinTrackDatabase::class.java,
                    "fintrack_database"
                )
                    .addCallback(FinTrackDatabaseCallback(scope))
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class FinTrackDatabaseCallback(
        private val scope: CoroutineScope
    ) : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            scope.launch(Dispatchers.IO) {
                INSTANCE?.let { database ->
                    populateInitialData(database.finTrackDao())
                }
            }
        }

        private suspend fun populateInitialData(dao: FinTrackDao) {
            val defaultCategories = listOf(
                CategoryEntity(name = "Food", type = "spent", colorHex = "#EF4444", iconName = "restaurant"),
                CategoryEntity(name = "Grocery", type = "spent", colorHex = "#EA580C", iconName = "shopping_basket"),
                CategoryEntity(name = "Shopping", type = "spent", colorHex = "#EC4899", iconName = "shopping_bag"),
                CategoryEntity(name = "Bills", type = "spent", colorHex = "#2563EB", iconName = "receipt_long"),
                CategoryEntity(name = "Medical", type = "spent", colorHex = "#059669", iconName = "medical_services"),
                CategoryEntity(name = "Entertainment", type = "spent", colorHex = "#8B5CF6", iconName = "movie"),
                CategoryEntity(name = "Investment", type = "spent", colorHex = "#6366F1", iconName = "trending_up"),
                CategoryEntity(name = "Travel", type = "spent", colorHex = "#0284C7", iconName = "directions_car"),
                CategoryEntity(name = "Rent", type = "spent", colorHex = "#D97706", iconName = "home"),
                CategoryEntity(name = "Education", type = "spent", colorHex = "#0D9488", iconName = "school"),
                CategoryEntity(name = "Other", type = "spent", colorHex = "#64748B", iconName = "more_horiz"),
                CategoryEntity(name = "Salary", type = "earned", colorHex = "#0284C7", iconName = "payments"),
                CategoryEntity(name = "Freelance", type = "earned", colorHex = "#8B5CF6", iconName = "work"),
                CategoryEntity(name = "Investment Return", type = "earned", colorHex = "#6366F1", iconName = "show_chart"),
                CategoryEntity(name = "Dividend", type = "earned", colorHex = "#10B981", iconName = "show_chart"),
                CategoryEntity(name = "Cashback and Rewards", type = "earned", colorHex = "#EC4899", iconName = "redeem"),
                CategoryEntity(name = "Gifts", type = "earned", colorHex = "#F43F5E", iconName = "redeem"),
                CategoryEntity(name = "Refund", type = "earned", colorHex = "#F97316", iconName = "receipt_long"),
                CategoryEntity(name = "Other", type = "earned", colorHex = "#64748B", iconName = "account_balance_wallet")
            )
            dao.insertCategories(defaultCategories)

            // Initial starter holdings for portfolio demonstration
            val initialAssets = listOf(
                AssetEntity(name = "HDFC Savings Account", assetType = "Bank Accounts", institution = "HDFC Bank", value = 145000.0, investedAmount = 145000.0, isLiability = false),
                AssetEntity(name = "SBI Emergency Fund", assetType = "Bank Accounts", institution = "State Bank of India", value = 250000.0, investedAmount = 250000.0, isLiability = false),
                AssetEntity(name = "Nifty 50 Index Fund", assetType = "Mutual Funds & Equities", institution = "Zerodha Coin", value = 620000.0, investedAmount = 480000.0, isLiability = false),
                AssetEntity(name = "Parag Parikh Flexi Cap", assetType = "Mutual Funds & Equities", institution = "Groww", value = 480000.0, investedAmount = 390000.0, isLiability = false),
                AssetEntity(name = "Sovereign Gold Bonds (SGB)", assetType = "Gold & SGBs", institution = "RBI / Zerodha", value = 320000.0, investedAmount = 250000.0, isLiability = false),
                AssetEntity(name = "Employees' Provident Fund (EPF)", assetType = "Retirement & PF", institution = "EPFO", value = 540000.0, investedAmount = 460000.0, isLiability = false),
                AssetEntity(name = "Fixed Deposit (7.25%)", assetType = "Fixed Deposits", institution = "ICICI Bank", value = 300000.0, investedAmount = 280000.0, isLiability = false),
                AssetEntity(name = "Cash in Hand", assetType = "Cash in Hand", institution = "Physical", value = 18500.0, investedAmount = 18500.0, isLiability = false),
                AssetEntity(name = "Car Loan Balance", assetType = "Liabilities & Loans", institution = "HDFC Bank", value = 280000.0, isLiability = true),
                AssetEntity(name = "Credit Card Bill", assetType = "Liabilities & Loans", institution = "Axis Bank", value = 32500.0, isLiability = true)
            )
            dao.insertAssets(initialAssets)

            // Initial milestones
            dao.insertMilestone(MilestoneEntity(title = "First ₹10 Lakhs Milestone", netWorth = 1000000.0, timestamp = System.currentTimeMillis() - 180L * 86400000L, note = "Hit 7-figure net worth!"))
            dao.insertMilestone(MilestoneEntity(title = "₹20 Lakhs Portfolio", netWorth = 2000000.0, timestamp = System.currentTimeMillis() - 60L * 86400000L, note = "Equity investments crossed ₹10L"))

            // Sample transactions for current month
            val cal = Calendar.getInstance()
            val now = cal.timeInMillis
            val sampleTransactions = listOf(
                ExpenseEntity(amount = 1250.0, type = "spent", category = "Food", date = now - 2 * 3600000L, notes = "Dinner at Olive Bistro", merchant = "Olive Bistro", isFromSMS = false),
                ExpenseEntity(amount = 640.0, type = "spent", category = "Grocery", date = now - 6 * 3600000L, notes = "Weekly vegetables & milk", merchant = "Blinkit", isFromSMS = true),
                ExpenseEntity(amount = 280.0, type = "spent", category = "Travel", date = now - 28 * 3600000L, notes = "Cab to office", merchant = "Uber", isFromSMS = true),
                ExpenseEntity(amount = 2499.0, type = "spent", category = "Shopping", date = now - 48 * 3600000L, notes = "Running shoes", merchant = "Amazon", isFromSMS = true),
                ExpenseEntity(amount = 1499.0, type = "spent", category = "Bills", date = now - 72 * 3600000L, notes = "Fiber broadband bill", merchant = "Airtel Xstream", isFromSMS = true),
                ExpenseEntity(amount = 120000.0, type = "earned", category = "Salary", date = now - 96 * 3600000L, notes = "Monthly salary credited", merchant = "Tech Employer", isFromSMS = true),
                ExpenseEntity(amount = 450.0, type = "spent", category = "Entertainment", date = now - 120 * 3600000L, notes = "Movie tickets", merchant = "PVR Cinemas", isFromSMS = false),
                ExpenseEntity(amount = 15000.0, type = "spent", category = "Investment", date = now - 140 * 3600000L, notes = "Monthly Mutual Fund SIP", merchant = "Zerodha Coin", isFromSMS = true),
                ExpenseEntity(amount = 12500.0, type = "earned", category = "Freelance", date = now - 170 * 3600000L, notes = "UI/UX Consultation fee", merchant = "Client Corp", isFromSMS = false)
            )
            dao.insertExpenses(sampleTransactions)
        }
    }
}
