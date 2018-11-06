/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayconfig.bundle.loader;

import com.ca.apim.gateway.cagatewayconfig.beans.Bundle;
import com.ca.apim.gateway.cagatewayconfig.beans.ClusterProperty;
import com.ca.apim.gateway.cagatewayconfig.util.entity.EntityTypes;
import org.w3c.dom.Element;

import javax.inject.Singleton;

import static com.ca.apim.gateway.cagatewayconfig.util.gateway.BundleElementNames.*;
import static com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentUtils.getSingleChildElement;

@Singleton
public class ClusterPropertyLoader implements BundleEntityLoader {

    @Override
    public void load(Bundle bundle, Element element) {
        final Element xml = getSingleChildElement(getSingleChildElement(element, RESOURCE), CLUSTER_PROPERTY);
        final String name = getSingleChildElement(xml, NAME).getTextContent();
        final String value = getSingleChildElement(xml, VALUE).getTextContent();
        final String id = xml.getAttribute(ATTRIBUTE_ID);

        ClusterProperty clusterProperty = new ClusterProperty();
        clusterProperty.setName(name);
        clusterProperty.setId(id);
        clusterProperty.setValue(value);

        bundle.getClusterProperties().put(name, clusterProperty);
    }

    @Override
    public String getEntityType() {
        return EntityTypes.CLUSTER_PROPERTY_TYPE;
    }
}