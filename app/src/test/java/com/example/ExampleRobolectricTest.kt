package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.AssetEntity
import com.example.data.local.ExpenseEntity
import com.example.data.local.FinTrackDao
import com.example.data.local.FinTrackDatabase
import com.example.util.CurrencyFormatter
import com.example.util.DateUtils
import com.example.util.SmsReaderUtil
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    private lateinit var database: FinTrackDatabase
    private lateinit var dao: FinTrackDao

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, FinTrackDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        dao = database.finTrackDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("FinTrack", appName)
    }

    @Test
    fun `currency formatter formats inr correctly`() {
        val formatted = CurrencyFormatter.formatInr(125000.0)
        assertTrue(formatted.contains("1,25,000"))
        assertTrue(formatted.startsWith("₹"))

        val compactLakh = CurrencyFormatter.formatCompactInr(4250000.0)
        assertEquals("₹42.50 L", compactLakh)

        val compactCrore = CurrencyFormatter.formatCompactInr(15000000.0)
        assertEquals("₹1.50 Cr", compactCrore)
    }

    @Test
    fun `sms parser extracts debit transaction correctly`() {
        val smsText = "Dear Customer, INR 749.00 debited from A/c XX4021 on 04-Sep-26 towards SWIGGY. Avl Bal: INR 45,210.00 - HDFC Bank"
        val parsed = SmsReaderUtil.parseSms(smsText)
        assertNotNull(parsed)
        assertEquals(749.0, parsed!!.amount, 0.01)
        assertEquals("spent", parsed.type)
        assertEquals("Food", parsed.category)
        assertTrue(parsed.merchant.contains("SWIGGY", ignoreCase = true))

        // Test AU Bank Credit Card with UPI merchant name
        val auBankSms = "INR 225.00 spent at UPI/Garhwal Paneer on AU Bank Credit Card XX8080 27-09-2026 07:05:41 PM"
        val auParsed = SmsReaderUtil.parseSms(auBankSms)
        assertNotNull(auParsed)
        assertEquals(225.0, auParsed!!.amount, 0.01)
        assertEquals("spent", auParsed.type)
        assertEquals("UPI/Garhwal Paneer", auParsed.merchant)
        assertEquals("Food", auParsed.category)
    }

    @Test
    fun `sms parser extracts credit transaction correctly`() {
        val smsText = "Salary of INR 1,25,000.00 credited to your A/c **8832 on 01-Sep-26 by TECH CORP. Avl Bal: INR 1,68,011.00"
        val parsed = SmsReaderUtil.parseSms(smsText)
        assertNotNull(parsed)
        assertEquals(125000.0, parsed!!.amount, 0.01)
        assertEquals("earned", parsed.type)
        assertEquals("Salary", parsed.category)

        val refundSms = "Dear Customer, INR 499.00 credited to A/c XX4021 on 05-Sep-26 as refund from SWIGGY. Avl Bal: INR 45,709.00"
        val parsedRefund = SmsReaderUtil.parseSms(refundSms)
        assertNotNull(parsedRefund)
        assertEquals(499.0, parsedRefund!!.amount, 0.01)
        assertEquals("earned", parsedRefund.type)
        assertEquals("Refund", parsedRefund.category)
    }

    @Test
    fun `database inserts and queries expenses and assets`() = runBlocking {
        val expense = ExpenseEntity(
            amount = 1500.0,
            type = "spent",
            category = "Grocery",
            date = System.currentTimeMillis(),
            merchant = "Blinkit",
            notes = "Weekly essentials"
        )
        dao.insertExpense(expense)

        val allExpenses = dao.getAllExpenses().first()
        assertEquals(1, allExpenses.size)
        assertEquals("Blinkit", allExpenses[0].merchant)

        val asset = AssetEntity(
            name = "Zerodha Equity Portfolio",
            assetType = "Mutual Funds & Equities",
            institution = "Zerodha",
            value = 500000.0,
            isLiability = false,
            updatedAt = System.currentTimeMillis()
        )
        dao.insertAsset(asset)

        val allAssets = dao.getAllAssets().first()
        assertEquals(1, allAssets.size)
        assertEquals(500000.0, allAssets[0].value, 0.01)
    }

    @Test
    fun `database filters spent and earned transactions`() = runBlocking {
        val spent = ExpenseEntity(
            amount = 850.0,
            type = "spent",
            category = "Food",
            date = System.currentTimeMillis(),
            merchant = "Zomato",
            notes = "Dinner"
        )
        val earned = ExpenseEntity(
            amount = 50000.0,
            type = "earned",
            category = "Salary",
            date = System.currentTimeMillis(),
            merchant = "Employer",
            notes = "Monthly payroll"
        )
        dao.insertExpense(spent)
        dao.insertExpense(earned)

        val all = dao.getAllExpenses().first()
        val allSpent = all.filter { it.type == "spent" }
        val allEarned = all.filter { it.type == "earned" }

        assertEquals(1, allSpent.size)
        assertEquals(850.0, allSpent[0].amount, 0.01)
        assertEquals(1, allEarned.size)
        assertEquals(50000.0, allEarned[0].amount, 0.01)
    }

    @Test
    fun `asset invested amount and return calculation`() = runBlocking {
        val equityAsset = AssetEntity(
            name = "Nifty 50 Index Fund",
            assetType = "Mutual Funds & Equities",
            institution = "Zerodha",
            value = 600000.0,
            investedAmount = 480000.0,
            isLiability = false,
            updatedAt = System.currentTimeMillis()
        )
        dao.insertAsset(equityAsset)

        val assets = dao.getAllAssets().first()
        val saved = assets.find { it.name == "Nifty 50 Index Fund" }
        org.junit.Assert.assertNotNull(saved)
        assertEquals(480000.0, saved!!.investedAmount ?: 0.0, 0.01)
        val returnPct = ((saved.value - saved.investedAmount!!) / saved.investedAmount!!) * 100.0
        assertEquals(25.0, returnPct, 0.01)
    }

    @Test
    fun `export and import json with invested amount`() = runBlocking {
        val repo = com.example.data.repository.FinTrackRepository(dao)
        val expense = ExpenseEntity(
            id = 1,
            amount = 1200.0,
            type = "spent",
            category = "Dining Out",
            date = System.currentTimeMillis(),
            notes = "Dinner",
            merchant = "Restaurant"
        )
        val asset = AssetEntity(
            id = 1,
            name = "Index Fund",
            assetType = "Mutual Funds & Equities",
            institution = "Groww",
            value = 50000.0,
            investedAmount = 40000.0,
            isLiability = false,
            updatedAt = System.currentTimeMillis()
        )
        val json = repo.exportToJson(listOf(expense), listOf(asset))
        org.junit.Assert.assertTrue(json.contains("\"investedAmount\": 40000"))

        val counts = repo.importFromJson(json)
        assertEquals(1, counts.first)
        assertEquals(1, counts.second)

        val importedAssets = dao.getAllAssets().first()
        val found = importedAssets.find { it.name == "Index Fund" }
        org.junit.Assert.assertNotNull(found)
        assertEquals(40000.0, found!!.investedAmount ?: 0.0, 0.01)
    }
}
