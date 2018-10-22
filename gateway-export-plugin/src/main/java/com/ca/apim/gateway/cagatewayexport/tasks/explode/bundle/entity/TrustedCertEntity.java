/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayexport.tasks.explode.bundle.entity;

import com.ca.apim.gateway.cagatewayexport.tasks.explode.bundle.Entity;

import javax.inject.Named;
import java.util.Map;

@Named("TRUSTED_CERT")
public class TrustedCertEntity implements Entity {
    private final String id;
    private final String name;
    private final Map<String, Object> properties;
    private final String encodedData;

    private TrustedCertEntity(final Builder builder) {
        id = builder.id;
        name = builder.name;
        properties = builder.properties;
        encodedData = builder.encodedData;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getName() {
        return name;
    }

    public Map<String, Object> getProperties() {
        return properties;
    }

    public String getEncodedData() {
        return encodedData;
    }

    public static class Builder {
        private String id;
        private String name;
        private Map<String, Object> properties;
        private String encodedData;

        public Builder id(String id) {
            this.id = id;
            return this;
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder properties(Map<String, Object> properties) {
            this.properties = properties;
            return this;
        }

        public Builder encodedData(String encodedData) {
            this.encodedData = encodedData;
            return this;
        }

        public TrustedCertEntity build() {
            return new TrustedCertEntity(this);
        }
    }
}
