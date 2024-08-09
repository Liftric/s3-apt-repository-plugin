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
 * Removes a Package from the Packages file List inside an S3 Apt Repository
 */

abstract class RemovePackage : DefaultTask() {
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
            val archs = debianFile.packageArchitectures.get()
            val packageName = debianFile.packageName.orNull
            val packageVersion = debianFile.packageVersion.orNull

            /** GPG Signing **/
            val signingKeyRingFileValue = signingKeyRingFile.orNull?.asFile
            val signingKeyPassphraseValue = signingKeyPassphrase.orNull

            /** AWS S3 values **/
            val accessKey = debianFile.accessKey.orNull ?: accessKey.get()
            val secretKey = debianFile.secretKey.orNull ?: secretKey.get()
            val bucket = debianFile.bucket.orNull ?: bucket.get()
            val bucketPath = debianFile.bucketPath.orNull ?: bucketPath.get()
            val region = debianFile.region.orNull ?: region.get()
            val endpoint = debianFile.endpoint.orNull ?: endpoint.orNull

            /** Release file values **/
            val origin = debianFile.origin.orNull ?: origin.get()
            val label = debianFile.label.orNull ?: label.get()
            val suite = debianFile.suite.orNull ?: suite.get()
            val components = debianFile.components.orNull ?: components.get()
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

            val s3Client = AwsS3Client(accessKey, secretKey, region, endpoint)
            val debianPackages =
                PackagesFactory.parseDebianFile(inputFile, archs, "", packageName, packageVersion)

            val packagesFiles =
                getCleanedPackagesFiles(
                    logger,
                    suite,
                    components,
                    s3Client,
                    bucket,
                    bucketPath,
                    debianPackages
                )

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
