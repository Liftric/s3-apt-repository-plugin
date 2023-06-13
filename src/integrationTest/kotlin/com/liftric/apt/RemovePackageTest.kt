package com.liftric.apt

import com.liftric.apt.model.readPackagesFile
import io.minio.GetObjectArgs
import org.gradle.testkit.runner.GradleRunner
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.fail
import org.junit.jupiter.api.io.TempDir
import org.testcontainers.containers.GenericContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.utility.DockerImageName
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.StandardCopyOption
import java.io.FileOutputStream
import java.io.InputStream

/**
 * This test verifies the correct functioning of the removePackage Gradle task.
 * removePackage is a Gradle task that removes a specific Version of a Package from
 * the Packages file from the Apt Repository. This task does not remove the actual
 * Debian File from the repository pool, so that users with an older version of the
 * Repository can still install the package. For deleting removed Packages use the
 * cleanPackages task.
 *
 * To ensure that the Apt Repository is still functioning correctly, an Ubuntu container
 * is used for testing. This provides a controlled environment where potential errors or
 * issues can be isolated and diagnosed more effectively.
 *
 * Additionally, the Packages file from the Minio client is downloaded and checked. This
 * helps to confirm that the correct version of the package has been removed as intended.
 */

const val REMOVE_PACKAGE_TEST_LOCATION = "build/removePackageTest"

@Testcontainers
class RemovePackageTest : AbstractContainerBaseTest() {
    @TempDir
    lateinit var tempDir: File

    @Container
    val ubuntuContainer: GenericContainer<*> =
        GenericContainer(DockerImageName.parse("ubuntu:22.04"))
            .withNetwork(network)
            .withCommand("tail", "-f", "/dev/null")

    @Test
    fun testRemovePackageTask() {
        uploadObjects(
            REMOVE_PACKAGE_TEST_BUCKET, mapOf(
                "src/integrationTest/resources/removePackage/Release" to "dists/stable/Release",
                "src/integrationTest/resources/removePackage/Release.gpg" to "dists/stable/Release.gpg",
                "src/integrationTest/resources/removePackage/Packages" to "dists/stable/main/binary-all/Packages",
                "src/integrationTest/resources/removePackage/Packages.gz" to "dists/stable/main/binary-all/Packages.gz",
                "src/integrationTest/resources/removePackage/foobar_1.0.0-1_all.deb" to "pool/main/f/foobar/foobar_1.0.0-1_all.deb",
                "src/integrationTest/resources/removePackage/foobar_0.0.9-1_all.deb" to "pool/main/f/foobar/foobar_0.0.9-1_all.deb",
            )
        )

        val projectDir = File(REMOVE_PACKAGE_TEST_LOCATION)
        projectDir.mkdirs()
        Files.copy(
            Paths.get("src/integrationTest/resources/$PRIVATE_KEY_FILE"),
            projectDir.toPath().resolve(PRIVATE_KEY_FILE),
            StandardCopyOption.REPLACE_EXISTING
        )
        Files.copy(
            Paths.get("src/integrationTest/resources/$PUBLIC_KEY_FILE"),
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
    id("com.netflix.nebula.ospackage") version "11.3.0"
}

group = "com.liftric.test"
version = "1.0.0"

val createDeb = tasks.create("createDeb", Deb::class) {
    packageName = "foobar"
    version = "0.0.9"
    release = "1"
    maintainer = "nvima <mirwald@liftric.com>"
    description = "Description"

    // Long Version of Key Id throws an error https://github.com/nebula-plugins/gradle-ospackage-plugin/issues/179#issuecomment-269423747
    signingKeyId = "$SIGNING_KEY_ID_SHORT"
    signingKeyPassphrase = "$MINIO_SECRET_KEY"
    signingKeyRingFile = file("$PRIVATE_KEY_FILE")

    into("/usr/bin") {
        from("./") {
            include("foobar")
        }
    }
}

s3AptRepository {
    bucket.set("$REMOVE_PACKAGE_TEST_BUCKET")
    region.set("eu-central-1")
    endpoint.set("http://localhost:${MINIO_CONTAINER.getMappedPort(MINIO_PORT)}")
    accessKey.set("$MINIO_ACCESS_KEY")
    secretKey.set("$MINIO_SECRET_KEY")
    signingKeyPassphrase.set("$SIGNING_KEY_PASSPHRASE")
    signingKeyRingFile.set(file("$PRIVATE_KEY_FILE"))
    debian {
        file.set(createDeb.archiveFile)
        architectures.set(setOf("all"))
        origin.set("Liftric")
        label.set("Liftric")
    }
}
        """
        )

        val result = GradleRunner.create().withProjectDir(projectDir).withArguments("build", "removePackage")
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
                echo "deb http://minio:$MINIO_PORT/$REMOVE_PACKAGE_TEST_BUCKET stable main" | tee /etc/apt/sources.list.d/s3bucket.list &&
                apt-get update -y &&
                apt-get install -y foobar &&
                cat /usr/bin/foobar
                """
            )
        assertTrue(packageInstall.stdout.contains(VERIFICATION_STRING))
        assertTrue(packageInstall.exitCode == 0)

        try {
            val stream: InputStream = minioClient.getObject(
                GetObjectArgs.builder()
                    .bucket(REMOVE_PACKAGE_TEST_BUCKET)
                    .`object`("dists/stable/main/binary-all/Packages")
                    .build()
            )

            val outputFile = File(tempDir, "outputFile")
            val outputStream = FileOutputStream(outputFile)
            val buffer = ByteArray(1024)
            var bytesRead: Int
            while (stream.read(buffer).also { bytesRead = it } != -1) {
                outputStream.write(buffer, 0, bytesRead)
            }
            outputStream.close()
            stream.close()

            val packagesInfos = readPackagesFile(outputFile)
            assertTrue(packagesInfos.size == 1)
            assertTrue(packagesInfos[0].packageInfo == "foobar")
            assertTrue(packagesInfos[0].version == "1.0.0-1")
        } catch (e: Exception) {
            fail("Unexpected exception: ${e.message}")
        }
    }
}
