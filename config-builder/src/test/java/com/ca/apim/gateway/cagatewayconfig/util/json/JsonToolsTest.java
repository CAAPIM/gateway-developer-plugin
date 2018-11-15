/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayconfig.util.json;

import com.ca.apim.gateway.cagatewayconfig.util.file.FileUtils;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.junit.jupiter.api.Test;

import java.io.File;

import static org.junit.jupiter.api.Assertions.*;

class JsonToolsTest {

    private JsonTools jsonTools = new JsonTools(FileUtils.INSTANCE);

    @Test
    void getObjectMapper() {
        assertNotNull(jsonTools.getObjectMapper());
        assertTrue(jsonTools.getObjectMapper().getFactory() instanceof YAMLFactory);
        jsonTools.setOutputType(JsonTools.JSON);
        assertFalse(jsonTools.getObjectMapper().getFactory() instanceof YAMLFactory);
        assertTrue(jsonTools.getObjectMapper(JsonTools.YAML).getFactory() instanceof YAMLFactory);
        assertFalse(jsonTools.getObjectMapper(JsonTools.JSON).getFactory() instanceof YAMLFactory);
        assertThrows(IllegalArgumentException.class, () -> jsonTools.getObjectMapper("xyz"));
        assertFalse(jsonTools.getObjectMapper(JsonTools.YAML).getDeserializationConfig().isEnabled(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES));
        assertFalse(jsonTools.getObjectMapper(JsonTools.YAML).getDeserializationConfig().isEnabled(DeserializationFeature.FAIL_ON_MISSING_EXTERNAL_TYPE_ID_PROPERTY));
        assertFalse(jsonTools.getObjectMapper(JsonTools.JSON).getDeserializationConfig().isEnabled(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES));
        assertFalse(jsonTools.getObjectMapper(JsonTools.JSON).getDeserializationConfig().isEnabled(DeserializationFeature.FAIL_ON_MISSING_EXTERNAL_TYPE_ID_PROPERTY));
    }

    @Test
    void getObjectWriter() {
        assertNotNull(jsonTools.getObjectWriter());
    }

    @Test
    void getTypeFromFile() {
        assertEquals(JsonTools.YAML, jsonTools.getTypeFromFile(new File("test.yml")));
        assertEquals(JsonTools.JSON, jsonTools.getTypeFromFile(new File("test.json")));
        assertThrows(JsonToolsException.class, () -> jsonTools.getTypeFromFile(new File("test.txt")));
    }

    @Test
    void getTypeFromExtension() {
        assertEquals(JsonTools.YAML, jsonTools.getTypeFromExtension("yml"));
        assertEquals(JsonTools.YAML, jsonTools.getTypeFromExtension("yaml"));
        assertEquals(JsonTools.JSON, jsonTools.getTypeFromExtension("json"));
        assertNull(jsonTools.getTypeFromExtension("test.txt"));
    }

    @Test
    void setOutputType() {
        jsonTools.setOutputType(JsonTools.YAML);
        assertTrue(jsonTools.getObjectMapper().getFactory() instanceof YAMLFactory);
        jsonTools.setOutputType(JsonTools.JSON);
        assertFalse(jsonTools.getObjectMapper().getFactory() instanceof YAMLFactory);
        jsonTools.setOutputType("xyz");
        assertTrue(jsonTools.getObjectMapper().getFactory() instanceof YAMLFactory);
    }

    @Test
    void getFileExtension() {
        assertEquals(".yml", jsonTools.getFileExtension());
        jsonTools.setOutputType(JsonTools.JSON);
        assertEquals(".json", jsonTools.getFileExtension());
    }
}