package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.RequestQuote
import androidx.compose.material.icons.filled.Sms
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.ExpenseEntity
import com.example.ui.components.CalendarMatrix
import com.example.ui.components.CardPatternType
import com.example.ui.components.DonutChart
import com.example.ui.components.PieSliceData
import com.example.ui.components.creativeCardBackground
import com.example.ui.theme.*
import com.example.ui.viewmodel.SpendsMonthMetrics
import com.example.util.CategoryHelper
import com.example.util.CurrencyFormatter
import com.example.util.DateUtils

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun SpendsScreen(
    year: Int,
    month: Int,
    monthMetrics: SpendsMonthMetrics,
    dailyNetMap: Map<Long, Double>,
    dailyHasTransactions: Set<Long>,
    selectedDate: Long?,
    selectedCategoryFilter: String?,
    selectedTypeFilter: String? = null,
    categorySlices: List<PieSliceData>,
    transactions: List<ExpenseEntity>,
    selectedDayTransactions: List<ExpenseEntity>,
    onPrevMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onTodayJump: () -> Unit,
    onDateSelected: (Long?) -> Unit,
    onCategoryFilterSelected: (String?) -> Unit,
    onTypeFilterSelected: (String?) -> Unit = {},
    onAddTransactionClick: (date: Long?, initialType: String) -> Unit,
    onEditTransactionClick: (ExpenseEntity) -> Unit,
    onOpenSmsReaderClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showDayDetailSheet by remember { mutableStateOf(false) }
    var isTransactionsExpanded by remember { mutableStateOf(false) }
    val isDark = isSystemInDarkTheme()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("spends_screen_column"),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 1. Monthly Summary Cards
        item {
            MonthlyMetricsCards(
                year = year,
                month = month,
                metrics = monthMetrics,
                selectedTypeFilter = selectedTypeFilter,
                onSpentClick = {
                    onTypeFilterSelected("spent")
                    isTransactionsExpanded = true
                },
                onEarnedClick = {
                    onTypeFilterSelected("earned")
                    isTransactionsExpanded = true
                },
                onAddClick = { onAddTransactionClick(null, "spent") },
                onSmsClick = onOpenSmsReaderClick,
                onPrevMonth = onPrevMonth,
                onNextMonth = onNextMonth,
                onTodayJump = onTodayJump
            )
        }

        // 2. Action Buttons (Add Spend - Red, Add Earned - Green, Bank SMS - Indigo)
        item {
            ActionButtonsGrid(
                onAddSpendClick = { onAddTransactionClick(null, "spent") },
                onAddEarnedClick = { onAddTransactionClick(null, "earned") },
                onSmsClick = onOpenSmsReaderClick
            )
        }

        // 3. Category Breakdown & Visual Analytics Donut
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("analytics_card"),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = if (isDark) DarkCard else Color.White),
                border = BorderStroke(1.dp, if (isDark) Color(0xFF334155).copy(alpha = 0.6f) else Slate200.copy(alpha = 0.6f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .creativeCardBackground(CardPatternType.OrbitsAndRings, accentColor = Indigo600, isDark = isDark)
                        .padding(18.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "Expense Breakdown",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (isDark) Color.White else Slate900
                            )
                            Text(
                                text = "Tap chart or category to filter",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (isDark) Slate400 else Slate500
                            )
                        }

                        if (selectedCategoryFilter != null) {
                            TextButton(
                                onClick = { onCategoryFilterSelected(null) },
                                modifier = Modifier.testTag("btn_clear_category_filter")
                            ) {
                                Icon(Icons.Default.Clear, contentDescription = null, modifier = Modifier.size(14.dp), tint = Indigo600)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Clear filter", style = MaterialTheme.typography.labelSmall, color = Indigo600, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    DonutChart(
                        slices = categorySlices,
                        totalAmount = monthMetrics.totalSpent,
                        selectedSliceLabel = selectedCategoryFilter,
                        onSliceSelected = { onCategoryFilterSelected(it) }
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    // Category Breakdown Compact View (Tight, compact lines without extra spaces)
                    FlowRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 2.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        for (slice in categorySlices) {
                            val isSelected = selectedCategoryFilter == slice.label
                            val catColor = slice.color
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (isSelected) catColor else catColor.copy(alpha = 0.12f),
                                border = BorderStroke(
                                    width = if (isSelected) 2.dp else 1.dp,
                                    color = if (isSelected) Color.White.copy(alpha = 0.9f) else catColor.copy(alpha = 0.35f)
                                ),
                                modifier = Modifier
                                    .padding(horizontal = 2.dp, vertical = 2.dp)
                                    .shadow(
                                        elevation = if (isSelected) 8.dp else 0.dp,
                                        shape = RoundedCornerShape(10.dp),
                                        ambientColor = catColor,
                                        spotColor = catColor
                                    )
                                    .clip(RoundedCornerShape(10.dp))
                                    .clickable {
                                        if (isSelected) {
                                            onCategoryFilterSelected(null)
                                        } else {
                                            onCategoryFilterSelected(slice.label)
                                            isTransactionsExpanded = true
                                        }
                                    }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(7.dp)
                                            .background(if (isSelected) Color.White else catColor, CircleShape)
                                    )
                                    Spacer(modifier = Modifier.width(5.dp))
                                    Text(
                                        text = slice.label,
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.5.sp),
                                        fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.SemiBold,
                                        color = if (isSelected) Color.White else catColor
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = CurrencyFormatter.formatCompactInr(slice.value),
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.5.sp),
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) Color.White.copy(alpha = 0.95f) else Slate600
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // 4. Single Collapsible Transactions Card Heading "Transactions"
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("card_transactions_collapsible"),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isDark) DarkCard else Color.White
                ),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = androidx.compose.ui.graphics.SolidColor(if (isDark) Color(0xFF334155).copy(alpha = 0.6f) else Slate200.copy(alpha = 0.8f))
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = if (isTransactionsExpanded) 2.dp else 1.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .creativeCardBackground(CardPatternType.GeometricFinance, accentColor = Indigo600, isDark = isDark)
                ) {
                    // Header Row (Clickable to expand/collapse)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { isTransactionsExpanded = !isTransactionsExpanded }
                            .padding(horizontal = 16.dp, vertical = 14.dp)
                            .testTag("btn_toggle_transactions"),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (isTransactionsExpanded) (if (isDark) Color(0xFF1E1B4B) else Indigo50) else (if (isDark) Color(0xFF1E293B) else Slate200.copy(alpha = 0.5f))),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ReceiptLong,
                                    contentDescription = null,
                                    tint = if (isTransactionsExpanded) (if (isDark) Indigo500 else Indigo600) else (if (isDark) Slate400 else Slate600),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    val title = when {
                                        selectedCategoryFilter != null -> "$selectedCategoryFilter"
                                        selectedTypeFilter == "spent" -> "Spent Transactions"
                                        selectedTypeFilter == "earned" -> "Earned Transactions"
                                        else -> "Transactions"
                                    }
                                    Text(
                                        text = title,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isDark) Color.White else Slate900
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = when {
                                            selectedTypeFilter == "spent" -> if (isDark) Color(0xFF4C0519).copy(alpha = 0.5f) else Rose50
                                            selectedTypeFilter == "earned" -> if (isDark) Color(0xFF064E3B).copy(alpha = 0.5f) else Emerald50
                                            isTransactionsExpanded -> if (isDark) Color(0xFF1E1B4B) else Indigo50
                                            else -> if (isDark) Color(0xFF1E293B) else Slate200
                                        }
                                    ) {
                                        Text(
                                            text = "${transactions.size}",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = when {
                                                selectedTypeFilter == "spent" -> Rose600
                                                selectedTypeFilter == "earned" -> Emerald600
                                                isTransactionsExpanded -> if (isDark) Indigo500 else Indigo600
                                                else -> if (isDark) Slate400 else Slate700
                                            },
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                                val subtitle = when {
                                    selectedCategoryFilter != null -> "Filtered by $selectedCategoryFilter (${transactions.size})"
                                    selectedTypeFilter == "spent" -> "Showing all spent transactions (${transactions.size})"
                                    selectedTypeFilter == "earned" -> "Showing all earned transactions (${transactions.size})"
                                    isTransactionsExpanded -> "Tap to collapse"
                                    else -> "Tap to show (${transactions.size} transactions)"
                                }
                                Text(
                                    text = subtitle,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (isDark) Slate400 else Slate500
                                )
                            }
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (selectedTypeFilter != null) {
                                SuggestionChip(
                                    onClick = { onTypeFilterSelected(null) },
                                    label = {
                                        Text(
                                            if (selectedTypeFilter == "spent") "Spent" else "Earned",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = if (selectedTypeFilter == "spent") Rose600 else Emerald600,
                                            fontWeight = FontWeight.Bold
                                        )
                                    },
                                    icon = {
                                        Icon(
                                            Icons.Default.Close,
                                            contentDescription = "Clear type filter",
                                            modifier = Modifier.size(12.dp),
                                            tint = if (selectedTypeFilter == "spent") Rose600 else Emerald600
                                        )
                                    },
                                    modifier = Modifier.padding(end = 6.dp)
                                )
                            }

                            if (selectedCategoryFilter != null) {
                                SuggestionChip(
                                    onClick = { onCategoryFilterSelected(null) },
                                    label = { Text("Clear", style = MaterialTheme.typography.labelSmall, color = Indigo600, fontWeight = FontWeight.Bold) },
                                    icon = { Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(12.dp), tint = Indigo600) },
                                    modifier = Modifier.padding(end = 6.dp)
                                )
                            }

                            Icon(
                                imageVector = if (isTransactionsExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                contentDescription = if (isTransactionsExpanded) "Collapse transactions" else "Expand transactions",
                                tint = if (isTransactionsExpanded) Indigo600 else Slate500,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }

                    // All transaction rows inside this single collapsible card
                    if (isTransactionsExpanded) {
                        HorizontalDivider(
                            color = Slate200.copy(alpha = 0.6f),
                            thickness = 1.dp
                        )

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 12.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            if (transactions.isEmpty()) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 20.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .background(Indigo50, CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Receipt,
                                            contentDescription = null,
                                            tint = Indigo600,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "No transactions found",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = Slate900
                                    )
                                    Text(
                                        text = "Tap '+ Add Spend' or sync Bank SMS to log",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Slate500,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            } else {
                                for (expense in transactions) {
                                    TransactionListItem(
                                        expense = expense,
                                        onClick = { onEditTransactionClick(expense) }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // 6. Interactive Calendar Activity Card (Displayed at the last after Expense Breakdown and all transactions)
        item {
            Column {
                Text(
                    text = "Calendar Activity",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Slate900
                )
                Spacer(modifier = Modifier.height(8.dp))
                CalendarMatrix(
                    year = year,
                    month = month,
                    dailyNetMap = dailyNetMap,
                    dailyHasTransactions = dailyHasTransactions,
                    selectedDate = selectedDate,
                    onDateSelected = { clickedDate ->
                        onDateSelected(clickedDate)
                        showDayDetailSheet = true
                    },
                    onPrevMonth = onPrevMonth,
                    onNextMonth = onNextMonth,
                    onTodayJump = onTodayJump
                )
            }
        }
    }

    // Day View Bottom Sheet
    if (showDayDetailSheet && selectedDate != null) {
        val daySheetState = rememberModalBottomSheetState()
        ModalBottomSheet(
            onDismissRequest = {
                showDayDetailSheet = false
                onDateSelected(null)
            },
            sheetState = daySheetState,
            dragHandle = null,
            shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
            containerColor = if (isDark) DarkSurface else Color.White
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = DateUtils.formatDateOnly(selectedDate),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (isDark) Color.White else Slate900
                        )
                        Text(
                            text = "${selectedDayTransactions.size} transactions on this day",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isDark) Slate400 else Slate500
                        )
                    }

                    IconButton(onClick = {
                        showDayDetailSheet = false
                        onDateSelected(null)
                    }) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Slate500)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Button(
                    onClick = {
                        showDayDetailSheet = false
                        onAddTransactionClick(selectedDate, "spent")
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("btn_add_for_day"),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Indigo600)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Add Transaction for this date", fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(14.dp))

                if (selectedDayTransactions.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No spends or earnings recorded on this date",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Slate400
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(320.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(selectedDayTransactions, key = { it.id }) { item ->
                            TransactionListItem(
                                expense = item,
                                onClick = {
                                    showDayDetailSheet = false
                                    onEditTransactionClick(item)
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun MonthlyMetricsCards(
    year: Int,
    month: Int,
    metrics: SpendsMonthMetrics,
    selectedTypeFilter: String? = null,
    onSpentClick: () -> Unit = {},
    onEarnedClick: () -> Unit = {},
    onAddClick: () -> Unit,
    onSmsClick: () -> Unit,
    onPrevMonth: () -> Unit = {},
    onNextMonth: () -> Unit = {},
    onTodayJump: () -> Unit = {}
) {
    val isDark = isSystemInDarkTheme()
    val isNetPositive = metrics.netBalance >= 0

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("card_monthly_metrics_summary"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = if (isDark) DarkCard else Color.White),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(if (isDark) Color(0xFF334155).copy(alpha = 0.6f) else Slate200.copy(alpha = 0.6f))
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .creativeCardBackground(CardPatternType.MeshGlow, accentColor = Indigo600, isDark = isDark)
                .padding(horizontal = 14.dp, vertical = 14.dp)
        ) {
            // Month Header at top of card with previous / next month navigation
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 2.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onPrevMonth,
                    modifier = Modifier
                        .size(32.dp)
                        .testTag("btn_metrics_prev_month")
                ) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .background(if (isDark) Color(0xFF1E293B) else PolishSurfaceVariant, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Previous Month",
                            tint = if (isDark) Color.White else Slate700,
                            modifier = Modifier.size(15.dp)
                        )
                    }
                }

                Text(
                    text = DateUtils.formatMonthYear(year, month),
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = if (isDark) Color.White else Slate900,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .clickable(onClick = onTodayJump)
                        .padding(vertical = 2.dp)
                )

                IconButton(
                    onClick = onNextMonth,
                    modifier = Modifier
                        .size(32.dp)
                        .testTag("btn_metrics_next_month")
                ) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .background(if (isDark) Color(0xFF1E293B) else PolishSurfaceVariant, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = "Next Month",
                            tint = if (isDark) Color.White else Slate700,
                            modifier = Modifier.size(15.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Values Row: Spent on left, Earned on right (both interactive)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                // Left side: Spent (clickable)
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = if (selectedTypeFilter == "spent") {
                        if (isDark) Color(0xFF4C0519).copy(alpha = 0.45f) else Rose50
                    } else Color.Transparent,
                    border = if (selectedTypeFilter == "spent") BorderStroke(1.5.dp, if (isDark) Rose600.copy(alpha = 0.6f) else Rose300) else null,
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(16.dp))
                        .clickable { onSpentClick() }
                        .testTag("card_spent_section")
                ) {
                    Column(
                        horizontalAlignment = Alignment.Start,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(18.dp)
                                    .background(if (selectedTypeFilter == "spent") (if (isDark) Color(0xFF881337) else Rose200) else (if (isDark) Color(0xFF1E293B) else Rose50), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ArrowUpward,
                                    contentDescription = null,
                                    tint = Rose600,
                                    modifier = Modifier.size(10.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Spent",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                                color = if (selectedTypeFilter == "spent") (if (isDark) Rose300 else Rose700) else (if (isDark) Slate400 else Slate500),
                                fontWeight = if (selectedTypeFilter == "spent") FontWeight.ExtraBold else FontWeight.SemiBold
                            )
                            if (selectedTypeFilter == "spent") {
                                Spacer(modifier = Modifier.width(4.dp))
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .background(Rose600, CircleShape)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (metrics.totalSpent >= 10000000.0) CurrencyFormatter.formatCompactInr(metrics.totalSpent) else CurrencyFormatter.formatInr(metrics.totalSpent),
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontSize = 25.sp,
                                fontWeight = FontWeight.Black
                            ),
                            color = Rose600,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.testTag("card_total_spent")
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Right side: Earned (clickable)
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = if (selectedTypeFilter == "earned") {
                        if (isDark) Color(0xFF064E3B).copy(alpha = 0.45f) else Emerald50
                    } else Color.Transparent,
                    border = if (selectedTypeFilter == "earned") BorderStroke(1.5.dp, if (isDark) Emerald600.copy(alpha = 0.6f) else Emerald300) else null,
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(16.dp))
                        .clickable { onEarnedClick() }
                        .testTag("card_earned_section")
                ) {
                    Column(
                        horizontalAlignment = Alignment.End,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (selectedTypeFilter == "earned") {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .background(Emerald600, CircleShape)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                            }
                            Text(
                                text = "Earned",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                                color = if (selectedTypeFilter == "earned") (if (isDark) Emerald300 else Emerald700) else (if (isDark) Slate400 else Slate500),
                                fontWeight = if (selectedTypeFilter == "earned") FontWeight.ExtraBold else FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .size(18.dp)
                                    .background(if (selectedTypeFilter == "earned") (if (isDark) Color(0xFF065F46) else Emerald200) else (if (isDark) Color(0xFF1E293B) else Emerald50), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ArrowDownward,
                                    contentDescription = null,
                                    tint = Emerald600,
                                    modifier = Modifier.size(10.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (metrics.totalEarned >= 10000000.0) CurrencyFormatter.formatCompactInr(metrics.totalEarned) else CurrencyFormatter.formatInr(metrics.totalEarned),
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontSize = 25.sp,
                                fontWeight = FontWeight.Black
                            ),
                            color = Emerald600,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.testTag("card_total_earned")
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Subtle divider line
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(if (isDark) Color(0xFF334155).copy(alpha = 0.5f) else Slate200.copy(alpha = 0.5f))
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Bottom Right: Net balance in small font size
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("card_net_balance"),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Net Balance: ",
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                    color = if (isDark) Slate400 else Slate500,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "${if (isNetPositive) "+" else ""}${if (kotlin.math.abs(metrics.netBalance) >= 10000000.0) CurrencyFormatter.formatCompactInr(metrics.netBalance) else CurrencyFormatter.formatInr(metrics.netBalance)}",
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                    fontWeight = FontWeight.Bold,
                    color = if (isNetPositive) Emerald600 else Rose600
                )
            }
        }
    }
}

@Composable
private fun ActionButtonsGrid(
    onAddSpendClick: () -> Unit,
    onAddEarnedClick: () -> Unit,
    onSmsClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // 1. Add Spend (Red)
        ActionButtonColouredCard(
            label = "Add Spend",
            icon = Icons.Default.ArrowUpward,
            backgroundColor = Color(0xFFE11D48),
            onClick = onAddSpendClick,
            testTag = "btn_grid_add",
            modifier = Modifier.weight(1f)
        )

        // 2. Add Earned (Green)
        ActionButtonColouredCard(
            label = "Add Earned",
            icon = Icons.Default.ArrowDownward,
            backgroundColor = Color(0xFF059669),
            onClick = onAddEarnedClick,
            testTag = "btn_grid_add_earned",
            modifier = Modifier.weight(1f)
        )

        // 3. Bank SMS (Indigo)
        ActionButtonColouredCard(
            label = "Bank SMS",
            icon = Icons.Default.Sms,
            backgroundColor = Color(0xFF4F46E5),
            onClick = onSmsClick,
            testTag = "btn_grid_sms",
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun ActionButtonColouredCard(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    backgroundColor: Color,
    onClick: () -> Unit,
    testTag: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .clickable { onClick() }
            .testTag(testTag),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp, horizontal = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.22f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.titleSmall.copy(fontSize = 14.sp),
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun TransactionListItem(
    expense: ExpenseEntity,
    onClick: () -> Unit
) {
    val isDark = isSystemInDarkTheme()
    val isCredit = expense.type == "earned"
    val catColor = CategoryHelper.getCategoryColor(expense.category)
    val containerBg = if (isDark) Color(0xFF141C2E) else catColor.copy(alpha = 0.08f)
    val borderColor = if (isDark) catColor.copy(alpha = 0.35f) else catColor.copy(alpha = 0.25f)
    val iconBg = if (isDark) catColor.copy(alpha = 0.25f) else catColor.copy(alpha = 0.18f)
    val accentColor = catColor
    val subTextColor = if (isDark) Slate400 else Slate500
    val catIcon = CategoryHelper.getCategoryIcon(null, expense.category)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .testTag("transaction_item_${expense.id}"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = containerBg),
        border = BorderStroke(1.dp, borderColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .creativeCardBackground(CardPatternType.SubtleTexture, accentColor = catColor, isDark = isDark)
                .padding(horizontal = 12.dp, vertical = 7.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Compact icon badge
            Box(
                modifier = Modifier
                    .size(26.dp)
                    .background(iconBg, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = catIcon,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(14.dp)
                )
            }

            Spacer(modifier = Modifier.width(9.dp))

            // Single-line compact title & date
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = expense.notes.ifBlank { expense.merchant.ifBlank { expense.category } },
                    style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp),
                    fontWeight = FontWeight.SemiBold,
                    color = if (isDark) Color.White else Slate900,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "•  ${DateUtils.formatDateShort(expense.date)}",
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                    color = subTextColor,
                    maxLines = 1
                )
                if (expense.isFromSMS) {
                    Spacer(modifier = Modifier.width(4.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(3.dp))
                            .background(iconBg)
                            .padding(horizontal = 3.dp, vertical = 0.5.dp)
                    ) {
                        Text(
                            text = "SMS",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp),
                            color = accentColor,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Formatted Amount
            Text(
                text = "${if (isCredit) "+" else "-"}${CurrencyFormatter.formatInr(expense.amount)}",
                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp),
                fontWeight = FontWeight.Bold,
                color = accentColor
            )
        }
    }
}
