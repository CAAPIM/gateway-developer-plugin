/*
 * Copyright (c) 2018. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayexport.tasks.explode.linker;

import com.ca.apim.gateway.cagatewayconfig.tasks.zip.beans.Bundle;
import com.ca.apim.gateway.cagatewayconfig.tasks.zip.beans.Policy;
import com.ca.apim.gateway.cagatewayconfig.tasks.zip.beans.ScheduledTask;

import javax.inject.Singleton;

import static com.ca.apim.gateway.cagatewayexport.tasks.explode.linker.PolicyLinker.getPolicyPath;

@Singleton
public class ScheduledTaskLinker implements EntityLinker<ScheduledTask> {
    @Override
    public Class<ScheduledTask> getEntityClass() {
        return ScheduledTask.class;
    }

    @Override
    public void link(ScheduledTask scheduledTaskEntity, Bundle bundle, Bundle targetBundle) {
        Policy policy = bundle.getEntities(Policy.class).get(scheduledTaskEntity.getPolicy());
        if (policy == null) {
            throw new LinkerException("Could not find policy for Scheduled Task: " + scheduledTaskEntity.getName() + ". Policy ID: " + scheduledTaskEntity.getPolicy());
        }
        scheduledTaskEntity.setPolicy(getPolicyPath(policy, bundle, scheduledTaskEntity));
    }
}
