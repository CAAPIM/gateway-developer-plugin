/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayexport.tasks.explode.writer;

import com.ca.apim.gateway.cagatewayconfig.tasks.zip.beans.Bundle;
import com.ca.apim.gateway.cagatewayconfig.tasks.zip.beans.PrivateKey;
import com.ca.apim.gateway.cagatewayconfig.util.file.DocumentFileUtils;
import com.ca.apim.gateway.cagatewayconfig.util.json.JsonTools;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.File;
import java.util.Map;

import static com.ca.apim.gateway.cagatewayexport.tasks.explode.writer.WriterHelper.writeFile;
import static java.util.stream.Collectors.toMap;

@Singleton
public class PrivateKeyWriter implements EntityWriter {

    private static final String FILE_NAME = "private-keys";

    private final DocumentFileUtils documentFileUtils;
    private final JsonTools jsonTools;

    @Inject
    public PrivateKeyWriter(DocumentFileUtils documentFileUtils, JsonTools jsonTools) {
        this.documentFileUtils = documentFileUtils;
        this.jsonTools = jsonTools;
    }

    @Override
    public void write(Bundle bundle, File rootFolder) {
        Map<String, PrivateKey> privateKeys = bundle.getEntities(PrivateKey.class)
                .values()
                .stream()
                .collect(toMap(PrivateKey::getName, this::getPrivateKeyBean));

        writeFile(rootFolder, documentFileUtils, jsonTools, privateKeys, FILE_NAME, PrivateKey.class);
    }

    private PrivateKey getPrivateKeyBean(PrivateKey entity) {
        PrivateKey privateKey = new PrivateKey();
        privateKey.setAlgorithm(entity.getAlgorithm());
        privateKey.setKeystore(entity.getKeyStoreType().getName());
        return privateKey;
    }
}
