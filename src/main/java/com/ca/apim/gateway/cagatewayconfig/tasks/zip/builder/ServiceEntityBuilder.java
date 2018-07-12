/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayconfig.tasks.zip.builder;

import com.ca.apim.gateway.cagatewayconfig.tasks.zip.beans.Bundle;
import com.ca.apim.gateway.cagatewayconfig.tasks.zip.beans.Policy;
import com.ca.apim.gateway.cagatewayconfig.tasks.zip.beans.Service;
import com.ca.apim.gateway.cagatewayconfig.util.IdGenerator;
import com.ca.apim.gateway.cagatewayconfig.util.file.DocumentFileUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ServiceEntityBuilder implements EntityBuilder {
    private final Document document;
    private final IdGenerator idGenerator;
    private final DocumentFileUtils documentFileUtils;

    public ServiceEntityBuilder(DocumentFileUtils documentFileUtils, Document document, IdGenerator idGenerator) {
        this.documentFileUtils = documentFileUtils;
        this.document = document;
        this.idGenerator = idGenerator;
    }

    public List<Entity> build(Bundle bundle) {
        return bundle.getServices().entrySet().stream().map(serviceEntry ->
                buildServiceEntity(bundle, serviceEntry.getKey(), serviceEntry.getValue())
        ).collect(Collectors.toList());
    }

    private Entity buildServiceEntity(Bundle bundle, String policyPath, Service service) {
        Element serviceDetailElement = document.createElement("l7:ServiceDetail");

        Policy policy = bundle.getPolicies().get(policyPath);
        if (policy == null) {
            throw new EntityBuilderException("Could not find policy for service. Policy Path: " + policyPath);
        }
        String id = idGenerator.generate();
        serviceDetailElement.setAttribute("id", id);
        serviceDetailElement.setAttribute("folderId", policy.getParentFolder().getId());
        Element nameElement = document.createElement("l7:Name");
        nameElement.setTextContent(policy.getName());
        serviceDetailElement.appendChild(nameElement);

        Element enabledElement = document.createElement("l7:Enabled");
        enabledElement.setTextContent("true");
        serviceDetailElement.appendChild(enabledElement);
        serviceDetailElement.appendChild(buildServiceMappings(service));

        if( service.getProperties() != null) {
            serviceDetailElement.appendChild(buildProperties(service));
        }

        Element serviceElement = document.createElement("l7:Service");
        serviceElement.setAttribute("id", id);
        serviceElement.appendChild(serviceDetailElement);

        Element resourcesElement = document.createElement("l7:Resources");

        Element resourceSetElement = document.createElement("l7:ResourceSet");
        resourceSetElement.setAttribute("tag", "policy");

        Element resourceElement = document.createElement("l7:Resource");
        resourceElement.setAttribute("type", "policy");

        resourceElement.setTextContent(documentFileUtils.elementToString(policy.getPolicyDocument()));

        resourceSetElement.appendChild(resourceElement);
        resourcesElement.appendChild(resourceSetElement);
        serviceElement.appendChild(resourcesElement);
        return new Entity("SERVICE", policy.getName(), id, serviceElement);
    }

    private Element buildServiceMappings(Service service) {
        Element serviceMappingsElement = document.createElement("l7:ServiceMappings");
        Element httpMappingElement = document.createElement("l7:HttpMapping");
        serviceMappingsElement.appendChild(httpMappingElement);
        Element urlPatternElement = document.createElement("l7:UrlPattern");
        urlPatternElement.setTextContent(service.getUrl());
        httpMappingElement.appendChild(urlPatternElement);
        Element verbsElement = document.createElement("l7:Verbs");
        service.getHttpMethods().forEach(method -> {
            Element verb = document.createElement("l7:Verb");
            verb.setTextContent(method);
            verbsElement.appendChild(verb);
        });
        httpMappingElement.appendChild(verbsElement);
        return serviceMappingsElement;
    }

    private Element buildProperties(Service service) {
        Map<String,Object> properties = new HashMap<>();
        for (String key: service.getProperties().keySet()) {
            properties.put(key,service.getProperties().get(key));
        }
        return BuilderUtils.buildPropertiesElement(properties, document);
    }

}
