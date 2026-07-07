package com.example

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import com.example.ui.screens.OnboardingView
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
    composeTestRule.setContent {
      MyApplicationTheme {
        OnboardingView(
          faculties = listOf("Computing and IT", "Engineering", "Business"),
          degreesByFaculty = mapOf(
            "Computing and IT" to listOf("BSc (Hons) in Software Engineering", "BSc (Hons) in Information Technology"),
            "Engineering" to listOf("BSc (Hons) in Civil Engineering"),
            "Business" to listOf("BBM (Hons) in Human Resource Management")
          ),
          degreeToTypeMap = mapOf(
            "BSc (Hons) in Software Engineering" to "Honours (Engineering/Tech/IT/Music)",
            "BSc (Hons) in Information Technology" to "Honours (Engineering/Tech/IT/Music)",
            "BSc (Hons) in Civil Engineering" to "Honours (Engineering/Tech/IT/Music)",
            "BBM (Hons) in Human Resource Management" to "Honours (Management/Tourism/Hospitality)"
          ),
          onOnboard = { _, _, _, _, _ -> }
        )
      }
    }

    composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/greeting.png")
  }
}
