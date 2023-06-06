package com.liftric.apt.extensions

import org.gradle.api.Project
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.SetProperty
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.Optional
import org.gradle.api.provider.Property
import java.io.Serializable

data class Package(
    /** The Package file to upload. Currently only Debian Files are supported */
    val file: RegularFileProperty,
    /** List of supported Architectures from Package */
    val architectures: SetProperty<String>,
    /** Optional: Used for override the default bucket */
    val bucket: String?,
    /** Optional: Used for override the default bucket path */
    val bucketPath: String?,
    /** Optional: Used for override the default region */
    val region: String?,
    /** Optional: Used for override the default accessKey */
    val accessKey: String?,
    /** Optional: Used for override the default secretKey */
    val secretKey: String?,
    /** Optional: Used for override the default Release suite */
    val suite: String?,
    /** Optional: Used for override the default Release component */
    val component: String?,
    /** Optional: Used for override the default Release origin */
    val origin: String?,
    /** Optional: Used for override the default Release label */
    val label: String?,
    /** Optional: Used for override the default package name */
    val packageName: String?,
    /** Optional: Used for override the default package version */
    val packageVersion: String?,
) : Serializable

@Suppress("MemberVisibilityCanBePrivate")
@ConfigDsl
class PackageBuilder(@get:Internal val project: Project) {
    @get:Input
    val file: RegularFileProperty = project.objects.fileProperty()

    @get:Input
    val architectures: SetProperty<String> = project.objects.setProperty(String::class.java)

    @get:Input
    @get:Optional
    val bucket: Property<String?> = project.objects.property(String::class.java)

    @get:Input
    @get:Optional
    val bucketPath: Property<String?> = project.objects.property(String::class.java)

    @get:Input
    @get:Optional
    val region: Property<String?> = project.objects.property(String::class.java)

    @get:Input
    @get:Optional
    val accessKey: Property<String?> = project.objects.property(String::class.java)

    @get:Input
    @get:Optional
    val secretKey: Property<String?> = project.objects.property(String::class.java)

    @get:Input
    @get:Optional
    val suite: Property<String?> = project.objects.property(String::class.java)

    @get:Input
    @get:Optional
    val component: Property<String?> = project.objects.property(String::class.java)

    @get:Input
    @get:Optional
    val origin: Property<String?> = project.objects.property(String::class.java)

    @get:Input
    @get:Optional
    val label: Property<String?> = project.objects.property(String::class.java)

    @get:Input
    @get:Optional
    val packageName: Property<String?> = project.objects.property(String::class.java)

    @get:Input
    @get:Optional
    val packageVersion: Property<String?> = project.objects.property(String::class.java)

    fun build(): Package = Package(
        file = file,
        architectures = architectures,
        bucket = bucket.orNull,
        bucketPath = bucketPath.orNull,
        region = region.orNull,
        accessKey = accessKey.orNull,
        secretKey = secretKey.orNull,
        suite = suite.orNull,
        component = component.orNull,
        origin = origin.orNull,
        label = label.orNull,
        packageName = packageName.orNull,
        packageVersion = packageVersion.orNull,
    )
}