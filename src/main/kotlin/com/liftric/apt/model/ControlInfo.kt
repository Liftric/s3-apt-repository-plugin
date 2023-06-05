package com.liftric.apt.model

/**
 * The ControlInfo data class represents the control information extracted from a Debian package file.
 * Each Debian package file is a compressed archive that contains a control file. This control file holds
 * crucial metadata about the package, such as its name, version, architecture, maintainer, and various
 * other properties.
 *
 * Each instance of this class corresponds to the control information of one Debian package. The properties
 * of this class mirror the fields found in the package's control file.
 *
 * After the control information of a Debian package is extracted and stored in a ControlInfo instance,
 * this information can be used to generate a PackagesInfo instance, suitable for inclusion in an apt repository.
 */

data class ControlInfo(
    var packageInfo: String? = null,
    var version: String? = null,
    var source: String? = null,
    var architecture: String? = null,
    var maintainer: String? = null,
    var installedSize: String? = null,
    var depends: String? = null,
    var preDepends: String? = null,
    var recommends: String? = null,
    var suggests: String? = null,
    var conflicts: String? = null,
    var replaces: String? = null,
    var enhances: String? = null,
    var breaks: String? = null,
    var builtUsing: String? = null,
    var multiArch: String? = null,
    var provides: String? = null,
    var filename: String? = null,
    var size: Long? = null,
    var md5sum: String? = null,
    var sha1: String? = null,
    var sha256: String? = null,
    var section: String? = null,
    var priority: String? = null,
    var homepage: String? = null,
    var description: String? = null
)
