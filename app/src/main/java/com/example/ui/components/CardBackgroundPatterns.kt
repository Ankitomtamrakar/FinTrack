package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.DarkCard
import com.example.ui.theme.Indigo600
import com.example.ui.theme.Slate200

enum class CardPatternType {
    MeshGlow,          // Rich subtle gradient with corner orbital rings & dot matrix
    GeometricFinance,  // Financial trend wave landscape & subtle diamond polygon
    OrbitsAndRings,    // Concentric dashed & solid orbits for analytics & donut charts
    ThemedLuster,      // Themed dynamic accent color with ambient spotlight & soft geometry
    SubtleTexture,     // Refined micro-gradient with delicate corner lines for compact cards
    BankGuilloche      // Currency / security guilloche waves & chip motif for Bank SMS
}

/**
 * Custom Modifier extension that draws non-plain, creative textured backgrounds
 * (gradients, geometric watermarks, orbital rings, micro-dot matrices, or financial waves)
 * behind the content with full Dark Mode & Light Mode support.
 */
@Composable
fun Modifier.creativeCardBackground(
    patternType: CardPatternType = CardPatternType.MeshGlow,
    accentColor: Color = Indigo600,
    isDark: Boolean = isSystemInDarkTheme()
): Modifier = this.drawBehind {
    when (patternType) {
        CardPatternType.MeshGlow -> drawMeshGlowPattern(accentColor, isDark)
        CardPatternType.GeometricFinance -> drawGeometricFinancePattern(accentColor, isDark)
        CardPatternType.OrbitsAndRings -> drawOrbitsAndRingsPattern(accentColor, isDark)
        CardPatternType.ThemedLuster -> drawThemedLusterPattern(accentColor, isDark)
        CardPatternType.SubtleTexture -> drawSubtleTexturePattern(accentColor, isDark)
        CardPatternType.BankGuilloche -> drawBankGuillochePattern(accentColor, isDark)
    }
}

/**
 * Reusable Card component with built-in creative non-plain background patterns and dark mode adaptation.
 */
@Composable
fun CreativeCard(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(22.dp),
    elevation: Dp = 2.dp,
    border: BorderStroke? = null,
    patternType: CardPatternType = CardPatternType.MeshGlow,
    accentColor: Color = Indigo600,
    isDark: Boolean = isSystemInDarkTheme(),
    content: @Composable BoxScope.() -> Unit
) {
    val defaultBorder = BorderStroke(
        1.dp,
        if (isDark) Color(0xFF334155).copy(alpha = 0.5f) else Slate200.copy(alpha = 0.6f)
    )
    Card(
        modifier = modifier,
        shape = shape,
        colors = CardDefaults.cardColors(containerColor = if (isDark) DarkCard else Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = elevation),
        border = border ?: defaultBorder
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .creativeCardBackground(patternType = patternType, accentColor = accentColor, isDark = isDark)
        ) {
            content()
        }
    }
}

// -------------------------------------------------------------
// Pattern Drawing Implementations with Light & Dark Mode Palettes
// -------------------------------------------------------------

