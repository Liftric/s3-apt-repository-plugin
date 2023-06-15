package com.liftric.apt.aptRepository

import com.liftric.apt.aptRepository.util.FileCompressor
import com.liftric.apt.service.AwsS3Client
import com.liftric.apt.service.PackagesFactory
import com.liftric.apt.service.getUpdatedPackagesFiles
import com.liftric.apt.utils.FileHashUtil.compressWithGzip
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import org.gradle.api.logging.Logger
import org.junit.jupiter.api.Test
import java.io.File

class UpdatePackagesFilesTest {
    private val logger = mockk<Logger>()
    private val s3Client = mockk<AwsS3Client>()
    private val fileCompressor = mockk<FileCompressor>()
    private val testDebFile = File("src/test/resources/foobar_1.0.0-1_all.deb")
    private val archs = setOf("all", "amd64")
    private val suite = "mockSuite"
    private val component = "mockComponent"
    private val bucket = "mockBucket"
    private val bucketPath = ""
    private val debianPackages =
        PackagesFactory.parseDebianFile(testDebFile, archs, "foobar_1.0.0-1_all.deb", null, null)
    private val mockFile = File.createTempFile("Packages", null).apply {
        writeText("Package: foobar\nVersion: 1.0.0-1\nFilename: pool/main/f/foobar/foobar_1.0.0-1_all.deb\n")
        deleteOnExit()
    }

    @Test
    fun `test getUpdatedPackagesFiles method`() {
        val mockCompressedFile = mockFile.compressWithGzip()

        every { s3Client.getObject(any(), any()) } returns mockFile
        every { fileCompressor.compressWithGzip(mockFile) } returns mockCompressedFile
        every { logger.info(any()) } just runs

        val result = getUpdatedPackagesFiles(logger, suite, component, s3Client, bucket, bucketPath, debianPackages)

        verify(exactly = archs.size) { s3Client.getObject(any(), any()) }

        for (arch in archs) {
            assert(result.keys.contains("dists/$suite/$component/binary-$arch/Packages"))
            assert(result.keys.contains("dists/$suite/$component/binary-$arch/Packages.gz"))
        }

        for (file in result.values) {
            assert(file.isFile)
        }

        for (arch in archs) {
            verify { logger.info("Parsing Packages file: s3://$bucket/dists/$suite/$component/binary-$arch/Packages") }
        }
    }

    @Test
    fun `test getUpdatedPackagesFiles method with clean s3 bucket`() {
        every { s3Client.getObject(any(), any()) } throws RuntimeException("No such key")
        every { fileCompressor.compressWithGzip(mockFile) } returns mockFile
        every { logger.info(any()) } just runs

        val result = getUpdatedPackagesFiles(logger, suite, component, s3Client, bucket, bucketPath, debianPackages)
        verify(exactly = archs.size) { s3Client.getObject(any(), any()) }

        for (arch in archs) {
            assert(result.keys.contains("dists/$suite/$component/binary-$arch/Packages"))
            assert(result.keys.contains("dists/$suite/$component/binary-$arch/Packages.gz"))
        }

        for (file in result.values) {
            assert(file.isFile)
        }

        for (arch in archs) {
            verify { logger.info("Packages file for '$arch' not found, creating new Packages file") }
        }
    }
}
