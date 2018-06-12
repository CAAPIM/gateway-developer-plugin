/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayexport.tasks.explode.writer;

import com.ca.apim.gateway.cagatewayexport.tasks.explode.bundle.Bundle;
import com.ca.apim.gateway.cagatewayexport.tasks.explode.bundle.entity.ClusterProperty;
import com.ca.apim.gateway.cagatewayexport.util.file.DocumentFileUtils;

import java.io.*;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

public class StaticPropertiesWriter implements EntityWriter {
    private final DocumentFileUtils documentFileUtils;

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

    /**
     * This is used in order to remove the first line when printing the properties to an output stream
     * that contains a date-timestamp. Inspired by https://stackoverflow.com/a/39043903/1108370
     */
    private static class StripFirstLineStream extends FilterOutputStream {
        private boolean firstlineseen = false;

        StripFirstLineStream(final OutputStream out) {
            super(out);
        }

        @Override
        public void write(final int b) throws IOException {
            if (firstlineseen) {
                super.write(b);
            } else if (b == '\n') {
                firstlineseen = true;
            }
        }
    }
}
