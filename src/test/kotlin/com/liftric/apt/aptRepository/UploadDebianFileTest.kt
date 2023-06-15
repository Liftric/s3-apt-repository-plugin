package com.liftric.apt.aptRepository

import com.liftric.apt.service.AwsS3Client
import com.liftric.apt.service.getFullBucketKey
import com.liftric.apt.service.uploadDebianFile
import io.mockk.*
import org.gradle.api.logging.Logger
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import software.amazon.awssdk.services.s3.model.PutObjectResponse
import java.io.File

class UploadDebianFileTest {
    private val mockLogger = mockk<Logger>()
    private val mockS3Client = mockk<AwsS3Client>()
    private val mockFile = mockk<File>()
    private val bucket = "mockBucket"
    private val bucketPath = ""
    private val bucketKey = "mockBucketKey"
    private val fullBucketKey = getFullBucketKey(bucketPath, bucketKey)

    @Test
    fun `test uploadDebianFile method with override false`() {
        val override = false

        every { mockS3Client.doesObjectExist(bucket, bucketKey) } returns true

        val exception = assertThrows<RuntimeException> {
            uploadDebianFile(mockLogger, mockS3Client, bucket, bucketPath, bucketKey, mockFile, override)
        }
        assertEquals("Version already exist in Repo", exception.message)
    }

    @Test
    fun `test uploadDebianFile method with override true`() {
        val override = true

        every { mockLogger.info(any<String>()) } just Runs
        every { mockS3Client.doesObjectExist(bucket, bucketKey) } returns true
        every { mockS3Client.uploadObject(bucket, fullBucketKey, mockFile) } returns mockk<PutObjectResponse>()

        assertDoesNotThrow {
            uploadDebianFile(mockLogger, mockS3Client, bucket, bucketPath, bucketKey, mockFile, override)
        }

        verify { mockLogger.info("File uploaded to s3://$bucket/${fullBucketKey}") }
    }


    @Test
    fun `test uploadDebianFile method with override false and no version of package found`() {
        val override = false

        every { mockLogger.info(any<String>()) } just Runs
        every { mockS3Client.doesObjectExist(bucket, bucketKey) } returns false
        every { mockS3Client.uploadObject(bucket, fullBucketKey, mockFile) } returns mockk<PutObjectResponse>()

        assertDoesNotThrow {
            uploadDebianFile(mockLogger, mockS3Client, bucket, bucketPath, bucketKey, mockFile, override)
        }

        verify { mockLogger.info("File uploaded to s3://$bucket/${fullBucketKey}") }
    }
}
