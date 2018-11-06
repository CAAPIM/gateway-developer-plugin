/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayexport.tasks.explode.writer;

import com.ca.apim.gateway.cagatewayconfig.beans.Bundle;
import com.ca.apim.gateway.cagatewayconfig.beans.JdbcConnection;
import com.ca.apim.gateway.cagatewayconfig.util.file.DocumentFileUtils;
import com.ca.apim.gateway.cagatewayconfig.util.json.JsonTools;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.File;
import java.util.Map;

import static com.ca.apim.gateway.cagatewayexport.tasks.explode.writer.WriterHelper.writeFile;
import static java.util.stream.Collectors.toMap;

@Singleton
public class JdbcConnectionWriter implements EntityWriter {

    private static final String JDBC_CONNECTIONS_FILE = "jdbc-connections";

    private final DocumentFileUtils documentFileUtils;
    private final JsonTools jsonTools;

    @Inject
    public JdbcConnectionWriter(DocumentFileUtils documentFileUtils, JsonTools jsonTools) {
        this.documentFileUtils = documentFileUtils;
        this.jsonTools = jsonTools;
    }

    @Override
    public void write(Bundle bundle, File rootFolder) {
        Map<String, JdbcConnection> jdbcConnections = bundle.getEntities(JdbcConnection.class)
                .values()
                .stream()
                .collect(toMap(JdbcConnection::getName, this::getJdbcConnectionBean));

        writeFile(rootFolder, documentFileUtils, jsonTools, jdbcConnections, JDBC_CONNECTIONS_FILE, JdbcConnection.class);
    }

    private JdbcConnection getJdbcConnectionBean(JdbcConnection entity) {
        JdbcConnection jdbcConnection = new JdbcConnection();
        jdbcConnection.setDriverClass(entity.getDriverClass());
        jdbcConnection.setJdbcUrl(entity.getJdbcUrl());
        jdbcConnection.setMaximumPoolSize(entity.getMaximumPoolSize());
        jdbcConnection.setMinimumPoolSize(entity.getMinimumPoolSize());
        jdbcConnection.setPasswordRef(entity.getPasswordRef());
        jdbcConnection.setPassword(entity.getPassword());
        jdbcConnection.setUser(entity.getUser());
        jdbcConnection.setProperties(entity.getProperties());
        return jdbcConnection;
    }
}
