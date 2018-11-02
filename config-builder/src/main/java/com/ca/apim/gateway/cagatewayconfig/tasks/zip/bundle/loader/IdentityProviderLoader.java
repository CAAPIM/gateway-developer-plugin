/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayconfig.tasks.zip.bundle.loader;

import com.ca.apim.gateway.cagatewayconfig.tasks.zip.beans.BindOnlyLdapIdentityProviderDetail;
import com.ca.apim.gateway.cagatewayconfig.tasks.zip.beans.Bundle;
import com.ca.apim.gateway.cagatewayconfig.tasks.zip.beans.FederatedIdentityProviderDetail;
import com.ca.apim.gateway.cagatewayconfig.tasks.zip.beans.IdentityProvider;
import com.ca.apim.gateway.cagatewayconfig.tasks.zip.beans.IdentityProvider.IdentityProviderType;
import com.ca.apim.gateway.cagatewayconfig.util.entity.EntityTypes;
import org.jetbrains.annotations.NotNull;
import org.w3c.dom.Element;

import javax.inject.Singleton;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import static com.ca.apim.gateway.cagatewayconfig.util.gateway.BuilderUtils.mapPropertiesElements;
import static com.ca.apim.gateway.cagatewayconfig.util.gateway.BundleElementNames.*;
import static com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentUtils.*;
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
        IdentityProviderType type = stream(IdentityProviderType.values()).filter(c -> c.getValue().equals(typeString)).findFirst().orElseThrow(() -> new DependencyBundleLoadException("Invalid Identity Provider Type: " + typeString));

        IdentityProvider identityProvider = new IdentityProvider();
        identityProvider.setId(identityProviderElement.getAttribute(ATTRIBUTE_ID));
        identityProvider.setName(name);
        identityProvider.setProperties(properties);
        identityProvider.setType(type);
        bundle.getIdentityProviders().put(name, identityProvider);

        switch (type) {
            case BIND_ONLY_LDAP:
                Element extensionXml = getSingleChildElement(identityProviderElement, EXTENSION);
                identityProvider.setIdentityProviderDetail(buildBindOnlyLdapIdentityProviderDetail(
                        getSingleChildElement(extensionXml, BIND_ONLY_ID_PROV_DETAIL)
                ));
                break;
            case FEDERATED:
                extensionXml = getSingleChildElement(identityProviderElement, EXTENSION, true);
                if (extensionXml != null) {
                    identityProvider.setIdentityProviderDetail(buildFederatedIdentityProviderDetail(
                            getSingleChildElement(extensionXml, FEDERATED_ID_PROV_DETAIL)
                    ));
                }
                break;
            case POLICY_BACKED:
            case INTERNAL:
            case LDAP:
            default:
        }
    }

    @NotNull
    private BindOnlyLdapIdentityProviderDetail buildBindOnlyLdapIdentityProviderDetail(Element bindOnlyLdapIdentityProviderDetailXml) {
        final BindOnlyLdapIdentityProviderDetail identityProviderDetail = new BindOnlyLdapIdentityProviderDetail();
        // Configure the detail bean
        identityProviderDetail.setServerUrls(
                getChildElementsTextContents(
                        getSingleChildElement(bindOnlyLdapIdentityProviderDetailXml, SERVER_URLS), STRING_VALUE
                )
        );
        identityProviderDetail.setUseSslClientAuthentication(
                Boolean.parseBoolean(
                        getSingleChildElementTextContent(bindOnlyLdapIdentityProviderDetailXml, USE_SSL_CLIENT_AUTH)
                )
        );
        identityProviderDetail.setBindPatternPrefix(
                getSingleChildElementTextContent(bindOnlyLdapIdentityProviderDetailXml, BIND_PATTERN_PREFIX)
        );
        identityProviderDetail.setBindPatternSuffix(
                getSingleChildElementTextContent(bindOnlyLdapIdentityProviderDetailXml, BIND_PATTERN_SUFFIX)
        );
        return identityProviderDetail;
    }

    private FederatedIdentityProviderDetail buildFederatedIdentityProviderDetail(final Element federatedIdentityProviderDetailXml) {
        if (federatedIdentityProviderDetailXml == null) {
            return null;
        }
        final List<String> certReferences = getChildElementsAttributeValues(
                getSingleChildElement(federatedIdentityProviderDetailXml, CERTIFICATE_REFERENCES), REFERENCE, ATTRIBUTE_ID
        );
        return new FederatedIdentityProviderDetail(new HashSet<>(certReferences));
    }

    @Override
    public String getEntityType() {
        return EntityTypes.ID_PROVIDER_CONFIG_TYPE;
    }
}
