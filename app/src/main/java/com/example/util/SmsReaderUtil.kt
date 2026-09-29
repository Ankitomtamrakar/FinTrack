package com.example.util

import android.content.Context
import android.net.Uri
import java.util.UUID
import java.util.regex.Pattern

data class ParsedSmsTransaction(
    val id: String = UUID.randomUUID().toString(),
    var amount: Double,
    var type: String, // "spent" or "earned"
    var category: String,
    var merchant: String,
    var date: Long,
    val rawBody: String,
    var isSelected: Boolean = true
)

object SmsReaderUtil {

    private val BANKING_KEYWORDS = listOf(
        "debited", "credited", "spent", "paid", "transferred", "withdrawn",
        "a/c", "acct", "inr", "rs.", "rs ", "₹", "bank", "upi", "vpa",
        "purchase", "txn", "refund", "salary", "atm", "pos", "card"
    )

    private val AMOUNT_PATTERNS = listOf(
        // e.g. "INR 1,250.00", "Rs. 450", "₹500.50", "Rs 1500"
        Pattern.compile("""(?:(?:rs\.?|inr|₹)\s*([\d,]+(?:\.\d{1,2})?))""", Pattern.CASE_INSENSITIVE),
        // e.g. "1,250.00 INR", "500 rs"
        Pattern.compile("""([\d,]+(?:\.\d{1,2})?)\s*(?:rs\.?|inr|₹)""", Pattern.CASE_INSENSITIVE),
        // e.g. "for INR 500", "for Rs 200"
        Pattern.compile("""(?:for|of)\s+(?:inr|rs\.?|₹)?\s*([\d,]+(?:\.\d{1,2})?)""", Pattern.CASE_INSENSITIVE)
    )

    private val SPENT_MERCHANT_PATTERNS = listOf(
        // 1. Explicit action followed by at: "spent at UPI/Garhwal Paneer", "paid at <merchant>", "done at <merchant>", "purchase at <merchant>"
        Pattern.compile("""(?:spent\s+at|paid\s+at|done\s+at|purchase\s+at|bought\s+at|transacted\s+at|txn\s+at)\s+([A-Za-z0-9\s\.\@\-\&\/\'\*\+]{2,40}?)(?=\s+(?:on\b|ref\b|dated\b|dt\b|via\b|using\b|card\b|credit\b|debit\b|avl\b|bal\b|balance\b|\.|\,|\;|\n|$))""", Pattern.CASE_INSENSITIVE),
        // 2. Spent on [Card] at <merchant>: e.g. "spent on AU Bank Card ending 1234 at SWIGGY"
        Pattern.compile("""(?:spent\s+on\s+[^\n\r]+?\s+at|paid\s+on\s+[^\n\r]+?\s+at)\s+([A-Za-z0-9\s\.\@\-\&\/\'\*\+]{2,40}?)(?=\s+(?:on\b|ref\b|dated\b|dt\b|via\b|using\b|avl\b|bal\b|\.|\,|\;|\n|$))""", Pattern.CASE_INSENSITIVE),
        // 3. Towards / Paid to / Transferred to / Sent to: "towards ZOMATO", "paid to UBER INDIA", "transfer to SWIGGY"
        Pattern.compile("""(?:towards|paid\s+to|transfer(?:red)?\s+to|sent\s+to)\s+(?:vpa\s+)?([A-Za-z0-9\s\.\@\-\&\/\'\*\+]{2,40}?)(?=\s+(?:on\b|ref\b|dated\b|dt\b|via\b|using\b|card\b|credit\b|debit\b|ref\s+no|avl\b|bal\b|\.|\,|\;|\n|$))""", Pattern.CASE_INSENSITIVE),
        // 4. Standalone "at <merchant>" (e.g. "at UPI/Garhwal Paneer on AU Bank", "at BLINKIT.")
        Pattern.compile("""\bat\s+([A-Za-z0-9\s\.\@\-\&\/\'\*\+]{2,40}?)(?=\s+(?:on\b|ref\b|dated\b|dt\b|via\b|using\b|card\b|credit\b|debit\b|avl\b|bal\b|balance\b|upi\s+ref|\.|\,|\;|\n|$))""", Pattern.CASE_INSENSITIVE),
        // 5. "debited for <merchant>" (e.g. "debited for AIRTEL BROADBAND")
        Pattern.compile("""(?:debited\s+for)\s+([A-Za-z0-9\s\.\@\-\&\/\'\*\+]{2,40}?)(?=\s+(?:on\b|ref\b|dated\b|dt\b|via\b|using\b|card\b|credit\b|debit\b|avl\b|bal\b|\.|\,|\;|\n|$))""", Pattern.CASE_INSENSITIVE),
        // 6. UPI / VPA identifiers
        Pattern.compile("""(?:vpa|upi\s*id)\s*[:\-]?\s*([A-Za-z0-9\.\@\-\/]{3,35})""", Pattern.CASE_INSENSITIVE),
        Pattern.compile("""(?:info|upi)\s*[:\/]\s*(?:(?:p2m|p2p|[0-9]+)\/)?([A-Za-z0-9\s\.\@\-\&]{2,30}?)(?:\/|\.|\,|\;|\n|$)""", Pattern.CASE_INSENSITIVE),
        // 7. Generic "to <merchant>" (must be non-account)
        Pattern.compile("""\bto\s+([A-Za-z0-9\s\.\@\-\&\/\'\*\+]{2,40}?)(?=\s+(?:on\b|ref\b|dated\b|dt\b|via\b|using\b|card\b|credit\b|debit\b|ref\s+no|avl\b|bal\b|\.|\,|\;|\n|$))""", Pattern.CASE_INSENSITIVE)
    )

