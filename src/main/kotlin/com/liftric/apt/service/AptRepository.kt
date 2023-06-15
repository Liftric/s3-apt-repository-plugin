package com.liftric.apt.service

import com.liftric.apt.model.*
import com.liftric.apt.utils.FileHashUtil.compressWithGzip
import com.liftric.apt.utils.FileHashUtil.md5Hash
import com.liftric.apt.utils.FileHashUtil.sha1Hash
import com.liftric.apt.utils.FileHashUtil.sha256Hash
import com.liftric.apt.utils.FileHashUtil.sha512Hash
import com.liftric.apt.utils.signReleaseFile
import org.gradle.api.logging.Logger
import java.io.File
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.*

/**
 * This file contains several utility functions for managing and interacting
 * with an APT (Advanced Package Tool) repository that's hosted on an Amazon S3 bucket.
 * It provides capabilities for operations like uploading, cleaning, updating, and
 * maintaining packages in the repository.
 */


fun uploadDebianFile(
    logger: Logger,
    s3Client: AwsS3Client,
    bucket: String,
    bucketPath: String,
    bucketKey: String,
    file: File,
    override: Boolean,
) {
    val fullBucketKey = getFullBucketKey(bucketPath, bucketKey)
    val exist = s3Client.doesObjectExist(bucket, bucketKey)

    if (!exist || override) {
        s3Client.uploadObject(bucket, fullBucketKey, file)
        logger.info("File uploaded to s3://$bucket/${fullBucketKey}")
    } else {
        throw RuntimeException("Version already exist in Repo")
    }
}

fun getPoolBucketKey(fileName: String, component: String): String {
    val firstLetter = fileName.substring(0, 1)
    val packageName = fileName.substringBefore("_")
    return "pool/$component/$firstLetter/$packageName/$fileName"
}

fun cleanPackages(
    logger: Logger,
    s3Client: AwsS3Client,
    bucket: String,
    bucketPath: String,
    component: String,
    usedPackages: Set<String>,
) {
    val files: List<String> = s3Client.listAllObjects(bucket, getFullBucketKey(bucketPath, "pool/$component/"))
    val filesToRemove = files.filter { !usedPackages.contains(it) }
    if (filesToRemove.isEmpty()) {
        logger.info("No files to remove")
        return
    }
    val deletedObjects = s3Client.deleteObjects(bucket, filesToRemove)
    logger.info("Deleted ${deletedObjects.deleted().size} objects")
}

fun getUsedPackagesPoolKeys(
    s3Client: AwsS3Client,
    bucket: String,
    bucketPath: String,
    suite: String,
    component: String,
): Set<String> {
    val fileKeys = s3Client.listAllObjects(bucket, getFullBucketKey(bucketPath, "dists/$suite/$component/binary-"))
    return fileKeys.filter { key -> key.endsWith("Packages") }
        .map { key -> s3Client.getObject(bucket, key) }
        .flatMap { file ->
            PackagesFactory.parsePackagesFile(file)
        }
        .map { packagesInfo -> getFullBucketKey(bucketPath, packagesInfo.fileName) }
        .toSet()
}

fun updateReleaseFiles(
    logger: Logger,
    s3Client: AwsS3Client,
    bucket: String,
    bucketPath: String,
    suite: String,
    component: String,
    origin: String,
    label: String,
    signingKeyRingFile: File? = null,
    signingKeyPassphrase: String? = null,
) {
    val releaseFileLocation = getFullBucketKey(bucketPath, "dists/$suite/")

    val oldReleaseInfo: ReleaseInfo? = try {
        val releaseFile = s3Client.getObject(bucket, "${releaseFileLocation}Release")
        logger.info("Parsing Release file: s3://$bucket/${releaseFileLocation}Release")
        ReleaseFactory.parseReleaseFile(releaseFile)
    } catch (e: Exception) {
        logger.info("Release File not found, creating new Release file")
        null
    }

    val files: List<String> =
        s3Client.listAllObjects(bucket, getFullBucketKey(bucketPath, "dists/$suite/$component/binary-"))

    val fileDataList = files.map { filePath ->
        val packageFile = s3Client.getObject(bucket, filePath)
        val relativeFilePath = "$component${filePath.substringAfter("$suite/$component")}"

        FileData(
            md5Sum = MD5Sum(packageFile.md5Hash(), packageFile.length(), relativeFilePath),
            sha1Sum = SHA1(packageFile.sha1Hash(), packageFile.length(), relativeFilePath),
            sha256Sum = SHA256(packageFile.sha256Hash(), packageFile.length(), relativeFilePath),
            sha512Sum = SHA512(packageFile.sha512Hash(), packageFile.length(), relativeFilePath)
        )
    }

    val newReleaseInfo = ReleaseInfo(
        origin = origin,
        label = label,
        suite = suite,
        components = component,
        architectures = getReleaseArchitecturesFromS3FileList(files),
        codename = oldReleaseInfo?.codename,
        date = getReleaseDate(),
        description = oldReleaseInfo?.description,
        version = oldReleaseInfo?.version,
        validUntil = oldReleaseInfo?.validUntil,
        notAutomatic = oldReleaseInfo?.notAutomatic,
        butAutomaticUpgrades = oldReleaseInfo?.butAutomaticUpgrades,
        acquireByHash = oldReleaseInfo?.acquireByHash,
        changelogs = oldReleaseInfo?.changelogs,
        snapshots = oldReleaseInfo?.snapshots,
        md5Sum = fileDataList.map { it.md5Sum },
        sha1 = fileDataList.map { it.sha1Sum },
        sha256 = fileDataList.map { it.sha256Sum },
        sha512 = fileDataList.map { it.sha512Sum },
    )

    val releaseFile = createTemporaryFile(newReleaseInfo.toFileString(), "Release")

    s3Client.uploadObject(bucket, "${releaseFileLocation}Release", releaseFile)
    logger.info("Release file uploaded to s3://$bucket/${releaseFileLocation}Release")
    if (signingKeyRingFile != null && signingKeyPassphrase != null) {
        val signedReleaseFile = signReleaseFile(signingKeyRingFile, signingKeyPassphrase.toCharArray(), releaseFile)
        s3Client.uploadObject(bucket, "${releaseFileLocation}Release.gpg", signedReleaseFile)
        logger.info("Signed Release file uploaded to s3://$bucket/${releaseFileLocation}Release.gpg")
    }
}

