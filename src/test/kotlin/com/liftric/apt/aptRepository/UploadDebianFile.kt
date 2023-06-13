package com.liftric.apt.aptRepository

import com.liftric.apt.service.AwsS3Client
import com.liftric.apt.service.uploadDebianFile
import org.gradle.api.logging.Logger
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito.*
import java.io.File

class UploadDebianFileTest {
    @Test
    fun `test uploadDebianFile method with override false`() {
        val mockLogger = mock(Logger::class.java)
        val mockS3Client = mock(AwsS3Client::class.java)
        val mockFile = mock(File::class.java)

        val bucket = "mockBucket"
        val bucketPath = "mockBucketPath"
        val bucketKey = "mockBucketKey"
        val override = false

        `when`(mockS3Client.doesObjectExist(bucket, bucketKey)).thenReturn(true)

        val exception = assertThrows<RuntimeException> {
            uploadDebianFile(mockLogger, mockS3Client, bucket, bucketPath, bucketKey, mockFile, override)
        }
        assertEquals("Version already exist in Repo", exception.message)
    }

    @Test
    fun `test uploadDebianFile method with override true`() {
        val mockLogger = mock(Logger::class.java)
        val mockS3Client = mock(AwsS3Client::class.java)
        val mockFile = mock(File::class.java)

        val bucket = "mockBucket"
        val bucketPath = ""
        val bucketKey = "mockBucketKey"
        val override = true

        `when`(mockS3Client.doesObjectExist(bucket, bucketKey)).thenReturn(true)

        assertDoesNotThrow {
            uploadDebianFile(mockLogger, mockS3Client, bucket, bucketPath, bucketKey, mockFile, override)
        }
    }

    @Test
    fun `test uploadDebianFile method with override false and no version of package found`() {
        val mockLogger = mock(Logger::class.java)
        val mockS3Client = mock(AwsS3Client::class.java)
        val mockFile = mock(File::class.java)

        val bucket = "mockBucket"
        val bucketPath = ""
        val bucketKey = "mockBucketKey"
        val override = false

        `when`(mockS3Client.doesObjectExist(bucket, bucketKey)).thenReturn(false)

        assertDoesNotThrow {
            uploadDebianFile(mockLogger, mockS3Client, bucket, bucketPath, bucketKey, mockFile, override)
        }
    }
}
