package com.liftric.apt.tasks

import com.liftric.apt.service.AwsS3Client
import com.liftric.apt.service.*
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.*
import org.gradle.api.provider.Property

abstract class CleanPackagesTask : DefaultTask() {
    @get:Input
    abstract val accessKey: Property<String>

    @get:Input
    abstract val secretKey: Property<String>

    @get:Input
    abstract val suite: Property<String>

    @get:Input
    abstract val component: Property<String>

    @get:Input
    abstract val region: Property<String>

    @get:Input
    abstract val bucket: Property<String>

    @get:Input
    abstract val bucketPath: Property<String>

    @TaskAction
    fun main() {
        val accessKeyValue = accessKey.get()
        val secretKeyValue = secretKey.get()
        val regionValue = region.get()
        val bucketValue = bucket.get()
        val bucketPathValue = bucketPath.get()
        val suiteValue = suite.get()
        val componentValue = component.get()

        val s3Client = AwsS3Client(accessKeyValue, secretKeyValue, regionValue)
        cleanPackages(logger, s3Client, bucketValue, bucketPathValue, suiteValue, componentValue)
    }
}
