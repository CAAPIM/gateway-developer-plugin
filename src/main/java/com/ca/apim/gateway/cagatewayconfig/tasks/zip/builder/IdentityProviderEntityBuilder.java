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
import com.ca.apim.gateway.cagatewayconfig.util.gateway.BuilderUtils;
import com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentTools;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class IdentityProviderEntityBuilder implements EntityBuilder {
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
        final Element identityProviderElement = document.createElement("l7:IdentityProvider");
        final String id = idGenerator.generate();
        identityProviderElement.setAttribute("id", id);
        identityProviderElement.appendChild(DocumentTools.createElement(document, "l7:Name", name));
        identityProviderElement.appendChild(DocumentTools.createElement(document, "l7:IdentityProviderType", identityProvider.getType().getValue()));

        switch(identityProvider.getType()) {
            case BIND_ONLY_LDAP:
                if (identityProvider.getProperties() != null) {
                    identityProviderElement.appendChild(BuilderUtils.buildPropertiesElement(
                            identityProvider.getProperties().entrySet()
                                    .stream()
                                    .collect(Collectors.toMap(stringStringEntry -> "property." + stringStringEntry.getKey(), Map.Entry::getValue)), document));
                }
                identityProviderElement.appendChild(buildBindOnlyLdapIPDetails(identityProvider));
                break;
            case LDAP:
            case INTERNAL:
            case FEDERATED:
            case POLICY_BACKED:
            default:
                throw new EntityBuilderException("Please Specify the Identity Provider Type as one of: 'BIND_ONLY_LDAP'");
        }

        return new Entity("ID_PROVIDER_CONFIG", name, id, identityProviderElement);
    }

    private Element buildBindOnlyLdapIPDetails(IdentityProvider identityProvider) {
        final BindOnlyLdapIdentityProviderDetail identityProviderDetail = (BindOnlyLdapIdentityProviderDetail) identityProvider.getIdentityProviderDetail();
        if (identityProviderDetail == null) {
            throw new EntityBuilderException("Identity Provider Detail must be specified for BIND_ONLY_LDAP");
        }
        final Element extensionElement = document.createElement("l7:Extension");
        final Element bindOnlyLdapIdentityProviderDetailElement = document.createElement("l7:BindOnlyLdapIdentityProviderDetail");
        extensionElement.appendChild(bindOnlyLdapIdentityProviderDetailElement);
        final Element serverUrlsElement = document.createElement("l7:ServerUrls");
        bindOnlyLdapIdentityProviderDetailElement.appendChild(serverUrlsElement);
        final List<String> serverUrls = identityProviderDetail.getServerUrls();
        if (serverUrls == null || serverUrls.isEmpty()) {
            throw new EntityBuilderException("serverUrls must be a list of urls.");
        }
        identityProviderDetail.getServerUrls().forEach(url ->
            serverUrlsElement.appendChild(DocumentTools.createElement(document, "l7:StringValue", url))
        );
        bindOnlyLdapIdentityProviderDetailElement.appendChild(
                DocumentTools.createElement(
                        document,
                        "l7:UseSslClientAuthentication",
                        String.valueOf(identityProviderDetail.isUseSslClientAuthentication())
                )
        );
        bindOnlyLdapIdentityProviderDetailElement.appendChild(
                DocumentTools.createElement(
                        document,
                        "l7:BindPatternPrefix",
                        identityProviderDetail.getBindPatternPrefix()
                )
        );
        bindOnlyLdapIdentityProviderDetailElement.appendChild(
                DocumentTools.createElement(
                        document,
                        "l7:BindPatternSuffix",
                        identityProviderDetail.getBindPatternSuffix()
                )
        );

        return extensionElement;
    }
}
