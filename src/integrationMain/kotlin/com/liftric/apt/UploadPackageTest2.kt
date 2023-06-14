package com.liftric.apt

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

const val UPLOAD_PACKAGE_TEST_LOCATION_2 = "build/uploadPackageTest2"

/**
 * This test verifies the functionality of the uploadPackage Gradle task, which is
 * responsible for creating an Apt Repository in an empty bucket or updating an existing Apt Repository.
 * This Class test the creation of a new Apt Repository.
 *
 * To ensure the newly created Apt Repository is functional, we use an Ubuntu container for
 * testing. This container adds the new Apt Repository and attempts to install a package from it.
 * The use of an Ubuntu container provides a controlled environment in which to verify the
 * functionality of the new repository. The successful installation of the package indicates
 * that the repository is working.
 */


@Testcontainers
class UploadPackageTest2 : AbstractContainerBaseTest() {

    @Container
    val ubuntuContainer: GenericContainer<*> =
        GenericContainer(DockerImageName.parse("ubuntu:22.04"))
            .withNetwork(network)
            .withCommand("tail", "-f", "/dev/null")

    @Test
    fun testUploadPackageTask() {
        val projectDir = File(UPLOAD_PACKAGE_TEST_LOCATION_2)
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
    version = "1.0.0"
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
    bucket.set("$UPLOAD_PACKAGE_TEST_BUCKET_2")
    region.set("eu-central-1")
    endpoint.set("http://localhost:${MINIO_CONTAINER.getMappedPort(MINIO_PORT)}")
    accessKey.set("$MINIO_ACCESS_KEY")
    secretKey.set("$MINIO_SECRET_KEY")
    signingKeyPassphrase.set("$SIGNING_KEY_PASSPHRASE")
    signingKeyRingFile.set(file("$PRIVATE_KEY_FILE"))
    debPackage {
        file.set(createDeb.archiveFile)
        architectures.set(setOf("all"))
        origin.set("Liftric")
        label.set("Liftric")
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
                echo "deb http://minio:$MINIO_PORT/$UPLOAD_PACKAGE_TEST_BUCKET_2 stable main" | tee /etc/apt/sources.list.d/s3bucket.list &&
                apt-get update -y &&
                apt-get install -y foobar &&
                cat /usr/bin/foobar
                """
            )

        assertTrue(packageInstall.stdout.contains(VERIFICATION_STRING))
        assertTrue(packageInstall.exitCode == 0)
    }
}
