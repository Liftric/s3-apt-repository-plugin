package com.liftric.apt.aptRepository

import com.liftric.apt.aptRepository.util.FileCompressor
import org.mockito.Mockito.*
import com.liftric.apt.model.PackagesInfo
import org.junit.jupiter.api.Test
import com.liftric.apt.utils.FileHashUtil.compressWithGzip
import com.liftric.apt.service.AwsS3Client
import com.liftric.apt.service.getCleanPackagesFiles
import org.gradle.api.logging.Logger
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.assertThrows
import java.io.File

class GetCleanPackagesFilesTest {
    @Test
    fun `test getCleanPackagesFiles method`() {
        val logger = mock(Logger::class.java)
        val s3Client = mock(AwsS3Client::class.java)
        val fileCompressor = mock(FileCompressor::class.java)
        val packagesInfo = PackagesInfo(
            packageInfo = "foobar",
            version = "1.0.0-1",
            architecture = "all",
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

        val out = getCleanPackagesFiles(logger, archList, suite, component, s3Client, bucket, bucketPath, packagesInfo)
        verify(s3Client, times(archList.size)).getObject(anyString(), anyString())

        for (arch in archList) {
            assert(out.keys.contains("dists/$suite/$component/binary-$arch/Packages"))
            assert(out.keys.contains("dists/$suite/$component/binary-$arch/Packages.gz"))
        }

        for (file in out.values) {
            assert(file.isFile)
            if (!file.name.endsWith(".gz")) {
                assert(!file.readText().contains("foobar"))
            }
        }

        for (arch in archList) {
            verify(logger).info("Parsing Packages file: s3://$bucket/dists/$suite/$component/binary-$arch/Packages")
        }
    }

    @Test
    fun `test getCleanPackagesFiles method with Exception`() {
        val logger = mock(Logger::class.java)
        val s3Client = mock(AwsS3Client::class.java)
        val packagesInfo = PackagesInfo(
            packageInfo = "foobar",
            version = "1.0.0-1",
            architecture = "all",
        )

        val archList = setOf("all")
        val suite = "mockSuite"
        val component = "mockComponent"
        val bucket = "mockBucket"
        val bucketPath = ""
        val exceptionMsg = "No such key"

        `when`(s3Client.getObject(anyString(), anyString())).thenThrow(RuntimeException(exceptionMsg))

        val exception = assertThrows<Exception> {
            getCleanPackagesFiles(logger, archList, suite, component, s3Client, bucket, bucketPath, packagesInfo)
        }
        Assertions.assertEquals(exceptionMsg, exception.message)

        for (arch in archList) {
            verify(logger).error("Packages file for Architecture '$arch' not found! Can't remove '${packagesInfo.packageInfo}'")
        }
    }
}
