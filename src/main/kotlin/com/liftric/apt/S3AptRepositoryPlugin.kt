package com.liftric.apt

import com.liftric.apt.extensions.S3AptRepositoryExtension
import com.liftric.apt.tasks.*
import org.gradle.api.Plugin
import org.gradle.api.Project

internal const val extensionName = "s3AptRepository"
internal const val taskGroup = "S3 Apt Repository Plugin"

class S3AptRepositoryPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        val extension =
            project.extensions.create(extensionName, S3AptRepositoryExtension::class.java, project)

        project.tasks.register("uploadDebian", CleanDebianTask::class.java) { task ->
            task.group = taskGroup
            task.description = "Upload Debian packages to S3 and update the repository"
            task.accessKey.set(extension.accessKey)
            task.secretKey.set(extension.secretKey)
            task.bucket.set(extension.bucket)
            task.bucketPath.set(extension.bucketPath.convention(""))
            task.region.set(extension.region)
            task.override.set(extension.override)
            task.debianFiles.set(extension.debianFiles)
            task.signingKeyRingFile.set(extension.signingKeyRingFile)
            task.signingKeyPassphrase.set(extension.signingKeyPassphrase)
        }
    }
}

fun Project.dependencyTrackCompanion(): S3AptRepositoryExtension {
    return extensions.getByName(extensionName) as? S3AptRepositoryExtension
        ?: throw IllegalStateException("$extensionName is not of the correct type")
}
