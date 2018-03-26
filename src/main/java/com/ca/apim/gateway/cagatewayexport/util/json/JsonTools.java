package com.ca.apim.gateway.cagatewayexport.util.json;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator;

import java.util.HashMap;
import java.util.Map;

public class JsonTools {
    public static final JsonTools INSTANCE = new JsonTools();

    public static final String JSON = "json";
    public static final String YAML = "yaml";
    private final Map<String, ObjectMapper> objectMapperMap = new HashMap<>();

    public JsonTools() {
        objectMapperMap.put(JSON, buildObjectMapper(new JsonFactory()));
        objectMapperMap.put(YAML, buildObjectMapper(new YAMLFactory().disable(YAMLGenerator.Feature.WRITE_DOC_START_MARKER)));
    }

    public ObjectMapper getObjectMapper(final String type) {
        ObjectMapper objectMapper = objectMapperMap.get(type);
        if (objectMapper == null) {
            throw new IllegalArgumentException("Unknown object mapper for type: " + type);
        }
        return objectMapper;
    }

    public ObjectWriter getObjectWriter(final String type) {
        return getObjectMapper(type).writer().withDefaultPrettyPrinter();
    }

    private static ObjectMapper buildObjectMapper(JsonFactory jf) {
        ObjectMapper objectMapper = new ObjectMapper(jf);
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        return objectMapper;
    }
}
