/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayexport.tasks.explode.writer;

import com.ca.apim.gateway.cagatewayconfig.tasks.zip.beans.Bundle;
import com.ca.apim.gateway.cagatewayconfig.tasks.zip.beans.StoredPassword;
import com.ca.apim.gateway.cagatewayconfig.util.file.DocumentFileUtils;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.File;
import java.util.Properties;

import static java.util.stream.Collectors.toMap;
import static org.apache.commons.lang3.StringUtils.EMPTY;

@Singleton
public class StoredPasswordWriter implements EntityWriter {

    @SuppressWarnings("squid:S2068") // sonarcloud believes this is a hardcoded password
    private static final String STORED_PASSWORDS_FILE = "stored-passwords";

    private final DocumentFileUtils documentFileUtils;

    @Inject
    public StoredPasswordWriter(DocumentFileUtils documentFileUtils) {
        this.documentFileUtils = documentFileUtils;
    }

    @Override
    public void write(Bundle bundle, File rootFolder) {
        Properties properties = new Properties();
        properties.putAll(bundle.getEntities(StoredPassword.class)
                .values()
                .stream()
                .collect(toMap(StoredPassword::getName, e -> EMPTY)));

        WriterHelper.writePropertiesFile(rootFolder, documentFileUtils, properties, STORED_PASSWORDS_FILE);
    }

}
