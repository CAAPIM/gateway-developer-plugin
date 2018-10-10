/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayconfig.tasks.zip.loader;

import com.ca.apim.gateway.cagatewayconfig.tasks.zip.beans.Bundle;
import com.ca.apim.gateway.cagatewayconfig.tasks.zip.beans.Service;
import com.ca.apim.gateway.cagatewayconfig.util.json.JsonTools;
import org.jetbrains.annotations.NotNull;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Map;

@Singleton
public class ServiceLoader extends EntityLoaderBase<Service> {

    private static final String FILE_NAME = "services";

    @Inject
    ServiceLoader(JsonTools jsonTools) {
        super(jsonTools);
    }

    @Override
    protected Class<Service> getBeanClass() {
        return Service.class;
    }

    @Override
    protected String getFileName() {
        return FILE_NAME;
    }

    @Override
    protected void putToBundle(Bundle bundle, @NotNull Map<String, Service> entitiesMap) {
        bundle.putAllServices(entitiesMap);
    }

    @Override
    public String getEntityType() {
        return "SERVICE";
    }
}
