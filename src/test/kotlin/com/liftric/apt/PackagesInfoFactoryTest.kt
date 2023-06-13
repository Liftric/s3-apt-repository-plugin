package com.liftric.apt

import com.liftric.apt.service.PackagesInfoFactory
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.io.File

class PackagesInfoFactoryTest {

    @Test
    fun `test packages info extraction`() {
        val testDebFile = File("src/test/resources/foobar_1.0.0-1_all.deb")
        val factory = PackagesInfoFactory(testDebFile)
        val packagesInfo = factory.packagesInfo

        assertEquals("foobar", packagesInfo.packageInfo)
        assertEquals("1.0.0-1", packagesInfo.version)
        assertEquals("all", packagesInfo.architecture)
    }
}