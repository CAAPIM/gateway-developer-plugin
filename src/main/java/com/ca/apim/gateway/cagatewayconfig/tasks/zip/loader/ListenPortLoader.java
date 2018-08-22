package com.ca.apim.gateway.cagatewayconfig.tasks.zip.loader;

import com.ca.apim.gateway.cagatewayconfig.tasks.zip.beans.Bundle;
import com.ca.apim.gateway.cagatewayconfig.tasks.zip.beans.ListenPort;
import com.ca.apim.gateway.cagatewayconfig.util.json.JsonTools;
import com.fasterxml.jackson.core.type.TypeReference;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class ListenPortLoader implements EntityLoader {

    private static final TypeReference<HashMap<String, ListenPort>> listenPortTypeMapping = new TypeReference<HashMap<String, ListenPort>>() {
    };
    private final JsonTools jsonTools;

    public ListenPortLoader(JsonTools jsonTools) {
        this.jsonTools = jsonTools;
    }

    @Override
    public void load(Bundle bundle, File rootDir) {
        final Map<String, ListenPort> listenPorts = jsonTools.parseDocumentFile(
                new File(rootDir, "config"),
                "listen-ports",
                listenPortTypeMapping);
        if (listenPorts != null) {
            bundle.putAllListenPorts(listenPorts);
        }
    }
}
