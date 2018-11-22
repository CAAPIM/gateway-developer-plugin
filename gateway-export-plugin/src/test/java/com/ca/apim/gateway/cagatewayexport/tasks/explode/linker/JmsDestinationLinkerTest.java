/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayexport.tasks.explode.linker;

import com.ca.apim.gateway.cagatewayconfig.beans.JmsDestination;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class JmsDestinationLinkerTest {
    
    private JmsDestinationLinker linker = new JmsDestinationLinker();

    // TODO - Test secure password(s), private key(s), and service. 
    
    @Test
    void testGetEntityClass() {
        assertEquals(JmsDestination.class, linker.getEntityClass());
    }
}
