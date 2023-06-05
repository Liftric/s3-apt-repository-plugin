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
    val file: RegularFileProperty,
    val architectures: SetProperty<String>,
    val bucket: String?,
    val bucketPath: String?,
    val region: String?,
    val accessKey: String?,
    val secretKey: String?,
    val suite: String?,
    val component: String?,
    val packageName: String?,
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
        packageName = packageName.orNull,
        packageVersion = packageVersion.orNull,
    )
}