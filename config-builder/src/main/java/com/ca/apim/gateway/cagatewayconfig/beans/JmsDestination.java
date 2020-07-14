/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayconfig.beans;

import com.ca.apim.gateway.cagatewayconfig.bundle.builder.AnnotableEntity;
import com.ca.apim.gateway.cagatewayconfig.bundle.builder.AnnotatedEntity;
import com.ca.apim.gateway.cagatewayconfig.bundle.builder.AnnotationDeserializer;
import com.ca.apim.gateway.cagatewayconfig.config.loader.ConfigLoadException;
import com.ca.apim.gateway.cagatewayconfig.config.spec.ConfigurationFile;
import com.ca.apim.gateway.cagatewayconfig.config.spec.EnvironmentType;
import com.ca.apim.gateway.cagatewayconfig.util.IdGenerator;
import com.ca.apim.gateway.cagatewayconfig.util.entity.EntityTypes;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.common.annotations.VisibleForTesting;
import org.jetbrains.annotations.Nullable;

import javax.inject.Named;

import java.io.File;
import java.util.Map;
import java.util.Set;

import static com.ca.apim.gateway.cagatewayconfig.config.spec.ConfigurationFile.FileType.JSON_YAML;
import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;
import static java.util.Arrays.stream;

@JsonInclude(NON_NULL)
@Named("JMS_ENDPOINT")
@ConfigurationFile(name = "jms-destinations", type= JSON_YAML)
@EnvironmentType("JMS_DESTINATION")
// sonarcloud believes this is a hardcoded password
// sonarcloud believes variables declared in builder class duplicates variables declared in entity class
@SuppressWarnings({"squid:S2068", "common-java:DuplicatedBlocks"})
public class JmsDestination extends GatewayEntity implements AnnotableEntity {

    public static final String PROVIDER_TYPE_GENERIC = null;
    public static final String PROVIDER_TYPE_TIBCO_EMS = "TIBCO EMS";
    public static final String PROVIDER_TYPE_WEBSPHERE_MQ_OVER_LDAP = "WebSphere MQ over LDAP";

    // Defaults
    public static final int DEFAULT_DEDICATED_CONSUMER_CONNECTION_SIZE = 1;
    public static final long DEFAULT_MAX_INBOUND_MESSAGE_SIZE = -1L;    // -1 = default - Do not override,  0 = Unlimited.
    
    // Basics
    private String providerType;
    
    // JNDI
    private String initialContextFactoryClassName;
    private String jndiUrl;
    private String jndiUsername;
    private String jndiPasswordRef;
    private String jndiPassword;
    private Map<String, Object> jndiProperties;

    // Destination
    private DestinationType destinationType;
    private String connectionFactoryName;
    private String destinationName;
    private String destinationUsername;
    private String destinationPasswordRef;
    private String destinationPassword;
    
    // Inbound
    private InboundJmsDestinationDetail inboundDetail;
    
    // Outbound
    private OutboundJmsDestinationDetail outboundDetail;
    
