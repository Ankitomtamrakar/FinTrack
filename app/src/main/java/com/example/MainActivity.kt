package com.example

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Sms
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.local.AssetEntity
import com.example.data.local.ExpenseEntity
import com.example.ui.screens.AddEditAssetSheet
import com.example.ui.screens.AddEditTransactionSheet
import com.example.ui.screens.LockScreen
import com.example.ui.screens.SettingsDialog
import com.example.ui.screens.SmsReaderDialog
import com.example.ui.screens.SpendsScreen
import com.example.ui.screens.WealthScreen
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.FinTrackTheme
import com.example.ui.theme.Indigo100
import com.example.ui.theme.Indigo50
import com.example.ui.theme.Indigo600
import com.example.ui.theme.PolishBackground
import com.example.ui.theme.Slate200
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate500
import com.example.ui.theme.Slate600
import com.example.ui.theme.Slate900
import com.example.ui.viewmodel.FinTrackViewModel
import com.example.util.SecurityPreferences

class MainActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FinTrackTheme {
                val securityPrefs = remember { SecurityPreferences.getInstance(this) }
                var isUnlocked by remember { mutableStateOf(!securityPrefs.isAppLockEnabled) }

                if (securityPrefs.isAppLockEnabled && !isUnlocked) {
                    LockScreen(
                        activity = this@MainActivity,
                        securityPrefs = securityPrefs,
                        onUnlocked = { isUnlocked = true }
                    )
                } else {
                    FinTrackApp(securityPrefs = securityPrefs)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FinTrackApp(
    viewModel: FinTrackViewModel = viewModel(),
    securityPrefs: SecurityPreferences = SecurityPreferences.getInstance(LocalContext.current)
) {
    var selectedTab by remember { mutableIntStateOf(0) } // 0 = Spends, 1 = Wealth

    // Modal Sheet States
    var showTransactionSheet by remember { mutableStateOf(false) }
    var editingExpense by remember { mutableStateOf<ExpenseEntity?>(null) }
    var initialDateForTransaction by remember { mutableStateOf<Long?>(null) }
    var initialTypeForTransaction by remember { mutableStateOf("spent") }

    var showAssetSheet by remember { mutableStateOf(false) }
    var editingAsset by remember { mutableStateOf<AssetEntity?>(null) }

    var showSmsReaderSheet by remember { mutableStateOf(false) }
    var showSettingsSheet by remember { mutableStateOf(false) }

    // Spends State
    val year by viewModel.activeYear.collectAsState()
    val month by viewModel.activeMonth.collectAsState()
    val monthMetrics by viewModel.monthMetrics.collectAsState()
    val dailyNetMap by viewModel.dailyNetMap.collectAsState()
    val dailyHasTransactions by viewModel.dailyHasTransactions.collectAsState()
    val selectedDate by viewModel.selectedDate.collectAsState()
    val selectedCategoryFilter by viewModel.selectedCategoryFilter.collectAsState()
    val selectedTypeFilter by viewModel.selectedTypeFilter.collectAsState()
    val categorySlices by viewModel.categorySlices.collectAsState()
    val displayedTransactions by viewModel.displayedTransactions.collectAsState()
    val selectedDayTransactions by viewModel.selectedDayTransactions.collectAsState()
    val categories by viewModel.allCategories.collectAsState()

    // Wealth State
    val wealthMetrics by viewModel.wealthMetrics.collectAsState()
    val assets by viewModel.allAssets.collectAsState()
    val milestones by viewModel.allMilestones.collectAsState()
    val netWorthTimeline by viewModel.netWorthTimeline.collectAsState()
    val selectedWealthTimeframe by viewModel.selectedWealthTimeframe.collectAsState()

    val transactionSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val assetSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val smsSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val settingsSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            val isDark = isSystemInDarkTheme()
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp))
                    .shadow(12.dp, RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp)),
                color = if (isDark) DarkSurface else Color.White
            ) {
                NavigationBar(
                    containerColor = if (isDark) DarkSurface else Color.White,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    tonalElevation = 0.dp
                ) {
                    NavigationBarItem(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        icon = {
                            Icon(
                                imageVector = if (selectedTab == 0) Icons.Default.CalendarMonth else Icons.Default.ReceiptLong,
                                contentDescription = "Spends"
                            )
                        },
                        label = {
                            Text(
                                "Spends",
                                fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 11.sp
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = if (isDark) Indigo100 else Indigo600,
                            selectedTextColor = if (isDark) Indigo100 else Indigo600,
                            unselectedIconColor = Slate400,
                            unselectedTextColor = Slate400,
                            indicatorColor = if (isDark) Color(0xFF312E81) else Indigo50
                        ),
                        modifier = Modifier.testTag("nav_tab_spends")
                    )

                    NavigationBarItem(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        icon = {
                            Icon(
                                imageVector = if (selectedTab == 1) Icons.Default.AccountBalanceWallet else Icons.Default.TrendingUp,
                                contentDescription = "Wealth"
                            )
                        },
                        label = {
                            Text(
                                "Wealth",
                                fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 11.sp
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = if (isDark) Indigo100 else Indigo600,
                            selectedTextColor = if (isDark) Indigo100 else Indigo600,
                            unselectedIconColor = Slate400,
                            unselectedTextColor = Slate400,
                            indicatorColor = if (isDark) Color(0xFF312E81) else Indigo50
                        ),
                        modifier = Modifier.testTag("nav_tab_wealth")
                    )

                    NavigationBarItem(
                        selected = false,
                        onClick = { showSettingsSheet = true },
                        icon = {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = "Settings"
                            )
                        },
                        label = {
                            Text(
                                "Settings",
                                fontWeight = FontWeight.Medium,
                                fontSize = 11.sp
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = if (isDark) Indigo100 else Indigo600,
                            selectedTextColor = if (isDark) Indigo100 else Indigo600,
                            unselectedIconColor = Slate400,
                            unselectedTextColor = Slate400,
                            indicatorColor = if (isDark) Color(0xFF312E81) else Indigo50
                        ),
                        modifier = Modifier.testTag("nav_tab_settings")
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (selectedTab == 0) {
                SpendsScreen(
                    year = year,
                    month = month,
                    monthMetrics = monthMetrics,
                    dailyNetMap = dailyNetMap,
                    dailyHasTransactions = dailyHasTransactions,
                    selectedDate = selectedDate,
                    selectedCategoryFilter = selectedCategoryFilter,
                    selectedTypeFilter = selectedTypeFilter,
                    categorySlices = categorySlices,
                    transactions = displayedTransactions,
                    selectedDayTransactions = selectedDayTransactions,
                    onPrevMonth = { viewModel.prevMonth() },
                    onNextMonth = { viewModel.nextMonth() },
                    onTodayJump = { viewModel.jumpToToday() },
                    onDateSelected = { viewModel.selectDate(it) },
                    onCategoryFilterSelected = { viewModel.selectCategoryFilter(it) },
                    onTypeFilterSelected = { viewModel.selectTypeFilter(it) },
                    onAddTransactionClick = { date, type ->
                        editingExpense = null
                        initialDateForTransaction = date ?: System.currentTimeMillis()
                        initialTypeForTransaction = type
                        showTransactionSheet = true
                    },
                    onEditTransactionClick = { exp ->
                        editingExpense = exp
                        showTransactionSheet = true
                    },
                    onOpenSmsReaderClick = { showSmsReaderSheet = true }
                )
            } else {
                WealthScreen(
                    wealthMetrics = wealthMetrics,
                    assets = assets,
                    netWorthTimeline = netWorthTimeline,
                    selectedTimeframe = selectedWealthTimeframe,
                    onTimeframeSelected = { viewModel.selectWealthTimeframe(it) },
                    onAddAssetClick = {
                        editingAsset = null
                        showAssetSheet = true
                    },
                    onEditAssetClick = { asset ->
                        editingAsset = asset
                        showAssetSheet = true
                    },
                    onQuickUpdateValuation = { asset, newVal ->
                        viewModel.updateAssetValuation(asset, newVal)
                    }
                )
            }
        }
    }

    // Modal: Add/Edit Transaction
    if (showTransactionSheet) {
        AddEditTransactionSheet(
            existingExpense = editingExpense,
            initialDate = initialDateForTransaction,
            initialType = initialTypeForTransaction,
            categories = categories,
            sheetState = transactionSheetState,
            onDismiss = { showTransactionSheet = false },
            onSave = { amount, type, category, date, merchant, notes ->
                if (editingExpense == null) {
                    viewModel.addExpense(amount, type, category, date, merchant, notes)
                } else {
                    viewModel.updateExpense(
                        editingExpense!!.copy(
                            amount = amount,
                            type = type,
                            category = category,
                            date = date,
                            merchant = merchant,
                            notes = notes
                        )
                    )
                }
            },
            onDelete = { exp ->
                viewModel.deleteExpense(exp)
            },
            onAddNewCategory = { name, type, colorHex, iconName ->
                viewModel.addCustomCategory(name, type, colorHex, iconName)
            }
        )
    }

    // Modal: Add/Edit Asset
    if (showAssetSheet) {
        AddEditAssetSheet(
            existingAsset = editingAsset,
            sheetState = assetSheetState,
            onDismiss = { showAssetSheet = false },
            onSave = { name, assetType, value, investedAmount, isLiability ->
                if (editingAsset == null) {
                    viewModel.addAsset(name, assetType, value = value, investedAmount = investedAmount, isLiability = isLiability)
                } else {
                    viewModel.updateAsset(
                        editingAsset!!.copy(
                            name = name,
                            assetType = assetType,
                            institution = "",
                            value = value,
                            investedAmount = investedAmount,
                            isLiability = isLiability
                        )
                    )
                }
            },
            onDelete = { asset ->
                viewModel.deleteAsset(asset)
            }
        )
    }

    // Modal: Smart Bank SMS Reader
    if (showSmsReaderSheet) {
        SmsReaderDialog(
            sheetState = smsSheetState,
            onDismiss = { showSmsReaderSheet = false },
            onImportConfirmed = { items ->
                viewModel.importSmsTransactions(items) { count ->
                    // Imported count
                }
            },
            categories = categories
        )
    }

    // Modal: Settings & Privacy
    if (showSettingsSheet) {
        SettingsDialog(
            sheetState = settingsSheetState,
            onDismiss = { showSettingsSheet = false },
            securityPrefs = securityPrefs,
            onExport = { callback ->
                viewModel.exportBackup(callback)
            },
            onImport = { json, resultCallback ->
                viewModel.importBackup(json, resultCallback)
            },
            onClearAllData = {
                viewModel.clearAllData()
            }
        )
    }
}
