/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayconfig.tasks.zip.builder;

import com.ca.apim.gateway.cagatewayconfig.tasks.zip.beans.Bundle;
import com.ca.apim.gateway.cagatewayconfig.tasks.zip.beans.identityprovider.BindOnlyLdapIdentityProviderDetail;
import com.ca.apim.gateway.cagatewayconfig.tasks.zip.beans.identityprovider.FederatedIdentityProviderDetail;
import com.ca.apim.gateway.cagatewayconfig.tasks.zip.beans.identityprovider.IdentityProvider;
import com.ca.apim.gateway.cagatewayconfig.util.IdGenerator;
import com.ca.apim.gateway.cagatewayconfig.util.gateway.BuilderUtils;
import com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentTools;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.List;
import java.util.stream.Collectors;

import static com.ca.apim.gateway.cagatewayconfig.util.gateway.BundleElementNames.*;

public class IdentityProviderEntityBuilder implements EntityBuilder {
    private static final String TRUSTED_CERT_URI = "http://ns.l7tech.com/2010/04/gateway-management/trustedCertificates";
    private final Document document;
    private final IdGenerator idGenerator;

    IdentityProviderEntityBuilder(Document document, IdGenerator idGenerator) {
        this.document = document;
        this.idGenerator = idGenerator;
    }

    public List<Entity> build(Bundle bundle) {
        return bundle.getIdentityProviders().entrySet().stream().map(identityProviderEntry ->
                buildIdentityProviderEntity(identityProviderEntry.getKey(), identityProviderEntry.getValue())
        ).collect(Collectors.toList());
    }

    private Entity buildIdentityProviderEntity(String name, IdentityProvider identityProvider) {
        final Element identityProviderElement = document.createElement(ID_PROV);
        final String id = idGenerator.generate();
        identityProviderElement.setAttribute(ATTRIBUTE_ID, id);
        identityProviderElement.appendChild(DocumentTools.createElementWithTextContent(document, NAME, name));
        identityProviderElement.appendChild(DocumentTools.createElementWithTextContent(document, ID_PROV_TYPE, identityProvider.getType().getValue()));
        if (identityProvider.getProperties() != null) {
            identityProviderElement.appendChild(BuilderUtils.buildPropertiesElement(identityProvider.getProperties(), document));
        }

        switch(identityProvider.getType()) {
            case BIND_ONLY_LDAP:
                identityProviderElement.appendChild(buildBindOnlyLdapIPDetails(identityProvider));
                break;
            case LDAP:
            case INTERNAL:
            case FEDERATED:
                final FederatedIdentityProviderDetail identityProviderDetail = (FederatedIdentityProviderDetail) identityProvider.getIdentityProviderDetail();
                if (identityProviderDetail != null) {
                    identityProviderElement.appendChild(buildFedIdProviderDetails(identityProviderDetail));
                }
                break;
            case POLICY_BACKED:
            default:
                throw new EntityBuilderException("Please Specify the Identity Provider Type as one of: 'BIND_ONLY_LDAP'");
        }

        return new Entity("ID_PROVIDER_CONFIG", name, id, identityProviderElement);
    }

    private Element buildFedIdProviderDetails(FederatedIdentityProviderDetail identityProviderDetail) {
        final Element extensionElement = document.createElement(EXTENSION);
        final Element federatedIdProviderDetailElem = document.createElement(FEDERATED_ID_PROV_DETAIL);
        extensionElement.appendChild(federatedIdProviderDetailElem);
        final Element certReferencesElem = DocumentTools.createElementWithAttribute(
                document,
                CERTIFICATE_REFERENCES,
                ATTRIBUTE_RESOURCE_URI,
                TRUSTED_CERT_URI);
        federatedIdProviderDetailElem.appendChild(certReferencesElem);
        final List<String> certReferences = identityProviderDetail.getCertificateReferences();
        if (certReferences == null || certReferences.isEmpty()) {
            throw new EntityBuilderException("Certificate References must not be empty.");
        }
        certReferences.forEach(
                certId -> certReferencesElem.appendChild(DocumentTools.createElementWithAttribute(document, REFERENCE, ATTRIBUTE_ID, certId))
        );
        return extensionElement;
    }

    private Element buildBindOnlyLdapIPDetails(IdentityProvider identityProvider) {
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
            throw new EntityBuilderException("Server Urls must not be empty.");
        }
        serverUrls.forEach(url ->
            serverUrlsElement.appendChild(DocumentTools.createElementWithTextContent(document, STRING_VALUE, url))
        );
        bindOnlyLdapIdentityProviderDetailElement.appendChild(
                DocumentTools.createElementWithTextContent(
                        document,
                        USE_SSL_CLIENT_AUTH,
                        String.valueOf(identityProviderDetail.isUseSslClientAuthentication())
                )
        );
        bindOnlyLdapIdentityProviderDetailElement.appendChild(
                DocumentTools.createElementWithTextContent(
                        document,
                        BIND_PATTERN_PREFIX,
                        identityProviderDetail.getBindPatternPrefix()
                )
        );
        bindOnlyLdapIdentityProviderDetailElement.appendChild(
                DocumentTools.createElementWithTextContent(
                        document,
                        BIND_PATTERN_SUFFIX,
                        identityProviderDetail.getBindPatternSuffix()
                )
        );

        return extensionElement;
    }
}
