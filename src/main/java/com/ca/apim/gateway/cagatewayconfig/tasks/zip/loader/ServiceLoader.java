/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayconfig.tasks.zip.loader;

import com.ca.apim.gateway.cagatewayconfig.tasks.zip.beans.Bundle;
import com.ca.apim.gateway.cagatewayconfig.tasks.zip.beans.Service;
import com.ca.apim.gateway.cagatewayconfig.util.json.JsonTools;
import com.fasterxml.jackson.core.type.TypeReference;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class ServiceLoader implements EntityLoader {
    private static final TypeReference<HashMap<String, Service>> servicesMapTypeMapping = new TypeReference<>() {
    };
    private final JsonTools jsonTools;

    public ServiceLoader(JsonTools jsonTools) {
        this.jsonTools = jsonTools;
    }

    @Override
    public void load(final Bundle bundle, final File rootDir) {
        final Map<String, Service> services = jsonTools.parseDocumentFile(new File(rootDir, "config"), "services", servicesMapTypeMapping);
        if (services != null) {
            bundle.putAllServices(services);
        }
    }
}
