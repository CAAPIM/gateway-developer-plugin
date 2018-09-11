/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayconfig.tasks.zip.beans.identityprovider;

import com.fasterxml.jackson.annotation.JsonTypeName;

import java.util.List;

@JsonTypeName("FEDERATED")
public class FederatedIdentityProviderDetail implements IdentityProviderDetail {
    private List<String> certificateReferences;

    public void setCertificateReferences(List<String> certificateReferences) {
        this.certificateReferences = certificateReferences;
    }

    public List<String> getCertificateReferences() {
        return certificateReferences;
    }
}
