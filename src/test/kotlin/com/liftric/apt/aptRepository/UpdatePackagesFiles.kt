package com.liftric.apt.aptRepository

import com.liftric.apt.aptRepository.util.FileCompressor
import org.junit.jupiter.api.Test
import org.mockito.Mockito.*
import com.liftric.apt.service.AwsS3Client
import com.liftric.apt.service.PackagesFactory
import com.liftric.apt.service.getUpdatedPackagesFiles
import com.liftric.apt.utils.FileHashUtil.compressWithGzip
import org.gradle.api.logging.Logger
import java.io.File

class UpdatePackagesFilesTest {
    @Test
    fun `test getUpdatedPackagesFiles method`() {
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
            writeText("Package: foobar\nVersion: 1.0.0-1\nFilename: pool/main/f/foobar/foobar_1.0.0-1_all.deb\n")
            deleteOnExit()
        }
        val mockCompressedFile = mockFile.compressWithGzip()

        `when`(s3Client.getObject(anyString(), anyString())).thenReturn(mockFile)
        `when`(fileCompressor.compressWithGzip(mockFile)).thenReturn(mockCompressedFile)

        val result = getUpdatedPackagesFiles(logger, suite, component, s3Client, bucket, bucketPath, debianPackages)
        verify(s3Client, times(archs.size)).getObject(anyString(), anyString())

        for (arch in archs) {
            assert(result.keys.contains("dists/$suite/$component/binary-$arch/Packages"))
            assert(result.keys.contains("dists/$suite/$component/binary-$arch/Packages.gz"))
        }

        for (file in result.values) {
            assert(file.isFile)
        }

        for (arch in archs) {
            verify(logger).info("Parsing Packages file: s3://$bucket/dists/$suite/$component/binary-$arch/Packages")
        }
    }

    @Test
    fun `test getUpdatedPackagesFiles method with clean s3 bucket`() {
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
            writeText("Package: foobar\nVersion: 1.0.0-1\nFilename: pool/main/f/foobar/foobar_1.0.0-1_all.deb\n")
            deleteOnExit()
        }

        `when`(s3Client.getObject(anyString(), anyString())).thenThrow(RuntimeException("No such key"))
        `when`(fileCompressor.compressWithGzip(mockFile)).thenReturn(mockFile)

        val result = getUpdatedPackagesFiles(logger, suite, component, s3Client, bucket, bucketPath, debianPackages)
        verify(s3Client, times(archs.size)).getObject(anyString(), anyString())

        for (arch in archs) {
            assert(result.keys.contains("dists/$suite/$component/binary-$arch/Packages"))
            assert(result.keys.contains("dists/$suite/$component/binary-$arch/Packages.gz"))
        }

        for (file in result.values) {
            assert(file.isFile)
        }

        for (arch in archs) {
            verify(logger).info("Packages file for '$arch' not found, creating new Packages file")
        }
    }

}
