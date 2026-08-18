/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.fenix.pdf

import android.view.Gravity
import android.view.View
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.children
import androidx.core.view.isVisible
import mozilla.components.compose.base.theme.layout.AcornWindowSize
import org.mozilla.fenix.R

/**
 * A [CoordinatorLayout.Behavior] that keeps the PDF tools clear of the browser chrome sharing their edge of the screen.
 *
 * The tools are inset by the height of that chrome and mirror its y-translation, so as the chrome is scrolled away the
 * tools follow it into the space it vacates rather than leaving a gap. Both are read from the chrome itself on every
 * layout pass, so no external notification is needed when the window size or the toolbar heights change.
 *
 * @param isAddressBarAtBottom Whether the address bar is at the bottom of the screen.
 */
class PdfToolsBehavior(
    isAddressBarAtBottom: Boolean,
) : CoordinatorLayout.Behavior<View>() {

    private val addressBarIds = listOf(R.id.toolbar, R.id.composable_toolbar)

    private val topEdgeIds = if (isAddressBarAtBottom) emptyList() else addressBarIds

    private val bottomEdgeIds =
        buildList {
            // The navigation bar's container holds the address bar too when both are at the bottom.
            add(R.id.navigation_bar)
            if (isAddressBarAtBottom) {
                addAll(addressBarIds)
            }
        }

    override fun layoutDependsOn(parent: CoordinatorLayout, child: View, dependency: View): Boolean =
        dependency.id in trackedIds(parent)

    override fun onLayoutChild(parent: CoordinatorLayout, child: View, layoutDirection: Int): Boolean {
        val params = child.layoutParams as? CoordinatorLayout.LayoutParams ?: return false
        val chrome = trackedChrome(parent)
        val inset = chrome.sumOf { it.height }

        if (isToolsAtTop(parent)) {
            // Tablet UI aligns to the top to form more of a toolbar.
            params.gravity = Gravity.TOP
            params.topMargin = inset
            params.bottomMargin = 0
        } else {
            // Phone UI aligns to the bottom to form a set of FABs.
            params.gravity = Gravity.BOTTOM
            params.topMargin = 0
            params.bottomMargin = inset
        }

        child.translationY = chrome.translationY()

        // The margins are read by the default layout that follows, so it does not need taking over.
        return false
    }

    override fun onDependentViewChanged(parent: CoordinatorLayout, child: View, dependency: View): Boolean {
        val translationY = trackedChrome(parent).translationY()

        if (child.translationY == translationY) {
            return false
        }

        child.translationY = translationY
        return true
    }

    // Read per layout pass rather than cached, as the window can be resized without the tools being recreated.
    private fun isToolsAtTop(parent: CoordinatorLayout) = AcornWindowSize.isLargeWindow(parent.context)

    private fun trackedIds(parent: CoordinatorLayout) = if (isToolsAtTop(parent)) topEdgeIds else bottomEdgeIds

    private fun trackedChrome(parent: CoordinatorLayout): List<View> {
        val ids = trackedIds(parent)
        return parent.children.filter { it.isVisible && it.id in ids }.toList()
    }

    /**
     * Each piece of chrome is translated away independently, so the space vacated along the tools' edge is the total of
     * their translations. Once all of it is hidden this equals the inset, leaving the tools flush with the edge.
     */
    private fun List<View>.translationY(): Float = map { it.translationY }.sum()
}
