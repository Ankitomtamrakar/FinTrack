package com.example.ui.components

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Today
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.CardPatternType
import com.example.ui.components.creativeCardBackground
import com.example.ui.theme.DarkCard
import com.example.ui.theme.Emerald300
import com.example.ui.theme.Emerald50
import com.example.ui.theme.Emerald600
import com.example.ui.theme.Indigo100
import com.example.ui.theme.Indigo50
import com.example.ui.theme.Indigo500
import com.example.ui.theme.Indigo600
import com.example.ui.theme.Rose300
import com.example.ui.theme.Rose50
import com.example.ui.theme.Rose600
import com.example.ui.theme.Slate200
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate500
import com.example.ui.theme.Slate600
import com.example.ui.theme.Slate900
import com.example.util.CurrencyFormatter
import com.example.util.DateUtils
import kotlin.math.abs

@Composable
fun CalendarMatrix(
    year: Int,
    month: Int,
    dailyNetMap: Map<Long, Double>,
    dailyHasTransactions: Set<Long>,
    selectedDate: Long?,
    onDateSelected: (Long) -> Unit,
    onPrevMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onTodayJump: () -> Unit,
    modifier: Modifier = Modifier
) {
    val monthDays = remember(year, month) {
        DateUtils.buildMonthDays(year, month)
    }

    val isDark = isSystemInDarkTheme()
    val weekdays = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("calendar_matrix_card"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isDark) DarkCard else Color.White
        ),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(if (isDark) Color(0xFF334155).copy(alpha = 0.5f) else Slate200.copy(alpha = 0.5f))),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .creativeCardBackground(CardPatternType.MeshGlow, accentColor = Indigo600, isDark = isDark)
                .padding(14.dp)
        ) {
            // Header Navigator
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(
                    onClick = onPrevMonth,
                    modifier = Modifier.testTag("btn_prev_month")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Previous Month",
                        tint = if (isDark) Slate400 else Slate600
                    )
                }

                Text(
                    text = DateUtils.formatMonthYear(year, month),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = if (isDark) Color.White else Slate900,
                    modifier = Modifier.clickable { onTodayJump() }
                )

                IconButton(
                    onClick = onNextMonth,
                    modifier = Modifier.testTag("btn_next_month")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "Next Month",
                        tint = if (isDark) Slate400 else Slate600
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Weekday Column Labels
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                for (wd in weekdays) {
                    Text(
                        text = wd,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                        fontWeight = FontWeight.Bold,
                        color = if (wd == "Sat" || wd == "Sun") Rose600.copy(alpha = 0.8f) else Slate400
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Calendar Days Grid (rows of 7)
            val rows = monthDays.chunked(7)
            for (row in rows) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 1.dp),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    for (day in row) {
                        val isSelected = selectedDate != null && DateUtils.isSameDay(day.timestamp, selectedDate)
                        val netBalance = dailyNetMap[day.timestamp]
                        val hasTx = dailyHasTransactions.contains(day.timestamp)

                        CalendarDayCell(
                            day = day,
                            netBalance = netBalance,
                            hasTransactions = hasTx,
                            isSelected = isSelected,
                            isDark = isDark,
                            onSelect = { onDateSelected(day.timestamp) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CalendarDayCell(
    day: DateUtils.CalendarDay,
    netBalance: Double?,
    hasTransactions: Boolean,
    isSelected: Boolean,
    isDark: Boolean,
    onSelect: () -> Unit,
    modifier: Modifier = Modifier
) {
    val cellShape = RoundedCornerShape(12.dp)

    val borderModifier = when {
        isSelected -> Modifier.border(2.dp, Indigo600, cellShape)
        day.isToday -> Modifier.border(1.5.dp, Indigo500.copy(alpha = 0.5f), cellShape)
        else -> Modifier
    }

    val backgroundModifier = if (isSelected) {
        Modifier.background(if (isDark) Color(0xFF312E81) else Indigo50, cellShape)
    } else {
        Modifier
    }

    Box(
        modifier = modifier
            .padding(1.dp)
            .clip(cellShape)
            .then(borderModifier)
            .then(backgroundModifier)
            .clickable { onSelect() }
            .padding(vertical = 2.dp, horizontal = 1.dp),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Day Number Badge
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .then(
                        if (day.isToday) Modifier.background(Indigo600, CircleShape)
                        else Modifier
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = day.dayOfMonth.toString(),
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = 14.5.sp,
                        lineHeight = 16.sp
                    ),
                    fontWeight = if (day.isToday || isSelected) FontWeight.ExtraBold else FontWeight.Bold,
                    color = when {
                        day.isToday -> Color.White
                        day.isCurrentMonth -> if (isDark) Color.White else Slate900
                        else -> Slate400.copy(alpha = 0.4f)
                    },
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(2.dp))

            // Net Balance Pill
            if (hasTransactions && netBalance != null) {
                val isNegative = netBalance < 0
                val isPositive = netBalance > 0

                val pillBgColor = when {
                    isNegative -> if (isDark) Color(0xFF4C0519).copy(alpha = 0.5f) else Rose50
                    isPositive -> if (isDark) Color(0xFF064E3B).copy(alpha = 0.5f) else Emerald50
                    else -> if (isDark) Color(0xFF1E293B) else Slate200.copy(alpha = 0.6f)
                }

                val pillTextColor = when {
                    isNegative -> if (isDark) Rose300 else Rose600
                    isPositive -> if (isDark) Emerald300 else Emerald600
                    else -> if (isDark) Slate400 else Slate600
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(pillBgColor)
                        .padding(horizontal = 3.dp, vertical = 1.5.dp)
                ) {
                    val formatted = when {
                        isNegative -> "-${CurrencyFormatter.formatCompactInr(abs(netBalance)).replace("₹", "")}"
                        isPositive -> "+${CurrencyFormatter.formatCompactInr(netBalance).replace("₹", "")}"
                        else -> "₹0"
                    }
                    Text(
                        text = formatted,
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.5.sp),
                        fontWeight = FontWeight.Bold,
                        color = pillTextColor,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            } else {
                Spacer(modifier = Modifier.height(14.dp))
            }
        }
    }
}
