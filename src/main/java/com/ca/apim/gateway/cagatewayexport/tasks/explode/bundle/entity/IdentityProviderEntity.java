/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayexport.tasks.explode.bundle.entity;

import com.ca.apim.gateway.cagatewayexport.tasks.explode.bundle.Entity;

import javax.inject.Named;
import java.util.Map;
import java.util.Set;

import static java.util.Arrays.stream;

@Named("ID_PROVIDER_CONFIG")
public class IdentityProviderEntity implements Entity {
    public static final String INTERNAL_IDP_ID = "0000000000000000fffffffffffffffe";
    private final String name;
    private final String id;
    private final Type type;
    private final Map<String, Object> properties;
    private IdentityProviderDetail identityProviderDetail;

    private IdentityProviderEntity(Builder idProviderBuilder) {
        this.id = idProviderBuilder.id;
        this.name = idProviderBuilder.name;
        this.type = idProviderBuilder.type;
        this.properties = idProviderBuilder.properties;
        this.identityProviderDetail = idProviderBuilder.identityProviderDetail;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getName() {
        return name;
    }

    public Type getType() {
        return type;
    }

    public Map<String, Object> getProperties() {
        return properties;
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
        private Type type;
        private Map<String, Object> properties;
        private IdentityProviderDetail identityProviderDetail;

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder type(Type type) {
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

        public IdentityProviderEntity build() {
            return new IdentityProviderEntity(this);
        }
    }

    private interface IdentityProviderDetail {

    }

    public abstract static class LdapIdentityProviderDetail implements IdentityProviderDetail {

        private Set<String> serverUrls;
        private boolean useSslClientAuthentication;

        public Set<String> getServerUrls() {
            return serverUrls;
        }

        public void setServerUrls(Set<String> serverUrls) {
            this.serverUrls = serverUrls;
        }

        public boolean isUseSslClientAuthentication() {
            return useSslClientAuthentication;
        }

        public void setUseSslClientAuthentication(boolean useSslClientAuthentication) {
            this.useSslClientAuthentication = useSslClientAuthentication;
        }
    }

    public static class FederatedIdentityProviderDetail implements IdentityProviderDetail {
        private Set<String> certificateReferences;

        public FederatedIdentityProviderDetail(final Set<String> certificateReferences) {
            this.certificateReferences = certificateReferences;
        }

        public Set<String> getCertificateReferences() {
            return certificateReferences;
        }
    }

    public static class BindOnlyLdapIdentityProviderDetail extends LdapIdentityProviderDetail {

        private String bindPatternPrefix;
        private String bindPatternSuffix;

        public String getBindPatternPrefix() {
            return bindPatternPrefix;
        }

        public void setBindPatternPrefix(String bindPatternPrefix) {
            this.bindPatternPrefix = bindPatternPrefix;
        }

        public String getBindPatternSuffix() {
            return bindPatternSuffix;
        }

        public void setBindPatternSuffix(String bindPatternSuffix) {
            this.bindPatternSuffix = bindPatternSuffix;
        }
    }

    public enum Type {
        INTERNAL("Internal"),
        LDAP("LDAP"),
        FEDERATED("Federated"),
        BIND_ONLY_LDAP("Simple LDAP"),
        POLICY_BACKED("Policy-backed");

        private String value;

        Type(String value) {
            this.value = value;
        }

        public String getValue() {
            return this.value;
        }

        public static Type fromType(String type) {
            return stream(values()).filter(c -> c.value.equals(type)).findFirst().orElse(null);
        }
    }
}
