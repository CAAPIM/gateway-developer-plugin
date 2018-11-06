/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayexport.tasks.explode.writer;

import com.ca.apim.gateway.cagatewayconfig.beans.Bundle;
import com.ca.apim.gateway.cagatewayconfig.beans.ListenPort;
import com.ca.apim.gateway.cagatewayconfig.util.file.DocumentFileUtils;
import com.ca.apim.gateway.cagatewayconfig.util.json.JsonTools;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.File;

import static com.ca.apim.gateway.cagatewayexport.tasks.explode.writer.WriterHelper.writeFile;

@Singleton
public class ListenPortWriter implements EntityWriter {

    private static final String LISTEN_PORTS_FILE = "listen-ports";

    private final DocumentFileUtils documentFileUtils;
    private final JsonTools jsonTools;

    @Inject
    public ListenPortWriter(DocumentFileUtils documentFileUtils, JsonTools jsonTools) {
        this.documentFileUtils = documentFileUtils;
        this.jsonTools = jsonTools;
    }

    @Override
    public void write(Bundle bundle, File rootFolder) {
        writeFile(rootFolder, documentFileUtils, jsonTools, bundle.getEntities(ListenPort.class), LISTEN_PORTS_FILE, ListenPort.class);
    }
}
