package com.liftric.apt.service

import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider
import software.amazon.awssdk.core.sync.RequestBody
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials
import software.amazon.awssdk.services.s3.model.*
import java.io.File

/**
 * The AwsS3Client class is a utility for interacting with AWS S3 service.
 * It provides simplified methods for common operations such as checking if an object exists,
 * uploading an object, and retrieving an object from an S3 bucket.
 *
 * The class employs the AWS SDK's S3Client to establish a connection to AWS S3,
 * which is configured at the time of the AwsS3Client instance creation.
 * The AWS region, access key, and secret key are required parameters.
 *
 * The provided methods wrap the underlying AWS SDK calls, abstracting away the complexity
 * of the raw API and offering a more streamlined and intuitive interface for the following operations:
 *
 * - doesObjectExist: Checks if an object exists in a specified S3 bucket.
 * - uploadObject: Uploads a file to a specified S3 bucket.
 * - getObject: Retrieves a specified object from an S3 bucket and writes it to a temporary local file.
 */

class AwsS3Client(
    accessKey: String,
    secretKey: String,
    region: String,
) {

    private val s3Client = S3Client.builder()
        .region(convertToRegion(region))
        .credentialsProvider(
            StaticCredentialsProvider.create(
                AwsBasicCredentials.create(
                    accessKey,
                    secretKey
                )
            )
        )
        .build()

    private fun convertToRegion(regionStr: String): Region {
        return Region.of(regionStr)
    }

    fun doesObjectExist(bucket: String, key: String): Boolean {
        return try {
            val headObjectRequest = HeadObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .build()
            s3Client.headObject(headObjectRequest)
            true
        } catch (e: NoSuchKeyException) {
            false
        }
    }

    fun uploadObject(bucket: String, key: String, file: File): PutObjectResponse {
        val path = file.toPath()
        val request = PutObjectRequest.builder()
            .bucket(bucket)
            .key(key)
            .build()

        return s3Client.putObject(request, RequestBody.fromFile(path.toFile()))
    }

    fun getObject(bucket: String, key: String): File {
        val request = GetObjectRequest.builder()
            .bucket(bucket)
            .key(key)
            .build()

        val response = s3Client.getObject(request)
        val file = File.createTempFile(key, null).apply {
            deleteOnExit()
        }
        response.use { input ->
            file.outputStream().use { fileOut ->
                input.copyTo(fileOut)
            }
        }
        return file
    }

    fun listAllObjects(bucket: String, prefix: String? = null): List<String> {
        val builder = ListObjectsV2Request.builder().bucket(bucket)
        if (prefix != null) {
            builder.prefix(prefix)
        }
        var request = builder.build()
        val keys = mutableListOf<String>()

        var response: ListObjectsV2Response
        do {
            response = s3Client.listObjectsV2(request)
            response.contents().forEach {
                keys.add(it.key())
            }
            val token = response.nextContinuationToken()
            request = request.toBuilder().continuationToken(token).build()
        } while (response.isTruncated)

        return keys
    }

    fun deleteObjects(bucket: String, keys: List<String>): DeleteObjectsResponse {
        val request = DeleteObjectsRequest.builder()
            .bucket(bucket)
            .delete(Delete.builder().objects(keys.map { ObjectIdentifier.builder().key(it).build() }).build())
            .build()
        return s3Client.deleteObjects(request)
    }
}
