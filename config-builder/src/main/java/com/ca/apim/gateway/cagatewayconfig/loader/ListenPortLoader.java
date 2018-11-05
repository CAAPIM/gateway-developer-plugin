/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */
package com.ca.apim.gateway.cagatewayconfig.loader;

import com.ca.apim.gateway.cagatewayconfig.beans.Bundle;
import com.ca.apim.gateway.cagatewayconfig.beans.ListenPort;
import com.ca.apim.gateway.cagatewayconfig.util.json.JsonTools;
import org.jetbrains.annotations.NotNull;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Map;

@Singleton
public class ListenPortLoader extends EntityLoaderBase<ListenPort> {

    private static final String FILE_NAME = "listen-ports";

    @Inject
    ListenPortLoader(JsonTools jsonTools) {
        super(jsonTools);
    }

    @Override
    protected Class<ListenPort> getBeanClass() {
        return ListenPort.class;
    }

    @Override
    protected String getFileName() {
        return FILE_NAME;
    }

    @Override
    protected void putToBundle(Bundle bundle, @NotNull Map<String, ListenPort> entitiesMap) {
        bundle.putAllListenPorts(entitiesMap);
    }

    @Override
    public String getEntityType() {
        return "LISTEN_PORT";
    }
}
