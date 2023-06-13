package com.liftric.apt

import com.liftric.apt.model.*
import org.junit.jupiter.api.Test
import java.io.File
import org.junit.jupiter.api.Assertions.assertEquals

class ReleaseInfoTest {

    @Test
    fun `test parsing ReleaseInfo from file`() {
        val releaseInfoData = """
            Origin: MyOrigin
            Label: MyLabel
            Suite: MySuite
            Components: MyComponent
            Codename: MyCodename
            Date: MyDate
            Architectures: MyArchitectures
            Description: MyDescription
            Version: MyVersion
            NotAutomatic: MyNotAutomatic
            ValidUntil: MyValidUntil
            NotAutomatic: MyNotAutomatic
            ButAutomaticUpgrades: MyButAutomaticUpgrades
            Acquire-By-Hash: MyAcquireByHash
            Changelogs: MyChangelogs
            Snapshots: MySnapshots
        """.trimIndent()

        val tempFile = File.createTempFile("prefix", "suffix").apply {
            writeText(releaseInfoData)
            deleteOnExit()
        }

        val releaseInfo = parseReleaseFile(tempFile)

        assertEquals("MyOrigin", releaseInfo.origin)
        assertEquals("MyLabel", releaseInfo.label)
        assertEquals("MySuite", releaseInfo.suite)
        assertEquals("MyComponent", releaseInfo.components)
        assertEquals("MyCodename", releaseInfo.codename)
        assertEquals("MyDate", releaseInfo.date)
        assertEquals("MyArchitectures", releaseInfo.architectures)
        assertEquals("MyDescription", releaseInfo.description)
        assertEquals("MyVersion", releaseInfo.version)
        assertEquals("MyValidUntil", releaseInfo.validUntil)
        assertEquals("MyNotAutomatic", releaseInfo.notAutomatic)
        assertEquals("MyButAutomaticUpgrades", releaseInfo.butAutomaticUpgrades)
        assertEquals("MyAcquireByHash", releaseInfo.acquireByHash)
        assertEquals("MyChangelogs", releaseInfo.changelogs)
        assertEquals("MySnapshots", releaseInfo.snapshots)
    }

    @Test
    fun `test converting ReleaseInfo to file string`() {
        val releaseInfo = ReleaseInfo(
            origin = "MyOrigin",
            label = "MyLabel",
            suite = "MySuite",
            components = "MyComponent",
            codename = "MyCodename",
            date = "MyDate",
            architectures = "MyArchitectures",
            description = "MyDescription",
            version = "MyVersion",
            validUntil = "MyValidUntil",
            notAutomatic = "MyNotAutomatic",
            butAutomaticUpgrades = "MyButAutomaticUpgrades",
            acquireByHash = "MyAcquireByHash",
            changelogs = "MyChangelogs",
            snapshots = "MySnapshots",
            md5Sum = mutableListOf(MD5Sum(md5 = "MyMD5Sum", size = 123, filename = "md5Filename")),
            sha1 = mutableListOf(SHA1(sha1 = "MySHA1", size = 234, filename = "sha1Filename")),
            sha256 = mutableListOf(SHA256(sha256 = "MySHA256", size = 345, filename = "sha256Filename")),
            sha512 = mutableListOf(SHA512(sha512 = "MySHA512", size = 456, filename = "sha512Filename")),

        )

        val fileString = releaseInfo.toFileString().trim()

        val expectedString = """
            Origin: MyOrigin
            Label: MyLabel
            Suite: MySuite
            Components: MyComponent
            Codename: MyCodename
            Date: MyDate
            Architectures: MyArchitectures
            Description: MyDescription
            Version: MyVersion
            ValidUntil: MyValidUntil
            NotAutomatic: MyNotAutomatic
            ButAutomaticUpgrades: MyButAutomaticUpgrades
            Acquire-By-Hash: MyAcquireByHash
            Changelogs: MyChangelogs
            Snapshots: MySnapshots
            MD5Sum:
             MyMD5Sum 123 md5Filename
            SHA1:
             MySHA1 234 sha1Filename
            SHA256:
             MySHA256 345 sha256Filename
            SHA512:
             MySHA512 456 sha512Filename
             
        """.trimIndent().trim()

        assertEquals(expectedString, fileString)
    }
}
