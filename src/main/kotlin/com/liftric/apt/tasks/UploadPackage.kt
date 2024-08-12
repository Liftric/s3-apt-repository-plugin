package com.liftric.apt.tasks

import com.liftric.apt.service.AwsS3Client
import com.liftric.apt.extensions.DebPackage
import com.liftric.apt.model.ReleaseInfo
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
    abstract val usePathStyle: Property<Boolean>

    @get:Input
    @get:Optional
    abstract val override: Property<Boolean>

    @get:InputFile
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
    abstract val components: Property<String>

    @get:Input
    @get:Optional
    abstract val architectures: Property<String>

    @get:Input
    @get:Optional
    abstract val codename: Property<String>

    @get:Input
    @get:Optional
    abstract val date: Property<String>

    @get:Input
    @get:Optional
    abstract val releaseDescription: Property<String>

    @get:Input
    @get:Optional
    abstract val releaseVersion: Property<String>

    @get:Input
    @get:Optional
    abstract val validUntil: Property<String>

    @get:Input
    @get:Optional
    abstract val notAutomatic: Property<String>

    @get:Input
    @get:Optional
    abstract val butAutomaticUpgrades: Property<String>

    @get:Input
    @get:Optional
    abstract val changelogs: Property<String>

    @get:Input
    @get:Optional
    abstract val snapshots: Property<String>

    @TaskAction
    fun main() {
        debianFiles.get().forEach { debianFile ->
            /** depPackage File **/
            val inputFile = debianFile.file.get().asFile

            /** Package file values **/
            val packageName = debianFile.packageName.orNull
            val packageVersion = debianFile.packageVersion.orNull
            val packageArchitectures = debianFile.packageArchitectures.get()

            /** GPG Signing **/
            val signingKeyRingFileValue = signingKeyRingFile.orNull?.asFile
            val signingKeyPassphraseValue = signingKeyPassphrase.orNull

            /** AWS S3 values **/
            val accessKey = debianFile.accessKey.orNull ?: accessKey.get()
            val secretKey = debianFile.secretKey.orNull ?: secretKey.get()
            val region = debianFile.region.orNull ?: region.get()
            val endpoint = debianFile.endpoint.orNull ?: endpoint.orNull
            val usePathStyle = debianFile.usePathStyle.orNull ?: usePathStyle.orNull ?: false
            val bucket = debianFile.bucket.orNull ?: bucket.get()
            val bucketPath = debianFile.bucketPath.orNull ?: bucketPath.get()

            /** Release file values **/
            val suite = debianFile.suite.orNull ?: suite.get()
            val components = debianFile.components.orNull ?: components.get()
            val origin = debianFile.origin.orNull ?: origin.get()
            val label = debianFile.label.orNull ?: label.get()
            val architectures = debianFile.architectures.orNull ?: architectures.orNull
            val codename = debianFile.codename.orNull ?: codename.orNull
            val date = debianFile.date.orNull ?: date.orNull
            val releaseDescription = debianFile.releaseDescription.orNull ?: releaseDescription.orNull
            val releaseVersion = debianFile.releaseVersion.orNull ?: releaseVersion.orNull
            val validUntil = debianFile.validUntil.orNull ?: validUntil.orNull
            val notAutomatic = debianFile.notAutomatic.orNull ?: notAutomatic.orNull
            val butAutomaticUpgrades = debianFile.butAutomaticUpgrades.orNull ?: butAutomaticUpgrades.orNull
            val changelogs = debianFile.changelogs.orNull ?: changelogs.orNull
            val snapshots = debianFile.snapshots.orNull ?: snapshots.orNull

            val s3Client = AwsS3Client(accessKey, secretKey, region, endpoint, usePathStyle)
            val debianPoolBucketKey = getPoolBucketKey(inputFile.name, components)
            val debianPackages = PackagesFactory.parseDebianFile(
                inputFile, packageArchitectures, debianPoolBucketKey, packageName, packageVersion
            )

            uploadDebianFile(
                logger, s3Client, bucket, bucketPath, debianPoolBucketKey, inputFile, override.getOrElse(true)
            )

            val packagesFiles =
                getUpdatedPackagesFiles(logger, suite, components, s3Client, bucket, bucketPath, debianPackages)

            uploadPackagesFiles(logger, packagesFiles, s3Client, bucket)

            val releaseInfo = ReleaseInfo(
                origin,
                label,
                suite,
                components,
                architectures,
                codename,
                date,
                releaseDescription,
                releaseVersion,
                validUntil,
                notAutomatic,
                butAutomaticUpgrades,
                changelogs,
                snapshots,
                md5Sum = listOf(),
                sha1 = listOf(),
                sha256 = listOf(),
                sha512 = listOf()
            )

            updateReleaseFile(
                logger,
                s3Client,
                bucket,
                bucketPath,
                releaseInfo,
                signingKeyRingFileValue,
                signingKeyPassphraseValue
            )
        }
    }
}
