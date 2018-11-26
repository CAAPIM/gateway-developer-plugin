/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayconfig.bundle.loader;

import com.ca.apim.gateway.cagatewayconfig.beans.Bundle;
import com.ca.apim.gateway.cagatewayconfig.beans.Folder;
import com.ca.apim.gateway.cagatewayconfig.beans.Service;
import com.ca.apim.gateway.cagatewayconfig.beans.WSDL;
import com.ca.apim.gateway.cagatewayconfig.util.entity.EntityTypes;
import com.ca.apim.gateway.cagatewayconfig.util.gateway.BuilderUtils;
import com.ca.apim.gateway.cagatewayconfig.util.string.EncodeDecodeUtils;
import org.apache.commons.lang3.StringUtils;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.inject.Singleton;
import java.util.*;

import static com.ca.apim.gateway.cagatewayconfig.util.gateway.BundleElementNames.*;
import static com.ca.apim.gateway.cagatewayconfig.util.properties.PropertyConstants.*;
import static com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentUtils.*;

@Singleton
public class ServiceLoader implements BundleEntityLoader {

    @Override
    public void load(Bundle bundle, Element element) {
        final Element service = getSingleChildElement(getSingleChildElement(element, RESOURCE), SERVICE);

        final Element serviceDetails = getSingleChildElement(service, SERVICE_DETAIL);
        final String id = serviceDetails.getAttribute(ATTRIBUTE_ID);
        final String folderId = serviceDetails.getAttribute(ATTRIBUTE_FOLDER_ID);
        Element nameElement = getSingleChildElement(serviceDetails, NAME);
        final String name = EncodeDecodeUtils.encodePath(nameElement.getTextContent());

        Element servicePropertiesElement = getSingleChildElement(serviceDetails, PROPERTIES);
        Map<String, Object> allProperties = BuilderUtils.mapPropertiesElements(servicePropertiesElement, PROPERTIES);
        Map<String, Object> properties = new HashMap<>();
        Service serviceEntity = new Service();
        populateServiceEntity(service, serviceEntity, allProperties, properties);
        boolean isSoapService = serviceEntity.getWsdl() != null;

        serviceEntity.setName(name);
        serviceEntity.setId(id);
        Folder folder = new Folder();
        folder.setId(folderId);
        serviceEntity.setParentFolder(folder);
        serviceEntity.setServiceDetailsElement(serviceDetails);

        Element serviceMappingsElement = getSingleChildElement(serviceEntity.getServiceDetailsElement(), SERVICE_MAPPINGS);
        Element httpMappingElement = getSingleChildElement(serviceMappingsElement, HTTP_MAPPING);

        if(isSoapService) {
            try {
                Element urlPatternElement = getSingleChildElement(httpMappingElement, URL_PATTERN);
                serviceEntity.setUrl(urlPatternElement.getTextContent());
            } catch (BundleLoadException e) {
                //It is okay to swallow this exception. As this url is optional for soap servie but mandatory for rest service.
            }
        } else {
            Element urlPatternElement = getSingleChildElement(httpMappingElement, URL_PATTERN);
            serviceEntity.setUrl(urlPatternElement.getTextContent());
        }

        Element verbsElement = getSingleChildElement(httpMappingElement, VERBS);
        NodeList verbs = verbsElement.getElementsByTagName(VERB);
        Set<String> httpMethods = new HashSet<>();
        for (Node verb : nodeList(verbs)) {
            httpMethods.add(verb.getTextContent());
        }
        serviceEntity.setHttpMethods(httpMethods);
        serviceEntity.setProperties(properties);

        bundle.getServices().put(name, serviceEntity);
    }

    private void populateServiceEntity(Element service, Service serviceEntity, Map<String, Object> allProperties, Map<String, Object> properties) {
        boolean isSoapService = false;
        String soapVersion = null;
        boolean wssProcessingEnabled = false;
        for (Map.Entry<String, Object> entry : allProperties.entrySet()) {
            if (entry.getKey().startsWith("property.")) {
                Object propertyValue = null;
                if (!entry.getKey().startsWith("property.ENV.")) {
                    propertyValue = entry.getValue();
                }
                properties.put(entry.getKey().substring(9), propertyValue);
            } else {
                switch(entry.getKey()) {
                    case KEY_VALUE_SOAP:
                        isSoapService = Boolean.valueOf(entry.getValue().toString());
                        break;
                    case KEY_VALUE_SOAP_VERSION:
                        soapVersion = entry.getValue().toString();
                        break;
                    case KEY_VALUE_WSS_PROCESSING_ENABLED:
                        wssProcessingEnabled = Boolean.valueOf(entry.getValue().toString());
                        break;
                    default:
                        break;
                }
            }
        }
        final Element resources = getSingleChildElement(service, RESOURCES);

        if(isSoapService) {
            extractResourceSetsForSoap(soapVersion, resources, serviceEntity);
            serviceEntity.getWsdl().setWssProcessingEnabled(wssProcessingEnabled);
        } else {
            final Element resourceSet = getSingleChildElement(resources, RESOURCE_SET);
            final Element resource = getSingleChildElement(resourceSet, RESOURCE);
            serviceEntity.setPolicy(resource.getTextContent());
        }
    }

    private void extractResourceSetsForSoap(String soapVersion, Element resources, Service serviceEntity) {
        List<Element> resourceSets = getChildElements(resources, RESOURCE_SET);
        for(Element resourceSet : resourceSets) {
            String tagValue = resourceSet.getAttribute(ATTRIBUTE_TAG);
            final Element resource = getSingleChildElement(resourceSet, RESOURCE);
            if(StringUtils.isEmpty(tagValue)) {
                throw new BundleLoadException("No tag attribute found under " + RESOURCE_SET);
            } else if(TAG_VALUE_POLICY.equals(tagValue)) {
                serviceEntity.setPolicy(resource.getTextContent());
            } else if(TAG_VALUE_WSDL.equals(tagValue)) {
                final String rootUrlForWsdl = resourceSet.getAttribute(ATTRIBUTE_ROOT_URL);
                final String wsdl = resource.getTextContent();

                if(StringUtils.isEmpty(wsdl) || StringUtils.isEmpty(rootUrlForWsdl)) {
                    throw new BundleLoadException("No wsdl or rootUrl found under " + RESOURCE_SET);
                } else {
                    WSDL wsdlBean = new WSDL();
                    wsdlBean.setRootUrl(rootUrlForWsdl);
                    wsdlBean.setWsdlXml(wsdl);
                    wsdlBean.setSoapVersion(soapVersion);
                    serviceEntity.setWsdl(wsdlBean);
                }
            }
        }
    }

    @Override
    public String getEntityType() {
        return EntityTypes.SERVICE_TYPE;
    }
}
