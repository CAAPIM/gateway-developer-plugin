/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayexport.tasks.explode.linker;

import com.ca.apim.gateway.cagatewayexport.tasks.explode.bundle.Bundle;
import com.ca.apim.gateway.cagatewayexport.tasks.explode.bundle.entity.PolicyBackedServiceEntity;
import com.ca.apim.gateway.cagatewayexport.tasks.explode.bundle.entity.PolicyEntity;

import javax.inject.Singleton;

import static com.ca.apim.gateway.cagatewayexport.tasks.explode.linker.PolicyLinker.getPolicyPath;

@Singleton
public class PolicyBackedServiceLinker implements EntityLinker<PolicyBackedServiceEntity> {

    @Override
    public Class<PolicyBackedServiceEntity> getEntityClass() {
        return PolicyBackedServiceEntity.class;
    }

    @Override
    public void link(PolicyBackedServiceEntity pbs, Bundle bundle, Bundle targetBundle) {
        for (String operation : pbs.getOperations().keySet()) {
            String policyId = pbs.getOperations().get(operation);
            PolicyEntity policy = bundle.getEntities(PolicyEntity.class).get(policyId);
            if (policy == null) {
                throw new LinkerException("Could not find policy for Policy Backed Service. Policy ID: " + policyId);
            }
            pbs.getOperations().put(operation, getPolicyPath(policy, bundle, pbs));
        }
    }
}
