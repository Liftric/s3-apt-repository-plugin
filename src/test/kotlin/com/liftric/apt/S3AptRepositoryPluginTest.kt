package com.liftric.apt

import org.junit.jupiter.api.Assertions.assertNotNull
import org.gradle.testfixtures.ProjectBuilder
import org.junit.jupiter.api.Test


class S3AptRepositoryPluginTest {
    private val project = ProjectBuilder.builder().build()

    @Test
    fun testApply() {
        project.pluginManager.apply("com.liftric.s3-apt-repository-plugin")
        assertNotNull(project.plugins.getPlugin(S3AptRepositoryPlugin::class.java))
    }

    @Test
    fun testExtension() {
        project.pluginManager.apply("com.liftric.s3-apt-repository-plugin")
        assertNotNull(project.s3AptRepository())
    }

    @Test
    fun testTasks() {
        project.pluginManager.apply("com.liftric.s3-apt-repository-plugin")
        assertNotNull(project.tasks.findByName("uploadPackage"))
        assertNotNull(project.tasks.findByName("cleanPackages"))
        assertNotNull(project.tasks.findByName("removePackage"))
    }
}
