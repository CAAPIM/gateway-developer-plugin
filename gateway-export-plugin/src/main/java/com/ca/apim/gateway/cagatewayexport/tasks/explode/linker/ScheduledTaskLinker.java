/*
 * Copyright (c) 2018. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayexport.tasks.explode.linker;

import com.ca.apim.gateway.cagatewayexport.tasks.explode.bundle.Bundle;
import com.ca.apim.gateway.cagatewayexport.tasks.explode.bundle.entity.Folder;
import com.ca.apim.gateway.cagatewayexport.tasks.explode.bundle.entity.PolicyEntity;
import com.ca.apim.gateway.cagatewayexport.tasks.explode.bundle.entity.ScheduledTaskEntity;

import javax.inject.Singleton;
import java.nio.file.Path;
import java.nio.file.Paths;

@Singleton
public class ScheduledTaskLinker implements EntityLinker<ScheduledTaskEntity> {
    @Override
    public Class<ScheduledTaskEntity> getEntityClass() {
        return ScheduledTaskEntity.class;
    }

    @Override
    public void link(ScheduledTaskEntity scheduledTaskEntity, Bundle bundle, Bundle targetBundle) {
        scheduledTaskEntity.setPolicyPath(getPolicyPath(bundle, scheduledTaskEntity));
    }

    private String getPolicyPath(Bundle bundle, ScheduledTaskEntity scheduledTaskEntity) {
        PolicyEntity policy = bundle.getEntities(PolicyEntity.class).get(scheduledTaskEntity.getPolicyId());
        if (policy == null) {
            throw new LinkerException("Could not find policy for Scheduled Task: " + scheduledTaskEntity.getName() + ". Policy ID: " + scheduledTaskEntity.getPolicyId());
        }
        Folder folder = bundle.getFolderTree().getFolderById(policy.getFolderId());
        if (folder == null) {
            throw new LinkerException("Could not find folder for Scheduled Task policy. Encapsulated Assertion: " + scheduledTaskEntity.getName() + ". Policy Name:ID: " + policy.getName() + ":" + policy.getId() + ". Folder Id: " + policy.getFolderId());
        }
        Path folderPath = bundle.getFolderTree().getPath(folder);
        return Paths.get(folderPath.toString(), policy.getName() + ".xml").toString();
    }
}
