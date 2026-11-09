package app.grapheneos.pdfviewer.test

import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.grapheneos.pdfviewer.PdfViewer
import app.grapheneos.pdfviewer.RetryableComposeRule
import app.grapheneos.pdfviewer.darkDocument
import app.grapheneos.pdfviewer.testrules.OrientationRules
import app.grapheneos.pdfviewer.testrules.RetryRules
import app.grapheneos.pdfviewer.util.PdfViewerLauncher
import app.grapheneos.pdfviewer.util.PdfViewerRobot
import app.grapheneos.pdfviewer.util.PdfViewerTestUtils
import org.junit.Assert.assertFalse
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain
import org.junit.runner.RunWith

/**
 * Dark document toggle and its persistence.
 */
@RunWith(AndroidJUnit4::class)
class PdfViewerDarkDocumentTest {

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
    fun toggle_appliesToPagesAndSurvivesRecreate() {
        PdfViewerLauncher.launchWithTestAsset("test-multipage.pdf").use { scenario ->
            PdfViewerTestUtils.waitForDocumentFullyLoaded(scenario)
            PdfViewerTestUtils.waitForCanvasRendered(scenario)

            // Off by default.
            scenario.onActivity { assertFalse(it.darkDocument) }
            assertFalse(isDarkInWebView(scenario))

            robot.clickDarkDocument()
            waitForDark(scenario, expected = true)

            // The preference is kept when the activity is recreated, and the
            // reloaded viewer applies it before drawing.
            scenario.recreate()
            PdfViewerTestUtils.waitForDocumentFullyLoaded(scenario)
            waitForDark(scenario, expected = true)

            robot.clickDarkDocument()
            waitForDark(scenario, expected = false)
        }
    }

    private fun isDarkInWebView(scenario: ActivityScenario<PdfViewer>): Boolean =
        PdfViewerTestUtils.evaluateJs(
            scenario,
            "document.body.classList.contains('dark-document')"
        ) == "true"

    private fun waitForDark(scenario: ActivityScenario<PdfViewer>, expected: Boolean) {
        PdfViewerTestUtils.pollUntil(
            timeout = 5_000,
            description = { "dark document = $expected in ViewModel and WebView" }
        ) {
            var state = !expected
            scenario.onActivity { state = it.darkDocument }
            state == expected && isDarkInWebView(scenario) == expected
        }
    }
}
