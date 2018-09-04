/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayexport.tasks.explode.writer;

import com.ca.apim.gateway.cagatewayexport.tasks.explode.bundle.Bundle;
import com.ca.apim.gateway.cagatewayexport.tasks.explode.bundle.entity.ListenPortEntity;
import com.ca.apim.gateway.cagatewayexport.tasks.explode.bundle.entity.ListenPortEntity.ListenPortEntityTlsSettings;
import com.ca.apim.gateway.cagatewayexport.tasks.explode.writer.beans.ListenPort;
import com.ca.apim.gateway.cagatewayexport.tasks.explode.writer.beans.ListenPort.ClientAuthentication;
import com.ca.apim.gateway.cagatewayexport.tasks.explode.writer.beans.ListenPort.ListenPortTlsSettings;
import com.ca.apim.gateway.cagatewayexport.util.file.DocumentFileUtils;
import com.ca.apim.gateway.cagatewayexport.util.json.JsonTools;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

import static com.ca.apim.gateway.cagatewayexport.tasks.explode.writer.WriterHelper.copyList;
import static com.ca.apim.gateway.cagatewayexport.tasks.explode.writer.WriterHelper.copyMap;
import static java.util.stream.Collectors.toMap;

@Singleton
public class ListenPortWriter implements EntityWriter {

    public static final String LISTEN_PORTS_FILE = "listen-ports.yml";

    private final DocumentFileUtils documentFileUtils;
    private final JsonTools jsonTools;

    @Inject
    public ListenPortWriter(DocumentFileUtils documentFileUtils, JsonTools jsonTools) {
        this.documentFileUtils = documentFileUtils;
        this.jsonTools = jsonTools;
    }

    @Override
    public void write(Bundle bundle, File rootFolder) {
        Map<String, ListenPort> listenPortBeans = bundle.getEntities(ListenPortEntity.class).values().stream().collect(toMap(ListenPortEntity::getName, this::getListenPortBean));

        WriterHelper.writeFile(rootFolder, documentFileUtils, jsonTools, listenPortBeans, LISTEN_PORTS_FILE);
    }

    private ListenPort getListenPortBean(ListenPortEntity listenPortEntity) {
        ListenPort listenPort = new ListenPort();
        listenPort.setPort(listenPortEntity.getPort());
        listenPort.setEnabledFeatures(listenPortEntity.getEnabledFeatures());
        listenPort.setProperties(new HashMap<>(listenPortEntity.getProperties()));
        listenPort.setProtocol(listenPortEntity.getProtocol());
        listenPort.setTlsSettings(getTlsSettingsBean(listenPortEntity.getTlsSettings()));
        listenPort.setTargetServiceReference(listenPortEntity.getTargetServiceReference());

        return listenPort;
    }

    private ListenPortTlsSettings getTlsSettingsBean(ListenPortEntityTlsSettings tlsSettingsEntity) {
        if (tlsSettingsEntity == null) {
            return null;
        }

        ListenPortTlsSettings tlsSettings = new ListenPortTlsSettings();
        tlsSettings.setClientAuthentication(ClientAuthentication.valueOf(tlsSettingsEntity.getClientAuthentication().name()));
        tlsSettings.setEnabledCipherSuites(copyList(tlsSettingsEntity.getEnabledCipherSuites()));
        tlsSettings.setEnabledVersions(copyList(tlsSettingsEntity.getEnabledVersions()));
        tlsSettings.setProperties(copyMap(tlsSettingsEntity.getProperties()));

        return tlsSettings;
    }
}