    private val EARNED_MERCHANT_PATTERNS = listOf(
        // 1. Refund / Cashback from <merchant>
        Pattern.compile("""(?:refund\s+(?:of\s+[^f\n]+?\s+)?from|cashback\s+(?:of\s+[^f\n]+?\s+)?from)\s+([A-Za-z0-9\s\.\@\-\&\/\'\*\+]{2,40}?)(?=\s+(?:on\b|ref\b|dated\b|dt\b|via\b|using\b|a\/c|account|\.|\,|\;|\n|$))""", Pattern.CASE_INSENSITIVE),
        // 2. Credited by / from, Received from, Deposited by
        Pattern.compile("""(?:credited\s+(?:by|from)|received\s+from|deposited\s+by)\s+([A-Za-z0-9\s\.\@\-\&\/\'\*\+]{2,40}?)(?=\s+(?:on\b|ref\b|dated\b|dt\b|via\b|using\b|a\/c|account|avl\b|bal\b|\.|\,|\;|\n|$))""", Pattern.CASE_INSENSITIVE),
        // 3. Salary / payroll / payment by / from
        Pattern.compile("""(?:salary|payroll|payment)\s+(?:of\s+[^\n]+?\s+)?(?:by|from)\s+([A-Za-z0-9\s\.\@\-\&\/\'\*\+]{2,40}?)(?=\s+(?:on\b|ref\b|dated\b|dt\b|via\b|using\b|avl\b|bal\b|\.|\,|\;|\n|$))""", Pattern.CASE_INSENSITIVE),
        // 4. Standalone "by <sender>" in credit context
        Pattern.compile("""\bby\s+([A-Za-z0-9\s\.\@\-\&\/\'\*\+]{2,40}?)(?=\s+(?:on\b|ref\b|dated\b|dt\b|via\b|using\b|avl\b|bal\b|\.|\,|\;|\n|$))""", Pattern.CASE_INSENSITIVE),
        // 5. Generic "from <sender>" in credit context
        Pattern.compile("""\bfrom\s+([A-Za-z0-9\s\.\@\-\&\/\'\*\+]{2,40}?)(?=\s+(?:on\b|ref\b|dated\b|dt\b|via\b|using\b|a\/c|account|avl\b|bal\b|\.|\,|\;|\n|$))""", Pattern.CASE_INSENSITIVE)
    )

