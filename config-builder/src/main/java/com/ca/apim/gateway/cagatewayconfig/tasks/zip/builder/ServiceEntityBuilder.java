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
import com.google.common.collect.ImmutableMap;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.List;
import java.util.stream.Collectors;

import static com.ca.apim.gateway.cagatewayconfig.util.entity.EntityTypes.SERVICE_TYPE;
import static com.ca.apim.gateway.cagatewayconfig.util.gateway.BuilderUtils.buildAndAppendPropertiesElement;
import static com.ca.apim.gateway.cagatewayconfig.util.gateway.BundleElementNames.*;
import static com.ca.apim.gateway.cagatewayconfig.util.properties.PropertyConstants.PREFIX_ENV;
import static com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentUtils.*;

public class ServiceEntityBuilder implements EntityBuilder {
    private final Document document;
    private final IdGenerator idGenerator;
    private final DocumentFileUtils documentFileUtils;

    ServiceEntityBuilder(DocumentFileUtils documentFileUtils, Document document, IdGenerator idGenerator) {
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
        Policy policy = bundle.getPolicies().get(policyPath);
        if (policy == null) {
            throw new EntityBuilderException("Could not find policy for service. Policy Path: " + policyPath);
        }
        String id = idGenerator.generate();
        service.setId(id);

        Element serviceDetailElement = createElementWithAttributes(document, SERVICE_DETAIL, ImmutableMap.of(ATTRIBUTE_ID, id, ATTRIBUTE_FOLDER_ID, policy.getParentFolder().getId()));
        serviceDetailElement.appendChild(createElementWithTextContent(document, NAME, policy.getName()));
        serviceDetailElement.appendChild(createElementWithTextContent(document, ENABLED, Boolean.TRUE.toString()));
        serviceDetailElement.appendChild(buildServiceMappings(service));

        if (service.getProperties() != null) {
            buildAndAppendPropertiesElement(service.getProperties()
                            .entrySet()
                            .stream()
                            .collect(Collectors
                                    .toMap(p -> "property." + p.getKey(), p -> p.getKey().startsWith(PREFIX_ENV) ? "SERVICE_PROPERTY_" + p.getKey() : p.getValue())),
                    document, serviceDetailElement);
        }

        Element serviceElement = createElementWithAttribute(document, SERVICE, ATTRIBUTE_ID, id);
        serviceElement.appendChild(serviceDetailElement);

        Element resourcesElement = document.createElement(RESOURCES);
        Element resourceSetElement = createElementWithAttribute(document, RESOURCE_SET, "tag", "policy");
        Element resourceElement = createElementWithAttribute(document, RESOURCE, "type", "policy");
        resourceElement.setTextContent(documentFileUtils.elementToString(policy.getPolicyDocument()));

        resourceSetElement.appendChild(resourceElement);
        resourcesElement.appendChild(resourceSetElement);
        serviceElement.appendChild(resourcesElement);
        return new Entity(SERVICE_TYPE, policy.getName(), id, serviceElement);
    }

    private Element buildServiceMappings(Service service) {
        Element serviceMappingsElement = document.createElement(SERVICE_MAPPINGS);
        Element httpMappingElement = document.createElement(HTTP_MAPPING);
        serviceMappingsElement.appendChild(httpMappingElement);

        httpMappingElement.appendChild(createElementWithTextContent(document, URL_PATTERN, service.getUrl()));
        Element verbsElement = document.createElement(VERBS);
        service.getHttpMethods().forEach(method -> {
            Element verb = document.createElement(VERB);
            verb.setTextContent(method);
            verbsElement.appendChild(verb);
        });
        httpMappingElement.appendChild(verbsElement);
        return serviceMappingsElement;
    }
}
