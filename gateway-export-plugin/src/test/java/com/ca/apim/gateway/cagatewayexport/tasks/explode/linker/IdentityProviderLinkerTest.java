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
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

class IdentityProviderLinkerTest {
    private IdentityProviderLinker linker = new IdentityProviderLinker();

    @Test
    void linkNoCert() {
        link();
    }

    @Test
    void linkOneCert() {
        link("id1");
    }

    @Test
    void linkMultiCerts() {
        link("id1", "id2");
    }

    private void link(String... certId) {
        Bundle bundle = new Bundle();
        for (String cert : certId) {
            bundle.addEntity(createTrustedCert(cert));
        }

        final IdentityProvider identityProvider = createIdentityProvider(certId);
        linker.link(identityProvider, bundle, bundle);


        assertNotNull(identityProvider.getIdentityProviderDetail());
        assertTrue(identityProvider.getIdentityProviderDetail() instanceof FederatedIdentityProviderDetail);
        FederatedIdentityProviderDetail idProvDetail = (FederatedIdentityProviderDetail) identityProvider.getIdentityProviderDetail();
        Set<String> nameList = Arrays.stream(certId).map(id -> id + "name").collect(Collectors.toSet());
        assertEquals(nameList, idProvDetail.getCertificateReferences());
    }

    private static IdentityProvider createIdentityProvider(String... certNames) {
        return new IdentityProvider.Builder()
                .name("Test")
                .type(IdentityProviderType.FEDERATED)
                .identityProviderDetail(new FederatedIdentityProviderDetail(new HashSet<>(Arrays.asList(certNames))))
                .build();
    }

    private static TrustedCert createTrustedCert(String id) {
        return new TrustedCert.Builder()
                .name(id + "name")
                .id(id)
                .encodedData("someData")
                .build();
    }
}
