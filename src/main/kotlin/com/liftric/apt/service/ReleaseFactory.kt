package com.liftric.apt.service

import com.liftric.apt.*
import com.liftric.apt.model.*
import com.liftric.apt.utils.parseToMap
import java.io.File

/**
 * The ReleaseFactory object is primarily used for reading Release files from an APT repository.
 *
 * The Release files contain important metadata about the repository, such as its origin, label, suite,
 * components, architectures, codename, and other details. These details are crucial for maintaining
 * and managing the repository.
 *
 * This object provides a function `parseReleaseFile` which takes a File object as an argument, which
 * should be the Release file from the APT repository. It parses the file and returns a ReleaseInfo object
 * which holds all the extracted information. The function uses key-value pairs in the Release file to fill
 * the properties of the ReleaseInfo object. If any information is missing in the Release file, it will use
 * default values.
 *
 * The resulting ReleaseInfo object can be used to facilitate various operations on the APT repository,
 * such as creating a new Release file and populating it with data from the old one.
 *
 * Note: This implementation assumes that the Release file is correctly formatted and can be successfully
 * parsed into a map using the `parseToMap` function. Errors or inconsistencies in the Release file may
 * lead to incorrect parsing and consequently incorrect ReleaseInfo data.
 */

object ReleaseFactory {
    fun parseReleaseFile(
        file: File,
    ): ReleaseInfo {
        val releaseInfo = parseToMap(file.readText())
        return ReleaseInfo(
            origin = releaseInfo["Origin"] ?: DEFAULT_ORIGIN,
            label = releaseInfo["Label"] ?: DEFAULT_LABEL,
            suite = releaseInfo["Suite"] ?: DEFAULT_SUITE,
            components = releaseInfo["Components"] ?: DEFAULT_COMPONENT,
            architectures = releaseInfo["Architectures"],
            codename = releaseInfo["Codename"],
            date = releaseInfo["Date"],
            description = releaseInfo["Description"],
            version = releaseInfo["Version"],
            validUntil = releaseInfo["Valid-Until"],
            notAutomatic = releaseInfo["NotAutomatic"],
            butAutomaticUpgrades = releaseInfo["ButAutomaticUpgrades"],
            acquireByHash = releaseInfo["Acquire-By-Hash"],
            changelogs = releaseInfo["Changelogs"],
            snapshots = releaseInfo["Snapshots"],
            md5Sum = listOf(),
            sha1 = listOf(),
            sha256 = listOf(),
            sha512 = listOf(),
        )
    }
}
