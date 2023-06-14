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
    val endpoint: Property<String?> = project.objects.property(String::class.java)

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
}
