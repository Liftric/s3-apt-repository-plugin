rootProject.name = "s3-apt-repository-plugin"

pluginManagement {
    dependencyResolutionManagement {
        versionCatalogs {
            create("libs") {
                version("kotlin", "1.9.25")
                version("junit", "5.10.3")
                version("awsSdk", "2.27.0")
                version("bouncycastle", "1.78.1")
                version("testContainers", "1.20.1")

                plugin("dockerCompose", "com.avast.gradle.docker-compose").version("0.17.7")
                plugin("kotlinJvm", "org.jetbrains.kotlin.jvm").versionRef("kotlin")
                plugin("kotlinSerialization", "org.jetbrains.kotlin.plugin.serialization").versionRef("kotlin")
                plugin("gradlePluginPublish", "com.gradle.plugin-publish").version("1.2.1")
                plugin("nemerosaVersioning", "net.nemerosa.versioning").version("3.1.0")

                library("kotlinStdlibJdk8", "org.jetbrains.kotlin", "kotlin-stdlib-jdk8").versionRef("kotlin")
                library("junitBom", "org.junit", "junit-bom").versionRef("junit")
                library("junitJupiter", "org.junit.jupiter", "junit-jupiter").versionRef("junit")
                library("awsS3", "software.amazon.awssdk", "s3").versionRef("awsSdk")
                library("bouncyCastleGPG", "org.bouncycastle", "bcpg-jdk18on").versionRef("bouncycastle")
                library("bouncyCastleProvider", "org.bouncycastle", "bcprov-jdk18on").versionRef("bouncycastle")
                library("xz", "org.tukaani", "xz").version("1.10")
                library("apacheCommons", "org.apache.commons", "commons-compress").version("1.27.0")
                library("testContainersJUnit5", "org.testcontainers", "junit-jupiter").versionRef("testContainers")
                library("testContainersMain", "org.testcontainers", "testcontainers").versionRef("testContainers")
                library("minio", "io.minio", "minio").version("8.5.11")
                library("mockk", "io.mockk", "mockk").version("1.13.12")
            }
        }
    }
}
