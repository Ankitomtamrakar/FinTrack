package com.example.ui.components

import android.graphics.Paint
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.DarkCard
import com.example.ui.theme.Emerald50
import com.example.ui.theme.Emerald600
import com.example.ui.theme.Indigo50
import com.example.ui.theme.Indigo500
import com.example.ui.theme.Indigo600
import com.example.ui.theme.Rose50
import com.example.ui.theme.Rose600
import com.example.ui.theme.Slate100
import com.example.ui.theme.Slate200
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate500
import com.example.ui.theme.Slate600
import com.example.ui.theme.Slate900
import com.example.util.CurrencyFormatter
import com.example.util.NetWorthPoint
import com.example.util.NetWorthTimeframe
import kotlin.math.abs

@Composable
fun NetWorthGrowthCard(
    timeline: List<NetWorthPoint>,
    selectedTimeframe: NetWorthTimeframe,
    onTimeframeSelected: (NetWorthTimeframe) -> Unit,
    modifier: Modifier = Modifier,
    initialExpanded: Boolean = true
) {
    var isExpanded by remember { mutableStateOf(initialExpanded) }
    var activeScrubIndex by remember { mutableStateOf<Int?>(null) }

    val initialPoint = timeline.firstOrNull()
    val latestPoint = timeline.lastOrNull()

    val currentPoint = if (activeScrubIndex != null && activeScrubIndex in timeline.indices) {
        timeline[activeScrubIndex!!]
    } else {
        latestPoint
    }

    val displayValue = currentPoint?.netWorth ?: 0.0
    val baselineValue = initialPoint?.netWorth ?: displayValue

    val changeAmount = displayValue - baselineValue
    val changePercent = if (baselineValue > 0.0) {
        (changeAmount / baselineValue) * 100.0
    } else 0.0

    val isDark = isSystemInDarkTheme()
    val isPositive = changeAmount >= 0.0
    val trendColor = if (isPositive) Emerald600 else Rose600
    val trendBg = if (isDark) {
        if (isPositive) Color(0xFF064E3B).copy(alpha = 0.45f) else Color(0xFF4C0519).copy(alpha = 0.45f)
    } else {
        if (isPositive) Emerald50 else Rose50
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("net_worth_growth_card"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = if (isDark) DarkCard else Color.White),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(if (isDark) Color(0xFF334155).copy(alpha = 0.6f) else Slate200.copy(alpha = 0.8f))
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .creativeCardBackground(CardPatternType.GeometricFinance, accentColor = trendColor, isDark = isDark)
                .padding(16.dp)
        ) {
            // Header: Title & Timeframe Selector
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .weight(1f, fill = false)
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { isExpanded = !isExpanded }
                        .padding(vertical = 4.dp, horizontal = 2.dp)
                        .testTag("btn_toggle_net_worth_growth")
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isDark) Color(0xFF1E1B4B) else Indigo50),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.TrendingUp,
                            contentDescription = "Net Worth Growth",
                            tint = if (isDark) Indigo500 else Indigo600,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Net Worth Growth",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (isDark) Color.White else Slate900,
                                modifier = Modifier.testTag("text_net_worth_growth_title")
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                contentDescription = if (isExpanded) "Collapse chart" else "Expand chart",
                                tint = if (isDark) Slate400 else Slate500,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Text(
                            text = if (isExpanded) "Wealth trend over time" else "Tap to expand chart",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isDark) Slate400 else Slate500
                        )
                    }
                }

                if (isExpanded) {
                    // Timeframe Chips (1M, 3M, 6M, 1Y, ALL)
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        for (tf in NetWorthTimeframe.values()) {
                            val isSelected = tf == selectedTimeframe
                            Box(
                                modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) Indigo600 else if (isDark) Color(0xFF1E293B) else Slate100)
                                .clickable {
                                    activeScrubIndex = null
                                    onTimeframeSelected(tf)
                                }
                                .padding(horizontal = 7.dp, vertical = 4.dp)
                                .testTag("timeframe_${tf.label.lowercase()}"),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = tf.label,
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) Color.White else if (isDark) Slate400 else Slate600
                                )
                            }
                        }
                    }
                } else {
                    // When collapsed, show quick compact valuation and trend pill on the right
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { isExpanded = !isExpanded }
                            .padding(vertical = 4.dp, horizontal = 4.dp)
                    ) {
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = CurrencyFormatter.formatCompactInr(displayValue),
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (isDark) Color.White else Slate900
                            )
                            val sign = if (isPositive) "+" else "-"
                            Text(
                                text = "$sign${String.format("%.1f", abs(changePercent))}% (${selectedTimeframe.label})",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                fontWeight = FontWeight.SemiBold,
                                color = trendColor
                            )
                        }
                    }
                }
            }

            AnimatedVisibility(
                visible = isExpanded,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Spacer(modifier = Modifier.height(14.dp))

                    // Value & Growth Indicator
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.Bottom,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = CurrencyFormatter.formatInr(displayValue),
                                style = MaterialTheme.typography.headlineMedium.copy(
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.ExtraBold
                                ),
                                color = if (isDark) Color.White else Slate900,
                                modifier = Modifier.testTag("text_net_worth_trend_value")
                            )

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(top = 2.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(trendBg)
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = if (isPositive) Icons.AutoMirrored.Filled.TrendingUp else Icons.AutoMirrored.Filled.TrendingDown,
                                            contentDescription = null,
                                            tint = trendColor,
                                            modifier = Modifier.size(12.dp)
                                        )
                                        Spacer(modifier = Modifier.width(3.dp))
                                        val sign = if (isPositive) "+" else "-"
                                        Text(
                                            text = "$sign${CurrencyFormatter.formatCompactInr(abs(changeAmount))} (${String.format("%.1f", abs(changePercent))}%)",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = trendColor
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.width(8.dp))

                                Text(
                                    text = if (activeScrubIndex != null) {
                                        currentPoint?.fullDateLabel ?: ""
                                    } else {
                                        selectedTimeframe.title
                                    },
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (activeScrubIndex != null) Indigo600 else Slate500,
                                    fontWeight = if (activeScrubIndex != null) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }

                        if (activeScrubIndex != null) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (isDark) Color(0xFF1E1B4B) else Indigo50)
                                    .clickable { activeScrubIndex = null }
                                    .padding(horizontal = 6.dp, vertical = 3.dp)
                            ) {
                                Text(
                                    text = "Reset",
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                    fontWeight = FontWeight.Bold,
                                    color = if (isDark) Indigo500 else Indigo600
                                )
                            }
                        } else {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(bottom = 4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.TouchApp,
                                    contentDescription = null,
                                    tint = Slate400,
                                    modifier = Modifier.size(13.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Drag to inspect",
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                    color = Slate400
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // The Line Chart Canvas
                    NetWorthLineChart(
                        timeline = timeline,
                        activeIndex = activeScrubIndex,
                        onIndexChange = { activeScrubIndex = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .testTag("net_worth_line_chart")
                    )
                }
            }
        }
    }
}

