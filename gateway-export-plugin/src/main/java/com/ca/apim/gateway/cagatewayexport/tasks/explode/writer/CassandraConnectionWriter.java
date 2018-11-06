/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayexport.tasks.explode.writer;

import com.ca.apim.gateway.cagatewayconfig.beans.Bundle;
import com.ca.apim.gateway.cagatewayconfig.beans.CassandraConnection;
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
        writeFile(rootFolder, documentFileUtils, jsonTools, bundle.getCassandraConnections(), CASSANDRA_CONNECTIONS_FILE, CassandraConnection.class);
    }
}
