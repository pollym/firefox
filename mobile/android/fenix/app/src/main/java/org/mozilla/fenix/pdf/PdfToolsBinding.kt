/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.fenix.pdf

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import mozilla.components.browser.state.selector.selectedTab
import mozilla.components.browser.state.state.BrowserState
import mozilla.components.browser.state.store.BrowserStore
import mozilla.components.lib.state.helpers.AbstractBinding
import org.mozilla.fenix.utils.Settings

/**
 * Observes the browser store and settings to determine if the PDF tools should change
 * visibility through [onPdfToolsVisibilityChanged].
 *
 * @param browserStore The [BrowserStore] to observe the PDF status of the tab.
 * @param settings The [Settings] to check whether the PDF tools are enabled.
 * @param onPdfToolsVisibilityChanged Called when the PDF tools should change state.
 * @param dispatcher The [CoroutineDispatcher] to use will default to [Dispatchers.Main].
 */
class PdfToolsBinding(
    browserStore: BrowserStore,
    private val settings: Settings,
    private val onPdfToolsVisibilityChanged: (Boolean) -> Unit,
    dispatcher: CoroutineDispatcher = Dispatchers.Main,
) : AbstractBinding<BrowserState>(browserStore, dispatcher) {

    override suspend fun onState(flow: Flow<BrowserState>) {
        flow.map { it.selectedTab?.content?.isPdf == true && settings.enablePdfTools }
            .distinctUntilChanged()
            .collect(onPdfToolsVisibilityChanged)
    }
}
