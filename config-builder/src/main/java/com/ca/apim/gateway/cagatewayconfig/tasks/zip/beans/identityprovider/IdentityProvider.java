/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayconfig.tasks.zip.beans.identityprovider;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.util.Map;

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
    }

    private IdentityProviderType type;
    private Map<String,String> properties;

    @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXTERNAL_PROPERTY, property = "type")
    @JsonSubTypes( {
            @JsonSubTypes.Type(value=BindOnlyLdapIdentityProviderDetail.class, name="BIND_ONLY_LDAP"),
            @JsonSubTypes.Type(value=LdapIdentityProviderDetail.class, name="LDAP")
    })
    private IdentityProviderDetail identityProviderDetail;

    public IdentityProviderType getType() {
        return type;
    }

    public void setType(IdentityProviderType type) {
        this.type = type;
    }

    public Map<String,String> getProperties() {
        return properties;
    }

    public void setProperties(Map<String,String> properties) {
        this.properties = properties;
    }

    public IdentityProviderDetail getIdentityProviderDetail() {
        return identityProviderDetail;
    }

    public void setIdentityProviderDetail(IdentityProviderDetail identityProviderDetail) {
        this.identityProviderDetail = identityProviderDetail;
    }
}
