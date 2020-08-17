/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayconfig.bundle.builder;

import com.ca.apim.gateway.cagatewayconfig.beans.*;
import com.ca.apim.gateway.cagatewayconfig.util.IdGenerator;
import com.ca.apim.gateway.cagatewayconfig.util.gateway.MappingActions;
import com.ca.apim.gateway.cagatewayconfig.util.paths.PathUtils;
import com.ca.apim.gateway.cagatewayconfig.util.string.CharacterBlacklistUtil;
import com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentTools;
import com.google.common.collect.ImmutableMap;
import org.jetbrains.annotations.NotNull;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.*;
import java.util.stream.Collectors;

import static com.ca.apim.gateway.cagatewayconfig.bundle.builder.EntityBuilder.BundleType.ENVIRONMENT;
import static com.ca.apim.gateway.cagatewayconfig.util.entity.EntityTypes.SERVICE_TYPE;
import static com.ca.apim.gateway.cagatewayconfig.util.gateway.BuilderUtils.buildAndAppendPropertiesElement;
import static com.ca.apim.gateway.cagatewayconfig.util.gateway.BuilderUtils.insertPrefixToEnvironmentVariable;
import static com.ca.apim.gateway.cagatewayconfig.util.gateway.BundleElementNames.*;
import static com.ca.apim.gateway.cagatewayconfig.util.properties.PropertyConstants.*;
import static com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentUtils.*;
import static java.util.Collections.emptyList;
import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;

@Singleton
public class ServiceEntityBuilder implements EntityBuilder {

    private static final Integer ORDER = 400;
    private final IdGenerator idGenerator;
    private final DocumentTools documentTools;

    @Inject
    ServiceEntityBuilder(DocumentTools documentTools, IdGenerator idGenerator) {
        this.documentTools = documentTools;
        this.idGenerator = idGenerator;
    }

    public List<Entity> build(Bundle bundle, BundleType bundleType, Document document) {
        return buildEntities(bundle.getServices(), bundle, bundleType, document);
    }

    private List<Entity> buildEntities(Map<String, ?> entities, Bundle bundle, BundleType bundleType, Document document) {
        // no service has to be added to environment bundle
        if (bundleType == ENVIRONMENT) {
            return emptyList();
        }

        return entities.entrySet().stream().map(serviceEntry ->
                buildServiceEntity(bundle, (Service) serviceEntry.getValue(), document)
        ).collect(Collectors.toList());
    }

    @NotNull
    @Override
    public Integer getOrder() {
        return ORDER;
    }

