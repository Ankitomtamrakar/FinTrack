package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Money
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.AssetEntity
import com.example.data.local.MilestoneEntity
import com.example.ui.theme.Amber50
import com.example.ui.theme.Amber500
import com.example.ui.theme.DarkCard
import com.example.ui.theme.DarkSurface
import com.example.ui.components.CardPatternType
import com.example.ui.components.NetWorthGrowthCard
import com.example.ui.components.creativeCardBackground
import com.example.ui.theme.Blue50
import com.example.ui.theme.Blue600
import com.example.ui.theme.Emerald50
import com.example.ui.theme.Emerald600
import com.example.ui.theme.Indigo100
import com.example.ui.theme.Indigo50
import com.example.ui.theme.Indigo500
import com.example.ui.theme.Indigo600
import com.example.ui.theme.Indigo700
import com.example.ui.theme.Orange50
import com.example.ui.theme.Orange600
import com.example.ui.theme.Rose50
import com.example.ui.theme.Rose600
import com.example.ui.theme.Slate200
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate500
import com.example.ui.theme.Slate600
import com.example.ui.theme.Slate900
import com.example.ui.viewmodel.WealthMetrics
import com.example.util.CurrencyFormatter
import com.example.util.DateUtils
import com.example.util.NetWorthPoint
import com.example.util.NetWorthTimeframe

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun WealthScreen(
    wealthMetrics: WealthMetrics,
    assets: List<AssetEntity>,
    netWorthTimeline: List<NetWorthPoint> = emptyList(),
    selectedTimeframe: NetWorthTimeframe = NetWorthTimeframe.SIX_MONTHS,
    onTimeframeSelected: (NetWorthTimeframe) -> Unit = {},
    onAddAssetClick: () -> Unit,
    onEditAssetClick: (AssetEntity) -> Unit,
    onQuickUpdateValuation: (AssetEntity, Double) -> Unit,
    modifier: Modifier = Modifier
) {
    var quickUpdateAsset by remember { mutableStateOf<AssetEntity?>(null) }

    val expandedStates = remember {
        mutableStateMapOf<String, Boolean>().apply {
            for (ac in ASSET_CLASSES) {
                this[ac] = false
            }
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("wealth_screen_column"),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 1. Professional Polish Indigo Hero Card
        item {
            NetWorthHeroCard(
                metrics = wealthMetrics,
                onAddHoldingClick = onAddAssetClick
            )
        }

        // 2. Net Worth Growth Over Time (Interactive Line Chart)
        item {
            NetWorthGrowthCard(
                timeline = netWorthTimeline,
                selectedTimeframe = selectedTimeframe,
                onTimeframeSelected = onTimeframeSelected
            )
        }

        // 3. Asset Allocation Breakdown Bar (rendered only when assets exist)
        if (wealthMetrics.totalAssets > 0) {
            item {
                AssetAllocationCard(assets = assets, totalAssets = wealthMetrics.totalAssets)
            }
        }

        // 3. Grouped Holdings
        for (ac in ASSET_CLASSES) {
            val classAssets = assets.filter { it.assetType.equals(ac, ignoreCase = true) }
            val classTotal = classAssets.sumOf { it.value }
            val isExpanded = expandedStates[ac] ?: false

            item(key = "asset_class_$ac") {
                AssetClassCard(
                    assetClassName = ac,
                    assets = classAssets,
                    totalValue = classTotal,
                    isExpanded = isExpanded,
                    onToggleExpand = { expandedStates[ac] = !isExpanded },
                    onAssetClick = { onEditAssetClick(it) },
                    onQuickUpdate = { quickUpdateAsset = it }
                )
            }
        }
    }

    // Quick Update Dialog
    quickUpdateAsset?.let { asset ->
        QuickValuationDialog(
            asset = asset,
            onDismiss = { quickUpdateAsset = null },
            onUpdateValue = { newVal ->
                onQuickUpdateValuation(asset, newVal)
                quickUpdateAsset = null
            }
        )
    }
}

@Composable
private fun NetWorthHeroCard(
    metrics: WealthMetrics,
    onAddHoldingClick: () -> Unit
) {
    val heroShape = RoundedCornerShape(32.dp)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(heroShape)
            .shadow(16.dp, heroShape, spotColor = Indigo600.copy(alpha = 0.25f))
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(Indigo600, Indigo700)
                )
            )
            .testTag("net_worth_hero_card")
    ) {
        // Decorative Indigo circles matching design HTML
        Box(
            modifier = Modifier
                .size(130.dp)
                .offset(x = 220.dp, y = (-40).dp)
                .background(Indigo500.copy(alpha = 0.45f), CircleShape)
        )
        Box(
            modifier = Modifier
                .size(70.dp)
                .offset(x = 280.dp, y = 30.dp)
                .background(Indigo500.copy(alpha = 0.3f), CircleShape)
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(22.dp)
        ) {
            Text(
                text = "Total Balance / Net Worth",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Medium,
                color = Color.White.copy(alpha = 0.82f)
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Main Balance Display
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = CurrencyFormatter.formatInr(metrics.totalNetWorth),
                    style = MaterialTheme.typography.headlineLarge.copy(fontSize = 32.sp),
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "(${CurrencyFormatter.formatCompactInr(metrics.totalNetWorth)})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White.copy(alpha = 0.85f),
                    modifier = Modifier.padding(bottom = 3.dp)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Row below Net Worth amount: Growth Pill on left, Add Asset button on right
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Growth / Returns Pill Indicator
                val returnDisplay = if (metrics.totalInvested > 0.0) {
                    val sign = if (metrics.totalReturnsPercentage >= 0) "+" else ""
                    "$sign${String.format(java.util.Locale.US, "%.1f", metrics.totalReturnsPercentage)}%"
                } else {
                    "+${metrics.netWorthGrowthRate}%"
                }
                val returnLabel = if (metrics.totalInvested > 0.0) "overall return" else "vs. last month"

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(Color.White.copy(alpha = 0.2f))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.TrendingUp,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = returnDisplay,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = returnLabel,
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(alpha = 0.75f)
                    )
                }

                // Add Asset Button (placed below Net Worth amount on the right side)
                Button(
                    onClick = onAddHoldingClick,
                    modifier = Modifier
                        .height(36.dp)
                        .testTag("btn_add_holding_wealth"),
                    shape = RoundedCornerShape(18.dp),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 0.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isSystemInDarkTheme()) Color(0xFF1E293B) else Color.White,
                        contentColor = if (isSystemInDarkTheme()) Color.White else Indigo600
                    ),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Add Asset",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Assets vs Liabilities Glassmorphic Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(Color.White.copy(alpha = 0.14f))
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Total Assets",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(alpha = 0.75f)
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = CurrencyFormatter.formatCompactInr(metrics.totalAssets),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFA7F3D0) // Mint green
                    )
                }

                Box(
                    modifier = Modifier
                        .height(30.dp)
                        .width(1.dp)
                        .background(Color.White.copy(alpha = 0.25f))
                )

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Total Liabilities",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(alpha = 0.75f)
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = CurrencyFormatter.formatCompactInr(metrics.totalLiabilities),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFFECDD3) // Soft rose
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AssetAllocationCard(assets: List<AssetEntity>, totalAssets: Double) {
    if (totalAssets <= 0) return

    val isDark = isSystemInDarkTheme()
    val nonLiabilities = assets.filter { !it.isLiability }
    val grouped = nonLiabilities.groupBy { it.assetType }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("asset_allocation_card"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = if (isDark) DarkCard else Color.White),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(if (isDark) Color(0xFF334155).copy(alpha = 0.6f) else Slate200.copy(alpha = 0.6f))
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .creativeCardBackground(CardPatternType.OrbitsAndRings, accentColor = Indigo600, isDark = isDark)
                .padding(18.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(if (isDark) Color(0xFF1E1B4B) else Indigo50, RoundedCornerShape(10.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.PieChart,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = if (isDark) Indigo500 else Indigo600
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Asset Allocation Distribution",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = if (isDark) Color.White else Slate900
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Multi-segment stacked progress bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(14.dp)
                    .clip(RoundedCornerShape(7.dp))
            ) {
                for ((type, items) in grouped) {
                    val sum = items.sumOf { it.value }
                    val weight = (sum / totalAssets).toFloat()
                    val color = getAssetClassColor(type)
                    if (weight > 0.001f) {
                        Box(
                            modifier = Modifier
                                .weight(weight)
                                .height(14.dp)
                                .background(color)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Legend labels
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                for ((type, items) in grouped) {
                    val sum = items.sumOf { it.value }
                    val pct = (sum / totalAssets) * 100.0
                    val color = getAssetClassColor(type)

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(color, CircleShape)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "$type: ${String.format(java.util.Locale.US, "%.1f", pct)}%",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isDark) Slate400 else Slate600
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AssetClassCard(
    assetClassName: String,
    assets: List<AssetEntity>,
    totalValue: Double,
    isExpanded: Boolean,
    onToggleExpand: () -> Unit,
    onAssetClick: (AssetEntity) -> Unit,
    onQuickUpdate: (AssetEntity) -> Unit
) {
    val isDark = isSystemInDarkTheme()
    val isLiabilityClass = assetClassName.equals("Liabilities & Loans", ignoreCase = true)
    val classColor = if (isLiabilityClass) Rose600 else getAssetClassColor(assetClassName)
    val classBg = if (isLiabilityClass) (if (isDark) Color(0xFF4C0519).copy(alpha = 0.5f) else Rose50) else (if (isDark) classColor.copy(alpha = 0.22f) else getAssetClassBg(assetClassName))
    val classIcon = getAssetClassIcon(assetClassName)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .border(1.dp, if (isDark) Color(0xFF334155).copy(alpha = 0.6f) else Slate200.copy(alpha = 0.6f), RoundedCornerShape(24.dp)),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = if (isDark) DarkCard else Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .creativeCardBackground(CardPatternType.ThemedLuster, accentColor = classColor, isDark = isDark)
        ) {
            // Header Row (Clickable to toggle expand)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onToggleExpand() }
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .background(classBg, RoundedCornerShape(14.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = classIcon,
                        contentDescription = null,
                        tint = classColor,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = assetClassName,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (isDark) Color.White else Slate900
                    )
                    Text(
                        text = "${assets.size} holding${if (assets.size == 1) "" else "s"}",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isDark) Slate400 else Slate500
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "${if (isLiabilityClass && totalValue > 0) "-" else ""}${CurrencyFormatter.formatCompactInr(totalValue)}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (isLiabilityClass) Rose600 else (if (isDark) Color.White else Slate900)
                    )
                }

                Spacer(modifier = Modifier.width(4.dp))

                Icon(
                    imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null,
                    tint = Slate400
                )
            }

            // Expanded Holdings List
            AnimatedVisibility(visible = isExpanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    if (assets.isEmpty()) {
                        Text(
                            text = "No holdings added in this class yet",
                            style = MaterialTheme.typography.bodySmall,
                            color = Slate400,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    } else {
                        for (asset in assets) {
                            HoldingItemRow(
                                asset = asset,
                                isLiability = isLiabilityClass || asset.isLiability,
                                onClick = { onAssetClick(asset) },
                                onQuickUpdate = { onQuickUpdate(asset) }
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HoldingItemRow(
    asset: AssetEntity,
    isLiability: Boolean,
    onClick: () -> Unit,
    onQuickUpdate: () -> Unit
) {
    val isDark = isSystemInDarkTheme()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .creativeCardBackground(CardPatternType.SubtleTexture, accentColor = if (isLiability) Rose600 else Indigo600, isDark = isDark)
            .border(1.dp, if (isDark) Color(0xFF334155).copy(alpha = 0.5f) else Slate200.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = asset.name,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = if (isDark) Color.White else Slate900,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (!isLiability && asset.investedAmount != null && asset.investedAmount > 0.0) {
                    Text(
                        text = "Inv: ${CurrencyFormatter.formatCompactInr(asset.investedAmount)}",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                        color = if (isDark) Slate400 else Slate500
                    )
                    Text(
                        text = " • ",
                        style = MaterialTheme.typography.labelSmall,
                        color = Slate400
                    )
                }
                Text(
                    text = "Updated ${DateUtils.formatDateShort(asset.updatedAt)}",
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                    color = Slate400
                )
            }
        }

        Spacer(modifier = Modifier.width(8.dp))

        // Valuation Pill & Return %
        Column(horizontalAlignment = Alignment.End) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (isLiability) (if (isDark) Color(0xFF4C0519).copy(alpha = 0.5f) else Rose50) else (if (isDark) Color(0xFF1E1B4B) else Indigo50))
                    .clickable { onQuickUpdate() }
                    .padding(horizontal = 10.dp, vertical = 5.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = CurrencyFormatter.formatInr(asset.value),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (isLiability) Rose600 else Indigo600
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Quick Update",
                        tint = if (isLiability) Rose600 else Indigo600,
                        modifier = Modifier.size(12.dp)
                    )
                }
            }

            if (!isLiability && asset.investedAmount != null && asset.investedAmount > 0.0) {
                val retPct = ((asset.value - asset.investedAmount) / asset.investedAmount) * 100.0
                val isPos = retPct > 0.0
                val isNeg = retPct < 0.0
                val retColor = when {
                    isPos -> Emerald600
                    isNeg -> Rose600
                    else -> Slate500
                }
                val retText = when {
                    isPos -> "+${String.format(java.util.Locale.US, "%.1f", retPct)}%"
                    isNeg -> "${String.format(java.util.Locale.US, "%.1f", retPct)}%"
                    else -> "0.0%"
                }

                Spacer(modifier = Modifier.height(2.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(end = 4.dp)
                ) {
                    Text(
                        text = retText,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold
                        ),
                        color = retColor
                    )
                    Text(
                        text = " return",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                        color = Slate400
                    )
                }
            }
        }
    }
}

private fun getAssetClassColor(assetType: String): Color {
    return when (assetType.lowercase().trim()) {
        "bank accounts", "bank", "savings" -> Blue600
        "mutual funds & equities", "equities", "stocks" -> Emerald600
        "fixed deposits", "fd", "rd" -> Indigo600
        "gold & sgbs", "gold", "sgb" -> Amber500
        "real estate", "property" -> Color(0xFF7C3AED) // Purple
        "retirement & pf", "pf", "epf", "nps" -> Color(0xFF0284C7) // Sky
        "cash in hand", "cash" -> Color(0xFF0D9488) // Teal
        "liabilities & loans", "loans", "debt" -> Rose600
        else -> Slate600
    }
}

private fun getAssetClassBg(assetType: String): Color {
    return when (assetType.lowercase().trim()) {
        "bank accounts", "bank", "savings" -> Blue50
        "mutual funds & equities", "equities", "stocks" -> Emerald50
        "fixed deposits", "fd", "rd" -> Indigo50
        "gold & sgbs", "gold", "sgb" -> Amber50
        "real estate", "property" -> Color(0xFFF5F3FF) // Purple 50
        "retirement & pf", "pf", "epf", "nps" -> Color(0xFFF0F9FF) // Sky 50
        "cash in hand", "cash" -> Color(0xFFF0FDFA) // Teal 50
        "liabilities & loans", "loans", "debt" -> Rose50
        else -> Indigo50
    }
}

private fun getAssetClassIcon(assetType: String): ImageVector {
    return when (assetType.lowercase().trim()) {
        "bank accounts", "bank", "savings" -> Icons.Default.AccountBalance
        "mutual funds & equities", "equities", "stocks" -> Icons.Default.ShowChart
        "fixed deposits", "fd", "rd" -> Icons.Default.Savings
        "gold & sgbs", "gold", "sgb" -> Icons.Default.MonetizationOn
        "real estate", "property" -> Icons.Default.Home
        "retirement & pf", "pf", "epf", "nps" -> Icons.Default.Work
        "cash in hand", "cash" -> Icons.Default.Money
        "liabilities & loans", "loans", "debt" -> Icons.Default.TrendingUp
        else -> Icons.Default.AccountBalance
    }
}
