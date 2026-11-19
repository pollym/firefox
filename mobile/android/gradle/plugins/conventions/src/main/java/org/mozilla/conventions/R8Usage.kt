/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.conventions

/**
 * The members that an R8 `usage.txt` lists as removed, by original class name.
 *
 * A class whose members were only partly removed ends with `:` and is followed by the indented declarations of its
 * removed members. A class removed entirely has no `:` and no members.
 */
internal class R8Usage(val removedMembers: Map<String, List<RemovedMember>>) {
    companion object {
        fun parse(lines: Sequence<String>): R8Usage {
            val removedMembers = mutableMapOf<String, MutableList<RemovedMember>>()
            var currentClass: String? = null
            lines.forEach { line ->
                if (line.isNotEmpty() && !line[0].isWhitespace()) {
                    currentClass = line.removeSuffix(":").takeIf { line.endsWith(":") }
                } else if (line.isNotBlank()) {
                    currentClass?.let { removedMembers.getOrPut(it) { mutableListOf() }.add(RemovedMember(line.trim())) }
                }
            }
            return R8Usage(removedMembers)
        }
    }
}

/** A removed member, declared with its modifiers, such as `public static final int ID` or `private void clear()`. */
internal data class RemovedMember(val declaration: String) {
    val isMethod get() = '(' in declaration
    val isStatic get() = "static" in declaration.substringBefore('(').split(' ')
}
