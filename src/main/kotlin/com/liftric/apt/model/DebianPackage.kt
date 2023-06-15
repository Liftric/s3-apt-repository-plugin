package com.liftric.apt.model

data class DebianPackage(
    val packageName: String,
    val version: String,
    val architecture: String,
    val fileName: String,
    val size: String,
    val maintainer: String?,
    val installedSize: String?,
    val depends: String?,
    val conflicts: String?,
    val replaces: String?,
    val provides: String?,
    val preDepends: String?,
    val recommends: String?,
    val suggests: String?,
    val enhances: String?,
    val builtUsing: String?,
    val section: String?,
    val priority: String?,
    val homepage: String?,
    val description: String?,
    val sha1: String?,
    val sha256: String?,
    val md5sum: String?,
)

fun DebianPackage.toFileString(): String {
    return buildString {
        appendLine("Package: $packageName")
        appendLine("Version: $version")
        appendLine("Architecture: $architecture")
        appendLine("Filename: $fileName")
        appendLine("Size: $size")
        maintainer?.let { appendLine("Maintainer: $it") }
        installedSize?.let { appendLine("Installed-Size: $it") }
        depends?.let { appendLine("Depends: $it") }
        conflicts?.let { appendLine("Conflicts: $it") }
        replaces?.let { appendLine("Replaces: $it") }
        provides?.let { appendLine("Provides: $it") }
        preDepends?.let { appendLine("Pre-Depends: $it") }
        recommends?.let { appendLine("Recommends: $it") }
        suggests?.let { appendLine("Suggests: $it") }
        enhances?.let { appendLine("Enhances: $it") }
        builtUsing?.let { appendLine("Built-Using: $it") }
        section?.let { appendLine("Section: $it") }
        priority?.let { appendLine("Priority: $it") }
        homepage?.let { appendLine("Homepage: $it") }
        description?.let { appendLine("Description: $it") }
        md5sum?.let { appendLine("MD5sum: $it") }
        sha1?.let { appendLine("SHA1: $it") }
        sha256?.let { appendLine("SHA256: $it") }
        appendLine()
    }
}

fun List<DebianPackage>.toFileString(): String {
    return buildString {
        for (packageItem in this@toFileString) {
            append(packageItem.toFileString())
        }
    }
}

fun List<DebianPackage>.combineDebianPackages(
    debianPackage: DebianPackage,
): List<DebianPackage> {
    return this.filter { it.packageName != debianPackage.packageName || it.version != debianPackage.version } + debianPackage
}

fun List<DebianPackage>.removeDebianPackage(
    debianPackage: DebianPackage,
): List<DebianPackage> {
    return this.filter { it.packageName != debianPackage.packageName || it.version != debianPackage.version }
}
