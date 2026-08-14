/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.fenix.pdf

import android.view.Gravity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalConfiguration
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.flow.map
import mozilla.components.browser.state.selector.selectedTab
import mozilla.components.browser.state.store.BrowserStore
import mozilla.components.compose.base.theme.layout.AcornWindowSize
import mozilla.components.support.base.feature.LifecycleAwareFeature
import org.mozilla.fenix.pdf.ui.PdfTools
import org.mozilla.fenix.theme.FirefoxTheme

/**
 * This integration is responsible for adding or removing PDF tools and properly anchoring it to the browser.
 * The tools render themselves only while the selected tab is displaying a PDF.
 *
 * @param container The containing browser [CoordinatorLayout] to add the PDF tools onto.
 * @param browserStore The [BrowserStore] to observe the PDF status of the selected tab.
 * @param topToolbarHeight The top toolbar height on the browser.
 * @param bottomToolbarHeight The bottom toolbar height on the browser.
 */
class PdfToolsIntegration(
    private val container: CoordinatorLayout,
    private val browserStore: BrowserStore,
    private val topToolbarHeight: () -> Int,
    private val bottomToolbarHeight: () -> Int,
) : LifecycleAwareFeature {

    private var pdfTools: ComposeView? = null

    override fun start() {
        if (pdfTools != null) {
            return
        }

        pdfTools = ComposeView(container.context).apply {
            // Only as tall as the tools, so that the view sits over as little of the page as possible.
            layoutParams = CoordinatorLayout.LayoutParams(
                CoordinatorLayout.LayoutParams.MATCH_PARENT,
                CoordinatorLayout.LayoutParams.WRAP_CONTENT,
            )

            setContent { PdfToolsHost() }
        }

        anchorTools(AcornWindowSize.isLargeWindow(container.context))
        container.addView(pdfTools)
    }

    override fun stop() {
        container.removeView(pdfTools)
        pdfTools = null
    }

    /**
     * Anchors the PDF tools to the toolbar heights and adjusts when changed.
     *
     * @param isLargeWindow Used to determine if the device should be treated as a tablet.
     */
    private fun anchorTools(isLargeWindow: Boolean) {
        val params = pdfTools?.layoutParams as? CoordinatorLayout.LayoutParams ?: return

        if (isLargeWindow) {
            // Tablet UI aligns to the top to form more of a toolbar.
            params.gravity = Gravity.TOP
            params.topMargin = topToolbarHeight()
            params.bottomMargin = 0
        } else {
            // Phone UI aligns to the bottom to form a set of FABs.
            params.gravity = Gravity.BOTTOM
            params.topMargin = 0
            params.bottomMargin = bottomToolbarHeight()
        }

        pdfTools?.layoutParams = params
    }

    @Composable
    private fun PdfToolsHost() {
        val isLargeWindow = AcornWindowSize.isLargeWindow()
        val configuration = LocalConfiguration.current
        LaunchedEffect(configuration) {
            anchorTools(isLargeWindow)
        }

        FirefoxTheme {
            PdfToolsContent(
                browserStore = browserStore,
                isLargeWindow = isLargeWindow,
            )
        }
    }
}

/**
 * Shows the [PdfTools] while the selected tab is displaying a PDF, and nothing otherwise.
 *
 * @param browserStore The [BrowserStore] to observe the PDF status of the selected tab.
 * @param isLargeWindow Used to determine if the device should be treated as a tablet.
 */
@Composable
internal fun PdfToolsContent(
    browserStore: BrowserStore,
    isLargeWindow: Boolean,
) {
    val isPdf by remember {
        browserStore.stateFlow.map { it.selectedTab?.content?.isPdf == true }
    }.collectAsStateWithLifecycle(
        initialValue = browserStore.state.selectedTab?.content?.isPdf == true,
    )

    if (isPdf) {
        PdfTools(
            isLargeWindow = isLargeWindow,
            // Bug 2054910
            onSignClick = {},
            // Bug 2054916
            onDownloadClick = {},
            // Bug 2054917
            onPrintClick = {},
            // Bug 2054918
            onShareClick = {},
        )
    }
}
