/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayconfig.tasks.zip.loader;

import com.ca.apim.gateway.cagatewayconfig.tasks.zip.beans.Bundle;
import com.ca.apim.gateway.cagatewayconfig.tasks.zip.beans.PolicyBackedService;
import com.ca.apim.gateway.cagatewayconfig.util.json.JsonTools;
import org.jetbrains.annotations.NotNull;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Map;

@Singleton
public class PolicyBackedServiceLoader extends EntityLoaderBase<PolicyBackedService> {

    private static final String FILE_NAME = "policy-backed-services";

    @Inject
    PolicyBackedServiceLoader(JsonTools jsonTools) {
        super(jsonTools);
    }

    @Override
    protected Class<PolicyBackedService> getBeanClass() {
        return PolicyBackedService.class;
    }

    @Override
    protected String getFileName() {
        return FILE_NAME;
    }

    @Override
    protected void putToBundle(Bundle bundle, @NotNull Map<String, PolicyBackedService> entitiesMap) {
        bundle.putAllPolicyBackedServices(entitiesMap);
    }

    @Override
    public String getEntityType() {
        return "POLICY_BACKED_SERVICE";
    }
}