    fun cleanMerchantCandidate(raw: String): String {
        var s = raw.trim()
        // Strip leading punctuation
        s = s.replace(Regex("""^[,\-:\.\s]+"""), "")
        // Strip trailing punctuation
        s = s.replace(Regex("""[,\-:\.\s]+$"""), "")
        // Strip trailing preposition tokens
        s = s.replace(Regex("""(?i)\s+(?:on|via|ref|dated|dt|using|at|for)$"""), "").trim()

        val lower = s.lowercase()
        val invalidTokens = setOf(
            "your", "your a/c", "your account", "a/c", "acct", "account",
            "card", "bank", "credit card", "debit card", "my", "self",
            "inr", "rs", "avl bal", "bal", "balance", "upi", "txn", "transfer",
            "not you", "call", "help"
        )
        if (lower in invalidTokens) return ""

        // Reject if it's user's account or card reference
        if (Regex("""^(?i)(?:your\s+)?(?:a\/c|account|acct|card|credit\s+card|debit\s+card)\b""").containsMatchIn(s)) {
            return ""
        }
        if (Regex("""^(?i)(?:xx+|\*+)\d+""").containsMatchIn(s)) {
            return ""
        }
        if (s.length < 2) return ""

        return s.take(35).trim()
    }

    fun extractMerchant(body: String, type: String): String {
        val patterns = if (type == "earned") EARNED_MERCHANT_PATTERNS else SPENT_MERCHANT_PATTERNS
        for (pattern in patterns) {
            val matcher = pattern.matcher(body)
            if (matcher.find()) {
                val rawCand = matcher.group(1)?.trim() ?: ""
                val clean = cleanMerchantCandidate(rawCand)
                if (clean.isNotBlank()) {
                    return clean
                }
            }
        }
        return ""
    }

