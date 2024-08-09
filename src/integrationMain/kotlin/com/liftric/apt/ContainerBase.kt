package com.liftric.apt

import io.minio.*
import org.testcontainers.containers.GenericContainer
import org.testcontainers.utility.DockerImageName
import io.minio.errors.MinioException
import org.testcontainers.containers.Network
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

/**
 * This is an abstract base class upon which all test classes are built.
 * This class sets up a simulated AWS S3 environment by starting a Minio container.
 * It prepares the environment for each test by creating dedicated buckets.
 * This includes initializing test buckets, creating the Minio client, and
 * preparing the buckets by ensuring they exist and have the correct policies.
 * Additionally, it provides a method to upload objects to these buckets.
 */

abstract class ContainerBase {
    companion object {
        private val testBuckets =
            listOf(
                UPLOAD_PACKAGE_TEST_BUCKET,
                UPLOAD_PACKAGE_TEST_BUCKET_2,
                REMOVE_PACKAGE_TEST_BUCKET,
                CLEAN_PACKAGES_TEST_BUCKET
            )
        val network: Network = Network.newNetwork()

        val MINIO_CONTAINER: GenericContainer<*> =
            GenericContainer(DockerImageName.parse("quay.io/minio/minio:RELEASE.2024-08-03T04-33-23Z"))
                .withPrivilegedMode(true)
                .withNetwork(network)
                .withNetworkAliases("minio")
                .withEnv("MINIO_ROOT_USER", MINIO_ACCESS_KEY)
                .withEnv("MINIO_ROOT_PASSWORD", MINIO_SECRET_KEY)
                .withCommand("server", "--console-address", ":9090", "/data")
                .withExposedPorts(MINIO_PORT, 9090)
                .apply { start() }

        lateinit var minioClient: MinioClient

        init {
            try {
                minioClient = MinioClient.Builder()
                    .endpoint("http://localhost:${MINIO_CONTAINER.getMappedPort(MINIO_PORT)}")
                    .credentials(MINIO_ACCESS_KEY, MINIO_SECRET_KEY)
                    .build()

                testBuckets.forEach {
                    val found = minioClient.bucketExists(
                        BucketExistsArgs.builder()
                            .bucket(it)
                            .build()
                    )

                    if (found) {
                        minioClient.removeBucket(
                            RemoveBucketArgs.builder()
                                .bucket(it)
                                .build()
                        )
                    }

                    minioClient.makeBucket(
                        MakeBucketArgs.builder()
                            .bucket(it)
                            .region(MINIO_BUCKET_REGION)
                            .build()
                    )

                    minioClient.setBucketPolicy(
                        SetBucketPolicyArgs.builder()
                            .bucket(it)
                            .config(
                                """
                                {
                                    "Version": "2012-10-17",
                                    "Statement": [
                                        {
                                            "Sid": "AddPerm",
                                            "Effect": "Allow",
                                            "Principal": "*",
                                            "Action": [
                                                "s3:GetObject"
                                            ],
                                            "Resource": [
                                                "arn:aws:s3:::$it/*"
                                            ]
                                        }
                                    ]
                                }
                                """.trimIndent()
                            )
                            .build()
                    )
                }

            } catch (e: MinioException) {
                println("Error occurred: $e")
            }
        }

        fun uploadObjects(bucket: String, objects: Map<String, String>) {
            objects.forEach { (key, value) ->
                minioClient.uploadObject(
                    UploadObjectArgs.builder()
                        .bucket(bucket)
                        .filename(key)
                        .`object`(value)
                        .build()
                )
            }
        }

        fun getFileFromBucket(path: String, bucket: String): File {
            val stream: InputStream = minioClient.getObject(
                GetObjectArgs.builder()
                    .bucket(bucket)
                    .`object`(path)
                    .build()
            )
            return File.createTempFile(path.substringAfterLast("/"), null).apply {
                deleteOnExit()
                FileOutputStream(this).use { output ->
                    stream.copyTo(output)
                }
            }
        }
    }
}
