/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayexport.tasks.explode.writer;

import com.ca.apim.gateway.cagatewayexport.tasks.explode.bundle.Bundle;
import com.ca.apim.gateway.cagatewayexport.tasks.explode.bundle.entity.ClusterProperty;
import com.ca.apim.gateway.cagatewayexport.util.file.DocumentFileUtils;
import com.ca.apim.gateway.cagatewayexport.util.file.StripFirstLineStream;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

@Singleton
public class StaticPropertiesWriter implements EntityWriter {
    private final DocumentFileUtils documentFileUtils;

    @Inject
    StaticPropertiesWriter(DocumentFileUtils documentFileUtils) {
        this.documentFileUtils = documentFileUtils;
    }

    @Override
    public void write(Bundle bundle, File rootFolder) {
        File configFolder = new File(rootFolder, "config");
        documentFileUtils.createFolder(configFolder.toPath());

        Map<String, ClusterProperty> clusterProperties = bundle.getEntities(ClusterProperty.class);

        Properties properties = new Properties();
        properties.putAll(clusterProperties.values().stream().collect(Collectors.toMap(ClusterProperty::getName, ClusterProperty::getValue)));

        File servicesFile = new File(configFolder, "static.properties");

        try (OutputStream outputStream = new StripFirstLineStream(new FileOutputStream(servicesFile))) {
            properties.store(outputStream, null);
        } catch (IOException e) {
            throw new WriteException("Could not create static properties file: " + e.getMessage(), e);
        }
    }
}
