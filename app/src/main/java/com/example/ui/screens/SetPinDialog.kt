package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Pin
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.Indigo50
import com.example.ui.theme.Indigo600
import com.example.ui.theme.Slate200
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate500
import com.example.ui.theme.Slate600
import com.example.ui.theme.Slate900

@Composable
fun SetPinDialog(
    onDismiss: () -> Unit,
    onPinSet: (String) -> Unit
) {
    var step by remember { mutableStateOf(1) } // 1 = Enter PIN, 2 = Confirm PIN
    var firstPin by remember { mutableStateOf("") }
    var confirmPin by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val currentPin = if (step == 1) firstPin else confirmPin

    fun onDigitPress(digit: String) {
        if (currentPin.length < 4) {
            val newPin = currentPin + digit
            errorMessage = null
            if (step == 1) {
                firstPin = newPin
                if (newPin.length == 4) {
                    // Move to confirm step
                    step = 2
                }
            } else {
                confirmPin = newPin
                if (newPin.length == 4) {
                    if (newPin == firstPin) {
                        onPinSet(newPin)
                    } else {
                        errorMessage = "PINs do not match. Try again."
                        confirmPin = ""
                    }
                }
            }
        }
    }

    fun onBackspace() {
        errorMessage = null
        if (step == 1) {
            if (firstPin.isNotEmpty()) firstPin = firstPin.dropLast(1)
        } else {
            if (confirmPin.isNotEmpty()) {
                confirmPin = confirmPin.dropLast(1)
            } else {
                step = 1
                firstPin = ""
            }
        }
    }

    val isDark = isSystemInDarkTheme()

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.testTag("set_pin_dialog"),
        shape = RoundedCornerShape(28.dp),
        containerColor = if (isDark) DarkSurface else Color.White,
        title = null,
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (step == 1) "Set 4-Digit PIN" else "Confirm 4-Digit PIN",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (isDark) Color.White else Slate900
                    )
                    IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = if (isDark) Color.White.copy(alpha = 0.7f) else Slate500)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = if (step == 1) "Enter a 4-digit security PIN for FinTrack" else "Re-enter your 4-digit PIN to confirm",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isDark) Slate400 else Slate500
                )

                Spacer(modifier = Modifier.height(20.dp))

                // PIN Dots
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    for (i in 0 until 4) {
                        val isFilled = i < currentPin.length
                        Box(
                            modifier = Modifier
                                .size(18.dp)
                                .clip(CircleShape)
                                .background(if (isFilled) Indigo600 else (if (isDark) DarkSurfaceVariant else Slate200))
                                .border(
                                    width = 1.5.dp,
                                    color = if (isFilled) Indigo600 else (if (isDark) Color(0xFF475569) else Slate400),
                                    shape = CircleShape
                                )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                if (errorMessage != null) {
                    Text(
                        text = errorMessage!!,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.SemiBold
                    )
                } else {
                    Text(
                        text = if (step == 2) "Step 2 of 2" else "Step 1 of 2",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isDark) Slate400 else Slate500
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Keypad
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val rows = listOf(
                        listOf("1", "2", "3"),
                        listOf("4", "5", "6"),
                        listOf("7", "8", "9"),
                        listOf("C", "0", "DEL")
                    )

                    for (row in rows) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            for (key in row) {
                                Box(
                                    modifier = Modifier
                                        .size(56.dp)
                                        .clip(CircleShape)
                                        .background(
                                            when (key) {
                                                "C", "DEL" -> Color.Transparent
                                                else -> if (isDark) DarkSurfaceVariant else Indigo50
                                            }
                                        )
                                        .clickable {
                                            when (key) {
                                                "C" -> {
                                                    if (step == 1) firstPin = "" else confirmPin = ""
                                                    errorMessage = null
                                                }
                                                "DEL" -> onBackspace()
                                                else -> onDigitPress(key)
                                            }
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    when (key) {
                                        "DEL" -> Icon(
                                            imageVector = Icons.AutoMirrored.Filled.Backspace,
                                            contentDescription = "Backspace",
                                            tint = if (isDark) Slate400 else Slate600,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        "C" -> Text(
                                            text = "Clear",
                                            style = MaterialTheme.typography.labelMedium,
                                            color = if (isDark) Slate400 else Slate500,
                                            fontWeight = FontWeight.Bold
                                        )
                                        else -> Text(
                                            text = key,
                                            fontSize = 20.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isDark) Color.White else Slate900
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = if (isDark) Slate400 else Slate500)
            }
        }
    )
}
