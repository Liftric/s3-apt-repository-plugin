package com.liftric.apt

import io.minio.GetObjectArgs
import io.minio.errors.ErrorResponseException
import org.gradle.testkit.runner.GradleRunner
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.fail
import org.testcontainers.containers.GenericContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.utility.DockerImageName
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.StandardCopyOption

/**
 * This test verifies the correct functioning of the cleanPackages Gradle task.
 * cleanPackages is a Gradle task that deletes unnecessary debian files from an APT repository, i.e., debian files
 * that are not listed in the Packages file.
 *
 * To test this task, an Ubuntu container is used to create a controlled environment
 * where a dummy APT repository is set up. The repository contains a package "foobar"
 * with multiple versions present in the pool directory but only one version is actually listed in the Packages file.
 *
 * After executing the task, the test checks whether the repository still functions correctly and
 * that no extra files are removed. It further checks the removal of old versions of the Debian package
 * from a minio bucket to ensure the cleanPackages task's effectiveness.
 */


@Testcontainers
class CleanPackagesTest : ContainerBase() {
    private val cleanPackagesTestLocation = "build/cleanPackagesTest"

    @Container
    val ubuntuContainer: GenericContainer<*> =
        GenericContainer(DockerImageName.parse("ubuntu:22.04"))
            .withNetwork(network)
            .withCommand("tail", "-f", "/dev/null")

    @Test
    fun testCleanPackageTask() {
        uploadObjects(
            CLEAN_PACKAGES_TEST_BUCKET, mapOf(
                "src/integrationMain/resources/cleanPackages/Release" to "dists/stable/Release",
                "src/integrationMain/resources/cleanPackages/Release.gpg" to "dists/stable/Release.gpg",
                "src/integrationMain/resources/cleanPackages/Packages" to "dists/stable/main/binary-all/Packages",
                "src/integrationMain/resources/cleanPackages/Packages.gz" to "dists/stable/main/binary-all/Packages.gz",
                "src/integrationMain/resources/cleanPackages/foobar_1.0.0-1_all.deb" to "pool/main/f/foobar/foobar_1.0.0-1_all.deb",
                "src/integrationMain/resources/cleanPackages/foobar_0.0.9-1_all.deb" to "pool/main/f/foobar/foobar_0.0.9-1_all.deb",
            )
        )

        val projectDir = File(cleanPackagesTestLocation)
        projectDir.mkdirs()
        Files.copy(
            Paths.get("src/integrationMain/resources/$PRIVATE_KEY_FILE"),
            projectDir.toPath().resolve(PRIVATE_KEY_FILE),
            StandardCopyOption.REPLACE_EXISTING
        )
        Files.copy(
            Paths.get("src/integrationMain/resources/$PUBLIC_KEY_FILE"),
            projectDir.toPath().resolve(PUBLIC_KEY_FILE),
            StandardCopyOption.REPLACE_EXISTING
        )
        projectDir.resolve("foobar").writeText(VERIFICATION_STRING)
        projectDir.resolve("settings.gradle.kts").writeText("")
        projectDir.resolve("build.gradle.kts").writeText(
            """
import com.liftric.apt.extensions.*
import com.netflix.gradle.plugins.deb.Deb

plugins {
    id("com.liftric.s3-apt-repository-plugin")
    id("com.netflix.nebula.ospackage") version "11.9.1"
}

group = "com.liftric.test"
version = "1.0.0"

s3AptRepository {
    bucket.set("$CLEAN_PACKAGES_TEST_BUCKET")
    region.set("eu-central-1")
    endpoint.set("http://localhost:${MINIO_CONTAINER.getMappedPort(MINIO_PORT)}")
    usePathStyle.set(true)
    accessKey.set("$MINIO_ACCESS_KEY")
    secretKey.set("$MINIO_SECRET_KEY")
}
        """
        )

        val result = GradleRunner.create().withProjectDir(projectDir).withArguments("build", "cleanPackages")
            .withPluginClasspath().build()
        assertTrue(result.output.contains("BUILD SUCCESSFUL"))

        val packageInstall = ubuntuContainer
            .withPrivilegedMode(true)
            .withNetwork(network)
            .execInContainer(
                "bash", "-c", """
                apt-get update -y &&
                apt-get install -y gnupg &&
                apt-key adv --keyserver keyserver.ubuntu.com --recv-keys $SIGNING_KEY_ID_LONG &&
                echo "deb http://minio:$MINIO_PORT/$CLEAN_PACKAGES_TEST_BUCKET stable main" | tee /etc/apt/sources.list.d/s3bucket.list &&
                apt-get update -y &&
                apt-get install -y foobar &&
                cat /usr/bin/foobar
                """
            )
        assertTrue(packageInstall.stdout.contains(VERIFICATION_STRING))
        assertTrue(packageInstall.exitCode == 0)

        try {
            minioClient.getObject(
                GetObjectArgs.builder()
                    .bucket(REMOVE_PACKAGE_TEST_BUCKET)
                    .`object`("pool/main/f/foobar/foobar_0.0.9-1_all.deb")
                    .build()
            )
        } catch (e: ErrorResponseException) {
            assertTrue(e.errorResponse().code() == "NoSuchKey")
            assertTrue(e.message == "The specified key does not exist.")
        } catch (e: Exception) {
            fail("Unexpected exception: ${e.message}")
        }
    }
}
