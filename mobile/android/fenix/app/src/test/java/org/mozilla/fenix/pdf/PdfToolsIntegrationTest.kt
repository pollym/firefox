/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.fenix.pdf

import android.view.Gravity
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.test.core.app.ApplicationProvider
import io.mockk.mockk
import kotlinx.coroutines.test.TestScope
import mozilla.components.browser.state.action.BrowserAction
import mozilla.components.browser.state.action.EngineAction
import mozilla.components.browser.state.engine.EngineMiddleware
import mozilla.components.browser.state.state.BrowserState
import mozilla.components.browser.state.state.createTab
import mozilla.components.browser.state.store.BrowserStore
import mozilla.components.support.test.middleware.CaptureActionsMiddleware
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
class PdfToolsIntegrationTest {
    private val topToolbarHeight = 100
    private val bottomToolbarHeight = 200
    private val tabId = "1"

    private val container = CoordinatorLayout(ApplicationProvider.getApplicationContext())

    private val captureActionsMiddleware = CaptureActionsMiddleware<BrowserState, BrowserAction>()

    private val browserStore =
        BrowserStore(
            initialState =
                BrowserState(
                    tabs = listOf(createTab(url = "https://mozilla.org", id = tabId)),
                    selectedTabId = tabId,
                ),
            middleware =
                listOf(captureActionsMiddleware) + EngineMiddleware.create(engine = mockk(), scope = TestScope()),
        )

    private fun integration() =
        PdfToolsIntegration(
            container = container,
            browserStore = browserStore,
            topToolbarHeight = { topToolbarHeight },
            bottomToolbarHeight = { bottomToolbarHeight },
        )

    private val layoutParams: CoordinatorLayout.LayoutParams
        get() = container.getChildAt(0).layoutParams as CoordinatorLayout.LayoutParams

    @Test
    fun `WHEN the feature is started and stopped THEN the tools are added then removed`() {
        val integration = integration()

        integration.start()
        assertEquals(1, container.childCount)

        integration.stop()
        assertEquals(0, container.childCount)
    }

    @Test
    fun `WHEN the feature is restarted THEN the tools are added back to the container`() {
        val integration = integration()

        integration.start()
        integration.stop()
        integration.start()

        assertEquals(1, container.childCount)
    }

    @Test
    fun `WHEN the feature is started twice THEN only one set of tools is added`() {
        val integration = integration()

        integration.start()
        integration.start()

        assertEquals(1, container.childCount)
    }

    @Test
    @Config(qualifiers = "sw400dp")
    fun `GIVEN a phone sized window WHEN the feature is started THEN the tools anchor to the bottom toolbar`() {
        integration().start()

        assertEquals(Gravity.BOTTOM, layoutParams.gravity)
        assertEquals(bottomToolbarHeight, layoutParams.bottomMargin)
        assertEquals(0, layoutParams.topMargin)
    }

    @Test
    @Config(qualifiers = "sw800dp")
    fun `GIVEN a tablet sized window WHEN the feature is started THEN the tools anchor to the top toolbar`() {
        integration().start()

        assertEquals(Gravity.TOP, layoutParams.gravity)
        assertEquals(topToolbarHeight, layoutParams.topMargin)
        assertEquals(0, layoutParams.bottomMargin)
    }

    @Test
    fun `WHEN download is activated THEN the selected tab is saved as a PDF`() {
        integration().handleDownloadClick()

        captureActionsMiddleware.assertFirstAction(EngineAction.SaveToPdfAction::class) {
            assertEquals(tabId, it.tabId)
        }
    }
}
