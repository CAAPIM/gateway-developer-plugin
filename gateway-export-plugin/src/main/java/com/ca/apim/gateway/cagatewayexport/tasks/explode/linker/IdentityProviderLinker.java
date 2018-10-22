/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */


package com.ca.apim.gateway.cagatewayexport.tasks.explode.linker;

import com.ca.apim.gateway.cagatewayexport.tasks.explode.bundle.Bundle;
import com.ca.apim.gateway.cagatewayexport.tasks.explode.bundle.entity.IdentityProviderEntity;
import com.ca.apim.gateway.cagatewayexport.tasks.explode.bundle.entity.IdentityProviderEntity.FederatedIdentityProviderDetail;
import com.ca.apim.gateway.cagatewayexport.tasks.explode.bundle.entity.TrustedCertEntity;

import javax.inject.Singleton;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * Linker for IdentityProvider and TrustedCertificate.
 */
@Singleton
public class IdentityProviderLinker implements EntityLinker<IdentityProviderEntity> {

    @Override
    public Class<IdentityProviderEntity> getEntityClass() {
        return IdentityProviderEntity.class;
    }

    @Override
    public void link(IdentityProviderEntity entity, Bundle bundle, Bundle targetBundle) {
        if (entity.getType() != IdentityProviderEntity.Type.FEDERATED) {
            return;
        }

        FederatedIdentityProviderDetail identityProviderDetail = (FederatedIdentityProviderDetail) entity.getIdentityProviderDetail();
        if (identityProviderDetail != null) {
            Set<String> certIds = identityProviderDetail.getCertificateReferences();

            Set<TrustedCertEntity> trustedCerts = bundle.getEntities(TrustedCertEntity.class).values()
                    .stream()
                    .filter(e -> certIds.contains(e.getId()))
                    .collect(Collectors.toSet());
            if (!trustedCerts.isEmpty()) {
                entity.setIdentityProviderDetail(new FederatedIdentityProviderDetail(trustedCerts.stream().map(TrustedCertEntity::getName).collect(Collectors.toSet())));
            }
        }
    }
}
