/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayconfig.tasks.zip.loader;

import com.ca.apim.gateway.cagatewayconfig.tasks.zip.beans.Bundle;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Loads the certificates specified in the 'config/certificates' directory. Certificates in this directory must end with
 * .der, .pem, .crt, or .cer.
 */
public class CertificatesLoader implements EntityLoader {
    @Override
    public void load(Bundle bundle, File rootDir) {
        final File certificatesDir = new File(rootDir, "config/certificates");
        if (certificatesDir.exists()) {
            final String[] certs = certificatesDir.list();
            if (certs != null && certs.length > 0) {
                final Map<String, File> map = new HashMap<>();
                Arrays.stream(certs).forEach(cert -> {
                    if (checkCertFormat(cert)) {
                        map.put(cert.substring(0, cert.length()-4), new File(certificatesDir, cert));
                    } else {
                        throw new BundleLoadException(cert + " must be a valid certificate extension.");
                    }
                });
                bundle.putAllCertificateFiles(map);
            }
        }
    }

    private boolean checkCertFormat(String certFileName) {
        final String lowerCaseName = certFileName.toLowerCase();
        return lowerCaseName.endsWith(".der") || lowerCaseName.endsWith(".pem") || lowerCaseName.endsWith(".crt") || lowerCaseName.endsWith(".cer");
    }
}
