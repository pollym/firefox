/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.conventions

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

/** The usage file is an excerpt of R8 9.5 output for Fenix. */
class R8UsageTest {
    private val usage = R8Usage.parse(
        """
        org.mozilla.fenix.datastore.BrowserMenuCustomization
        org.mozilla.fenix.datastore.SelectedPocketStoriesCategories:
            public static final int VALUES_FIELD_NUMBER
            private com.google.protobuf.ProtobufArrayList values_
            private void clearValues()
            public static org.mozilla.fenix.datastore.SelectedPocketStoriesCategories parseFrom(byte[])
        org.mozilla.fenix.datastore.SelectedPocketStoriesCategoriesOuterClass
        """.trimIndent().lineSequence(),
    )
    private val members = usage.removedMembers.getValue("org.mozilla.fenix.datastore.SelectedPocketStoriesCategories")

    @Test
    fun `members are listed under the class they were removed from`() {
        assertEquals(
            listOf(
                "public static final int VALUES_FIELD_NUMBER",
                "private com.google.protobuf.ProtobufArrayList values_",
                "private void clearValues()",
                "public static org.mozilla.fenix.datastore.SelectedPocketStoriesCategories parseFrom(byte[])",
            ),
            members.map { it.declaration },
        )
    }

    @Test
    fun `classes removed entirely have no members`() {
        assertEquals(setOf("org.mozilla.fenix.datastore.SelectedPocketStoriesCategories"), usage.removedMembers.keys)
    }

    @Test
    fun `members are told apart by whether they are methods and whether they are static`() {
        assertEquals(
            listOf(false to true, false to false, true to false, true to true),
            members.map { it.isMethod to it.isStatic },
        )
    }
}
