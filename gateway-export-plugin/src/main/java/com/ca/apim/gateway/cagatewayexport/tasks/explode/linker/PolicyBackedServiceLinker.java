/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayexport.tasks.explode.linker;

import com.ca.apim.gateway.cagatewayconfig.tasks.zip.beans.*;

import javax.inject.Singleton;

import static com.ca.apim.gateway.cagatewayexport.tasks.explode.linker.PolicyLinker.getPolicyPath;

@Singleton
public class PolicyBackedServiceLinker implements EntityLinker<PolicyBackedService> {

    @Override
    public Class<PolicyBackedService> getEntityClass() {
        return PolicyBackedService.class;
    }

    @Override
    public void link(PolicyBackedService pbs, Bundle bundle, Bundle targetBundle) {
        for (PolicyBackedServiceOperation operation : pbs.getOperations()) {
            String policyId = operation.getPolicy();
            Policy policy = bundle.getPolicies().get(policyId);
            if (policy == null) {
                throw new LinkerException("Could not find policy for Policy Backed Service. Policy ID: " + policyId);
            }
            operation.setPolicy(getPolicyPath(policy, bundle, pbs));
        }
    }
}
