/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayconfig.config.loader;

import com.ca.apim.gateway.cagatewayconfig.beans.Bundle;
import com.ca.apim.gateway.cagatewayconfig.beans.CassandraConnection;
import com.ca.apim.gateway.cagatewayconfig.util.json.JsonTools;
import org.jetbrains.annotations.NotNull;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Map;

@Singleton
public class CassandraConnectionsLoader extends EntityLoaderBase<CassandraConnection> {

    private static final String CASSANDRA_CONNECTIONS = "cassandra-connections";

    @Inject
    CassandraConnectionsLoader(final JsonTools jsonTools) {
        super(jsonTools);
    }

    @Override
    protected Class<CassandraConnection> getBeanClass() {
        return CassandraConnection.class;
    }

    @Override
    protected String getFileName() {
        return CASSANDRA_CONNECTIONS;
    }

    @Override
    protected void putToBundle(Bundle bundle, @NotNull Map<String, CassandraConnection> entitiesMap) {
        bundle.putAllCassandraConnections(entitiesMap);
    }

    @Override
    public String getEntityType() {
        return "CASSANDRA_CONNECTION";
    }
}
