package com.liftric.apt.tasks

import com.liftric.apt.service.AwsS3Client
import com.liftric.apt.extensions.PackageBuilder
import com.liftric.apt.model.*
import com.liftric.apt.service.*
import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.tasks.*
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Optional

abstract class RemovePackageTask : DefaultTask() {
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

    @get:Nested
    abstract val debianFiles: ListProperty<PackageBuilder>

    @get:Input
    @get:Optional
    abstract val override: Property<Boolean>

    @get:Input
    @get:Optional
    abstract val signingKeyRingFile: RegularFileProperty

    @get:Input
    @get:Optional
    abstract val signingKeyPassphrase: Property<String>

    @TaskAction
    fun removePackage() {
        val debianFilesValues = debianFiles.get()
        val accessKeyValue = accessKey.get()
        val secretKeyValue = secretKey.get()
        val regionValue = region.get()
        val bucketValue = bucket.get()
        val bucketPathValue = bucketPath.get()

        debianFilesValues.forEach { debianFile ->
            val inputFile = debianFile.file.get().asFile
            val archs = debianFile.architectures.get()
            val accessKey = debianFile.accessKey.orNull ?: accessKeyValue
            val secretKey = debianFile.secretKey.orNull ?: secretKeyValue
            val region = debianFile.region.orNull ?: regionValue
            val bucket = debianFile.bucket.orNull ?: bucketValue
            val bucketPath = debianFile.bucketPath.orNull ?: bucketPathValue
            val suite = debianFile.suite.orNull ?: DEFAULT_SUITE
            val component = debianFile.component.orNull ?: DEFAULT_COMPONENT
            val packageName = debianFile.packageName.orNull
            val packageVersion = debianFile.packageVersion.orNull
            val signingKeyRingFileValue = signingKeyRingFile.orNull?.asFile
            val signingKeyPassphraseValue = signingKeyPassphrase.orNull

            val s3Client = AwsS3Client(accessKey, secretKey, region)

            val packagesInfo = PackagesInfoFactory(inputFile)
            val debianPoolBucketKey = getPoolBucketKey(inputFile.name, suite)
            packagesInfo.packagesInfo.fileName = debianPoolBucketKey
            packagesInfo.packagesInfo.packageInfo = packageName ?: packagesInfo.packagesInfo.packageInfo
            packagesInfo.packagesInfo.version = packageVersion ?: packagesInfo.packagesInfo.version

            val packagesFiles =
                cleanPackagesFiles(
                    logger,
                    archs,
                    suite,
                    component,
                    s3Client,
                    bucket,
                    bucketPath,
                    packagesInfo.packagesInfo
                )

            uploadPackagesFiles(logger, packagesFiles, s3Client, bucket)

            updateReleaseFiles(
                logger,
                s3Client,
                bucket,
                bucketPath,
                suite,
                component,
                signingKeyRingFileValue,
                signingKeyPassphraseValue
            )
        }
    }
}
