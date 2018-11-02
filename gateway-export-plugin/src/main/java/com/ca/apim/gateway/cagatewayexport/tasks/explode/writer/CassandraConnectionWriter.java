/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayexport.tasks.explode.writer;

import com.ca.apim.gateway.cagatewayconfig.tasks.zip.beans.Bundle;
import com.ca.apim.gateway.cagatewayconfig.tasks.zip.beans.CassandraConnection;
import com.ca.apim.gateway.cagatewayconfig.util.file.DocumentFileUtils;
import com.ca.apim.gateway.cagatewayconfig.util.json.JsonTools;
import com.google.common.annotations.VisibleForTesting;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.File;
import java.util.Map;

import static com.ca.apim.gateway.cagatewayexport.tasks.explode.writer.WriterHelper.writeFile;
import static java.util.stream.Collectors.toMap;

@Singleton
public class CassandraConnectionWriter implements EntityWriter {

    private static final String CASSANDRA_CONNECTIONS_FILE = "cassandra-connections";

    private final DocumentFileUtils documentFileUtils;
    private final JsonTools jsonTools;

    @Inject
    public CassandraConnectionWriter(DocumentFileUtils documentFileUtils, JsonTools jsonTools) {
        this.documentFileUtils = documentFileUtils;
        this.jsonTools = jsonTools;
    }

    @Override
    public void write(Bundle bundle, File rootFolder) {
        Map<String, CassandraConnection> jdbcConnections = bundle.getCassandraConnections()
                .values()
                .stream()
                .collect(toMap(CassandraConnection::getName, this::getBean));

        writeFile(rootFolder, documentFileUtils, jsonTools, jdbcConnections, CASSANDRA_CONNECTIONS_FILE, CassandraConnection.class);
    }

    @VisibleForTesting
    CassandraConnection getBean(CassandraConnection entity) {
        CassandraConnection cassandraConnection = new CassandraConnection();
        cassandraConnection.setCompression(entity.getCompression());
        cassandraConnection.setContactPoint(entity.getContactPoint());
        cassandraConnection.setPort(entity.getPort());
        cassandraConnection.setSsl(entity.getSsl());
        cassandraConnection.setUsername(entity.getUsername());
        cassandraConnection.setKeyspace(entity.getKeyspace());
        cassandraConnection.setStoredPasswordName(entity.getStoredPasswordName());
        cassandraConnection.setProperties(entity.getProperties());
        cassandraConnection.setTlsCiphers(entity.getTlsCiphers());
        return cassandraConnection;
    }
}
