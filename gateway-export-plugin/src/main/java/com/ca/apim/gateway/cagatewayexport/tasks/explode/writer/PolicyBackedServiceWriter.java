/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayexport.tasks.explode.writer;

import com.ca.apim.gateway.cagatewayconfig.tasks.zip.beans.PolicyBackedService;
import com.ca.apim.gateway.cagatewayconfig.tasks.zip.beans.PolicyBackedServiceOperation;
import com.ca.apim.gateway.cagatewayconfig.util.file.DocumentFileUtils;
import com.ca.apim.gateway.cagatewayexport.tasks.explode.bundle.Bundle;
import com.ca.apim.gateway.cagatewayexport.tasks.explode.bundle.entity.PolicyBackedServiceEntity;
import com.ca.apim.gateway.cagatewayexport.util.json.JsonTools;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.File;
import java.util.Map;
import java.util.stream.Collectors;

import static com.ca.apim.gateway.cagatewayexport.tasks.explode.writer.WriterHelper.writeFile;
import static java.util.stream.Collectors.toSet;

@Singleton
public class PolicyBackedServiceWriter implements EntityWriter {
    private static final String POLICY_BACKED_SERVICES_FILE = "policy-backed-services";
    private final DocumentFileUtils documentFileUtils;
    private final JsonTools jsonTools;

    @Inject
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

        writeFile(rootFolder, documentFileUtils, jsonTools, policyBackedServiceBeans, POLICY_BACKED_SERVICES_FILE, PolicyBackedService.class);
    }

    private PolicyBackedService getPolicyBackedServiceBean(PolicyBackedServiceEntity policyBackedServiceEntity) {
        PolicyBackedService policyBackedServiceBean = new PolicyBackedService();
        policyBackedServiceBean.setInterfaceName(policyBackedServiceEntity.getInterfaceName());
        policyBackedServiceBean.setOperations(policyBackedServiceEntity.getOperations().entrySet().stream().map(e -> new PolicyBackedServiceOperation(e.getKey(), e.getValue())).collect(toSet()));
        return policyBackedServiceBean;
    }
}
