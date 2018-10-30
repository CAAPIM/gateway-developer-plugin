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
        assertNull(jdbcConnection.getPassword());
    }

    @Test
    void linkMissingPassword() {
        final JdbcConnectionEntity jdbcConnection = createJdbcConnection(String.format(PASSWORD_REF_FORMAT, GATEWAY));
        assertThrows(LinkerException.class, () -> linker.link(jdbcConnection, new Bundle(), new Bundle()));
    }

    @Test
    void linkNullPassword() {
        final JdbcConnectionEntity jdbcConnection = createJdbcConnection(null);
        linker.link(jdbcConnection, new Bundle(), new Bundle());
        assertNull(jdbcConnection.getPasswordRef());
        assertNull(jdbcConnection.getPassword());
    }

    @Test
    void linkWrongPassword() {
        Bundle bundle = new Bundle();
        bundle.addEntity(createStoredPassword(GATEWAY + GATEWAY));

        final JdbcConnectionEntity jdbcConnection = createJdbcConnection(String.format(PASSWORD_REF_FORMAT, GATEWAY));
        assertThrows(LinkerException.class, () -> linker.link(jdbcConnection, bundle, bundle));
    }

    @Test
    void linkNotPasswordVariable() {
        final JdbcConnectionEntity jdbcConnection = createJdbcConnection("${" + GATEWAY + "}");
        linker.link(jdbcConnection, new Bundle(), new Bundle());
        assertNull(jdbcConnection.getPasswordRef());
        assertEquals("${" + GATEWAY + "}", jdbcConnection.getPassword());
        assertNotNull(jdbcConnection.getPassword());
    }

    @Test
    void linkNotVariableFormat() {
        final JdbcConnectionEntity jdbcConnection = createJdbcConnection(GATEWAY);
        linker.link(jdbcConnection, new Bundle(), new Bundle());
        assertNull(jdbcConnection.getPasswordRef());
        assertEquals(GATEWAY, jdbcConnection.getPassword());
        assertNotNull(jdbcConnection.getPassword());
    }

    @Test
    void linkL7C2VariableFormat() {
        final JdbcConnectionEntity jdbcConnection = createJdbcConnection("$L7C2$1,3ok3RLhLqpD3Z3QdyTpoa2iHU2dRYdAF3TgSchF2ttI=$2EqC+niJG4yw7LOJ52Rur0VcGccT/r1WpHE+4Aiqj5GcNNYXub9h7pO5CrGT7eFGhyub2ilKx5M+ULQtbU5ZTcGxgj4K+H0+y9Yq5LNbKggoHYa+3T8r9pIcUamcCx7q");
        linker.link(jdbcConnection, new Bundle(), new Bundle());
        assertNull(jdbcConnection.getPasswordRef());
        assertNull(jdbcConnection.getPassword());
    }

    private static JdbcConnectionEntity createJdbcConnection(String password) {
        return new JdbcConnectionEntity.Builder()
                .name("Test")
                .user("gateway")
                .password(password)
                .build();
    }

    private static StoredPasswordEntity createStoredPassword(String name) {
        return new StoredPasswordEntity
                .Builder()
                .name(name)
                .build();
    }
}
