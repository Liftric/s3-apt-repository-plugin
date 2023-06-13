package com.liftric.apt

import com.liftric.apt.model.PackagesInfo
import com.liftric.apt.model.readPackagesFile
import com.liftric.apt.model.toFileString
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.io.File

class PackagesInfoTest {
    @Test
    fun `test toFileString method`() {
        val packagesInfo = PackagesInfo(
            packageInfo = "MyPackage",
            version = "1.0",
            architecture = "amd64",
            maintainer = "maintainer@example.com",
            installedSize = "1234",
            depends = "some dependency",
            conflicts = "some conflict",
            replaces = "some replacement",
            provides = "some provider",
            preDepends = "some pre-dependency",
            recommends = "some recommendation",
            suggests = "some suggestion",
            enhances = "some enhancement",
            builtUsing = "some build tool",
            section = "some section",
            priority = "some priority",
            homepage = "some homepage",
            description = "some description",
            fileName = "pool/main/m/mypackage/mypackage_1.0_amd64.deb",
            size = 1234,
            sha1 = "abcd1234",
            sha256 = "abcd1234",
            md5sum = "abcd1234"
        )

        val fileString = packagesInfo.toFileString().trim()

        val expectedString = """
            Package: MyPackage
            Version: 1.0
            Architecture: amd64
            Maintainer: maintainer@example.com
            Installed-Size: 1234
            Depends: some dependency
            Conflicts: some conflict
            Replaces: some replacement
            Provides: some provider
            Pre-Depends: some pre-dependency
            Recommends: some recommendation
            Suggests: some suggestion
            Enhances: some enhancement
            Built-Using: some build tool
            Section: some section
            Priority: some priority
            Homepage: some homepage
            Description: some description
            Filename: pool/main/m/mypackage/mypackage_1.0_amd64.deb
            Size: 1234
            SHA1: abcd1234
            SHA256: abcd1234
            MD5sum: abcd1234
            """.trimIndent().trim()

        assertEquals(expectedString, fileString)
    }

    @Test
    fun `test readPackagesFile method`() {
        val data = """
            Package: MyPackage
            Version: 1.0
            Architecture: amd64
            Maintainer: maintainer@example.com
            Installed-Size: 1234
            Depends: some dependency
            Conflicts: some conflict
            Replaces: some replacement
            Provides: some provider
            Pre-Depends: some pre-dependency
            Recommends: some recommendation
            Suggests: some suggestion
            Enhances: some enhancement
            Built-Using: some build tool
            Section: some section
            Priority: some priority
            Homepage: some homepage
            Description: some description
            Filename: pool/main/m/mypackage/mypackage_1.0_amd64.deb
            Size: 1234
            SHA1: abcd1234
            SHA256: abcd1234
            MD5sum: abcd1234
        """.trimIndent()

        val tempFile = File.createTempFile("prefix", "suffix").apply {
            writeText(data)
            deleteOnExit()
        }

        val packages = readPackagesFile(tempFile)

        assertEquals(1, packages.size)
        val packageInfo = packages.first()
        assertEquals("MyPackage", packageInfo.packageInfo)
        assertEquals("1.0", packageInfo.version)
        assertEquals("amd64", packageInfo.architecture)
        assertEquals("maintainer@example.com", packageInfo.maintainer)
        assertEquals("1234", packageInfo.installedSize)
        assertEquals("some dependency", packageInfo.depends)
        assertEquals("some conflict", packageInfo.conflicts)
        assertEquals("some replacement", packageInfo.replaces)
        assertEquals("some provider", packageInfo.provides)
        assertEquals("some pre-dependency", packageInfo.preDepends)
        assertEquals("some recommendation", packageInfo.recommends)
        assertEquals("some suggestion", packageInfo.suggests)
        assertEquals("some enhancement", packageInfo.enhances)
        assertEquals("some build tool", packageInfo.builtUsing)
        assertEquals("some section", packageInfo.section)
        assertEquals("some priority", packageInfo.priority)
        assertEquals("some homepage", packageInfo.homepage)
        assertEquals("some description", packageInfo.description)
        assertEquals("pool/main/m/mypackage/mypackage_1.0_amd64.deb", packageInfo.fileName)
        assertEquals(1234, packageInfo.size)
        assertEquals("abcd1234", packageInfo.sha1)
        assertEquals("abcd1234", packageInfo.sha256)
        assertEquals("abcd1234", packageInfo.md5sum)
    }
}
