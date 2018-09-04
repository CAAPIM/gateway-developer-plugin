/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayexport.tasks.explode.writer;

import com.ca.apim.gateway.cagatewayexport.tasks.explode.bundle.Bundle;
import com.ca.apim.gateway.cagatewayexport.tasks.explode.bundle.entity.IdentityProviderEntity;
import com.ca.apim.gateway.cagatewayexport.tasks.explode.writer.beans.BindOnlyLdapIdentityProviderDetail;
import com.ca.apim.gateway.cagatewayexport.tasks.explode.writer.beans.IdentityProvider;
import com.ca.apim.gateway.cagatewayexport.util.file.DocumentFileUtils;
import com.ca.apim.gateway.cagatewayexport.util.json.JsonTools;
import org.jetbrains.annotations.NotNull;
import org.w3c.dom.Element;

import java.io.File;
import java.util.Map;
import java.util.stream.Collectors;

import static com.ca.apim.gateway.cagatewayexport.tasks.explode.bundle.BundleElementNames.*;
import static com.ca.apim.gateway.cagatewayexport.tasks.explode.writer.WriterHelper.copyMap;
import static com.ca.apim.gateway.cagatewayexport.tasks.explode.writer.WriterHelper.writeFile;
import static com.ca.apim.gateway.cagatewayexport.tasks.explode.writer.beans.IdentityProvider.*;
import static com.ca.apim.gateway.cagatewayexport.tasks.explode.writer.beans.IdentityProvider.IdentityProviderType.fromType;
import static com.ca.apim.gateway.cagatewayexport.util.xml.DocumentUtils.*;

public class IdentityProviderWriter implements EntityWriter {
    private static final String IDENTITY_PROVIDERS_FILE = "identity-providers.yml";
    private final DocumentFileUtils documentFileUtils;
    private final JsonTools jsonTools;

    public IdentityProviderWriter(DocumentFileUtils documentFileUtils, JsonTools jsonTools) {
        this.documentFileUtils = documentFileUtils;
        this.jsonTools = jsonTools;
    }

    @Override
    public void write(Bundle bundle, File rootFolder) {
        Map<String, IdentityProvider> identityProviderBeans = bundle.getEntities(IdentityProviderEntity.class)
                .values()
                .stream()
                .collect(Collectors.toMap(IdentityProviderEntity::getName, this::getIdentityProviderBean));

        writeFile(rootFolder, documentFileUtils, jsonTools, identityProviderBeans, IDENTITY_PROVIDERS_FILE);
    }

    private IdentityProvider getIdentityProviderBean(final IdentityProviderEntity identityProviderEntity) {
        final IdentityProvider idProvider = new IdentityProvider();
        final IdentityProviderType type = fromType(identityProviderEntity.getIdProviderType().getType());
        idProvider.setIdProviderType(type);
        idProvider.setProperties(copyMap(identityProviderEntity.getProperties()));

        switch (type) {
            case BIND_ONLY_LDAP:
                idProvider.setIdentityProviderDetail(
                        getBindOnlyLdapIdentityProviderDetailBean(
                                getSingleChildElement(
                                        identityProviderEntity.getExtensionXml(),
                                        BIND_ONLY_ID_PROV_DETAIL
                                )
                        )
                );
                return idProvider;
            case LDAP:
            case INTERNAL:
            case POLICY_BACKED:
            case FEDERATED:
            default:
                return null;
        }
    }

    @NotNull
    private BindOnlyLdapIdentityProviderDetail getBindOnlyLdapIdentityProviderDetailBean(Element bindOnlyLdapIdentityProviderDetailXml) {
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
}
