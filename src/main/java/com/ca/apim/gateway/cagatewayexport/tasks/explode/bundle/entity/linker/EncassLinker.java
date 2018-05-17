/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayexport.tasks.explode.bundle.entity.linker;

import com.ca.apim.gateway.cagatewayexport.tasks.explode.bundle.Bundle;
import com.ca.apim.gateway.cagatewayexport.tasks.explode.bundle.entity.EncassEntity;
import com.ca.apim.gateway.cagatewayexport.tasks.explode.bundle.entity.Folder;
import com.ca.apim.gateway.cagatewayexport.tasks.explode.bundle.entity.PolicyEntity;
import com.ca.apim.gateway.cagatewayexport.tasks.explode.bundle.entity.ServiceEntity;
import com.ca.apim.gateway.cagatewayexport.tasks.explode.writer.WriteException;
import com.ca.apim.gateway.cagatewayexport.tasks.explode.writer.WriterHelper;
import com.ca.apim.gateway.cagatewayexport.tasks.explode.writer.beans.Encass;
import com.ca.apim.gateway.cagatewayexport.util.xml.DocumentParseException;
import com.ca.apim.gateway.cagatewayexport.util.xml.DocumentTools;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.stream.Collectors;

public class EncassLinker implements EntityLinker<EncassEntity> {
    public EncassLinker() {
    }

    public void link(Bundle filteredBundle, Bundle bundle) {
        filteredBundle.getEntities(EncassEntity.class).values().forEach(e -> link(e, bundle));
    }

    private void link(EncassEntity encass, Bundle bundle) {
        encass.setPath(getEncassPath(bundle, encass));
    }


    private String getEncassPath(Bundle bundle, EncassEntity encassEntity) {
        PolicyEntity policy = bundle.getEntities(PolicyEntity.class).get(encassEntity.getPolicyId());
        Folder folder = bundle.getFolderTree().getFolderById(policy.getFolderId());
        Path folderPath = bundle.getFolderTree().getPath(folder);
        return Paths.get(folderPath.toString(), policy.getName() + ".xml").toString();
    }
}
