package com.liftric.apt.tasks

import com.liftric.apt.service.AwsS3Client
import com.liftric.apt.service.*
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.*
import org.gradle.api.provider.Property

/**
 * Delete all Packages from pool that are not referenced in the Packages file
 */

abstract class CleanPackages : DefaultTask() {
    @get:Input
    abstract val suite: Property<String>

    @get:Input
    abstract val components: Property<String>

    @get:Input
    abstract val accessKey: Property<String>

    @get:Input
    abstract val secretKey: Property<String>

    @get:Input
    abstract val bucket: Property<String>

    @get:Input
    abstract val bucketPath: Property<String>

    @get:Input
    abstract val region: Property<String>

    @get:Input
    @get:Optional
    abstract val endpoint: Property<String>

    @TaskAction
    fun main() {
        val suiteValue = suite.get()
        val componentValue = components.get()
        val accessKeyValue = accessKey.get()
        val secretKeyValue = secretKey.get()
        val bucketValue = bucket.get()
        val bucketPathValue = bucketPath.get()
        val regionValue = region.get()
        val endpointValue = endpoint.orNull

        val s3Client = AwsS3Client(accessKeyValue, secretKeyValue, regionValue, endpointValue)

        val usedPackages = getUsedPackagesPoolKeys(s3Client, bucketValue, bucketPathValue, suiteValue, componentValue)
        cleanPackages(logger, s3Client, bucketValue, bucketPathValue, componentValue, usedPackages)
    }
}
