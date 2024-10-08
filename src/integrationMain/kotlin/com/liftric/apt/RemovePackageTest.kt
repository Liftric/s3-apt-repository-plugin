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


@Testcontainers
class RemovePackageTest : ContainerBase() {
    private val removePackageTestLocation = "build/removePackageTest"

    @Container
    val ubuntuContainer: GenericContainer<*> =
        GenericContainer(DockerImageName.parse("ubuntu:22.04"))
            .withNetwork(network)
            .withCommand("tail", "-f", "/dev/null")

    @Test
    fun testRemovePackageTask() {
        uploadObjects(
            REMOVE_PACKAGE_TEST_BUCKET, mapOf(
                "src/integrationMain/resources/removePackage/Release" to "dists/stable/Release",
                "src/integrationMain/resources/removePackage/Release.gpg" to "dists/stable/Release.gpg",
                "src/integrationMain/resources/removePackage/Packages" to "dists/stable/main/binary-all/Packages",
                "src/integrationMain/resources/removePackage/Packages.gz" to "dists/stable/main/binary-all/Packages.gz",
                "src/integrationMain/resources/removePackage/foobar_1.0.0-1_all.deb" to "pool/main/f/foobar/foobar_1.0.0-1_all.deb",
                "src/integrationMain/resources/removePackage/foobar_0.0.9-1_all.deb" to "pool/main/f/foobar/foobar_0.0.9-1_all.deb",
            )
        )

        val projectDir = File(removePackageTestLocation)
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
    usePathStyle.set(true)
    accessKey.set("$MINIO_ACCESS_KEY")
    secretKey.set("$MINIO_SECRET_KEY")
    signingKeyPassphrase.set("$SIGNING_KEY_PASSPHRASE")
    signingKeyRingFile.set(file("$PRIVATE_KEY_FILE"))
    debPackage {
        file.set(createDeb.archiveFile)
        packageArchitectures.set(setOf("all"))
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

        val outputFile = getFileFromBucket("dists/stable/main/binary-all/Packages", REMOVE_PACKAGE_TEST_BUCKET)
        val debianPackages = PackagesFactory.parsePackagesFile(outputFile)

        assertTrue(debianPackages.size == 1)
        assertTrue(debianPackages[0].packageName == "foobar")
        assertTrue(debianPackages[0].version == "1.0.0-1")
    }
}
