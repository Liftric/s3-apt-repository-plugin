package com.liftric.apt.extensions

import org.gradle.api.Project
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.SetProperty
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.Optional
import org.gradle.api.provider.Property
import java.io.Serializable

data class DebianFile(
    val file: RegularFileProperty,
    val architectures: SetProperty<String>,
    val bucket: String?,
    val bucketPath: String?,
    val region: String?,
    val accessKey: String?,
    val secretKey: String?,
    val suite: String?,
    val component: String?,
) : Serializable

@Suppress("MemberVisibilityCanBePrivate")
@ConfigDsl
class DebianFileBuilder(@get:Internal val project: Project) {
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

    fun build(): DebianFile = DebianFile(
        file = file,
        architectures = architectures,
        bucket = bucket.orNull,
        bucketPath = bucketPath.orNull,
        region = region.orNull,
        accessKey = accessKey.orNull,
        secretKey = secretKey.orNull,
        suite = suite.orNull,
        component = component.orNull,
    )
}