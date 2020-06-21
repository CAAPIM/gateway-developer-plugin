/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayconfig.config.loader;

import com.ca.apim.gateway.cagatewayconfig.beans.Bundle;
import com.ca.apim.gateway.cagatewayconfig.util.file.FileUtils;
import com.ca.apim.gateway.cagatewayconfig.util.file.SupplierWithIO;
import org.apache.commons.io.IOUtils;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

import static com.ca.apim.gateway.cagatewayconfig.util.file.FileUtils.findConfigFileOrDir;
import static java.util.Arrays.stream;
import static org.apache.commons.io.FilenameUtils.getBaseName;

/**
 * Loads the certificates specified in the 'certificates' directory. Certificates in this directory must end with
 * .der, .pem, .crt, or .cer.
 */
@Singleton
public class CertificatesLoader implements EntityLoader {

    private FileUtils fileUtils;

    @Inject
    CertificatesLoader(FileUtils fileUtils) {
        this.fileUtils = fileUtils;
    }

    @Override
    public void load(Bundle bundle, File rootDir) {
        final File certificatesDir = findConfigFileOrDir(rootDir, "certificates");
        if (certificatesDir != null && certificatesDir.exists()) {
            final String[] certs = certificatesDir.list();
            if (certs != null && certs.length > 0) {
                final Map<String, SupplierWithIO<InputStream>> map = new HashMap<>();
                stream(certs).forEach(cert -> {
                    if (checkCertFormat(cert)) {
                        map.put(getBaseName(cert), () -> new FileInputStream(new File(certificatesDir, cert)));
                    } else {
                        throw new ConfigLoadException(cert + " must be a valid certificate extension.");
                    }
                });
                bundle.putAllCertificateFiles(map);
            }
        }
    }

    @Override
    public void load(Bundle bundle, String name, String value) {
        if (checkCertFormat(name)) {
            bundle.getCertificateFiles().put(name.substring(0, name.length() - 4), () -> IOUtils.toInputStream(value, Charset.defaultCharset()));
        } else {
            throw new ConfigLoadException(name + " must have a valid certificate extension.");
        }
    }

    @Override
    public Object loadSingle(String name, File entitiesFile) {
        if (!checkCertFormat(entitiesFile.getName())) {
            throw new ConfigLoadException(name + " must have a valid certificate extension.");
        }
        return fileUtils.getFileAsString(entitiesFile);
    }

    @Override
    public Map<String, Object> load(File entitiesFile) {
        throw new ConfigLoadException("Cannot load certificates from config file");
    }

    private boolean checkCertFormat(String certFileName) {
        final String lowerCaseName = certFileName.toLowerCase();
        return lowerCaseName.endsWith(".der") || lowerCaseName.endsWith(".pem") || lowerCaseName.endsWith(".crt") || lowerCaseName.endsWith(".cer");
    }

    @Override
    public String getEntityType() {
        return "CERTIFICATE_FILE";
    }
}
