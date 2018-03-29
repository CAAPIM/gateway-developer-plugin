/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayexport.tasks.explode.writer;

import com.ca.apim.gateway.cagatewayexport.tasks.explode.bundle.Bundle;
import com.ca.apim.gateway.cagatewayexport.tasks.explode.bundle.entity.ClusterProperty;
import com.ca.apim.gateway.cagatewayexport.util.file.DocumentFileUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

public class GlobalPropertiesWriter implements EntityWriter {
    private final DocumentFileUtils documentFileUtils;

    public GlobalPropertiesWriter(DocumentFileUtils documentFileUtils) {
        this.documentFileUtils = documentFileUtils;
    }

    @Override
    public void write(Bundle bundle, File rootFolder) {
        File configFolder = new File(rootFolder, "config");
        documentFileUtils.createFolder(configFolder.toPath());

        Map<String, ClusterProperty> clusterProperties = bundle.getEntities(ClusterProperty.class);

        Properties properties = new Properties();
        properties.putAll(clusterProperties.values().stream().collect(Collectors.toMap(ClusterProperty::getName, ClusterProperty::getValue)));

        File servicesFile = new File(configFolder, "global.properties");

        try (FileOutputStream fileOutputStream = new FileOutputStream(servicesFile)) {
            properties.store(fileOutputStream, null);
        } catch (IOException e) {
            throw new WriteException("Could not create global properties file: " + e.getMessage(), e);
        }
    }
}
