/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayconfig.bundle.builder;

import com.ca.apim.gateway.cagatewayconfig.beans.Bundle;
import com.ca.apim.gateway.cagatewayconfig.beans.JmsDestination;
import com.ca.apim.gateway.cagatewayconfig.util.IdGenerator;
import com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentTools;
import org.junit.jupiter.api.Test;
import org.testcontainers.shaded.com.google.common.collect.ImmutableMap;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JmsDestinationEntityBuilderTest {

    private static final IdGenerator ID_GENERATOR = new IdGenerator();

    @Test
    void testBuildFromEmptyBundle_noJmsDestination() {
        JmsDestinationEntityBuilder builder = new JmsDestinationEntityBuilder(ID_GENERATOR);
        final List<Entity> entities = builder.build(new Bundle(), EntityBuilder.BundleType.ENVIRONMENT, DocumentTools.INSTANCE.getDocumentBuilder().newDocument());

        assertTrue(entities.isEmpty());
    }

    @Test
    void testBuildWithConnection_checkBundleContainsJmsDestination() {
        JmsDestinationEntityBuilder builder = new JmsDestinationEntityBuilder(ID_GENERATOR);
        final Bundle bundle = new Bundle();
        bundle.putAllJmsDestinations(ImmutableMap.of("jms-1", buildJdbcConnection()));

        final List<Entity> entities = builder.build(bundle, EntityBuilder.BundleType.ENVIRONMENT, DocumentTools.INSTANCE.getDocumentBuilder().newDocument());

        assertFalse(entities.isEmpty());
        assertEquals(1, entities.size());
    }

    private static JmsDestination buildJdbcConnection() {
        JmsDestination jmsDestination = new JmsDestination();
        jmsDestination.setIsInbound(true);
        jmsDestination.setIsTemplate(false);
        jmsDestination.setProviderType("TIBCO EMS");
        jmsDestination.setInitialContextFactoryClassName("com.tibco.tibjms.naming.TibjmsInitialContextFactory");
        jmsDestination.setJndiUrl("tibjmsnaming://machinename:7222");
        
        return jmsDestination;
    }
    
    // (kpak) - add more tests here
}
