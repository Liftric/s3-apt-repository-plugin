package com.liftric.apt.aptRepository

import org.junit.jupiter.api.Test
import org.mockito.Mockito.*
import com.liftric.apt.service.AwsS3Client
import com.liftric.apt.service.cleanPackages
import org.gradle.api.logging.Logger
import software.amazon.awssdk.services.s3.model.DeleteObjectsResponse
import software.amazon.awssdk.services.s3.model.DeletedObject

class CleanPackagesTest {
    @Test
    fun `test cleanPackages method`() {
        val mockLogger = mock(Logger::class.java)
        val mockS3Client = mock(AwsS3Client::class.java)

        val bucket = "mockBucket"
        val bucketPath = ""
        val component = "mockComponent"
        val usedPackages = setOf("package1", "package2")

        val allFiles = listOf("package1", "package2", "package3")
        val filesToRemove = allFiles.filter { !usedPackages.contains(it) }

        `when`(mockS3Client.listAllObjects(bucket, "pool/$component/")).thenReturn(allFiles)

        val mockedResponse = mock(DeleteObjectsResponse::class.java)
        `when`(mockedResponse.deleted()).thenReturn(filesToRemove.map { mock(DeletedObject::class.java) })
        `when`(mockS3Client.deleteObjects(bucket, filesToRemove)).thenReturn(mockedResponse)

        cleanPackages(mockLogger, mockS3Client, bucket, bucketPath, component, usedPackages)

        verify(mockS3Client).listAllObjects(bucket, "pool/$component/")
        verify(mockS3Client).deleteObjects(bucket, filesToRemove)
        verify(mockLogger).info("Deleted ${filesToRemove.size} objects")
    }
}