private fun DrawScope.drawMeshGlowPattern(accentColor: Color, isDark: Boolean) {
    val w = size.width
    val h = size.height

    // 1. Base luminous linear gradient
    val bgBrush = if (isDark) {
        Brush.linearGradient(
            colors = listOf(
                Color(0xFF161F30),
                Color(0xFF111827),
                Color(0xFF0D1420)
            ),
            start = Offset(0f, 0f),
            end = Offset(w, h)
        )
    } else {
        Brush.linearGradient(
            colors = listOf(
                Color.White,
                Color(0xFFF9FAFF),
                Color(0xFFF1F5FD)
            ),
            start = Offset(0f, 0f),
            end = Offset(w, h)
        )
    }
    drawRect(brush = bgBrush)

    // 2. Corner soft ambient radial glow
    val glowBrush = Brush.radialGradient(
        colors = if (isDark) {
            listOf(
                accentColor.copy(alpha = 0.28f),
                accentColor.copy(alpha = 0.08f),
                Color.Transparent
            )
        } else {
            listOf(
                accentColor.copy(alpha = 0.10f),
                accentColor.copy(alpha = 0.03f),
                Color.Transparent
            )
        },
        center = Offset(w * 0.95f, h * 0.05f),
        radius = (w * 0.55f).coerceAtLeast(140.dp.toPx())
    )
    drawCircle(
        brush = glowBrush,
        radius = (w * 0.55f).coerceAtLeast(140.dp.toPx()),
        center = Offset(w * 0.95f, h * 0.05f)
    )

    // 3. Concentric subtle decorative rings at top-right
    val ringCenter = Offset(w * 0.95f, h * 0.05f)
    val ring1Alpha = if (isDark) 0.30f else 0.10f
    val ring2Alpha = if (isDark) 0.18f else 0.06f

    drawCircle(
        color = accentColor.copy(alpha = ring1Alpha),
        radius = 80.dp.toPx(),
        center = ringCenter,
        style = Stroke(width = if (isDark) 1.5.dp.toPx() else 1.2.dp.toPx())
    )
    drawCircle(
        color = accentColor.copy(alpha = ring2Alpha),
        radius = 130.dp.toPx(),
        center = ringCenter,
        style = Stroke(width = 1.dp.toPx())
    )

    // 4. Delicate micro-dot grid at bottom-left corner
    val dotSpacing = 14.dp.toPx()
    val dotRadius = if (isDark) 1.5.dp.toPx() else 1.3.dp.toPx()
    val dotBaseAlpha = if (isDark) 0.40f else 0.14f
    val dotColor = accentColor.copy(alpha = dotBaseAlpha)
    val startX = 18.dp.toPx()
    val startY = h - 50.dp.toPx()

    for (row in 0..2) {
        for (col in 0..4) {
            val alphaFade = (1f - (row * 0.25f) - (col * 0.15f)).coerceIn(0.1f, 1f)
            drawCircle(
                color = dotColor.copy(alpha = dotColor.alpha * alphaFade),
                radius = dotRadius,
                center = Offset(startX + col * dotSpacing, startY + row * dotSpacing)
            )
        }
    }
}

private fun DrawScope.drawGeometricFinancePattern(accentColor: Color, isDark: Boolean) {
    val w = size.width
    val h = size.height

    // 1. Base gradient
    val bgBrush = if (isDark) {
        Brush.linearGradient(
            colors = listOf(
                Color(0xFF172033),
                Color(0xFF111828),
                Color(0xFF0D1422)
            ),
            start = Offset(0f, 0f),
            end = Offset(w * 0.6f, h)
        )
    } else {
        Brush.linearGradient(
            colors = listOf(
                Color(0xFFFFFFFF),
                Color(0xFFF8FAFD),
                Color(0xFFEEF4FB)
            ),
            start = Offset(0f, 0f),
            end = Offset(w * 0.6f, h)
        )
    }
    drawRect(brush = bgBrush)

    // 2. Smooth curved landscape financial wave watermark across bottom
    val wavePath = Path().apply {
        moveTo(0f, h * 0.78f)
        cubicTo(
            w * 0.25f, h * 0.65f,
            w * 0.55f, h * 0.88f,
            w, h * 0.70f
        )
        lineTo(w, h)
        lineTo(0f, h)
        close()
    }

    val waveGradientColors = if (isDark) {
        listOf(
            accentColor.copy(alpha = 0.22f),
            accentColor.copy(alpha = 0.05f)
        )
    } else {
        listOf(
            accentColor.copy(alpha = 0.08f),
            accentColor.copy(alpha = 0.02f)
        )
    }

    drawPath(
        path = wavePath,
        brush = Brush.verticalGradient(
            colors = waveGradientColors,
            startY = h * 0.65f,
            endY = h
        )
    )

    // Second overlapping wave line
    val waveLine = Path().apply {
        moveTo(0f, h * 0.84f)
        cubicTo(
            w * 0.35f, h * 0.92f,
            w * 0.7f, h * 0.68f,
            w, h * 0.78f
        )
    }
    drawPath(
        path = waveLine,
        color = accentColor.copy(alpha = if (isDark) 0.38f else 0.16f),
        style = Stroke(
            width = if (isDark) 1.6.dp.toPx() else 1.5.dp.toPx(),
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f)
        )
    )

    // 3. Subtle diamond polygon watermark in top-right
    val diamondSize = 38.dp.toPx()
    val diamondCenterX = w - 32.dp.toPx()
    val diamondCenterY = 32.dp.toPx()
    val diamondPath = Path().apply {
        moveTo(diamondCenterX, diamondCenterY - diamondSize / 2f)
        lineTo(diamondCenterX + diamondSize / 2f, diamondCenterY)
        lineTo(diamondCenterX, diamondCenterY + diamondSize / 2f)
        lineTo(diamondCenterX - diamondSize / 2f, diamondCenterY)
        close()
    }
    drawPath(
        path = diamondPath,
        color = accentColor.copy(alpha = if (isDark) 0.28f else 0.12f),
        style = Stroke(width = if (isDark) 1.4.dp.toPx() else 1.2.dp.toPx())
    )
}

