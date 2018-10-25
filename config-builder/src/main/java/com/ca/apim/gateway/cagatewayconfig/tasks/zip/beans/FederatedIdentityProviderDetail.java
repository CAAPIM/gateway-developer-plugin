/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayconfig.tasks.zip.beans;

import com.fasterxml.jackson.annotation.JsonTypeName;

import java.util.Set;

@JsonTypeName("FEDERATED")
public class FederatedIdentityProviderDetail implements IdentityProviderDetail {
    public FederatedIdentityProviderDetail() {}

    private Set<String> certificateReferences;

    public FederatedIdentityProviderDetail(final Set<String> certificateReferences) {
        this.certificateReferences = certificateReferences;
    }

    public void setCertificateReferences(Set<String> certificateReferences) {
        this.certificateReferences = certificateReferences;
    }

    public Set<String> getCertificateReferences() {
        return certificateReferences;
    }
}
