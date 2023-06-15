package com.liftric.apt.service

import com.liftric.apt.*
import com.liftric.apt.model.*
import com.liftric.apt.utils.parseToMap
import java.io.File

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
