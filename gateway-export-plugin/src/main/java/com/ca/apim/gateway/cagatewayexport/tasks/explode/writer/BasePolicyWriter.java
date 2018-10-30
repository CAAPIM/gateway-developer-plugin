/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayexport.tasks.explode.writer;

import com.ca.apim.gateway.cagatewayconfig.tasks.zip.beans.Policy;
import com.ca.apim.gateway.cagatewayconfig.util.file.DocumentFileUtils;
import com.ca.apim.gateway.cagatewayconfig.util.json.JsonTools;
import com.ca.apim.gateway.cagatewayexport.tasks.explode.bundle.Bundle;
import com.ca.apim.gateway.cagatewayexport.tasks.explode.bundle.entity.PolicyEntity;
import com.google.common.annotations.VisibleForTesting;

import javax.inject.Inject;
import java.io.File;
import java.util.Map;
import java.util.stream.Collectors;

public abstract class BasePolicyWriter implements EntityWriter {

    private final DocumentFileUtils documentFileUtils;
    private final JsonTools jsonTools;

    @Inject
    BasePolicyWriter(DocumentFileUtils documentFileUtils, JsonTools jsonTools) {
        this.documentFileUtils = documentFileUtils;
        this.jsonTools = jsonTools;
    }

    @Override
    public void write(Bundle bundle, File rootFolder) {
        final Map<String, Policy> globalPolicies = filterPolicies(bundle).values().stream()
                .collect(Collectors.toMap(PolicyEntity::getName, this::getPolicyBean));

        WriterHelper.writeFile(rootFolder, documentFileUtils, jsonTools, globalPolicies, getFileName(), Policy.class);
    }

    @VisibleForTesting
    Policy getPolicyBean(PolicyEntity policyEntity) {
        Policy policy = new Policy();
        policy.setTag(policyEntity.getTag());
        policy.setPath(policyEntity.getPolicyPath());
        return policy;
    }

    /**
     * @return the file name for this policy writer
     */
    abstract String getFileName();

    /**
     * @param bundle bundle object containing policies
     * @return the policy entities filtered by this writer
     */
    abstract Map<String, PolicyEntity> filterPolicies(Bundle bundle);

}
