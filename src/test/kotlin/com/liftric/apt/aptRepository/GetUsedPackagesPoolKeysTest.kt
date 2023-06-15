package com.liftric.apt.aptRepository

import com.liftric.apt.service.AwsS3Client
import com.liftric.apt.service.getUsedPackagesPoolKeys
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.io.File

class GetUsedPackagesPoolKeysTest {
    private val mockS3Client = mockk<AwsS3Client>()
    private val bucket = "mockBucket"
    private val bucketPath = ""
    private val suite = "stable"
    private val component = "main"

    @Test
    fun `test getUsedPackagesPoolKeys method`() {
        val mockFilePaths =
            listOf("dists/$suite/$component/binary-all/Packages.gz", "dists/$suite/$component/binary-all/Packages")

        every { mockS3Client.listAllObjects(any(), any()) } returns mockFilePaths

        val filteredFilePaths = mockFilePaths.filter { it.endsWith("Packages") }
        val tempFile = File.createTempFile("Packages", null).apply {
            writeText("Package: foobar\nVersion: 1.0.0-1\nArchitecture: all\nSize: 123\nFilename: pool/main/f/foobar/foobar_1.0.0-1_all.deb\n")
            deleteOnExit()
        }

        every { mockS3Client.getObject(any(), any()) } returns tempFile

        val usedPackages = getUsedPackagesPoolKeys(mockS3Client, bucket, bucketPath, suite, component)
        assertEquals(setOf("pool/main/f/foobar/foobar_1.0.0-1_all.deb"), usedPackages)

        verify { mockS3Client.getObject(bucket, filteredFilePaths[0]) }
    }
}
