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

import static com.ca.apim.gateway.cagatewayconfig.tasks.zip.beans.PolicyType.INTERNAL;
import static java.util.function.Function.identity;
import static org.apache.commons.lang3.ObjectUtils.firstNonNull;
import static org.apache.commons.lang3.StringUtils.EMPTY;

@Singleton
public class AuditPolicyWriter extends BasePolicyWriter {

    private static final String FILE_NAME = "audit-policies";

    @Inject
    AuditPolicyWriter(DocumentFileUtils documentFileUtils, JsonTools jsonTools) {
        super(documentFileUtils, jsonTools);
    }

    @Override
    Map<String, Policy> filterPolicies(Bundle bundle) {
        return bundle.getPolicies()
                .values()
                .stream()
                .filter(e -> e.getPolicyType() == INTERNAL && firstNonNull(e.getTag(), EMPTY).startsWith("audit"))
                .collect(Collectors.toMap(Policy::getName, identity()));
    }

    @NotNull
    String getFileName() {
        return FILE_NAME;
    }

}
