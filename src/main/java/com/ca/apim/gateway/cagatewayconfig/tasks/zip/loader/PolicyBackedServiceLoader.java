/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayconfig.tasks.zip.loader;

import com.ca.apim.gateway.cagatewayconfig.tasks.zip.beans.Bundle;
import com.ca.apim.gateway.cagatewayconfig.tasks.zip.beans.PolicyBackedService;
import com.ca.apim.gateway.cagatewayconfig.util.json.JsonTools;
import com.fasterxml.jackson.core.type.TypeReference;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class PolicyBackedServiceLoader implements EntityLoader {
    private static final TypeReference<HashMap<String, PolicyBackedService>> policyBackedServiceMapTypeMapping = new TypeReference<>() {
    };
    private final JsonTools jsonTools;

    public PolicyBackedServiceLoader(JsonTools jsonTools) {
        this.jsonTools = jsonTools;
    }

    @Override
    public void load(final Bundle bundle, final File rootDir) {
        final Map<String, PolicyBackedService> policyBackedServices = jsonTools.parseDocumentFile(new File(rootDir, "config"), "policy-backed-services", policyBackedServiceMapTypeMapping);
        if (policyBackedServices != null) {
            bundle.putAllPolicyBackedServices(policyBackedServices);
        }
    }
}
