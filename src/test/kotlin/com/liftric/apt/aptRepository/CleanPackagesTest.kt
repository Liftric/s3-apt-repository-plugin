package com.liftric.apt.aptRepository

import org.junit.jupiter.api.Test
import com.liftric.apt.service.AwsS3Client
import com.liftric.apt.service.cleanPackages
import io.mockk.*
import org.gradle.api.logging.Logger
import software.amazon.awssdk.services.s3.model.DeleteObjectsResponse
import software.amazon.awssdk.services.s3.model.DeletedObject

class CleanPackagesTest {
   private val mockLogger = mockk<Logger>()
   private val mockS3Client = mockk<AwsS3Client>()
   private val bucket = "mockBucket"
   private val bucketPath = ""
   private val component = "mockComponent"

    @Test
    fun `test cleanPackages method`() {
        val usedPackages = setOf("package1", "package2")
        val allFiles = listOf("package1", "package2", "package3")
        val filesToRemove = allFiles.filter { !usedPackages.contains(it) }

        every { mockS3Client.listAllObjects(bucket, "pool/$component/") } returns allFiles

        val mockedResponse = mockk<DeleteObjectsResponse>()
        every { mockedResponse.deleted() } returns filesToRemove.map { mockk<DeletedObject>() }
        every { mockS3Client.deleteObjects(bucket, filesToRemove) } returns mockedResponse
        every { mockLogger.info(any()) } just runs

        cleanPackages(mockLogger, mockS3Client, bucket, bucketPath, component, usedPackages)

        verify { mockS3Client.listAllObjects(bucket, "pool/$component/") }
        verify { mockS3Client.deleteObjects(bucket, filesToRemove) }
        verify { mockLogger.info("Deleted ${filesToRemove.size} objects") }
    }

    @Test
    fun `test cleanPackages method with no files to remove`() {
        val usedPackages = setOf("package1", "package2", "package3")
        val allFiles = listOf("package1", "package2", "package3")

        every { mockS3Client.listAllObjects(bucket, "pool/$component/") } returns allFiles
        every { mockLogger.info(any()) } just runs

        cleanPackages(mockLogger, mockS3Client, bucket, bucketPath, component, usedPackages)

        verify { mockS3Client.listAllObjects(bucket, "pool/$component/") }
        verify(exactly = 0) { mockS3Client.deleteObjects(any(), any()) }
        verify { mockLogger.info("No files to remove") }
    }
}
