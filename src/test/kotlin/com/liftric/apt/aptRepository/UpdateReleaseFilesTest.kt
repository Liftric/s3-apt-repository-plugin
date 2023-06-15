package com.liftric.apt.aptRepository

import com.liftric.apt.service.AwsS3Client
import com.liftric.apt.service.updateReleaseFiles
import io.mockk.*
import org.gradle.api.logging.Logger
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import software.amazon.awssdk.services.s3.model.PutObjectResponse
import java.io.File

class UpdateReleaseFilesTest {
    private val mockLogger = mockk<Logger>()
    private val mockS3Client = mockk<AwsS3Client>()
    private val bucket = "mockBucket"
    private val bucketPath = ""
    private val suite = "mockSuite"
    private val component = "mockComponent"
    private val origin = "mockOrigin"
    private val label = "mockLabel"

    @Test
    fun `test updateReleaseFiles method`() {
        val mockFile = File.createTempFile("mockFile", null).apply {
            deleteOnExit()
            writeText(
                """
                Origin: Liftric
                Label: Liftric
                Suite: stable
                Components: main
                Date: Fri, 09 Jun 2023 09:34:26 UTC
                Architectures: all
                MD5Sum:
                 3a422598c40f3e4fa01260bddb315d94 388 main/binary-all/Packages
                 4883de5b75465c6b9748ab23708a5797 303 main/binary-all/Packages.gz
                SHA1:
                 c57e7599a71c534a379aa9a078de33814496f002 388 main/binary-all/Packages
                 ab03a75c6473e744331e4b74853518c2a662bb71 303 main/binary-all/Packages.gz
                SHA256:
                 f8bdb77df801b557403a71f9ee2cb732521a8ceeea420e28eb3d04b781576c5d 388 main/binary-all/Packages
                 207c8c3f8cb79d6a6e0e015533474c8cad782ecd9587a54350282f09cf6955a1 303 main/binary-all/Packages.gz
                SHA512:
                 c5fbe3fedd1775c6e5580b6e0c92d308c310c8b953c1bfcdde8b41d9e5d0849c15dac5ff3a8c28406b6be48a5ebbf9e517b051c912e7b3aa6189298dcdce3811 388 main/binary-all/Packages
                 ef9f90a0edf4f5e576734896f09970e3e6eb4cc0f645955b461228908abf46de97cdec7f6db714d251f4bbbef5d305da9098d7e27301211bb469ec80b24de433 303 main/binary-all/Packages.gz
                """.trimIndent().trim()
            )
        }

        every { mockS3Client.getObject(any(), any()) } returns mockFile
        every { mockS3Client.listAllObjects(any(), any()) } returns listOf("filePath")
        every { mockS3Client.uploadObject(bucket, any(), any()) } returns mockk<PutObjectResponse>()
        every { mockLogger.info(any()) } just Runs

        assertDoesNotThrow {
            updateReleaseFiles(
                mockLogger,
                mockS3Client,
                bucket,
                bucketPath,
                suite,
                component,
                origin,
                label,
                null,
                null
            )
        }

        verify { mockLogger.info("Parsing Release file: s3://$bucket/${bucketPath}dists/$suite/Release") }
        verify { mockLogger.info("Release file uploaded to s3://$bucket/${bucketPath}dists/$suite/Release") }
    }

    @Test
    fun `test updateReleaseFiles method with no Release File found`() {
        every { mockS3Client.listAllObjects(any(), any()) } returns listOf("filePath")
        every { mockS3Client.getObject(any(), any()) } throws RuntimeException("File not found")
        every { mockLogger.info(any()) } just Runs

        val exception = assertThrows<RuntimeException> {
            updateReleaseFiles(
                mockLogger,
                mockS3Client,
                bucket,
                bucketPath,
                suite,
                component,
                origin,
                label,
                null,
                null
            )
        }
        Assertions.assertEquals("File not found", exception.message)

        verify { mockLogger.info("Release File not found, creating new Release file") }
    }
}
