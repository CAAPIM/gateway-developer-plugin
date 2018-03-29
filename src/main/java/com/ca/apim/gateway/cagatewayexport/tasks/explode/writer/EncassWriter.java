/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayexport.tasks.explode.writer;

import com.ca.apim.gateway.cagatewayexport.tasks.explode.bundle.Bundle;
import com.ca.apim.gateway.cagatewayexport.tasks.explode.bundle.entity.EncassEntity;
import com.ca.apim.gateway.cagatewayexport.tasks.explode.bundle.entity.Folder;
import com.ca.apim.gateway.cagatewayexport.tasks.explode.bundle.entity.PolicyEntity;
import com.ca.apim.gateway.cagatewayexport.tasks.explode.bundle.entity.loader.EntityLoaderHelper;
import com.ca.apim.gateway.cagatewayexport.tasks.explode.writer.beans.Encass;
import com.ca.apim.gateway.cagatewayexport.tasks.explode.writer.beans.EncassParam;
import com.ca.apim.gateway.cagatewayexport.util.file.DocumentFileUtils;
import com.ca.apim.gateway.cagatewayexport.util.json.JsonTools;
import com.fasterxml.jackson.databind.ObjectWriter;
import org.jetbrains.annotations.NotNull;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class EncassWriter implements EntityWriter {
    private final DocumentFileUtils documentFileUtils;
    private final JsonTools jsonTools;

    public EncassWriter(DocumentFileUtils documentFileUtils, JsonTools jsonTools) {
        this.documentFileUtils = documentFileUtils;
        this.jsonTools = jsonTools;
    }

    @Override
    public void write(Bundle bundle, File rootFolder) {
        File configFolder = new File(rootFolder, "config");
        documentFileUtils.createFolder(configFolder.toPath());

        Map<String, EncassEntity> encasses = bundle.getEntities(EncassEntity.class);

        Map<String, Encass> encassBeans = encasses.values().stream().collect(Collectors.toMap(e -> getEncassPath(bundle, e), this::getEncassBean));

        File servicesFile = new File(configFolder, "encass.yml");

        ObjectWriter yamlWriter = jsonTools.getObjectWriter(JsonTools.YAML);
        try (OutputStream fileStream = Files.newOutputStream(servicesFile.toPath())) {
            yamlWriter.writeValue(fileStream, encassBeans);
        } catch (IOException e) {
            throw new WriteException("Exception writing encasses config file", e);
        }
    }

    private String getEncassPath(Bundle bundle, EncassEntity encassEntity) {
        PolicyEntity policy = bundle.getEntities(PolicyEntity.class).get(encassEntity.getPolicyId());
        Folder folder = bundle.getFolderTree().getFolderById(policy.getFolderId());
        Path folderPath = bundle.getFolderTree().getPath(folder);
        return Paths.get(folderPath.toString(), policy.getName() + ".xml").toString();
    }

    @NotNull
    private Encass getEncassBean(EncassEntity encassEntity) {
        Encass encassBean = new Encass();
        Element encapsulatedArgumentsElement = EntityLoaderHelper.getSingleElement(encassEntity.getXml(), "l7:EncapsulatedArguments");
        NodeList encapsulatedAssertionArgumentElement = encapsulatedArgumentsElement.getElementsByTagName("l7:EncapsulatedAssertionArgument");
        List<EncassParam> encassArguments = new ArrayList<>(encapsulatedAssertionArgumentElement.getLength());
        for (int i = 0; i < encapsulatedAssertionArgumentElement.getLength(); i++) {
            if (!(encapsulatedAssertionArgumentElement.item(i) instanceof Element)) {
                throw new WriteException("Unexpected encass argument node: " + encapsulatedArgumentsElement.getClass());
            }
            Element argumentNameElement = EntityLoaderHelper.getSingleElement((Element) encapsulatedAssertionArgumentElement.item(i), "l7:ArgumentName");
            Element argumentTypeElement = EntityLoaderHelper.getSingleElement((Element) encapsulatedAssertionArgumentElement.item(i), "l7:ArgumentType");
            encassArguments.add(new EncassParam(argumentNameElement.getTextContent(), argumentTypeElement.getTextContent()));
        }
        encassBean.setArguments(encassArguments);

        Element encapsulatedResultsElement = EntityLoaderHelper.getSingleElement(encassEntity.getXml(), "l7:EncapsulatedResults");
        NodeList encapsulatedAssertionResultElement = encapsulatedResultsElement.getElementsByTagName("l7:EncapsulatedAssertionResult");
        List<EncassParam> encassResults = new ArrayList<>(encapsulatedAssertionResultElement.getLength());
        for (int i = 0; i < encapsulatedAssertionResultElement.getLength(); i++) {
            if (!(encapsulatedAssertionResultElement.item(i) instanceof Element)) {
                throw new WriteException("Unexpected encass results node: " + encapsulatedResultsElement.getClass());
            }
            Element resultNameElement = EntityLoaderHelper.getSingleElement((Element) encapsulatedAssertionResultElement.item(i), "l7:ResultName");
            Element resultTypeElement = EntityLoaderHelper.getSingleElement((Element) encapsulatedAssertionResultElement.item(i), "l7:ResultType");
            encassResults.add(new EncassParam(resultNameElement.getTextContent(), resultTypeElement.getTextContent()));
        }
        encassBean.setResults(encassResults);
        return encassBean;
    }
}
