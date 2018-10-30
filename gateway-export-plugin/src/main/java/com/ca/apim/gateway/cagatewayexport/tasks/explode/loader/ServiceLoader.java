/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayexport.tasks.explode.loader;

import com.ca.apim.gateway.cagatewayconfig.util.string.EncodeDecodeUtils;
import com.ca.apim.gateway.cagatewayexport.tasks.explode.bundle.entity.ServiceEntity;
import org.w3c.dom.Element;

import javax.inject.Singleton;

import static com.ca.apim.gateway.cagatewayconfig.util.gateway.BundleElementNames.*;
import static com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentUtils.getSingleChildElement;

@Singleton
public class ServiceLoader implements EntityLoader<ServiceEntity> {

    @Override
    public ServiceEntity load(Element element) {
        final Element service = getSingleChildElement(getSingleChildElement(element, RESOURCE), SERVICE);

        final Element serviceDetails = getSingleChildElement(service, SERVICE_DETAIL);
        final String id = serviceDetails.getAttribute(ATTRIBUTE_ID);
        final String folderId = serviceDetails.getAttribute(ATTRIBUTE_FOLDER_ID);
        Element nameElement = getSingleChildElement(serviceDetails, NAME);
        final String name = EncodeDecodeUtils.encodePath(nameElement.getTextContent());

        final Element resources = getSingleChildElement(service, RESOURCES);
        final Element resourceSet = getSingleChildElement(resources, RESOURCE_SET);
        final Element resource = getSingleChildElement(resourceSet, RESOURCE);
        final String servicePolicyString = resource.getTextContent();
        return new ServiceEntity(name, id, folderId, serviceDetails, servicePolicyString);
    }

    @Override
    public Class<ServiceEntity> entityClass() {
        return ServiceEntity.class;
    }
}
