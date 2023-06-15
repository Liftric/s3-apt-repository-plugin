package com.liftric.apt

import org.junit.jupiter.api.Assertions.assertNotNull
import org.gradle.testfixtures.ProjectBuilder
import org.junit.jupiter.api.Test


class S3AptRepositoryPluginTest {
    @Test
    fun testApply() {
        val project = ProjectBuilder.builder().build()
        project.pluginManager.apply("com.liftric.s3-apt-repository-plugin")
        assertNotNull(project.plugins.getPlugin(S3AptRepositoryPlugin::class.java))
    }

    @Test
    fun testExtension() {
        val project = ProjectBuilder.builder().build()
        project.pluginManager.apply("com.liftric.s3-apt-repository-plugin")
        assertNotNull(project.s3AptRepository())
    }

    @Test
    fun testTasks() {
        val project = ProjectBuilder.builder().build()
        project.pluginManager.apply("com.liftric.s3-apt-repository-plugin")
        assertNotNull(project.tasks.findByName("uploadPackage"))
        assertNotNull(project.tasks.findByName("cleanPackages"))
        assertNotNull(project.tasks.findByName("removePackage"))
    }
}
