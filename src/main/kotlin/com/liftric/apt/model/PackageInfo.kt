package com.liftric.apt.model

import java.io.File

/**
 * The PackagesInfo data class represents the metadata of a package in an apt repository.
 * This metadata typically resides in "Packages" files within the repository and provides essential
 * details about each package, such as its name, version, architecture, dependencies, conflicts,
 * and various other properties.
 *
 * Each instance of this class corresponds to one package's metadata. The properties of this class
 * mirror the fields found in the repository's "Packages" file entries.
 *
 * In addition to the package's attributes, this class includes methods for constructing a string representation
 * of the package's metadata, suitable for inclusion in a "Packages" file.
 *
 * The `toFileString` method generates a string representation of a PackagesInfo object that follows
 * the conventional formatting in "Packages" files. This makes it easy to write the package's metadata back
 * to a repository.
 */

data class PackagesInfo(
    var packageInfo: String? = null,
    var version: String? = null,
    var architecture: String? = null,
    var maintainer: String? = null,
    var installedSize: String? = null,
    var depends: String? = null,
    var conflicts: String? = null,
    var replaces: String? = null,
    var provides: String? = null,
    var preDepends: String? = null,
    var recommends: String? = null,
    var suggests: String? = null,
    var enhances: String? = null,
    var builtUsing: String? = null,
    var section: String? = null,
    var priority: String? = null,
    var homepage: String? = null,
    var description: String? = null,
    var fileName: String? = null,
    var size: Long? = null,
    var sha1: String? = null,
    var sha256: String? = null,
    var md5sum: String? = null
)

fun PackagesInfo.toFileString(): String {
    return buildString {
        packageInfo?.let { appendLine("Package: $it") }
        version?.let { appendLine("Version: $it") }
        architecture?.let { appendLine("Architecture: $it") }
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
        fileName?.let { appendLine("Filename: $it") }
        size?.let { appendLine("Size: $it") }
        sha1?.let { appendLine("SHA1: $it") }
        sha256?.let { appendLine("SHA256: $it") }
        md5sum?.let { appendLine("MD5sum: $it") }
        appendLine()
    }
}

fun readPackagesFile(file: File): List<PackagesInfo> {
    val packages = mutableListOf<PackagesInfo>()
    var currentPackage = PackagesInfo()
    var multilineDescription = false

    file.forEachLine { line ->
        when {
            line.startsWith("Package:") -> {
                currentPackage.packageInfo = line.removePrefix("Package:").trim()
                multilineDescription = false
            }
            line.startsWith("Version:") -> currentPackage.version = line.removePrefix("Version:").trim()
            line.startsWith("Architecture:") -> currentPackage.architecture = line.removePrefix("Architecture:").trim()
            line.startsWith("Maintainer:") -> currentPackage.maintainer = line.removePrefix("Maintainer:").trim()
            line.startsWith("Installed-Size:") -> currentPackage.installedSize = line.removePrefix("Installed-Size:").trim()
            line.startsWith("Depends:") -> currentPackage.depends = line.removePrefix("Depends:").trim()
            line.startsWith("Pre-Depends:") -> currentPackage.preDepends = line.removePrefix("Pre-Depends:").trim()
            line.startsWith("Recommends:") -> currentPackage.recommends = line.removePrefix("Recommends:").trim()
            line.startsWith("Suggests:") -> currentPackage.suggests = line.removePrefix("Suggests:").trim()
            line.startsWith("Enhances:") -> currentPackage.enhances = line.removePrefix("Enhances:").trim()
            line.startsWith("Built-Using:") -> currentPackage.builtUsing = line.removePrefix("Built-Using:").trim()
            line.startsWith("Conflicts:") -> currentPackage.conflicts = line.removePrefix("Conflicts:").trim()
            line.startsWith("Replaces:") -> currentPackage.replaces = line.removePrefix("Replaces:").trim()
            line.startsWith("Provides:") -> currentPackage.provides = line.removePrefix("Provides:").trim()
            line.startsWith("Filename:") -> currentPackage.fileName = line.removePrefix("Filename:").trim()
            line.startsWith("Size:") -> currentPackage.size = line.removePrefix("Size:").trim().toLongOrNull()
            line.startsWith("MD5sum:") -> currentPackage.md5sum = line.removePrefix("MD5sum:").trim()
            line.startsWith("SHA1:") -> currentPackage.sha1 = line.removePrefix("SHA1:").trim()
            line.startsWith("SHA256:") -> currentPackage.sha256 = line.removePrefix("SHA256:").trim()
            line.startsWith("Section:") -> currentPackage.section = line.removePrefix("Section:").trim()
            line.startsWith("Priority:") -> currentPackage.priority = line.removePrefix("Priority:").trim()
            line.startsWith("Homepage:") -> currentPackage.homepage = line.removePrefix("Homepage:").trim()
            line.startsWith("Description:") -> {
                currentPackage.description = line.removePrefix("Description:").trim()
                multilineDescription = true
            }
            line.isBlank() -> {
                packages.add(currentPackage)
                currentPackage = PackagesInfo() // start a new package when encountering an empty line
                multilineDescription = false
            }
            multilineDescription -> currentPackage.description += "\n${line.trim()}"
        }
    }

    // add the last package if the last line isn't empty
    if (currentPackage.packageInfo != null) {
        packages.add(currentPackage)
    }

    return packages
}
