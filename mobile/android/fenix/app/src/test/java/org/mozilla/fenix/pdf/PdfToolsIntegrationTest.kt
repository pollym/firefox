/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.fenix.pdf

import android.view.Gravity
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.test.core.app.ApplicationProvider
import mozilla.components.browser.state.state.BrowserState
import mozilla.components.browser.state.state.createTab
import mozilla.components.browser.state.store.BrowserStore
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
class PdfToolsIntegrationTest {
    private val topToolbarHeight = 100
    private val bottomToolbarHeight = 200

    private val container = CoordinatorLayout(ApplicationProvider.getApplicationContext())

    private val browserStore = BrowserStore(
        BrowserState(
            tabs = listOf(createTab(url = "https://mozilla.org/doc.pdf", id = "1")),
            selectedTabId = "1",
        ),
    )

    private fun integration() = PdfToolsIntegration(
        container = container,
        browserStore = browserStore,
        topToolbarHeight = { topToolbarHeight },
        bottomToolbarHeight = { bottomToolbarHeight },
    )

    private val layoutParams: CoordinatorLayout.LayoutParams
        get() = container.getChildAt(0).layoutParams as CoordinatorLayout.LayoutParams

    @Test
    fun `starting and stopping the feature adds then removes the tools`() {
        val integration = integration()

        integration.start()
        assertEquals(1, container.childCount)

        integration.stop()
        assertEquals(0, container.childCount)
    }

    @Test
    fun `restarting the feature adds tools back to the container`() {
        val integration = integration()

        integration.start()
        integration.stop()
        integration.start()

        assertEquals(1, container.childCount)
        assertNotNull(container.getChildAt(0))
    }

    @Test
    fun `starting the feature twice only adds one set of tools`() {
        val integration = integration()

        integration.start()
        integration.start()

        assertEquals(1, container.childCount)
    }

    @Test
    @Config(qualifiers = "sw400dp")
    fun `on a phone, tools are anchored to the bottom toolbar`() {
        integration().start()

        assertEquals(Gravity.BOTTOM, layoutParams.gravity)
        assertEquals(bottomToolbarHeight, layoutParams.bottomMargin)
        assertEquals(0, layoutParams.topMargin)
    }

    @Test
    @Config(qualifiers = "sw800dp")
    fun `on a tablet, tools are anchored to the top toolbar`() {
        integration().start()

        assertEquals(Gravity.TOP, layoutParams.gravity)
        assertEquals(topToolbarHeight, layoutParams.topMargin)
        assertEquals(0, layoutParams.bottomMargin)
    }
}
