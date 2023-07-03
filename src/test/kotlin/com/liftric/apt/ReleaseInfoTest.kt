package com.liftric.apt

import com.liftric.apt.model.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.assertEquals

class ReleaseInfoTest {
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
            changelogs = "MyChangelogs",
            snapshots = "MySnapshots",
            md5Sum = listOf(MD5Sum(md5 = "MyMD5Sum", size = 123, filename = "md5Filename")),
            sha1 = listOf(SHA1(sha1 = "MySHA1", size = 234, filename = "sha1Filename")),
            sha256 = listOf(SHA256(sha256 = "MySHA256", size = 345, filename = "sha256Filename")),
            sha512 = listOf(SHA512(sha512 = "MySHA512", size = 456, filename = "sha512Filename")),
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
