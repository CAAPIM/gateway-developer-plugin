/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayconfig.util.json;

import com.ca.apim.gateway.cagatewayconfig.util.file.FileUtils;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.apache.commons.io.FilenameUtils.getExtension;

public class JsonTools {
    private static final Logger LOGGER = Logger.getLogger(JsonTools.class.getName());
    public static final JsonTools INSTANCE = new JsonTools(FileUtils.INSTANCE);

    private static final String JSON = "json";
    private static final String YAML = "yaml";
    private static final String JSON_EXTENSION = "json";
    private static final String YAML_EXTENSION = "yml";
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

    public File getDocumentFileFromConfigDir(final File rootDir, final String fileName) {
        final File directory = new File(rootDir, "config");
        final File jsonFile = new File(directory, fileName + "." + JSON_EXTENSION);
        final File ymlFile = new File(directory, fileName + "." + YAML_EXTENSION);

        if (jsonFile.exists() && ymlFile.exists()) {
            throw new JsonToolsException("Can have either a " + fileName + ".json or a " + fileName + ".yml not both.");
        } else if (jsonFile.isFile()) {
            return jsonFile;
        } else if (ymlFile.isFile()) {
            return ymlFile;
        }

        LOGGER.log(Level.FINE, "Did not find a {0} configuration file. Not loading any.", fileName);
        // no services to bundle
        return null;
    }

    public <T> T readDocumentFile(final File file, final JavaType entityMapType) {
        final String type = getTypeFromFile(file);
        final ObjectMapper objectMapper = getObjectMapper(type);
        try (InputStream stream = fileUtils.getInputStream(file)) {
            return objectMapper.readValue(stream, entityMapType);
        } catch (IOException e) {
            throw new JsonToolsException("Could not parse configuration file for type: " + entityMapType.getGenericSignature() + " Message:" + e.getMessage(), e);
        }
    }

    @NotNull
    public String getTypeFromFile(File file) {
        final String extension = getExtension(file.getName());
        String type;
        switch (extension) {
            case JSON_EXTENSION:
                type = JSON;
                break;
            case YAML_EXTENSION:
                type = YAML;
                break;
            default:
                throw new JsonToolsException("Invalid file: " + file.getName() + ". Expecting json or yaml file formats.");
        }
        return type;
    }

    private static ObjectMapper buildObjectMapper(JsonFactory jf) {
        ObjectMapper objectMapper = new ObjectMapper(jf);
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        return objectMapper;
    }
}
