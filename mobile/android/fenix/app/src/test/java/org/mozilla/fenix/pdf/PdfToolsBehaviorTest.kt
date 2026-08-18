/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.fenix.pdf

import android.view.Gravity
import android.view.View
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.isVisible
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.mozilla.fenix.R
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

private const val CHROME_HEIGHT = 100
private const val NAV_BAR_HEIGHT = 60

@RunWith(RobolectricTestRunner::class)
class PdfToolsBehaviorTest {
    private val container = CoordinatorLayout(ApplicationProvider.getApplicationContext())

    private val tools =
        View(container.context).apply {
            layoutParams =
                CoordinatorLayout.LayoutParams(
                    CoordinatorLayout.LayoutParams.MATCH_PARENT,
                    CoordinatorLayout.LayoutParams.WRAP_CONTENT,
                )
        }

    private val layoutParams: CoordinatorLayout.LayoutParams
        get() = tools.layoutParams as CoordinatorLayout.LayoutParams

    private fun addChrome(id: Int, height: Int = CHROME_HEIGHT, isVisible: Boolean = true): View =
        View(container.context).apply {
            this.id = id
            this.isVisible = isVisible
            container.addView(this)
            // Robolectric does not lay out the container, so the height is applied directly.
            layout(0, 0, 500, height)
        }

    private fun behavior(isAddressBarAtBottom: Boolean) =
        PdfToolsBehavior(isAddressBarAtBottom = isAddressBarAtBottom)

    @Test
    @Config(qualifiers = "sw400dp")
    fun `on a phone the tools are inset from the bottom chrome`() {
        addChrome(R.id.navigation_bar)

        behavior(isAddressBarAtBottom = true).onLayoutChild(container, tools, View.LAYOUT_DIRECTION_LTR)

        assertEquals(Gravity.BOTTOM, layoutParams.gravity)
        assertEquals(CHROME_HEIGHT, layoutParams.bottomMargin)
        assertEquals(0, layoutParams.topMargin)
    }

    @Test
    @Config(qualifiers = "sw800dp")
    fun `on a tablet the tools are inset from the top chrome`() {
        addChrome(R.id.composable_toolbar)

        behavior(isAddressBarAtBottom = false).onLayoutChild(container, tools, View.LAYOUT_DIRECTION_LTR)

        assertEquals(Gravity.TOP, layoutParams.gravity)
        assertEquals(CHROME_HEIGHT, layoutParams.topMargin)
        assertEquals(0, layoutParams.bottomMargin)
    }

    @Test
    @Config(qualifiers = "sw400dp")
    fun `when the address bar and nav bar are both at the bottom the inset covers both`() {
        addChrome(R.id.composable_toolbar)
        addChrome(R.id.navigation_bar, height = NAV_BAR_HEIGHT)

        behavior(isAddressBarAtBottom = true).onLayoutChild(container, tools, View.LAYOUT_DIRECTION_LTR)

        assertEquals(CHROME_HEIGHT + NAV_BAR_HEIGHT, layoutParams.bottomMargin)
    }

    @Test
    @Config(qualifiers = "sw400dp")
    fun `when the address bar is at the top and the tools are at the bottom the inset is for the nav bar`() {
        addChrome(R.id.composable_toolbar)
        addChrome(R.id.navigation_bar, height = NAV_BAR_HEIGHT)

        behavior(isAddressBarAtBottom = false).onLayoutChild(container, tools, View.LAYOUT_DIRECTION_LTR)

        assertEquals(NAV_BAR_HEIGHT, layoutParams.bottomMargin)
    }

    @Test
    @Config(qualifiers = "sw800dp")
    fun `on a tablet with the address bar at the bottom the tools are not inset`() {
        addChrome(R.id.composable_toolbar)

        behavior(isAddressBarAtBottom = true).onLayoutChild(container, tools, View.LAYOUT_DIRECTION_LTR)

        assertEquals(Gravity.TOP, layoutParams.gravity)
        assertEquals(0, layoutParams.topMargin)
    }

    @Test
    @Config(qualifiers = "sw400dp")
    fun `when the chrome is not visible the tools are not inset from it`() {
        addChrome(R.id.navigation_bar, isVisible = false)

        behavior(isAddressBarAtBottom = true).onLayoutChild(container, tools, View.LAYOUT_DIRECTION_LTR)

        assertEquals(0, layoutParams.bottomMargin)
    }

    @Test
    @Config(qualifiers = "sw400dp")
    fun `layout doesn't depend on an unrelated view`() {
        val navBar = addChrome(R.id.navigation_bar)
        val unrelated = addChrome(R.id.findInPageView)

        val behavior = behavior(isAddressBarAtBottom = true)

        assertTrue(behavior.layoutDependsOn(container, tools, navBar))
        assertFalse(behavior.layoutDependsOn(container, tools, unrelated))
    }

    @Test
    @Config(qualifiers = "sw400dp")
    fun `when the tracked chrome is scrolled away the tools follow it`() {
        val navBar = addChrome(R.id.navigation_bar)
        navBar.translationY = 40f

        val behavior = behavior(isAddressBarAtBottom = true)

        assertTrue(behavior.onDependentViewChanged(container, tools, navBar))
        assertEquals(40f, tools.translationY, 0f)
    }

    @Test
    @Config(qualifiers = "sw400dp")
    fun `when two pieces of chrome are scrolled away the tools follow the total offset`() {
        val addressBar = addChrome(R.id.composable_toolbar)
        val navBar = addChrome(R.id.navigation_bar, height = NAV_BAR_HEIGHT)
        addressBar.translationY = CHROME_HEIGHT.toFloat()
        navBar.translationY = NAV_BAR_HEIGHT.toFloat()

        val behavior = behavior(isAddressBarAtBottom = true)
        behavior.onDependentViewChanged(container, tools, navBar)

        // The tools end up flush with the edge, having moved by the whole inset.
        assertEquals((CHROME_HEIGHT + NAV_BAR_HEIGHT).toFloat(), tools.translationY, 0f)
    }

    @Test
    @Config(qualifiers = "sw400dp")
    fun `when the tools already match the tracked chrome no change is reported`() {
        val navBar = addChrome(R.id.navigation_bar)

        assertFalse(behavior(isAddressBarAtBottom = true).onDependentViewChanged(container, tools, navBar))
    }
}
