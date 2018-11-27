/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayexport.tasks.explode.linker;

import com.ca.apim.gateway.cagatewayconfig.beans.Bundle;
import com.ca.apim.gateway.cagatewayconfig.beans.JmsDestination;
import com.ca.apim.gateway.cagatewayconfig.beans.StoredPassword;
import com.ca.apim.gateway.cagatewayconfig.util.IdGenerator;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class JmsDestinationLinkerTest {

    private static final IdGenerator ID_GENERATOR = new IdGenerator();
    private static final String PASSWORD_REF_FORMAT = "${secpass.%s.plaintext}";
    private static final String JNDI_STORED_PASSWORD_NAME = "jndi.password.name";
    private static final String DESTINATION_STORED_PASSWORD_NAME = "destination.password.name";
    
    
    private JmsDestinationLinker linker = new JmsDestinationLinker();

    // TODO - Test private key(s), and service. 

    @Test
    void testGetEntityClass() {
        assertEquals(JmsDestination.class, linker.getEntityClass());
    }

    @Test
    void testLinkStorePassword() {
        Bundle bundle = new Bundle();
        bundle.addEntity(createStoredPassword(JNDI_STORED_PASSWORD_NAME));
        bundle.addEntity(createStoredPassword(DESTINATION_STORED_PASSWORD_NAME));

        final JmsDestination jmsDestination = createJmsDestination(
                String.format(PASSWORD_REF_FORMAT, JNDI_STORED_PASSWORD_NAME),
                String.format(PASSWORD_REF_FORMAT, DESTINATION_STORED_PASSWORD_NAME));
        linker.link(jmsDestination, bundle, bundle);
        
        assertEquals(JNDI_STORED_PASSWORD_NAME, jmsDestination.getJndiPasswordRef());
        assertNull(jmsDestination.getJndiPassword());
        assertEquals(DESTINATION_STORED_PASSWORD_NAME, jmsDestination.getDestinationPasswordRef());
        assertNull(jmsDestination.getDestinationPassword());
    }

    @Test
    void testLinkPasswordPlainText() {
        final String jndiPassword = "jndi-plaintextpassword";
        final String destinationPassword = "destination-plaintextpassword";

        final JmsDestination jmsDestination = createJmsDestination(jndiPassword, destinationPassword);
        linker.link(jmsDestination, new Bundle(), new Bundle());

        assertEquals(jndiPassword, jmsDestination.getJndiPassword());
        assertNull(jmsDestination.getJndiPasswordRef());
        assertEquals(destinationPassword, jmsDestination.getDestinationPassword());
        assertNull(jmsDestination.getDestinationPasswordRef());
    }

    @Test
    void testLinkPasswordL7C2VariableFormat() {
        final String jndiPassword = "$L7C2$1,3ok3RLhLqpD3Z3QdyTpoa2iHU2dRYdAF3TgSchF2ttI=$2EqC+niJG4yw7LOJ52Rur0VcGccT/r1WpHE+4Aiqj5GcNNYXub9h7pO5CrGT7eFGhyub2ilKx5M+ULQtbU5ZTcGxgj4K+H0+y9Yq5LNbKggoHYa+3T8r9pIcUamcCx7q\"";
        final String destinationPassword = "$L7C2$1,3ok3RLhLqpD3Z3QdyTpoa2iHU2dRYdAF3TgSchF2ttI=$2EqC+niJG4yw7LOJ52Rur0VcGccT/r1WpHE+4Aiqj5GcNNYXub9h7pO5CrGT7eFGhyub2ilKx5M+ULQtbU5ZTcGxgj4K+H0+y9Yq5LNbKggoHYa+3T8r9pIcUamcCx7q\"";

        final JmsDestination jmsDestination = createJmsDestination(jndiPassword, destinationPassword);
        linker.link(jmsDestination, new Bundle(), new Bundle());

        assertNull(jmsDestination.getJndiPasswordRef());
        assertNull(jmsDestination.getJndiPassword());
        assertNull(jmsDestination.getDestinationPasswordRef());
        assertNull(jmsDestination.getDestinationPassword());
    }
    
    @Test
    void testLinkMissingJndiStoredPassword() {
        Bundle bundle = new Bundle();
        bundle.addEntity(createStoredPassword(DESTINATION_STORED_PASSWORD_NAME));

        final JmsDestination jmsDestination = createJmsDestination(
                String.format(PASSWORD_REF_FORMAT, JNDI_STORED_PASSWORD_NAME),
                String.format(PASSWORD_REF_FORMAT, DESTINATION_STORED_PASSWORD_NAME));
        
        assertThrows(LinkerException.class, () -> linker.link(jmsDestination, new Bundle(), new Bundle()));
    }

    @Test
    void testLinkMissingDestinationStoredPassword() {
        Bundle bundle = new Bundle();
        bundle.addEntity(createStoredPassword(JNDI_STORED_PASSWORD_NAME));

        final JmsDestination jmsDestination = createJmsDestination(
                String.format(PASSWORD_REF_FORMAT, JNDI_STORED_PASSWORD_NAME),
                String.format(PASSWORD_REF_FORMAT, DESTINATION_STORED_PASSWORD_NAME));
        
        assertThrows(LinkerException.class, () -> linker.link(jmsDestination, new Bundle(), new Bundle()));
    }

    @Test
    void testLinkNullJndiStoredPassword() {
        Bundle bundle = new Bundle();
        bundle.addEntity(createStoredPassword(DESTINATION_STORED_PASSWORD_NAME));
        
        final JmsDestination jmsDestination = createJmsDestination(
                null,
                String.format(PASSWORD_REF_FORMAT, DESTINATION_STORED_PASSWORD_NAME) );
        linker.link(jmsDestination, bundle, bundle);
        
        assertNull(jmsDestination.getJndiPasswordRef());
        assertNull(jmsDestination.getJndiPassword());
        assertEquals(DESTINATION_STORED_PASSWORD_NAME, jmsDestination.getDestinationPasswordRef());
        assertNull(jmsDestination.getDestinationPassword());
    }

    @Test
    void testLinkNullDestinationStoredPassword() {
        Bundle bundle = new Bundle();
        bundle.addEntity(createStoredPassword(JNDI_STORED_PASSWORD_NAME));

        final JmsDestination jmsDestination = createJmsDestination(
                String.format(PASSWORD_REF_FORMAT, JNDI_STORED_PASSWORD_NAME),
                null);
        linker.link(jmsDestination, bundle, bundle);

        assertEquals(JNDI_STORED_PASSWORD_NAME, jmsDestination.getJndiPasswordRef());
        assertNull(jmsDestination.getJndiPassword());
        assertNull(jmsDestination.getDestinationPasswordRef());
        assertNull(jmsDestination.getDestinationPassword());
    }
    
    private static JmsDestination createJmsDestination(
            String jndiPassword, String destinationPassword) {
        return new JmsDestination.Builder()
                .name("jms-connection")
                .jndiUsername("jndi-user")
                .jndiPassword(jndiPassword)
                .destinationUsername("destination-user")
                .destinationPassword(destinationPassword)
                .build();
    }

    private static StoredPassword createStoredPassword(String name) {
        return new StoredPassword
                .Builder()
                .id(ID_GENERATOR.generate())
                .name(name)
                .build();
    }
}
