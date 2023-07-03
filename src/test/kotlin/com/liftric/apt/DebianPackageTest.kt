package com.liftric.apt

import com.liftric.apt.model.DebianPackage
import com.liftric.apt.model.combineDebianPackages
import com.liftric.apt.model.removeDebianPackage
import com.liftric.apt.model.toFileString
import org.junit.jupiter.api.Test

class DebianPackageTest {
    private val package1 = DebianPackage(
        packageName = "foo",
        version = "1.0.0",
        architecture = "all",
        fileName = "foo_1.0.0_all.deb",
        size = "123",
        maintainer = null,
        installedSize = null,
        depends = null,
        conflicts = null,
        replaces = null,
        provides = null,
        preDepends = null,
        recommends = null,
        suggests = null,
        enhances = null,
        builtUsing = null,
        section = null,
        priority = null,
        homepage = null,
        description = null,
        sha1 = null,
        sha256 = null,
        md5sum = null
    )

    private val package2 = DebianPackage(
        packageName = "foo",
        version = "1.0.1",
        architecture = "all",
        fileName = "foo_1.0.1_all.deb",
        size = "123",
        maintainer = null,
        installedSize = null,
        depends = null,
        conflicts = null,
        replaces = null,
        provides = null,
        preDepends = null,
        recommends = null,
        suggests = null,
        enhances = null,
        builtUsing = null,
        section = null,
        priority = null,
        homepage = null,
        description = null,
        sha1 = null,
        sha256 = null,
        md5sum = null
    )

    private val package3 = DebianPackage(
        packageName = "foo",
        version = "1.0.2",
        architecture = "all",
        fileName = "foo_1.0.2_all.deb",
        size = "234",
        maintainer = null,
        installedSize = null,
        depends = null,
        conflicts = null,
        replaces = null,
        provides = null,
        preDepends = null,
        recommends = null,
        suggests = null,
        enhances = null,
        builtUsing = null,
        section = null,
        priority = null,
        homepage = null,
        description = null,
        sha1 = null,
        sha256 = null,
        md5sum = null
    )

    @Test
    fun `test combineDebianPackages with different versions`() {
        val packageList = listOf(package1, package2)
        val combined = packageList.combineDebianPackages(package3)

        assert(combined.contains(package1))
        assert(combined.contains(package2))
        assert(combined.contains(package3))
        assert(combined.size == 3)
    }

    @Test
    fun `test combineDebianPackages with duplicate version`() {
        val packageList = listOf(package1, package2)
        val packageDuplicate = package2.copy(size = "234")
        val combined = packageList.combineDebianPackages(packageDuplicate)

        assert(combined.contains(package1))
        assert(!combined.contains(package2))
        assert(combined.contains(packageDuplicate))
        assert(combined.size == 2)
    }

    @Test
    fun `test removeDebianPackages`() {
        val packageList = listOf(package1, package2)
        val removePackage = package2.copy()
        val combined = packageList.removeDebianPackage(removePackage)

        assert(combined.contains(package1))
        assert(!combined.contains(package2))
        assert(!combined.contains(removePackage))
        assert(combined.size == 1)
    }

    @Test
    fun `test removeDebianPackages with a Package that is not there`() {
        val packageList = listOf(package1, package2)
        val combined = packageList.removeDebianPackage(package3)

        assert(combined.contains(package1))
        assert(combined.contains(package2))
        assert(!combined.contains(package3))
        assert(combined.size == 2)
    }

    @Test
    fun `test toFileString for DebianPackage`() {
        val expected = """
            Package: foo
            Version: 1.0.0
            Architecture: all
            Filename: foo_1.0.0_all.deb
            Size: 123
            
        """.trimIndent() + "\n"

        assert(expected == package1.toFileString())
    }

    @Test
    fun `test toFileString for List of DebianPackage`() {
        val packageList = listOf(package1, package2)
        val expected = """
            Package: foo
            Version: 1.0.0
            Architecture: all
            Filename: foo_1.0.0_all.deb
            Size: 123

            Package: foo
            Version: 1.0.1
            Architecture: all
            Filename: foo_1.0.1_all.deb
            Size: 123

        """.trimIndent() + "\n"

        assert(expected == packageList.toFileString())
    }
}
