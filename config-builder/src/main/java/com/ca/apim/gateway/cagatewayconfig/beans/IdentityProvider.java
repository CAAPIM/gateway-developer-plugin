/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayconfig.beans;

import com.ca.apim.gateway.cagatewayconfig.bundle.builder.AnnotableEntity;
import com.ca.apim.gateway.cagatewayconfig.bundle.builder.AnnotatedEntity;
import com.ca.apim.gateway.cagatewayconfig.bundle.builder.AnnotationDeserializer;
import com.ca.apim.gateway.cagatewayconfig.config.spec.ConfigurationFile;
import com.ca.apim.gateway.cagatewayconfig.config.spec.EnvironmentType;
import com.ca.apim.gateway.cagatewayconfig.util.IdGenerator;
import com.ca.apim.gateway.cagatewayconfig.util.entity.EntityTypes;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.common.annotations.VisibleForTesting;

import javax.inject.Named;
import java.io.File;
import java.util.Map;
import java.util.Set;

import static com.ca.apim.gateway.cagatewayconfig.config.spec.ConfigurationFile.FileType.JSON_YAML;
import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_EMPTY;
import static java.util.Arrays.stream;

@JsonInclude(NON_EMPTY)
@Named("ID_PROVIDER_CONFIG")
@ConfigurationFile(name = "identity-providers", type = JSON_YAML)
@EnvironmentType("IDENTITY_PROVIDER")
public class IdentityProvider extends GatewayEntity implements AnnotableEntity {

    public static final String INTERNAL_IDP_ID = "0000000000000000fffffffffffffffe";
    public static final String INTERNAL_IDP_NAME = "Internal Identity Provider";

    public enum IdentityProviderType {
        INTERNAL("Internal"),
        LDAP("LDAP"),
        FEDERATED("Federated"),
        BIND_ONLY_LDAP("Simple LDAP"),
        POLICY_BACKED("Policy-Backed");

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
    @JsonDeserialize(using = AnnotationDeserializer.class)
    private Set<Annotation> annotations;
    @JsonIgnore
    private AnnotatedEntity<? extends GatewayEntity> annotatedEntity;

    @Override
    public Set<Annotation> getAnnotations() {
        return annotations;
    }

    public void setAnnotations(Set<Annotation> annotations) {
        this.annotations = annotations;
    }
    @Override
    public AnnotatedEntity getAnnotatedEntity() {
        if (annotatedEntity == null && annotations != null) {
            annotatedEntity = createAnnotatedEntity();
        }
        return annotatedEntity;
    }

    @Override
    public String getEntityType() {
        return EntityTypes.ID_PROVIDER_CONFIG_TYPE;
    }

    @VisibleForTesting
    public void setAnnotatedEntity(AnnotatedEntity<Encass> annotatedEntity) {
        this.annotatedEntity = annotatedEntity;
    }

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

    @Override
    public void postLoad(String entityKey, Bundle bundle, File rootFolder, IdGenerator idGenerator) {
        setId(idGenerator.generate());
    }

    @Override
    public String getId(){
        if (getAnnotatedEntity() != null && annotatedEntity.getId() != null) {
            return annotatedEntity.getId();
        }
        return super.getId();
    }
}
