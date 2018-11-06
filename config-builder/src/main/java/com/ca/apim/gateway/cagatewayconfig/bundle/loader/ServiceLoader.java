/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayconfig.bundle.loader;

import com.ca.apim.gateway.cagatewayconfig.beans.Service;
import com.ca.apim.gateway.cagatewayconfig.beans.Bundle;
import com.ca.apim.gateway.cagatewayconfig.beans.Folder;
import com.ca.apim.gateway.cagatewayconfig.util.entity.EntityTypes;
import com.ca.apim.gateway.cagatewayconfig.util.string.EncodeDecodeUtils;
import org.w3c.dom.Element;

import javax.inject.Singleton;

import static com.ca.apim.gateway.cagatewayconfig.util.gateway.BundleElementNames.*;
import static com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentUtils.getSingleChildElement;

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

        final Element resources = getSingleChildElement(service, RESOURCES);
        final Element resourceSet = getSingleChildElement(resources, RESOURCE_SET);
        final Element resource = getSingleChildElement(resourceSet, RESOURCE);
        final String servicePolicyString = resource.getTextContent();

        Service serviceEntity = new Service();
        serviceEntity.setName(name);
        serviceEntity.setId(id);
        Folder folder = new Folder();
        folder.setId(folderId);
        serviceEntity.setParentFolder(folder);
        serviceEntity.setServiceDetailsElement(serviceDetails);
        serviceEntity.setPolicy(servicePolicyString);

        bundle.getServices().put(name, serviceEntity);
    }

    @Override
    public String getEntityType() {
        return EntityTypes.SERVICE_TYPE;
    }
}
