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
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import mozilla.components.browser.state.action.EngineAction
import mozilla.components.browser.state.selector.selectedTab
import mozilla.components.browser.state.state.BrowserState
import mozilla.components.browser.state.store.BrowserStore
import mozilla.components.compose.base.theme.layout.AcornWindowSize
import mozilla.components.support.base.feature.LifecycleAwareFeature
import mozilla.telemetry.glean.private.NoExtras
import org.mozilla.fenix.GleanMetrics.PdfViewer
import org.mozilla.fenix.pdf.ui.PdfTools
import org.mozilla.fenix.theme.FirefoxTheme

/**
 * This integration is responsible for adding or removing PDF tools and properly anchoring it to the browser. [PdfTools]
 * only show when a PDF is displayed on the browser.
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

        pdfTools =
            ComposeView(container.context).apply {
                layoutParams =
                    CoordinatorLayout.LayoutParams(
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

    /** Saves the PDF the selected tab is displaying to the device. */
    internal fun handleDownloadClick() {
        PdfViewer.downloadTapped.record(NoExtras())
        browserStore.state.selectedTabId?.let {
            browserStore.dispatch(EngineAction.SaveToPdfAction(it))
        }
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
                onDownloadClick = ::handleDownloadClick,
            )
        }
    }
}

/**
 * [PdfTools] are only shown when the browser is on a PDF page.
 *
 * @param browserStore Used to observe the PDF status of the selected tab.
 * @param isLargeWindow Used to determine if the device should be treated as a tablet.
 * @param onDownloadClick Invoked when the user activates the download PDF button.
 */
@Composable
internal fun PdfToolsContent(
    browserStore: BrowserStore,
    isLargeWindow: Boolean,
    onDownloadClick: () -> Unit,
) {
    val isPdf by remember {
        browserStore.stateFlow.map { it.isSelectedTabPdf }.distinctUntilChanged()
    }
        .collectAsStateWithLifecycle(initialValue = browserStore.state.isSelectedTabPdf)

    if (isPdf) {
        PdfTools(
            isLargeWindow = isLargeWindow,
            // Bug 2054910
            onSignClick = {},
            onDownloadClick = onDownloadClick,
            // Bug 2054917
            onPrintClick = {},
            // Bug 2054918
            onShareClick = {},
        )
    }
}

private val BrowserState.isSelectedTabPdf: Boolean
    get() = selectedTab?.content?.isPdf == true
