package com.liftric.apt.tasks

import com.liftric.apt.service.AwsS3Client
import com.liftric.apt.extensions.DebianFileBuilder
import com.liftric.apt.model.*
import com.liftric.apt.service.PackagesInfoFactory
import com.liftric.apt.utils.FileHashUtil.compressWithGzip
import com.liftric.apt.utils.FileHashUtil.md5Hash
import com.liftric.apt.utils.FileHashUtil.sha1Hash
import com.liftric.apt.utils.FileHashUtil.sha256Hash
import com.liftric.apt.utils.FileHashUtil.sha512Hash
import com.liftric.apt.utils.FileHashUtil.size
import com.liftric.apt.utils.signReleaseFile
import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.tasks.*
import java.io.File
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Optional
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.*

abstract class UploadDebianTask : DefaultTask() {
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
    abstract val debianFiles: ListProperty<DebianFileBuilder>

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
    fun updateRepo() {
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

            val s3Client = AwsS3Client(accessKey, secretKey, region)

            val packagesInfo = PackagesInfoFactory(inputFile)
            val debianPoolBucketKey = getPoolBucketKey(inputFile.name, suite)
            packagesInfo.packagesInfo.fileName = debianPoolBucketKey
            packagesInfo.packagesInfo.packageInfo = packageName ?: packagesInfo.packagesInfo.packageInfo
            packagesInfo.packagesInfo.version = packageVersion ?: packagesInfo.packagesInfo.version

            uploadDebianFile(s3Client, bucket, bucketPath, debianPoolBucketKey, inputFile)

            val packagesFiles =
                createPackagesFiles(archs, suite, component, s3Client, bucket, bucketPath, packagesInfo.packagesInfo)

            uploadPackagesFiles(packagesFiles, s3Client, bucket)
            updateReleaseFiles(s3Client, bucket, bucketPath, suite, component)

        }
    }

    private fun getPoolBucketKey(fileName: String, suite: String): String {
        val firstLetter = fileName.substring(0, 1)
        val packageName = fileName.substringBefore("_")
        return "pool/$suite/$firstLetter/$packageName/$fileName"
    }

    private fun updateReleaseFiles(
        s3Client: AwsS3Client,
        bucket: String,
        bucketPath: String,
        suite: String,
        component: String,
    ) {

        val files = s3Client.listAllObjects(bucket, getFullBucketKey(bucketPath, "dists/$suite/$component/binary-"))
        val releaseFileLocation = getFullBucketKey(bucketPath, "dists/$suite/")
        var releaseInfo = ReleaseInfo()
        try {
            val releaseFile = s3Client.getObject(bucket, "${releaseFileLocation}Release")
            logger.info("Parsing Release file: s3://$bucket/${releaseFileLocation}Release")
            releaseInfo = parseReleaseFile(releaseFile)
        } catch (e: Exception) {
            logger.info("Release File not found, creating new Release file")
        }

        releaseInfo.architectures = getReleaseArchitecturesFromS3FileList(files)

        for (filePath in files) {
            val packageFile = s3Client.getObject(bucket, filePath)
            val relativeFilePath = "$component${filePath.substringAfter("$suite/$component")}"

            val md5Sum = MD5Sum(
                packageFile.md5Hash(),
                packageFile.size(),
                relativeFilePath,
            )
            releaseInfo.md5Sum.add(md5Sum)

            val sha1 = SHA1(
                packageFile.sha1Hash(),
                packageFile.size(),
                relativeFilePath,
            )
            releaseInfo.sha1.add(sha1)

            val sha256 = SHA256(
                packageFile.sha256Hash(),
                packageFile.size(),
                relativeFilePath,
            )
            releaseInfo.sha256.add(sha256)

            val sha512 = SHA512(
                packageFile.sha512Hash(),
                packageFile.size(),
                relativeFilePath,
            )
            releaseInfo.sha512.add(sha512)
        }
        releaseInfo.date = getReleaseDate()

        val releaseString = releaseInfo.toFileString()
        val releaseFile = File.createTempFile("${releaseFileLocation}Release", null).apply {
            writeText(releaseString)
            deleteOnExit()
        }

        val signingKeyRingFileValue = signingKeyRingFile.orNull?.asFile
        val signingKeyPassphraseValue = signingKeyPassphrase.orNull
        if (signingKeyRingFileValue != null && signingKeyPassphraseValue != null) {
            val signedReleaseFile =
                signReleaseFile(signingKeyRingFileValue, signingKeyPassphraseValue.toCharArray(), releaseFile)
            s3Client.uploadObject(bucket, "${releaseFileLocation}Release.gpg", signedReleaseFile)
            logger.info("Signed Release file uploaded to s3://$bucket/${releaseFileLocation}Release.gpg")
            s3Client.uploadObject(bucket, "${releaseFileLocation}Release", releaseFile)
            logger.info("Release file uploaded to s3://$bucket/${releaseFileLocation}Release")
        } else {
            s3Client.uploadObject(bucket, "${releaseFileLocation}Release", releaseFile)
            logger.info("Release file uploaded to s3://$bucket/${releaseFileLocation}Release")
        }
    }

    private fun getReleaseDate(): String {
        val now = ZonedDateTime.now()
        val formatter = DateTimeFormatter.ofPattern("EEE, dd MMM yyyy HH:mm:ss z", Locale.ENGLISH)
        return now.format(formatter)
    }

    private fun getReleaseArchitecturesFromS3FileList(files: List<String>): String {
        val archs = mutableSetOf<String>()
        for (filePath in files) {
            val arch = filePath.substringAfterLast("binary-").substringBefore("/Packages")
            archs.add(arch)
        }
        return archs.joinToString(separator = " ")
    }

    private fun uploadPackagesFiles(packageFiles: Map<String, File>, s3Client: AwsS3Client, bucket: String) {
        packageFiles.forEach { (key, value) ->
            logger.info("Uploading $key")
            s3Client.uploadObject(bucket, key, value)
        }
    }

    private fun createPackagesFiles(
        archList: Set<String>,
        suite: String,
        component: String,
        s3Client: AwsS3Client,
        bucket: String,
        bucketPath: String,
        packagesInfo: PackagesInfo,
    ): Map<String, File> {
        val packagesFiles = mutableMapOf<String, File>()
        archList.forEach { arch ->
            val relativePackagesFileLocation = "dists/$suite/$component/binary-$arch/Packages"
            val packagesFileLocation = getFullBucketKey(bucketPath, relativePackagesFileLocation)

            packagesInfo.architecture = arch
            val packagesFile = getPackagesFile(arch, s3Client, bucket, packagesInfo, packagesFileLocation)
            val gzipPackagesFile = packagesFile.compressWithGzip()

            packagesFiles[packagesFileLocation] = packagesFile
            packagesFiles["$packagesFileLocation.gz"] = gzipPackagesFile
        }

        return packagesFiles
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

    private fun getPackagesFile(
        arch: String,
        s3Client: AwsS3Client,
        bucket: String,
        packagesInfo: PackagesInfo,
        packagesFileLocation: String,
    ): File {
        return try {
            val packagesFile = s3Client.getObject(bucket, packagesFileLocation)
            logger.info("Parsing Packages file: s3://$bucket/${packagesFileLocation}")
            parsePackagesFile(packagesFile, packagesInfo)
        } catch (e: Exception) {
            logger.info("Packages file for '$arch' not found, creating new Packages file")
            val packagesString = packagesInfo.toFileString()
            val packagesFile = File.createTempFile(packagesFileLocation, null).apply {
                writeText(packagesString)
                deleteOnExit()
            }
            packagesFile
        }
    }

    private fun parsePackagesFile(file: File, packagesInfo: PackagesInfo): File {
        val packagesInfoList: List<PackagesInfo> = readPackagesFile(file)
        val sb = StringBuilder()

        var found = false
        packagesInfoList.forEach { packageInfo ->
            if (packageInfo.packageInfo == packagesInfo.packageInfo && packageInfo.version == packagesInfo.version) {
                found = true
                sb.append(packagesInfo.toFileString())
            } else {
                sb.append(packageInfo.toFileString())
            }
        }

        if (!found) {
            sb.append(packagesInfo.toFileString())
        }

        val packagesFileContent = sb.toString()
        val packagesFileTemp = File.createTempFile("packages", null).apply {
            writeText(packagesFileContent)
            deleteOnExit()
        }
        return packagesFileTemp
    }

    private fun uploadDebianFile(
        s3Client: AwsS3Client,
        bucket: String,
        bucketPath: String,
        bucketKey: String,
        file: File,
    ) {
        val fullBucketKey = getFullBucketKey(bucketPath, bucketKey)
        val exist = s3Client.doesObjectExist(bucket, bucketKey)

        if (!exist || override.getOrElse(true)) {
            s3Client.uploadObject(bucket, fullBucketKey, file)
            logger.info("File uploaded to s3://$bucket/${fullBucketKey}")
        } else {
            throw RuntimeException("Version already exist in Repo")
        }
    }
}
