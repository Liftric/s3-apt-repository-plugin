package com.liftric.apt.service

import com.liftric.apt.utils.FileHashUtil.md5Hash
import com.liftric.apt.utils.FileHashUtil.sha1Hash
import com.liftric.apt.utils.FileHashUtil.sha256Hash
import com.liftric.apt.model.DebianPackage
import org.apache.commons.compress.archivers.ar.ArArchiveEntry
import org.apache.commons.compress.archivers.ar.ArArchiveInputStream
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream
import org.apache.commons.compress.compressors.CompressorStreamFactory
import java.io.BufferedInputStream
import java.io.File
import java.io.FileInputStream

object PackagesFactory {

    fun parseDebianFile(
        file: File,
        archs: Set<String>,
        poolBucketKey: String,
        packageName: String?,
        packageVersion: String?,
    ): List<DebianPackage> {
        val controlInfo = readDebFile(file)
        return parseControlInfo(file, controlInfo, archs, poolBucketKey, packageName, packageVersion)
    }

    fun parsePackagesFile(file: File): List<DebianPackage> {
        val packagesText = file.readText()
        val packages = packagesText.split("\n\n").filter { it.isNotBlank() }

        return packages.map {
            val parsedMap = parseToMap(it)
            DebianPackage(
                packageName = parsedMap["Package"] ?: error("Invalid Packages file, PackageName missing"),
                version = parsedMap["Version"] ?: error("Invalid Packages file, Version missing"),
                architecture = parsedMap["Architecture"] ?: error("Invalid Packages file, Architecture missing"),
                fileName = parsedMap["Filename"] ?: error("Invalid Packages file, Filename missing"),
                size = parsedMap["Size"] ?: error("Invalid Packages file, Size missing"),
                sha1 = parsedMap["SHA1"],
                sha256 = parsedMap["SHA256"],
                md5sum = parsedMap["MD5sum"],
                maintainer = parsedMap["Maintainer"],
                installedSize = parsedMap["Installed-Size"],
                depends = parsedMap["Depends"],
                conflicts = parsedMap["Conflicts"],
                replaces = parsedMap["Replaces"],
                provides = parsedMap["Provides"],
                preDepends = parsedMap["Pre-Depends"],
                recommends = parsedMap["Recommends"],
                suggests = parsedMap["Suggests"],
                enhances = parsedMap["Enhances"],
                builtUsing = parsedMap["Built-Using"],
                section = parsedMap["Section"],
                priority = parsedMap["Priority"],
                homepage = parsedMap["Homepage"],
                description = parsedMap["Description"],
            )
        }
    }

    private fun parseToMap(text: String): Map<String, String?> {
        val removeMultiLines = text
            .replace(Regex("\n\\s+"), " ")

        val parsedMap = removeMultiLines.split("\n").associate {
            val strings = it.split(": ")
            strings[0].trim() to
                    strings.getOrNull(1)
                        ?.trim()
        }
        return parsedMap
    }


    private fun parseControlInfo(
        file: File,
        controlInfo: String,
        archs: Set<String>,
        fileName: String,
        packageName: String?,
        packageVersion: String?,
    ): List<DebianPackage> {
        val controlMap = parseToMap(controlInfo)
        return archs.map { arch ->
            DebianPackage(
                packageName = packageName ?: controlMap["Package"]
                ?: error("Package Name in Control Info missing but required"),
                version = packageVersion ?: controlMap["Version"]
                ?: error("Version in Control Info missing but required"),
                architecture = arch,
                fileName = fileName,
                size = file.length().toString(),
                sha1 = file.sha1Hash(),
                sha256 = file.sha256Hash(),
                md5sum = file.md5Hash(),
                maintainer = controlMap["Maintainer"],
                installedSize = controlMap["Installed-Size"],
                depends = controlMap["Depends"],
                conflicts = controlMap["Conflicts"],
                replaces = controlMap["Replaces"],
                provides = controlMap["Provides"],
                preDepends = controlMap["Pre-Depends"],
                recommends = controlMap["Recommends"],
                suggests = controlMap["Suggests"],
                enhances = controlMap["Enhances"],
                builtUsing = controlMap["Built-Using"],
                section = controlMap["Section"],
                priority = controlMap["Priority"],
                homepage = controlMap["Homepage"],
                description = controlMap["Description"],
            )
        }
    }

    private fun readDebFile(file: File): String {
        val arInput = ArArchiveInputStream(FileInputStream(file))
        var entry: ArArchiveEntry? = arInput.nextArEntry
        while (entry != null) {
            if (entry.name.startsWith("control.tar")) {
                val byteArray = arInput.readBytes()
                return processTarBytes(byteArray)
            }
            entry = arInput.nextArEntry
        }
        arInput.close()
        throw Exception("control.tar not found")
    }

    private fun processTarBytes(tarBytes: ByteArray): String {
        val compressorInput =
            CompressorStreamFactory().createCompressorInputStream(BufferedInputStream(tarBytes.inputStream()))
        val tarInput = TarArchiveInputStream(compressorInput)
        var tarEntry = tarInput.nextTarEntry
        while (tarEntry != null) {
            if (tarEntry.name == "./control") {
                return String(tarInput.readBytes())
            }
            tarEntry = tarInput.nextTarEntry
        }
        tarInput.close()
        throw Exception("control not found")
    }
}
