package com.liftric.apt

import com.liftric.apt.model.ControlInfo
import com.liftric.apt.service.PackagesInfoFactory
import com.liftric.apt.service.parseControlInfo
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.io.File

class PackagesInfoFactoryTest {

    @Test
    fun `test packages info extraction`() {
        val testDebFile = File("src/test/resources/foobar_1.0.0-1_all.deb")
        val factory = PackagesInfoFactory(testDebFile)
        val packagesInfo = factory.packagesInfo

        assertEquals("foobar", packagesInfo.packageInfo)
        assertEquals("1.0.0-1", packagesInfo.version)
        assertEquals("all", packagesInfo.architecture)
    }

    @Test
    fun curl() {
        val packageInfo = """Package: curl
Version: 7.35.0-1ubuntu2.20
Architecture: amd64
Maintainer: Ubuntu Developers <ubuntu-devel-discuss@lists.ubuntu.com>
Installed-Size: 309
Depends: libc6 (>= 2.17), libcurl3 (= 7.35.0-1ubuntu2.20), zlib1g (>= 1:1.1.4)
Section: web
Priority: optional
Multi-Arch: foreign
Homepage: http://curl.haxx.se
Description: command line tool for transferring data with URL syntax 
 curl is a command line tool for transferring data with URL syntax, supporting
 DICT, FILE, FTP, FTPS, GOPHER, HTTP, HTTPS, IMAP, IMAPS, LDAP, LDAPS, POP3,
 POP3S, RTMP, RTSP, SCP, SFTP, SMTP, SMTPS, TELNET and TFTP.
 .
 curl supports SSL certificates, HTTP POST, HTTP PUT, FTP uploading, HTTP form
 based upload, proxies, cookies, user+password authentication (Basic, Digest,
 NTLM, Negotiate, kerberos...), file transfer resume, proxy tunneling and a
 busload of other useful tricks.
Original-Maintainer: Alessandro Ghedini <ghedo@debian.org>"""
        val controlInfo = ControlInfo().also {
            it.parseControlInfo(packageInfo)
        }
        val map = mapOf<String, String>()
        ControlInfo(
            section = map["Section"]
        )
        // replace multiline with marker so they can be restored after parsing
        val removeMultilines = packageInfo
            .replace(Regex("\n\\s+"), "###!###")
        println(removeMultilines)

        // parse file and restore multilines
        val map1 = removeMultilines.split("\n").associate {
            val strings = it.split(": ")
            strings[0].trim() to
                    strings.getOrNull(1)
                        ?.trim()
                        ?.replace("###!###", "\n")
        }

        map1.forEach {
            println("k=${it.key} v=${it.value}")
        }
        ControlInfo(
            packageInfo = map1["Package"],
            version = map1["Version"] ?: error("Version in Control Info missing but required"),
        ).also { println(it) }
        println(controlInfo)
    }
}

//Description: command line tool for transferring data with URL syntax curl is a command line tool for transferring data with URL syntax, supportingDICT, FILE, FTP, FTPS, GOPHER, HTTP, HTTPS, IMAP, IMAPS, LDAP, LDAPS, POP3,POP3S, RTMP, RTSP, SCP, SFTP, SMTP, SMTPS, TELNET and TFTP..curl supports SSL certificates, HTTP POST, HTTP PUT, FTP uploading, HTTP formbased upload, proxies, cookies, user+password authentication (Basic, Digest,NTLM, Negotiate, kerberos...), file transfer resume, proxy tunneling and abusload of other useful tricks.
