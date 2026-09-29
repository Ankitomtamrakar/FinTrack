package com.example

import com.example.data.local.AssetEntity
import com.example.data.local.ExpenseEntity
import com.example.data.local.MilestoneEntity
import com.example.util.NetWorthTimeframe
import com.example.util.NetWorthTimelineHelper
import org.junit.Assert.*
import org.junit.Test

class ExampleUnitTest {
  @Test
  fun addition_isCorrect() {
    assertEquals(4, 2 + 2)
  }

  @Test
  fun netWorthTimeline_lastPointMatchesCurrentNetWorth() {
    val currentNetWorth = 2450000.0
    val timeline = NetWorthTimelineHelper.generateTimeline(
      currentNetWorth = currentNetWorth,
      assets = listOf(
        AssetEntity(name = "Savings Account", assetType = "Bank Accounts", value = 450000.0),
        AssetEntity(name = "Index Fund", assetType = "Mutual Funds & Equities", value = 2000000.0, investedAmount = 1500000.0)
      ),
      expenses = emptyList(),
      milestones = emptyList(),
      timeframe = NetWorthTimeframe.SIX_MONTHS
    )

    assertTrue("Timeline should not be empty", timeline.isNotEmpty())
    assertEquals(
      "Last point must match current net worth",
      currentNetWorth,
      timeline.last().netWorth,
      0.01
    )
  }

  @Test
  fun netWorthTimeline_supportsAllTimeframes() {
    val currentNetWorth = 1800000.0
    for (tf in NetWorthTimeframe.values()) {
      val timeline = NetWorthTimelineHelper.generateTimeline(
        currentNetWorth = currentNetWorth,
        assets = emptyList(),
        expenses = emptyList(),
        milestones = emptyList(),
        timeframe = tf
      )
      assertTrue("Timeline for ${tf.label} must have at least 2 points", timeline.size >= 2)
      assertEquals(currentNetWorth, timeline.last().netWorth, 0.01)
      // Verify timestamps are in chronological order
      for (i in 0 until timeline.size - 1) {
        assertTrue(timeline[i].timestamp < timeline[i + 1].timestamp)
      }
    }
  }

  @Test
  fun netWorthTimeline_reflectsCashFlowAccurately() {
    val now = System.currentTimeMillis()
    val currentNetWorth = 100000.0
    // User earned 20000 and spent 5000 in the last 5 days
    val expenses = listOf(
      ExpenseEntity(amount = 20000.0, category = "Salary", type = "earned", date = now - 2L * 86400000L),
      ExpenseEntity(amount = 5000.0, category = "Food", type = "spent", date = now - 1L * 86400000L)
    )

    val timeline = NetWorthTimelineHelper.generateTimeline(
      currentNetWorth = currentNetWorth,
      assets = emptyList(),
      expenses = expenses,
      milestones = emptyList(),
      timeframe = NetWorthTimeframe.ONE_MONTH
    )

    assertEquals(currentNetWorth, timeline.last().netWorth, 0.01)
    // First point before those transactions should be current - (20000 - 5000) = 85000
    assertEquals(85000.0, timeline.first().netWorth, 0.01)
  }
}

