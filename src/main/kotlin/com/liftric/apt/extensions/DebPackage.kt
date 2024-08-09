package com.liftric.apt.extensions

import org.gradle.api.Project
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.SetProperty
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.Optional
import org.gradle.api.provider.Property
import org.gradle.api.tasks.InputFile

@Suppress("MemberVisibilityCanBePrivate")
@ConfigDsl
class DebPackage(@get:Internal val project: Project) {

    @get:InputFile
    // The Package file to upload. Currently only Debian Files are supported */
    val file: RegularFileProperty = project.objects.fileProperty()

    @get:Input
    @get:Optional
    // Used for override the default bucket
    val bucket: Property<String?> = project.objects.property(String::class.java)

    @get:Input
    @get:Optional
    // Used for override the default bucket path
    val bucketPath: Property<String?> = project.objects.property(String::class.java)

    @get:Input
    @get:Optional
    // Used for override the default region
    val region: Property<String?> = project.objects.property(String::class.java)

    @get:Input
    @get:Optional
    // Used for override the default S3 endpoint
    val endpoint: Property<String?> = project.objects.property(String::class.java)

    @get:Input
    @get:Optional
    // Used for override the default S3 endpoint
    val usePathStyle: Property<Boolean?> = project.objects.property(Boolean::class.java)

    @get:Input
    @get:Optional
    // Optional: Used for override the default accessKey
    val accessKey: Property<String?> = project.objects.property(String::class.java)

    @get:Input
    @get:Optional
    // Used for override the default secretKey
    val secretKey: Property<String?> = project.objects.property(String::class.java)

    @get:Input
    @get:Optional
    // Used for override the default Release Origin
    val origin: Property<String?> = project.objects.property(String::class.java)

    @get:Input
    @get:Optional
    // Used for override the default Release Label
    val label: Property<String?> = project.objects.property(String::class.java)

    @get:Input
    @get:Optional
    // Used for override the default Release Suite
    val suite: Property<String?> = project.objects.property(String::class.java)

    @get:Input
    @get:Optional
    // Used for override the default Release Component
    val components: Property<String?> = project.objects.property(String::class.java)

    @get:Input
    @get:Optional
    // Used for override the default Release Architectures
    val architectures: Property<String?> = project.objects.property(String::class.java)

    @get:Input
    @get:Optional
    // Used for override the default Release Codename
    val codename: Property<String?> = project.objects.property(String::class.java)

    @get:Input
    @get:Optional
    // Used for override the default Release Date
    val date: Property<String?> = project.objects.property(String::class.java)

    @get:Input
    @get:Optional
    // Used for override the default Release Description
    val releaseDescription: Property<String?> = project.objects.property(String::class.java)

    @get:Input
    @get:Optional
    // Used for override the default Release Version
    val releaseVersion: Property<String?> = project.objects.property(String::class.java)

    @get:Input
    @get:Optional
    // Used for override the default Release Valid-Until
    val validUntil: Property<String?> = project.objects.property(String::class.java)

    @get:Input
    @get:Optional
    // Used for override the default Release NotAutomatic
    val notAutomatic: Property<String?> = project.objects.property(String::class.java)

    @get:Input
    @get:Optional
    // Used for override the default Release ButAutomaticUpgrades
    val butAutomaticUpgrades: Property<String?> = project.objects.property(String::class.java)

    @get:Input
    @get:Optional
    // Used for override the default Release Changelogs
    val changelogs: Property<String?> = project.objects.property(String::class.java)

    @get:Input
    @get:Optional
    // Used for override the default Release Snapshots
    val snapshots: Property<String?> = project.objects.property(String::class.java)

    @get:Input
    // Set of supported Architectures from Package
    val packageArchitectures: SetProperty<String> = project.objects.setProperty(String::class.java)

    @get:Input
    @get:Optional
    // Used for override the default package name
    val packageName: Property<String?> = project.objects.property(String::class.java)

    @get:Input
    @get:Optional
    // Used for override the default package version
    val packageVersion: Property<String?> = project.objects.property(String::class.java)
}
