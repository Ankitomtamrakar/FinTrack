package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface FinTrackDao {
    // --- Expenses ---
    @Query("SELECT * FROM expenses ORDER BY date DESC, id DESC")
    fun getAllExpenses(): Flow<List<ExpenseEntity>>

    @Query("SELECT * FROM expenses WHERE date >= :startDate AND date <= :endDate ORDER BY date DESC, id DESC")
    fun getExpensesForRange(startDate: Long, endDate: Long): Flow<List<ExpenseEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpense(expense: ExpenseEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpenses(expenses: List<ExpenseEntity>)

    @Update
    suspend fun updateExpense(expense: ExpenseEntity)

    @Delete
    suspend fun deleteExpense(expense: ExpenseEntity)

    @Query("DELETE FROM expenses WHERE id = :id")
    suspend fun deleteExpenseById(id: Long)

    @Query("DELETE FROM expenses")
    suspend fun deleteAllExpenses()

    // --- Assets ---
    @Query("SELECT * FROM assets ORDER BY isLiability ASC, value DESC")
    fun getAllAssets(): Flow<List<AssetEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAsset(asset: AssetEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAssets(assets: List<AssetEntity>)

    @Update
    suspend fun updateAsset(asset: AssetEntity)

    @Delete
    suspend fun deleteAsset(asset: AssetEntity)

    @Query("DELETE FROM assets WHERE id = :id")
    suspend fun deleteAssetById(id: Long)

    @Query("DELETE FROM assets")
    suspend fun deleteAllAssets()

    // --- Categories ---
    @Query("SELECT * FROM categories ORDER BY id ASC")
    fun getAllCategories(): Flow<List<CategoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategory(category: CategoryEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategories(categories: List<CategoryEntity>)

    @Delete
    suspend fun deleteCategory(category: CategoryEntity)

    @Query("UPDATE categories SET name = :newName, colorHex = :newColor WHERE name = :oldName")
    suspend fun renameCategory(oldName: String, newName: String, newColor: String)

    @Query("UPDATE categories SET name = :newName, colorHex = :newColor WHERE name = :oldName AND type = :type")
    suspend fun renameCategoryByType(oldName: String, newName: String, newColor: String, type: String)

    @Query("UPDATE expenses SET category = :newName WHERE category = :oldName")
    suspend fun renameExpenseCategory(oldName: String, newName: String)

    @Query("UPDATE expenses SET category = :newName WHERE category = :oldName AND type = :type")
    suspend fun renameExpenseCategoryByType(oldName: String, newName: String, type: String)

    @Query("UPDATE categories SET colorHex = :newColor WHERE name = :name")
    suspend fun updateCategoryColor(name: String, newColor: String)

    @Query("SELECT COUNT(*) FROM categories WHERE name = :name")
    suspend fun countCategoryByName(name: String): Int

    @Query("SELECT COUNT(*) FROM categories WHERE name = :name AND type = :type")
    suspend fun countCategoryByNameAndType(name: String, type: String): Int

    // --- Milestones ---
    @Query("SELECT * FROM milestones ORDER BY timestamp DESC")
    fun getAllMilestones(): Flow<List<MilestoneEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMilestone(milestone: MilestoneEntity): Long

    @Delete
    suspend fun deleteMilestone(milestone: MilestoneEntity)
}
