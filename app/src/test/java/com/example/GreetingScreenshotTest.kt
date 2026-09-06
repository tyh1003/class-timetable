package com.example

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import com.example.data.model.Course
import com.example.data.model.DefaultData
import com.example.data.model.FontSizeScale
import com.example.ui.components.TimetableGrid
import com.example.ui.theme.MyApplicationTheme
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
@Config(qualifiers = RobolectricDeviceQualifiers.Pixel8, sdk = [36])
class GreetingScreenshotTest {

  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun greeting_screenshot() {
    val sampleCourses = listOf(
      Course(
        id = 1,
        semesterId = 1,
        courseName = "計算機概論",
        location = "綜一館 201",
        note = "需攜帶筆電",
        dayOfWeek = 1,
        startSlotCode = "1",
        endSlotCode = "2",
        colorHex = "#E06D85",
        categoryName = "必修"
      ),
      Course(
        id = 2,
        semesterId = 1,
        courseName = "統計學",
        location = "管院 302",
        note = "",
        dayOfWeek = 2,
        startSlotCode = "3",
        endSlotCode = "4",
        colorHex = "#5E84C7",
        categoryName = "選修"
      ),
      Course(
        id = 3,
        semesterId = 1,
        courseName = "專題論文",
        location = "研究室",
        note = "每週討論進度",
        dayOfWeek = 3,
        startSlotCode = "5",
        endSlotCode = "6",
        colorHex = "#588B6B",
        categoryName = "論文"
      )
    )

    composeTestRule.setContent {
      MyApplicationTheme {
        TimetableGrid(
          timeSlots = DefaultData.benchmarkSlots,
          courses = sampleCourses,
          showWeekend = false,
          fontSizeScale = FontSizeScale.MEDIUM,
          onCellClicked = { _, _ -> }
        )
      }
    }

    composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/greeting.png")
  }
}

