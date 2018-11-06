/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayexport.tasks.explode.writer;

import com.ca.apim.gateway.cagatewayconfig.beans.Bundle;
import com.ca.apim.gateway.cagatewayconfig.beans.EnvironmentProperty;
import com.ca.apim.gateway.cagatewayconfig.util.file.DocumentFileUtils;
import com.ca.apim.gateway.cagatewayexport.tasks.explode.linker.LinkerException;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.File;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

import static com.ca.apim.gateway.cagatewayexport.tasks.explode.writer.WriterHelper.writePropertiesFile;

@Singleton
public class EnvironmentPropertiesWriter implements EntityWriter {

    private static final String FILE_NAME = "env";
    private final DocumentFileUtils documentFileUtils;

    @Inject
    EnvironmentPropertiesWriter(DocumentFileUtils documentFileUtils) {
        this.documentFileUtils = documentFileUtils;
    }

    @Override
    public void write(Bundle bundle, File rootFolder) {
        Map<String, EnvironmentProperty> environmentProperty = bundle.getEntities(EnvironmentProperty.class);

        Properties properties = new Properties();
        properties.putAll(environmentProperty.values().stream()
                .collect(Collectors.toMap(this::getPropertyName, EnvironmentProperty::getValue)));

        writePropertiesFile(rootFolder, documentFileUtils, properties, FILE_NAME);
    }

    private String getPropertyName(EnvironmentProperty property) {
        switch (property.getType()) {
            case LOCAL:
                return property.getName();
            case GLOBAL:
                return "gateway." + property.getName();
            case SERVICE:
                return "service.property." + property.getName();
            default:
                throw new LinkerException("Unknown Environment Property Type: " + property.getType());
        }
    }
}
