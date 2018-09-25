/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayexport.tasks.explode.linker;

import com.ca.apim.gateway.cagatewayexport.tasks.explode.bundle.Bundle;
import com.ca.apim.gateway.cagatewayexport.tasks.explode.bundle.entity.JdbcConnectionEntity;
import com.ca.apim.gateway.cagatewayexport.tasks.explode.bundle.entity.StoredPasswordEntity;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class JdbcConnectionLinkerTest {

    private static final String GATEWAY = "gateway";
    private static final String GATEWAY_WITH_DOTS = "gateway.gateway";
    private static final String PASSWORD_REF_FORMAT = "${secpass.%s.plaintext}";

    private JdbcConnectionLinker linker = new JdbcConnectionLinker();

    @Test
    void link() {
        link(GATEWAY);
    }

    @Test
    void linkUsingPasswordWithDots() {
        link(GATEWAY_WITH_DOTS);
    }

    private void link(String passwordVar) {
        Bundle bundle = new Bundle();
        bundle.addEntity(createStoredPassword(passwordVar));

        final JdbcConnectionEntity jdbcConnection = createJdbcConnection(String.format(PASSWORD_REF_FORMAT, passwordVar));
        linker.link(jdbcConnection, bundle, bundle);

        assertNotNull(jdbcConnection.getPasswordRef());
        assertEquals(passwordVar, jdbcConnection.getPasswordRef());
    }

    @Test
    void linkMissingPassword() {
        final JdbcConnectionEntity jdbcConnection = createJdbcConnection(String.format(PASSWORD_REF_FORMAT, GATEWAY));
        assertThrows(LinkerException.class, () -> linker.link(jdbcConnection, new Bundle(), new Bundle()));
    }

    @Test
    void linkWrongPassword() {
        Bundle bundle = new Bundle();
        bundle.addEntity(createStoredPassword(GATEWAY + GATEWAY));

        final JdbcConnectionEntity jdbcConnection = createJdbcConnection(String.format(PASSWORD_REF_FORMAT, GATEWAY));
        assertThrows(LinkerException.class, () -> linker.link(jdbcConnection, bundle, bundle));
    }

    @Test
    void linkInvalidVariable() {
        final JdbcConnectionEntity jdbcConnection = createJdbcConnection("${" + GATEWAY + "}");
        assertThrows(LinkerException.class, () -> linker.link(jdbcConnection, new Bundle(), new Bundle()));
    }

    @Test
    void linkInvalidVariableFormat() {
        final JdbcConnectionEntity jdbcConnection = createJdbcConnection(GATEWAY);
        assertThrows(LinkerException.class, () -> linker.link(jdbcConnection, new Bundle(), new Bundle()));
    }

    private static JdbcConnectionEntity createJdbcConnection(String passwordRef) {
        return new JdbcConnectionEntity.Builder()
                .name("Test")
                .user("gateway")
                .passwordRef(passwordRef)
                .build();
    }

    private static StoredPasswordEntity createStoredPassword(String name) {
        return new StoredPasswordEntity
                .Builder()
                .name(name)
                .build();
    }
}
