/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.conventions

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

/** The mappings and usage files are excerpts of R8 9.5 output for a message with `uint32 schema_version = 1`. */
class CheckProtobufFieldsKeptTest {
    private val dynamicMethod = "    java.lang.Object dynamicMethod(com.google.protobuf.GeneratedMessageLite\$MethodToInvoke," +
        "java.lang.Object,java.lang.Object) -> dynamicMethod"

    private fun problems(mapping: String, usage: String = "") =
        protobufFieldProblems(mapping.lines().asSequence(), usage.lines().asSequence())

    @Test
    fun `fields that R8 left alone, apart from renaming their type's class, pass`() {
        val mapping = """
            com.google.protobuf.Internal${'$'}ProtobufList -> x:
            exp.Record -> exp.Record:
                com.google.protobuf.Internal${'$'}ProtobufList ids_ -> ids_
                  # {"id":"com.android.tools.r8.residualsignature","signature":"Lx;"}
        """.trimIndent() + "\n" + dynamicMethod
        assertEquals(emptyList<String>(), problems(mapping))
    }

    @Test
    fun `a field whose type R8 changed fails`() {
        val mapping = """
            exp.Record -> exp.Record:
                int schemaVersion_ -> schemaVersion_
                  # {"id":"com.android.tools.r8.residualsignature","signature":"Z"}
        """.trimIndent() + "\n" + dynamicMethod
        assertEquals(listOf("exp.Record.schemaVersion_ changed from int to boolean"), problems(mapping))
    }

    @Test
    fun `the residual signature of a method is not taken for the field before it`() {
        val mapping = """
            exp.Record -> exp.Record:
                int schemaVersion_ -> schemaVersion_
        """.trimIndent() + "\n" + dynamicMethod + "\n" +
            """      # {"id":"com.android.tools.r8.residualsignature","signature":"(La;Ljava/lang/Object;)Ljava/lang/Object;"}"""
        assertEquals(emptyList<String>(), problems(mapping))
    }

    @Test
    fun `a field R8 renamed fails`() {
        val mapping = """
            exp.Record -> l0:
                int schemaVersion_ -> i
        """.trimIndent() + "\n" + dynamicMethod.replace("-> dynamicMethod", "-> b")
        assertEquals(listOf("exp.Record.schemaVersion_ renamed to i"), problems(mapping))
    }

    @Test
    fun `an instance field R8 removed fails, a static one passes`() {
        val usage = """
            exp.Record:
                public static final int IDS_FIELD_NUMBER
                private com.google.protobuf.ProtobufArrayList values_
                private void clearIds()
        """.trimIndent()
        assertEquals(
            listOf("exp.Record: removed private com.google.protobuf.ProtobufArrayList values_"),
            problems("exp.Record -> exp.Record:\n$dynamicMethod", usage),
        )
    }

    @Test
    fun `fields of a class that is not a message are not checked`() {
        val mapping = """
            exp.Other -> a:
                int count_ -> b
                  # {"id":"com.android.tools.r8.residualsignature","signature":"Z"}
        """.trimIndent()
        assertEquals(emptyList<String>(), problems(mapping, "exp.Other:\n    private int value_"))
    }
}
