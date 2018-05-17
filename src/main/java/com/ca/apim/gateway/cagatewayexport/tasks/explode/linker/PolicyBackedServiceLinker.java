/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayexport.tasks.explode.linker;

import com.ca.apim.gateway.cagatewayexport.tasks.explode.bundle.Bundle;
import com.ca.apim.gateway.cagatewayexport.tasks.explode.bundle.entity.Folder;
import com.ca.apim.gateway.cagatewayexport.tasks.explode.bundle.entity.PolicyBackedServiceEntity;
import com.ca.apim.gateway.cagatewayexport.tasks.explode.bundle.entity.PolicyEntity;

import java.nio.file.Path;
import java.nio.file.Paths;

public class PolicyBackedServiceLinker implements EntityLinker<PolicyBackedServiceEntity> {

    @Override
    public Class<PolicyBackedServiceEntity> getEntityClass() {
        return PolicyBackedServiceEntity.class;
    }

    @Override
    public void link(PolicyBackedServiceEntity pbs, Bundle bundle) {
        for (String operation : pbs.getOperations().keySet()) {
            String policyId = pbs.getOperations().get(operation);
            pbs.getOperations().put(operation, getPolicyPath(bundle, policyId));
        }
    }

    private String getPolicyPath(Bundle bundle, String policyId) {
        PolicyEntity policy = bundle.getEntities(PolicyEntity.class).get(policyId);
        Folder folder = bundle.getFolderTree().getFolderById(policy.getFolderId());
        Path folderPath = bundle.getFolderTree().getPath(folder);
        return Paths.get(folderPath.toString(), policy.getName() + ".xml").toString();
    }
}
