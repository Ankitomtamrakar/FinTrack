package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SheetState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.data.local.AssetEntity

val ASSET_CLASSES = listOf(
    "Bank Accounts",
    "Mutual Funds & Equities",
    "Fixed Deposits",
    "Gold & SGBs",
    "Real Estate",
    "Retirement & PF",
    "Cash in Hand",
    "Liabilities & Loans"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditAssetSheet(
    existingAsset: AssetEntity?,
    sheetState: SheetState,
    onDismiss: () -> Unit,
    onSave: (name: String, assetType: String, value: Double, investedAmount: Double?, isLiability: Boolean) -> Unit,
    onDelete: ((AssetEntity) -> Unit)? = null
) {
    var name by remember { mutableStateOf(existingAsset?.name ?: "") }
    var assetType by remember { mutableStateOf(existingAsset?.assetType ?: "Mutual Funds & Equities") }
    var valueText by remember {
        mutableStateOf(
            if (existingAsset != null) {
                if (existingAsset.value % 1.0 == 0.0) existingAsset.value.toLong().toString()
                else existingAsset.value.toString()
            } else ""
        )
    }
    var investedAmountText by remember {
        mutableStateOf(
            if (existingAsset?.investedAmount != null) {
                if (existingAsset.investedAmount % 1.0 == 0.0) existingAsset.investedAmount.toLong().toString()
                else existingAsset.investedAmount.toString()
            } else ""
        )
    }
    var isLiability by remember {
        mutableStateOf(existingAsset?.isLiability ?: (assetType == "Liabilities & Loans"))
    }
    var dropdownExpanded by remember { mutableStateOf(false) }

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
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = if (existingAsset == null) "Add Asset / Holding" else "Edit Holding",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                if (existingAsset != null && onDelete != null) {
                    IconButton(
                        onClick = {
                            onDelete(existingAsset)
                            onDismiss()
                        },
                        modifier = Modifier.testTag("btn_delete_asset")
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

            // Asset Type Dropdown
            ExposedDropdownMenuBox(
                expanded = dropdownExpanded,
                onExpandedChange = { dropdownExpanded = !dropdownExpanded }
            ) {
                OutlinedTextField(
                    value = assetType,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Asset Class") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = dropdownExpanded) },
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor()
                )

                ExposedDropdownMenu(
                    expanded = dropdownExpanded,
                    onDismissRequest = { dropdownExpanded = false }
                ) {
                    for (ac in ASSET_CLASSES) {
                        DropdownMenuItem(
                            text = { Text(ac) },
                            onClick = {
                                assetType = ac
                                isLiability = (ac == "Liabilities & Loans")
                                dropdownExpanded = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Holding Name
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Holding / Asset Name (e.g. Nifty 50 Index, Emergency Fund)") },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("input_asset_name")
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Current Valuation
            OutlinedTextField(
                value = valueText,
                onValueChange = { valueText = it.filter { c -> c.isDigit() || c == '.' } },
                label = { Text("Current Valuation (₹)") },
                placeholder = { Text("0.00") },
                leadingIcon = {
                    Text(
                        text = "₹",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = if (isLiability) Color(0xFFF43F5E) else MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                textStyle = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("input_asset_value")
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Invested Amount (Optional / for calculating returns)
            if (!isLiability) {
                OutlinedTextField(
                    value = investedAmountText,
                    onValueChange = { investedAmountText = it.filter { c -> c.isDigit() || c == '.' } },
                    label = { Text("Invested Amount (₹)") },
                    placeholder = { Text("Optional / Buy cost") },
                    leadingIcon = {
                        Text(
                            text = "₹",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_asset_invested")
                )

                // Return % live preview if both valuation and invested amount are present
                val curVal = valueText.toDoubleOrNull()
                val invAmt = investedAmountText.toDoubleOrNull()
                if (curVal != null && invAmt != null && invAmt > 0.0) {
                    val retPct = ((curVal - invAmt) / invAmt) * 100.0
                    val retDiff = curVal - invAmt
                    val isPositive = retPct >= 0.0
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Estimated Return:",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "${if (isPositive) "+" else ""}${String.format(java.util.Locale.getDefault(), "%.1f", retPct)}% (${if (isPositive) "+" else ""}₹${String.format(java.util.Locale.getDefault(), "%,.0f", retDiff)})",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = if (isPositive) Color(0xFF059669) else Color(0xFFEF4444)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
            } else {
                Spacer(modifier = Modifier.height(2.dp))
            }

            // Is Liability Switch
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "This is a Liability (Debt / Loan)",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "Subtracted from your Net Worth",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked = isLiability,
                    onCheckedChange = { isLiability = it }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Save Button
            val isValid = name.isNotBlank() && (valueText.toDoubleOrNull() ?: 0.0) >= 0.0
            Button(
                onClick = {
                    val v = valueText.toDoubleOrNull() ?: 0.0
                    val inv = if (isLiability) null else investedAmountText.toDoubleOrNull()
                    onSave(name.trim(), assetType, v, inv, isLiability)
                    onDismiss()
                },
                enabled = isValid,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("btn_save_asset"),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isLiability) Color(0xFFF43F5E) else MaterialTheme.colorScheme.primary
                )
            ) {
                Text(
                    text = if (existingAsset == null) "Add to Portfolio" else "Update Holding",
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun QuickValuationDialog(
    asset: AssetEntity,
    onDismiss: () -> Unit,
    onUpdateValue: (Double) -> Unit
) {
    var valText by remember {
        mutableStateOf(
            if (asset.value % 1.0 == 0.0) asset.value.toLong().toString() else asset.value.toString()
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text("Quick Update Valuation", fontWeight = FontWeight.Bold)
                Text(
                    text = "${asset.name} (${asset.assetType})",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        text = {
            Column {
                OutlinedTextField(
                    value = valText,
                    onValueChange = { valText = it.filter { c -> c.isDigit() || c == '.' } },
                    label = { Text("Updated Value (₹)") },
                    leadingIcon = {
                        Text(
                            text = "₹",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_quick_valuation")
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val nv = valText.toDoubleOrNull()
                    if (nv != null && nv >= 0.0) {
                        onUpdateValue(nv)
                        onDismiss()
                    }
                }
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
