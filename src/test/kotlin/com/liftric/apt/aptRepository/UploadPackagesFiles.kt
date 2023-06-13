package com.liftric.apt.aptRepository

import org.junit.jupiter.api.Test
import org.mockito.Mockito.*
import java.io.File
import com.liftric.apt.service.AwsS3Client
import com.liftric.apt.service.uploadPackagesFiles
import org.gradle.api.logging.Logger

class UploadPackagesFilesTest {
    @Test
    fun `test uploadPackagesFiles method`() {
        val mockLogger = mock(Logger::class.java)
        val mockS3Client = mock(AwsS3Client::class.java)
        val bucket = "mockBucket"

        val mockFile1 = mock(File::class.java)
        val mockFile2 = mock(File::class.java)

        val packageFiles = mapOf(
            "file1" to mockFile1,
            "file2" to mockFile2
        )

        uploadPackagesFiles(mockLogger, packageFiles, mockS3Client, bucket)

        packageFiles.forEach { (key, file) ->
            verify(mockLogger).info("Uploading $key")
            verify(mockS3Client).uploadObject(bucket, key, file)
        }
    }
}
