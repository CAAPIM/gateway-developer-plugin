/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayexport.tasks.explode.loader;

import com.ca.apim.gateway.cagatewayexport.tasks.explode.bundle.entity.StoredPasswordEntity;
import org.w3c.dom.Element;

import javax.inject.Singleton;

import java.util.Map;

import static com.ca.apim.gateway.cagatewayconfig.util.gateway.BundleElementNames.*;
import static com.ca.apim.gateway.cagatewayexport.tasks.explode.loader.EntityLoaderHelper.mapPropertiesElements;
import static com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentUtils.getSingleChildElement;
import static com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentUtils.getSingleChildElementTextContent;

@Singleton
public class StoredPasswordLoader implements EntityLoader<StoredPasswordEntity> {

    @Override
    public StoredPasswordEntity load(Element element) {
        final Element storedPass = getSingleChildElement(getSingleChildElement(element, RESOURCE), STORED_PASSWD);

        final String name = getSingleChildElementTextContent(storedPass, NAME);
        final Map<String, Object> properties = mapPropertiesElements(getSingleChildElement(storedPass, PROPERTIES, true), PROPERTIES);

        return new StoredPasswordEntity
                .Builder()
                .id(storedPass.getAttribute(ATTRIBUTE_ID))
                .name(name)
                .properties(properties)
                .build();
    }

    @Override
    public Class<StoredPasswordEntity> entityClass() {
        return StoredPasswordEntity.class;
    }
}
