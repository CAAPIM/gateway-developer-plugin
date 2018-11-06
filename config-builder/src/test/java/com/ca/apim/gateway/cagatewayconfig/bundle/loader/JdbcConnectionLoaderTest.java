/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayconfig.bundle.loader;

import com.ca.apim.gateway.cagatewayconfig.beans.Bundle;
import com.ca.apim.gateway.cagatewayconfig.beans.JdbcConnection;
import com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentTools;
import org.junit.jupiter.api.Test;

import static com.ca.apim.gateway.cagatewayconfig.util.TestUtils.createJdbcXml;
import static org.junit.jupiter.api.Assertions.*;

class JdbcConnectionLoaderTest {

    private JdbcConnectionLoader loader = new JdbcConnectionLoader();

    @Test
    void load() {
        Bundle bundle = new Bundle();
        loader.load(bundle, createJdbcXml(DocumentTools.INSTANCE.getDocumentBuilder().newDocument()));

        assertFalse(bundle.getJdbcConnections().isEmpty());
        assertEquals(1, bundle.getJdbcConnections().size());
        assertNotNull(bundle.getJdbcConnections().get("Test"));

        JdbcConnection entity = bundle.getJdbcConnections().get("Test");
        assertNotNull(entity);
        assertEquals(1, entity.getMinimumPoolSize().intValue());
        assertEquals(2, entity.getMaximumPoolSize().intValue());
    }

}