# Gradle S3 Apt Plugin 

This Gradle plugin enables you to manage an Apt Repository in an Amazon S3 bucket. It's designed to assist with the task of handling Debian packages in an S3 environment. The plugin comes with three key tasks: `uploadPackage`, `removePackage`, and `cleanPackages`. 

- The `uploadPackage` task allows for the upload of new Debian packages into the S3 Apt repository, including the necessary updating of the Release and Packages files. If no existing repository is found, a new one will be created.
- The `removePackage` task is designed for the removal of specific versions of packages from the Packages file of a repository in an S3 bucket, with automatic updating of the Release file. The actual Debian file, however, will not be removed from the pool.
- The `cleanPackages` task serves the purpose of deleting all Debian files that are no longer listed in the Packages file.

> **Recommendation**: To create Debian packages from a Gradle project, there is this Gradle plugin https://github.com/nebula-plugins/gradle-ospackage-plugin

> **Caution**: This plugin is still in the early stages of development and has not yet been extensively tested in a production environment. It is strongly recommended to backup your S3 bucket before using this plugin in a production setting to prevent any potential data loss.

## Example

```kotlin
import com.liftric.apt.extensions.*

plugins {
    id("com.liftric.s3-apt-repository-plugin")
}

s3AptRepository {
    bucket.set("s3-apt-repo")
    region.set("eu-central-1")
    accessKey.set(System.getenv("AWS_ACCESS_KEY"))
    secretKey.set(System.getenv("AWS_SECRET_KEY"))
    signingKeyPassphrase.set(System.getenv("PGP_PASSPHRASE"))
    signingKeyRingFile.set(file("private.key"))
    debPackage {
        file.set(file("foobar_1.0.0-1_all.deb"))
        packageArchitectures.set(setOf("all", "amd64"))
    }
}
```

### uploadPackage

The `uploadPackage` task comes equipped with a range of both required and optional attributes.

- **bucket**: *(Required)* The name of the S3 bucket where the Apt Repository resides.
- **region**: *(Required)* The AWS region of the S3 bucket.
- **accessKey**: *(Required)* The AWS Access Key for accessing the S3 bucket. Can be overridden in the `debPackage` section.
- **secretKey**: *(Required)* The AWS Secret Key for accessing the S3 bucket. Can be overridden in the `debPackage` section.
- **signingKeyRingFile**: *(Required)* The PGP private key file used for signing the Release files.
- **signingKeyPassphrase**: *(Required)* The passphrase for the PGP private key.
- **bucketPath**: *(Optional)* The path within the bucket to store the Apt Repository. If not specified, the repository is stored at the root of the bucket.
- **endpoint**: *(Optional)* Custom S3 endpoint. Use this to override the default AWS S3 endpoint.
- **override**: *(Optional)* Boolean value indicating whether to override existing version of a Package. By default, it is `true`.
 
`Release File Attributes` can be override in s3AptRepository or per debPackage

- **origin**: *(Optional)* The value of the 'Origin' field in the Release files. By default, it is 'Debian'.
- **label**: *(Optional)* The value of the 'Label' field in the Release files. By default, it is 'Debian'.
- **suite**: *(Optional)* The value of the 'Suite' field in the Release files. By default, it is 'stable'.
- **component**: *(Optional)* The value of the 'Component' field in the Release files. By default, it is 'main'.
- **architectures**: *(Optional)* The value of the 'Architectures' field in the Release files. By default, it is set correctly by the Plugin. If you need to override it, you can do so here.
- **codename**: *(Optional)* The value of the 'Codename' field in the Release files. By default, it is not set.
- **date**: *(Optional)* The value of the 'Date' field in the Release files. By default, it is set to the current date.
- **releaseDescription**: *(Optional)* The value of the 'Description' field in the Release files. By default, it is not set.
- **releaseVersion**: *(Optional)* The value of the 'Version' field in the Release files. By default, it is not set.
- **validUntil**: *(Optional)* The value of the 'Valid-Until' field in the Release files. By default, it is not set.
- **notAutomatic**: *(Optional)* The value of the 'NotAutomatic' field in the Release files. By default, it is not set.
- **butAutomaticUpgrades**: *(Optional)* The value of the 'ButAutomaticUpgrades' field in the Release files. By default, it is not set.
- **changelogs**: *(Optional)* The value of the 'Changelogs' field in the Release files. By default, it is not set.
- **snapshots**: *(Optional)* The value of the 'Snapshots' field in the Release files. By default, it is not set.


- **debPackage**: *(Required)* See below for more information.
 
In the `debPackage` section, you can specify package-specific attributes. These can override the top-level attributes if needed:

