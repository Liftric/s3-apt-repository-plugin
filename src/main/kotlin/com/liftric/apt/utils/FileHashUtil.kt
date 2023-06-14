package com.liftric.apt.utils

import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.math.BigInteger
import java.security.MessageDigest
import java.util.zip.GZIPOutputStream

/**
 * FileHashUtil is a utility object that extends the Java File class with additional functionalities
 * specifically designed for managing and manipulating files within an apt repository.
 *
 * It provides convenient methods to compute various types of hashes (MD5, SHA-1, SHA-256) for a file,
 * which are commonly used in apt repositories for data integrity and verification purposes.
 *
 * Besides hashing, it also provides a method to retrieve the size of a file and a function to compress
 * a file using the GZIP compression algorithm, another operation frequently needed when working with
 * apt repositories.
 *
 * These methods are implemented as extension functions to the File class, making them readily available
 * on any File object within the scope of this object.
 */

object FileHashUtil {
    private fun File.hash(algo: String, padStart: Int, padChar: Char): String {
        val md = MessageDigest.getInstance(algo)
        val inputStream = this.inputStream()
        return inputStream.use {
            val buffer = ByteArray(1024 * 1024 * 8)
            var bytesRead = it.read(buffer)

            while (bytesRead > 0) {
                md.update(buffer, 0, bytesRead)
                bytesRead = it.read(buffer)
            }

            val hashedBytes = md.digest()

            BigInteger(1, hashedBytes).toString(16).padStart(padStart, padChar)
        }
    }

    fun File.md5Hash(): String = hash("MD5", 32, '0')

    fun File.sha1Hash(): String = hash("SHA-1", 40, '0')

    fun File.sha256Hash(): String = hash("SHA-256", 64, '0')

    fun File.sha512Hash(): String = hash("SHA-512", 128, '0')

    fun File.compressWithGzip(): File {
        val gzipFile = File(this.parentFile, this.name + ".gz")
        FileInputStream(this).use { fileInputStream ->
            GZIPOutputStream(FileOutputStream(gzipFile)).use { gzipOutputStream ->
                fileInputStream.copyTo(gzipOutputStream)
            }
        }
        return gzipFile
    }
}