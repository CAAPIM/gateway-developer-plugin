/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayconfig.util.string;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class EncodeDecodeUtilsTest {

    @Test
    void decodePath() {
        assertEquals("example/folder-/-\\-slashed/example-/-\\-slashed.xml", EncodeDecodeUtils.decodePath("example/folder-_¯-¯_-slashed/example-_¯-¯_-slashed.xml"));
    }

    @Test
    void encodePath() {
        assertEquals("example-_¯-¯_-slashed.xml", EncodeDecodeUtils.encodePath("example-/-\\-slashed.xml"));
    }
}