/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayconfig.bundle.builder;

import com.ca.apim.gateway.cagatewayconfig.beans.*;
import org.apache.commons.collections4.CollectionUtils;
import org.jetbrains.annotations.NotNull;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.inject.Singleton;
import java.util.*;
import java.util.stream.Collectors;

import static com.ca.apim.gateway.cagatewayconfig.util.entity.EntityTypes.ID_PROVIDER_CONFIG_TYPE;
import static com.ca.apim.gateway.cagatewayconfig.util.gateway.BuilderUtils.buildAndAppendPropertiesElement;
import static com.ca.apim.gateway.cagatewayconfig.util.gateway.BundleElementNames.*;
import static com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentUtils.createElementWithAttribute;
import static com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentUtils.createElementWithTextContent;

@Singleton
public class IdentityProviderEntityBuilder implements EntityBuilder {

    private static final Integer ORDER = 1100;
    private static final String TRUSTED_CERT_URI = "http://ns.l7tech.com/2010/04/gateway-management/trustedCertificates";

    IdentityProviderEntityBuilder() {
    }

    public List<Entity> build(Bundle bundle, BundleType bundleType, Document document) {
        if (bundle instanceof AnnotatedBundle) {
            Map<String, IdentityProvider> identityProviderMap = Optional.ofNullable(bundle.getIdentityProviders()).orElse(Collections.emptyMap());
            return buildEntities(identityProviderMap, ((AnnotatedBundle)bundle).getFullBundle(), bundleType, document);
        } else {
            return buildEntities(bundle.getIdentityProviders(), bundle, bundleType, document);
        }
    }

    private List<Entity> buildEntities(Map<String, ?> entities, Bundle bundle, BundleType bundleType, Document document) {
        switch (bundleType) {
            case DEPLOYMENT:
                return entities.entrySet().stream()
                        .map(
                                identityProviderEntry -> EntityBuilderHelper.getEntityWithOnlyMapping(ID_PROVIDER_CONFIG_TYPE, bundle.applyUniqueName(identityProviderEntry.getKey(), BundleType.ENVIRONMENT), ((IdentityProvider)identityProviderEntry.getValue()).getId())
                        ).collect(Collectors.toList());
            case ENVIRONMENT:
                return entities.entrySet().stream().map(identityProviderEntry ->
                        buildIdentityProviderEntity(bundle, bundle.applyUniqueName(identityProviderEntry.getKey(), bundleType), (IdentityProvider)identityProviderEntry.getValue(), document)
                ).collect(Collectors.toList());
            default:
                throw new EntityBuilderException("Unknown bundle type: " + bundleType);
        }
    }

    private Entity buildIdentityProviderEntity(Bundle bundle, String name, IdentityProvider identityProvider, Document document) {
        final String id = identityProvider.getId();
        final Element identityProviderElement = createElementWithAttribute(document, ID_PROV, ATTRIBUTE_ID, id);
        identityProviderElement.appendChild(createElementWithTextContent(document, NAME, name));
        identityProviderElement.appendChild(createElementWithTextContent(document, ID_PROV_TYPE, identityProvider.getType().getValue()));
        if (identityProvider.getProperties() != null) {
            buildAndAppendPropertiesElement(identityProvider.getProperties(),
                    document, identityProviderElement);
        }

        switch (identityProvider.getType()) {
            case BIND_ONLY_LDAP:
                identityProviderElement.appendChild(buildBindOnlyLdapIPDetails(identityProvider, document));
                break;
            case FEDERATED:
                final FederatedIdentityProviderDetail identityProviderDetail = (FederatedIdentityProviderDetail) identityProvider.getIdentityProviderDetail();
                appendFedIdProvDetails(bundle, identityProviderDetail, document, identityProviderElement);
                break;
            case LDAP:
            case INTERNAL:
            case POLICY_BACKED:
            default:
                throw new EntityBuilderException("Please Specify the Identity Provider Type as one of: 'BIND_ONLY_LDAP', 'FEDERATED'");
        }

        return EntityBuilderHelper.getEntityWithNameMapping(ID_PROVIDER_CONFIG_TYPE, name, id, identityProviderElement);
    }

    private void appendFedIdProvDetails(Bundle bundle,
                                        FederatedIdentityProviderDetail identityProviderDetail,
                                        Document document,
                                        Element identityProviderElement) {
        if (identityProviderDetail == null) {
            return;
        }
        final Element extensionElement = document.createElement(EXTENSION);
        final Element federatedIdProviderDetailElem = document.createElement(FEDERATED_ID_PROV_DETAIL);
        extensionElement.appendChild(federatedIdProviderDetailElem);
        final Element certReferencesElem = createElementWithAttribute(
                document,
                CERTIFICATE_REFERENCES,
                ATTRIBUTE_RESOURCE_URI,
                TRUSTED_CERT_URI);
        federatedIdProviderDetailElem.appendChild(certReferencesElem);
        final Set<String> certReferences = identityProviderDetail.getCertificateReferences();
        if (CollectionUtils.isEmpty(certReferences)) {
            throw new EntityBuilderException("Certificate References must not be empty.");
        }
        final Map<String, TrustedCert> trustedCertMap = bundle.getTrustedCerts();
        certReferences.forEach(
                certName -> {
                    TrustedCert cert = trustedCertMap.get(certName);
                    if (cert == null) {
                        throw new EntityBuilderException("Certificate Reference with name: " + certName + " not found.");
                    }
                    certReferencesElem.appendChild(createElementWithAttribute(document, REFERENCE, ATTRIBUTE_ID, cert.getId()));
                }
        );
        identityProviderElement.appendChild(extensionElement);
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
        final Set<String> serverUrls = identityProviderDetail.getServerUrls();
        if (CollectionUtils.isEmpty(serverUrls)) {
            throw new EntityBuilderException("Server Urls must not be empty.");
        }
        serverUrls.forEach(url ->
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
    @NotNull
    public Integer getOrder() {
        return ORDER;
    }
}
