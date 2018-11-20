/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayconfig.util.gateway;

import io.github.glytching.junit.extension.folder.TemporaryFolder;
import io.github.glytching.junit.extension.folder.TemporaryFolderExtension;
import org.apache.commons.io.FileUtils;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.Security;
import java.security.cert.CertificateFactory;

import static com.ca.apim.gateway.cagatewayconfig.util.gateway.CertificateUtils.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(TemporaryFolderExtension.class)
class CertificateUtilsTest {

    private TemporaryFolder rootProjectDir;

    @BeforeAll
    static void beforeAll() {
        Security.insertProviderAt(new BouncyCastleProvider(), 1);
    }

    @BeforeEach
    void setUp(final TemporaryFolder temporaryFolder) {
        rootProjectDir = temporaryFolder;
    }

    @Test
    void buildCertDataFromMissingFile() {
        assertThrows(
                CertificateUtilsException.class,
                () -> buildCertDataFromFile(
                        () -> Files.newInputStream(Paths.get(rootProjectDir.getRoot().toPath().toString(), "cert.cert")),
                        DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument(),
                        CertificateFactory.getInstance("X.509")
                )
        );
    }

    @Test
    void buildCertDataFromInvalidCert() {
        assertThrows(
                CertificateUtilsException.class,
                () -> buildCertDataFromFile(
                        () -> new ByteArrayInputStream("certificate".getBytes()),
                        DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument(),
                        CertificateFactory.getInstance("X.509")
                )
        );
    }

    @Test
    void prepareCertificateData() {
        final byte[] certificateData = CertificateUtils.prepareCertificateData("ENCODED");

        assertEquals(PEM_CERT_BEGIN_MARKER +
                LINE_SEPARATOR +
                "ENCODED" +
                LINE_SEPARATOR +
                PEM_CERT_END_MARKER,
                new String(certificateData)
        );
    }

    @Test
    void buildCertificateFileName() {
        assertEquals("cert" + PEM_CERT_FILE_EXTENSION, CertificateUtils.buildCertificateFileName("cert"));
    }

    @Test
    void writeCertificateData() throws IOException {
        CertificateUtils.writeCertificateData(rootProjectDir.getRoot(), "cert", "cert");

        File certFile = new File(rootProjectDir.getRoot(), "cert" + PEM_CERT_FILE_EXTENSION);
        assertTrue(certFile.exists());
        assertEquals(PEM_CERT_BEGIN_MARKER +
                LINE_SEPARATOR +
                "cert" +
                LINE_SEPARATOR +
                PEM_CERT_END_MARKER, FileUtils.readFileToString(certFile, Charset.defaultCharset()));
    }
}