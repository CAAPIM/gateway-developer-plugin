/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayexport.tasks.explode.writer;

import com.ca.apim.gateway.cagatewayexport.tasks.explode.bundle.Bundle;
import com.ca.apim.gateway.cagatewayexport.tasks.explode.bundle.entity.PolicyBackedServiceEntity;
import com.ca.apim.gateway.cagatewayexport.tasks.explode.writer.beans.PolicyBackedService;
import com.ca.apim.gateway.cagatewayexport.tasks.explode.writer.beans.PolicyBackedServiceOperation;
import com.ca.apim.gateway.cagatewayexport.util.file.DocumentFileUtils;
import com.ca.apim.gateway.cagatewayexport.util.json.JsonTools;

import java.io.File;
import java.util.Map;
import java.util.stream.Collectors;

public class PolicyBackedServiceWriter implements EntityWriter {
    private static final String POLICY_BACKED_SERVICES_FILE = "policy-backed-services";
    private final DocumentFileUtils documentFileUtils;
    private final JsonTools jsonTools;

    PolicyBackedServiceWriter(DocumentFileUtils documentFileUtils, JsonTools jsonTools) {
        this.documentFileUtils = documentFileUtils;
        this.jsonTools = jsonTools;
    }

    @Override
    public void write(Bundle bundle, File rootFolder) {
        Map<String, PolicyBackedService> policyBackedServiceBeans = bundle.getEntities(PolicyBackedServiceEntity.class)
                .values()
                .stream()
                .collect(Collectors.toMap(PolicyBackedServiceEntity::getName, this::getPolicyBackedServiceBean));

        WriterHelper.writeFile(rootFolder, documentFileUtils, jsonTools, policyBackedServiceBeans, POLICY_BACKED_SERVICES_FILE);
    }

    private PolicyBackedService getPolicyBackedServiceBean(PolicyBackedServiceEntity policyBackedServiceEntity) {
        PolicyBackedService policyBackedServiceBean = new PolicyBackedService();
        policyBackedServiceBean.setInterfaceName(policyBackedServiceEntity.getInterfaceName());
        policyBackedServiceBean.setOperations(policyBackedServiceEntity.getOperations().entrySet().stream().map(e -> new PolicyBackedServiceOperation(e.getKey(), e.getValue())).collect(Collectors.toList()));
        return policyBackedServiceBean;
    }
}
