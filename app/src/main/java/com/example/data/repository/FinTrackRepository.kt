package com.example.data.repository

import com.example.data.local.AssetEntity
import com.example.data.local.CategoryEntity
import com.example.data.local.ExpenseEntity
import com.example.data.local.FinTrackDao
import com.example.data.local.MilestoneEntity
import kotlinx.coroutines.flow.Flow
import org.json.JSONArray
import org.json.JSONObject

class FinTrackRepository(private val dao: FinTrackDao) {

    val allExpenses: Flow<List<ExpenseEntity>> = dao.getAllExpenses()
    val allAssets: Flow<List<AssetEntity>> = dao.getAllAssets()
    val allCategories: Flow<List<CategoryEntity>> = dao.getAllCategories()
    val allMilestones: Flow<List<MilestoneEntity>> = dao.getAllMilestones()

    fun getExpensesForRange(startDate: Long, endDate: Long): Flow<List<ExpenseEntity>> {
        return dao.getExpensesForRange(startDate, endDate)
    }

    suspend fun insertExpense(expense: ExpenseEntity): Long = dao.insertExpense(expense)

    suspend fun insertExpenses(expenses: List<ExpenseEntity>) = dao.insertExpenses(expenses)

    suspend fun updateExpense(expense: ExpenseEntity) = dao.updateExpense(expense)

    suspend fun deleteExpense(expense: ExpenseEntity) = dao.deleteExpense(expense)

    suspend fun deleteExpenseById(id: Long) = dao.deleteExpenseById(id)

    suspend fun insertAsset(asset: AssetEntity): Long = dao.insertAsset(asset)

    suspend fun updateAsset(asset: AssetEntity) = dao.updateAsset(asset)

    suspend fun deleteAsset(asset: AssetEntity) = dao.deleteAsset(asset)

    suspend fun insertCategory(category: CategoryEntity): Long = dao.insertCategory(category)

    suspend fun renameCategoryAndExpenses(oldName: String, newName: String, newColor: String) {
        dao.renameCategory(oldName, newName, newColor)
        dao.renameExpenseCategory(oldName, newName)
    }

    suspend fun renameCategoryAndExpensesByType(oldName: String, newName: String, newColor: String, type: String) {
        dao.renameCategoryByType(oldName, newName, newColor, type)
        dao.renameExpenseCategoryByType(oldName, newName, type)
    }

    suspend fun updateCategoryColor(name: String, newColor: String) {
        dao.updateCategoryColor(name, newColor)
    }

    suspend fun ensureCategoryExists(category: CategoryEntity) {
        val count = dao.countCategoryByNameAndType(category.name, category.type)
        if (count == 0) {
            dao.insertCategory(category)
        }
    }

    suspend fun insertMilestone(milestone: MilestoneEntity): Long = dao.insertMilestone(milestone)

    suspend fun deleteMilestone(milestone: MilestoneEntity) = dao.deleteMilestone(milestone)

    suspend fun exportToJson(expenses: List<ExpenseEntity>, assets: List<AssetEntity>): String {
        val root = JSONObject()
        root.put("version", 1)
        root.put("exportTimestamp", System.currentTimeMillis())

        val expensesArray = JSONArray()
        for (e in expenses) {
            val obj = JSONObject()
            obj.put("id", e.id)
            obj.put("amount", e.amount)
            obj.put("type", e.type)
            obj.put("category", e.category)
            obj.put("date", e.date)
            obj.put("notes", e.notes)
            obj.put("merchant", e.merchant)
            obj.put("isFromSMS", e.isFromSMS)
            expensesArray.put(obj)
        }
        root.put("expenses", expensesArray)

        val assetsArray = JSONArray()
        for (a in assets) {
            val obj = JSONObject()
            obj.put("id", a.id)
            obj.put("name", a.name)
            obj.put("assetType", a.assetType)
            obj.put("institution", a.institution)
            obj.put("value", a.value)
            if (a.investedAmount != null) {
                obj.put("investedAmount", a.investedAmount)
            }
            obj.put("isLiability", a.isLiability)
            obj.put("updatedAt", a.updatedAt)
            assetsArray.put(obj)
        }
        root.put("assets", assetsArray)

        return root.toString(2)
    }

    suspend fun exportToCsv(expenses: List<ExpenseEntity>): String {
        val sb = StringBuilder()
        sb.append("ID,Date,Type,Category,Amount,Merchant,Notes,IsFromSMS\n")
        val sdf = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.getDefault())
        for (e in expenses) {
            val dateStr = sdf.format(java.util.Date(e.date))
            val cleanNotes = e.notes.replace("\"", "\"\"")
            val cleanMerchant = e.merchant.replace("\"", "\"\"")
            sb.append("${e.id},\"${dateStr}\",${e.type},\"${e.category}\",${e.amount},\"${cleanMerchant}\",\"${cleanNotes}\",${e.isFromSMS}\n")
        }
        return sb.toString()
    }

    suspend fun importFromJson(jsonStr: String): Pair<Int, Int> {
        val root = JSONObject(jsonStr)
        var importedExpensesCount = 0
        var importedAssetsCount = 0

        if (root.has("expenses")) {
            val expensesArray = root.getJSONArray("expenses")
            val newExpenses = mutableListOf<ExpenseEntity>()
            for (i in 0 until expensesArray.length()) {
                val obj = expensesArray.getJSONObject(i)
                newExpenses.add(
                    ExpenseEntity(
                        id = 0, // Auto-generate new IDs on import to avoid conflict
                        amount = obj.optDouble("amount", 0.0),
                        type = obj.optString("type", "spent"),
                        category = obj.optString("category", "Other"),
                        date = obj.optLong("date", System.currentTimeMillis()),
                        notes = obj.optString("notes", ""),
                        merchant = obj.optString("merchant", ""),
                        isFromSMS = obj.optBoolean("isFromSMS", false)
                    )
                )
            }
            if (newExpenses.isNotEmpty()) {
                dao.insertExpenses(newExpenses)
                importedExpensesCount = newExpenses.size
            }
        }

        if (root.has("assets")) {
            val assetsArray = root.getJSONArray("assets")
            val newAssets = mutableListOf<AssetEntity>()
            for (i in 0 until assetsArray.length()) {
                val obj = assetsArray.getJSONObject(i)
                newAssets.add(
                    AssetEntity(
                        id = 0,
                        name = obj.optString("name", "Asset"),
                        assetType = obj.optString("assetType", "Bank Accounts"),
                        institution = obj.optString("institution", ""),
                        value = obj.optDouble("value", 0.0),
                        investedAmount = if (obj.has("investedAmount") && !obj.isNull("investedAmount")) obj.optDouble("investedAmount") else null,
                        isLiability = obj.optBoolean("isLiability", false),
                        updatedAt = obj.optLong("updatedAt", System.currentTimeMillis())
                    )
                )
            }
            if (newAssets.isNotEmpty()) {
                dao.insertAssets(newAssets)
                importedAssetsCount = newAssets.size
            }
        }

        return Pair(importedExpensesCount, importedAssetsCount)
    }

    suspend fun clearAllData() {
        dao.deleteAllExpenses()
        dao.deleteAllAssets()
    }
}
