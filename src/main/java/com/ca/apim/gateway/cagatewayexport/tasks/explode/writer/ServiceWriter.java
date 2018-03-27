/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayexport.tasks.explode.writer;

import com.ca.apim.gateway.cagatewayexport.tasks.explode.bundle.Bundle;
import com.ca.apim.gateway.cagatewayexport.tasks.explode.bundle.entity.Folder;
import com.ca.apim.gateway.cagatewayexport.tasks.explode.bundle.entity.ServiceEntity;
import com.ca.apim.gateway.cagatewayexport.tasks.explode.bundle.entity.loader.EntityLoaderHelper;
import com.ca.apim.gateway.cagatewayexport.tasks.explode.writer.beans.Service;
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
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class ServiceWriter implements EntityWriter {
    private final DocumentFileUtils documentFileUtils;
    private final JsonTools jsonTools;

    public ServiceWriter(DocumentFileUtils documentFileUtils, JsonTools jsonTools) {
        this.documentFileUtils = documentFileUtils;
        this.jsonTools = jsonTools;
    }

    @Override
    public void write(Bundle bundle, File rootFolder) {
        File configFolder = new File(rootFolder, "config");
        documentFileUtils.createFolder(configFolder.toPath());

        Map<String, ServiceEntity> services = bundle.getEntities(ServiceEntity.class);

        Map<String, Service> serviceBeans = services.values().stream().collect(Collectors.toMap(s -> getServicePath(bundle, s), this::getServiceBean));

        File servicesFile = new File(configFolder, "services.yml");

        ObjectWriter yamlWriter = jsonTools.getObjectWriter(JsonTools.YAML);
        try (OutputStream fileStream = Files.newOutputStream(servicesFile.toPath())) {
            yamlWriter.writeValue(fileStream, serviceBeans);
        } catch (IOException e) {
            throw new WriteException("Exception writing services config file", e);
        }
    }

    private String getServicePath(Bundle bundle, ServiceEntity serviceEntity) {
        Folder folder = bundle.getFolderTree().getFolderById(serviceEntity.getFolderId());
        Path folderPath = bundle.getFolderTree().getPath(folder);
        return Paths.get(folderPath.toString(), serviceEntity.getName() + ".xml").toString();
    }

    @NotNull
    private Service getServiceBean(ServiceEntity serviceEntity) {
        Service serviceBean = new Service();
        Element serviceMappingsElement = EntityLoaderHelper.getSingleElement(serviceEntity.getServiceDetailsElement(), "l7:ServiceMappings");
        Element httpMappingElement = EntityLoaderHelper.getSingleElement(serviceMappingsElement, "l7:HttpMapping");
        Element urlPatternElement = EntityLoaderHelper.getSingleElement(httpMappingElement, "l7:UrlPattern");
        serviceBean.setUrl(urlPatternElement.getTextContent());

        Element verbsElement = EntityLoaderHelper.getSingleElement(httpMappingElement, "l7:Verbs");
        NodeList verbs = verbsElement.getElementsByTagName("l7:Verb");
        Set<String> httpMethods = new HashSet<>();
        for (int i = 0; i < verbs.getLength(); i++) {
            httpMethods.add(verbs.item(i).getTextContent());
        }
        serviceBean.setHttpMethods(httpMethods);
        return serviceBean;
    }
}
