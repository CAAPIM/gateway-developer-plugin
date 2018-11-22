/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayconfig.beans;

import com.ca.apim.gateway.cagatewayconfig.config.spec.ConfigurationFile;
import com.ca.apim.gateway.cagatewayconfig.config.spec.EnvironmentType;
import com.fasterxml.jackson.annotation.JsonInclude;

import javax.inject.Named;

import static com.ca.apim.gateway.cagatewayconfig.config.spec.ConfigurationFile.FileType.JSON_YAML;
import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;

@JsonInclude(NON_NULL)
@Named("JMS_ENDPOINT")
@ConfigurationFile(name = "jms-destinations", type= JSON_YAML)
@EnvironmentType("JMS_DESTINATION")
public class JmsDestination extends GatewayEntity {
    private boolean isInbound;
    private boolean isTemplate;
    private String providerType;
    
    public JmsDestination() {
    }
    
    private JmsDestination(Builder builder) {
        setName(builder.name);
        setId(builder.id);
        isInbound = builder.isInbound;
        isTemplate = builder.isTemplate;
        providerType = builder.providerType;
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
    
    public static class Builder {
        private String name;
        private String id;
        private boolean isInbound;
        private boolean isTemplate;
        private String providerType;

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
        
        public JmsDestination build() {
            return new JmsDestination(this);
        }
    }
}
