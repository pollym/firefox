/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.conventions

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class CheckProtobufFieldsKeptTest {
    private val record = "org.example.Record"
    private val messages = mapOf(record to mapOf("schemaVersion_" to "I", "sections_" to "Ljava/util/List;"))

    @Test
    fun `unchanged fields pass`() {
        assertEquals(emptyList<String>(), protobufFieldProblems(messages, messages, emptyMap(), emptySet()))
    }

    @Test
    fun `a field whose type R8 changed fails`() {
        val output = mapOf(record to mapOf("schemaVersion_" to "Z", "sections_" to "Ljava/util/List;"))
        assertEquals(
            listOf("$record.schemaVersion_ changed from I to Z"),
            protobufFieldProblems(messages, output, emptyMap(), emptySet()),
        )
    }

    @Test
    fun `a field R8 removed or renamed fails`() {
        val output = mapOf(record to mapOf("a" to "I", "sections_" to "Ljava/util/List;"))
        assertEquals(
            listOf("$record.schemaVersion_ (I) is missing, removed or renamed"),
            protobufFieldProblems(messages, output, emptyMap(), emptySet()),
        )
    }

    @Test
    fun `a message class missing from the output fails`() {
        assertEquals(
            listOf("$record is missing from the APK"),
            protobufFieldProblems(messages, emptyMap(), emptyMap(), emptySet()),
        )
    }

    @Test
    fun `a message class R8 removed passes`() {
        assertEquals(emptyList<String>(), protobufFieldProblems(messages, emptyMap(), emptyMap(), setOf(record)))
    }

    @Test
    fun `a message class and a field type that R8 renamed pass`() {
        val output = mapOf("a.b" to mapOf("schemaVersion_" to "I", "sections_" to "Lkm5;"))
        assertEquals(
            emptyList<String>(),
            protobufFieldProblems(messages, output, mapOf("a.b" to record, "km5" to "java.util.List"), emptySet()),
        )
    }
}
