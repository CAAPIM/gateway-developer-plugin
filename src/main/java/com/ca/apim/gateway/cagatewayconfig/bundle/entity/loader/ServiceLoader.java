/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayconfig.bundle.entity.loader;

import com.ca.apim.gateway.cagatewayconfig.bundle.Entity;
import com.ca.apim.gateway.cagatewayconfig.bundle.entity.Service;
import org.w3c.dom.Element;

import java.util.ArrayList;
import java.util.List;

public class ServiceLoader implements EntityLoader {
    @Override
    public Entity load(Element element) {
        final Element service = EntityLoaderHelper.getSingleElement(element, "l7:Service");

        final Element serviceDetails = EntityLoaderHelper.getSingleElement(service, "l7:ServiceDetail");
        final String id = serviceDetails.getAttribute("id");
        final String folderId = serviceDetails.getAttribute("folderId");

        Element nameElement = EntityLoaderHelper.getSingleElement(serviceDetails, "l7:Name");
        Element enabledElement = EntityLoaderHelper.getSingleElement(serviceDetails, "l7:Enabled");

        Element serviceMappingsElement = EntityLoaderHelper.getSingleElement(serviceDetails, "l7:ServiceMappings");
        Element httpMappingElement = EntityLoaderHelper.getSingleElement(serviceMappingsElement, "l7:HttpMapping");
        Element urlPatternElement = EntityLoaderHelper.getSingleElement(httpMappingElement, "l7:UrlPattern");
        Element verbsElement = EntityLoaderHelper.getSingleElement(httpMappingElement, "l7:Verbs");

        final String name = nameElement.getTextContent();
        final boolean enabled = Boolean.parseBoolean(enabledElement.getTextContent());
        final String url = urlPatternElement.getTextContent();
        final List<String> verbs = new ArrayList<>(verbsElement.getChildNodes().getLength());
        for(int i = 0; i < verbsElement.getChildNodes().getLength(); i++) {
            verbs.add(verbsElement.getChildNodes().item(i).getTextContent());
        }

        final Element resources = EntityLoaderHelper.getSingleElement(service, "l7:Resources");
        final Element resourceSet = EntityLoaderHelper.getSingleElement(resources, "l7:ResourceSet");
        final Element resource = EntityLoaderHelper.getSingleElement(resourceSet, "l7:Resource");
        final String servicePolicyString = resource.getTextContent();
        return new Service(name, id, folderId, service, servicePolicyString, enabled, url, verbs);
    }
}
