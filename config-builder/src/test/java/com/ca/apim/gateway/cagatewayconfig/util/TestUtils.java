/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayconfig.util;

import java.util.Map;

import static org.junit.Assert.*;

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
}
