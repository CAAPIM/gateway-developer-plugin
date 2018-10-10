/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayconfig.tasks.zip.bundle.loader;

import com.ca.apim.gateway.cagatewayconfig.tasks.zip.beans.Bundle;
import com.ca.apim.gateway.cagatewayconfig.tasks.zip.beans.identityprovider.IdentityProvider;
import com.ca.apim.gateway.cagatewayconfig.util.entity.EntityTypes;
import org.w3c.dom.Element;

import javax.inject.Singleton;
import java.util.Map;

import static com.ca.apim.gateway.cagatewayconfig.util.gateway.BuilderUtils.mapPropertiesElements;
import static com.ca.apim.gateway.cagatewayconfig.util.gateway.BundleElementNames.*;
import static com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentUtils.getSingleChildElement;
import static com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentUtils.getSingleChildElementTextContent;
import static java.util.Arrays.stream;

@Singleton
public class IdentityProviderLoader implements BundleDependencyLoader {

    IdentityProviderLoader() {
    }

    @Override
    public void load(final Bundle bundle, final Element element) {
        final Element identityProviderElement = getSingleChildElement(getSingleChildElement(element, RESOURCE), ID_PROV);
        final String name = getSingleChildElementTextContent(identityProviderElement, NAME);
        final Map<String, Object> properties = mapPropertiesElements(getSingleChildElement(identityProviderElement, PROPERTIES, true), PROPERTIES);

        final String typeString = getSingleChildElementTextContent(identityProviderElement, ID_PROV_TYPE);
        IdentityProvider.IdentityProviderType type = stream(IdentityProvider.IdentityProviderType.values()).filter(c -> c.getValue().equals(typeString)).findFirst().orElse(null);

        IdentityProvider identityProvider = new IdentityProvider();
        identityProvider.setProperties(properties);
        identityProvider.setType(type);
        bundle.getIdentityProviders().put(name, identityProvider);
    }

    @Override
    public String getEntityType() {
        return EntityTypes.ID_PROVIDER_CONFIG_TYPE;
    }
}
