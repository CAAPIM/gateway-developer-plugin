/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayexport.tasks.explode.writer;

import com.ca.apim.gateway.cagatewayconfig.tasks.zip.beans.Bundle;
import com.ca.apim.gateway.cagatewayconfig.tasks.zip.beans.Policy;
import com.ca.apim.gateway.cagatewayconfig.util.file.DocumentFileUtils;
import com.ca.apim.gateway.cagatewayconfig.util.json.JsonTools;
import org.jetbrains.annotations.NotNull;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Map;
import java.util.stream.Collectors;

import static com.ca.apim.gateway.cagatewayconfig.tasks.zip.beans.PolicyType.GLOBAL;
import static java.util.function.Function.identity;

@Singleton
public class GlobalPolicyWriter extends BasePolicyWriter {

    private static final String FILE_NAME = "global-policies";

    @Inject
    GlobalPolicyWriter(DocumentFileUtils documentFileUtils, JsonTools jsonTools) {
        super(documentFileUtils, jsonTools);
    }

    @Override
    Map<String, Policy> filterPolicies(Bundle bundle) {
        return bundle.getEntities(Policy.class)
                .values().stream().filter(e -> e.getPolicyType() == GLOBAL).collect(Collectors.toMap(Policy::getName, identity()));
    }

    @NotNull
    String getFileName() {
        return FILE_NAME;
    }

}
