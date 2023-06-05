package com.liftric.apt.model

import java.io.File

const val DEFAULT_ORIGIN = "Debian"
const val DEFAULT_LABEL = "Debian"
const val DEFAULT_SUITE = "stable"
const val DEFAULT_COMPONENT = "main"

data class ReleaseInfo(
    var origin: String = DEFAULT_ORIGIN,
    var label: String = DEFAULT_LABEL,
    var suite: String = DEFAULT_SUITE,
    var components: String = DEFAULT_COMPONENT,
    var architectures: String? = null,
    var codename: String? = null,
    var date: String? = null,
    var description: String? = null,
    var version: String? = null,
    var noSupportForArchitectureAll: String? = null,
    var validUntil: String? = null,
    var notAutomatic: String? = null,
    var butAutomaticUpgrades: String? = null,
    var acquireByHash: String? = null,
    var changelogs: String? = null,
    var snapshots: String? = null,
    var md5Sum: MutableList<MD5Sum> = mutableListOf(),
    var sha1: MutableList<SHA1> = mutableListOf(),
    var sha256: MutableList<SHA256> = mutableListOf(),
    var sha512: MutableList<SHA512> = mutableListOf(),
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
        noSupportForArchitectureAll?.let { appendLine("NotAutomatic: $it") }
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

fun parseReleaseFile(file: File): ReleaseInfo {
    val releaseInfo = ReleaseInfo()

    file.forEachLine { line ->
        when {
            line.startsWith("Origin:") -> releaseInfo.origin = line.removePrefix("Origin:").trim()
            line.startsWith("Label:") -> releaseInfo.label = line.removePrefix("Label:").trim()
            line.startsWith("Suite:") -> releaseInfo.suite = line.removePrefix("Suite:").trim()
            line.startsWith("Codename:") -> releaseInfo.codename = line.removePrefix("Codename:").trim()
            line.startsWith("Date:") -> releaseInfo.date = line.removePrefix("Date:").trim()
            line.startsWith("Architectures:") -> releaseInfo.architectures = line.removePrefix("Architectures:").trim()
            line.startsWith("Components:") -> releaseInfo.components = line.removePrefix("Components:").trim()
            line.startsWith("Description:") -> releaseInfo.description = line.removePrefix("Description:").trim()
            line.startsWith("Version:") -> releaseInfo.version = line.removePrefix("Version:").trim()
            line.startsWith("NotAutomatic:") -> releaseInfo.noSupportForArchitectureAll =
                line.removePrefix("NotAutomatic:").trim()

            line.startsWith("ValidUntil:") -> releaseInfo.validUntil = line.removePrefix("ValidUntil:").trim()
            line.startsWith("NotAutomatic:") -> releaseInfo.notAutomatic = line.removePrefix("NotAutomatic:").trim()
            line.startsWith("ButAutomaticUpgrades:") -> releaseInfo.butAutomaticUpgrades =
                line.removePrefix("ButAutomaticUpgrades:").trim()

            line.startsWith("Acquire-By-Hash:") -> releaseInfo.acquireByHash =
                line.removePrefix("Acquire-By-Hash:").trim()

            line.startsWith("Changelogs:") -> releaseInfo.changelogs = line.removePrefix("Changelogs:").trim()
            line.startsWith("Snapshots:") -> releaseInfo.snapshots = line.removePrefix("Snapshots:").trim()
        }
    }

    return releaseInfo
}
