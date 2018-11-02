/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayexport.tasks.explode.writer;

import com.ca.apim.gateway.cagatewayconfig.tasks.zip.beans.Bundle;
import com.ca.apim.gateway.cagatewayconfig.tasks.zip.beans.ListenPort;
import com.ca.apim.gateway.cagatewayconfig.util.file.DocumentFileUtils;
import com.ca.apim.gateway.cagatewayconfig.util.json.JsonTools;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

import static com.ca.apim.gateway.cagatewayconfig.tasks.zip.beans.ListenPort.ClientAuthentication;
import static com.ca.apim.gateway.cagatewayconfig.tasks.zip.beans.ListenPort.ListenPortTlsSettings;
import static com.ca.apim.gateway.cagatewayexport.tasks.explode.writer.WriterHelper.*;
import static java.util.stream.Collectors.toMap;

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
        Map<String, ListenPort> listenPortBeans = bundle.getEntities(ListenPort.class)
                .values()
                .stream()
                .collect(toMap(ListenPort::getName, this::getListenPortBean));

        writeFile(rootFolder, documentFileUtils, jsonTools, listenPortBeans, LISTEN_PORTS_FILE, ListenPort.class);
    }

    private ListenPort getListenPortBean(ListenPort listenPortEntity) {
        ListenPort listenPort = new ListenPort();
        listenPort.setPort(listenPortEntity.getPort());
        listenPort.setEnabledFeatures(listenPortEntity.getEnabledFeatures());
        listenPort.setProperties(new HashMap<>(listenPortEntity.getProperties()));
        listenPort.setProtocol(listenPortEntity.getProtocol());
        listenPort.setTlsSettings(getTlsSettingsBean(listenPortEntity.getTlsSettings()));
        listenPort.setTargetServiceReference(listenPortEntity.getTargetServiceReference());

        return listenPort;
    }

    private ListenPortTlsSettings getTlsSettingsBean(ListenPortTlsSettings tlsSettingsEntity) {
        if (tlsSettingsEntity == null) {
            return null;
        }

        ListenPortTlsSettings tlsSettings = new ListenPortTlsSettings();
        tlsSettings.setClientAuthentication(ClientAuthentication.valueOf(tlsSettingsEntity.getClientAuthentication().name()));
        tlsSettings.setEnabledCipherSuites(copySet(tlsSettingsEntity.getEnabledCipherSuites()));
        tlsSettings.setEnabledVersions(copySet(tlsSettingsEntity.getEnabledVersions()));
        tlsSettings.setProperties(copyMap(tlsSettingsEntity.getProperties()));

        return tlsSettings;
    }
}
