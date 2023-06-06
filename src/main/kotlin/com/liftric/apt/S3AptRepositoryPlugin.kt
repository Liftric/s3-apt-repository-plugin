package com.liftric.apt

import com.liftric.apt.extensions.PluginExtension
import com.liftric.apt.tasks.*
import org.gradle.api.Plugin
import org.gradle.api.Project

internal const val extensionName = "s3AptRepository"
internal const val taskGroup = "S3 Apt Repository Plugin"

class S3AptRepositoryPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        val extension =
            project.extensions.create(extensionName, PluginExtension::class.java, project)

        project.tasks.register("uploadPackage", UploadPackageTask::class.java) { task ->
            task.group = taskGroup
            task.description = "Upload Package to S3 and updates Repository Package List"
            task.accessKey.set(extension.accessKey)
            task.secretKey.set(extension.secretKey)
            task.bucket.set(extension.bucket)
            task.bucketPath.set(extension.bucketPath.convention(""))
            task.region.set(extension.region)
            task.override.set(extension.override)
            task.debianFiles.set(extension.debianFiles)
            task.signingKeyRingFile.set(extension.signingKeyRingFile)
            task.signingKeyPassphrase.set(extension.signingKeyPassphrase)
            task.origin.set(extension.origin)
            task.label.set(extension.label)
            task.suite.set(extension.suite)
            task.component.set(extension.component)
        }

        project.tasks.register("removePackage", RemovePackageTask::class.java) { task ->
            task.group = taskGroup
            task.description = "Remove Package from S3 APT Repository"
            task.accessKey.set(extension.accessKey)
            task.secretKey.set(extension.secretKey)
            task.bucket.set(extension.bucket)
            task.bucketPath.set(extension.bucketPath.convention(""))
            task.region.set(extension.region)
            task.debianFiles.set(extension.debianFiles)
            task.signingKeyRingFile.set(extension.signingKeyRingFile)
            task.signingKeyPassphrase.set(extension.signingKeyPassphrase)
            task.origin.set(extension.origin)
            task.label.set(extension.label)
            task.suite.set(extension.suite)
            task.component.set(extension.component)
        }

        project.tasks.register("cleanPackages", CleanPackagesTask::class.java) { task ->
            task.group = taskGroup
            task.description = "Delete removed Debian Files from S3"
            task.accessKey.set(extension.accessKey)
            task.secretKey.set(extension.secretKey)
            task.bucket.set(extension.bucket)
            task.bucketPath.set(extension.bucketPath.convention(""))
            task.region.set(extension.region)
            task.suite.set(extension.suite.convention("stable"))
            task.component.set(extension.component.convention("main"))
        }
    }
}

fun Project.dependencyTrackCompanion(): PluginExtension {
    return extensions.getByName(extensionName) as? PluginExtension
        ?: throw IllegalStateException("$extensionName is not of the correct type")
}
