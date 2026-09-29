package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "assets")
data class AssetEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val assetType: String, // Bank Accounts, Mutual Funds & Equities, Fixed Deposits, Gold & SGB, Real Estate, Retirement & PF, Cash in Hand, Liabilities & Loans
    val institution: String = "",
    val value: Double,
    val investedAmount: Double? = null,
    val isLiability: Boolean = false, // Subtracted from net worth
    val updatedAt: Long = System.currentTimeMillis()
)
