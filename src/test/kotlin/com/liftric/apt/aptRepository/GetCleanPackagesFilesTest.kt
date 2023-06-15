package com.liftric.apt.aptRepository

import com.liftric.apt.aptRepository.util.FileCompressor
import org.junit.jupiter.api.Test
import com.liftric.apt.utils.FileHashUtil.compressWithGzip
import com.liftric.apt.service.AwsS3Client
import com.liftric.apt.service.PackagesFactory
import com.liftric.apt.service.getCleanedPackagesFiles
import io.mockk.*
import org.gradle.api.logging.Logger
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.assertThrows
import java.io.File

class GetCleanPackagesFilesTest {
    private val logger = mockk<Logger>()
    private val s3Client = mockk<AwsS3Client>()
    private val fileCompressor = mockk<FileCompressor>()

    private val testDebFile = File("src/test/resources/foobar_1.0.0-1_all.deb")
    private val suite = "mockSuite"
    private val component = "mockComponent"
    private val bucket = "mockBucket"
    private val bucketPath = ""

    @Test
    fun `test getCleanedPackagesFiles method`() {
        val archs = setOf("all", "amd64")
        val debianPackages = PackagesFactory.parseDebianFile(testDebFile, archs, "foobar_1.0.0-1_all.deb", null, null)

        val mockFile = File.createTempFile("Packages", null).apply {
            writeText("Package: foobar\nVersion: 1.0.0-1\nArchitecture: all\nSize: 123\nFilename: pool/main/f/foobar/foobar_1.0.0-1_all.deb\n")
            deleteOnExit()
        }
        val mockCompressedFile = mockFile.compressWithGzip()

        every { logger.info(any()) } just runs
        every { s3Client.getObject(any(), any()) } returns mockFile
        every { fileCompressor.compressWithGzip(mockFile) } returns mockCompressedFile

        val out = getCleanedPackagesFiles(logger, suite, component, s3Client, bucket, bucketPath, debianPackages)
        verify(exactly = archs.size) { s3Client.getObject(any(), any()) }

        for (arch in archs) {
            assert(out.keys.contains("dists/$suite/$component/binary-$arch/Packages"))
            assert(out.keys.contains("dists/$suite/$component/binary-$arch/Packages.gz"))
        }

        for (file in out.values) {
            assert(file.isFile)
            if (!file.name.endsWith(".gz")) {
                assert(!file.readText().contains("foobar"))
            }
        }

        for (arch in archs) {
            verify { logger.info("Parsing Packages file: s3://$bucket/dists/$suite/$component/binary-$arch/Packages") }
        }
    }

    @Test
    fun `test getCleanedPackagesFiles method with Exception`() {
        val archs = setOf("all")
        val debianPackages = PackagesFactory.parseDebianFile(testDebFile, archs, "foobar_1.0.0-1_all.deb", null, null)
        val exceptionMsg = "No such key"

        every { logger.error(any()) } just runs
        every { s3Client.getObject(any(), any()) } throws RuntimeException(exceptionMsg)

        val exception = assertThrows<Exception> {
            getCleanedPackagesFiles(logger, suite, component, s3Client, bucket, bucketPath, debianPackages)
        }
        assertEquals(exceptionMsg, exception.message)

        for (debianPackage in debianPackages) {
            verify { logger.error("Packages file for Architecture '${debianPackage.architecture}' not found! Can't remove '${debianPackage.packageName}'") }
        }
    }
}
