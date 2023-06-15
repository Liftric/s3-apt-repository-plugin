package com.liftric.apt.aptRepository

import com.liftric.apt.service.AwsS3Client
import com.liftric.apt.service.uploadPackagesFiles
import io.mockk.*
import org.gradle.api.logging.Logger
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import software.amazon.awssdk.services.s3.model.PutObjectResponse
import java.io.File

class UploadPackagesFilesTest {
    @Test
    fun `test uploadPackagesFiles method`() {
        val mockLogger = mockk<Logger>()
        val mockS3Client = mockk<AwsS3Client>()
        val bucket = "mockBucket"

        val mockFile1 = mockk<File>()
        val mockFile2 = mockk<File>()

        val packageFiles = mapOf(
            "file1" to mockFile1,
            "file2" to mockFile2
        )

        every { mockLogger.info(any<String>()) } just Runs
        every { mockS3Client.uploadObject(bucket, any(), any()) } returns mockk<PutObjectResponse>()

        assertDoesNotThrow {
            uploadPackagesFiles(mockLogger, packageFiles, mockS3Client, bucket)
        }

        packageFiles.forEach { (key, file) ->
            verify { mockLogger.info("Uploading $key") }
            verify { mockS3Client.uploadObject(bucket, key, file) }
        }
    }
}
