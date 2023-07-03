package com.liftric.apt

import com.liftric.apt.service.PackagesFactory
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.io.File

class PackagesFactoryTest {
    private val testDebFile = File("src/test/resources/foobar_1.0.0-1_all.deb")
    private val archs = setOf("all", "amd64")

    @Test
    fun `test parseDebianFile`() {
        val list = PackagesFactory.parseDebianFile(testDebFile, archs, "foobar_1.0.0-1_all.deb", null, null)

        assert(list.isNotEmpty())
        list.forEach {
            assertEquals("foobar", it.packageName)
            assertEquals("1.0.0-1", it.version)
            assert(archs.contains(it.architecture))
        }
    }

    @Test
    fun `test parseDebianFile with custom input`() {
        val list = PackagesFactory.parseDebianFile(testDebFile, archs, "foobar_1.0.0-1_all.deb", "foobaz", "1.0.0")

        assert(list.isNotEmpty())
        list.forEach {
            assertEquals("foobaz", it.packageName)
            assertEquals("1.0.0", it.version)
            assert(archs.contains(it.architecture))
        }
    }

    @Test
    fun `test parsePackagesFile`() {
        val testPackagesFile = File("src/integrationMain/resources/removePackage/Packages")
        val debianPackages = PackagesFactory.parsePackagesFile(testPackagesFile)

        assert(debianPackages.size == 2)
    }
}
