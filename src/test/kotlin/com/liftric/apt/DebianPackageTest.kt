package com.liftric.apt

import com.liftric.apt.model.DebianPackage
import com.liftric.apt.model.combineDebianPackages
import com.liftric.apt.model.removeDebianPackage
import org.junit.jupiter.api.Test


class DebianPackageTest {
    @Test
    fun `test combineDebianPackages`() {
        val package1 = DebianPackage(
            packageName = "foo",
            version = "1.0.0",
            architecture = "all",
            fileName = "foo_1.0.0_all.deb",
            size = "123",
            maintainer = null,
            installedSize = null,
            depends = null,
            conflicts = null,
            replaces = null,
            provides = null,
            preDepends = null,
            recommends = null,
            suggests = null,
            enhances = null,
            builtUsing = null,
            section = null,
            priority = null,
            homepage = null,
            description = null,
            sha1 = null,
            sha256 = null,
            md5sum = null
        )

        val package2 = DebianPackage(
            packageName = "foo",
            version = "1.0.1",
            architecture = "all",
            fileName = "foo_1.0.1_all.deb",
            size = "123",
            maintainer = null,
            installedSize = null,
            depends = null,
            conflicts = null,
            replaces = null,
            provides = null,
            preDepends = null,
            recommends = null,
            suggests = null,
            enhances = null,
            builtUsing = null,
            section = null,
            priority = null,
            homepage = null,
            description = null,
            sha1 = null,
            sha256 = null,
            md5sum = null
        )
        val packageList = listOf(package1, package2)

        val package3 = DebianPackage(
            packageName = "foo",
            version = "1.0.2",
            architecture = "all",
            fileName = "foo_1.0.2_all.deb",
            size = "234",
            maintainer = null,
            installedSize = null,
            depends = null,
            conflicts = null,
            replaces = null,
            provides = null,
            preDepends = null,
            recommends = null,
            suggests = null,
            enhances = null,
            builtUsing = null,
            section = null,
            priority = null,
            homepage = null,
            description = null,
            sha1 = null,
            sha256 = null,
            md5sum = null
        )

        val combined = packageList.combineDebianPackages(package3)
        assert(combined.contains(package1))
        assert(combined.contains(package2))
        assert(combined.contains(package3))
        assert(combined.size == 3)
    }

    @Test
    fun `test combineDebianPackages with duplicate`() {
        val package1 = DebianPackage(
            packageName = "foo",
            version = "1.0.0",
            architecture = "all",
            fileName = "foo_1.0.0_all.deb",
            size = "123",
            maintainer = null,
            installedSize = null,
            depends = null,
            conflicts = null,
            replaces = null,
            provides = null,
            preDepends = null,
            recommends = null,
            suggests = null,
            enhances = null,
            builtUsing = null,
            section = null,
            priority = null,
            homepage = null,
            description = null,
            sha1 = null,
            sha256 = null,
            md5sum = null
        )

        val package2 = DebianPackage(
            packageName = "foo",
            version = "1.0.1",
            architecture = "all",
            fileName = "foo_1.0.1_all.deb",
            size = "123",
            maintainer = null,
            installedSize = null,
            depends = null,
            conflicts = null,
            replaces = null,
            provides = null,
            preDepends = null,
            recommends = null,
            suggests = null,
            enhances = null,
            builtUsing = null,
            section = null,
            priority = null,
            homepage = null,
            description = null,
            sha1 = null,
            sha256 = null,
            md5sum = null
        )
        val packageList = listOf(package1, package2)

        val package3 = DebianPackage(
            packageName = "foo",
            version = "1.0.1",
            architecture = "all",
            fileName = "foo_1.0.1_all.deb",
            size = "234",
            maintainer = null,
            installedSize = null,
            depends = null,
            conflicts = null,
            replaces = null,
            provides = null,
            preDepends = null,
            recommends = null,
            suggests = null,
            enhances = null,
            builtUsing = null,
            section = null,
            priority = null,
            homepage = null,
            description = null,
            sha1 = null,
            sha256 = null,
            md5sum = null
        )

        val combined = packageList.combineDebianPackages(package3)
        assert(combined.contains(package1))
        assert(!combined.contains(package2))
        assert(combined.contains(package3))
        assert(combined.size == 2)
    }

