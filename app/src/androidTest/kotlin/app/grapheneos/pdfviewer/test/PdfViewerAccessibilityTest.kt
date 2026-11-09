package app.grapheneos.pdfviewer.test

import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.state.ToggleableState
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.grapheneos.pdfviewer.RetryableComposeRule
import app.grapheneos.pdfviewer.TestTags
import app.grapheneos.pdfviewer.testrules.OrientationRules
import app.grapheneos.pdfviewer.testrules.RetryRules
import app.grapheneos.pdfviewer.util.PdfViewerLauncher
import app.grapheneos.pdfviewer.util.PdfViewerRobot
import app.grapheneos.pdfviewer.util.PdfViewerTestUtils
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain
import org.junit.runner.RunWith

/**
 * What screen readers are told about controls.
 */
@RunWith(AndroidJUnit4::class)
class PdfViewerAccessibilityTest {

    private val composeRule = RetryableComposeRule()

    @get:Rule
    val rules: RuleChain = RuleChain
        .outerRule(RetryRules())
        .around(OrientationRules())
        .around(composeRule)

    private val robot = PdfViewerRobot(composeRule)

    @Before
    fun setup() {
        PdfViewerTestUtils.init(composeRule)
    }

    @Test
    fun overflowMenu_hasReadableLabel() {
        PdfViewerLauncher.launchWithTestAsset("test-multipage.pdf").use { scenario ->
            PdfViewerTestUtils.waitForDocumentFullyLoaded(scenario)

            composeRule.onNodeWithContentDescription("More options").assertIsDisplayed()
        }
    }

    @Test
    fun checkableMenuItems_reportTheirState() {
        PdfViewerLauncher.launchWithTestAsset("test-multipage.pdf").use { scenario ->
            PdfViewerTestUtils.waitForDocumentFullyLoaded(scenario)
            composeRule.onNodeWithTag(TestTags.OVERFLOW_MENU).performClick()
            composeRule.waitForIdle()

            // Defaults: continuous scrolling and fit width on, dark document off.
            composeRule.onNode(hasText("Continuous scrolling"), useUnmergedTree = false)
                .assert(toggleable(ToggleableState.On))
            composeRule.onNode(hasText("Dark document"), useUnmergedTree = false)
                .assert(toggleable(ToggleableState.Off))
            composeRule.onNode(hasText("Fit width"), useUnmergedTree = false)
                .assert(SemanticsMatcher.expectValue(SemanticsProperties.Selected, true))
            composeRule.onNode(hasText("Fit page"), useUnmergedTree = false)
                .assert(SemanticsMatcher.expectValue(SemanticsProperties.Selected, false))
        }
    }

    @Test
    fun passwordVisibilityToggle_hasReadableLabel() {
        PdfViewerLauncher.launchDefault().use { scenario ->
            robot.showPasswordDialog(scenario)
            robot.waitForPasswordDialog()

            composeRule.onNodeWithContentDescription("Show password").performClick()
            composeRule.onNodeWithContentDescription("Hide password").assertIsDisplayed()
        }
    }

    private fun toggleable(state: ToggleableState) =
        SemanticsMatcher.expectValue(SemanticsProperties.ToggleableState, state)
}
