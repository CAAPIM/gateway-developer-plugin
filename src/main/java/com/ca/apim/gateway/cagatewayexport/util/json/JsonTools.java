/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayexport.util.json;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.fasterxml.jackson.dataformat.yaml.YAMLGenerator.Feature.WRITE_DOC_START_MARKER;
import static com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES;

public class JsonTools {
    public static final JsonTools INSTANCE = new JsonTools();
    private static final Logger LOGGER = Logger.getLogger(JsonTools.class.getName());

    public static final String JSON = "json";
    public static final String YAML = "yaml";
    public static final String JSON_FILE_EXTENSION = ".json";
    public static final String YAML_FILE_EXTENSION = ".yml";

    private final Map<String, ObjectMapper> objectMapperMap = new HashMap<>();
    private String outputType;
    private String fileExtension;

    public JsonTools() {
        objectMapperMap.put(JSON, buildObjectMapper(new JsonFactory()));
        objectMapperMap.put(YAML, buildObjectMapper(new YAMLFactory().disable(WRITE_DOC_START_MARKER)));
        outputType = YAML;
        fileExtension = YAML_FILE_EXTENSION;
    }

    private ObjectMapper getObjectMapper(final String type) {
        ObjectMapper objectMapper = objectMapperMap.get(type);
        if (objectMapper == null) {
            throw new IllegalArgumentException("Unknown object mapper for type: " + type);
        }
        return objectMapper;
    }

    public ObjectMapper getObjectMapper() {
        return getObjectMapper(outputType);
    }

    public ObjectWriter getObjectWriter() {
        return getObjectMapper(outputType).writer().withDefaultPrettyPrinter();
    }

    private static ObjectMapper buildObjectMapper(JsonFactory jf) {
        ObjectMapper objectMapper = new ObjectMapper(jf);
        objectMapper.configure(FAIL_ON_UNKNOWN_PROPERTIES, false);
        if (jf instanceof YAMLFactory) {
            //make it so that null values get left as blanks rather then the string `null`
            objectMapper.getSerializerProvider().setNullValueSerializer(new JsonSerializer<Object>() {
                @Override
                public void serialize(Object value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
                    gen.writeNumber("");
                }
            });
        }
        return objectMapper;
    }

    public void setOutputType(String outputType) {
        if (JSON.equalsIgnoreCase(outputType)) {
            this.outputType = JSON;
            fileExtension = JSON_FILE_EXTENSION;
        } else if (YAML.equalsIgnoreCase(outputType)) {
            this.outputType = YAML;
            fileExtension = YAML_FILE_EXTENSION;
        } else {
            LOGGER.log(Level.WARNING,
                    "Output type specified is not YAML nor JSON. Using YAML as the default output type.");
        }
    }

    public String getFileExtension() {
        return fileExtension;
    }
}
