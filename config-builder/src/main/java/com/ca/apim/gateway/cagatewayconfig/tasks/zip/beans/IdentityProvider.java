/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayconfig.tasks.zip.beans;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.util.Map;

import static java.util.Arrays.stream;

public class IdentityProvider {
    public enum IdentityProviderType {
        INTERNAL("Internal"),
        LDAP("LDAP"),
        FEDERATED("Federated"),
        BIND_ONLY_LDAP("Simple LDAP"),
        POLICY_BACKED("Policy-backed");

        private String value;
        IdentityProviderType(String value) {
            this.value = value;
        }
        public String getValue() {
            return this.value;
        }

        public static IdentityProviderType fromType(String type) {
            return stream(values()).filter(c -> c.value.equals(type)).findFirst().orElse(null);
        }
    }

    private IdentityProviderType type;
    private Map<String,Object> properties;

    @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXTERNAL_PROPERTY, property = "type")
    @JsonSubTypes( {
            @JsonSubTypes.Type(value= BindOnlyLdapIdentityProviderDetail.class, name="BIND_ONLY_LDAP"),
            @JsonSubTypes.Type(value= FullLdapIdentityProviderDetail.class, name="LDAP"),
            @JsonSubTypes.Type(value= FederatedIdentityProviderDetail.class, name="FEDERATED")
    })
    private IdentityProviderDetail identityProviderDetail;

    public IdentityProviderType getType() {
        return type;
    }

    public void setType(IdentityProviderType type) {
        this.type = type;
    }

    public Map<String,Object> getProperties() {
        return properties;
    }

    public void setProperties(Map<String,Object> properties) {
        this.properties = properties;
    }

    public IdentityProviderDetail getIdentityProviderDetail() {
        return identityProviderDetail;
    }

    public void setIdentityProviderDetail(IdentityProviderDetail identityProviderDetail) {
        this.identityProviderDetail = identityProviderDetail;
    }
}
