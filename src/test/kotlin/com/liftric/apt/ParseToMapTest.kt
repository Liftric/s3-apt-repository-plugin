package com.liftric.apt

import com.liftric.apt.utils.parseToMap
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class ParseToMapTest {
    @Test
    fun `when input is properly formatted, it should return a correct map`() {
        val input = """
            key1: value1
            key2: value2
            key3: value3
        """.trimIndent()
        val expectedMap = mapOf(
            "key1" to "value1",
            "key2" to "value2",
            "key3" to "value3"
        )
        assertEquals(expectedMap, parseToMap(input))

    }

    @Test
    fun `when input has extra spaces, it should still return a correct map`() {
        val input = """
            key1   :    value1   
            key2:      value2  
            key3 : value3
        """.trimIndent()
        val expectedMap = mapOf(
            "key1" to "value1",
            "key2" to "value2",
            "key3" to "value3"
        )
        assertEquals(expectedMap, parseToMap(input))
    }

    @Test
    fun `when input has multiline values, it should merge them into a single line`() {
        val input = """
            key1: value1
            key2: value with
                   multiple lines
            key3: value3
        """.trimIndent()
        val expectedMap = mapOf(
            "key1" to "value1",
            "key2" to "value with multiple lines",
            "key3" to "value3"
        )
        assertEquals(expectedMap, parseToMap(input))
    }
}
