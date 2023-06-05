rootProject.name = "s3-apt-repository-plugin"

pluginManagement {
    dependencyResolutionManagement {
        versionCatalogs {
            create("libs") {
                version("kotlin", "1.8.21")
                version("ktor", "2.3.0")
                version("junit-bom", "5.9.3")
                version("awsSdk", "2.17.125")

                plugin("dockerCompose", "com.avast.gradle.docker-compose").version("0.16.12")
                plugin("kotlinJvm", "org.jetbrains.kotlin.jvm").versionRef("kotlin")
                plugin("kotlinSerialization", "org.jetbrains.kotlin.plugin.serialization").versionRef("kotlin")
                plugin("gradlePluginPublish", "com.gradle.plugin-publish").version("1.2.0")

                library("kotlinStdlibJdk8", "org.jetbrains.kotlin", "kotlin-stdlib-jdk8").versionRef("kotlin")
                library("junitBom", "org.junit", "junit-bom").versionRef("junit-bom")
                library("junitJupiter", "org.junit.jupiter", "junit-jupiter").versionRef("junit-bom")
                library("awsS3", "software.amazon.awssdk", "s3").versionRef("awsSdk")
            }
        }
    }
}
