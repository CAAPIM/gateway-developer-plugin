/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayconfig.tasks.zip.loader;

import com.ca.apim.gateway.cagatewayconfig.tasks.zip.beans.Bundle;
import com.ca.apim.gateway.cagatewayconfig.tasks.zip.beans.Service;
import com.ca.apim.gateway.cagatewayconfig.util.file.FileUtils;
import com.ca.apim.gateway.cagatewayconfig.util.json.JsonTools;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ServiceLoader implements EntityLoader {
    private static final Logger LOGGER = Logger.getLogger(ServiceLoader.class.getName());

    private static final TypeReference<HashMap<String, Service>> servicesMapTypeMapping = new TypeReference<HashMap<String, Service>>() {
    };
    private final JsonTools jsonTools;
    private final FileUtils fileUtils;

    public ServiceLoader(FileUtils fileUtils, JsonTools jsonTools) {
        this.fileUtils = fileUtils;
        this.jsonTools = jsonTools;
    }

    @Override
    public void load(final Bundle bundle, final File rootDir) {
        final File serviceJsonFile = new File(rootDir, "config/services.json");
        final File serviceYmlFile = new File(rootDir, "config/services.yml");

        final String type;
        final InputStream servicesStream;
        if (serviceJsonFile.exists() && serviceYmlFile.exists()) {
            throw new BundleLoadException("Can have either a services.json or a services.yml not both.");
        } else if (serviceJsonFile.isFile()) {
            type = JsonTools.JSON;
            servicesStream = fileUtils.getInputStream(serviceJsonFile);
        } else if (serviceYmlFile.isFile()) {
            type = JsonTools.YAML;
            servicesStream = fileUtils.getInputStream(serviceYmlFile);
        } else {
            LOGGER.log(Level.FINE, "Did not find a service configuration file. Not loading any services.");
            // no services to bundle
            return;
        }

        final Map<String, Service> services = getServices(servicesStream, type);
        bundle.putAllServices(services);
    }

    private Map<String, Service> getServices(final InputStream servicesStream, final String type) {
        final ObjectMapper objectMapper = jsonTools.getObjectMapper(type);
        try {
            return objectMapper.readValue(servicesStream, servicesMapTypeMapping);
        } catch (IOException e) {
            throw new BundleLoadException("Could not parse services configuration file:" + e.getMessage(), e);
        }
    }
}
