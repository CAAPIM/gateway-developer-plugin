/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayexport.tasks.explode.bundle.entity;

import com.ca.apim.gateway.cagatewayexport.tasks.explode.bundle.Entity;
import org.w3c.dom.Element;

import javax.inject.Named;
import java.util.Map;

import static java.util.Arrays.stream;

@Named("ID_PROVIDER_CONFIG")
public class IdentityProviderEntity implements Entity {
    public static final String INTERNAL_IDP_ID = "0000000000000000fffffffffffffffe";
    private final String name;
    private final String id;
    private final IdentityProviderType idProviderType;
    private final Map<String, Object> properties;
    private final Element extensionXml;

    private IdentityProviderEntity(Builder idProviderBuilder) {
        this.id = idProviderBuilder.id;
        this.name = idProviderBuilder.name;
        this.idProviderType = idProviderBuilder.idProviderType;
        this.properties = idProviderBuilder.properties;
        this.extensionXml = idProviderBuilder.extensionXml;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getName() {
        return name;
    }

    public IdentityProviderType getIdProviderType() {
        return idProviderType;
    }

    public Map<String, Object> getProperties() {
        return properties;
    }

    public Element getExtensionXml() {
        return extensionXml;
    }

    public static class Builder {
        private String name;
        private String id;
        private IdentityProviderType idProviderType;
        private Map<String, Object> properties;
        private Element extensionXml;

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder idProviderType(IdentityProviderType idProviderType) {
            this.idProviderType = idProviderType;
            return this;
        }

        public Builder extensionXml(Element extensionXml) {
            this.extensionXml = extensionXml;
            return this;
        }

        public Builder id(String id) {
            this.id = id;
            return this;
        }

        public Builder properties(Map<String, Object> properties) {
            this.properties  = properties;
            return this;
        }

        public IdentityProviderEntity build() {
            return new IdentityProviderEntity(this);
        }
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
