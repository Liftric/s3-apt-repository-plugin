package com.liftric.apt.aptRepository

import com.liftric.apt.aptRepository.util.FileCompressor
import org.mockito.Mockito.*
import org.junit.jupiter.api.Test
import com.liftric.apt.utils.FileHashUtil.compressWithGzip
import com.liftric.apt.service.AwsS3Client
import com.liftric.apt.service.PackagesFactory
import com.liftric.apt.service.getCleanedPackagesFiles
import org.gradle.api.logging.Logger
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.assertThrows
import java.io.File

class GetCleanPackagesFilesTest {
    @Test
    fun `test getCleanedPackagesFiles method`() {
        val logger = mock(Logger::class.java)
        val s3Client = mock(AwsS3Client::class.java)
        val fileCompressor = mock(FileCompressor::class.java)

        val testDebFile = File("src/test/resources/foobar_1.0.0-1_all.deb")
        val archs = setOf("all", "amd64")
        val suite = "mockSuite"
        val component = "mockComponent"
        val bucket = "mockBucket"
        val bucketPath = ""

        val debianPackages = PackagesFactory.parseDebianFile(testDebFile, archs, "foobar_1.0.0-1_all.deb", null, null)

        val mockFile = File.createTempFile("Packages", null).apply {
            writeText("Package: foobar\nVersion: 1.0.0-1\nArchitecture: all\nSize: 123\nFilename: pool/main/f/foobar/foobar_1.0.0-1_all.deb\n")
            deleteOnExit()
        }
        val mockCompressedFile = mockFile.compressWithGzip()

        `when`(s3Client.getObject(anyString(), anyString())).thenReturn(mockFile)
        `when`(fileCompressor.compressWithGzip(mockFile)).thenReturn(mockCompressedFile)

        val out = getCleanedPackagesFiles(logger, suite, component, s3Client, bucket, bucketPath, debianPackages)
        verify(s3Client, times(archs.size)).getObject(anyString(), anyString())

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
            verify(logger).info("Parsing Packages file: s3://$bucket/dists/$suite/$component/binary-$arch/Packages")
        }
    }

    @Test
    fun `test getCleanedPackagesFiles method with Exception`() {
        val logger = mock(Logger::class.java)
        val s3Client = mock(AwsS3Client::class.java)

        val testDebFile = File("src/test/resources/foobar_1.0.0-1_all.deb")
        val archs = setOf("all")
        val suite = "mockSuite"
        val component = "mockComponent"
        val bucket = "mockBucket"
        val bucketPath = ""

        val debianPackages = PackagesFactory.parseDebianFile(testDebFile, archs, "foobar_1.0.0-1_all.deb", null, null)
        val exceptionMsg = "No such key"

        `when`(s3Client.getObject(anyString(), anyString())).thenThrow(RuntimeException(exceptionMsg))

        val exception = assertThrows<Exception> {
            getCleanedPackagesFiles(logger, suite, component, s3Client, bucket, bucketPath, debianPackages)
        }
        Assertions.assertEquals(exceptionMsg, exception.message)

        for (debianPackage in debianPackages) {
            verify(logger).error("Packages file for Architecture '${debianPackage.architecture}' not found! Can't remove '${debianPackage.packageName}'")
        }
    }
}
