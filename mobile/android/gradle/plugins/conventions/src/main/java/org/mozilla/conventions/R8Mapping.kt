/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.conventions

/**
 * The classes in an R8 mapping file, by original name.
 *
 * The mapping lists each class left in the app, followed by the lines of the members it records. These include the
 * class's methods and the fields R8 renamed or changed the type of, but not the fields it left alone. A member line
 * may be followed by comment lines of metadata, including a `residualsignature` if R8 changed the member's type.
 */
internal class R8Mapping(val classes: Map<String, MappedClass>) {
    companion object {
        fun parse(lines: Sequence<String>): R8Mapping {
            val classes = mutableListOf<ClassBuilder>()
            var currentField: FieldBuilder? = null

            lines.forEach { line ->
                val mappedClass = CLASS.matchEntire(line)
                val mappedField = FIELD.matchEntire(line)
                val mappedMethod = METHOD.matchEntire(line)
                when {
                    mappedClass != null -> {
                        val (originalName, newName) = mappedClass.destructured
                        classes.add(ClassBuilder(originalName, newName))
                        currentField = null
                    }
                    mappedField != null -> {
                        val (type, name, newName) = mappedField.destructured
                        currentField = FieldBuilder(type, name, newName).also { classes.lastOrNull()?.fields?.add(it) }
                    }
                    mappedMethod != null -> {
                        val (returnType, name, parameters) = mappedMethod.destructured
                        // Methods inlined into this one are listed by their qualified names.
                        if ('.' !in name) classes.lastOrNull()?.methodSignatures?.add("$returnType $name($parameters)")
                        currentField = null
                    }
                    line.startsWith(METADATA_PREFIX) -> {
                        if (RESIDUAL_SIGNATURE in line) currentField?.newType = SIGNATURE.find(line)?.groupValues?.get(1)
                    }
                    else -> currentField = null
                }
            }

            val originalNames = classes.associate { it.newName to it.originalName }
            return R8Mapping(classes.associate { it.originalName to it.build(originalNames) })
        }

        private val CLASS = Regex("""^(\S+) -> (\S+):$""")
        private val FIELD = Regex("""^ {4}(\S+) ([^\s(]+) -> (\S+)$""")
        private val METHOD = Regex("""^ {4}(?:\d+:\d+:)?(\S+) ([^\s(]+)\(([^)]*)\)(?::\d+)* -> \S+$""")
        private const val METADATA_PREFIX = "      # "
        private const val RESIDUAL_SIGNATURE = "\"com.android.tools.r8.residualsignature\""
        private val SIGNATURE = Regex(""""signature":"([^"]+)"""")
    }
}

/**
 * A class in an R8 mapping, with the original signatures of its methods, such as `int getId(java.lang.String)`, and
 * the fields R8 renamed or changed the type of.
 */
internal class MappedClass(
    val originalName: String,
    val newName: String,
    val fields: List<MappedField>,
    val methodSignatures: Set<String>,
)

/**
 * A field in an R8 mapping, with its original type and name. [newType] is the Java type R8 gave it, using original
 * class names, or null if R8 did not change it.
 */
internal data class MappedField(val type: String, val name: String, val newName: String, val newType: String?) {
    val isRenamed get() = newName != name
    val isRetyped get() = newType != null && newType != type
}

private class ClassBuilder(val originalName: String, val newName: String) {
    val fields = mutableListOf<FieldBuilder>()
    val methodSignatures = mutableSetOf<String>()

    fun build(originalNames: Map<String, String>) =
        MappedClass(originalName, newName, fields.map { it.build(originalNames) }, methodSignatures)
}

private class FieldBuilder(val type: String, val name: String, val newName: String) {
    var newType: String? = null

    fun build(originalNames: Map<String, String>) =
        MappedField(type, name, newName, newType?.let { javaTypeName(it, originalNames) })
}

/** Converts a type descriptor to a Java type name, mapping class names back through [originalNames]. */
private fun javaTypeName(descriptor: String, originalNames: Map<String, String>): String {
    val element = descriptor.trimStart('[')
    val name = PRIMITIVES[element] ?: element.removeSurrounding("L", ";").replace('/', '.').let {
        originalNames[it] ?: it
    }
    return name + "[]".repeat(descriptor.length - element.length)
}

private val PRIMITIVES = mapOf(
    "Z" to "boolean", "B" to "byte", "C" to "char", "S" to "short",
    "I" to "int", "J" to "long", "F" to "float", "D" to "double",
)
