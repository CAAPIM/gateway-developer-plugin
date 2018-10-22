/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayconfig.util;

import com.ca.apim.gateway.cagatewayconfig.tasks.zip.beans.Bundle;
import com.ca.apim.gateway.cagatewayconfig.tasks.zip.builder.Entity;
import com.ca.apim.gateway.cagatewayconfig.tasks.zip.builder.EntityBuilder;
import com.ca.apim.gateway.cagatewayconfig.tasks.zip.builder.EntityBuilder.BundleType;
import com.ca.apim.gateway.cagatewayconfig.tasks.zip.builder.EntityBuilderException;
import com.ca.apim.gateway.cagatewayconfig.util.gateway.MappingActions;
import com.ca.apim.gateway.cagatewayconfig.util.gateway.MappingProperties;
import org.mockito.*;
import org.w3c.dom.Document;

import java.util.List;
import java.util.Map;

import static com.ca.apim.gateway.cagatewayconfig.util.gateway.MappingProperties.*;
import static org.junit.Assert.*;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

/**
 * Utility methods for testing purposes.
 */
public class TestUtils {

    /**
     * Assert contents of both maps are the same. Does not check ordering.
     *
     * @param expected expected map of elements
     * @param actual actual map of elements
     */
    public static void assertPropertiesContent(Map<String, Object> expected, Map<String, Object> actual) {
        assertNotNull(expected);
        assertNotNull(actual);
        assertEquals(expected.size(), actual.size());

        expected.forEach((key, value) -> {
            assertNotNull(actual.get(key));
            assertEquals(value, actual.get(key));
            actual.remove(key);
        });

        assertTrue(actual.isEmpty());
    }

    public static void testDeploymentBundleWithOnlyMapping(EntityBuilder builder,
                                                           Bundle bundle,
                                                           Document document,
                                                           String entityType,
                                                           List<String> expectedEntityNames) {
        final List<Entity> entities = builder.build(bundle, BundleType.DEPLOYMENT, document);
        assertNotNull(entities);
        assertEquals(expectedEntityNames.size(), entities.size());
        entities.forEach(e -> {
            assertNotNull(e.getId());
            assertNull(e.getXml());
            assertNotNull(e.getName());
            assertTrue(expectedEntityNames.contains(e.getName()));
            assertEquals(MappingActions.NEW_OR_EXISTING, e.getMappingAction());
            assertNotNull(e.getType());
            assertEquals(entityType, e.getType());
            assertNotNull(e.getMappingProperties());
            assertFalse(e.getMappingProperties().isEmpty());
            assertEquals(true, e.getMappingProperties().get(FAIL_ON_NEW));
            assertEquals(MappingProperties.NAME, e.getMappingProperties().get(MAP_BY));
            assertEquals(e.getName(), e.getMappingProperties().get(MAP_TO));
        });
    }

}
