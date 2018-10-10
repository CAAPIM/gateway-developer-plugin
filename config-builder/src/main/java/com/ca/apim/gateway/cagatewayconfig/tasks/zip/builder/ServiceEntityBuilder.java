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

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;
import java.util.stream.Collectors;

import static com.ca.apim.gateway.cagatewayconfig.util.entity.EntityTypes.SERVICE_TYPE;
import static com.ca.apim.gateway.cagatewayconfig.util.gateway.BuilderUtils.buildAndAppendPropertiesElement;
import static com.ca.apim.gateway.cagatewayconfig.util.gateway.BundleElementNames.*;
import static com.ca.apim.gateway.cagatewayconfig.util.properties.PropertyConstants.PREFIX_ENV;
import static com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentUtils.*;

@Singleton
public class ServiceEntityBuilder implements EntityBuilder {

    private static final Integer ORDER = 400;
    private final IdGenerator idGenerator;
    private final DocumentFileUtils documentFileUtils;

    @Inject
    ServiceEntityBuilder(DocumentFileUtils documentFileUtils, IdGenerator idGenerator) {
        this.documentFileUtils = documentFileUtils;
        this.idGenerator = idGenerator;
    }

    public List<Entity> build(Bundle bundle, BundleType bundleType, Document document) {
        return bundle.getServices().entrySet().stream().map(serviceEntry ->
                buildServiceEntity(bundle, serviceEntry.getKey(), serviceEntry.getValue(), document)
        ).collect(Collectors.toList());
    }

    @Override
    public Integer getOrder() {
        return ORDER;
    }

    private Entity buildServiceEntity(Bundle bundle, String policyPath, Service service, Document document) {
        Policy policy = bundle.getPolicies().get(policyPath);
        if (policy == null) {
            throw new EntityBuilderException("Could not find policy for service. Policy Path: " + policyPath);
        }
        String id = idGenerator.generate();
        service.setId(id);

        Element serviceDetailElement = createElementWithAttributes(document, SERVICE_DETAIL, ImmutableMap.of(ATTRIBUTE_ID, id, ATTRIBUTE_FOLDER_ID, policy.getParentFolder().getId()));
        serviceDetailElement.appendChild(createElementWithTextContent(document, NAME, policy.getName()));
        serviceDetailElement.appendChild(createElementWithTextContent(document, ENABLED, Boolean.TRUE.toString()));
        serviceDetailElement.appendChild(buildServiceMappings(service, document));

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

    private Element buildServiceMappings(Service service, Document document) {
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
