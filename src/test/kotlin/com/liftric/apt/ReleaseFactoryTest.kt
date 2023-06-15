package com.liftric.apt

import com.liftric.apt.service.ReleaseFactory
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.io.File

class ReleaseFactoryTest {
    @Test
    fun `test parseReleaseFile`() {
        val testReleaseFile = File("src/test/resources/Release")
        val releaseInfo = ReleaseFactory.parseReleaseFile(testReleaseFile)

        assertEquals("Liftric", releaseInfo.origin)
        assertEquals("Liftric", releaseInfo.label)
        assertEquals("stable", releaseInfo.suite)
        assertEquals("main", releaseInfo.components)
        assertEquals("all", releaseInfo.architectures)
        assertEquals(null, releaseInfo.description)
    }
}
