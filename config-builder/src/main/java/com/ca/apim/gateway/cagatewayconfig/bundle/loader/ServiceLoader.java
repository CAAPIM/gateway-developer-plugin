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
import com.ca.apim.gateway.cagatewayconfig.util.string.EncodeDecodeUtils;
import com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentUtils;
import org.apache.commons.lang3.StringUtils;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.inject.Singleton;
import java.util.*;

import static com.ca.apim.gateway.cagatewayconfig.util.gateway.BundleElementNames.*;
import static com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentUtils.*;

@Singleton
public class ServiceLoader implements BundleEntityLoader {

    private static final String KEY_VALUE_SOAP = "soap";
    private static final String TAG_VALUE_POLICY = "policy";
    private static final String TAG_VALUE_WSDL = "wsdl";

    @Override
    public void load(Bundle bundle, Element element) {
        final Element service = getSingleChildElement(getSingleChildElement(element, RESOURCE), SERVICE);

        final Element serviceDetails = getSingleChildElement(service, SERVICE_DETAIL);
        final String id = serviceDetails.getAttribute(ATTRIBUTE_ID);
        final String folderId = serviceDetails.getAttribute(ATTRIBUTE_FOLDER_ID);
        Element nameElement = getSingleChildElement(serviceDetails, NAME);
        final String name = EncodeDecodeUtils.encodePath(nameElement.getTextContent());

        Element propertiesElement = getSingleChildElement(serviceDetails, PROPERTIES);
        boolean isSoapService = getBooleanServicePropertyValue(propertiesElement, PROPERTY, BOOLEAN_VALUE, ATTRIBUTE_KEY, KEY_VALUE_SOAP);
        final Element resources = getSingleChildElement(service, RESOURCES);
        Service serviceEntity = new Service();

        if(isSoapService) {
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
                        wsdlBean.setUrl(rootUrlForWsdl);
                        wsdlBean.setWsdl(wsdl);
                        serviceEntity.setWsdl(wsdlBean);
                    }
                }
            }
        } else {
            final Element resourceSet = getSingleChildElement(resources, RESOURCE_SET);
            final Element resource = getSingleChildElement(resourceSet, RESOURCE);
            serviceEntity.setPolicy(resource.getTextContent());
        }

        serviceEntity.setName(name);
        serviceEntity.setId(id);
        Folder folder = new Folder();
        folder.setId(folderId);
        serviceEntity.setParentFolder(folder);
        serviceEntity.setServiceDetailsElement(serviceDetails);

        Element serviceMappingsElement = getSingleChildElement(serviceEntity.getServiceDetailsElement(), SERVICE_MAPPINGS);
        Element httpMappingElement = getSingleChildElement(serviceMappingsElement, HTTP_MAPPING);
        Element urlPatternElement = getSingleChildElement(httpMappingElement, URL_PATTERN);
        serviceEntity.setUrl(urlPatternElement.getTextContent());
        Element verbsElement = getSingleChildElement(httpMappingElement, VERBS);
        NodeList verbs = verbsElement.getElementsByTagName(VERB);
        Set<String> httpMethods = new HashSet<>();
        for (Node verb : nodeList(verbs)) {
            httpMethods.add(verb.getTextContent());
        }
        serviceEntity.setHttpMethods(httpMethods);

        Element servicePropertiesElement = getSingleChildElement(serviceEntity.getServiceDetailsElement(), PROPERTIES);
        NodeList propertyNodes = servicePropertiesElement.getElementsByTagName(PROPERTY);
        Map<String, Object> properties = new HashMap<>();
        for (int i = 0; i < propertyNodes.getLength(); i++) {
            if (propertyNodes.item(i).getAttributes().getNamedItem("key").getTextContent().startsWith("property.")) {
                String propertyValue = null;
                if (!propertyNodes.item(i).getAttributes().getNamedItem("key").getTextContent().startsWith("property.ENV.")) {
                    propertyValue = getSingleChildElement((Element) propertyNodes.item(i), STRING_VALUE).getTextContent();
                }
                properties.put(propertyNodes.item(i).getAttributes().getNamedItem("key").getTextContent().substring(9), propertyValue);
            }
        }
        serviceEntity.setProperties(properties);

        bundle.getServices().put(name, serviceEntity);
    }

    @Override
    public String getEntityType() {
        return EntityTypes.SERVICE_TYPE;
    }

    private boolean getBooleanServicePropertyValue(final Element entityItemElement, final String elementName, final String childElementName, final String attributeName, final String attributeValue) {
        String value = getServicePropertyValue(entityItemElement, elementName, childElementName, attributeName, attributeValue);
        if(value != null) {
            return Boolean.parseBoolean(value);
        } else {
            return false;
        }
    }

    /**
     *
     * This Method returns value for mentioned key. e.g. if internal is passed then method will return false,
     * for soapVersion it will return 1.1.
     *<l7:Properties>
     *      <l7:Property key="internal">
     *          <l7:BooleanValue>false</l7:BooleanValue>
     *      </l7:Property>
     *      <l7:Property key="policyRevision">
     *          <l7:LongValue>1</l7:LongValue>
     *      </l7:Property>
     *      <l7:Property key="soap">
     *          <l7:BooleanValue>true</l7:BooleanValue>
     *      </l7:Property>
     *      <l7:Property key="soapVersion">
     *          <l7:StringValue>1.1</l7:StringValue>
     *      </l7:Property>
     *      <l7:Property key="tracingEnabled">
     *          <l7:BooleanValue>false</l7:BooleanValue>
     *      </l7:Property>
     *      <l7:Property key="wssProcessingEnabled">
     *          <l7:BooleanValue>true</l7:BooleanValue>
     *      </l7:Property>
     *</l7:Properties>
     *
     * */
    private String getServicePropertyValue(final Element entityItemElement, final String elementName, final String childElementName, final String attributeName, final String attributeValue) {
        List<Element> childElements = getChildElements(entityItemElement, elementName);
        String value = null;
        for(Element child : childElements) {
            final String key = child.getAttribute(attributeName);
            if (attributeValue.equals(key)) {
                value = DocumentUtils.getSingleChildElementTextContent(child, childElementName);
                break;
            }
        }
        return value;
    }
}
