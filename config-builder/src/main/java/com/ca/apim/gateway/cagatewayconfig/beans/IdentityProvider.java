/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayconfig.beans;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import javax.inject.Named;
import java.util.Map;

import static java.util.Arrays.stream;

@Named("ID_PROVIDER_CONFIG")
public class IdentityProvider extends GatewayEntity {

    public static final String INTERNAL_IDP_ID = "0000000000000000fffffffffffffffe";
    public static final String INTERNAL_IDP_NAME = "Internal Identity Provider";

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

    public IdentityProvider() {}

    private IdentityProvider(Builder builder) {
        this.setId(builder.id);
        this.setName(builder.name);
        this.type = builder.type;
        this.properties = builder.properties;
        this.identityProviderDetail = builder.identityProviderDetail;
    }


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

    public static class Builder {
        private String name;
        private String id;
        private IdentityProviderType type;
        private Map<String, Object> properties;
        private IdentityProviderDetail identityProviderDetail;

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder type(IdentityProviderType type) {
            this.type = type;
            return this;
        }

        public Builder identityProviderDetail(IdentityProviderDetail identityProviderDetail) {
            this.identityProviderDetail = identityProviderDetail;
            return this;
        }

        public Builder id(String id) {
            this.id = id;
            return this;
        }

        public Builder properties(Map<String, Object> properties) {
            this.properties = properties;
            return this;
        }

        public IdentityProvider build() {
            return new IdentityProvider(this);
        }
    }
}
