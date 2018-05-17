/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayexport.tasks.explode.writer;

import com.ca.apim.gateway.cagatewayexport.tasks.explode.bundle.Bundle;
import com.ca.apim.gateway.cagatewayexport.tasks.explode.bundle.entity.EncassEntity;
import com.ca.apim.gateway.cagatewayexport.tasks.explode.writer.beans.Encass;
import com.ca.apim.gateway.cagatewayexport.tasks.explode.writer.beans.EncassParam;
import com.ca.apim.gateway.cagatewayexport.util.file.DocumentFileUtils;
import com.ca.apim.gateway.cagatewayexport.util.json.JsonTools;
import com.fasterxml.jackson.databind.ObjectWriter;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.Map;
import java.util.stream.Collectors;

public class EncassWriter implements EntityWriter {
    private final DocumentFileUtils documentFileUtils;
    private final JsonTools jsonTools;

    EncassWriter(DocumentFileUtils documentFileUtils, JsonTools jsonTools) {
        this.documentFileUtils = documentFileUtils;
        this.jsonTools = jsonTools;
    }

    @Override
    public void write(Bundle bundle, File rootFolder) {
        File configFolder = new File(rootFolder, "config");
        documentFileUtils.createFolder(configFolder.toPath());

        Map<String, EncassEntity> encasses = bundle.getEntities(EncassEntity.class);

        Map<String, Encass> encassBeans = encasses.values().stream().collect(Collectors.toMap(EncassEntity::getPath, this::getEncassBean));

        File servicesFile = new File(configFolder, "encass.yml");

        ObjectWriter yamlWriter = jsonTools.getObjectWriter(JsonTools.YAML);
        try (OutputStream fileStream = Files.newOutputStream(servicesFile.toPath())) {
            yamlWriter.writeValue(fileStream, encassBeans);
        } catch (IOException e) {
            throw new WriteException("Exception writing encasses config file", e);
        }
    }

    @NotNull
    private Encass getEncassBean(EncassEntity encassEntity) {
        Encass encassBean = new Encass();
        encassBean.setArguments(encassEntity.getArguments().stream().map(encassParam -> new EncassParam(encassParam.getName(), encassParam.getType())).collect(Collectors.toList()));
        encassBean.setResults(encassEntity.getResults().stream().map(encassParam -> new EncassParam(encassParam.getName(), encassParam.getType())).collect(Collectors.toList()));
        return encassBean;
    }
}
