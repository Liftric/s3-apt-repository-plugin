package com.liftric.apt.aptRepository

import com.liftric.apt.aptRepository.util.FileCompressor
import com.liftric.apt.model.PackagesInfo
import org.junit.jupiter.api.Test
import org.mockito.Mockito.*
import com.liftric.apt.service.AwsS3Client
import com.liftric.apt.service.updatePackagesFiles
import com.liftric.apt.utils.FileHashUtil.compressWithGzip
import org.gradle.api.logging.Logger
import java.io.File

class UpdatePackagesFilesTest {
    @Test
    fun `test updatePackagesFiles method`() {
        val logger = mock(Logger::class.java)
        val s3Client = mock(AwsS3Client::class.java)
        val fileCompressor = mock(FileCompressor::class.java)
        val packagesInfo = PackagesInfo(
        )

        val archList = setOf("all")
        val suite = "mockSuite"
        val component = "mockComponent"
        val bucket = "mockBucket"
        val bucketPath = ""

        val mockFile = File.createTempFile("Packages", null).apply {
            writeText("Package: foobar\nVersion: 1.0.0-1\nFilename: pool/main/f/foobar/foobar_1.0.0-1_all.deb\n")
            deleteOnExit()
        }
        val mockCompressedFile = mockFile.compressWithGzip()

        `when`(s3Client.getObject(anyString(), anyString())).thenReturn(mockFile)
        `when`(fileCompressor.compressWithGzip(mockFile)).thenReturn(mockCompressedFile)

        val result = updatePackagesFiles(logger, archList, suite, component, s3Client, bucket, bucketPath, packagesInfo)
        verify(s3Client, times(archList.size)).getObject(anyString(), anyString())

        for (arch in archList) {
            assert(result.keys.contains("dists/$suite/$component/binary-$arch/Packages"))
            assert(result.keys.contains("dists/$suite/$component/binary-$arch/Packages.gz"))
        }

        for (file in result.values) {
            assert(file.isFile)
        }

        for (arch in archList) {
            verify(logger).info("Parsing Packages file: s3://$bucket/dists/$suite/$component/binary-$arch/Packages")
        }
    }

    @Test
    fun `test updatePackagesFiles method with clean s3 bucket`() {
        val logger = mock(Logger::class.java)
        val s3Client = mock(AwsS3Client::class.java)
        val fileCompressor = mock(FileCompressor::class.java)
        val packagesInfo = PackagesInfo()

        val archList = setOf("all")
        val suite = "mockSuite"
        val component = "mockComponent"
        val bucket = "mockBucket"
        val bucketPath = ""

        val mockFile = File.createTempFile("Packages", null).apply {
            writeText("Package: foobar\nVersion: 1.0.0-1\nFilename: pool/main/f/foobar/foobar_1.0.0-1_all.deb\n")
            deleteOnExit()
        }

        `when`(s3Client.getObject(anyString(), anyString())).thenThrow(RuntimeException("No such key"))
        `when`(fileCompressor.compressWithGzip(mockFile)).thenReturn(mockFile)

        val result = updatePackagesFiles(logger, archList, suite, component, s3Client, bucket, bucketPath, packagesInfo)
        verify(s3Client, times(archList.size)).getObject(anyString(), anyString())

        for (arch in archList) {
            assert(result.keys.contains("dists/$suite/$component/binary-$arch/Packages"))
            assert(result.keys.contains("dists/$suite/$component/binary-$arch/Packages.gz"))
        }

        for (file in result.values) {
            assert(file.isFile)
        }

        for (arch in archList) {
            verify(logger).info("Packages file for '$arch' not found, creating new Packages file")
        }
    }

}
