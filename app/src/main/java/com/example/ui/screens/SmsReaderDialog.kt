package com.example.ui.screens

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.MarkEmailRead
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Sms
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SheetState
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.data.local.CategoryEntity
import com.example.ui.components.CardPatternType
import com.example.ui.components.creativeCardBackground
import com.example.ui.theme.Emerald600
import com.example.ui.theme.Rose600
import com.example.util.CategoryHelper
import com.example.util.CurrencyFormatter
import com.example.util.DateUtils
import com.example.util.ParsedSmsTransaction
import com.example.util.SmsReaderUtil

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SmsReaderDialog(
    sheetState: SheetState,
    onDismiss: () -> Unit,
    onImportConfirmed: (List<ParsedSmsTransaction>) -> Unit,
    categories: List<CategoryEntity> = emptyList()
) {
    val context = LocalContext.current
    var selectedTab by remember { mutableIntStateOf(0) } // 0 = Inbox Scan, 1 = Manual Paste

    val parsedItems = remember { mutableStateListOf<ParsedSmsTransaction>() }
    var hasSmsPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.READ_SMS) == PackageManager.PERMISSION_GRANTED
        )
    }
    var permissionDeniedPermanently by remember { mutableStateOf(false) }
    var rawSmsInput by remember { mutableStateOf("") }
    var parseError by remember { mutableStateOf<String?>(null) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasSmsPermission = isGranted
        if (isGranted) {
            permissionDeniedPermanently = false
            val scanned = SmsReaderUtil.readRecentBankingSms(context)
            parsedItems.clear()
            parsedItems.addAll(scanned)
        } else {
            permissionDeniedPermanently = true
        }
    }

    fun scanInbox() {
        if (hasSmsPermission) {
            val scanned = SmsReaderUtil.readRecentBankingSms(context)
            parsedItems.clear()
            parsedItems.addAll(scanned)
        } else {
            permissionLauncher.launch(Manifest.permission.READ_SMS)
        }
    }

    LaunchedEffect(Unit) {
        if (hasSmsPermission) {
            scanInbox()
        }
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
                .fillMaxHeight(0.88f)
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Sms,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Smart Bank SMS Reader",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Privacy-first: on-device regex extraction",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                IconButton(onClick = onDismiss) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Dual Tab Row
            TabRow(selectedTabIndex = selectedTab) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Inbox Scan", fontWeight = FontWeight.SemiBold) },
                    icon = { Icon(Icons.Default.MarkEmailRead, contentDescription = null, modifier = Modifier.size(16.dp)) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Paste Raw SMS", fontWeight = FontWeight.SemiBold) },
                    icon = { Icon(Icons.Default.ContentPaste, contentDescription = null, modifier = Modifier.size(16.dp)) }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (selectedTab == 0) {
                // Tab 0: Inbox Scan
                if (!hasSmsPermission) {
                    // Permission Request Callout
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "SMS Permission Required",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "FinTrack requires READ_SMS permission to auto-detect bank debit/credit alerts. Your SMS data NEVER leaves your phone.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Row {
                                Button(
                                    onClick = { scanInbox() },
                                    modifier = Modifier.testTag("btn_grant_sms_permission"),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Text("Grant Permission")
                                }
                                if (permissionDeniedPermanently) {
                                    Spacer(modifier = Modifier.width(8.dp))
                                    OutlinedButton(
                                        onClick = {
                                            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                                data = Uri.fromParts("package", context.packageName, null)
                                            }
                                            context.startActivity(intent)
                                        },
                                        shape = RoundedCornerShape(10.dp)
                                    ) {
                                        Text("Open Settings")
                                    }
                                }
                            }
                        }
                    }
                }

                // If permission granted or has sample items
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "Found ${parsedItems.size} transactions",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Tap transaction to view full SMS",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Row {
                        if (parsedItems.isEmpty()) {
                            TextButton(
                                onClick = {
                                    // Load realistic sample Indian bank SMS for instant testing
                                    val samples = listOf(
                                        "INR 225.00 spent at UPI/Garhwal Paneer on AU Bank Credit Card XX8080 27-09-2026 07:05:41 PM",
                                        "Dear Customer, INR 749.00 debited from A/c XX4021 on 04-Sep-26 towards SWIGGY. Avl Bal: INR 45,210.00 - HDFC Bank",
                                        "A/c *1928 debited for Rs. 2,199.00 on 05-Sep-26 at AMAZON PAY UPI ref 424819201. Bal: Rs 43,011.00 - ICICI Bank",
                                        "Salary of INR 1,25,000.00 credited to your A/c **8832 on 01-Sep-26 by TECH CORP. Avl Bal: INR 1,68,011.00",
                                        "Alert: Rs 320.00 paid to UBER INDIA on 04-Sep-26 using UPI. Avl Limit: Rs 50,000",
                                        "Your A/c 5510 is debited for Rs 850.00 on 03-Sep-26 at BLINKIT. Info: UPI/BLINKIT/4921",
                                        "Rs. 1,499.00 debited for AIRTEL BROADBAND on 02-Sep-26 via AutoPay. Avl Bal Rs 41,200.00",
                                        "Cashback of INR 150.00 credited to A/c 4021 for CRED payment on 03-Sep-26."
                                    )
                                    parsedItems.clear()
                                    for ((idx, s) in samples.withIndex()) {
                                        SmsReaderUtil.parseSms(s, System.currentTimeMillis() - idx * 43200000L)?.let {
                                            parsedItems.add(it)
                                        }
                                    }
                                }
                            ) {
                                Text("Load Sample SMS", style = MaterialTheme.typography.labelSmall)
                            }
                        }

                        if (parsedItems.isNotEmpty()) {
                            val allSelected = parsedItems.all { it.isSelected }
                            TextButton(
                                onClick = {
                                    val target = !allSelected
                                    for (i in parsedItems.indices) {
                                        parsedItems[i] = parsedItems[i].copy(isSelected = target)
                                    }
                                }
                            ) {
                                Text(if (allSelected) "Deselect All" else "Select All", style = MaterialTheme.typography.labelSmall)
                            }

                            IconButton(onClick = { scanInbox() }, modifier = Modifier.size(32.dp)) {
                                Icon(Icons.Default.Refresh, contentDescription = "Refresh", modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // List of Parsed Items
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    itemsIndexed(parsedItems) { index, item ->
                        ParsedTransactionCard(
                            item = item,
                            onToggleSelect = {
                                parsedItems[index] = item.copy(isSelected = !item.isSelected)
                            },
                            onCategoryChange = { newCat ->
                                parsedItems[index] = item.copy(category = newCat)
                            },
                            onTypeToggle = {
                                val nextType = if (item.type == "spent") "earned" else "spent"
                                val nextCategoryList = if (nextType == "spent") {
                                    listOf(
                                        "Food", "Grocery", "Shopping", "Bills", "Medical",
                                        "Entertainment", "Investment", "Travel", "Rent", "Education", "Other"
                                    )
                                } else {
                                    listOf(
                                        "Salary", "Freelance", "Investment Return", "Dividend",
                                        "Cashback and Rewards", "Gifts", "Refund", "Other"
                                    )
                                }
                                val currentNormalized = if (item.category.equals("Other Expense", ignoreCase = true) || item.category.equals("Other Income", ignoreCase = true)) {
                                    "Other"
                                } else {
                                    item.category
                                }
                                val updatedCategory = if (nextCategoryList.any { it.equals(currentNormalized, ignoreCase = true) }) {
                                    currentNormalized
                                } else {
                                    if (nextType == "earned") "Salary" else "Food"
                                }
                                parsedItems[index] = item.copy(type = nextType, category = updatedCategory)
                            },
                            onMerchantChange = { newMerchant ->
                                parsedItems[index] = item.copy(merchant = newMerchant)
                            },
                            availableCategories = categories
                        )
                    }
                }

            } else {
                // Tab 1: Manual Paste
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    Text(
                        text = "Paste any Bank SMS alert text below:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = rawSmsInput,
                        onValueChange = {
                            rawSmsInput = it
                            parseError = null
                        },
                        placeholder = {
                            Text("e.g. INR 1,250.00 debited from A/c XX4021 on 06-Sep-26 towards Zomato. Avl Bal: INR 45,000")
                        },
                        minLines = 4,
                        maxLines = 6,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_raw_sms"),
                        shape = RoundedCornerShape(14.dp)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        TextButton(
                            onClick = {
                                rawSmsInput = "Dear Customer, INR 1,850.00 debited from A/c XX4021 on 06-Sep-26 towards ZOMATO. Avl Bal: INR 42,100.00 - HDFC Bank"
                            }
                        ) {
                            Text("Paste Example SMS", style = MaterialTheme.typography.labelSmall)
                        }

                        Button(
                            onClick = {
                                val parsed = SmsReaderUtil.parseSms(rawSmsInput)
                                if (parsed != null) {
                                    parsedItems.add(0, parsed)
                                    selectedTab = 0
                                    rawSmsInput = ""
                                    parseError = null
                                } else {
                                    parseError = "Could not detect transaction amount or keywords. Please check format."
                                }
                            },
                            enabled = rawSmsInput.isNotBlank(),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.testTag("btn_parse_raw_sms")
                        ) {
                            Text("Parse & Add")
                        }
                    }

                    if (parseError != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = parseError!!,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Tips for bank SMS:",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "• Works with all Indian banks (HDFC, SBI, ICICI, Axis, Kotak, etc.)\n• Automatically detects UPI, debit cards, salary, and credits\n• Never captures personal balances as expenses",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Bottom Confirm Import Button
            val selectedCount = parsedItems.count { it.isSelected }
            Button(
                onClick = {
                    onImportConfirmed(parsedItems.filter { it.isSelected })
                    onDismiss()
                },
                enabled = selectedCount > 0,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("btn_confirm_import_sms"),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text(
                    text = "Import $selectedCount Transactions",
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
    }
}

@Composable
private fun ParsedTransactionCard(
    item: ParsedSmsTransaction,
    onToggleSelect: () -> Unit,
    onCategoryChange: (String) -> Unit,
    onTypeToggle: () -> Unit,
    onMerchantChange: (String) -> Unit = {},
    availableCategories: List<CategoryEntity> = emptyList()
) {
    var isExpanded by remember { mutableStateOf(false) }
    val isCredit = item.type == "earned"
    val color = if (isCredit) Color(0xFF0284C7) else Color(0xFFF43F5E)

    var showCategoryMenu by remember { mutableStateOf(false) }

    val displayCategory = if (item.category.equals("Other Expense", ignoreCase = true) || item.category.equals("Other Income", ignoreCase = true)) {
        "Other"
    } else {
        item.category
    }

    val typeCategories = remember(item.type, availableCategories) {
        if (item.type == "spent") {
            val standard = listOf(
                "Food", "Grocery", "Shopping", "Bills", "Medical",
                "Entertainment", "Investment", "Travel", "Rent", "Education"
            )
            val custom = availableCategories
                .filter { it.type == "spent" }
                .map { it.name }
                .filterNot { name ->
                    name.equals("Other Expense", ignoreCase = true) ||
                    name.equals("Other Income", ignoreCase = true) ||
                    name.equals("Other", ignoreCase = true) ||
                    standard.any { it.equals(name, ignoreCase = true) }
                }
            standard + custom + listOf("Other")
        } else {
            val standard = listOf(
                "Salary", "Freelance", "Investment Return", "Dividend",
                "Cashback and Rewards", "Gifts", "Refund"
            )
            val custom = availableCategories
                .filter { it.type == "earned" }
                .map { it.name }
                .filterNot { name ->
                    name.equals("Other Expense", ignoreCase = true) ||
                    name.equals("Other Income", ignoreCase = true) ||
                    name.equals("Other", ignoreCase = true) ||
                    standard.any { it.equals(name, ignoreCase = true) }
                }
            standard + custom + listOf("Other")
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .border(
                1.dp,
                if (item.isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.5f) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
                RoundedCornerShape(14.dp)
            )
            .clickable { isExpanded = !isExpanded }
            .animateContentSize()
            .testTag("parsed_sms_card_${item.id}"),
        colors = CardDefaults.cardColors(
            containerColor = if (item.isSelected) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .creativeCardBackground(
                    patternType = CardPatternType.BankGuilloche,
                    accentColor = if (item.type == "spent") Rose600 else Emerald600
                )
                .padding(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = item.isSelected,
                    onCheckedChange = { onToggleSelect() },
                    colors = CheckboxDefaults.colors(checkedColor = MaterialTheme.colorScheme.primary)
                )

                Spacer(modifier = Modifier.width(6.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = item.merchant,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )

                        Spacer(modifier = Modifier.width(6.dp))

                        Text(
                            text = CurrencyFormatter.formatInrSigned(item.amount, item.type),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = color
                        )

                        Spacer(modifier = Modifier.width(4.dp))

                        Icon(
                            imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                            contentDescription = if (isExpanded) "Collapse SMS details" else "Show full SMS message",
                            modifier = Modifier.size(20.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // Type Badge (clickable to toggle spent/earned)
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(color.copy(alpha = 0.15f))
                                .clickable { onTypeToggle() }
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = if (isCredit) Icons.Default.ArrowDownward else Icons.Default.ArrowUpward,
                                    contentDescription = null,
                                    modifier = Modifier.size(10.dp),
                                    tint = color
                                )
                                Spacer(modifier = Modifier.width(2.dp))
                                Text(
                                    text = if (isCredit) "Earned" else "Spent",
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                    fontWeight = FontWeight.Bold,
                                    color = color
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(6.dp))

                        // Category Selector Box
                        Box {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                                    .clickable { showCategoryMenu = true }
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = displayCategory,
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            DropdownMenu(
                                expanded = showCategoryMenu,
                                onDismissRequest = { showCategoryMenu = false }
                            ) {
                                for (cat in typeCategories) {
                                    DropdownMenuItem(
                                        text = {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                val catColor = CategoryHelper.getCategoryColor(cat)
                                                Box(
                                                    modifier = Modifier
                                                        .size(8.dp)
                                                        .clip(CircleShape)
                                                        .background(catColor)
                                                )
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text(
                                                    text = cat,
                                                    style = MaterialTheme.typography.bodySmall,
                                                    fontWeight = if (cat.equals(displayCategory, ignoreCase = true)) FontWeight.Bold else FontWeight.Normal
                                                )
                                            }
                                        },
                                        onClick = {
                                            onCategoryChange(cat)
                                            showCategoryMenu = false
                                        }
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.weight(1f))

                        Text(
                            text = DateUtils.formatDateShort(item.date),
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    }
                }
            }

            // Expanded view showing complete original SMS message
            AnimatedVisibility(visible = isExpanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp, start = 4.dp, end = 4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Editable Remark / Merchant field
                    OutlinedTextField(
                        value = item.merchant,
                        onValueChange = onMerchantChange,
                        label = { Text("Remark / Merchant Name") },
                        placeholder = { Text("e.g. UPI/Garhwal Paneer") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_parsed_merchant_${item.id}")
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Sms,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Complete Bank SMS",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        Text(
                            text = "Tap to collapse",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f))
                            .border(
                                0.5.dp,
                                MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                                RoundedCornerShape(10.dp)
                            )
                            .padding(10.dp)
                    ) {
                        Text(
                            text = if (item.rawBody.isNotBlank()) item.rawBody else "No original SMS text recorded.",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontSize = 12.sp,
                                lineHeight = 17.sp
                            ),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "Verify the detected amount, merchant, and category above.",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                        modifier = Modifier.padding(start = 2.dp)
                    )
                }
            }
        }
    }
}
