package com.liftric.apt

import com.liftric.apt.service.PackagesFactory
import org.gradle.testkit.runner.GradleRunner
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.testcontainers.containers.GenericContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.utility.DockerImageName
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.StandardCopyOption


/**
 * This test verifies the functionality of the uploadPackage Gradle task, which is
 * responsible for creating an Apt Repository in an empty bucket or updating an existing Apt Repository.
 * This Class test the updating functionality of an existing Apt Repository.
 *
 * To ensure that the Apt Repository is updated correctly, an Ubuntu container
 * is used for testing. The newer version of the package with a different VERIFICATION_CODE
 * gets installed and tested.
 *
 * Additionally, the Packages, Release file from the Minio client is downloaded and checked. This
 * helps to confirm that the old Version is still present and the Release File is updated correctly.
 */


@Testcontainers
class UploadPackageTest : ContainerBase() {
    private val uploadPackageTestLocation = "build/uploadPackageTest"

    @Container
    val ubuntuContainer: GenericContainer<*> =
        GenericContainer(DockerImageName.parse("ubuntu:22.04"))
            .withNetwork(network)
            .withCommand("tail", "-f", "/dev/null")

    @Test
    fun testUploadPackageTask() {
        uploadObjects(
            UPLOAD_PACKAGE_TEST_BUCKET, mapOf(
                "src/integrationMain/resources/uploadPackage/Release" to "dists/stable/Release",
                "src/integrationMain/resources/uploadPackage/Release.gpg" to "dists/stable/Release.gpg",
                "src/integrationMain/resources/uploadPackage/Packages" to "dists/stable/main/binary-all/Packages",
                "src/integrationMain/resources/uploadPackage/Packages.gz" to "dists/stable/main/binary-all/Packages.gz",
                "src/integrationMain/resources/uploadPackage/foobar_1.0.0-1_all.deb" to "pool/main/f/foobar/foobar_1.0.0-1_all.deb",
            )
        )

        val projectDir = File(uploadPackageTestLocation)
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
        projectDir.resolve("foobar").writeText(VERIFICATION_STRING_2)
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
version = "1.0.1"

val createDeb = tasks.create("createDeb", Deb::class) {
    packageName = "foobar"
    version = "1.0.1"
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
    bucket.set("$UPLOAD_PACKAGE_TEST_BUCKET")
    region.set("eu-central-1")
    endpoint.set("http://localhost:${MINIO_CONTAINER.getMappedPort(MINIO_PORT)}")
    accessKey.set("$MINIO_ACCESS_KEY")
    secretKey.set("$MINIO_SECRET_KEY")
    signingKeyPassphrase.set("$SIGNING_KEY_PASSPHRASE")
    signingKeyRingFile.set(file("$PRIVATE_KEY_FILE"))
    origin.set("Foobar")
    label.set("Foobar")
    debPackage {
        file.set(createDeb.archiveFile)
        packageArchitectures.set(setOf("all"))
        releaseDescription.set("Foobar Repository")
        releaseVersion.set("1.0.0")
    }
}
        """
        )

        val result = GradleRunner
            .create()
            .withProjectDir(projectDir)
            .withArguments("build", "uploadPackage")
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
                echo "deb http://minio:$MINIO_PORT/$UPLOAD_PACKAGE_TEST_BUCKET stable main" | tee /etc/apt/sources.list.d/s3bucket.list &&
                apt-get update -y &&
                apt-get install -y foobar &&
                cat /usr/bin/foobar
                """
            )

        assertTrue(packageInstall.stdout.contains(VERIFICATION_STRING_2))
        assertTrue(packageInstall.exitCode == 0)

        val packagesFile = getFileFromBucket("dists/stable/main/binary-all/Packages", UPLOAD_PACKAGE_TEST_BUCKET)
        val debianPackages = PackagesFactory.parsePackagesFile(packagesFile)
        assertTrue(debianPackages.size == 2)
        assertTrue(debianPackages[0].packageName == "foobar")
        assertTrue(debianPackages[0].version == "1.0.0-1")

        val releaseFileString = getFileFromBucket("dists/stable/Release", UPLOAD_PACKAGE_TEST_BUCKET).readText()
        assertTrue(releaseFileString.contains("Version: 1.0.0"))
        assertTrue(releaseFileString.contains("Origin: Foobar"))
        assertTrue(releaseFileString.contains("Label: Foobar"))
        assertTrue(releaseFileString.contains("Description: Foobar Repository"))
    }
}