    private Entity buildServiceEntity(Bundle bundle, Service service, Document document) {
        AnnotatedEntity annotatedEntity = bundle instanceof AnnotatedBundle ? ((AnnotatedBundle) bundle).getAnnotatedEntity() : null;

        final String serviceOriginalPath = service.getName();
        String servicePathWithTargetFolder = EntityBuilderHelper.getPath(service.getParentFolder(), PathUtils.extractName(service.getName()));
        servicePathWithTargetFolder = CharacterBlacklistUtil.decodePath(servicePathWithTargetFolder);
        String baseName = PathUtils.extractName(servicePathWithTargetFolder);
        String basePath = PathUtils.extractPath(servicePathWithTargetFolder);
        String uniqueName = baseName;
        String uniqueServicePath = servicePathWithTargetFolder;
        boolean isRedeployableBundle = false;
        if (annotatedEntity != null) {
            isRedeployableBundle = annotatedEntity.isRedeployable();
            uniqueName = bundle.applyUniqueName(baseName, BundleType.DEPLOYMENT);
            uniqueServicePath = basePath + uniqueName;
        }
        service.setName(uniqueName);

        Policy policy = bundle.getPolicies().get(service.getPolicy());
        final Set<SoapResource> soapResourceBeans = service.getSoapResources();

        if (isNotEmpty(soapResourceBeans)) {
            soapResourceBeans.forEach(soapResourceBean -> {
                String path = PathUtils.unixPath(service.getParentFolder().getPath(), baseName, soapResourceBean.getFileName());
                String content;
                if (bundle instanceof AnnotatedBundle) {
                    content = ((AnnotatedBundle) bundle).getFullBundle().getSoapResources().get(path).getContent();
                } else {
                    content = bundle.getSoapResources().get(path).getContent();
                }
                soapResourceBean.setContent(content);
            });
        }

        boolean isSoapService = isNotEmpty(soapResourceBeans);
        if (policy == null) {
            throw new EntityBuilderException("Could not find policy for service. Policy Path: " + service.getPolicy());
        }

        if (service.getId() == null) {
            service.setId(idGenerator.generate());
        }
        String id = service.getId();

        Element serviceDetailElement = createElementWithAttributes(document, SERVICE_DETAIL, ImmutableMap.of(ATTRIBUTE_ID, id, ATTRIBUTE_FOLDER_ID, service.getParentFolder().getId()));
        serviceDetailElement.appendChild(createElementWithTextContent(document, NAME, uniqueName));
        serviceDetailElement.appendChild(createElementWithTextContent(document, ENABLED, Boolean.TRUE.toString()));
        serviceDetailElement.appendChild(buildServiceMappings(service, document));

        Map<String, Object> properties = null;

        if (service.getProperties() != null) {
            properties = service.getProperties()
                    .entrySet()
                    .stream()
                    .collect(Collectors
                            .toMap(p -> "property." + p.getKey(),
                                    p -> p.getKey().startsWith(PREFIX_ENV) ? "SERVICE_PROPERTY_" + insertPrefixToEnvironmentVariable(p.getKey(), service.getName()) : p.getValue()));
        }

        if (properties == null) {
            properties = new HashMap<>();
        }

        properties.put(KEY_VALUE_WSS_PROCESSING_ENABLED, false);
        if (isSoapService) {
            properties.put(KEY_VALUE_SOAP, true);
            properties.put(KEY_VALUE_SOAP_VERSION, service.getSoapVersion());
            properties.put(KEY_VALUE_WSS_PROCESSING_ENABLED, service.isWssProcessingEnabled());
        }

        buildAndAppendPropertiesElement(properties, document, serviceDetailElement);

        Element serviceElement = createElementWithAttribute(document, SERVICE, ATTRIBUTE_ID, id);
        serviceElement.appendChild(serviceDetailElement);

        Element resourcesElement = document.createElement(RESOURCES);
        Element policyResourceSetElement = createElementWithAttribute(document, RESOURCE_SET, ATTRIBUTE_TAG, TAG_VALUE_POLICY);
        Element policyResourceElement = createElementWithAttribute(document, RESOURCE, ATTRIBUTE_TYPE, TAG_VALUE_POLICY);
        policyResourceElement.setTextContent(documentTools.elementToString(policy.getPolicyDocument()));
        policyResourceSetElement.appendChild(policyResourceElement);
        resourcesElement.appendChild(policyResourceSetElement);

        if (isSoapService) {
            Element wsdlResourceSetElement = createElementWithAttributes(document, RESOURCE_SET, ImmutableMap.of(ATTRIBUTE_TAG, TAG_VALUE_WSDL, ATTRIBUTE_ROOT_URL, service.getWsdlRootUrl()));
            soapResourceBeans.forEach(soapResourceBean -> {
                Element resourceElement = createElementWithAttributes(document, RESOURCE, ImmutableMap.of(ATTRIBUTE_TYPE, soapResourceBean.getType(), ATTRIBUTE_SOURCE_URL, soapResourceBean.getRootUrl()));
                resourceElement.setTextContent(soapResourceBean.getContent());
                wsdlResourceSetElement.appendChild(resourceElement);
            });
            resourcesElement.appendChild(wsdlResourceSetElement);
        }

        serviceElement.appendChild(resourcesElement);
        Entity entity = EntityBuilderHelper.getEntityWithPathMapping(SERVICE_TYPE, serviceOriginalPath, uniqueServicePath, id,
                serviceElement, policy.isHasRouting(), service);
        if (isRedeployableBundle) {
            entity.setMappingAction(MappingActions.NEW_OR_UPDATE);
        } else {
            entity.setMappingAction(EntityBuilderHelper.getDefaultEntityMappingAction());
        }

        return entity;
    }

    private Element buildServiceMappings(Service service, Document document) {
        Element serviceMappingsElement = document.createElement(SERVICE_MAPPINGS);
        Element httpMappingElement = document.createElement(HTTP_MAPPING);
        serviceMappingsElement.appendChild(httpMappingElement);

        if (service.getUrl() != null) {
            httpMappingElement.appendChild(createElementWithTextContent(document, URL_PATTERN, service.getUrl()));
        }
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
