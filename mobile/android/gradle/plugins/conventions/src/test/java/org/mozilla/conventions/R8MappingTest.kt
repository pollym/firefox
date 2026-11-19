/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.conventions

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

/** The mapping is based on R8 9.5 output for Fenix. */
class R8MappingTest {
    private val mapping = R8Mapping.parse(
        $$"""
        # compiler: R8
        com.google.protobuf.Internal$ProtobufList -> km5:
        org.mozilla.fenix.datastore.CustomizedMenuRecord -> a.b:
        # {"id":"sourceFile","fileName":"CustomizedMenuRecord.java"}
            com.google.protobuf.Internal$ProtobufList sections_ -> sections_
              # {"id":"com.android.tools.r8.residualsignature","signature":"Lkm5;"}
            int schemaVersion_ -> c
              # {"id":"com.android.tools.r8.residualsignature","signature":"Z"}
            java.lang.String[] ids_ -> ids_
              # {"id":"com.android.tools.r8.residualsignature","signature":"[Lkm5;"}
            1:5:void <clinit>():1279:1279 -> <clinit>
            4:5:com.google.protobuf.Internal$ProtobufList com.google.protobuf.GeneratedMessageLite.emptyProtobufList():1565 -> <init>
            java.lang.Object dynamicMethod(com.google.protobuf.GeneratedMessageLite$MethodToInvoke,java.lang.Object,java.lang.Object) -> d
            boolean isNavigationSectionMoved_ -> isNavigationSectionMoved_
            1:3:void addAllSections(java.lang.Iterable):666 -> e
              # {"id":"com.android.tools.r8.residualsignature","signature":"(Ljava/util/ArrayList;)V"}
        """.trimIndent().lineSequence(),
    )
    private val record = mapping.classes.getValue("org.mozilla.fenix.datastore.CustomizedMenuRecord")

    private fun field(name: String) = record.fields.single { it.name == name }

    @Test
    fun `classes are listed by original name, with the name R8 gave them`() {
        assertEquals(
            setOf("com.google.protobuf.Internal\$ProtobufList", "org.mozilla.fenix.datastore.CustomizedMenuRecord"),
            mapping.classes.keys,
        )
        assertEquals("a.b", record.newName)
    }

    @Test
    fun `a field's new type uses original class names`() {
        assertEquals("com.google.protobuf.Internal\$ProtobufList", field("sections_").newType)
        assertEquals(false, field("sections_").isRetyped)
    }

    @Test
    fun `a field whose type R8 changed is retyped`() {
        assertEquals("boolean", field("schemaVersion_").newType)
        assertEquals(true, field("schemaVersion_").isRetyped)
    }

    @Test
    fun `an array field's new type keeps its dimensions`() {
        assertEquals("com.google.protobuf.Internal\$ProtobufList[]", field("ids_").newType)
    }

    @Test
    fun `a field R8 renamed is renamed`() {
        assertEquals(true, field("schemaVersion_").isRenamed)
        assertEquals(false, field("sections_").isRenamed)
    }

    @Test
    fun `the residual signature of a method is not taken for the field before it`() {
        assertNull(field("isNavigationSectionMoved_").newType)
    }

    @Test
    fun `methods are listed by original signature, without methods inlined into them`() {
        assertEquals(
            setOf(
                "void <clinit>()",
                $$"java.lang.Object dynamicMethod(com.google.protobuf.GeneratedMessageLite$MethodToInvoke,java.lang.Object,java.lang.Object)",
                "void addAllSections(java.lang.Iterable)",
            ),
            record.methodSignatures,
        )
    }
}
