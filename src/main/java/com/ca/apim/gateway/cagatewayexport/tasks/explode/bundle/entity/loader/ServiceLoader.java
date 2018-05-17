/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayexport.tasks.explode.bundle.entity.loader;

import com.ca.apim.gateway.cagatewayexport.tasks.explode.bundle.Entity;
import com.ca.apim.gateway.cagatewayexport.tasks.explode.bundle.entity.ServiceEntity;
import org.w3c.dom.Element;

public class ServiceLoader implements EntityLoader {
    @Override
    public Entity load(Element element) {
        final Element service = EntityLoaderHelper.getSingleChildElement(EntityLoaderHelper.getSingleChildElement(element, "l7:Resource"), "l7:Service");

        final Element serviceDetails = EntityLoaderHelper.getSingleChildElement(service, "l7:ServiceDetail");
        final String id = serviceDetails.getAttribute("id");
        final String folderId = serviceDetails.getAttribute("folderId");
        Element nameElement = EntityLoaderHelper.getSingleChildElement(serviceDetails, "l7:Name");
        final String name = nameElement.getTextContent();

        final Element resources = EntityLoaderHelper.getSingleChildElement(service, "l7:Resources");
        final Element resourceSet = EntityLoaderHelper.getSingleChildElement(resources, "l7:ResourceSet");
        final Element resource = EntityLoaderHelper.getSingleChildElement(resourceSet, "l7:Resource");
        final String servicePolicyString = resource.getTextContent();
        return new ServiceEntity(name, id, folderId, service, serviceDetails, servicePolicyString);
    }
}
