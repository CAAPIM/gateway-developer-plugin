/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayexport.tasks.explode.loader;

import com.ca.apim.gateway.cagatewayexport.tasks.explode.bundle.entity.IdentityProviderEntity;
import org.w3c.dom.Element;

import javax.inject.Singleton;
import java.util.Map;

import static com.ca.apim.gateway.cagatewayexport.tasks.explode.bundle.BundleElementNames.*;
import static com.ca.apim.gateway.cagatewayexport.tasks.explode.bundle.entity.IdentityProviderEntity.Builder;
import static com.ca.apim.gateway.cagatewayexport.tasks.explode.bundle.entity.IdentityProviderEntity.IdentityProviderType;
import static com.ca.apim.gateway.cagatewayexport.tasks.explode.bundle.entity.IdentityProviderEntity.IdentityProviderType.fromType;
import static com.ca.apim.gateway.cagatewayexport.tasks.explode.loader.EntityLoaderHelper.mapPropertiesElements;
import static com.ca.apim.gateway.cagatewayexport.util.xml.DocumentUtils.getSingleChildElement;
import static com.ca.apim.gateway.cagatewayexport.util.xml.DocumentUtils.getSingleChildElementTextContent;

@Singleton
public class IdentityProviderLoader implements EntityLoader<IdentityProviderEntity> {

    @Override
    public IdentityProviderEntity load(Element element) {
        final Element identityProvider = getSingleChildElement(getSingleChildElement(element, RESOURCE), ID_PROV);
        final String name = getSingleChildElementTextContent(identityProvider, NAME);
        final Map<String, Object> properties = mapPropertiesElements(getSingleChildElement(identityProvider, PROPERTIES, true));
        final IdentityProviderType type = fromType(getSingleChildElementTextContent(identityProvider, ID_PROV_TYPE));

        switch (type) {
            case BIND_ONLY_LDAP:
                return new Builder()
                        .name(name)
                        .id(identityProvider.getAttribute(ATTRIBUTE_ID))
                        .idProviderType(type)
                        .properties(properties)
                        .extensionXml(getSingleChildElement(identityProvider, EXTENSION))
                        .build();
            case FEDERATED:
            case POLICY_BACKED:
            case INTERNAL:
            case LDAP:
            default:
                return null;
        }
    }

    @Override
    public Class<IdentityProviderEntity> entityClass() {
        return IdentityProviderEntity.class;
    }
}
