package com.example.util

import com.example.data.local.AssetEntity
import com.example.data.local.ExpenseEntity
import com.example.data.local.MilestoneEntity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class NetWorthTimeframe(val label: String, val title: String) {
    ONE_MONTH("1M", "1 Month"),
    THREE_MONTHS("3M", "3 Months"),
    SIX_MONTHS("6M", "6 Months"),
    ONE_YEAR("1Y", "1 Year"),
    ALL("ALL", "All Time")
}

data class NetWorthPoint(
    val timestamp: Long,
    val dateLabel: String,
    val fullDateLabel: String,
    val netWorth: Double
)

private data class TimeframeConfig(
    val pointCount: Int,
    val stepMillis: Long,
    val dateFormat: SimpleDateFormat,
    val fullDateFormat: SimpleDateFormat
)

object NetWorthTimelineHelper {

    fun generateTimeline(
        currentNetWorth: Double,
        assets: List<AssetEntity>,
        expenses: List<ExpenseEntity>,
        milestones: List<MilestoneEntity>,
        timeframe: NetWorthTimeframe
    ): List<NetWorthPoint> {
        val now = System.currentTimeMillis()
        val fullDateFormat = SimpleDateFormat("dd MMMM yyyy", Locale.getDefault())

        val config = when (timeframe) {
            NetWorthTimeframe.ONE_MONTH -> {
                val step = (30L * 86400000L) / 4
                TimeframeConfig(5, step, SimpleDateFormat("dd MMM", Locale.getDefault()), fullDateFormat)
            }
            NetWorthTimeframe.THREE_MONTHS -> {
                val step = (90L * 86400000L) / 5
                TimeframeConfig(6, step, SimpleDateFormat("dd MMM", Locale.getDefault()), fullDateFormat)
            }
            NetWorthTimeframe.SIX_MONTHS -> {
                val step = (180L * 86400000L) / 5
                TimeframeConfig(6, step, SimpleDateFormat("MMM yy", Locale.getDefault()), fullDateFormat)
            }
            NetWorthTimeframe.ONE_YEAR -> {
                val step = (365L * 86400000L) / 6
                TimeframeConfig(7, step, SimpleDateFormat("MMM", Locale.getDefault()), fullDateFormat)
            }
            NetWorthTimeframe.ALL -> {
                val earliestExpense = expenses.minOfOrNull { it.date } ?: (now - 730L * 86400000L)
                val earliestMilestone = milestones.minOfOrNull { it.timestamp } ?: now
                val earliest = minOf(earliestExpense, earliestMilestone, now - 365L * 86400000L)
                val totalSpan = maxOf(now - earliest, 180L * 86400000L)
                val count = 7
                val step = totalSpan / (count - 1)
                TimeframeConfig(count, step, SimpleDateFormat("MMM yy", Locale.getDefault()), fullDateFormat)
            }
        }

        val timestamps = (0 until config.pointCount).map { i ->
            now - (config.pointCount - 1 - i) * config.stepMillis
        }

        val points = timestamps.mapIndexed { idx, ts ->
            val dateLabel = config.dateFormat.format(Date(ts))
            val fullLabel = config.fullDateFormat.format(Date(ts))

            if (idx == timestamps.lastIndex) {
                NetWorthPoint(ts, dateLabel, fullLabel, currentNetWorth)
            } else {
                // Find milestones that happened around or before this timestamp
                val nearestPastMilestone = milestones
                    .filter { it.timestamp <= ts }
                    .maxByOrNull { it.timestamp }

                // Check cash flow (earned - spent) between ts and now
                val cashFlowAfter = expenses
                    .filter { it.date in (ts + 1)..now }
                    .sumOf { if (it.type == "earned") it.amount else -it.amount }

                val calculatedFromCashFlow = currentNetWorth - cashFlowAfter

                val pointVal = if (nearestPastMilestone != null && (ts - nearestPastMilestone.timestamp) < config.stepMillis * 2) {
                    val progress = (ts - nearestPastMilestone.timestamp).toDouble() / (now - nearestPastMilestone.timestamp).coerceAtLeast(1L)
                    nearestPastMilestone.netWorth + (currentNetWorth - nearestPastMilestone.netWorth) * progress.coerceIn(0.0, 1.0)
                } else if (cashFlowAfter != 0.0) {
                    calculatedFromCashFlow.coerceAtLeast(0.0)
                } else {
                    // Fallback using portfolio invested baseline vs current value
                    val totalInvested = assets.filter { !it.isLiability }.sumOf { it.investedAmount ?: it.value }
                    val fraction = idx.toDouble() / (timestamps.size - 1)
                    val baseline = if (totalInvested in 1.0..currentNetWorth) totalInvested else currentNetWorth * 0.85
                    baseline + (currentNetWorth - baseline) * (0.85 * fraction + 0.15 * fraction * fraction)
                }

                NetWorthPoint(ts, dateLabel, fullLabel, pointVal.coerceAtLeast(0.0))
            }
        }

        return points
    }
}
