package com.liftric.apt.extensions

import org.gradle.api.Project
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property

abstract class S3AptRepositoryExtension(val project: Project) {
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

    /** Override Versions that already exist in Apt Repository, Default is true */
    abstract val override: Property<Boolean>

    /** You can specify multiple Debian Files for different Architectures */
    abstract val debianFiles: ListProperty<DebianFileBuilder>

    /** PGP Signing of Release File */
    abstract val signingKeyRingFile: RegularFileProperty
    abstract val signingKeyPassphrase: Property<String>
}

fun S3AptRepositoryExtension.debian(action: DebianFileBuilder.() -> Unit) {
    debianFiles.add(DebianFileBuilder(project).apply(action))
}