    // Contains provider specific settings. 
    // For example, for providers:
    // - TIBCO EMS 
    // - WebSphere MQ over LDAP
    private Map<String, Object> additionalProperties;

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
        return EntityTypes.JMS_DESTINATION_TYPE;
    }

    @VisibleForTesting
    public void setAnnotatedEntity(AnnotatedEntity<Encass> annotatedEntity) {
        this.annotatedEntity = annotatedEntity;
    }
    
    public JmsDestination() {
        super();
    }
    
    private JmsDestination(Builder builder) {
        super();
        setName(builder.name);
        setId(builder.id);
        providerType = builder.providerType;
        initialContextFactoryClassName = builder.initialContextFactoryClassName;
        jndiUrl = builder.jndiUrl;
        jndiUsername = builder.jndiUsername;
        jndiPasswordRef = builder.jndiPasswordRef;
        jndiPassword = builder.jndiPassword;
        jndiProperties = builder.jndiProperties;
        destinationType = builder.destinationType;
        connectionFactoryName = builder.connectionFactoryName;
        destinationName = builder.destinationName;
        destinationUsername = builder.destinationUsername;
        destinationPasswordRef = builder.destinationPasswordRef;
        destinationPassword = builder.destinationPassword;
        inboundDetail = builder.inboundDetail;
        outboundDetail = builder.outboundDetail;
        additionalProperties = builder.additionalProperties;
    }
    
    public String getProviderType() {
        return providerType;
    }

    public void setProviderType(String providerType) {
        this.providerType = providerType;
    }

    public String getInitialContextFactoryClassName() {
        return initialContextFactoryClassName;
    }

    public void setInitialContextFactoryClassName(String initialContextFactoryClassName) {
        this.initialContextFactoryClassName = initialContextFactoryClassName;
    }

    public Map<String, Object> getJndiProperties() {
        return jndiProperties;
    }

    public void setJndiProperties(Map<String, Object> jndiProperties) {
        this.jndiProperties = jndiProperties;
    }

    public String getJndiUrl() {
        return jndiUrl;
    }

    public void setJndiUrl(String jndiUrl) {
        this.jndiUrl = jndiUrl;
    }

    public String getJndiUsername() {
        return jndiUsername;
    }

    public void setJndiUsername(String jndiUsername) {
        this.jndiUsername = jndiUsername;
    }

    public String getJndiPasswordRef() {
        return jndiPasswordRef;
    }

    public void setJndiPasswordRef(String jndiPasswordRef) {
        this.jndiPasswordRef = jndiPasswordRef;
    }
    
    public String getJndiPassword() {
        return jndiPassword;
    }

    public void setJndiPassword(String jndiPassword) {
        this.jndiPassword = jndiPassword;
    }

    public DestinationType getDestinationType() {
        return destinationType;
    }

    public void setDestinationType(DestinationType destinationType) {
        this.destinationType = destinationType;
    }

    public String getConnectionFactoryName() {
        return connectionFactoryName;
    }

    public void setConnectionFactoryName(String connectionFactoryName) {
        this.connectionFactoryName = connectionFactoryName;
    }

    public String getDestinationName() {
        return destinationName;
    }

    public void setDestinationName(String destinationName) {
        this.destinationName = destinationName;
    }

    public String getDestinationUsername() {
        return destinationUsername;
    }

    public void setDestinationUsername(String destinationUsername) {
        this.destinationUsername = destinationUsername;
    }

    public String getDestinationPasswordRef() {
        return destinationPasswordRef;
    }

    public void setDestinationPasswordRef(String destinationPasswordRef) {
        this.destinationPasswordRef = destinationPasswordRef;
    }

    public String getDestinationPassword() {
        return destinationPassword;
    }

    public void setDestinationPassword(String destinationPassword) {
        this.destinationPassword = destinationPassword;
    }

    public InboundJmsDestinationDetail getInboundDetail() {
        return inboundDetail;
    }

    public void setInboundDetail(InboundJmsDestinationDetail inboundDetail) {
        this.inboundDetail = inboundDetail;
    }

    public OutboundJmsDestinationDetail getOutboundDetail() {
        return outboundDetail;
    }

    public void setOutboundDetail(OutboundJmsDestinationDetail outboundDetail) {
        this.outboundDetail = outboundDetail;
    }

    public Map<String, Object> getAdditionalProperties() {
        return additionalProperties;
    }

    public void setAdditionalProperties(Map<String, Object> additionalProperties) {
        this.additionalProperties = additionalProperties;
    }

    @Override
    public void postLoad(String entityKey, Bundle bundle, @Nullable File rootFolder, IdGenerator idGenerator) {
        if (getJndiPasswordRef() != null && getJndiPassword() != null) {
            throw new ConfigLoadException("Cannot specify both a password reference and a password for JNDI password for JMS destination: " + entityKey);
        }

        if (getDestinationPasswordRef() != null && getDestinationPassword() != null) {
            throw new ConfigLoadException("Cannot specify both a password reference and a password for Destination password for JMS destination: " + entityKey);
        }

        if (this.inboundDetail == null && this.outboundDetail == null) {
            throw new ConfigLoadException("Must specify inbound or outbound details for JMS destination: " + entityKey);
        }
        
        if (this.inboundDetail != null && this.outboundDetail != null) {
            throw new ConfigLoadException("Cannot specify both an inbound and an outbound details for JMS destination: " + entityKey);
        }

        setId(idGenerator.generate());
    }
    
    public enum DestinationType {
        QUEUE("Queue"),
        TOPIC("Topic");
        
        private String type;
        
        DestinationType(String type) {
            this.type = type;
        }
        
        public String getType() {
            return type;
        }
        
        public static DestinationType fromType(String type) {
            return stream(values()).filter(c -> c.type.equals(type)).findFirst().orElse(null);
        }
    }

    public static class Builder {
        private Map<String, Object> additionalProperties;
        private OutboundJmsDestinationDetail outboundDetail;
        private InboundJmsDestinationDetail inboundDetail;
        private String destinationPassword;
        private String destinationPasswordRef;
        private String destinationUsername;
        private String destinationName;
        private String connectionFactoryName;
        private DestinationType destinationType;
        private Map<String, Object> jndiProperties;
        private String jndiPassword;
        private String jndiPasswordRef;
        private String jndiUsername;
        private String jndiUrl;
        private String initialContextFactoryClassName;
        private String providerType;
        private String id;
        private String name;

        public Builder name(String name) {
            this.name = name;
            return this;
        }
        
        public Builder id(String id) {
            this.id = id;
            return this;
        }

        public Builder providerType(String providerType) {
            this.providerType = providerType;
            return this;
        }
        
        public Builder initialContextFactoryClassName(String initialContextFactoryClassName) {
            this.initialContextFactoryClassName = initialContextFactoryClassName;
            return this;
        }
        
        public Builder jndiUrl(String jndiUrl) {
            this.jndiUrl = jndiUrl;
            return this;
        }

        public Builder jndiUsername(String jndiUsername) {
            this.jndiUsername = jndiUsername;
            return this;
        }

        public Builder jndiPasswordRef(String jndiPasswordRef) {
            this.jndiPasswordRef = jndiPasswordRef;
            return this;
        }
        
        public Builder jndiPassword(String jndiPassword) {
            this.jndiPassword = jndiPassword;
            return this;
        }
        
        public Builder jndiProperties(Map<String, Object> jndiProperties) {
            this.jndiProperties = jndiProperties;
            return this;
        }

        public Builder destinationType(DestinationType destinationType) {
            this.destinationType = destinationType;
            return this;
        }

        public Builder connectionFactoryName(String connectionFactoryName) {
            this.connectionFactoryName = connectionFactoryName;
            return this;
        }

        public Builder destinationUsername(String destinationUsername) {
            this.destinationUsername = destinationUsername;
            return this;
        }
        
        public Builder destinationName(String destinationName) {
            this.destinationName = destinationName;
            return this;
        }

        public Builder destinationPasswordRef(String destinationPasswordRef) {
            this.destinationPasswordRef = destinationPasswordRef;
            return this;
        }

        public Builder destinationPassword(String destinationPassword) {
            this.destinationPassword = destinationPassword;
            return this;
        }

        public Builder inboundDetail(InboundJmsDestinationDetail inboundDetail) {
            this.inboundDetail = inboundDetail;
            return this;
        }

        public Builder outboundDetail(OutboundJmsDestinationDetail outboundDetail) {
            this.outboundDetail = outboundDetail;
            return this;
        }
        
        public Builder additionalProperties(Map<String, Object> additionalProperties) {
            this.additionalProperties = additionalProperties;
            return this;
        }
        public JmsDestination build() {
            return new JmsDestination(this);
        }
    }

    @Override
    public String getId(){
        if (getAnnotatedEntity() != null && annotatedEntity.getId() != null) {
            return annotatedEntity.getId();
        }
        return super.getId();
    }
}
