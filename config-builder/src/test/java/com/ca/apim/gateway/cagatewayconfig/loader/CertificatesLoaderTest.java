/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayconfig.loader;

import com.ca.apim.gateway.cagatewayconfig.beans.Bundle;
import io.github.glytching.junit.extension.folder.TemporaryFolder;
import io.github.glytching.junit.extension.folder.TemporaryFolderExtension;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.Extensions;
import org.mockito.junit.jupiter.*;
import org.testcontainers.shaded.com.google.common.io.Files;

import java.io.File;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

@Extensions({@ExtendWith(MockitoExtension.class), @ExtendWith(TemporaryFolderExtension.class)})
class CertificatesLoaderTest {

    private TemporaryFolder rootProjectDir;
    private File certsDir;

    @BeforeEach
    void setUp(final TemporaryFolder temporaryFolder) {
        rootProjectDir = temporaryFolder;

        certsDir = new File(temporaryFolder.getRoot(), "config/certificates");
        certsDir.mkdirs();
    }

    @Test
    void load() throws IOException {
        createCertificates("cert1.cer", "cert2.cer", "cert3.cer");

        CertificatesLoader loader = new CertificatesLoader();
        Bundle bundle = new Bundle();
        loader.load(bundle, rootProjectDir.getRoot());

        assertFalse(bundle.getCertificateFiles().isEmpty());
        assertEquals(3, bundle.getCertificateFiles().size());
        assertNotNull(bundle.getCertificateFiles().get("cert1"));
        assertNotNull(bundle.getCertificateFiles().get("cert2"));
        assertNotNull(bundle.getCertificateFiles().get("cert3"));
    }

    @Test
    void loadNoCerts() {
        CertificatesLoader loader = new CertificatesLoader();
        Bundle bundle = new Bundle();
        loader.load(bundle, rootProjectDir.getRoot());

        assertTrue(bundle.getCertificateFiles().isEmpty());
    }

    @Test
    void loadInvalidCert() throws IOException {
        createCertificates("cert1.cert");

        CertificatesLoader loader = new CertificatesLoader();
        Bundle bundle = new Bundle();
        assertThrows(BundleLoadException.class, () -> loader.load(bundle, rootProjectDir.getRoot()));
    }

    @Test
    void loadFromEnvironment() {
        CertificatesLoader loader = new CertificatesLoader();
        Bundle bundle = new Bundle();
        loader.load(bundle, "cert1.cer", "CERTIFICATE");

        assertFalse(bundle.getCertificateFiles().isEmpty());
        assertEquals(1, bundle.getCertificateFiles().size());
        assertNotNull(bundle.getCertificateFiles().get("cert1"));
    }

    @Test
    void loadFromEnvironmentInvalidName() {
        CertificatesLoader loader = new CertificatesLoader();
        Bundle bundle = new Bundle();
        assertThrows(BundleLoadException.class, () -> loader.load(bundle, "cert1.cert", "CERTIFICATE"));
    }

    private void createCertificates(String... certs) throws IOException {
        for (String c : certs) {
            Files.touch(new File(certsDir, c));
        }
    }
}