private fun DrawScope.drawOrbitsAndRingsPattern(accentColor: Color, isDark: Boolean) {
    val w = size.width
    val h = size.height

    // 1. Base gradient
    val bgBrush = if (isDark) {
        Brush.radialGradient(
            colors = listOf(
                Color(0xFF192438),
                Color(0xFF121929),
                Color(0xFF0D1320)
            ),
            center = Offset(w * 0.85f, h * 0.3f),
            radius = (w * 0.8f).coerceAtLeast(200.dp.toPx())
        )
    } else {
        Brush.radialGradient(
            colors = listOf(
                Color.White,
                Color(0xFFFAFBFE),
                Color(0xFFEFF3FC)
            ),
            center = Offset(w * 0.85f, h * 0.3f),
            radius = (w * 0.8f).coerceAtLeast(200.dp.toPx())
        )
    }
    drawRect(brush = bgBrush)

    val centerOffset = Offset(w * 0.88f, h * 0.25f)

    // 2. Concentric orbits
    val innerRingAlpha = if (isDark) 0.32f else 0.12f
    val midRingAlpha = if (isDark) 0.36f else 0.16f
    val outerRingAlpha = if (isDark) 0.18f else 0.08f

    // Inner solid ring
    drawCircle(
        color = accentColor.copy(alpha = innerRingAlpha),
        radius = 50.dp.toPx(),
        center = centerOffset,
        style = Stroke(width = if (isDark) 1.5.dp.toPx() else 1.2.dp.toPx())
    )
    // Middle dashed ring
    drawCircle(
        color = accentColor.copy(alpha = midRingAlpha),
        radius = 90.dp.toPx(),
        center = centerOffset,
        style = Stroke(
            width = if (isDark) 1.2.dp.toPx() else 1.dp.toPx(),
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(5f, 6f))
        )
    )
    // Outer faint ring
    drawCircle(
        color = accentColor.copy(alpha = outerRingAlpha),
        radius = 140.dp.toPx(),
        center = centerOffset,
        style = Stroke(width = 1.2.dp.toPx())
    )

    // Orbiting planetoid dots with subtle halo
    val dot1Offset = Offset(centerOffset.x - 90.dp.toPx() * 0.707f, centerOffset.y + 90.dp.toPx() * 0.707f)
    val dot2Offset = Offset(centerOffset.x + 50.dp.toPx(), centerOffset.y)

    if (isDark) {
        // Halo glow in dark mode
        drawCircle(
            color = accentColor.copy(alpha = 0.25f),
            radius = 6.dp.toPx(),
            center = dot1Offset
        )
        drawCircle(
            color = accentColor.copy(alpha = 0.30f),
            radius = 7.dp.toPx(),
            center = dot2Offset
        )
    }

    drawCircle(
        color = accentColor.copy(alpha = if (isDark) 0.85f else 0.40f),
        radius = if (isDark) 3.dp.toPx() else 2.5.dp.toPx(),
        center = dot1Offset
    )
    drawCircle(
        color = accentColor.copy(alpha = if (isDark) 0.95f else 0.50f),
        radius = if (isDark) 3.5.dp.toPx() else 3.dp.toPx(),
        center = dot2Offset
    )
}

