package com.liftric.apt.extensions

import org.gradle.api.Project
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property

abstract class PluginExtension(val project: Project) {
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
    abstract val debianFiles: ListProperty<PackageBuilder>

    /** PGP Signing of Release File */
    abstract val signingKeyRingFile: RegularFileProperty
    abstract val signingKeyPassphrase: Property<String>

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
     * The component property represents the component of an APT repository. In APT repositories, components
     * categorize software packages based on their source or licensing. Commonly used components are "main",
     * "contrib", and "non-free".
     *  Default: main
     */
    abstract val component: Property<String>
}

fun PluginExtension.debian(action: PackageBuilder.() -> Unit) {
    debianFiles.add(PackageBuilder(project).apply(action))
}
