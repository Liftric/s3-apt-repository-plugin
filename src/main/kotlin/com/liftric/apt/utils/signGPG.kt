package com.liftric.apt.utils

import org.bouncycastle.bcpg.ArmoredOutputStream
import org.bouncycastle.jce.provider.BouncyCastleProvider
import org.bouncycastle.openpgp.PGPPrivateKey
import org.bouncycastle.openpgp.PGPSecretKey
import org.bouncycastle.openpgp.PGPSecretKeyRing
import org.bouncycastle.openpgp.PGPSignature
import org.bouncycastle.openpgp.PGPSignatureGenerator
import org.bouncycastle.openpgp.PGPUtil
import org.bouncycastle.openpgp.jcajce.JcaPGPObjectFactory
import org.bouncycastle.openpgp.operator.PBESecretKeyDecryptor
import org.bouncycastle.openpgp.operator.jcajce.JcaPGPContentSignerBuilder
import org.bouncycastle.openpgp.operator.jcajce.JcePBESecretKeyDecryptorBuilder
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.security.Security

/**
 * Signs the Release file of an Apt Repository using a given private key.
 * This function first ensures the Bouncy Castle security provider is added, then loads the private key,
 * and sets up a signature generator with the proper signing algorithm.
 * It reads the Release file into a byte array, which is then used to update the signature generator.
 * After generating the signature, it writes the signature into a temporary file which will be deleted when the program ends.
 *
 * @param privateKeyFile File object representing the private key file
 * @param passphrase CharArray object representing the passphrase to the private key file
 * @param releaseFile File object representing the Release file to be signed
 * @return File object representing the signed Release file
 */


fun signReleaseFile(privateKeyFile: File, passphrase: CharArray, releaseFile: File): File {
    if (Security.getProvider("BC") == null) {
        Security.addProvider(BouncyCastleProvider());
    }

    val privateKey = loadPrivateKey(privateKeyFile, passphrase)

    val sigGen = PGPSignatureGenerator(JcaPGPContentSignerBuilder(privateKey.publicKeyPacket.algorithm, PGPUtil.SHA512))
    sigGen.init(PGPSignature.BINARY_DOCUMENT, privateKey)

    val fileBytes = releaseFile.readBytes()
    sigGen.update(fileBytes)

    val signedFile = File.createTempFile("Release", ".gpg").apply {
        deleteOnExit()
    }

    val fileOutputStream = FileOutputStream(signedFile)
    val armoredOutputStream = ArmoredOutputStream(fileOutputStream)

    sigGen.generate().encode(armoredOutputStream)

    armoredOutputStream.close()
    fileOutputStream.close()

    return signedFile
}

fun loadPrivateKey(privateKeyFile: File, passphrase: CharArray): PGPPrivateKey {
    val keyIn = PGPUtil.getDecoderStream(FileInputStream(privateKeyFile))
    val pgpF = JcaPGPObjectFactory(keyIn)
    val pgpSec = pgpF.nextObject() as PGPSecretKeyRing

    var secretKey: PGPSecretKey? = null
    val keys = pgpSec.secretKeys
    while (secretKey == null && keys.hasNext()) {
        val key = keys.next()
        if (key.isSigningKey) {
            secretKey = key
        }
    }

    if (secretKey == null) {
        throw IllegalArgumentException("Can't find signing key in key ring.")
    }

    val decryptor: PBESecretKeyDecryptor =
        JcePBESecretKeyDecryptorBuilder().setProvider(BouncyCastleProvider()).build(passphrase)
    return secretKey.extractPrivateKey(decryptor)
}
