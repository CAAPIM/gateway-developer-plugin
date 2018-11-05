/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */


package com.ca.apim.gateway.cagatewayexport.tasks.explode.linker;

import com.ca.apim.gateway.cagatewayconfig.beans.Bundle;
import com.ca.apim.gateway.cagatewayconfig.beans.FederatedIdentityProviderDetail;
import com.ca.apim.gateway.cagatewayconfig.beans.IdentityProvider;
import com.ca.apim.gateway.cagatewayconfig.beans.IdentityProvider.IdentityProviderType;
import com.ca.apim.gateway.cagatewayconfig.beans.TrustedCert;

import javax.inject.Singleton;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Linker for IdentityProvider and TrustedCertificate.
 */
@Singleton
public class IdentityProviderLinker implements EntityLinker<IdentityProvider> {

    @Override
    public Class<IdentityProvider> getEntityClass() {
        return IdentityProvider.class;
    }

    @Override
    public void link(IdentityProvider entity, Bundle bundle, Bundle targetBundle) {
        if (entity.getType() != IdentityProviderType.FEDERATED) {
            return;
        }

        FederatedIdentityProviderDetail identityProviderDetail = (FederatedIdentityProviderDetail) entity.getIdentityProviderDetail();
        if (identityProviderDetail != null) {
            Set<String> certIds = identityProviderDetail.getCertificateReferences();

            Set<TrustedCert> trustedCerts = bundle.getEntities(TrustedCert.class).values()
                    .stream()
                    .filter(e -> certIds.contains(e.getId()))
                    .collect(Collectors.toSet());
            if (!trustedCerts.isEmpty()) {
                entity.setIdentityProviderDetail(new FederatedIdentityProviderDetail(trustedCerts.stream().map(TrustedCert::getName).collect(Collectors.toSet())));
            }
        }
    }
}
