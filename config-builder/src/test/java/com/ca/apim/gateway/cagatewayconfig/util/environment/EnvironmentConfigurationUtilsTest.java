package com.ca.apim.gateway.cagatewayconfig.util.environment;

import com.ca.apim.gateway.cagatewayconfig.environment.MissingEnvironmentException;
import com.ca.apim.gateway.cagatewayconfig.util.json.JsonTools;
import org.junit.jupiter.api.Test;

import static com.ca.apim.gateway.cagatewayconfig.util.environment.EnvironmentConfigurationUtils.tryInferContentTypeFromValue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class EnvironmentConfigurationUtilsTest {

    @Test
    void inferContentYaml() {
        assertEquals(JsonTools.YAML, tryInferContentTypeFromValue("test1:1\ntest1:2"));
    }

    @Test
    void inferContentJson() {
        assertEquals(JsonTools.JSON, tryInferContentTypeFromValue("{ 'test1': 1, 'test2': 2 }"));
    }

    @Test
    void inferContentJsonArray() {
        assertEquals(JsonTools.JSON, tryInferContentTypeFromValue("[ { 'test1': 1, 'test2': 2 }, { 'test3': 3, 'test4': 4 } ]"));
    }

    @Test
    void inferContentXMLUnsupported() {
        assertThrows(MissingEnvironmentException.class, () -> tryInferContentTypeFromValue("<test1>1</test1><test2>2</test2>"));
    }
}