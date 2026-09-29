package com.example.util

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Flight
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocalAtm
import androidx.compose.material.icons.filled.LocalCafe
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material.icons.filled.LocalGroceryStore
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Redeem
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Work
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

object CategoryHelper {

    val PALETTE_COLORS = listOf(
        Color(0xFFEF4444), // Red
        Color(0xFFEA580C), // Orange
        Color(0xFFEC4899), // Pink
        Color(0xFF2563EB), // Blue
        Color(0xFF0284C7), // Sky
        Color(0xFF059669), // Emerald
        Color(0xFF0D9488), // Teal
        Color(0xFF6366F1), // Indigo
        Color(0xFF8B5CF6), // Purple
        Color(0xFFE11D48), // Rose
        Color(0xFF64748B)  // Slate
    )

    fun getCategoryColor(categoryName: String, fallbackHex: String? = null): Color {
        val oldGreensOrYellows = setOf(
            "#10b981", "#059669", "#34d399", "#6ee7b7", "#2dd4bf",
            "#f59e0b", "#fbbf24"
        )
        fallbackHex?.let {
            try {
                if (it.startsWith("#")) {
                    val colorLong = java.lang.Long.parseLong(it.removePrefix("#"), 16)
                    val parsedColor = Color(colorLong or 0x00000000FF000000)
                    // If it's old yellow or green, upgrade it to the new distinct non-green color
                    if (!oldGreensOrYellows.contains(it.lowercase())) {
                        return parsedColor
                    }
                }
            } catch (e: Exception) {
                // Ignore fallback error
            }
        }

        return when (categoryName.lowercase().trim()) {
            "food", "food & dining", "dining", "restaurant" -> Color(0xFFEF4444)
            "grocery", "grocery & mart", "grocery and mart", "groceries", "mart" -> Color(0xFFEA580C)
            "shopping", "clothes", "electronics" -> Color(0xFFEC4899)
            "rent", "house rent", "home rent" -> Color(0xFFD97706)
            "bills", "bills & utilities", "bills and utilities", "utilities", "electricity", "water" -> Color(0xFF2563EB)
            "travel", "travel & fuel", "travel and fuel", "fuel", "commute", "transport" -> Color(0xFF0284C7)
            "medical", "health & medical", "health and medical", "health", "pharmacy" -> Color(0xFF059669)
            "entertainment", "movies", "games", "streaming" -> Color(0xFF8B5CF6)
            "investments", "investment return", "investment returns", "investment", "sip", "stocks", "mutual funds", "returns" -> Color(0xFF6366F1)
            "education", "books", "courses" -> Color(0xFF0D9488)
            "salary", "payroll" -> Color(0xFF0284C7)
            "freelance", "freelance & consulting", "consulting", "business" -> Color(0xFF8B5CF6)
            "dividend", "dividends" -> Color(0xFF10B981)
            "rewards", "cashback and rewards", "cashback & rewards", "cashback" -> Color(0xFFEC4899)
            "gifts", "gift" -> Color(0xFFF43F5E)
            "refund", "refunds" -> Color(0xFFF97316)
            "other income", "other expense", "other" -> Color(0xFF64748B)
            else -> Color(0xFF64748B)
        }
    }

    fun getCategoryIcon(iconName: String?, categoryName: String): ImageVector {
        val key = (iconName ?: categoryName).lowercase().trim()
        return when {
            key.contains("restaurant") || key.contains("food") || key.contains("dining") -> Icons.Default.Restaurant
            key.contains("grocery") || key.contains("mart") || key.contains("basket") -> Icons.Default.LocalGroceryStore
            key.contains("shopping") || key.contains("bag") || key.contains("clothes") -> Icons.Default.ShoppingBag
            key.contains("cart") -> Icons.Default.ShoppingCart
            key.contains("receipt") || key.contains("bill") || key.contains("utility") || key.contains("refund") -> Icons.Default.ReceiptLong
            key.contains("car") || key.contains("travel") || key.contains("fuel") || key.contains("drive") -> Icons.Default.DirectionsCar
            key.contains("gas") || key.contains("petrol") -> Icons.Default.LocalGasStation
            key.contains("flight") || key.contains("plane") -> Icons.Default.Flight
            key.contains("medical") || key.contains("health") || key.contains("doctor") -> Icons.Default.MedicalServices
            key.contains("movie") || key.contains("entertainment") || key.contains("cinema") -> Icons.Default.Movie
            key.contains("invest") || key.contains("trending") || key.contains("stock") -> Icons.Default.TrendingUp
            key.contains("chart") || key.contains("returns") -> Icons.Default.ShowChart
            key.contains("school") || key.contains("education") -> Icons.Default.School
            key.contains("salary") || key.contains("payments") -> Icons.Default.Payments
            key.contains("work") || key.contains("freelance") || key.contains("business") -> Icons.Default.Work
            key.contains("redeem") || key.contains("reward") || key.contains("cashback") || key.contains("gift") -> Icons.Default.Redeem
            key.contains("wallet") -> Icons.Default.AccountBalanceWallet
            key.contains("bank") -> Icons.Default.AccountBalance
            key.contains("atm") -> Icons.Default.LocalAtm
            key.contains("cafe") -> Icons.Default.LocalCafe
            key.contains("home") || key.contains("rent") -> Icons.Default.Home
            key.contains("money") -> Icons.Default.AttachMoney
            else -> Icons.Default.Category
        }
    }

    val ICON_OPTIONS = listOf(
        "restaurant", "shopping_bag", "local_grocery_store", "receipt_long",
        "directions_car", "local_gas_station", "medical_services", "movie",
        "trending_up", "school", "payments", "work", "redeem", "account_balance_wallet",
        "account_balance", "home", "local_cafe", "flight"
    )
}
