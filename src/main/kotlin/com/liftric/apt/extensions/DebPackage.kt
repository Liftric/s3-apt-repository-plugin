package com.liftric.apt.extensions

import org.gradle.api.Project
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.SetProperty
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.Optional
import org.gradle.api.provider.Property

@Suppress("MemberVisibilityCanBePrivate")
@ConfigDsl
class DebPackage(@get:Internal val project: Project) {

    @get:Input
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
    // Optional: Used for override the default accessKey
    val accessKey: Property<String?> = project.objects.property(String::class.java)

    @get:Input
    @get:Optional
    // Used for override the default secretKey
    val secretKey: Property<String?> = project.objects.property(String::class.java)

    @get:Input
    @get:Optional
    // Used for override the default Release suite
    val suite: Property<String?> = project.objects.property(String::class.java)

    @get:Input
    @get:Optional
    // Used for override the default Release component
    val component: Property<String?> = project.objects.property(String::class.java)

    @get:Input
    @get:Optional
    // Used for override the default Release origin
    val origin: Property<String?> = project.objects.property(String::class.java)

    @get:Input
    @get:Optional
    // Used for override the default Release label
    val label: Property<String?> = project.objects.property(String::class.java)

    @get:Input
    // List of supported Architectures from Package
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
