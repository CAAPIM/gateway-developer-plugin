/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayexport.tasks.explode.writer;

import com.ca.apim.gateway.cagatewayexport.tasks.explode.bundle.Bundle;
import com.ca.apim.gateway.cagatewayexport.tasks.explode.bundle.entity.ServiceEntity;
import com.ca.apim.gateway.cagatewayexport.tasks.explode.writer.beans.Service;
import com.ca.apim.gateway.cagatewayexport.util.file.DocumentFileUtils;
import com.ca.apim.gateway.cagatewayexport.util.json.JsonTools;
import org.jetbrains.annotations.NotNull;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static com.ca.apim.gateway.cagatewayexport.tasks.explode.bundle.BundleElementNames.*;
import static com.ca.apim.gateway.cagatewayexport.util.xml.DocumentUtils.getSingleChildElement;

public class ServiceWriter implements EntityWriter {

    private static final String SERVICES_FILE = "services";
    private final DocumentFileUtils documentFileUtils;
    private final JsonTools jsonTools;

    ServiceWriter(DocumentFileUtils documentFileUtils, JsonTools jsonTools) {
        this.documentFileUtils = documentFileUtils;
        this.jsonTools = jsonTools;
    }

    @Override
    public void write(Bundle bundle, File rootFolder) {
        Map<String, Service> serviceBeans = bundle.getEntities(ServiceEntity.class)
                .values()
                .stream()
                .collect(Collectors.toMap(ServiceEntity::getPath, this::getServiceBean));

        WriterHelper.writeFile(rootFolder, documentFileUtils, jsonTools, serviceBeans, SERVICES_FILE + jsonTools.getFileExtension());
    }

    @NotNull
    private Service getServiceBean(ServiceEntity serviceEntity) {
        Service serviceBean = new Service();
        Element serviceMappingsElement = getSingleChildElement(serviceEntity.getServiceDetailsElement(), SERVICE_MAPPINGS);
        Element httpMappingElement = getSingleChildElement(serviceMappingsElement, HTTP_MAPPING);
        Element urlPatternElement = getSingleChildElement(httpMappingElement, URL_PATTERN);
        serviceBean.setUrl(urlPatternElement.getTextContent());

        Element verbsElement = getSingleChildElement(httpMappingElement, VERBS);
        NodeList verbs = verbsElement.getElementsByTagName(VERB);
        Set<String> httpMethods = new HashSet<>();
        for (int i = 0; i < verbs.getLength(); i++) {
            httpMethods.add(verbs.item(i).getTextContent());
        }
        serviceBean.setHttpMethods(httpMethods);

        Element servicePropertiesElement = getSingleChildElement(serviceEntity.getServiceDetailsElement(), PROPERTIES);
        NodeList propertyNodes = servicePropertiesElement.getElementsByTagName(PROPERTY);
        Map<String, String> properties = new HashMap<>();
        for (int i = 0; i < propertyNodes.getLength(); i++) {
            if (propertyNodes.item(i).getAttributes().getNamedItem("key").getTextContent().startsWith("property.")) {
                String propertyValue = null;
                if (!propertyNodes.item(i).getAttributes().getNamedItem("key").getTextContent().startsWith("property.ENV.")) {
                    propertyValue = getSingleChildElement((Element) propertyNodes.item(i), STRING_VALUE).getTextContent();
                }
                properties.put(propertyNodes.item(i).getAttributes().getNamedItem("key").getTextContent().substring(9), propertyValue);
            }
        }
        serviceBean.setProperties(properties);

        return serviceBean;
    }
}
