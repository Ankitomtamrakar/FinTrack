package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AssetEntity
import com.example.data.local.CategoryEntity
import com.example.data.local.ExpenseEntity
import com.example.data.local.FinTrackDatabase
import com.example.data.local.MilestoneEntity
import com.example.data.repository.FinTrackRepository
import com.example.ui.components.PieSliceData
import com.example.util.CategoryHelper
import com.example.util.DateUtils
import com.example.util.NetWorthPoint
import com.example.util.NetWorthTimeframe
import com.example.util.NetWorthTimelineHelper
import com.example.util.ParsedSmsTransaction
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar

data class SpendsMonthMetrics(
    val totalSpent: Double = 0.0,
    val totalEarned: Double = 0.0,
    val netBalance: Double = 0.0
)

data class WealthMetrics(
    val totalNetWorth: Double = 0.0,
    val totalAssets: Double = 0.0,
    val totalLiabilities: Double = 0.0,
    val totalInvested: Double = 0.0,
    val totalReturnsPercentage: Double = 0.0,
    val netWorthGrowthRate: Double = 4.2 // Historic estimate percentage
)

class FinTrackViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: FinTrackRepository

    init {
        val db = FinTrackDatabase.getDatabase(application, viewModelScope)
        repository = FinTrackRepository(db.finTrackDao())

        // Ensure category names are updated to the clean, shorter names across DB
        viewModelScope.launch(Dispatchers.IO) {
            val spendMigrations = listOf(
                Triple("Food & Dining", "Food", "#EF4444"),
                Triple("Grocery & Mart", "Grocery", "#EA580C"),
                Triple("Bills & Utilities", "Bills", "#2563EB"),
                Triple("Travel & Fuel", "Travel", "#0284C7"),
                Triple("Health & Medical", "Medical", "#059669"),
                Triple("Investments", "Investment", "#6366F1"),
                Triple("Investment Return", "Investment", "#6366F1"),
                Triple("Investment Returns", "Investment", "#6366F1"),
                Triple("Other Expense", "Other", "#64748B")
            )
            for ((oldName, newName, color) in spendMigrations) {
                repository.renameCategoryAndExpensesByType(oldName, newName, color, "spent")
            }

            // Rename earned categories to shorter names with vibrant non-green colors
            val earnedMigrations = listOf(
                Triple("Freelance & Consulting", "Freelance", "#8B5CF6"),
                Triple("Investment Returns", "Investment Return", "#6366F1"),
                Triple("Investments", "Investment Return", "#6366F1"),
                Triple("Investment", "Investment Return", "#6366F1"),
                Triple("Cashback & Rewards", "Cashback and Rewards", "#EC4899"),
                Triple("Rewards", "Cashback and Rewards", "#EC4899"),
                Triple("Other Income", "Other", "#64748B")
            )
            for ((oldName, newName, color) in earnedMigrations) {
                repository.renameCategoryAndExpensesByType(oldName, newName, color, "earned")
            }

            // Update default colors for existing earned categories away from green
            repository.updateCategoryColor("Salary", "#0284C7")
            repository.updateCategoryColor("Freelance", "#8B5CF6")
            repository.updateCategoryColor("Investment Return", "#6366F1")
            repository.updateCategoryColor("Cashback and Rewards", "#EC4899")
            repository.updateCategoryColor("Other", "#64748B")

            // Ensure ALL standard default categories exist in DB
            val allDefaultCategories = listOf(
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
            for (cat in allDefaultCategories) {
                repository.ensureCategoryExists(cat)
            }
        }
    }

    private val calendar = Calendar.getInstance()

    private val _activeYear = MutableStateFlow(calendar.get(Calendar.YEAR))
    val activeYear: StateFlow<Int> = _activeYear.asStateFlow()

    private val _activeMonth = MutableStateFlow(calendar.get(Calendar.MONTH))
    val activeMonth: StateFlow<Int> = _activeMonth.asStateFlow()

    private val _selectedDate = MutableStateFlow<Long?>(null)
    val selectedDate: StateFlow<Long?> = _selectedDate.asStateFlow()

    private val _selectedCategoryFilter = MutableStateFlow<String?>(null)
    val selectedCategoryFilter: StateFlow<String?> = _selectedCategoryFilter.asStateFlow()

    private val _selectedTypeFilter = MutableStateFlow<String?>(null)
    val selectedTypeFilter: StateFlow<String?> = _selectedTypeFilter.asStateFlow()

    val allExpenses: StateFlow<List<ExpenseEntity>> = repository.allExpenses
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allAssets: StateFlow<List<AssetEntity>> = repository.allAssets
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allCategories: StateFlow<List<CategoryEntity>> = repository.allCategories
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allMilestones: StateFlow<List<MilestoneEntity>> = repository.allMilestones
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Active Month's Expenses
    val activeMonthExpenses: StateFlow<List<ExpenseEntity>> = combine(
        allExpenses,
        _activeYear,
        _activeMonth
    ) { expenses, year, month ->
        val start = DateUtils.getStartOfMonth(year, month)
        val end = DateUtils.getEndOfMonth(year, month)
        expenses.filter { it.date in start..end }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Metrics for Active Month
    val monthMetrics: StateFlow<SpendsMonthMetrics> = activeMonthExpenses.combine(_activeMonth) { expenses, _ ->
        var spent = 0.0
        var earned = 0.0
        for (e in expenses) {
            if (e.type == "spent") {
                spent += e.amount
            } else {
                earned += e.amount
            }
        }
        SpendsMonthMetrics(
            totalSpent = spent,
            totalEarned = earned,
            netBalance = earned - spent
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SpendsMonthMetrics())

    // Daily Net Map for Calendar Matrix (startOfDay -> earned - spent)
    val dailyNetMap: StateFlow<Map<Long, Double>> = activeMonthExpenses.combine(_activeMonth) { expenses, _ ->
        val map = mutableMapOf<Long, Double>()
        for (e in expenses) {
            val sod = DateUtils.getStartOfDay(e.date)
            val delta = if (e.type == "earned") e.amount else -e.amount
            map[sod] = (map[sod] ?: 0.0) + delta
        }
        map
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    val dailyHasTransactions: StateFlow<Set<Long>> = activeMonthExpenses.combine(_activeMonth) { expenses, _ ->
        expenses.map { DateUtils.getStartOfDay(it.date) }.toSet()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptySet())

    // Pie Slices for Visual Analytics Donut
    val categorySlices: StateFlow<List<PieSliceData>> = activeMonthExpenses.combine(_activeMonth) { expenses, _ ->
        val spentExpenses = expenses.filter { it.type == "spent" }
        val totalSpent = spentExpenses.sumOf { it.amount }
        if (totalSpent <= 0.0) {
            emptyList()
        } else {
            spentExpenses.groupBy { it.category }
                .map { (catName, items) ->
                    val sum = items.sumOf { it.amount }
                    val pct = ((sum / totalSpent) * 100.0).toFloat()
                    val color = CategoryHelper.getCategoryColor(catName)
                    PieSliceData(
                        label = catName,
                        value = sum,
                        percentage = pct,
                        color = color
                    )
                }
                .sortedByDescending { it.value }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Filtered transaction list for display
    val displayedTransactions: StateFlow<List<ExpenseEntity>> = combine(
        activeMonthExpenses,
        _selectedCategoryFilter,
        _selectedTypeFilter
    ) { expenses, categoryFilter, typeFilter ->
        var list = expenses
        if (!typeFilter.isNullOrBlank()) {
            list = list.filter { it.type.equals(typeFilter, ignoreCase = true) }
        }
        if (!categoryFilter.isNullOrBlank()) {
            list = list.filter { it.category.equals(categoryFilter, ignoreCase = true) }
        }
        list
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Selected Day's Transactions
    val selectedDayTransactions: StateFlow<List<ExpenseEntity>> = combine(
        allExpenses,
        _selectedDate
    ) { expenses, date ->
        if (date == null) {
            emptyList()
        } else {
            expenses.filter { DateUtils.isSameDay(it.date, date) }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Wealth Metrics
    val wealthMetrics: StateFlow<WealthMetrics> = allAssets.combine(_activeYear) { assets, _ ->
        var totalAssets = 0.0
        var totalLiabilities = 0.0
        var totalInvested = 0.0
        var totalInvestedAssetVal = 0.0
        for (a in assets) {
            if (a.isLiability) {
                totalLiabilities += a.value
            } else {
                totalAssets += a.value
                if (a.investedAmount != null && a.investedAmount > 0.0) {
                    totalInvested += a.investedAmount
                    totalInvestedAssetVal += a.value
                }
            }
        }
        val returnPct = if (totalInvested > 0.0) {
            ((totalInvestedAssetVal - totalInvested) / totalInvested) * 100.0
        } else {
            0.0
        }
        WealthMetrics(
            totalNetWorth = totalAssets - totalLiabilities,
            totalAssets = totalAssets,
            totalLiabilities = totalLiabilities,
            totalInvested = totalInvested,
            totalReturnsPercentage = returnPct
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), WealthMetrics())

    // Net Worth Growth Timeline & Timeframe
    private val _selectedWealthTimeframe = MutableStateFlow(NetWorthTimeframe.SIX_MONTHS)
    val selectedWealthTimeframe: StateFlow<NetWorthTimeframe> = _selectedWealthTimeframe.asStateFlow()

    val netWorthTimeline: StateFlow<List<NetWorthPoint>> = combine(
        wealthMetrics,
        allAssets,
        allExpenses,
        allMilestones,
        _selectedWealthTimeframe
    ) { metrics, assets, expenses, milestones, timeframe ->
        NetWorthTimelineHelper.generateTimeline(
            currentNetWorth = metrics.totalNetWorth,
            assets = assets,
            expenses = expenses,
            milestones = milestones,
            timeframe = timeframe
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun selectWealthTimeframe(timeframe: NetWorthTimeframe) {
        _selectedWealthTimeframe.value = timeframe
    }

    // Month Navigation
    fun prevMonth() {
        if (_activeMonth.value == 0) {
            _activeMonth.value = 11
            _activeYear.value -= 1
        } else {
            _activeMonth.value -= 1
        }
    }

    fun nextMonth() {
        if (_activeMonth.value == 11) {
            _activeMonth.value = 0
            _activeYear.value += 1
        } else {
            _activeMonth.value += 1
        }
    }

    fun jumpToToday() {
        val c = Calendar.getInstance()
        _activeYear.value = c.get(Calendar.YEAR)
        _activeMonth.value = c.get(Calendar.MONTH)
        _selectedDate.value = c.timeInMillis
    }

    fun selectDate(date: Long?) {
        _selectedDate.value = date
    }

    fun selectCategoryFilter(category: String?) {
        _selectedCategoryFilter.value = category
        if (category != null) {
            _selectedTypeFilter.value = null
        }
    }

    fun selectTypeFilter(type: String?) {
        if (_selectedTypeFilter.value.equals(type, ignoreCase = true)) {
            _selectedTypeFilter.value = null
        } else {
            _selectedTypeFilter.value = type
            _selectedCategoryFilter.value = null
        }
    }

    // Transactions CRUD
    fun addExpense(
        amount: Double,
        type: String,
        category: String,
        date: Long,
        merchant: String,
        notes: String,
        isFromSMS: Boolean = false
    ) {
        viewModelScope.launch {
            repository.insertExpense(
                ExpenseEntity(
                    amount = amount,
                    type = type,
                    category = category,
                    date = date,
                    merchant = merchant,
                    notes = notes,
                    isFromSMS = isFromSMS
                )
            )
        }
    }

    fun updateExpense(expense: ExpenseEntity) {
        viewModelScope.launch {
            repository.updateExpense(expense)
        }
    }

    fun deleteExpense(expense: ExpenseEntity) {
        viewModelScope.launch {
            repository.deleteExpense(expense)
        }
    }

    // Batch Import from Parsed SMS
    fun importSmsTransactions(items: List<ParsedSmsTransaction>, onComplete: (Int) -> Unit) {
        viewModelScope.launch {
            val entities = items.filter { it.isSelected }.map {
                val remark = if (it.merchant.isNotBlank() &&
                    it.merchant != "Merchant / Store" &&
                    it.merchant != "Bank / Remitter"
                ) {
                    it.merchant
                } else {
                    it.category.ifBlank { "Bank SMS" }
                }
                ExpenseEntity(
                    amount = it.amount,
                    type = it.type,
                    category = it.category,
                    date = it.date,
                    merchant = it.merchant,
                    notes = remark,
                    isFromSMS = true
                )
            }
            if (entities.isNotEmpty()) {
                repository.insertExpenses(entities)
            }
            onComplete(entities.size)
        }
    }

    // Assets CRUD
    fun addAsset(
        name: String,
        assetType: String,
        value: Double,
        investedAmount: Double? = null,
        isLiability: Boolean,
        institution: String = ""
    ) {
        viewModelScope.launch {
            repository.insertAsset(
                AssetEntity(
                    name = name,
                    assetType = assetType,
                    institution = institution,
                    value = value,
                    investedAmount = investedAmount,
                    isLiability = isLiability,
                    updatedAt = System.currentTimeMillis()
                )
            )
        }
    }

    fun updateAsset(asset: AssetEntity) {
        viewModelScope.launch {
            repository.updateAsset(asset.copy(updatedAt = System.currentTimeMillis()))
        }
    }

    fun updateAssetValuation(asset: AssetEntity, newValue: Double) {
        viewModelScope.launch {
            repository.updateAsset(
                asset.copy(
                    value = newValue,
                    updatedAt = System.currentTimeMillis()
                )
            )
        }
    }

    fun deleteAsset(asset: AssetEntity) {
        viewModelScope.launch {
            repository.deleteAsset(asset)
        }
    }

    // Milestones CRUD
    fun addMilestone(title: String, netWorth: Double, note: String) {
        viewModelScope.launch {
            repository.insertMilestone(
                MilestoneEntity(
                    title = title,
                    netWorth = netWorth,
                    note = note,
                    timestamp = System.currentTimeMillis()
                )
            )
        }
    }

    fun deleteMilestone(milestone: MilestoneEntity) {
        viewModelScope.launch {
            repository.deleteMilestone(milestone)
        }
    }

    // Custom Category
    fun addCustomCategory(name: String, type: String, colorHex: String, iconName: String) {
        viewModelScope.launch {
            repository.insertCategory(
                CategoryEntity(
                    name = name,
                    type = type,
                    colorHex = colorHex,
                    iconName = iconName
                )
            )
        }
    }

    // Backup & Restore
    fun exportBackup(onExportReady: (jsonString: String, csvString: String) -> Unit) {
        viewModelScope.launch {
            val expenses = allExpenses.value
            val assets = allAssets.value
            val json = repository.exportToJson(expenses, assets)
            val csv = repository.exportToCsv(expenses)
            onExportReady(json, csv)
        }
    }

    fun importBackup(jsonString: String, onComplete: (Pair<Int, Int>?, String?) -> Unit) {
        viewModelScope.launch {
            try {
                val counts = repository.importFromJson(jsonString)
                onComplete(counts, null)
            } catch (e: Exception) {
                onComplete(null, e.localizedMessage ?: "Invalid JSON format")
            }
        }
    }

    fun clearAllData() {
        viewModelScope.launch {
            repository.clearAllData()
        }
    }
}
