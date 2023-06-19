package com.liftric.apt

import com.liftric.apt.extensions.S3AptRepositoryPluginExtension
import com.liftric.apt.tasks.*
import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * This plugin provides tasks for managing an Apt repository hosted on S3.
 * The tasks include uploading a package, removing a package, and cleaning removed packages from the repository.
 * It allows configuration through a DSL by creating a PluginExtension object for the project.
 * These import in the build.gradle is needed to access the DSL:
 * import com.liftric.apt.extensions.*
 */

internal const val extensionName = "s3AptRepository"
internal const val taskGroup = "S3 Apt Repository Plugin"

const val DEFAULT_ORIGIN = "Debian"
const val DEFAULT_LABEL = "Debian"
const val DEFAULT_SUITE = "stable"
const val DEFAULT_COMPONENT = "main"

class S3AptRepositoryPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        val extension = project.extensions.create(extensionName, S3AptRepositoryPluginExtension::class.java, project)

        project.tasks.register("uploadPackage", UploadPackage::class.java) { task ->
            task.group = taskGroup
            task.description = "Upload Package to S3 and create/update S3 Apt Repository"
            task.accessKey.set(extension.accessKey)
            task.secretKey.set(extension.secretKey)
            task.bucket.set(extension.bucket)
            task.bucketPath.set(extension.bucketPath.convention(""))
            task.region.set(extension.region)
            task.endpoint.set(extension.endpoint)
            task.override.set(extension.override)
            task.debianFiles.set(extension.debPackages)
            task.signingKeyRingFile.set(extension.signingKeyRingFile)
            task.signingKeyPassphrase.set(extension.signingKeyPassphrase)
            task.origin.set(extension.origin.convention(DEFAULT_ORIGIN))
            task.label.set(extension.label.convention(DEFAULT_LABEL))
            task.suite.set(extension.suite.convention(DEFAULT_SUITE))
            task.components.set(extension.components.convention(DEFAULT_COMPONENT))
            task.architectures.set(extension.architectures)
            task.codename.set(extension.codename)
            task.date.set(extension.date)
            task.releaseDescription.set(extension.releaseDescription)
            task.releaseVersion.set(extension.releaseVersion)
            task.validUntil.set(extension.validUntil)
            task.notAutomatic.set(extension.notAutomatic)
            task.butAutomaticUpgrades.set(extension.butAutomaticUpgrades)
            task.changelogs.set(extension.changelogs)
            task.snapshots.set(extension.snapshots)
        }

        project.tasks.register("removePackage", RemovePackage::class.java) { task ->
            task.group = taskGroup
            task.description = "Remove Package from S3 Apt Repository Packages List"
            task.accessKey.set(extension.accessKey)
            task.secretKey.set(extension.secretKey)
            task.bucket.set(extension.bucket)
            task.bucketPath.set(extension.bucketPath.convention(""))
            task.region.set(extension.region)
            task.endpoint.set(extension.endpoint)
            task.debianFiles.set(extension.debPackages)
            task.signingKeyRingFile.set(extension.signingKeyRingFile)
            task.signingKeyPassphrase.set(extension.signingKeyPassphrase)
            task.origin.set(extension.origin.convention(DEFAULT_ORIGIN))
            task.label.set(extension.label.convention(DEFAULT_LABEL))
            task.suite.set(extension.suite.convention(DEFAULT_SUITE))
            task.components.set(extension.components.convention(DEFAULT_COMPONENT))
            task.architectures.set(extension.architectures)
            task.codename.set(extension.codename)
            task.date.set(extension.date)
            task.releaseDescription.set(extension.releaseDescription)
            task.releaseVersion.set(extension.releaseVersion)
            task.validUntil.set(extension.validUntil)
            task.notAutomatic.set(extension.notAutomatic)
            task.butAutomaticUpgrades.set(extension.butAutomaticUpgrades)
            task.changelogs.set(extension.changelogs)
            task.snapshots.set(extension.snapshots)
       }

        project.tasks.register("cleanPackages", CleanPackages::class.java) { task ->
            task.group = taskGroup
            task.description = "Delete removed Packages from S3 Apt Repository"
            task.accessKey.set(extension.accessKey)
            task.secretKey.set(extension.secretKey)
            task.bucket.set(extension.bucket)
            task.bucketPath.set(extension.bucketPath.convention(""))
            task.region.set(extension.region)
            task.endpoint.set(extension.endpoint)
            task.suite.set(extension.suite.convention(DEFAULT_SUITE))
            task.components.set(extension.components.convention(DEFAULT_COMPONENT))
        }
    }
}

fun Project.s3AptRepository(): S3AptRepositoryPluginExtension {
    return extensions.getByName(extensionName) as? S3AptRepositoryPluginExtension
        ?: throw IllegalStateException("$extensionName is not of the correct type")
}
