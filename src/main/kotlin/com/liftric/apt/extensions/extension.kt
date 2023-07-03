package com.liftric.apt.extensions

import org.gradle.api.Project
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property

abstract class S3AptRepositoryPluginExtension(val project: Project) {
    /**
     * AWS Configuration
     * These properties can be individually overridden for each Debian file.
     * For more information see the DebianFileBuilder
     */
    abstract val accessKey: Property<String>
    abstract val secretKey: Property<String>
    abstract val bucket: Property<String>
    abstract val bucketPath: Property<String>
    abstract val region: Property<String>
    abstract val endpoint: Property<String>

    /** Override Versions that already exist in Apt Repository, Default is true */
    abstract val override: Property<Boolean>

    /** You can specify multiple Debian Files for different Architectures */
    abstract val debPackages: ListProperty<DebPackage>

    /** PGP Signing of Release File */
    abstract val signingKeyRingFile: RegularFileProperty
    abstract val signingKeyPassphrase: Property<String>


    /*****************************************************************************
     * Release File Fields:
     * https://wiki.debian.org/DebianRepository/Format#A.22Release.22_files
     * If u don't know what to set, just leave it as it is.
     ******************************************************************************/

    /** Set the APT Repository Origin, Default: Debian */
    abstract val origin: Property<String>

    /** Set the APT Repository Label, Default: Debian */
    abstract val label: Property<String>

    /**
     *  The suite field may describe the suite. A suite is a single word.
     *  In Debian, this shall be one of oldstable, stable, testing, unstable, or experimental.
     *  Default: stable
     */
    abstract val suite: Property<String>

    /**
     * The Components property represents the components of an APT repository. In APT repositories, components
     * categorize software packages based on their source or licensing. Commonly used components are "main",
     * "contrib", and "non-free".
     *  Default: main
     */
    abstract val components: Property<String>

    /**
     * Optional: If not specified gets automated set by the plugin. It read all binary-* Folders and set
     * the architectures property. Can also be overridden by the user if needed.
     * Whitespace separated unique single words identifying Debian machine architectures
     */
    abstract val architectures: Property<String>

    /**
     * Optional: The Codename field shall describe the codename of the release.
     * A codename is a single word. Debian's releases are codenamed after Toy Story Characters,
     * and the unstable suite has the codename sid, the experimental suite has the codename experimental.
     */
    abstract val codename: Property<String>

    /**
     * Optional: The Date Field of the Release File. Gets automatically set by the plugin.
     * Can be overridden by the user if needed. Be sure to set a valid date format.
     */
    abstract val date: Property<String>

    /**
     * Optional: The Description Field of the Release File.
     * Gets automatically set by the plugin, if the old Release File has this Field set,
     * or if the user sets this property. Is not needed for a valid Release File.
     */
    abstract val releaseDescription: Property<String>

    /**
     * Optional: The Version Field of the Release File.
     * Gets automatically set by the plugin, if the old Release File has this Field set,
     * or if the user sets this property. Is not needed for a valid Release File.
     */
    abstract val releaseVersion: Property<String>


    /**
     * Optional: The Valid-Until Field of the Release File.
     * The Valid-Until field may specify at which time the Release file should be considered expired by the client.
     * Gets automatically set by the plugin, if the old Release File has this Field set,
     * or if the user sets this property. Is not needed for a valid Release File.
     */
    abstract val validUntil: Property<String>

    /**
     * Optional: The NotAutomatic and ButAutomaticUpgrades fields are optional boolean fields instructing the package manager.
     * They may contain the values "yes" and "no". If one the fields is not specified, this has the same meaning as a value of "no".
     * If a value of "yes" is specified for the NotAutomatic field, a package manager should not install
     * packages (or upgrade to newer versions) from this repository without explicit
     * user consent (APT assigns priority 1 to this) If the field ButAutomaticUpgrades is specified as well and has the value "yes",
     * the package manager should automatically install package upgrades from this repository,
     * if the installed version of the package is higher than the version of the package in other sources (APT assigns priority 100).
     * Specifying "yes" for ButAutomaticUpgrades without specifying "yes" for NotAutomatic is invalid.
     */
    abstract val notAutomatic: Property<String>
    abstract val butAutomaticUpgrades: Property<String>

    /**
     * Optional: The Changelogs field tells the client where to find changelogs.
     */
    abstract val changelogs: Property<String>

    /**
     * Optional: The Snapshots field tells the client where to find snapshots for this archive.
     */
    abstract val snapshots: Property<String>
}

fun S3AptRepositoryPluginExtension.debPackage(action: DebPackage.() -> Unit) {
    debPackages.add(DebPackage(project).apply(action))
}