@Composable
fun NetWorthLineChart(
    timeline: List<NetWorthPoint>,
    activeIndex: Int?,
    onIndexChange: (Int?) -> Unit,
    modifier: Modifier = Modifier,
    isDark: Boolean = isSystemInDarkTheme()
) {
    val density = LocalDensity.current

    val textPaint = remember(density, isDark) {
        Paint().apply {
            color = if (isDark) {
                android.graphics.Color.argb(210, 148, 163, 184) // Slate 400
            } else {
                android.graphics.Color.argb(170, 100, 116, 139) // Slate 500
            }
            textSize = with(density) { 9.5.sp.toPx() }
            isAntiAlias = true
            textAlign = Paint.Align.LEFT
        }
    }

    val datePaint = remember(density, isDark) {
        Paint().apply {
            color = if (isDark) {
                android.graphics.Color.argb(210, 148, 163, 184)
            } else {
                android.graphics.Color.argb(170, 100, 116, 139)
            }
            textSize = with(density) { 9.5.sp.toPx() }
            isAntiAlias = true
            textAlign = Paint.Align.CENTER
        }
    }

    val activeDatePaint = remember(density) {
        Paint().apply {
            color = android.graphics.Color.argb(255, 79, 70, 229) // Indigo 600
            textSize = with(density) { 10.sp.toPx() }
            isAntiAlias = true
            isFakeBoldText = true
            textAlign = Paint.Align.CENTER
        }
    }

    if (timeline.isEmpty()) {
        Box(
            modifier = modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "No wealth records available",
                style = MaterialTheme.typography.bodySmall,
                color = Slate400
            )
        }
        return
    }

    Canvas(
        modifier = modifier
            .pointerInput(timeline) {
                detectTapGestures(
                    onTap = { tapOffset ->
                        val leftPad = with(density) { 12.dp.toPx() }
                        val rightPad = with(density) { 46.dp.toPx() }
                        val chartWidth = size.width - leftPad - rightPad
                        if (chartWidth > 0 && timeline.size > 1) {
                            val clampedX = (tapOffset.x - leftPad).coerceIn(0f, chartWidth)
                            val fraction = clampedX / chartWidth
                            val index = (fraction * (timeline.size - 1)).toInt().coerceIn(0, timeline.lastIndex)
                            onIndexChange(if (activeIndex == index) null else index)
                        }
                    }
                )
            }
            .pointerInput(timeline) {
                detectDragGestures(
                    onDragStart = { startOffset ->
                        val leftPad = with(density) { 12.dp.toPx() }
                        val rightPad = with(density) { 46.dp.toPx() }
                        val chartWidth = size.width - leftPad - rightPad
                        if (chartWidth > 0 && timeline.size > 1) {
                            val clampedX = (startOffset.x - leftPad).coerceIn(0f, chartWidth)
                            val fraction = clampedX / chartWidth
                            val index = (fraction * (timeline.size - 1)).toInt().coerceIn(0, timeline.lastIndex)
                            onIndexChange(index)
                        }
                    },
                    onDrag = { change, _ ->
                        val leftPad = with(density) { 12.dp.toPx() }
                        val rightPad = with(density) { 46.dp.toPx() }
                        val chartWidth = size.width - leftPad - rightPad
                        if (chartWidth > 0 && timeline.size > 1) {
                            val clampedX = (change.position.x - leftPad).coerceIn(0f, chartWidth)
                            val fraction = clampedX / chartWidth
                            val index = (fraction * (timeline.size - 1)).toInt().coerceIn(0, timeline.lastIndex)
                            onIndexChange(index)
                        }
                    }
                )
            }
    ) {
        val width = size.width
        val height = size.height

        val leftPad = 12.dp.toPx()
        val rightPad = 48.dp.toPx()
        val topPad = 16.dp.toPx()
        val bottomPad = 26.dp.toPx()

        val chartWidth = width - leftPad - rightPad
        val chartHeight = height - topPad - bottomPad

        if (chartWidth <= 0 || chartHeight <= 0) return@Canvas

        val values = timeline.map { it.netWorth }
        val rawMin = values.minOrNull() ?: 0.0
        val rawMax = values.maxOrNull() ?: 1.0

        val rangeDelta = (rawMax - rawMin)
        val yMin = if (rangeDelta == 0.0) {
            (rawMin * 0.9).coerceAtLeast(0.0)
        } else {
            (rawMin - rangeDelta * 0.12).coerceAtLeast(0.0)
        }
        val yMax = if (rangeDelta == 0.0) {
            if (rawMax > 0) rawMax * 1.1 else 10000.0
        } else {
            rawMax + rangeDelta * 0.12
        }
        val yRange = (yMax - yMin).coerceAtLeast(1.0)

        // 1. Draw 3 Horizontal Gridlines with Y-Axis Currency Labels
        val gridFractions = listOf(0.0f, 0.5f, 1.0f) // top, middle, bottom
        val gridDashEffect = PathEffect.dashPathEffect(floatArrayOf(6.dp.toPx(), 6.dp.toPx()), 0f)

        for (frac in gridFractions) {
            val yPos = topPad + frac * chartHeight
            val gridValue = yMax - frac * yRange

            // Gridline
            drawLine(
                color = if (isDark) Color(0xFF334155).copy(alpha = 0.6f) else Slate200.copy(alpha = 0.7f),
                start = Offset(leftPad, yPos),
                end = Offset(leftPad + chartWidth, yPos),
                strokeWidth = 1.dp.toPx(),
                pathEffect = gridDashEffect
            )

            // Y-Axis label on right
            val formatted = CurrencyFormatter.formatCompactInr(gridValue).replace("₹", "")
            drawContext.canvas.nativeCanvas.drawText(
                "₹$formatted",
                leftPad + chartWidth + 6.dp.toPx(),
                yPos + 3.5.dp.toPx(),
                textPaint
            )
        }

        // 2. Compute Point Offsets
        val points = timeline.mapIndexed { idx, pt ->
            val fractionX = if (timeline.size > 1) idx.toFloat() / (timeline.size - 1) else 0.5f
            val x = leftPad + fractionX * chartWidth
            val fractionY = ((pt.netWorth - yMin) / yRange).toFloat().coerceIn(0f, 1f)
            val y = topPad + (1f - fractionY) * chartHeight
            Offset(x, y)
        }

        // 3. Build Smooth Monotone Cubic Bezier Curve
        val strokePath = Path()
        val fillPath = Path()

        if (points.isNotEmpty()) {
            strokePath.moveTo(points[0].x, points[0].y)

            for (i in 0 until points.size - 1) {
                val p0 = points[i]
                val p1 = points[i + 1]
                val controlX1 = p0.x + (p1.x - p0.x) / 2f
                val controlY1 = p0.y
                val controlX2 = p0.x + (p1.x - p0.x) / 2f
                val controlY2 = p1.y
                strokePath.cubicTo(controlX1, controlY1, controlX2, controlY2, p1.x, p1.y)
            }

            // Area Gradient Fill
            fillPath.addPath(strokePath)
            fillPath.lineTo(points.last().x, topPad + chartHeight)
            fillPath.lineTo(points.first().x, topPad + chartHeight)
            fillPath.close()

            val fillBrush = Brush.verticalGradient(
                colors = listOf(
                    Indigo600.copy(alpha = 0.22f),
                    Indigo600.copy(alpha = 0.01f)
                ),
                startY = topPad,
                endY = topPad + chartHeight
            )
            drawPath(fillPath, brush = fillBrush)

            // Line Stroke
            drawPath(
                path = strokePath,
                color = Indigo600,
                style = Stroke(
                    width = 2.8.dp.toPx(),
                    cap = StrokeCap.Round,
                    join = StrokeJoin.Round
                )
            )

            // Data Points
            for (i in points.indices) {
                val isSelected = (activeIndex == i)
                val isLast = (i == points.lastIndex)

                if (isSelected) {
                    // Vertical Scrubber Line
                    drawLine(
                        color = Indigo600.copy(alpha = 0.5f),
                        start = Offset(points[i].x, topPad),
                        end = Offset(points[i].x, topPad + chartHeight),
                        strokeWidth = 1.5.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(4.dp.toPx(), 4.dp.toPx()), 0f)
                    )

                    // Glowing Anchor Point
                    drawCircle(
                        color = Indigo600.copy(alpha = 0.2f),
                        radius = 10.dp.toPx(),
                        center = points[i]
                    )
                    drawCircle(
                        color = Indigo600,
                        radius = 5.dp.toPx(),
                        center = points[i]
                    )
                    drawCircle(
                        color = Color.White,
                        radius = 2.5.dp.toPx(),
                        center = points[i]
                    )
                } else if (isLast) {
                    // Pulse ring on the latest current point
                    drawCircle(
                        color = Indigo600.copy(alpha = 0.25f),
                        radius = 6.dp.toPx(),
                        center = points[i]
                    )
                    drawCircle(
                        color = Indigo600,
                        radius = 3.5.dp.toPx(),
                        center = points[i]
                    )
                    drawCircle(
                        color = Color.White,
                        radius = 1.8.dp.toPx(),
                        center = points[i]
                    )
                } else {
                    // Subtle dot
                    drawCircle(
                        color = Color.White,
                        radius = 3.dp.toPx(),
                        center = points[i]
                    )
                    drawCircle(
                        color = Indigo600,
                        radius = 2.dp.toPx(),
                        center = points[i]
                    )
                }
            }
        }

        // 4. Draw X-Axis Date Labels
        val labelIndices = if (points.size <= 5) {
            points.indices.toList()
        } else {
            // First, 2-3 middle, and last
            val step = points.size / 4
            listOf(0, step, step * 2, step * 3, points.lastIndex).distinct()
        }

        for (idx in labelIndices) {
            val pt = points[idx]
            val dateText = timeline[idx].dateLabel
            val isCurrentActive = (activeIndex == idx)

            drawContext.canvas.nativeCanvas.drawText(
                dateText,
                pt.x,
                topPad + chartHeight + 16.dp.toPx(),
                if (isCurrentActive) activeDatePaint else datePaint
            )
        }
    }
}