private fun DrawScope.drawThemedLusterPattern(accentColor: Color, isDark: Boolean) {
    val w = size.width
    val h = size.height

    // 1. Dynamic subtle tint gradient
    val bgBrush = if (isDark) {
        Brush.linearGradient(
            colorStops = arrayOf(
                0.0f to Color(0xFF172033),
                0.55f to Color(0xFF121929),
                1.0f to accentColor.copy(alpha = 0.22f)
            ),
            start = Offset(0f, 0f),
            end = Offset(w, h)
        )
    } else {
        Brush.linearGradient(
            colorStops = arrayOf(
                0.0f to Color.White,
                0.55f to Color.White.copy(alpha = 0.98f),
                1.0f to accentColor.copy(alpha = 0.085f)
            ),
            start = Offset(0f, 0f),
            end = Offset(w, h)
        )
    }
    drawRect(brush = bgBrush)

    // 2. Top-right corner ambient glow matching theme
    drawCircle(
        brush = Brush.radialGradient(
            colors = if (isDark) {
                listOf(
                    accentColor.copy(alpha = 0.32f),
                    accentColor.copy(alpha = 0.08f),
                    Color.Transparent
                )
            } else {
                listOf(
                    accentColor.copy(alpha = 0.16f),
                    accentColor.copy(alpha = 0.04f),
                    Color.Transparent
                )
            },
            center = Offset(w, 0f),
            radius = 120.dp.toPx()
        ),
        radius = 120.dp.toPx(),
        center = Offset(w, 0f)
    )

    // 3. Modern geometric rounded capsule/ring watermark
    val capsuleWidth = 90.dp.toPx()
    val capsuleHeight = 44.dp.toPx()
    val capsulePath = Path().apply {
        addRoundRect(
            RoundRect(
                left = w - capsuleWidth + 20.dp.toPx(),
                top = -15.dp.toPx(),
                right = w + 20.dp.toPx(),
                bottom = capsuleHeight - 15.dp.toPx(),
                cornerRadius = CornerRadius(22.dp.toPx(), 22.dp.toPx())
            )
        )
    }
    drawPath(
        path = capsulePath,
        color = accentColor.copy(alpha = if (isDark) 0.32f else 0.14f),
        style = Stroke(width = if (isDark) 1.5.dp.toPx() else 1.2.dp.toPx())
    )

    // 4. Subtle corner accent dot cluster
    drawCircle(
        color = accentColor.copy(alpha = if (isDark) 0.65f else 0.30f),
        radius = if (isDark) 2.5.dp.toPx() else 2.dp.toPx(),
        center = Offset(w - 22.dp.toPx(), h - 16.dp.toPx())
    )
    drawCircle(
        color = accentColor.copy(alpha = if (isDark) 0.45f else 0.18f),
        radius = if (isDark) 2.dp.toPx() else 1.5.dp.toPx(),
        center = Offset(w - 34.dp.toPx(), h - 16.dp.toPx())
    )
}

