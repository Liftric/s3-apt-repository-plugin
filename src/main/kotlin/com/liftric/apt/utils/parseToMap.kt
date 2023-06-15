package com.liftric.apt.utils

//TODO Add tests
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
