/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.conventions

import com.android.tools.r8.CompilationMode
import com.android.tools.r8.D8
import com.android.tools.r8.D8Command
import com.android.tools.r8.DexIndexedConsumer
import com.android.tools.r8.inspector.ClassInspector
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.file.Directory
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFile
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.tasks.Classpath
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import java.io.File
import java.nio.file.Path
import java.util.zip.ZipFile

/**
 * Fails the build if R8 changed or removed an instance field of a protobuf-lite message class of the project.
 *
 * Protobuf-lite reads and writes message fields through `Unsafe`, assuming the types declared in the `.proto`, so R8
 * must leave them as they are (bug 2094206). This compares the fields before R8 with the fields in the APK. Classes
 * may be renamed, since protobuf-lite refers to them by `Class`, but fields may not, since it looks them up by name.
 */
abstract class CheckProtobufFieldsKept : DefaultTask() {
    /** Jars holding the project's classes before R8 runs. */
    @get:Classpath abstract val classJars: ListProperty<RegularFile>

    /** Directories holding the project's classes before R8 runs. */
    @get:Classpath abstract val classDirs: ListProperty<Directory>

    /** Directory holding the variant's APKs. */
    @get:InputDirectory
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val apkDir: DirectoryProperty

    /** R8's mapping file, used to map renamed classes back to their original names. */
    @get:InputFile
    @get:PathSensitive(PathSensitivity.NONE)
    abstract val mappingFile: RegularFileProperty

    /** R8's `usage.txt`, listing the classes and members it removed. */
    @get:InputFile
    @get:PathSensitive(PathSensitivity.NONE)
    abstract val usageFile: RegularFileProperty

    @TaskAction
    fun check() {
        val inputs = classDirs.get().flatMap { dir ->
            dir.asFile.walk().filter { it.isFile && it.extension == "class" }.map { it.toPath() }.toList()
        } + classJars.get().map { it.asFile.toPath() }
        val messages = instanceFields(inputs) { it.isProtobufLiteMessage() }

        val apk = apkDir.get().asFile.walk().firstOrNull { it.isFile && it.extension == "apk" }
            ?: throw GradleException("No APK in ${apkDir.get().asFile}")
        val originalNames = originalClassNames(mappingFile.get().asFile)
        val output = instanceFields(extractDex(apk)) {
            it.classReference.typeName.let { name -> originalNames[name] ?: name } in messages
        }

        val problems = protobufFieldProblems(messages, output, originalNames, removedClasses(usageFile.get().asFile))
        if (problems.isNotEmpty()) {
            throw GradleException(
                "R8 changed protobuf-lite message fields, which protobuf-lite then reads and writes incorrectly:\n" +
                    problems.joinToString("\n") { "  $it" } +
                    "\nKeep them with -keepclassmembers class * extends com.google.protobuf.GeneratedMessageLite " +
                    "{ <fields>; }, without allowoptimization or allowobfuscation.",
            )
        }
    }

    /**
     * Returns the instance fields, as field name to type descriptor, of the classes among [programFiles] that
     * [include] accepts, by class name.
     */
    private fun instanceFields(
        programFiles: List<Path>,
        include: (ClassInspector) -> Boolean,
    ): Map<String, Map<String, String>> {
        val result = mutableMapOf<String, Map<String, String>>()
        D8.run(
            D8Command.builder()
                .addProgramFiles(programFiles)
                .setMode(CompilationMode.DEBUG)
                .setMinApiLevel(NO_DEX_LIMITS_MIN_API)
                .setDisableDesugaring(true)
                .setProgramConsumer(DexIndexedConsumer.emptyConsumer())
                .addOutputInspection { inspector ->
                    inspector.forEachClass { classInspector ->
                        if (!include(classInspector)) return@forEachClass
                        val fields = mutableMapOf<String, String>()
                        classInspector.forEachField { field ->
                            if (!field.isStatic()) {
                                fields[field.fieldReference.fieldName] = field.fieldReference.fieldType.descriptor
                            }
                        }
                        synchronized(result) { result[classInspector.classReference.typeName] = fields }
                    }
                }
                .build(),
        )
        return result
    }

    /** Protoc generates `dynamicMethod`, through which protobuf-lite reaches the message's fields, in every message. */
    private fun ClassInspector.isProtobufLiteMessage(): Boolean {
        var found = false
        forEachMethod { method ->
            val reference = method.methodReference
            if (reference.methodName == "dynamicMethod" &&
                reference.formalTypes.map { it.typeName } == DYNAMIC_METHOD_PARAMETERS
            ) {
                found = true
            }
        }
        return found
    }

    private fun extractDex(apk: File): List<Path> {
        val dir = File(temporaryDir, "dex")
        dir.deleteRecursively()
        dir.mkdirs()
        return ZipFile(apk).use { zip ->
            zip.entries().asSequence().filter { DEX_ENTRY.matches(it.name) }.map { entry ->
                val file = File(dir, entry.name)
                zip.getInputStream(entry).use { input -> file.outputStream().use { input.copyTo(it) } }
                file.toPath()
            }.toList()
        }
    }

    private fun originalClassNames(mapping: File): Map<String, String> =
        mapping.useLines { lines ->
            lines.mapNotNull { MAPPING_CLASS.matchEntire(it) }.associate { it.groupValues[2] to it.groupValues[1] }
        }

    private fun removedClasses(usage: File): Set<String> =
        usage.useLines { lines -> lines.filter { it.isNotEmpty() && !it[0].isWhitespace() && !it.endsWith(":") }.toSet() }

    private companion object {
        val DYNAMIC_METHOD_PARAMETERS = listOf(
            "com.google.protobuf.GeneratedMessageLite\$MethodToInvoke",
            "java.lang.Object",
            "java.lang.Object",
        )
        val DEX_ENTRY = Regex("""classes\d*\.dex""")
        val MAPPING_CLASS = Regex("""^(\S+) -> (\S+):$""")

        /** Lifts the single dex file limit and D8's need to desugar; the output is only inspected, never used. */
        const val NO_DEX_LIMITS_MIN_API = 26
    }
}

/**
 * Returns a description of each instance field of [messages], the protobuf-lite message classes before R8, that is not
 * in [output] with the same name and type, unless R8 removed the whole message class ([removedClasses]).
 * Both maps are class name to field name to type descriptor. Class names in [output], including those in its type
 * descriptors, are mapped back through [originalNames], from R8's name to the original name.
 */
internal fun protobufFieldProblems(
    messages: Map<String, Map<String, String>>,
    output: Map<String, Map<String, String>>,
    originalNames: Map<String, String>,
    removedClasses: Set<String>,
): List<String> {
    fun original(descriptor: String) = OBJECT_DESCRIPTOR.replace(descriptor) { match ->
        val name = match.groupValues[1].replace('/', '.')
        "L${(originalNames[name] ?: name).replace('.', '/')};"
    }
    val originalOutput = output.entries.associate { (name, fields) ->
        (originalNames[name] ?: name) to fields.mapValues { original(it.value) }
    }

    return messages.filterKeys { it !in removedClasses }.flatMap { (className, fields) ->
        val outputFields = originalOutput[className] ?: return@flatMap listOf("$className is missing from the APK")
        fields.mapNotNull { (name, type) ->
            when (val outputType = outputFields[name]) {
                type -> null
                null -> "$className.$name ($type) is missing, removed or renamed"
                else -> "$className.$name changed from $type to $outputType"
            }
        }
    }.sorted()
}

private val OBJECT_DESCRIPTOR = Regex("""L([^;]+);""")
