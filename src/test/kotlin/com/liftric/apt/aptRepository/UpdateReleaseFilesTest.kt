package com.liftric.apt.aptRepository

import com.liftric.apt.model.*
import com.liftric.apt.service.AwsS3Client
import com.liftric.apt.service.updateReleaseFile
import io.mockk.*
import org.gradle.api.logging.Logger
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import software.amazon.awssdk.services.s3.model.PutObjectResponse
import java.io.File

class UpdateReleaseFilesTest {
    private val mockLogger = mockk<Logger>()
    private val mockS3Client = mockk<AwsS3Client>()
    private val bucket = "mockBucket"
    private val bucketPath = ""
    private val releaseInfo = ReleaseInfo(
        origin = "mockOrigin",
        label = "mockLabel",
        suite = "mockSuite",
        components = "main",
        architectures = "all amd64",
        codename = "mockCodename",
        date = null,
        description = "mockDescription",
        version = "mockVersion",
        validUntil = null,
        notAutomatic = null,
        butAutomaticUpgrades = null,
        changelogs = null,
        snapshots = null,
        md5Sum = listOf(),
        sha1 = listOf(),
        sha256 = listOf(),
        sha512 = listOf()
    )

    private val mockFile = File.createTempFile("mockFile", null).apply {
        deleteOnExit()
        writeText(
            """
                Package: foobar
                Version: 1.0.0-1
                Architecture: all
                Maintainer: nvima <mirwald@liftric.com>
                Installed-Size: 0
                Section: java
                Priority: optional
                Description: foobar
                Filename: pool/main/f/foobar/foobar_1.0.0-1_all.deb
                Size: 1018
                SHA1: abd00a88a4ff3eb30dfe4412779ca97c5aabf529
                SHA256: b9381b36cf73b10ba01a6cdfa50be13e807687cdf434925e18278d109920b1b2
                MD5sum: 53829e7ab536c57c2fddc96d0e2690b8
                """.trimIndent().trim()
        )
    }

    @Test
    fun `test updateReleaseFiles method`() {
        every { mockS3Client.getObject(any(), any()) } returns mockFile
        every { mockS3Client.listAllObjects(any(), any()) } returns listOf("filePath")
        every { mockS3Client.uploadObject(bucket, any(), any()) } returns mockk<PutObjectResponse>()
        every { mockLogger.info(any()) } just Runs

        assertDoesNotThrow {
            updateReleaseFile(
                mockLogger,
                mockS3Client,
                bucket,
                bucketPath,
                releaseInfo,
                null,
                null
            )
        }

        verify { mockLogger.info("Release file uploaded to s3://$bucket/${bucketPath}dists/${releaseInfo.suite}/Release") }
    }
}
