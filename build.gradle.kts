@Suppress("DSL_SCOPE_VIOLATION") // IntelliJ incorrectly marks libs as not callable
plugins {
    `maven-publish`
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.kotlinSerialization)
    alias(libs.plugins.dockerCompose)
    alias(libs.plugins.gradlePluginPublish)
}

repositories {
    mavenCentral()
    gradlePluginPortal()
}

sourceSets {
    val main by getting
    val integrationTest by creating {
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

    val integrationTest by sourceSets
    val integrationTestTask = register<Test>("integrationTest") {
        description = "Runs the integration tests"
        group = "verification"
        testClassesDirs = integrationTest.output.classesDirs
        classpath = integrationTest.runtimeClasspath
        mustRunAfter(test)
        useJUnitPlatform()
    }
}


gradlePlugin {
    val integrationTest by sourceSets
    testSourceSets(integrationTest)
    plugins {
        create("s3-apt-repository-plugin") {
            id = "${project.property("pluginGroup")}.${project.property("pluginName")}"
            implementationClass = "${project.property("pluginGroup")}.apt.S3AptRepositoryPlugin"
            displayName = project.property("pluginName").toString()
            version = project.property("pluginVersion").toString()
            group = project.property("pluginGroup").toString()
        }
    }
}

pluginBundle {
    website = "https://github.com/Liftric/s3-apt-repository-plugin"
    vcsUrl = "https://github.com/Liftric/s3-apt-repository-plugin"
    description = "A Gradle Plugin for managing an APT Repository on S3"
    tags = listOf("s3", "apt", "repository", "plugin", "gradle")
}

dependencies {
    implementation(libs.kotlinStdlibJdk8)
    implementation(libs.apacheCommons)
    implementation(libs.xz)
    implementation(libs.bouncyCastleGPG)
    implementation(libs.bouncyCastleProvider)
    implementation(libs.awsS3)

    testImplementation(libs.junitJupiter)

    "integrationTestImplementation"(gradleTestKit())
    "integrationTestImplementation"(libs.junitJupiter)
    "integrationTestImplementation"(libs.testContainersJUnit5)
    "integrationTestImplementation"(libs.testContainersMain)
    "integrationTestImplementation"("io.minio:minio:8.5.3")
}
