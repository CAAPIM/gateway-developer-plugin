/*
 * Copyright (c) 2018. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayexport.tasks.explode.linker;

import com.ca.apim.gateway.cagatewayexport.tasks.explode.bundle.Bundle;
import com.ca.apim.gateway.cagatewayexport.tasks.explode.bundle.entity.PolicyEntity;
import com.ca.apim.gateway.cagatewayexport.tasks.explode.bundle.entity.ScheduledTaskEntity;

import javax.inject.Singleton;

import static com.ca.apim.gateway.cagatewayexport.tasks.explode.linker.PolicyLinker.getPolicyPath;

@Singleton
public class ScheduledTaskLinker implements EntityLinker<ScheduledTaskEntity> {
    @Override
    public Class<ScheduledTaskEntity> getEntityClass() {
        return ScheduledTaskEntity.class;
    }

    @Override
    public void link(ScheduledTaskEntity scheduledTaskEntity, Bundle bundle, Bundle targetBundle) {
        PolicyEntity policy = bundle.getEntities(PolicyEntity.class).get(scheduledTaskEntity.getPolicyId());
        if (policy == null) {
            throw new LinkerException("Could not find policy for Scheduled Task: " + scheduledTaskEntity.getName() + ". Policy ID: " + scheduledTaskEntity.getPolicyId());
        }
        scheduledTaskEntity.setPolicyPath(getPolicyPath(policy, bundle, scheduledTaskEntity));
    }
}
