/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayconfig.tasks.zip.loader;

import com.ca.apim.gateway.cagatewayconfig.tasks.zip.beans.Bundle;
import com.ca.apim.gateway.cagatewayconfig.tasks.zip.beans.Policy;
import com.ca.apim.gateway.cagatewayconfig.util.json.JsonTools;
import org.jetbrains.annotations.NotNull;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Map;
import java.util.stream.Collectors;

import static com.ca.apim.gateway.cagatewayconfig.tasks.zip.beans.PolicyType.GLOBAL;

@Singleton
public class GlobalPolicyLoader extends EntityLoaderBase<Policy> {

    private static final String FILE_NAME = "global-policies";

    @Inject
    GlobalPolicyLoader(JsonTools jsonTools) {
        super(jsonTools);
    }

    @Override
    protected void putToBundle(Bundle bundle, @NotNull Map<String, Policy> entitiesMap) {
        final Map<String, Policy> policiesByPath = entitiesMap.values().stream().collect(Collectors.toMap(Policy::getPath, policy -> {
            policy.setPolicyType(GLOBAL);
            return policy;
        }));
        bundle.putAllPolicies(policiesByPath);
    }

    @Override
    public String getEntityType() {
        return "GLOBAL_POLICY";
    }

    @Override
    protected Class<Policy> getBeanClass() {
        return Policy.class;
    }

    @Override
    protected String getFileName() {
        return FILE_NAME;
    }
}
