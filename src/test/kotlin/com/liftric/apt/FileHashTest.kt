package com.liftric.apt

import com.liftric.apt.utils.FileHashUtil.compressWithGzip
import com.liftric.apt.utils.FileHashUtil.md5Hash
import com.liftric.apt.utils.FileHashUtil.sha1Hash
import com.liftric.apt.utils.FileHashUtil.sha256Hash
import com.liftric.apt.utils.FileHashUtil.sha512Hash
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.io.File
import java.io.FileInputStream
import java.util.zip.GZIPInputStream

class FileHashUtilTest {
    @Test
    fun `md5 hash test`() {
        val testFile = File("src/test/resources/test_hash.txt")
        val expectedMd5Hash = "3858f62230ac3c915f300c664312c63f"
        val actualMd5Hash = testFile.md5Hash()

        assertEquals(expectedMd5Hash, actualMd5Hash)
    }

    @Test
    fun `sha1 hash test`() {
        val testFile = File("src/test/resources/test_hash.txt")
        val expectedSha1Hash = "8843d7f92416211de9ebb963ff4ce28125932878"
        val actualSha1Hash = testFile.sha1Hash()

        assertEquals(expectedSha1Hash, actualSha1Hash)
    }

    @Test
    fun `sha256 hash test`() {
        val testFile = File("src/test/resources/test_hash.txt")
        val expectedSha256Hash = "c3ab8ff13720e8ad9047dd39466b3c8974e592c2fa383d4a3960714caef0c4f2"
        val actualSha256Hash = testFile.sha256Hash()

        assertEquals(expectedSha256Hash, actualSha256Hash)
    }

    @Test
    fun `sha512 hash test`() {
        val testFile = File("src/test/resources/test_hash.txt")
        val expectedSha512Hash =
            "0a50261ebd1a390fed2bf326f2673c145582a6342d523204973d0219337f81616a8069b012587cf5635f6925f1b56c360230c19b273500ee013e030601bf2425"
        val actualSha512Hash = testFile.sha512Hash()

        assertEquals(expectedSha512Hash, actualSha512Hash)
    }

    @Test
    fun `gzip compression test`() {
        val testFile = File("src/test/resources/test_hash.txt")
        val compressedFile = testFile.compressWithGzip()

        assertTrue(compressedFile.exists(), "Compressed file does not exist")
        assertTrue(compressedFile.extension == "gz", "Compressed file does not have '.gz' extension")

        val decompressedFileContent =
            GZIPInputStream(FileInputStream(compressedFile)).bufferedReader().use { it.readText() }
        val originalFileContent = testFile.readText()

        assertEquals(
            originalFileContent,
            decompressedFileContent,
            "Decompressed file content does not match the original file content"
        )
        compressedFile.delete()
    }
}
