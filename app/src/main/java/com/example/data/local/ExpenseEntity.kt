package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "expenses")
data class ExpenseEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val amount: Double,
    val type: String, // "spent" or "earned"
    val category: String,
    val date: Long, // Epoch timestamp in milliseconds (start of day or specific time)
    val notes: String = "",
    val merchant: String = "",
    val isFromSMS: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)
