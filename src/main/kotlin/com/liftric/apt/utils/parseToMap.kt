package com.liftric.apt.utils

/**
 * This utility function, `parseToMap`, is used for parsing text-based files from an APT repository
 * such as Release or Packages files.
 *
 * Note: This function does not check if the input text is properly formatted. Malformed input may
 * lead to unexpected results.
 */

fun parseToMap(text: String): Map<String, String?> {
    val removeMultiLines = text
        .replace(Regex("\n\\s+"), " ")

    val parsedMap = removeMultiLines.split("\n").associate {
        val strings = it.split(": ")
        strings[0].trim() to
                strings.getOrNull(1)
                    ?.trim()
    }
    return parsedMap
}
