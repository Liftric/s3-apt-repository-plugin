package com.liftric.apt.service

import org.apache.commons.compress.archivers.ar.ArArchiveEntry
import org.apache.commons.compress.archivers.ar.ArArchiveInputStream
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream
import org.apache.commons.compress.compressors.CompressorStreamFactory
import java.io.BufferedInputStream
import java.io.File
import java.io.FileInputStream
import com.liftric.apt.model.ControlInfo
import com.liftric.apt.model.PackagesInfo
import com.liftric.apt.utils.FileHashUtil.md5Hash
import com.liftric.apt.utils.FileHashUtil.sha1Hash
import com.liftric.apt.utils.FileHashUtil.sha256Hash
import com.liftric.apt.utils.FileHashUtil.size

/**
 * The PackagesInfoFactory class serves a vital role in the management of apt repositories.
 * It enables the extraction of critical package information from a given Debian file,
 * which is then used to integrate the file into an apt repository.
 *
 * A primary function of this class is to read the Debian file, extract control and package
 * information, and calculate file hashes. The extracted details, encapsulated in a PackagesInfo object,
 * form the basis for repository management operations.
 *
 * The class also determines the 'poolBucketKey', which denotes the relative file path of the Debian
 * file within the apt repository, adhering to the standard apt repository structure.
 */

class PackagesInfoFactory(private val file: File) {
    val packagesInfo = PackagesInfo()
    private val controlInfo = ControlInfo()

    init {
        readDebFile()
        setPackagesInfo()
    }

    private fun readDebFile() {
        val arInput = ArArchiveInputStream(FileInputStream(file))
        var entry: ArArchiveEntry? = arInput.nextArEntry
        while (entry != null) {
            if (entry.name.startsWith("control.tar")) {
                val byteArray = arInput.readBytes()
                processTarBytes(byteArray)
                break
            }
            entry = arInput.nextArEntry
        }
        arInput.close()
    }

    private fun processTarBytes(tarBytes: ByteArray) {
        val compressorInput =
            CompressorStreamFactory().createCompressorInputStream(BufferedInputStream(tarBytes.inputStream()))
        val tarInput = TarArchiveInputStream(compressorInput)
        var tarEntry = tarInput.nextTarEntry
        while (tarEntry != null) {
            if (tarEntry.name == "./control") {
                setControlInfo(tarInput)
            }
            tarEntry = tarInput.nextTarEntry
        }
        tarInput.close()
    }

    private fun setControlInfo(tarInput: TarArchiveInputStream) {
        val controlFileContent = String(tarInput.readBytes())
        val descriptionLines = mutableListOf<String>()
        var isDescription = false
        controlFileContent.lineSequence().forEach { line ->
            when {
                isDescription -> {
                    descriptionLines.add(line.trim())
                }

                line.startsWith("Package: ") -> controlInfo.packageInfo = line.removePrefix("Package: ")
                line.startsWith("Version: ") -> controlInfo.version = line.removePrefix("Version: ")
                line.startsWith("Source: ") -> controlInfo.source = line.removePrefix("Source: ")
                line.startsWith("Architecture: ") -> controlInfo.architecture = line.removePrefix("Architecture: ")
                line.startsWith("Maintainer: ") -> controlInfo.maintainer = line.removePrefix("Maintainer: ")
                line.startsWith("Installed-Size: ") -> controlInfo.installedSize = line.removePrefix("Installed-Size: ")
                line.startsWith("Depends: ") -> controlInfo.depends = line.removePrefix("Depends: ")
                line.startsWith("Pre-Depends: ") -> controlInfo.preDepends = line.removePrefix("Pre-Depends: ")
                line.startsWith("Recommends: ") -> controlInfo.recommends = line.removePrefix("Recommends: ")
                line.startsWith("Suggests: ") -> controlInfo.suggests = line.removePrefix("Suggests: ")
                line.startsWith("Conflicts: ") -> controlInfo.conflicts = line.removePrefix("Conflicts: ")
                line.startsWith("Replaces: ") -> controlInfo.replaces = line.removePrefix("Replaces: ")
                line.startsWith("Enhances: ") -> controlInfo.enhances = line.removePrefix("Enhances: ")
                line.startsWith("Breaks: ") -> controlInfo.breaks = line.removePrefix("Breaks: ")
                line.startsWith("Built-Using: ") -> controlInfo.builtUsing = line.removePrefix("Built-Using: ")
                line.startsWith("Multi-Arch: ") -> controlInfo.multiArch = line.removePrefix("Multi-Arch: ")
                line.startsWith("Provides: ") -> controlInfo.provides = line.removePrefix("Provides: ")
                line.startsWith("Section: ") -> controlInfo.section = line.removePrefix("Section: ")
                line.startsWith("Priority: ") -> controlInfo.priority = line.removePrefix("Priority: ")
                line.startsWith("Homepage: ") -> controlInfo.homepage = line.removePrefix("Homepage: ")
                line.startsWith("Description: ") -> {
                    isDescription = true
                    val descriptionLine = line.removePrefix("Description: ")
                    descriptionLines.add(descriptionLine.trim())
                }
            }
        }
        controlInfo.description = descriptionLines.joinToString(" ")
    }

    private fun setPackagesInfo() {
        packagesInfo.packageInfo = controlInfo.packageInfo
        packagesInfo.version = controlInfo.version
        packagesInfo.architecture = controlInfo.architecture
        packagesInfo.maintainer = controlInfo.maintainer
        packagesInfo.installedSize = controlInfo.installedSize
        packagesInfo.depends = controlInfo.depends
        packagesInfo.section = controlInfo.section
        packagesInfo.priority = controlInfo.priority
        packagesInfo.homepage = controlInfo.homepage
        packagesInfo.description = controlInfo.description
        packagesInfo.conflicts = controlInfo.conflicts
        packagesInfo.replaces = controlInfo.replaces
        packagesInfo.provides = controlInfo.provides
        packagesInfo.recommends = controlInfo.recommends
        packagesInfo.suggests = controlInfo.suggests
        packagesInfo.size = file.size()
        packagesInfo.sha1 = file.sha1Hash()
        packagesInfo.sha256 = file.sha256Hash()
        packagesInfo.md5sum = file.md5Hash()
    }
}
