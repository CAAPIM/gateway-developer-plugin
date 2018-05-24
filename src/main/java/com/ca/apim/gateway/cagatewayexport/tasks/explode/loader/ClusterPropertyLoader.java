/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayexport.tasks.explode.loader;

import com.ca.apim.gateway.cagatewayexport.tasks.explode.bundle.Entity;
import com.ca.apim.gateway.cagatewayexport.tasks.explode.bundle.entity.ClusterProperty;
import org.w3c.dom.Element;

public class ClusterPropertyLoader implements EntityLoader {
    @Override
    public Entity load(Element element) {
        final Element xml = EntityLoaderHelper.getSingleChildElement(EntityLoaderHelper.getSingleChildElement(element, "l7:Resource"), "l7:ClusterProperty");
        final String name = EntityLoaderHelper.getSingleChildElement(xml, "l7:Name").getTextContent();
        final String value = EntityLoaderHelper.getSingleChildElement(xml, "l7:Value").getTextContent();
        final String id = xml.getAttribute("id");
        return new ClusterProperty(name, value, id);
    }
}