private fun DrawScope.drawSubtleTexturePattern(accentColor: Color, isDark: Boolean) {
    val w = size.width
    val h = size.height

    // 1. Very clean, elegant micro-gradient
    val bgBrush = if (isDark) {
        Brush.linearGradient(
            colors = listOf(
                Color(0xFF172033),
                Color(0xFF111828),
                Color(0xFF0E1422)
            ),
            start = Offset(0f, 0f),
            end = Offset(w, h)
        )
    } else {
        Brush.linearGradient(
            colors = listOf(
                Color.White,
                Color(0xFFFAFBFE),
                Color(0xFFF3F6FD)
            ),
            start = Offset(0f, 0f),
            end = Offset(w, h)
        )
    }
    drawRect(brush = bgBrush)

    // 2. Corner subtle curved accent arc
    val arcCenter = Offset(w, 0f)
    drawCircle(
        color = accentColor.copy(alpha = if (isDark) 0.30f else 0.12f),
        radius = 45.dp.toPx(),
        center = arcCenter,
        style = Stroke(width = if (isDark) 1.3.dp.toPx() else 1.dp.toPx())
    )

    // 3. Second mini inner arc
    drawCircle(
        color = accentColor.copy(alpha = if (isDark) 0.20f else 0.08f),
        radius = 24.dp.toPx(),
        center = arcCenter,
        style = Stroke(width = 1.dp.toPx())
    )
}

private fun DrawScope.drawBankGuillochePattern(accentColor: Color, isDark: Boolean) {
    val w = size.width
    val h = size.height

    // 1. Banknote canvas
    val bgBrush = if (isDark) {
        Brush.linearGradient(
            colors = listOf(
                Color(0xFF161F30),
                Color(0xFF111827),
                Color(0xFF0D1422)
            ),
            start = Offset(0f, 0f),
            end = Offset(w, h)
        )
    } else {
        Brush.linearGradient(
            colors = listOf(
                Color.White,
                Color(0xFFFBFBFE),
                Color(0xFFF2F4FB)
            ),
            start = Offset(0f, 0f),
            end = Offset(w, h)
        )
    }
    drawRect(brush = bgBrush)

    // 2. Security guilloche wavy lines watermark across the middle
    val wavePath1 = Path().apply {
        moveTo(0f, h * 0.45f)
        cubicTo(
            w * 0.25f, h * 0.35f,
            w * 0.75f, h * 0.55f,
            w, h * 0.42f
        )
    }
    val wavePath2 = Path().apply {
        moveTo(0f, h * 0.55f)
        cubicTo(
            w * 0.3f, h * 0.65f,
            w * 0.7f, h * 0.45f,
            w, h * 0.58f
        )
    }

    val line1Alpha = if (isDark) 0.30f else 0.12f
    val line2Alpha = if (isDark) 0.22f else 0.08f

    drawPath(
        path = wavePath1,
        color = accentColor.copy(alpha = line1Alpha),
        style = Stroke(
            width = if (isDark) 1.2.dp.toPx() else 1.dp.toPx(),
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(4f, 4f))
        )
    )
    drawPath(
        path = wavePath2,
        color = accentColor.copy(alpha = line2Alpha),
        style = Stroke(
            width = if (isDark) 1.2.dp.toPx() else 1.dp.toPx(),
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(4f, 4f))
        )
    )

    // 3. Bank card chip outline watermark in top-right
    val chipW = 28.dp.toPx()
    val chipH = 22.dp.toPx()
    val chipX = w - 40.dp.toPx()
    val chipY = 12.dp.toPx()

    drawRoundRect(
        color = accentColor.copy(alpha = if (isDark) 0.35f else 0.14f),
        topLeft = Offset(chipX, chipY),
        size = Size(chipW, chipH),
        cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx()),
        style = Stroke(width = if (isDark) 1.2.dp.toPx() else 1.dp.toPx())
    )
    // Chip inner dividing line
    drawLine(
        color = accentColor.copy(alpha = if (isDark) 0.28f else 0.10f),
        start = Offset(chipX + chipW * 0.5f, chipY),
        end = Offset(chipX + chipW * 0.5f, chipY + chipH),
        strokeWidth = 1.dp.toPx()
    )
}
