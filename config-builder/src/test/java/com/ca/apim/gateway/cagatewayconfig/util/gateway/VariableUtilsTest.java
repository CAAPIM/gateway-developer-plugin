/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayconfig.util.gateway;

import org.junit.jupiter.api.Test;

import static com.ca.apim.gateway.cagatewayconfig.util.gateway.VariableUtils.extractVariableName;
import static org.junit.jupiter.api.Assertions.*;

class VariableUtilsTest {

    @Test
    void extractValidVariableName() {
        assertEquals("var", extractVariableName("${var}"));
        assertEquals("var.subvar", extractVariableName("${var.subvar}"));
    }

    @Test
    void invalidExpression() {
        assertNull(extractVariableName("${abc"));
        assertNull(extractVariableName("$abc"));
        assertNull(extractVariableName("abc"));
        assertNull(extractVariableName("${abc}${abc}"));
    }

    @Test
    void nullExpression() {
        assertNull(extractVariableName(null));
        assertNull(extractVariableName(""));
    }
}