/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayexport.tasks.explode.writer;

import com.ca.apim.gateway.cagatewayconfig.tasks.zip.beans.Bundle;
import com.ca.apim.gateway.cagatewayconfig.tasks.zip.beans.ClusterProperty;
import com.ca.apim.gateway.cagatewayconfig.util.file.DocumentFileUtils;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.File;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

@Singleton
public class StaticPropertiesWriter implements EntityWriter {

    private static final String STATIC_PROPERTIES_FILE = "static";

    private final DocumentFileUtils documentFileUtils;

    @Inject
    StaticPropertiesWriter(DocumentFileUtils documentFileUtils) {
        this.documentFileUtils = documentFileUtils;
    }

    @Override
    public void write(Bundle bundle, File rootFolder) {
        Map<String, ClusterProperty> clusterProperties = bundle.getEntities(ClusterProperty.class);

        Properties properties = new Properties();
        properties.putAll(clusterProperties.values().stream().collect(Collectors.toMap(ClusterProperty::getName, ClusterProperty::getValue)));

        WriterHelper.writePropertiesFile(rootFolder, documentFileUtils, properties, STATIC_PROPERTIES_FILE);
    }
}
