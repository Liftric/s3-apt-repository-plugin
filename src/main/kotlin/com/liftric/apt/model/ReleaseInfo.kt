package com.liftric.apt.model

/**
 * The ReleaseInfo data class represents the metadata of a repository in an apt system.
 * This metadata typically resides in "Release" files within the repository and provides essential
 * details about the repository, such as its origin, label, suite, components, architectures, and
 * various other properties.
 *
 * In addition to the repository's attributes, this class includes methods for constructing a string representation
 * of the repository's metadata, suitable for inclusion in a "Release" file.
 *
 * The `toFileString` method generates a string representation of a ReleaseInfo object that follows
 * the conventional formatting in "Release" files. This makes it easy to write the repository's metadata back
 * to the apt system.
 *
 * The `parseReleaseFile` method reads a "Release" file and populates a ReleaseInfo object with its contents. It
 * creates and returns a new ReleaseInfo instance, representing the metadata of the repository described by the "Release" file.
 *
 * The MD5Sum, SHA1, SHA256, and SHA512 data classes represent different types of checksums for the package indexes in the repository.
 * These checksums are typically included in "Release" files to ensure the integrity and authenticity of the package indexes.
 */

data class ReleaseInfo(
    val origin: String,
    val label: String,
    val suite: String,
    val components: String,
    val architectures: String?,
    val codename: String?,
    val date: String?,
    val description: String?,
    val version: String?,
    val validUntil: String?,
    val notAutomatic: String?,
    val butAutomaticUpgrades: String?,
    val acquireByHash: String?,
    val changelogs: String?,
    val snapshots: String?,
    val md5Sum: List<MD5Sum>,
    val sha1: List<SHA1>,
    val sha256: List<SHA256>,
    val sha512: List<SHA512>,
)

data class MD5Sum(
    val md5: String,
    val size: Long,
    val filename: String,
)

data class SHA1(
    val sha1: String,
    val size: Long,
    val filename: String,
)

data class SHA256(
    val sha256: String,
    val size: Long,
    val filename: String,
)

data class SHA512(
    val sha512: String,
    val size: Long,
    val filename: String,
)

data class FileData(
    val md5Sum: MD5Sum,
    val sha1Sum: SHA1,
    val sha256Sum: SHA256,
    val sha512Sum: SHA512,
)

fun ReleaseInfo.toFileString(): String {
    return buildString {
        appendLine("Origin: $origin")
        appendLine("Label: $label")
        appendLine("Suite: $suite")
        appendLine("Components: $components")
        codename?.let { appendLine("Codename: $it") }
        date?.let { appendLine("Date: $it") }
        architectures?.let { appendLine("Architectures: $it") }
        description?.let { appendLine("Description: $it") }
        version?.let { appendLine("Version: $it") }
        validUntil?.let { appendLine("ValidUntil: $it") }
        notAutomatic?.let { appendLine("NotAutomatic: $it") }
        butAutomaticUpgrades?.let { appendLine("ButAutomaticUpgrades: $it") }
        acquireByHash?.let { appendLine("Acquire-By-Hash: $it") }
        changelogs?.let { appendLine("Changelogs: $it") }
        snapshots?.let { appendLine("Snapshots: $it") }
        md5Sum.let { list ->
            appendLine("MD5Sum:")
            list.forEach { md5Sum ->
                appendLine(" ${md5Sum.md5} ${md5Sum.size} ${md5Sum.filename}")
            }
        }
        sha1.let { list ->
            appendLine("SHA1:")
            list.forEach { sha1 ->
                appendLine(" ${sha1.sha1} ${sha1.size} ${sha1.filename}")
            }
        }
        sha256.let { list ->
            appendLine("SHA256:")
            list.forEach { sha256 ->
                appendLine(" ${sha256.sha256} ${sha256.size} ${sha256.filename}")
            }
        }
        sha512.let { list ->
            appendLine("SHA512:")
            list.forEach { sha512 ->
                appendLine(" ${sha512.sha512} ${sha512.size} ${sha512.filename}")
            }
        }
    }
}
