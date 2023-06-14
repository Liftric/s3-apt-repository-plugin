package com.liftric.apt

import io.minio.*
import org.testcontainers.containers.GenericContainer
import org.testcontainers.utility.DockerImageName
import io.minio.errors.MinioException
import org.testcontainers.containers.Network

const val MINIO_PORT = 9000
const val MINIO_ACCESS_KEY = "admin"
const val MINIO_SECRET_KEY = "abcd1234"
const val MINIO_BUCKET_REGION = "eu-central-1"
const val SIGNING_KEY_ID_SHORT = "CAAB5A05"
const val SIGNING_KEY_ID_LONG = "D84ED0773ABB0A0AF2D9921331860335CAAB5A05"
const val SIGNING_KEY_PASSPHRASE = "abcd1234"
const val PRIVATE_KEY_FILE = "private.key"
const val PUBLIC_KEY_FILE = "public.key"
const val VERIFICATION_STRING = "BAZ_BAR"
const val VERIFICATION_STRING_2 = "BAZ_BAR2"
const val UPLOAD_PACKAGE_TEST_BUCKET = "upload-package-test-1"
const val UPLOAD_PACKAGE_TEST_BUCKET_2 = "upload-package-test-2"
const val REMOVE_PACKAGE_TEST_BUCKET = "remove-package-test-1"
const val CLEAN_PACKAGES_TEST_BUCKET = "clean-packages-test-1"

/**
 * This is an abstract base class upon which all test classes are built.
 * This class sets up a simulated AWS S3 environment by starting a Minio container.
 * It prepares the environment for each test by creating dedicated buckets.
 * This includes initializing test buckets, creating the Minio client, and
 * preparing the buckets by ensuring they exist and have the correct policies.
 * Additionally, it provides a method to upload objects to these buckets.
 */

abstract class AbstractContainerBaseTest {
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
            GenericContainer(DockerImageName.parse("quay.io/minio/minio:RELEASE.2023-06-02T23-17-26Z"))
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
    }
}
