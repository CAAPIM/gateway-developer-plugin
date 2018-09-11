/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayconfig.util.json;

import com.ca.apim.gateway.cagatewayconfig.util.file.FileUtils;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.fasterxml.jackson.databind.DeserializationFeature.*;

public class JsonTools {
    private static final Logger LOGGER = Logger.getLogger(JsonTools.class.getName());
    public static final JsonTools INSTANCE = new JsonTools(FileUtils.INSTANCE);

    public static final String JSON = "json";
    public static final String YAML = "yaml";
    private final Map<String, ObjectMapper> objectMapperMap = new HashMap<>();
    private final FileUtils fileUtils;

    public JsonTools(FileUtils fileUtils) {
        this.fileUtils = fileUtils;
        objectMapperMap.put(JSON, buildObjectMapper(new JsonFactory()));
        objectMapperMap.put(YAML, buildObjectMapper(new YAMLFactory()));
    }

    public ObjectMapper getObjectMapper(final String type) {
        ObjectMapper objectMapper = objectMapperMap.get(type);
        if (objectMapper == null) {
            throw new IllegalArgumentException("Unknown object mapper for type: " + type);
        }
        return objectMapper;
    }

    public <T> T parseDocumentFile(final File directory, final String fileName, TypeReference<T> typeMapping) {
        final File jsonFile = new File(directory, fileName + ".json");
        final File ymlFile = new File(directory, fileName + ".yml");

        final String type;
        final InputStream inputStream;
        if (jsonFile.exists() && ymlFile.exists()) {
            throw new JsonToolsException("Can have either a " + fileName + ".json or a " + fileName + ".yml not both.");
        } else if (jsonFile.isFile()) {
            type = JsonTools.JSON;
            inputStream = fileUtils.getInputStream(jsonFile);
        } else if (ymlFile.isFile()) {
            type = JsonTools.YAML;
            inputStream = fileUtils.getInputStream(ymlFile);
        } else {
            LOGGER.log(Level.FINE, "Did not find a {0} configuration file. Not loading any.", fileName);
            // no services to bundle
            return null;
        }

        return readDocumentFile(inputStream, type, typeMapping);
    }

    public <T> T readDocumentFile(final InputStream encassStream, final String type, TypeReference<T> typeMapping) {
        final ObjectMapper objectMapper = getObjectMapper(type);
        try {
            return objectMapper.readValue(encassStream, typeMapping);
        } catch (IOException e) {
            throw new JsonToolsException("Could not parse configuration file for type: " + typeMapping.getType().getTypeName() + " Message:" + e.getMessage(), e);
        }
    }

    private static ObjectMapper buildObjectMapper(JsonFactory jf) {
        ObjectMapper objectMapper = new ObjectMapper(jf);
        objectMapper.configure(FAIL_ON_UNKNOWN_PROPERTIES, false);
        objectMapper.configure(FAIL_ON_MISSING_EXTERNAL_TYPE_ID_PROPERTY, false);
        return objectMapper;
    }
}
