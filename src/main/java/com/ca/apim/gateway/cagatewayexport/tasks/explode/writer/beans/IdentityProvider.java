/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayexport.tasks.explode.writer.beans;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.Map;

import static java.util.Arrays.stream;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class IdentityProvider {

    private IdentityProviderType idProviderType;
    private Map<String, Object> properties;
    private IdentityProviderDetail identityProviderDetail;

    public IdentityProviderType getIdProviderType() {
        return idProviderType;
    }

    public void setIdProviderType(IdentityProviderType idProviderType) {
        this.idProviderType = idProviderType;
    }

    public Map<String, Object> getProperties() {
        return properties;
    }

    public void setProperties(Map<String, Object> properties) {
        this.properties = properties;
    }

    public IdentityProviderDetail getIdentityProviderDetail() {
        return identityProviderDetail;
    }

    public void setIdentityProviderDetail(IdentityProviderDetail identityProviderDetail) {
        this.identityProviderDetail = identityProviderDetail;
    }

    public enum IdentityProviderType {
        INTERNAL("Internal"),
        LDAP("LDAP"),
        FEDERATED("Federated"),
        BIND_ONLY_LDAP("Simple LDAP"),
        POLICY_BACKED("Policy-backed");

        private String type;

        IdentityProviderType(String type) {
            this.type = type;
        }

        public String getType() {
            return this.type;
        }

        public static IdentityProviderType fromType(String type) {
            return stream(values()).filter(c -> c.type.equals(type)).findFirst().orElse(null);
        }
    }
}
