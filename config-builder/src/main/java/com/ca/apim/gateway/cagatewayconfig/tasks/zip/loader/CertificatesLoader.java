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

public class CertificatesLoader implements EntityLoader {
    @Override
    public void load(Bundle bundle, File rootDir) {
        final File certificatesDir = new File(rootDir, "config/certificates");
        if (certificatesDir.exists()) {
            final String[] certs = certificatesDir.list();
            if (certs != null && certs.length > 0) {
                Map<String, String> map = new HashMap<>();
                Arrays.stream(certs).forEach(cert -> {
                    if (checkCertFormat(cert)) {
                        map.put(cert, certificatesDir.getPath() + "/" + cert);
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
