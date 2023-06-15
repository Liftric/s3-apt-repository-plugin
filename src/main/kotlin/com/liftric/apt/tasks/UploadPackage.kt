package com.liftric.apt.tasks

import com.liftric.apt.service.AwsS3Client
import com.liftric.apt.extensions.DebPackage
import com.liftric.apt.service.*
import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.tasks.*
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Optional

/**
 * Uploads a debian packages to a s3 bucket and create or update the Repository.
 */

abstract class UploadPackage : DefaultTask() {
    @get:Nested
    abstract val debianFiles: ListProperty<DebPackage>

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

    @get:Input
    @get:Optional
    abstract val override: Property<Boolean>

    @get:Input
    @get:Optional
    abstract val signingKeyRingFile: RegularFileProperty

    @get:Input
    @get:Optional
    abstract val signingKeyPassphrase: Property<String>

    @get:Input
    @get:Optional
    abstract val origin: Property<String>

    @get:Input
    @get:Optional
    abstract val label: Property<String>

    @get:Input
    @get:Optional
    abstract val suite: Property<String>

    @get:Input
    @get:Optional
    abstract val component: Property<String>

    @TaskAction
    fun main() {
        val debianFilesValues = debianFiles.get()
        val accessKeyValue = accessKey.get()
        val secretKeyValue = secretKey.get()
        val bucketValue = bucket.get()
        val bucketPathValue = bucketPath.get()
        val regionValue = region.get()
        val endpointValue = endpoint.orNull
        val originValue = origin.get()
        val labelValue = label.get()
        val suiteValue = suite.get()
        val componentValue = component.get()

        debianFilesValues.forEach { debianFile ->
            val inputFile = debianFile.file.get().asFile
            val archs = debianFile.architectures.get()
            val accessKey = debianFile.accessKey.orNull ?: accessKeyValue
            val secretKey = debianFile.secretKey.orNull ?: secretKeyValue
            val region = debianFile.region.orNull ?: regionValue
            val endpoint = debianFile.endpoint.orNull ?: endpointValue
            val bucket = debianFile.bucket.orNull ?: bucketValue
            val bucketPath = debianFile.bucketPath.orNull ?: bucketPathValue
            val suite = debianFile.suite.orNull ?: suiteValue
            val component = debianFile.component.orNull ?: componentValue
            val origin = debianFile.origin.orNull ?: originValue
            val label = debianFile.label.orNull ?: labelValue
            val packageName = debianFile.packageName.orNull
            val packageVersion = debianFile.packageVersion.orNull
            val signingKeyRingFileValue = signingKeyRingFile.orNull?.asFile
            val signingKeyPassphraseValue = signingKeyPassphrase.orNull

            val s3Client = AwsS3Client(accessKey, secretKey, region, endpoint)

            val debianPoolBucketKey = getPoolBucketKey(inputFile.name, component)

            val debianPackages =
                PackagesFactory.parseDebianFile(inputFile, archs, debianPoolBucketKey, packageName, packageVersion)

            uploadDebianFile(
                logger,
                s3Client,
                bucket,
                bucketPath,
                debianPoolBucketKey,
                inputFile,
                override.getOrElse(true)
            )

            val packagesFiles =
                getUpdatedPackagesFiles(logger, suite, component, s3Client, bucket, bucketPath, debianPackages)

            uploadPackagesFiles(logger, packagesFiles, s3Client, bucket)

            updateReleaseFiles(
                logger,
                s3Client,
                bucket,
                bucketPath,
                suite,
                component,
                origin,
                label,
                signingKeyRingFileValue,
                signingKeyPassphraseValue
            )
        }
    }
}