fun uploadPackagesFiles(
    logger: Logger,
    packageFiles: Map<String, File>,
    s3Client: AwsS3Client,
    bucket: String,
) {
    packageFiles.forEach { (key, value) ->
        logger.info("Uploading $key")
        s3Client.uploadObject(bucket, key, value)
    }
}

fun getUpdatedPackagesFiles(
    logger: Logger,
    suite: String,
    component: String,
    s3Client: AwsS3Client,
    bucket: String,
    bucketPath: String,
    debianPackages: List<DebianPackage>,
): Map<String, File> {
    return debianPackages.flatMap { debianPackage ->
        val relativePackagesFileLocation = "dists/$suite/$component/binary-${debianPackage.architecture}/Packages"
        val packagesFileLocation = getFullBucketKey(bucketPath, relativePackagesFileLocation)

        val packagesFile = try {
            val packagesFile = s3Client.getObject(bucket, packagesFileLocation)
            logger.info("Parsing Packages file: s3://$bucket/${packagesFileLocation}")
            val oldDebianPackages = PackagesFactory.parsePackagesFile(packagesFile)
            val combinedPackages = oldDebianPackages.combineDebianPackages(debianPackage)
            createTemporaryFile(combinedPackages.toFileString(), packagesFileLocation)
        } catch (e: Exception) {
            logger.info("Packages file for '${debianPackage.architecture}' not found, creating new Packages file")
            createTemporaryFile(debianPackage.toFileString(), packagesFileLocation)
        }
        val gzipPackagesFile = packagesFile.compressWithGzip()

        listOf(
            packagesFileLocation to packagesFile,
            "$packagesFileLocation.gz" to gzipPackagesFile
        )
    }.toMap()
}

fun getCleanedPackagesFiles(
    logger: Logger,
    suite: String,
    component: String,
    s3Client: AwsS3Client,
    bucket: String,
    bucketPath: String,
    debianPackages: List<DebianPackage>,
): Map<String, File> {
    return debianPackages.flatMap { debianPackage ->
        val relativePackagesFileLocation = "dists/$suite/$component/binary-${debianPackage.architecture}/Packages"
        val packagesFileLocation = getFullBucketKey(bucketPath, relativePackagesFileLocation)

        val packagesFile = try {
            val packagesFile = s3Client.getObject(bucket, packagesFileLocation)
            logger.info("Parsing Packages file: s3://$bucket/${packagesFileLocation}")
            val oldDebianPackages = PackagesFactory.parsePackagesFile(packagesFile)
            val cleanedDebianPackages = oldDebianPackages.removeDebianPackage(debianPackage)
            createTemporaryFile(cleanedDebianPackages.toFileString(), packagesFileLocation)
        } catch (e: Exception) {
            logger.error("Packages file for Architecture '${debianPackage.architecture}' not found! Can't remove '${debianPackage.packageName}'")
            throw e
        }

        val gzipPackagesFile = packagesFile.compressWithGzip()

        listOf(
            packagesFileLocation to packagesFile,
            "$packagesFileLocation.gz" to gzipPackagesFile
        )
    }.toMap()
}


private fun getReleaseDate(): String {
    val now = ZonedDateTime.now(ZoneId.of("UTC"))
    val formatter = DateTimeFormatter.ofPattern("EEE, dd MMM yyyy HH:mm:ss z", Locale.ENGLISH)
    return now.format(formatter)
}

private fun getReleaseArchitecturesFromS3FileList(files: List<String>): String {
    return files.map { filePath ->
        filePath.substringAfterLast("binary-").substringBefore("/Packages")
    }.toSet().joinToString(separator = " ")
}

private fun getFullBucketKey(bucketPath: String, bucketKey: String): String {
    return if (bucketPath.lastOrNull() == '/') {
        "$bucketPath$bucketKey"
    } else if (bucketPath == "") {
        bucketKey
    } else {
        "$bucketPath/$bucketKey"
    }
}

private fun createTemporaryFile(content: String, fileName: String, prefix: String? = null): File {
    return File.createTempFile(fileName, prefix).apply {
        writeText(content)
        deleteOnExit()
    }
}