    @Test
    fun `test removeDebianPackages`() {
        val package1 = DebianPackage(
            packageName = "foo",
            version = "1.0.0",
            architecture = "all",
            fileName = "foo_1.0.0_all.deb",
            size = "123",
            maintainer = null,
            installedSize = null,
            depends = null,
            conflicts = null,
            replaces = null,
            provides = null,
            preDepends = null,
            recommends = null,
            suggests = null,
            enhances = null,
            builtUsing = null,
            section = null,
            priority = null,
            homepage = null,
            description = null,
            sha1 = null,
            sha256 = null,
            md5sum = null
        )

        val package2 = DebianPackage(
            packageName = "foo",
            version = "1.0.1",
            architecture = "all",
            fileName = "foo_1.0.1_all.deb",
            size = "123",
            maintainer = null,
            installedSize = null,
            depends = null,
            conflicts = null,
            replaces = null,
            provides = null,
            preDepends = null,
            recommends = null,
            suggests = null,
            enhances = null,
            builtUsing = null,
            section = null,
            priority = null,
            homepage = null,
            description = null,
            sha1 = null,
            sha256 = null,
            md5sum = null
        )
        val packageList = listOf(package1, package2)

        val package3 = DebianPackage(
            packageName = "foo",
            version = "1.0.1",
            architecture = "all",
            fileName = "foo_1.0.1_all.deb",
            size = "123",
            maintainer = null,
            installedSize = null,
            depends = null,
            conflicts = null,
            replaces = null,
            provides = null,
            preDepends = null,
            recommends = null,
            suggests = null,
            enhances = null,
            builtUsing = null,
            section = null,
            priority = null,
            homepage = null,
            description = null,
            sha1 = null,
            sha256 = null,
            md5sum = null
        )

        val combined = packageList.removeDebianPackage(package3)
        assert(combined.contains(package1))
        assert(!combined.contains(package2))
        assert(!combined.contains(package3))
        assert(combined.size == 1)
    }

    @Test
    fun `test removeDebianPackages with a Package that is not there`() {
        val package1 = DebianPackage(
            packageName = "foo",
            version = "1.0.0",
            architecture = "all",
            fileName = "foo_1.0.0_all.deb",
            size = "123",
            maintainer = null,
            installedSize = null,
            depends = null,
            conflicts = null,
            replaces = null,
            provides = null,
            preDepends = null,
            recommends = null,
            suggests = null,
            enhances = null,
            builtUsing = null,
            section = null,
            priority = null,
            homepage = null,
            description = null,
            sha1 = null,
            sha256 = null,
            md5sum = null
        )

        val package2 = DebianPackage(
            packageName = "foo",
            version = "1.0.1",
            architecture = "all",
            fileName = "foo_1.0.1_all.deb",
            size = "123",
            maintainer = null,
            installedSize = null,
            depends = null,
            conflicts = null,
            replaces = null,
            provides = null,
            preDepends = null,
            recommends = null,
            suggests = null,
            enhances = null,
            builtUsing = null,
            section = null,
            priority = null,
            homepage = null,
            description = null,
            sha1 = null,
            sha256 = null,
            md5sum = null
        )
        val packageList = listOf(package1, package2)

        val package3 = DebianPackage(
            packageName = "foo",
            version = "1.0.3",
            architecture = "all",
            fileName = "foo_1.0.3_all.deb",
            size = "234",
            maintainer = null,
            installedSize = null,
            depends = null,
            conflicts = null,
            replaces = null,
            provides = null,
            preDepends = null,
            recommends = null,
            suggests = null,
            enhances = null,
            builtUsing = null,
            section = null,
            priority = null,
            homepage = null,
            description = null,
            sha1 = null,
            sha256 = null,
            md5sum = null
        )

        val combined = packageList.removeDebianPackage(package3)
        assert(combined.contains(package1))
        assert(combined.contains(package2))
        assert(!combined.contains(package3))
        assert(combined.size == 2)
    }

}
