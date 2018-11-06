/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayexport.tasks.explode.writer;

import com.ca.apim.gateway.cagatewayconfig.beans.BindOnlyLdapIdentityProviderDetail;
import com.ca.apim.gateway.cagatewayconfig.beans.Bundle;
import com.ca.apim.gateway.cagatewayconfig.beans.FederatedIdentityProviderDetail;
import com.ca.apim.gateway.cagatewayconfig.beans.IdentityProvider;
import com.ca.apim.gateway.cagatewayconfig.util.file.DocumentFileUtils;
import com.ca.apim.gateway.cagatewayconfig.util.json.JsonTools;
import org.jetbrains.annotations.NotNull;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.File;
import java.util.Map;
import java.util.stream.Collectors;

import static com.ca.apim.gateway.cagatewayconfig.beans.IdentityProvider.IdentityProviderType;
import static com.ca.apim.gateway.cagatewayconfig.beans.IdentityProvider.IdentityProviderType.fromType;
import static com.ca.apim.gateway.cagatewayexport.tasks.explode.writer.WriterHelper.copyMap;
import static com.ca.apim.gateway.cagatewayexport.tasks.explode.writer.WriterHelper.writeFile;

@Singleton
public class IdentityProviderWriter implements EntityWriter {
    private static final String IDENTITY_PROVIDERS_FILE = "identity-providers";
    private final DocumentFileUtils documentFileUtils;
    private final JsonTools jsonTools;

    @Inject
    IdentityProviderWriter(DocumentFileUtils documentFileUtils, JsonTools jsonTools) {
        this.documentFileUtils = documentFileUtils;
        this.jsonTools = jsonTools;
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public void write(Bundle bundle, File rootFolder) {
        Map<String, IdentityProvider> identityProviderBeans = bundle.getEntities(IdentityProvider.class)
                .values()
                .stream()
                .collect(Collectors.toMap(IdentityProvider::getName, this::getIdentityProviderBean));

        writeFile(rootFolder, documentFileUtils, jsonTools, identityProviderBeans, IDENTITY_PROVIDERS_FILE, IdentityProvider.class);
    }

    private IdentityProvider getIdentityProviderBean(final IdentityProvider identityProviderEntity) {
        final IdentityProvider idProvider = new IdentityProvider();
        final IdentityProviderType type = fromType(identityProviderEntity.getType().getValue());
        idProvider.setType(type);
        idProvider.setProperties(copyMap(identityProviderEntity.getProperties()));

        switch (type) {
            case BIND_ONLY_LDAP:
                idProvider.setIdentityProviderDetail(getBindOnlyLdapIdentityProviderDetailBean(
                        (BindOnlyLdapIdentityProviderDetail) identityProviderEntity.getIdentityProviderDetail()));
                return idProvider;
            case LDAP:
            case INTERNAL:
            case POLICY_BACKED:
                return null;
            case FEDERATED:
                if (identityProviderEntity.getIdentityProviderDetail() != null) {
                    idProvider.setIdentityProviderDetail(
                            getFederatedIdentityProviderDetailBean((FederatedIdentityProviderDetail) identityProviderEntity.getIdentityProviderDetail())
                    );
                }
                return idProvider;
            default:
                return null;
        }
    }

    @NotNull
    private FederatedIdentityProviderDetail getFederatedIdentityProviderDetailBean(final FederatedIdentityProviderDetail identityProviderDetailEntity) {
        return new FederatedIdentityProviderDetail(identityProviderDetailEntity.getCertificateReferences());
    }

    @NotNull
    private BindOnlyLdapIdentityProviderDetail getBindOnlyLdapIdentityProviderDetailBean(BindOnlyLdapIdentityProviderDetail identityProviderDetailEntity) {
        final BindOnlyLdapIdentityProviderDetail identityProviderDetail = new BindOnlyLdapIdentityProviderDetail();
        identityProviderDetail.setServerUrls(identityProviderDetailEntity.getServerUrls());
        identityProviderDetail.setUseSslClientAuthentication(identityProviderDetailEntity.isUseSslClientAuthentication());
        identityProviderDetail.setBindPatternPrefix(identityProviderDetailEntity.getBindPatternPrefix());
        identityProviderDetail.setBindPatternSuffix(identityProviderDetailEntity.getBindPatternSuffix());
        return identityProviderDetail;
    }
}
