package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.Indigo500
import com.example.ui.theme.Indigo600
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate500
import com.example.ui.theme.Slate900
import com.example.util.CurrencyFormatter
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

data class PieSliceData(
    val label: String,
    val value: Double,
    val percentage: Float, // 0..100
    val color: Color
)

@Composable
fun DonutChart(
    slices: List<PieSliceData>,
    totalAmount: Double,
    selectedSliceLabel: String?,
    onSliceSelected: (String?) -> Unit,
    modifier: Modifier = Modifier
) {
    if (slices.isEmpty() || totalAmount <= 0) {
        Box(
            modifier = modifier
                .size(220.dp)
                .testTag("donut_chart_empty"),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val strokeWidth = 26.dp.toPx()
                val radius = (size.minDimension - strokeWidth) / 2
                drawCircle(
                    color = Color.LightGray.copy(alpha = 0.25f),
                    radius = radius,
                    style = Stroke(width = strokeWidth)
                )
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "No Spends",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Slate500
                )
                Text(
                    text = "₹0",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
            }
        }
        return
    }

    val selectedSlice = slices.find { it.label == selectedSliceLabel }

    val animationProgress = remember { Animatable(0f) }
    LaunchedEffect(slices) {
        animationProgress.snapTo(0f)
        animationProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 650, easing = FastOutSlowInEasing)
        )
    }

    Box(
        modifier = modifier
            .size(230.dp)
            .testTag("donut_chart"),
        contentAlignment = Alignment.Center
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(slices) {
                    detectTapGestures { tapOffset ->
                        val center = Offset(size.width / 2f, size.height / 2f)
                        val dx = tapOffset.x - center.x
                        val dy = tapOffset.y - center.y
                        val dist = sqrt(dx * dx + dy * dy)
                        val strokeWidth = 28.dp.toPx()
                        val outerRadius = minOf(size.width, size.height).toFloat() / 2f
                        val innerRadius = outerRadius - strokeWidth

                        if (dist < innerRadius) {
                            // Tap in center resets selection
                            onSliceSelected(null)
                        } else if (dist <= outerRadius + 10.dp.toPx()) {
                            // Tap on a slice
                            var angle = Math.toDegrees(atan2(dy.toDouble(), dx.toDouble())).toFloat()
                            if (angle < 0) angle += 360f
                            // Adjust for -90deg starting position (12 o'clock)
                            val chartAngle = (angle + 90f) % 360f

                            var currentAngle = 0f
                            var hitLabel: String? = null
                            for (slice in slices) {
                                val sweep = (slice.percentage / 100f) * 360f
                                if (chartAngle in currentAngle..(currentAngle + sweep)) {
                                    hitLabel = slice.label
                                    break
                                }
                                currentAngle += sweep
                            }
                            if (hitLabel == selectedSliceLabel) {
                                onSliceSelected(null)
                            } else {
                                onSliceSelected(hitLabel)
                            }
                        }
                    }
                }
        ) {
            val strokeWidthNormal = 24.dp.toPx()
            val strokeWidthSelected = 28.dp.toPx()
            val center = Offset(size.width / 2f, size.height / 2f)
            val baseRadius = (size.minDimension - strokeWidthSelected) / 2f
            val sliceSize = Size(baseRadius * 2, baseRadius * 2)
            val topLeft = Offset(center.x - baseRadius, center.y - baseRadius)

            var startAngle = -90f
            val gapDegrees = if (slices.size > 1) 0.5f else 0f

            for (slice in slices) {
                val rawSweep = (slice.percentage / 100f) * 360f * animationProgress.value
                val isSelected = slice.label == selectedSliceLabel
                val currentStroke = if (isSelected) strokeWidthSelected else strokeWidthNormal

                if (rawSweep > gapDegrees) {
                    val sliceStart = startAngle + (gapDegrees / 2f)
                    val sliceSweep = rawSweep - gapDegrees

                    drawArc(
                        color = if (selectedSliceLabel == null || isSelected) slice.color else slice.color.copy(alpha = 0.35f),
                        startAngle = sliceStart,
                        sweepAngle = sliceSweep,
                        useCenter = false,
                        topLeft = topLeft,
                        size = sliceSize,
                        style = Stroke(width = currentStroke, cap = StrokeCap.Butt)
                    )
                } else if (rawSweep > 0.5f) {
                    drawArc(
                        color = if (selectedSliceLabel == null || isSelected) slice.color else slice.color.copy(alpha = 0.35f),
                        startAngle = startAngle,
                        sweepAngle = rawSweep,
                        useCenter = false,
                        topLeft = topLeft,
                        size = sliceSize,
                        style = Stroke(width = currentStroke, cap = StrokeCap.Butt)
                    )
                }

                startAngle += rawSweep
            }
        }

        // Center Readout
        Column(
            modifier = Modifier.padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (selectedSlice != null) {
                Text(
                    text = selectedSlice.label,
                    style = MaterialTheme.typography.labelMedium,
                    color = selectedSlice.color,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = CurrencyFormatter.formatInr(selectedSlice.value),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.Black,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "${String.format(java.util.Locale.US, "%.1f", selectedSlice.percentage)}%",
                    style = MaterialTheme.typography.labelSmall,
                    color = Slate500
                )
            } else {
                Text(
                    text = "Total Spent",
                    style = MaterialTheme.typography.labelMedium,
                    color = Slate500
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = CurrencyFormatter.formatInr(totalAmount),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.Black,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "${slices.size} Categories",
                    style = MaterialTheme.typography.labelSmall,
                    color = Indigo600
                )
            }
        }
    }
}