    fun readRecentBankingSms(context: Context, limit: Int = 40): List<ParsedSmsTransaction> {
        val parsedList = mutableListOf<ParsedSmsTransaction>()
        val uri = Uri.parse("content://sms/inbox")
        val projection = arrayOf("_id", "address", "body", "date")

        try {
            val cursor = context.contentResolver.query(
                uri,
                projection,
                null,
                null,
                "date DESC LIMIT $limit"
            )

            cursor?.use {
                val bodyIdx = it.getColumnIndexOrThrow("body")
                val dateIdx = it.getColumnIndexOrThrow("date")

                while (it.moveToNext()) {
                    val body = it.getString(bodyIdx) ?: continue
                    val date = it.getLong(dateIdx)

                    val parsed = parseSms(body, date)
                    if (parsed != null) {
                        parsedList.add(parsed)
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return parsedList
    }

    fun parseSms(body: String, date: Long = System.currentTimeMillis()): ParsedSmsTransaction? {
        val lower = body.lowercase()

        // 1. Must match at least two banking/financial terms or specific strong trigger
        var keywordHits = 0
        for (kw in BANKING_KEYWORDS) {
            if (lower.contains(kw)) keywordHits++
        }
        if (keywordHits < 2 && !lower.contains("debited") && !lower.contains("credited")) {
            return null
        }

        // 2. Determine type (Spent vs Earned)
        val isCredit = lower.contains("credited") ||
                lower.contains("received") ||
                lower.contains("deposited") ||
                lower.contains("refund") ||
                lower.contains("cashback") ||
                (lower.contains("cr to") || lower.contains("added to"))

        val isDebit = lower.contains("debited") ||
                lower.contains("spent") ||
                lower.contains("paid") ||
                lower.contains("sent") ||
                lower.contains("withdrawn") ||
                lower.contains("purchase") ||
                lower.contains("dr from") ||
                lower.contains("charge")

        if (!isCredit && !isDebit) {
            return null
        }
        val type = if (isCredit) "earned" else "spent"

        // 3. Extract amount
        var extractedAmount: Double? = null

        // Remove balance strings first to avoid misidentifying avl bal as spent
        val cleanBody = body
            .replace(Regex("""(?:bal|balance|avl bal|credit limit)[^\d]{0,10}[\d,]+(?:\.\d{1,2})?""", RegexOption.IGNORE_CASE), "")

        for (p in AMOUNT_PATTERNS) {
            val matcher = p.matcher(cleanBody)
            if (matcher.find()) {
                val groupVal = (1..matcher.groupCount())
                    .firstNotNullOfOrNull { matcher.group(it) }
                    ?.replace(",", "")
                    ?.trim()
                val parsed = groupVal?.toDoubleOrNull()
                if (parsed != null && parsed > 0.0) {
                    extractedAmount = parsed
                    break
                }
            }
        }

        if (extractedAmount == null || extractedAmount <= 0.0) {
            return null
        }

        // 4. Extract Merchant using comprehensive banking rules
        var merchant = extractMerchant(body, type)
        if (merchant.isBlank()) {
            merchant = if (type == "spent") "Merchant / Store" else "Bank / Remitter"
        }

        // 5. Categorize based on merchant & keywords
        val category = categorize(merchant, lower, type)

        return ParsedSmsTransaction(
            amount = extractedAmount,
            type = type,
            category = category,
            merchant = merchant,
            date = date,
            rawBody = body
        )
    }

    private fun categorize(merchant: String, bodyLower: String, type: String): String {
        if (type == "earned") {
            return when {
                bodyLower.contains("salary") || bodyLower.contains("payroll") -> "Salary"
                bodyLower.contains("dividend") -> "Dividend"
                bodyLower.contains("interest") || bodyLower.contains("returns") -> "Investment Return"
                bodyLower.contains("refund") || bodyLower.contains("reversed") -> "Refund"
                bodyLower.contains("gift") || bodyLower.contains("shagun") -> "Gifts"
                bodyLower.contains("cashback") || bodyLower.contains("reward") -> "Cashback and Rewards"
                bodyLower.contains("freelance") || bodyLower.contains("consult") || bodyLower.contains("client") -> "Freelance"
                else -> "Other"
            }
        }

        val text = "$merchant $bodyLower".lowercase()
        return when {
            text.contains("rent") || text.contains("house rent") || text.contains("nobroker") -> "Rent"

            text.contains("swiggy") || text.contains("zomato") || text.contains("mcdonald") ||
            text.contains("starbucks") || text.contains("kfc") || text.contains("dominos") ||
            text.contains("burger king") || text.contains("pizza") || text.contains("restaurant") ||
            text.contains("cafe") || text.contains("dineout") || text.contains("eatclub") ||
            text.contains("paneer") || text.contains("dairy") || text.contains("bakery") ||
            text.contains("sweets") || text.contains("dhaba") || text.contains("kitchen") -> "Food"

            text.contains("blinkit") || text.contains("zepto") || text.contains("instamart") ||
            text.contains("bigbasket") || text.contains("dmart") || text.contains("grocer") ||
            text.contains("spencers") || text.contains("nature's basket") || text.contains("supermarket") -> "Grocery"

            text.contains("amazon") || text.contains("flipkart") || text.contains("myntra") ||
            text.contains("zara") || text.contains("ajio") || text.contains("nykaa") ||
            text.contains("h&m") || text.contains("meesho") || text.contains("uniqlo") ||
            text.contains("mall") || text.contains("shopping") -> "Shopping"

            text.contains("bescom") || text.contains("adani") || text.contains("electricity") ||
            text.contains("airtel") || text.contains("jio") || text.contains("vodafone") ||
            text.contains("water") || text.contains("gas") || text.contains("broadband") ||
            text.contains("fibernet") || text.contains("recharge") || text.contains("billdesk") -> "Bills"

            text.contains("uber") || text.contains("ola") || text.contains("rapido") ||
            text.contains("metro") || text.contains("petrol") || text.contains("fuel") ||
            text.contains("indianoil") || text.contains("hpcl") || text.contains("bpcl") ||
            text.contains("shell") || text.contains("irctc") || text.contains("fastag") ||
            text.contains("makemytrip") || text.contains("cleartrip") || text.contains("indigo") -> "Travel"

            text.contains("apollo") || text.contains("pharmeasy") || text.contains("1mg") ||
            text.contains("netmeds") || text.contains("hospital") || text.contains("clinic") ||
            text.contains("medplus") || text.contains("pharmacy") || text.contains("practo") ||
            text.contains("medical") -> "Medical"

            text.contains("netflix") || text.contains("spotify") || text.contains("prime") ||
            text.contains("pvr") || text.contains("inox") || text.contains("bookmyshow") ||
            text.contains("hotstar") || text.contains("sonyliv") || text.contains("steam") ||
            text.contains("movie") || text.contains("cinema") -> "Entertainment"

            text.contains("zerodha") || text.contains("groww") || text.contains("indmoney") ||
            text.contains("kuvera") || text.contains("upstox") || text.contains("mutual") ||
            text.contains("sip") || text.contains("nps") || text.contains("coin") -> "Investment"

            else -> "Other"
        }
    }
}
