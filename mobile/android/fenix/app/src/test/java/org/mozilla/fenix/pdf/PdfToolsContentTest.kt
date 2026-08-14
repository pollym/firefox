/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.fenix.pdf

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.test.ext.junit.runners.AndroidJUnit4
import mozilla.components.browser.state.action.ContentAction
import mozilla.components.browser.state.state.BrowserState
import mozilla.components.browser.state.state.createTab
import mozilla.components.browser.state.store.BrowserStore
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mozilla.fenix.theme.FirefoxTheme
import org.mozilla.fenix.theme.Theme

@RunWith(AndroidJUnit4::class)
class PdfToolsContentTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    private val tabId = "1"

    private val browserStore = BrowserStore(
        BrowserState(
            tabs = listOf(createTab(url = "https://mozilla.org/doc.pdf", id = tabId)),
            selectedTabId = tabId,
        ),
    )

    private fun setTestContent() {
        composeTestRule.setContent {
            // The theme is passed in so that it is not resolved from the AppStore, which is not
            // available in these tests.
            FirefoxTheme(theme = Theme.Light) {
                PdfToolsContent(browserStore = browserStore, isLargeWindow = false)
            }
        }
    }

    @Test
    fun `if the selected tab is not a pdf, nothing is shown`() {
        setTestContent()

        composeTestRule.onNodeWithTag(PdfToolsTestTag.BAR).assertDoesNotExist()
        composeTestRule.onNodeWithTag(PdfToolsTestTag.SIGN_FAB).assertDoesNotExist()
    }

    @Test
    fun `when the selected tab starts showing a pdf, the tools are shown`() {
        setTestContent()

        browserStore.dispatch(ContentAction.EnteredPdfViewer(tabId))
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag(PdfToolsTestTag.BAR).assertIsDisplayed()
        composeTestRule.onNodeWithTag(PdfToolsTestTag.SIGN_FAB).assertIsDisplayed()
    }

    @Test
    fun `when the selected tab stops showing a pdf, the tools are hidden again`() {
        setTestContent()

        browserStore.dispatch(ContentAction.EnteredPdfViewer(tabId))
        composeTestRule.waitForIdle()
        browserStore.dispatch(ContentAction.ExitedPdfViewer(tabId))
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag(PdfToolsTestTag.BAR).assertDoesNotExist()
    }
}
