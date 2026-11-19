/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.conventions

import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction

/**
 * Fails the build if R8 changed the type of, renamed or removed a field of a protobuf-lite message class.
 *
 * Protobuf-lite looks message fields up by name and reads and writes them through `Unsafe`, assuming the types
 * declared in the `.proto`, so R8 must leave them as they are (bug 2094206). R8 records each field it renamed or
 * changed the type of in its mapping, and each member it removed in `usage.txt`.
 */
abstract class CheckProtobufFieldsKept : DefaultTask() {
    @get:InputFile
    @get:PathSensitive(PathSensitivity.NONE)
    abstract val mappingFile: RegularFileProperty

    @get:InputFile
    @get:PathSensitive(PathSensitivity.NONE)
    abstract val usageFile: RegularFileProperty

    @TaskAction
    fun check() {
        val mapping = mappingFile.get().asFile.useLines { R8Mapping.parse(it) }
        val usage = usageFile.get().asFile.useLines { R8Usage.parse(it) }
        val problems = protobufFieldProblems(mapping, usage)
        if (problems.isNotEmpty()) {
            throw GradleException(
                "R8 changed protobuf-lite message fields, which protobuf-lite then reads and writes incorrectly:\n" +
                    problems.joinToString("\n") { "  $it" } +
                    "\nKeep them with -keepclassmembers class * extends com.google.protobuf.GeneratedMessageLite " +
                    "{ <fields>; }, without allowshrinking, allowoptimization or allowobfuscation.",
            )
        }
    }
}

/**
 * Returns a description of each field of a protobuf-lite message class that R8 renamed or changed the type of,
 * according to its [mapping], or removed, according to its [usage]. Removed static fields are ignored, since
 * protobuf-lite does not read them.
 */
internal fun protobufFieldProblems(mapping: R8Mapping, usage: R8Usage): List<String> =
    mapping.classes.values.filter { it.isProtobufLiteMessage() }.flatMap { message ->
        val className = message.originalName
        val changed = message.fields.flatMap { field ->
            listOfNotNull(
                "$className.${field.name} renamed to ${field.newName}".takeIf { field.isRenamed },
                "$className.${field.name} changed from ${field.type} to ${field.newType}".takeIf { field.isRetyped },
            )
        }
        val removed = usage.removedMembers[className].orEmpty()
            .filter { !it.isMethod && !it.isStatic }
            .map { "$className: removed ${it.declaration}" }
        changed + removed
    }.sorted()

/** Protoc generates `dynamicMethod`, through which protobuf-lite reaches the fields, in every message class. */
private fun MappedClass.isProtobufLiteMessage() =
    originalName != GENERATED_MESSAGE_LITE && DYNAMIC_METHOD in methodSignatures

private const val GENERATED_MESSAGE_LITE = "com.google.protobuf.GeneratedMessageLite"
private const val DYNAMIC_METHOD =
    "java.lang.Object dynamicMethod(com.google.protobuf.GeneratedMessageLite\$MethodToInvoke,java.lang.Object,java.lang.Object)"
