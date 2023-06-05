package com.liftric.apt.tasks

import com.liftric.apt.service.AwsS3Client
import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.*
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Optional

abstract class CleanPackagesTask : DefaultTask() {
    @get:Input
    abstract val accessKey: Property<String>

    @get:Input
    abstract val secretKey: Property<String>

    @get:Input
    abstract val region: Property<String>

    @get:Input
    abstract val bucket: Property<String>

    @get:Input
    abstract val bucketPath: Property<String>

    @get:Input
    @get:Optional
    abstract val signingKeyRingFile: RegularFileProperty

    @get:Input
    @get:Optional
    abstract val signingKeyPassphrase: Property<String>

    @TaskAction
    fun cleanPackages() {
        val accessKeyValue = accessKey.get()
        val secretKeyValue = secretKey.get()
        val regionValue = region.get()
        val bucketValue = bucket.get()
        val bucketPathValue = bucketPath.get()

        val s3Client = AwsS3Client(accessKeyValue, secretKeyValue, regionValue)
    }
}
