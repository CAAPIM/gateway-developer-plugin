/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayconfig.config.loader;

import com.ca.apim.gateway.cagatewayconfig.beans.Bundle;
import com.ca.apim.gateway.cagatewayconfig.util.file.SupplierWithIO;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.FileUtils;

import javax.inject.Singleton;
import java.io.*;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static com.ca.apim.gateway.cagatewayconfig.util.file.FileUtils.findConfigFileOrDir;
import static org.apache.commons.io.FilenameUtils.getBaseName;

/**
 * Loads the private keys specified in the 'privateKeys' directory. Private Keys in this directory must end with .p12.
 */
@Singleton
public class PrivateKeyLoader implements EntityLoader {

    @Override
    public void load(Bundle bundle, File rootDir) {
        final File privateKeysDir = findConfigFileOrDir(rootDir, "privateKeys");
        if (privateKeysDir != null && privateKeysDir.exists()) {
            final String[] privateKeyFiles = privateKeysDir.list();
            if (privateKeyFiles != null && privateKeyFiles.length > 0) {
                final Map<String, SupplierWithIO<InputStream>> map = new HashMap<>();
                Arrays.stream(privateKeyFiles).forEach(privateKey -> {
                    if (checkPrivateKeyFormat(privateKey)) {
                        map.put(getBaseName(privateKey),
                                () -> new FileInputStream(new File(privateKeysDir, privateKey)));
                    } else {
                        throw new ConfigLoadException(privateKey + " must be a valid private key extension (.p12).");
                    }
                });
                bundle.putAllPrivateKeyFiles(map);
            }
        }
    }

    @Override
    public void load(Bundle bundle, String name, String value) {
        if (checkPrivateKeyFormat(name)) {
            bundle.getPrivateKeyFiles().put(name.substring(0, name.length() - 4),
                    () -> new ByteArrayInputStream(Base64.decodeBase64(value)));
        } else {
            throw new ConfigLoadException(name + " must be a valid private key extension (.p12).");
        }
    }

    @Override
    public Object loadSingle(String name, File entitiesFile) {
        if (!checkPrivateKeyFormat(entitiesFile.getName())) {
            throw new ConfigLoadException(name + " must be a valid private key extension (.p12).");
        }
        try {
            return Base64.encodeBase64String(FileUtils.readFileToByteArray(entitiesFile));
        } catch (IOException e) {
            throw new ConfigLoadException("Cannot load private key " + name);
        }
    }

    @Override
    public Map<String, Object> load(File entitiesFile) {
        throw new ConfigLoadException("Cannot load private keys from " + entitiesFile);
    }

    private boolean checkPrivateKeyFormat(String certFileName) {
        return certFileName.toLowerCase().endsWith(".p12");
    }

    @Override
    public String getEntityType() {
        return "PRIVATE_KEY_FILE";
    }
}
