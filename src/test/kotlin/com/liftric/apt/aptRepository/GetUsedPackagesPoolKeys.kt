package com.liftric.apt.aptRepository

import com.liftric.apt.service.AwsS3Client
import com.liftric.apt.service.getUsedPackagesPoolKeys
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import kotlin.io.path.createTempFile

class GetUsedPackagesPoolKeysTest {
    @Test
    fun `test getUsedPackagesPoolKeys method`() {
        val mockS3Client = mock(AwsS3Client::class.java)

        val bucket = "mockBucket"
        val bucketPath = ""
        val suite = "stable"
        val component = "main"

        val mockFilePaths =
            listOf("dists/$suite/$component/binary-all/Packages.gz", "dists/$suite/$component/binary-all/Packages")
        `when`(mockS3Client.listAllObjects(anyString(), anyString())).thenReturn(mockFilePaths)

        val filteredFilePaths = mockFilePaths.filter { it.endsWith("Packages") }

        val tempFile = createTempFile()
        tempFile.toFile()
            .writeText("Package: foobar\nVersion: 1.0.0-1\nArchitecture: all\nSize: 123\nFilename: pool/main/f/foobar/foobar_1.0.0-1_all.deb\n")

        `when`(mockS3Client.getObject(anyString(), anyString())).thenReturn(tempFile.toFile())

        val usedPackages = getUsedPackagesPoolKeys(mockS3Client, bucket, bucketPath, suite, component)
        assertEquals(setOf("pool/main/f/foobar/foobar_1.0.0-1_all.deb"), usedPackages)

        verify(mockS3Client).getObject(bucket, filteredFilePaths[0])
        tempFile.toFile().delete()
    }
}
