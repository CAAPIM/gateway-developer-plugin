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
import com.ca.apim.gateway.cagatewayconfig.util.file.SupplierWithIO;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;

import javax.inject.Named;
import java.io.File;
import java.io.InputStream;
import java.util.Collection;

import static com.ca.apim.gateway.cagatewayconfig.config.spec.ConfigurationFile.FileType.JSON_YAML;
import static com.fasterxml.jackson.annotation.JsonInclude.Include;
import static java.nio.file.Files.newInputStream;

@JsonInclude(Include.NON_NULL)
@SuppressWarnings("squid:S2068") // sonarcloud believes 'password' field names may have hardcoded passwords
@Named("SSG_KEY_ENTRY")
@ConfigurationFile(name = "private-keys", type = JSON_YAML)
@EnvironmentType("PRIVATE_KEY")
public class PrivateKey extends GatewayEntity {

    public static final String SSL_DEFAULT_PRIVATE_KEY = "SSL";

    private String keystore;
    private String algorithm;
    private String keyPassword;
    @JsonIgnore
    private KeyStoreType keyStoreType;
    private SupplierWithIO<InputStream> privateKeyFile;

    public PrivateKey() {
    }

    private PrivateKey(Builder builder) {
        setId(builder.id);
        setAlias(builder.alias);
        setKeyStoreType(builder.keystore);
        setAlgorithm(builder.algorithm);
    }

    @JsonIgnore
    public String getAlias() {
        return this.getName();
    }

    public void setAlias(String alias) {
        this.setName(alias);
    }

    public String getKeystore() {
        return keystore;
    }

    public void setKeystore(String keystore) {
        this.keystore = keystore;
    }

    public String getAlgorithm() {
        return algorithm;
    }

    public void setAlgorithm(String algorithm) {
        this.algorithm = algorithm;
    }

    public KeyStoreType getKeyStoreType() {
        return keyStoreType;
    }

    public void setKeyStoreType(KeyStoreType keyStoreType) {
        this.keyStoreType = keyStoreType;
    }

    public String getKeyPassword() {
        return keyPassword;
    }

    public void setKeyPassword(String keyPassword) {
        this.keyPassword = keyPassword;
    }

    public SupplierWithIO<InputStream> getPrivateKeyFile() {
        return privateKeyFile;
    }

    public void setPrivateKeyFile(SupplierWithIO<InputStream> privateKeyFile) {
        this.privateKeyFile = privateKeyFile;
    }

    public static class Builder {

        private String id;
        private String alias;
        private KeyStoreType keystore;
        private String algorithm;

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

        public PrivateKey build() {
            return new PrivateKey(this);
        }
    }

    @Override
    public void postLoad(String entityKey, Bundle bundle, File rootFolder, IdGenerator idGenerator) {
        setAlias(entityKey);
        setKeyStoreType(KeyStoreType.fromName(getKeystore()));
        if (bundle.getPrivateKeyFiles().get(getAlias()) != null) {
            setPrivateKeyFile(bundle.getPrivateKeyFiles().get(getAlias()));
        } else if (rootFolder != null && getPrivateKeyFile() == null) {
            loadPrivateKey(this, new File(rootFolder, "config/privateKeys"), false);
        }

        if (getPrivateKeyFile() != null) {
            bundle.getPrivateKeyFiles().put(getAlias(), getPrivateKeyFile());
        }
    }

    public static void loadFromDirectory(Collection<PrivateKey> privateKeys, File privateKeysDirectory, boolean failOnMissingKeys) {
        if (!privateKeys.isEmpty()) {
            if (!privateKeysDirectory.exists() && failOnMissingKeys) {
                throw new ConfigLoadException("Directory specified for private keys does not exist: " + privateKeysDirectory.getPath());
            }

            // load p12 file
            if (privateKeysDirectory.exists()) {
                privateKeys.forEach(k -> loadPrivateKey(k, privateKeysDirectory, failOnMissingKeys));
            }
        }
    }

    private static void loadPrivateKey(PrivateKey k, File privateKeysDirectory, boolean failOnMissingKeys) {
        File pk = new File(privateKeysDirectory, k.getAlias() + ".p12");
        if (pk.exists()) {
            k.setPrivateKeyFile(() -> newInputStream(pk.toPath()));
        } else if (failOnMissingKeys) {
            throw new ConfigLoadException("Private Key file for key '" + k.getAlias() + "' not found in the private keys directory specified: " + privateKeysDirectory.getPath());
        }
    }
}
