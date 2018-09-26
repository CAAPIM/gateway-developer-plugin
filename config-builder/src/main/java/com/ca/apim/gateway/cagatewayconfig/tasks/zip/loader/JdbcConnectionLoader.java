/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayconfig.tasks.zip.loader;

import com.ca.apim.gateway.cagatewayconfig.tasks.zip.beans.Bundle;
import com.ca.apim.gateway.cagatewayconfig.tasks.zip.beans.JdbcConnection;
import com.ca.apim.gateway.cagatewayconfig.util.json.JsonTools;
import com.fasterxml.jackson.core.type.TypeReference;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class JdbcConnectionLoader implements EntityLoader {

    private static final String JDBC_CONNECTIONS = "jdbc-connections";
    private static final TypeReference<HashMap<String, JdbcConnection>> jdbcMapTypeMapping = new TypeReference<HashMap<String, JdbcConnection>>() {
    };

    private final JsonTools jsonTools;

    JdbcConnectionLoader(JsonTools jsonTools) {
        this.jsonTools = jsonTools;
    }

    @Override
    public void load(final Bundle bundle, final File rootDir) {
        final Map<String, JdbcConnection> jdbcConections = jsonTools.parseDocumentFileFromConfigDir(
                rootDir,
                JDBC_CONNECTIONS,
                jdbcMapTypeMapping
        );

        if (jdbcConections != null){
            bundle.putAllJdbcConnections(jdbcConections);
        }
    }
}
