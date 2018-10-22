/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayexport.tasks.explode.linker;

import com.ca.apim.gateway.cagatewayexport.tasks.explode.bundle.Bundle;
import com.ca.apim.gateway.cagatewayexport.tasks.explode.bundle.entity.EncassEntity;
import com.ca.apim.gateway.cagatewayexport.tasks.explode.bundle.entity.Folder;
import com.ca.apim.gateway.cagatewayexport.tasks.explode.bundle.entity.PolicyEntity;

import javax.inject.Singleton;
import java.nio.file.Path;
import java.nio.file.Paths;

@Singleton
public class EncassLinker implements EntityLinker<EncassEntity> {
    @Override
    public Class<EncassEntity> getEntityClass() {
        return EncassEntity.class;
    }

    @Override
    public void link(EncassEntity encass, Bundle bundle, Bundle targetBundle) {
        encass.setPath(getEncassPath(bundle, encass));
    }

    private String getEncassPath(Bundle bundle, EncassEntity encassEntity) {
        PolicyEntity policy = bundle.getEntities(PolicyEntity.class).get(encassEntity.getPolicyId());
        if (policy == null) {
            throw new LinkerException("Could not find policy for Encapsulated Assertion: " + encassEntity.getName() + ". Policy ID: " + encassEntity.getPolicyId());
        }
        Folder folder = bundle.getFolderTree().getFolderById(policy.getFolderId());
        if (folder == null) {
            throw new LinkerException("Could not find folder for Encapsulated Assertion policy. Encapsulated Assertion: " + encassEntity.getName() + ". Policy Name:ID: " + policy.getName() + ":" + policy.getId() + ". Folder Id: " + policy.getFolderId());
        }
        Path folderPath = bundle.getFolderTree().getPath(folder);
        return Paths.get(folderPath.toString(), policy.getName() + ".xml").toString();
    }
}
