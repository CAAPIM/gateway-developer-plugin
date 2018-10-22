/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayexport.tasks.explode.loader;

import com.ca.apim.gateway.cagatewayexport.tasks.explode.bundle.entity.IdentityProviderEntity;
import com.ca.apim.gateway.cagatewayexport.tasks.explode.bundle.entity.IdentityProviderEntity.BindOnlyLdapIdentityProviderDetail;
import com.ca.apim.gateway.cagatewayexport.tasks.explode.bundle.entity.IdentityProviderEntity.FederatedIdentityProviderDetail;
import org.jetbrains.annotations.NotNull;
import org.w3c.dom.Element;

import javax.inject.Singleton;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import static com.ca.apim.gateway.cagatewayexport.tasks.explode.bundle.BundleElementNames.*;
import static com.ca.apim.gateway.cagatewayexport.tasks.explode.bundle.entity.IdentityProviderEntity.Builder;
import static com.ca.apim.gateway.cagatewayexport.tasks.explode.bundle.entity.IdentityProviderEntity.Type;
import static com.ca.apim.gateway.cagatewayexport.tasks.explode.bundle.entity.IdentityProviderEntity.Type.fromType;
import static com.ca.apim.gateway.cagatewayexport.tasks.explode.loader.EntityLoaderHelper.mapPropertiesElements;
import static com.ca.apim.gateway.cagatewayexport.util.xml.DocumentUtils.*;

@Singleton
public class IdentityProviderLoader implements EntityLoader<IdentityProviderEntity> {

    @Override
    public IdentityProviderEntity load(Element element) {
        final Element identityProvider = getSingleChildElement(getSingleChildElement(element, RESOURCE), ID_PROV);
        final String name = getSingleChildElementTextContent(identityProvider, NAME);
        final Map<String, Object> properties = mapPropertiesElements(getSingleChildElement(identityProvider, PROPERTIES, true), PROPERTIES);
        final Type type = fromType(getSingleChildElementTextContent(identityProvider, ID_PROV_TYPE));

        switch (type) {
            case BIND_ONLY_LDAP:
                Element extensionXml = getSingleChildElement(identityProvider, EXTENSION);
                return new Builder()
                        .name(name)
                        .id(identityProvider.getAttribute(ATTRIBUTE_ID))
                        .type(type)
                        .properties(properties)
                        .identityProviderDetail(
                                buildBindOnlyLdapIdentityProviderDetail(
                                        getSingleChildElement(extensionXml, BIND_ONLY_ID_PROV_DETAIL)
                                )
                        )
                        .build();
            case FEDERATED:
                extensionXml = getSingleChildElement(identityProvider, EXTENSION, true);
                final Builder builder = new Builder()
                        .name(name)
                        .id(identityProvider.getAttribute(ATTRIBUTE_ID))
                        .type(type)
                        .properties(properties);

                if (extensionXml != null) {
                    return builder.identityProviderDetail(
                            buildFederatedIdentityProviderDetail(
                                    getSingleChildElement(extensionXml, FEDERATED_ID_PROV_DETAIL)
                            )
                    ).build();
                } else {
                    return builder.build();
                }
            case POLICY_BACKED:
            case INTERNAL:
            case LDAP:
            default:
                return null;
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
    public Class<IdentityProviderEntity> entityClass() {
        return IdentityProviderEntity.class;
    }
}
