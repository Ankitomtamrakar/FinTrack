package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SheetState
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
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
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.CategoryEntity
import com.example.data.local.ExpenseEntity
import com.example.ui.theme.Slate200
import com.example.ui.theme.Slate700
import com.example.util.CategoryHelper
import com.example.util.DateUtils

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AddEditTransactionSheet(
    existingExpense: ExpenseEntity?,
    initialDate: Long?,
    initialType: String = "spent",
    categories: List<CategoryEntity>,
    sheetState: SheetState,
    onDismiss: () -> Unit,
    onSave: (amount: Double, type: String, category: String, date: Long, merchant: String, notes: String) -> Unit,
    onDelete: ((ExpenseEntity) -> Unit)? = null,
    onAddNewCategory: (name: String, type: String, colorHex: String, iconName: String) -> Unit
) {
    var type by remember { mutableStateOf(existingExpense?.type ?: initialType) }
    var amountText by remember {
        mutableStateOf(
            if (existingExpense != null) {
                if (existingExpense.amount % 1.0 == 0.0) existingExpense.amount.toLong().toString()
                else existingExpense.amount.toString()
            } else ""
        )
    }
    var selectedCategory by remember {
        mutableStateOf(
            existingExpense?.category?.let { cat ->
                if (cat.equals("Other Expense", ignoreCase = true) || cat.equals("Other Income", ignoreCase = true)) "Other"
                else cat
            } ?: if (type == "spent") "Food" else "Salary"
        )
    }
    var remarksText by remember {
        mutableStateOf(
            existingExpense?.notes?.ifBlank {
                if (existingExpense.merchant != existingExpense.category && existingExpense.merchant != "Expense" && existingExpense.merchant != "Income") {
                    existingExpense.merchant
                } else ""
            } ?: ""
        )
    }
    var selectedTimestamp by remember {
        mutableStateOf(existingExpense?.date ?: (initialDate ?: System.currentTimeMillis()))
    }

    var showDatePicker by remember { mutableStateOf(false) }
    var showNewCategoryDialog by remember { mutableStateOf(false) }

    val defaultCategoriesForType = remember(type) {
        if (type == "spent") {
            listOf(
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
                CategoryEntity(name = "Other", type = "spent", colorHex = "#64748B", iconName = "more_horiz")
            )
        } else {
            listOf(
                CategoryEntity(name = "Salary", type = "earned", colorHex = "#0284C7", iconName = "payments"),
                CategoryEntity(name = "Freelance", type = "earned", colorHex = "#8B5CF6", iconName = "work"),
                CategoryEntity(name = "Investment Return", type = "earned", colorHex = "#6366F1", iconName = "show_chart"),
                CategoryEntity(name = "Dividend", type = "earned", colorHex = "#10B981", iconName = "show_chart"),
                CategoryEntity(name = "Cashback and Rewards", type = "earned", colorHex = "#EC4899", iconName = "redeem"),
                CategoryEntity(name = "Gifts", type = "earned", colorHex = "#F43F5E", iconName = "redeem"),
                CategoryEntity(name = "Refund", type = "earned", colorHex = "#F97316", iconName = "receipt_long"),
                CategoryEntity(name = "Other", type = "earned", colorHex = "#64748B", iconName = "account_balance_wallet")
            )
        }
    }

    val filteredCategories = remember(categories, type, defaultCategoriesForType) {
        val userCategories = categories.filter { it.type == type }.map { cat ->
            if (cat.name.equals("Other Income", ignoreCase = true) || cat.name.equals("Other Expense", ignoreCase = true)) {
                cat.copy(name = "Other")
            } else {
                cat
            }
        }
        val combined = (defaultCategoriesForType + userCategories)
            .distinctBy { it.name.lowercase().trim() }

        val sortedList = if (type == "spent") {
            val spentOrder = listOf(
                "food", "grocery", "shopping", "bills",
                "medical", "entertainment", "investment", "travel",
                "rent", "education", "other", "other expense"
            )
            combined.sortedBy { cat ->
                val idx = spentOrder.indexOf(cat.name.lowercase().trim())
                if (idx != -1) idx else 999
            }
        } else {
            val earnedOrder = listOf(
                "salary", "freelance", "investment return", "dividend",
                "cashback and rewards", "gifts", "refund", "other", "other income"
            )
            combined.sortedBy { cat ->
                val idx = earnedOrder.indexOf(cat.name.lowercase().trim())
                if (idx != -1) idx else 999
            }
        }

        val (others, regulars) = sortedList.partition {
            it.name.equals("Other", ignoreCase = true) ||
            it.name.equals("Other Income", ignoreCase = true) ||
            it.name.equals("Other Expense", ignoreCase = true)
        }
        regulars + others
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        dragHandle = null,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 20.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Title & Close/Delete
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = if (existingExpense == null) "Add Transaction" else "Edit Transaction",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                if (existingExpense != null && onDelete != null) {
                    IconButton(
                        onClick = {
                            onDelete(existingExpense)
                            onDismiss()
                        },
                        modifier = Modifier.testTag("btn_delete_transaction")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Spent vs Earned Toggle
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(4.dp)
            ) {
                val isSpent = type == "spent"
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (isSpent) Color(0xFFF43F5E) else Color.Transparent)
                        .clickable {
                            type = "spent"
                            selectedCategory = "Food"
                        }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Spent (Outflow)",
                        color = if (isSpent) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = if (isSpent) FontWeight.Bold else FontWeight.Medium,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (!isSpent) Color(0xFF10B981) else Color.Transparent)
                        .clickable {
                            type = "earned"
                            selectedCategory = "Salary"
                        }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Earned (Inflow)",
                        color = if (!isSpent) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = if (!isSpent) FontWeight.Bold else FontWeight.Medium,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Amount Input with Rupee symbol
            OutlinedTextField(
                value = amountText,
                onValueChange = { amountText = it.filter { c -> c.isDigit() || c == '.' } },
                label = { Text("Amount") },
                placeholder = { Text("0.00") },
                leadingIcon = {
                    Text(
                        text = "₹",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = if (type == "spent") Color(0xFFF43F5E) else Color(0xFF10B981),
                        modifier = Modifier.padding(start = 8.dp)
                    )
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                textStyle = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("input_transaction_amount")
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Category Selector
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Category",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )

                TextButton(onClick = { showNewCategoryDialog = true }) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("New", style = MaterialTheme.typography.labelMedium)
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Compact Category Bubbles
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                for (cat in filteredCategories) {
                    val isSelected = selectedCategory.equals(cat.name, ignoreCase = true) ||
                        (cat.name.equals("Other", ignoreCase = true) && (selectedCategory.equals("Other Expense", ignoreCase = true) || selectedCategory.equals("Other Income", ignoreCase = true)))
                    val catColor = CategoryHelper.getCategoryColor(cat.name, cat.colorHex)
                    val icon = CategoryHelper.getCategoryIcon(cat.iconName, cat.name)

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (isSelected) catColor else catColor.copy(alpha = 0.12f),
                        border = BorderStroke(
                            width = if (isSelected) 2.dp else 1.dp,
                            color = if (isSelected) Color.White.copy(alpha = 0.9f) else catColor.copy(alpha = 0.35f)
                        ),
                        modifier = Modifier
                            .shadow(
                                elevation = if (isSelected) 8.dp else 0.dp,
                                shape = RoundedCornerShape(12.dp),
                                ambientColor = catColor,
                                spotColor = catColor
                            )
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { selectedCategory = cat.name }
                            .testTag("category_chip_${cat.name}")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(20.dp)
                                    .background(
                                        if (isSelected) Color.White.copy(alpha = 0.25f) else catColor.copy(alpha = 0.18f),
                                        CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = null,
                                    modifier = Modifier.size(12.dp),
                                    tint = if (isSelected) Color.White else catColor
                                )
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = cat.name,
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.5.sp),
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                                color = if (isSelected) Color.White else catColor
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Date Picker Field
            OutlinedTextField(
                value = DateUtils.formatDateOnly(selectedTimestamp),
                onValueChange = {},
                readOnly = true,
                label = { Text("Date") },
                trailingIcon = {
                    IconButton(onClick = { showDatePicker = true }) {
                        Icon(imageVector = Icons.Default.CalendarMonth, contentDescription = "Pick Date")
                    }
                },
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showDatePicker = true }
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Remarks / Notes (Merchant option removed; remarks used for any specific mention)
            OutlinedTextField(
                value = remarksText,
                onValueChange = { remarksText = it },
                label = { Text("Remarks (Optional)") },
                placeholder = { Text("e.g. Swiggy, Groceries, Dinner, Rent") },
                maxLines = 2,
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Words
                ),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("input_transaction_remarks")
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Save Button
            val isValid = (amountText.toDoubleOrNull() ?: 0.0) > 0.0
            Button(
                onClick = {
                    val amt = amountText.toDoubleOrNull() ?: 0.0
                    if (amt > 0.0) {
                        onSave(
                            amt,
                            type,
                            selectedCategory,
                            selectedTimestamp,
                            remarksText.ifBlank { selectedCategory },
                            remarksText
                        )
                        onDismiss()
                    }
                },
                enabled = isValid,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("btn_save_transaction"),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (type == "spent") com.example.ui.theme.Indigo600 else com.example.ui.theme.Emerald600
                )
            ) {
                Text(
                    text = if (existingExpense == null) "Add Transaction" else "Save Changes",
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    // Material 3 DatePickerDialog
    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = selectedTimestamp
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let {
                            selectedTimestamp = it
                        }
                        showDatePicker = false
                    }
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    // Custom Category Creation Dialog
    if (showNewCategoryDialog) {
        var newCatName by remember { mutableStateOf("") }
        var selectedColorHex by remember { mutableStateOf("#EF4444") }
        var selectedIconName by remember { mutableStateOf("restaurant") }

        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showNewCategoryDialog = false },
            title = { Text("Create Category") },
            text = {
                Column {
                    OutlinedTextField(
                        value = newCatName,
                        onValueChange = { newCatName = it },
                        label = { Text("Category Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Color", style = MaterialTheme.typography.labelMedium)
                    Spacer(modifier = Modifier.height(6.dp))

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        for (c in CategoryHelper.PALETTE_COLORS) {
                            val hex = "#" + Integer.toHexString(c.hashCode()).uppercase().takeLast(6)
                            val isColorSelected = selectedColorHex.equals(hex, ignoreCase = true)
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .background(c)
                                    .then(
                                        if (isColorSelected) Modifier.border(2.dp, MaterialTheme.colorScheme.onSurface, CircleShape)
                                        else Modifier
                                    )
                                    .clickable { selectedColorHex = hex },
                                contentAlignment = Alignment.Center
                            ) {
                                if (isColorSelected) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (newCatName.isNotBlank()) {
                            onAddNewCategory(newCatName.trim(), type, selectedColorHex, selectedIconName)
                            selectedCategory = newCatName.trim()
                            showNewCategoryDialog = false
                        }
                    },
                    enabled = newCatName.isNotBlank()
                ) {
                    Text("Create")
                }
            },
            dismissButton = {
                TextButton(onClick = { showNewCategoryDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
