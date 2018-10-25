/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayexport.tasks.explode.loader;

import com.ca.apim.gateway.cagatewayexport.tasks.explode.bundle.entity.ClusterProperty;
import org.w3c.dom.Element;

import javax.inject.Singleton;

import static com.ca.apim.gateway.cagatewayconfig.util.gateway.BundleElementNames.*;
import static com.ca.apim.gateway.cagatewayexport.util.xml.DocumentUtils.getSingleChildElement;

@Singleton
public class ClusterPropertyLoader implements EntityLoader<ClusterProperty> {

    @Override
    public ClusterProperty load(Element element) {
        final Element xml = getSingleChildElement(getSingleChildElement(element, RESOURCE), CLUSTER_PROPERTY);
        final String name = getSingleChildElement(xml, NAME).getTextContent();
        final String value = getSingleChildElement(xml, VALUE).getTextContent();
        final String id = xml.getAttribute(ATTRIBUTE_ID);
        return new ClusterProperty(name, value, id);
    }

    @Override
    public Class<ClusterProperty> entityClass() {
        return ClusterProperty.class;
    }
}