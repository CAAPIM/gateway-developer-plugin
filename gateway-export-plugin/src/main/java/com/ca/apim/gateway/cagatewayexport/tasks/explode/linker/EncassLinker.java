/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayexport.tasks.explode.linker;

import com.ca.apim.gateway.cagatewayexport.tasks.explode.bundle.Bundle;
import com.ca.apim.gateway.cagatewayexport.tasks.explode.bundle.entity.EncassEntity;
import com.ca.apim.gateway.cagatewayexport.tasks.explode.bundle.entity.PolicyEntity;

import javax.inject.Singleton;

import static com.ca.apim.gateway.cagatewayexport.tasks.explode.linker.PolicyLinker.getPolicyPath;

@Singleton
public class EncassLinker implements EntityLinker<EncassEntity> {
    @Override
    public Class<EncassEntity> getEntityClass() {
        return EncassEntity.class;
    }

    @Override
    public void link(EncassEntity encass, Bundle bundle, Bundle targetBundle) {
        PolicyEntity policy = bundle.getEntities(PolicyEntity.class).get(encass.getPolicyId());
        if (policy == null) {
            throw new LinkerException("Could not find policy for Encapsulated Assertion: " + encass.getName() + ". Policy ID: " + encass.getPolicyId());
        }
        encass.setPath(getPolicyPath(policy, bundle, encass));
    }
}
