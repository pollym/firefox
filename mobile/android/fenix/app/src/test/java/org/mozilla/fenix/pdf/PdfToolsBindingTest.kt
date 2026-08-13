/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.fenix.pdf

import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import mozilla.components.browser.state.action.ContentAction
import mozilla.components.browser.state.state.BrowserState
import mozilla.components.browser.state.state.createTab
import mozilla.components.browser.state.store.BrowserStore
import org.junit.Assert.assertEquals
import org.junit.Test
import org.mozilla.fenix.utils.Settings

class PdfToolsBindingTest {
    private val testDispatcher = StandardTestDispatcher()

    private val tabId = "1"
    private val settings: Settings = mockk()
    private val reported = mutableListOf<Boolean>()

    private val browserStore = BrowserStore(
        BrowserState(
            tabs = listOf(createTab(url = "https://mozilla.org/doc.pdf", id = tabId)),
            selectedTabId = tabId,
        ),
    )

    private fun startBinding(pdfToolsEnabled: Boolean = true) {
        every { settings.enablePdfTools } returns pdfToolsEnabled

        PdfToolsBinding(
            browserStore = browserStore,
            settings = settings,
            onPdfToolsVisibilityChanged = { reported.add(it) },
            dispatcher = testDispatcher,
        ).start()
        testDispatcher.scheduler.advanceUntilIdle()
    }

    private fun enterPdfViewer() {
        browserStore.dispatch(ContentAction.EnteredPdfViewer(tabId))
        testDispatcher.scheduler.advanceUntilIdle()
    }

    private fun exitPdfViewer() {
        browserStore.dispatch(ContentAction.ExitedPdfViewer(tabId))
        testDispatcher.scheduler.advanceUntilIdle()
    }

    @Test
    fun `WHEN the tab starts and stops showing a PDF THEN each change is reported`() = runTest {
        startBinding()

        enterPdfViewer()
        exitPdfViewer()

        assertEquals(listOf(false, true, false), reported)
    }

    @Test
    fun `GIVEN the PDF tools are disabled WHEN the selected tab shows a PDF THEN they stay hidden`() = runTest {
        startBinding(pdfToolsEnabled = false)

        enterPdfViewer()

        assertEquals(listOf(false), reported)
    }

    @Test
    fun `WHEN the PDF status does not change THEN nothing further is reported`() = runTest {
        startBinding()

        enterPdfViewer()
        enterPdfViewer()

        assertEquals(listOf(false, true), reported)
    }
}
