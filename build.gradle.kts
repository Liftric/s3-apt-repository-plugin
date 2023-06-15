@Suppress("DSL_SCOPE_VIOLATION") // IntelliJ incorrectly marks libs as not callable
plugins {
    `maven-publish`
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.kotlinSerialization)
    alias(libs.plugins.dockerCompose)
    alias(libs.plugins.gradlePluginPublish)
    alias(libs.plugins.nemerosaVersioning)
}

group = "com.liftric"
version = with(versioning.info) {
    if (branch == "HEAD" && dirty.not()) {
        tag
    } else {
        full
    }
}

repositories {
    mavenCentral()
    gradlePluginPortal()
}

sourceSets {
    val main by getting
    val integrationMain by creating {
        compileClasspath += main.output
        runtimeClasspath += main.output
    }
}

tasks {
    val test by existing
    withType<Test> {
        useJUnitPlatform()
        testLogging {
            events("passed", "skipped", "failed")
        }
        systemProperty("org.gradle.testkit.dir", gradle.gradleUserHomeDir)
    }

    register<Test>("integrationTest") {
        val integrationMain by sourceSets
        description = "Runs the integration tests"
        group = "verification"
        testClassesDirs = integrationMain.output.classesDirs
        classpath = integrationMain.runtimeClasspath
        mustRunAfter(test)
        useJUnitPlatform()
    }
}


gradlePlugin {
    val integrationMain by sourceSets
    testSourceSets(integrationMain)
    plugins {
        create("s3-apt-repository-plugin") {
            id = "$group.s3-apt-repository-plugin"
            implementationClass = "$group.apt.S3AptRepositoryPlugin"
            displayName = "s3-apt-repository-plugin"
        }
    }
}

pluginBundle {
    website = "https://github.com/Liftric/s3-apt-repository-plugin"
    vcsUrl = "https://github.com/Liftric/s3-apt-repository-plugin"
    description = "A Gradle Plugin for managing an APT Repository on S3"
    tags = listOf("s3", "apt", "repository", "plugin", "gradle", "debian")
}

dependencies {
    implementation(libs.kotlinStdlibJdk8)
    implementation(libs.apacheCommons)
    implementation(libs.xz)
    implementation(libs.bouncyCastleGPG)
    implementation(libs.bouncyCastleProvider)
    implementation(libs.awsS3)

    testImplementation(libs.junitJupiter)
    testImplementation(libs.mockk)

    "integrationMainImplementation"(gradleTestKit())
    "integrationMainImplementation"(libs.junitJupiter)
    "integrationMainImplementation"(libs.testContainersJUnit5)
    "integrationMainImplementation"(libs.testContainersMain)
    "integrationMainImplementation"(libs.minio)
}
