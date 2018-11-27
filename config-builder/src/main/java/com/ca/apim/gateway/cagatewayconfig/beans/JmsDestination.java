/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayconfig.beans;

import com.ca.apim.gateway.cagatewayconfig.config.loader.ConfigLoadException;
import com.ca.apim.gateway.cagatewayconfig.config.spec.ConfigurationFile;
import com.ca.apim.gateway.cagatewayconfig.config.spec.EnvironmentType;
import com.ca.apim.gateway.cagatewayconfig.util.IdGenerator;
import com.fasterxml.jackson.annotation.JsonInclude;
import org.jetbrains.annotations.Nullable;

import javax.inject.Named;

import java.io.File;
import java.util.Map;

import static com.ca.apim.gateway.cagatewayconfig.config.spec.ConfigurationFile.FileType.JSON_YAML;
import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;
import static java.util.Arrays.stream;

@JsonInclude(NON_NULL)
@Named("JMS_ENDPOINT")
@ConfigurationFile(name = "jms-destinations", type= JSON_YAML)
@EnvironmentType("JMS_DESTINATION")
@SuppressWarnings("squid:S2068") // sonarcloud believes this is a hardcoded password
public class JmsDestination extends GatewayEntity {
    
    // Basics
    private boolean isInbound;
    private boolean isTemplate;
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
    
    // Outbound
    
    public JmsDestination() {
    }
    
    private JmsDestination(Builder builder) {
        setName(builder.name);
        setId(builder.id);
        isInbound = builder.isInbound;
        isTemplate = builder.isTemplate;
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
    }

    public boolean isInbound() {
        return isInbound;
    }

    public void setIsInbound(boolean isInbound) {
        this.isInbound = isInbound;
    }

    public boolean isTemplate() {
        return isTemplate;
    }

    public void setIsTemplate(boolean isTemplate) {
        this.isTemplate = isTemplate;
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

    @Override
    public void postLoad(String entityKey, Bundle bundle, @Nullable File rootFolder, IdGenerator idGenerator) {
        if (getJndiPasswordRef() != null && getJndiPassword() != null) {
            throw new ConfigLoadException("Cannot specify both a password reference and a password for JNDI password for JMS destination: " + entityKey);
        }

        if (getDestinationPasswordRef() != null && getDestinationPassword() != null) {
            throw new ConfigLoadException("Cannot specify both a password reference and a password for Destination password for JMS destination: " + entityKey);
        }
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
        private String name;
        private String id;
        private boolean isInbound;
        private boolean isTemplate;
        private String providerType;
        private String initialContextFactoryClassName;
        private String jndiUrl;
        private String jndiUsername;
        private String jndiPasswordRef;
        private String jndiPassword;
        private Map<String, Object> jndiProperties;
        private DestinationType destinationType;
        private String connectionFactoryName;
        private String destinationName;
        private String destinationUsername;
        private String destinationPasswordRef;
        private String destinationPassword;

        public Builder name(String name) {
            this.name = name;
            return this;
        }
        
        public Builder id(String id) {
            this.id = id;
            return this;
        }

        public Builder isInbound(boolean isInbound) {
            this.isInbound = isInbound;
            return this;
        }

        public Builder isTemplate(boolean isTemplate) {
            this.isTemplate = isTemplate;
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
        
        public JmsDestination build() {
            return new JmsDestination(this);
        }
    }
}
