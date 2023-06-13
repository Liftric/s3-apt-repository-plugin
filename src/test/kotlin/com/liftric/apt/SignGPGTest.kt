package com.liftric.apt

import com.liftric.apt.utils.signReleaseFile
import org.bouncycastle.jce.provider.BouncyCastleProvider
import org.bouncycastle.openpgp.PGPSignatureList
import org.bouncycastle.openpgp.PGPUtil
import org.bouncycastle.openpgp.jcajce.JcaPGPObjectFactory
import org.bouncycastle.openpgp.jcajce.JcaPGPPublicKeyRingCollection
import org.bouncycastle.openpgp.operator.jcajce.JcaPGPContentVerifierBuilderProvider
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.io.File
import java.io.FileInputStream

class ReleaseFileSigningTest {

    @Test
    fun `release file signing test`() {
        val privateKeyFile = File("src/test/resources/private.key")
        val publicKeyFile = File("src/test/resources/public.key")
        val releaseFile = File("src/test/resources/Release")
        val passphrase = "abcd1234".toCharArray()
        val signedFile = signReleaseFile(privateKeyFile, passphrase, releaseFile)

        assertTrue(signedFile.exists(), "Signed file does not exist")
        assertTrue(signedFile.length() > 0, "Signed file is empty")

        val isValid = verifySignedFile(signedFile, publicKeyFile, releaseFile)
        assertTrue(isValid, "Signature is not valid")
    }

    @Test
    fun `check wrong public key`() {
        val privateKeyFile = File("src/test/resources/private.key")
        val fakePublicKeyFile = File("src/test/resources/fake_public.key")
        val releaseFile = File("src/test/resources/Release")
        val passphrase = "abcd1234".toCharArray()
        val signedFile = signReleaseFile(privateKeyFile, passphrase, releaseFile)

        assertTrue(signedFile.exists(), "Signed file does not exist")
        assertTrue(signedFile.length() > 0, "Signed file is empty")

        val isValid = verifySignedFile(signedFile, fakePublicKeyFile, releaseFile)
        assertFalse(isValid, "Signature should not be valid")
    }

    //Just for Internal Use, to verify the result of the signing process
    private fun verifySignedFile(signedFile: File, publicKeyFile: File, releaseFile: File): Boolean {
        val pubIn = PGPUtil.getDecoderStream(FileInputStream(publicKeyFile))
        val pgpPubRingCollection = JcaPGPPublicKeyRingCollection(pubIn)
        val keyRing = pgpPubRingCollection.keyRings.next()
        val publicKey = keyRing.publicKey

        val pgpFact = JcaPGPObjectFactory(PGPUtil.getDecoderStream(FileInputStream(signedFile)))
        val signatureList = pgpFact.nextObject() as PGPSignatureList
        val signature = signatureList[0]

        signature.init(JcaPGPContentVerifierBuilderProvider().setProvider(BouncyCastleProvider()), publicKey)

        val fileBytes = releaseFile.readBytes()
        signature.update(fileBytes)

        return signature.verify()
    }
}
