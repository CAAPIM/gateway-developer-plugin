/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayconfig.bundle.loader;

import com.ca.apim.gateway.cagatewayconfig.beans.Bundle;
import com.ca.apim.gateway.cagatewayconfig.beans.GenericEntity;
import com.ca.apim.gateway.cagatewayconfig.util.entity.EntityTypes;
import org.w3c.dom.Element;

import javax.inject.Singleton;

import static com.ca.apim.gateway.cagatewayconfig.util.gateway.BundleElementNames.*;
import static com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentUtils.getSingleChildElement;
import static com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentUtils.getSingleChildElementTextContent;

/**
 * Loader for GenericEntities - read information from bundle XML and add to the bundle object.
 */
@Singleton
public class GenericEntityLoader implements BundleEntityLoader {

    @Override
    public void load(Bundle bundle, Element element) {
        final Element genericEntityElement = getSingleChildElement(getSingleChildElement(element, RESOURCE), GENERIC_ENTITY);

        final String name = getSingleChildElementTextContent(genericEntityElement, NAME);
        final String entityClassName = getSingleChildElementTextContent(genericEntityElement, ENTITY_CLASS_NAME);
        final String valueXml = getSingleChildElementTextContent(genericEntityElement, VALUE_XML);

        GenericEntity genericEntity = new GenericEntity();
        genericEntity.setId(genericEntityElement.getAttribute(ATTRIBUTE_ID));
        genericEntity.setName(name);
        genericEntity.setEntityClassName(entityClassName);
        genericEntity.setXml(valueXml);

        bundle.getGenericEntities().put(name, genericEntity);
    }

    @Override
    public String getEntityType() {
        return EntityTypes.GENERIC_ENTITY_TYPE;
    }
}
