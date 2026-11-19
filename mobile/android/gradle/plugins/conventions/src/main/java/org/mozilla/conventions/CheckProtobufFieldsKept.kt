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
        val problems = mappingFile.get().asFile.useLines { mapping ->
            usageFile.get().asFile.useLines { usage -> protobufFieldProblems(mapping, usage) }
        }
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
 * according to the lines of its [mapping], or removed, according to the lines of its [usage] file.
 */
internal fun protobufFieldProblems(mapping: Sequence<String>, usage: Sequence<String>): List<String> {
    val r8Mapping = readMapping(mapping)
    return (changedFieldProblems(r8Mapping) + removedFieldProblems(usage, r8Mapping.messages)).sorted()
}

/** What R8's mapping says about the classes of the app. */
private class R8Mapping(
    /** Original class names, by the names R8 gave them. */
    val originalNames: Map<String, String>,
    /** Original names of the protobuf-lite message classes. */
    val messages: Set<String>,
    /** The fields R8 renamed or changed the type of. The mapping does not list the other fields. */
    val changedFields: List<MappedField>,
)

/** A field listed in R8's mapping. [newType] is the type descriptor R8 gave it, if it changed the type. */
private class MappedField(val className: String, val type: String, val name: String, val newName: String) {
    var newType: String? = null
}

/**
 * Reads R8's mapping, in which each class line is followed by the lines of its members. A member line may be followed
 * by comment lines of metadata, including a `residualsignature` if R8 changed the member's type.
 */
private fun readMapping(lines: Sequence<String>): R8Mapping {
    val originalNames = mutableMapOf<String, String>()
    val messages = mutableSetOf<String>()
    val changedFields = mutableListOf<MappedField>()
    var currentClass: String? = null
    var currentField: MappedField? = null

    lines.forEach { line ->
        val mappedClass = MAPPED_CLASS.matchEntire(line)
        val mappedField = MAPPED_FIELD.matchEntire(line)
        when {
            mappedClass != null -> {
                val (originalName, newName) = mappedClass.destructured
                originalNames[newName] = originalName
                currentClass = originalName
                currentField = null
            }
            // Protoc generates dynamicMethod, through which protobuf-lite reaches the fields, in every message.
            DYNAMIC_METHOD in line -> {
                currentClass?.let { messages.add(it) }
                currentField = null
            }
            mappedField != null -> {
                val (type, name, newName) = mappedField.destructured
                currentField = MappedField(currentClass!!, type, name, newName).also { changedFields.add(it) }
            }
            line.startsWith(METADATA_PREFIX) -> {
                if (RESIDUAL_SIGNATURE in line) currentField?.newType = SIGNATURE.find(line)?.groupValues?.get(1)
            }
            else -> currentField = null
        }
    }
    messages.remove(GENERATED_MESSAGE_LITE)
    return R8Mapping(originalNames, messages, changedFields)
}

private fun changedFieldProblems(mapping: R8Mapping): List<String> =
    mapping.changedFields.filter { it.className in mapping.messages }.flatMap { field ->
        val newType = field.newType?.let { javaTypeName(it, mapping.originalNames) }
        listOfNotNull(
            "${field.className}.${field.name} renamed to ${field.newName}".takeIf { field.newName != field.name },
            "${field.className}.${field.name} changed from ${field.type} to $newType"
                .takeIf { newType != null && newType != field.type },
        )
    }

/**
 * Returns a description of each instance field of [messages] that [usage], the lines of R8's `usage.txt`, lists as
 * removed. Static fields are ignored, since protobuf-lite does not read them.
 *
 * In `usage.txt`, a class whose members were only partly removed ends with `:` and is followed by the indented lines
 * of its removed members. A class that was removed entirely has no `:`.
 */
private fun removedFieldProblems(usage: Sequence<String>, messages: Set<String>): List<String> {
    var currentClass: String? = null
    return usage.mapNotNull { line ->
        if (line.isNotEmpty() && !line[0].isWhitespace()) {
            currentClass = line.removeSuffix(":").takeIf { line.endsWith(":") }
            return@mapNotNull null
        }
        val member = line.trim()
        "$currentClass: removed $member".takeIf { currentClass in messages && isInstanceField(member) }
    }.toList()
}

/** Whether [member], as listed in `usage.txt` with its modifiers and type, is an instance field. */
private fun isInstanceField(member: String) = '(' !in member && "static" !in member.split(' ')

/** Converts a type descriptor to a Java type name, mapping class names back through [originalNames]. */
private fun javaTypeName(descriptor: String, originalNames: Map<String, String>): String {
    val element = descriptor.trimStart('[')
    val name = PRIMITIVES[element] ?: element.removeSurrounding("L", ";").replace('/', '.').let {
        originalNames[it] ?: it
    }
    return name + "[]".repeat(descriptor.length - element.length)
}

private const val GENERATED_MESSAGE_LITE = "com.google.protobuf.GeneratedMessageLite"
private const val DYNAMIC_METHOD =
    " dynamicMethod(com.google.protobuf.GeneratedMessageLite\$MethodToInvoke,java.lang.Object,java.lang.Object)"
private const val METADATA_PREFIX = "      # "
private const val RESIDUAL_SIGNATURE = "\"com.android.tools.r8.residualsignature\""
private val MAPPED_CLASS = Regex("""^(\S+) -> (\S+):$""")
private val MAPPED_FIELD = Regex("""^ {4}(\S+) ([^\s(]+) -> (\S+)$""")
private val SIGNATURE = Regex(""""signature":"([^"]+)"""")
private val PRIMITIVES = mapOf(
    "Z" to "boolean", "B" to "byte", "C" to "char", "S" to "short",
    "I" to "int", "J" to "long", "F" to "float", "D" to "double",
)
