package com.example.ui.screens

import android.content.Intent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.RadioButtonChecked
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SheetState
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ui.theme.DarkCard
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.Emerald600
import com.example.ui.theme.Indigo50
import com.example.ui.theme.Indigo600
import com.example.ui.theme.PolishWhite
import com.example.ui.theme.Slate200
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate500
import com.example.ui.theme.Slate600
import com.example.ui.theme.Slate900
import com.example.util.SecurityPreferences

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsDialog(
    sheetState: SheetState,
    onDismiss: () -> Unit,
    securityPrefs: SecurityPreferences = SecurityPreferences.getInstance(LocalContext.current),
    onExport: ((json: String, csv: String) -> Unit) -> Unit,
    onImport: (jsonString: String, onResult: (Pair<Int, Int>?, String?) -> Unit) -> Unit,
    onClearAllData: () -> Unit
) {
    val context = LocalContext.current
    var showImportDialog by remember { mutableStateOf(false) }
    var showClearConfirm by remember { mutableStateOf(false) }
    var importStatusMessage by remember { mutableStateOf<String?>(null) }
    var pendingJsonToSave by remember { mutableStateOf<String?>(null) }
    var pendingCsvToSave by remember { mutableStateOf<String?>(null) }

    val createJsonFileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        if (uri != null && pendingJsonToSave != null) {
            try {
                context.contentResolver.openOutputStream(uri)?.use { stream ->
                    stream.write(pendingJsonToSave!!.toByteArray(Charsets.UTF_8))
                }
                importStatusMessage = "JSON Backup saved to storage successfully!"
                Toast.makeText(context, "Backup file saved to storage!", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                importStatusMessage = "Failed to save file: ${e.localizedMessage}"
                Toast.makeText(context, "Error saving file: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
            }
        }
    }

    val createCsvFileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("text/csv")
    ) { uri ->
        if (uri != null && pendingCsvToSave != null) {
            try {
                context.contentResolver.openOutputStream(uri)?.use { stream ->
                    stream.write(pendingCsvToSave!!.toByteArray(Charsets.UTF_8))
                }
                importStatusMessage = "Expenses CSV saved to storage successfully!"
                Toast.makeText(context, "CSV file saved to storage!", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                importStatusMessage = "Failed to save file: ${e.localizedMessage}"
                Toast.makeText(context, "Error saving file: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
            }
        }
    }

    val browseRestoreFileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            try {
                val content = context.contentResolver.openInputStream(uri)?.bufferedReader()?.use { it.readText() }
                if (!content.isNullOrBlank()) {
                    onImport(content) { counts, err ->
                        if (counts != null) {
                            importStatusMessage = "Restored ${counts.first} transactions & ${counts.second} asset holdings from storage!"
                            Toast.makeText(context, "Backup restored successfully!", Toast.LENGTH_LONG).show()
                        } else {
                            importStatusMessage = err ?: "Invalid backup file format."
                            Toast.makeText(context, importStatusMessage, Toast.LENGTH_LONG).show()
                        }
                    }
                } else {
                    importStatusMessage = "The selected file is empty."
                    Toast.makeText(context, "Selected file was empty.", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                importStatusMessage = "Error reading backup file: ${e.localizedMessage}"
                Toast.makeText(context, "Failed to read file: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
            }
        }
    }

    // Security & App Lock State
    var isAppLockEnabled by remember { mutableStateOf(securityPrefs.isAppLockEnabled) }
    var currentLockType by remember { mutableStateOf(securityPrefs.lockType) }
    var hasPinSet by remember { mutableStateOf(securityPrefs.hasPin()) }
    var showSetPinDialog by remember { mutableStateOf(false) }

    val isDark = isSystemInDarkTheme()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        dragHandle = null,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        containerColor = if (isDark) DarkSurface else PolishWhite
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 18.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Settings & Privacy",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = if (isDark) Color.White else Slate900
                )

                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = if (isDark) Color.White.copy(alpha = 0.7f) else Slate500
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // App Lock & Security Section
            Text(
                text = "App Lock & Security",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = if (isDark) Color.White else Slate900
            )

            Spacer(modifier = Modifier.height(10.dp))

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("app_lock_settings_card"),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = if (isDark) DarkCard else Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Main Toggle Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
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
                                    .background(if (isAppLockEnabled) Indigo50 else Slate200.copy(alpha = 0.5f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = null,
                                    tint = if (isAppLockEnabled) Indigo600 else Slate500,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Enable App Lock",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isDark) Color.White else Slate900
                                )
                                Text(
                                    text = if (isAppLockEnabled) "App is protected upon opening" else "Require authentication on app launch",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (isDark) Slate400 else Slate500
                                )
                            }
                        }

                        Switch(
                            checked = isAppLockEnabled,
                            onCheckedChange = { enabled ->
                                isAppLockEnabled = enabled
                                securityPrefs.isAppLockEnabled = enabled
                                if (enabled && currentLockType == SecurityPreferences.LOCK_TYPE_PIN && !hasPinSet) {
                                    showSetPinDialog = true
                                }
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = Indigo600
                            ),
                            modifier = Modifier.testTag("switch_app_lock")
                        )
                    }

                    // Configuration Options (Visible when enabled)
                    AnimatedVisibility(visible = isAppLockEnabled) {
                        Column(modifier = Modifier.padding(top = 16.dp)) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(1.dp)
                                    .background(if (isDark) Color(0xFF334155) else Slate200)
                            )
                            Spacer(modifier = Modifier.height(12.dp))

                            Text(
                                text = "Choose Lock Method:",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = if (isDark) Slate400 else Slate600
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            // Option 1: Biometrics & Screen Lock
                            val isBiometric = currentLockType == SecurityPreferences.LOCK_TYPE_BIOMETRIC
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(if (isBiometric) (if (isDark) Color(0xFF1E1B4B) else Indigo50) else (if (isDark) DarkSurfaceVariant else Color(0xFFFAFAFC)))
                                    .border(
                                        width = 1.dp,
                                        color = if (isBiometric) Indigo600 else (if (isDark) Color(0xFF334155) else Slate200),
                                        shape = RoundedCornerShape(14.dp)
                                    )
                                    .clickable {
                                        currentLockType = SecurityPreferences.LOCK_TYPE_BIOMETRIC
                                        securityPrefs.lockType = SecurityPreferences.LOCK_TYPE_BIOMETRIC
                                    }
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(34.dp)
                                        .background(if (isBiometric) Indigo600 else (if (isDark) Color(0xFF334155) else Slate200), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Fingerprint,
                                        contentDescription = null,
                                        tint = if (isBiometric) Color.White else (if (isDark) Slate400 else Slate600),
                                        modifier = Modifier.size(20.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.width(10.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Biometrics & Screen Lock",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isDark) Color.White else Slate900
                                    )
                                    Text(
                                        text = "Fingerprint, Face unlock, or phone's screen lock",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (isDark) Slate400 else Slate500
                                    )
                                }

                                Icon(
                                    imageVector = if (isBiometric) Icons.Default.RadioButtonChecked else Icons.Default.RadioButtonUnchecked,
                                    contentDescription = null,
                                    tint = if (isBiometric) Indigo600 else Slate400,
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Option 2: 4-Digit PIN
                            val isPinMode = currentLockType == SecurityPreferences.LOCK_TYPE_PIN
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(if (isPinMode) (if (isDark) Color(0xFF1E1B4B) else Indigo50) else (if (isDark) DarkSurfaceVariant else Color(0xFFFAFAFC)))
                                    .border(
                                        width = 1.dp,
                                        color = if (isPinMode) Indigo600 else (if (isDark) Color(0xFF334155) else Slate200),
                                        shape = RoundedCornerShape(14.dp)
                                    )
                                    .clickable {
                                        currentLockType = SecurityPreferences.LOCK_TYPE_PIN
                                        securityPrefs.lockType = SecurityPreferences.LOCK_TYPE_PIN
                                        if (!hasPinSet) {
                                            showSetPinDialog = true
                                        }
                                    }
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(34.dp)
                                        .background(if (isPinMode) Indigo600 else (if (isDark) Color(0xFF334155) else Slate200), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Key,
                                        contentDescription = null,
                                        tint = if (isPinMode) Color.White else (if (isDark) Slate400 else Slate600),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.width(10.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Custom 4-Digit PIN",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isDark) Color.White else Slate900
                                    )
                                    Text(
                                        text = if (hasPinSet) "PIN is active" else "No PIN set yet • Tap to configure",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (hasPinSet) Emerald600 else (if (isDark) Slate400 else Slate500)
                                    )
                                }

                                Icon(
                                    imageVector = if (isPinMode) Icons.Default.RadioButtonChecked else Icons.Default.RadioButtonUnchecked,
                                    contentDescription = null,
                                    tint = if (isPinMode) Indigo600 else Slate400,
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            // PIN Action Button
                            if (isPinMode || hasPinSet) {
                                Spacer(modifier = Modifier.height(10.dp))
                                TextButton(
                                    onClick = { showSetPinDialog = true },
                                    modifier = Modifier.align(Alignment.End)
                                ) {
                                    Text(
                                        text = if (hasPinSet) "Change 4-Digit PIN" else "Set 4-Digit PIN",
                                        color = Indigo600,
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Backup & Restore Section
            Text(
                text = "Backup & Restore",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = if (isDark) Color.White else Slate900
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Save your data backup or restore it from local storage.",
                style = MaterialTheme.typography.bodySmall,
                color = if (isDark) Slate400 else Slate500
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(modifier = Modifier.fillMaxWidth()) {
                Button(
                    onClick = {
                        onExport { json, csv ->
                            pendingJsonToSave = json
                            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmm", Locale.getDefault()).format(Date())
                            createJsonFileLauncher.launch("FinTrack_Backup_$timeStamp.json")
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("btn_export_json"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Indigo600,
                        contentColor = Color.White
                    )
                ) {
                    Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Save JSON")
                }

                Spacer(modifier = Modifier.width(10.dp))

                OutlinedButton(
                    onClick = {
                        onExport { json, csv ->
                            pendingCsvToSave = csv
                            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmm", Locale.getDefault()).format(Date())
                            createCsvFileLauncher.launch("FinTrack_Expenses_$timeStamp.csv")
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("btn_export_csv"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = if (isDark) Color.White else Slate900
                    )
                ) {
                    Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Save CSV")
                }
            }

            // Auxiliary Share Option
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp, bottom = 4.dp),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .clickable {
                            onExport { json, csv ->
                                val sendIntent = Intent().apply {
                                    action = Intent.ACTION_SEND
                                    putExtra(Intent.EXTRA_TEXT, json)
                                    type = "text/plain"
                                    putExtra(Intent.EXTRA_TITLE, "FinTrack_Backup.json")
                                }
                                val shareIntent = Intent.createChooser(sendIntent, "Share JSON Backup")
                                context.startActivity(shareIntent)
                            }
                        }
                        .padding(horizontal = 6.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = if (isDark) Slate400 else Slate600
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Share JSON via apps",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isDark) Slate400 else Slate600
                    )
                }
            }

            Button(
                onClick = {
                    browseRestoreFileLauncher.launch(
                        arrayOf(
                            "application/json",
                            "text/plain",
                            "text/json",
                            "application/octet-stream",
                            "*/*"
                        )
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("btn_import_json"),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Emerald600,
                    contentColor = Color.White
                )
            ) {
                Icon(Icons.Default.Upload, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Restore Backup file", fontWeight = FontWeight.Bold)
            }

            TextButton(
                onClick = { showImportDialog = true },
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                Text(
                    "Or paste JSON text manually",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isDark) Indigo50 else Slate600
                )
            }

            if (importStatusMessage != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = importStatusMessage!!,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Danger Zone
            Text(
                text = "Danger Zone",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.error
            )

            Spacer(modifier = Modifier.height(10.dp))

            Button(
                onClick = { showClearConfirm = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("btn_clear_all_data"),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) {
                Icon(Icons.Default.DeleteForever, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Clear All Data", fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }

    // Set PIN Dialog
    if (showSetPinDialog) {
        SetPinDialog(
            onDismiss = { showSetPinDialog = false },
            onPinSet = { newPin ->
                securityPrefs.userPin = newPin
                hasPinSet = true
                showSetPinDialog = false
            }
        )
    }

    // Import Dialog
    if (showImportDialog) {
        var jsonInput by remember { mutableStateOf("") }
        var importError by remember { mutableStateOf<String?>(null) }

        AlertDialog(
            onDismissRequest = { showImportDialog = false },
            title = { Text("Restore from JSON", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text(
                        text = "Paste the contents of your exported FinTrack JSON backup file:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = jsonInput,
                        onValueChange = {
                            jsonInput = it
                            importError = null
                        },
                        placeholder = { Text("{\n  \"expenses\": [...],\n  \"assets\": [...]\n}") },
                        minLines = 5,
                        maxLines = 8,
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (importError != null) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(text = importError!!, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (jsonInput.isNotBlank()) {
                            onImport(jsonInput) { counts, err ->
                                if (counts != null) {
                                    importStatusMessage = "Successfully imported ${counts.first} transactions and ${counts.second} asset holdings!"
                                    showImportDialog = false
                                } else {
                                    importError = err ?: "Import failed. Please verify JSON format."
                                }
                            }
                        }
                    },
                    enabled = jsonInput.isNotBlank()
                ) {
                    Text("Import")
                }
            },
            dismissButton = {
                TextButton(onClick = { showImportDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Clear Confirmation Dialog
    if (showClearConfirm) {
        AlertDialog(
            onDismissRequest = { showClearConfirm = false },
            title = { Text("Clear All Data?", fontWeight = FontWeight.Bold) },
            text = {
                Text("Are you sure you want to permanently delete all expenses, transactions, and asset holdings? This action cannot be undone.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        onClearAllData()
                        showClearConfirm = false
                        onDismiss()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Delete Everything")
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearConfirm = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
