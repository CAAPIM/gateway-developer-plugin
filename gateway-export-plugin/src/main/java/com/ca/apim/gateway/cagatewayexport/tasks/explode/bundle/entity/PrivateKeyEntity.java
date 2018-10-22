/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayexport.tasks.explode.bundle.entity;

import com.ca.apim.gateway.cagatewayexport.tasks.explode.bundle.Entity;

import javax.inject.Named;
import java.util.ArrayList;
import java.util.List;

import static java.util.Collections.unmodifiableList;

@Named("SSG_KEY_ENTRY")
public class PrivateKeyEntity implements Entity {

    public static final String SSL_DEFAULT_PRIVATE_KEY = "SSL";

    private final String id;
    private final String alias;
    private final KeyStoreType keystore;
    private final String algorithm;
    private final List<String> certificateChainData;

    private PrivateKeyEntity(Builder builder) {
        id = builder.id;
        alias = builder.alias;
        keystore = builder.keystore;
        algorithm = builder.algorithm;
        certificateChainData = unmodifiableList(builder.certificateChainData);
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getName() {
        return alias;
    }

    public KeyStoreType getKeystore() {
        return keystore;
    }

    public String getAlgorithm() {
        return algorithm;
    }

    public List<String> getCertificateChainData() {
        return certificateChainData;
    }

    public static class Builder {

        private String id;
        private String alias;
        private KeyStoreType keystore;
        private String algorithm;
        private List<String> certificateChainData = new ArrayList<>();

        public Builder setId(String id) {
            this.id = id;
            return this;
        }

        public Builder setAlias(String alias) {
            this.alias = alias;
            return this;
        }

        public Builder setKeystore(KeyStoreType keystore) {
            this.keystore = keystore;
            return this;
        }

        public Builder setAlgorithm(String algorithm) {
            this.algorithm = algorithm;
            return this;
        }

        public Builder setCertificateChainData(List<String> certificateChainData) {
            this.certificateChainData = certificateChainData;
            return this;
        }

        public PrivateKeyEntity build() {
            return new PrivateKeyEntity(this);
        }
    }
}
