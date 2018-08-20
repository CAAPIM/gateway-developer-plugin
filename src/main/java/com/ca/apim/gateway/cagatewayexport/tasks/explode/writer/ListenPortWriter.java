package com.ca.apim.gateway.cagatewayexport.tasks.explode.writer;

import com.ca.apim.gateway.cagatewayexport.tasks.explode.bundle.Bundle;
import com.ca.apim.gateway.cagatewayexport.tasks.explode.bundle.entity.ListenPortEntity;
import com.ca.apim.gateway.cagatewayexport.tasks.explode.bundle.entity.ListenPortEntity.ListenPortEntityTlsSettings;
import com.ca.apim.gateway.cagatewayexport.tasks.explode.writer.beans.ListenPort;
import com.ca.apim.gateway.cagatewayexport.tasks.explode.writer.beans.ListenPort.ClientAuthentication;
import com.ca.apim.gateway.cagatewayexport.tasks.explode.writer.beans.ListenPort.ListenPortTlsSettings;
import com.ca.apim.gateway.cagatewayexport.util.file.DocumentFileUtils;
import com.ca.apim.gateway.cagatewayexport.util.json.JsonTools;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static java.util.stream.Collectors.toMap;

public class ListenPortWriter implements EntityWriter {

    public static final String LISTEN_PORTS_FILE = "listen-ports.yml";

    private final DocumentFileUtils documentFileUtils;
    private final JsonTools jsonTools;

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

        return listenPort;
    }

    private ListenPortTlsSettings getTlsSettingsBean(ListenPortEntityTlsSettings tlsSettingsEntity) {
        ListenPortTlsSettings tlsSettings = new ListenPortTlsSettings();
        tlsSettings.setClientAuthentication(ClientAuthentication.valueOf(tlsSettings.getClientAuthentication().name()));
        tlsSettings.setEnabledCipherSuites(new ArrayList<>(tlsSettingsEntity.getEnabledCipherSuites()));
        tlsSettings.setEnabledVersions(new ArrayList<>(tlsSettingsEntity.getEnabledVersions()));
        tlsSettings.setProperties(new HashMap<>(tlsSettingsEntity.getProperties()));

        return tlsSettings;
    }
}
