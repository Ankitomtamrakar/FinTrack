package com.example

import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.unit.dp
import com.example.ui.components.CalendarMatrix
import com.example.ui.theme.FinTrackTheme
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = RobolectricDeviceQualifiers.Pixel8, sdk = [34])
class GreetingScreenshotTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun calendar_matrix_screenshot() {
        composeTestRule.setContent {
            FinTrackTheme {
                CalendarMatrix(
                    year = 2026,
                    month = 8, // September
                    dailyNetMap = mapOf(1788566400000L to -749.0, 1788652800000L to 125000.0),
                    dailyHasTransactions = setOf(1788566400000L, 1788652800000L),
                    selectedDate = null,
                    onDateSelected = {},
                    onPrevMonth = {},
                    onNextMonth = {},
                    onTodayJump = {},
                    modifier = Modifier.padding(16.dp)
                )
            }
        }

        composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/greeting.png")
    }
}