- **file**: *(Required)* The Debian package file to upload.
- **packageArchitectures**: *(Required)* Set of architectures that the package supports.
- **packageName**, **packageVersion**: *(Optional)* These attributes can be used to override the default package name and version extracted form the Debian File.
- **origin**, **label**, **suite**, **component**, **architectures**, **codename**, **date**, **releaseDescription**, **releaseVersion**, **validUntil**, **notAutomatic**, **butAutomaticUpgrades**, **changelogs**, **snapshots** : *(Optional)* These attributes can be used to override the default Release file fields for the specific package.
- **accessKey**, **secretKey**, **bucket**, **bucketPath**, **region**, **endpoint**: *(Optional)* These attributes can be used to override their respective top-level attributes for the specific package.



 

### removePackage

The `removePackage` task comes equipped with a range of both required and optional attributes.

- **bucket**: *(Required)* The name of the S3 bucket where the Apt Repository resides.
- **region**: *(Required)* The AWS region of the S3 bucket.
- **accessKey**: *(Required)* The AWS Access Key for accessing the S3 bucket. Can be overridden in the `debPackage` section.
- **secretKey**: *(Required)* The AWS Secret Key for accessing the S3 bucket. Can be overridden in the `debPackage` section.
- **signingKeyRingFile**: *(Required)* The PGP private key file used for signing the Release files.
- **signingKeyPassphrase**: *(Required)* The passphrase for the PGP private key.
- **bucketPath**: *(Optional)* The path within the bucket to store the Apt Repository. If not specified, the repository is stored at the root of the bucket.
- **endpoint**: *(Optional)* Custom S3 endpoint. Use this to override the default AWS S3 endpoint.
- **origin**: *(Optional)* The value of the 'Origin' field in the Release files. By default, it is 'Debian'.

`Release File Attributes` can be override in s3AptRepository or per debPackage

- **label**: *(Optional)* The value of the 'Label' field in the Release files. By default, it is 'Debian'.
- **suite**: *(Optional)* The value of the 'Suite' field in the Release files. By default, it is 'stable'.
- **component**: *(Optional)* The value of the 'Component' field in the Release files. By default, it is 'main'.
- **architectures**: *(Optional)* The value of the 'Architectures' field in the Release files. By default, it is set correctly by the Plugin. If you need to override it, you can do so here.
- **codename**: *(Optional)* The value of the 'Codename' field in the Release files. By default, it is not set.
- **date**: *(Optional)* The value of the 'Date' field in the Release files. By default, it is set to the current date.
- **releaseDescription**: *(Optional)* The value of the 'Description' field in the Release files. By default, it is not set.
- **releaseVersion**: *(Optional)* The value of the 'Version' field in the Release files. By default, it is not set.
- **validUntil**: *(Optional)* The value of the 'Valid-Until' field in the Release files. By default, it is not set.
- **notAutomatic**: *(Optional)* The value of the 'NotAutomatic' field in the Release files. By default, it is not set.
- **butAutomaticUpgrades**: *(Optional)* The value of the 'ButAutomaticUpgrades' field in the Release files. By default, it is not set.
- **changelogs**: *(Optional)* The value of the 'Changelogs' field in the Release files. By default, it is not set.
- **snapshots**: *(Optional)* The value of the 'Snapshots' field in the Release files. By default, it is not set.

- **debPackage**: *(Required)* See below for more information.

In the `debPackage` section, you can specify package-specific attributes. These can override the top-level attributes if needed:

- **file**: *(Required)* The Debian package file to extract Version and Package Name.
- **packageArchitectures**: *(Required)* Set of architectures that the package supports.
- **packageName**, **packageVersion**: *(Optional)* These attributes can be used to override the default package name and version extracted form the Debian File.
- **origin**, **label**, **suite**, **component**, **architectures**, **codename**, **date**, **releaseDescription**, **releaseVersion**, **validUntil**, **notAutomatic**, **butAutomaticUpgrades**, **changelogs**, **snapshots** : *(Optional)* These attributes can be used to override the default Release file fields for the specific package.
- **accessKey**, **secretKey**, **bucket**, **bucketPath**, **region**, **endpoint**: *(Optional)* These attributes can be used to override their respective top-level attributes for the specific package.


### cleanPackages

The `cleanPackages` task comes equipped with a range of both required and optional attributes.

- **bucket**: *(Required)* The name of the S3 bucket where the Apt Repository resides.
- **region**: *(Required)* The AWS region of the S3 bucket.
- **accessKey**: *(Required)* The AWS Access Key for accessing the S3 bucket. Can be overridden in the `debPackage` section.
- **secretKey**: *(Required)* The AWS Secret Key for accessing the S3 bucket. Can be overridden in the `debPackage` section.
- **bucketPath**: *(Optional)* The path within the bucket to store the Apt Repository. If not specified, the repository is stored at the root of the bucket.
- **endpoint**: *(Optional)* Custom S3 endpoint. Use this to override the default AWS S3 endpoint.
- **suite**: *(Optional)* The value of the 'Suite' field in the Release files. By default, it is 'stable'.
- **component**: *(Optional)* The value of the 'Component' field in the Release files. By default, it is 'main'.

## License

This S3 Apt Repository Plugin is released under MIT License.
