package com.liftric.apt.aptRepository

import com.liftric.apt.service.getPoolBucketKey
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class GetPoolBucketKeyTest {
    @Test
    fun `test getPoolBucketKey method`() {
        val fileName = "foobar_1.0.0-1_all.deb"
        val component = "main"
        val expectedKey = "pool/main/f/foobar/foobar_1.0.0-1_all.deb"

        val actualKey = getPoolBucketKey(fileName, component)
        assertEquals(expectedKey, actualKey)
    }
}