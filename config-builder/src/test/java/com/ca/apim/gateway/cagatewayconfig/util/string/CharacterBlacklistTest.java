/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayconfig.util.string;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CharacterBlacklistTest {

    @Test
    void filterAndReplace() {
        assertEquals("example-----slashed.xml", CharacterBlacklistUtil.filterAndReplace("example-/-\\-slashed.xml"));
    }
}