/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayconfig.tasks.zip.builder;

import com.ca.apim.gateway.cagatewayconfig.tasks.zip.beans.Bundle;
import com.ca.apim.gateway.cagatewayconfig.tasks.zip.beans.identityprovider.BindOnlyLdapIdentityProviderDetail;
import com.ca.apim.gateway.cagatewayconfig.tasks.zip.beans.identityprovider.IdentityProvider;
import com.ca.apim.gateway.cagatewayconfig.util.IdGenerator;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.ca.apim.gateway.cagatewayconfig.tasks.zip.builder.EntityBuilderHelper.getEntityWithNameMapping;
import static com.ca.apim.gateway.cagatewayconfig.tasks.zip.builder.EntityBuilderHelper.getEntityWithOnlyMapping;
import static com.ca.apim.gateway.cagatewayconfig.util.entity.EntityTypes.ID_PROVIDER_CONFIG_TYPE;
import static com.ca.apim.gateway.cagatewayconfig.util.gateway.BuilderUtils.buildAndAppendPropertiesElement;
import static com.ca.apim.gateway.cagatewayconfig.util.gateway.BundleElementNames.*;
import static com.ca.apim.gateway.cagatewayconfig.util.properties.PropertyConstants.PREFIX_PROPERTY;
import static com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentUtils.createElementWithAttribute;
import static com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentUtils.createElementWithTextContent;

@Singleton
public class IdentityProviderEntityBuilder implements EntityBuilder {

    private static final Integer ORDER = 700;
    private final IdGenerator idGenerator;

    @Inject
    IdentityProviderEntityBuilder(IdGenerator idGenerator) {
        this.idGenerator = idGenerator;
    }

    public List<Entity> build(Bundle bundle, BundleType bundleType, Document document) {
        switch (bundleType) {
            case DEPLOYMENT:
                return bundle.getIdentityProviders().entrySet().stream()
                        .map(
                                identityProviderEntry -> getEntityWithOnlyMapping(ID_PROVIDER_CONFIG_TYPE, identityProviderEntry.getKey(), idGenerator.generate())
                        ).collect(Collectors.toList());
            case ENVIRONMENT:
                return bundle.getIdentityProviders().entrySet().stream().map(identityProviderEntry ->
                        buildIdentityProviderEntity(identityProviderEntry.getKey(), identityProviderEntry.getValue(), document)
                ).collect(Collectors.toList());
            default:
                throw new EntityBuilderException("Unknown bundle type: " + bundleType);
        }
    }

    private Entity buildIdentityProviderEntity(String name, IdentityProvider identityProvider, Document document) {
        final String id = idGenerator.generate();
        final Element identityProviderElement = createElementWithAttribute(document, ID_PROV, ATTRIBUTE_ID, id);
        identityProviderElement.appendChild(createElementWithTextContent(document, NAME, name));
        identityProviderElement.appendChild(createElementWithTextContent(document, ID_PROV_TYPE, identityProvider.getType().getValue()));

        switch (identityProvider.getType()) {
            case BIND_ONLY_LDAP:
                if (identityProvider.getProperties() != null) {
                    buildAndAppendPropertiesElement(identityProvider.getProperties().entrySet()
                                    .stream()
                                    .collect(Collectors.toMap(stringStringEntry -> PREFIX_PROPERTY + stringStringEntry.getKey(), Map.Entry::getValue)),
                            document, identityProviderElement);
                }
                identityProviderElement.appendChild(buildBindOnlyLdapIPDetails(identityProvider, document));
                break;
            case LDAP:
            case INTERNAL:
            case FEDERATED:
            case POLICY_BACKED:
            default:
                throw new EntityBuilderException("Please Specify the Identity Provider Type as one of: 'BIND_ONLY_LDAP'");
        }

        return getEntityWithNameMapping(ID_PROVIDER_CONFIG_TYPE, name, id, identityProviderElement);
    }

    private Element buildBindOnlyLdapIPDetails(IdentityProvider identityProvider, Document document) {
        final BindOnlyLdapIdentityProviderDetail identityProviderDetail = (BindOnlyLdapIdentityProviderDetail) identityProvider.getIdentityProviderDetail();
        if (identityProviderDetail == null) {
            throw new EntityBuilderException("Identity Provider Detail must be specified for BIND_ONLY_LDAP");
        }
        final Element extensionElement = document.createElement(EXTENSION);
        final Element bindOnlyLdapIdentityProviderDetailElement = document.createElement(BIND_ONLY_ID_PROV_DETAIL);
        extensionElement.appendChild(bindOnlyLdapIdentityProviderDetailElement);
        final Element serverUrlsElement = document.createElement(SERVER_URLS);
        bindOnlyLdapIdentityProviderDetailElement.appendChild(serverUrlsElement);
        final List<String> serverUrls = identityProviderDetail.getServerUrls();
        if (serverUrls == null || serverUrls.isEmpty()) {
            throw new EntityBuilderException("serverUrls must be a list of urls.");
        }
        identityProviderDetail.getServerUrls().forEach(url ->
                serverUrlsElement.appendChild(createElementWithTextContent(document, STRING_VALUE, url))
        );
        bindOnlyLdapIdentityProviderDetailElement.appendChild(
                createElementWithTextContent(
                        document,
                        USE_SSL_CLIENT_AUTH,
                        String.valueOf(identityProviderDetail.isUseSslClientAuthentication())
                )
        );
        bindOnlyLdapIdentityProviderDetailElement.appendChild(
                createElementWithTextContent(
                        document,
                        BIND_PATTERN_PREFIX,
                        identityProviderDetail.getBindPatternPrefix()
                )
        );
        bindOnlyLdapIdentityProviderDetailElement.appendChild(
                createElementWithTextContent(
                        document,
                        BIND_PATTERN_SUFFIX,
                        identityProviderDetail.getBindPatternSuffix()
                )
        );

        return extensionElement;
    }

    @Override
    public Integer getOrder() {
        return ORDER;
    }
}
