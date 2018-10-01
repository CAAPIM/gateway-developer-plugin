/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayconfig.tasks.zip.loader;

import com.ca.apim.gateway.cagatewayconfig.tasks.zip.beans.Bundle;
import com.ca.apim.gateway.cagatewayconfig.tasks.zip.beans.JdbcConnection;
import com.ca.apim.gateway.cagatewayconfig.util.json.JsonTools;
import org.jetbrains.annotations.NotNull;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Map;

@Singleton
public class JdbcConnectionLoader extends EntityLoaderBase<JdbcConnection> {

    private static final String JDBC_CONNECTIONS = "jdbc-connections";

    @Inject
    JdbcConnectionLoader(JsonTools jsonTools) {
        super(jsonTools);
    }

    @Override
    protected Class<JdbcConnection> getBeanClass() {
        return JdbcConnection.class;
    }

    @Override
    protected String getFileName() {
        return JDBC_CONNECTIONS;
    }

    @Override
    protected void putToBundle(Bundle bundle, @NotNull Map<String, JdbcConnection> entitiesMap) {
        bundle.putAllJdbcConnections(entitiesMap);
    }
}
