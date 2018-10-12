/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayconfig.util.keystore;

import com.ca.apim.gateway.cagatewayconfig.tasks.zip.beans.PrivateKey;
import com.google.common.annotations.VisibleForTesting;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.RandomStringUtils;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.jetbrains.annotations.NotNull;

import javax.inject.Singleton;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.*;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.util.*;
import java.util.stream.Collectors;

import static com.google.common.base.MoreObjects.firstNonNull;
import static java.security.KeyStore.getInstance;

@Singleton
@SuppressWarnings("squid:S2068") // sonarcloud believes 'password' field names may have hardcoded passwords
public class KeystoreHelper {

    public static final String ENV_VAR_KEYSTORE_PATH = "ENV.PRIVATE_KEYS.PATH";

    private static final String ENV_VAR_KEY_PASSWORD_FORMAT = "ENV.PRIVATE_KEY_PASSWORD.%s";
    private static final SecureRandom RANDOM = new SecureRandom();
    private final char[] keystorePassword;

    KeystoreHelper() {
        // Generate a random passphrase with any type of char and using a secure random generator, in order to encrypt the secrets.
        final String password = RandomStringUtils.random(64, 0, 0, true, true, null, RANDOM);
        this.keystorePassword = Base64.getEncoder().encodeToString(password.getBytes(StandardCharsets.UTF_8)).toCharArray();
    }

    /**
     * Create a PKCS12 KeyStore and write it to a byte array, to be used during the environment application.
     *
     * @param keystorePath path where the key files are located
     * @param environmentProperties env properties with the key passwords
     * @return a byte array containing the keystore data
     */
    public byte[] createKeyStoreFromEnvironment(String keystorePath, Map<String, String> environmentProperties) {
        final File[] keyFiles = firstNonNull(Paths.get(keystorePath).toFile().listFiles(), new File[0]);
        final List<PrivateKey> privateKeys = Arrays.stream(keyFiles)
                .filter(f -> f.getName().endsWith(".p12"))
                .map(f -> createPrivateKeyFromEnvironment(f, environmentProperties))
                .filter(p -> p.getKeyPassword() != null) // ignoring keys with unspecified passwords
                .collect(Collectors.toList());
        return createKeyStore(privateKeys);
    }

    private PrivateKey createPrivateKeyFromEnvironment(File privateKeyFile, Map<String, String> environmentProperties) {
        PrivateKey privateKey = new PrivateKey();
        privateKey.setAlias(FilenameUtils.getBaseName(privateKeyFile.getName()));
        privateKey.setPrivateKeyFile(() -> Files.newInputStream(privateKeyFile.toPath()));
        privateKey.setKeyPassword(environmentProperties.get(String.format(ENV_VAR_KEY_PASSWORD_FORMAT, privateKey.getAlias())));
        return privateKey;
    }

    @VisibleForTesting
    byte[] createKeyStore(Collection<PrivateKey> privateKeys) {
        // if no private Keys specified do nothing
        if (privateKeys.isEmpty()) {
            return new byte[0];
        }

        KeyStore ks = createKeyStoreInstance(privateKeys);

        // transform it to byte array so it can be written properly anywhere
        return toBytes(ks);
    }

    @VisibleForTesting
    byte[] toBytes(KeyStore ks) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        try {
            ks.store(stream, keystorePassword);
        } catch (IOException | NoSuchAlgorithmException | CertificateException | KeyStoreException e) {
            throw new KeyStoreCreationException("Error saving Key Store", e);
        }
        return stream.toByteArray();
    }

    @VisibleForTesting
    KeyStore createKeyStoreInstance(Collection<PrivateKey> privateKeys) {
        Security.insertProviderAt(new BouncyCastleProvider(), 1);

        // create a default Keystore
        KeyStore ks;
        try {
            ks = loadKeyStore(null, keystorePassword);
        } catch (CertificateException | NoSuchAlgorithmException | IOException | KeyStoreException e) {
            throw new KeyStoreCreationException("Error creating default key store", e);
        }

        // for each private key, loads its key and cert chain and add to the keystore
        privateKeys.forEach(pk -> storeKey(ks, pk));
        return ks;
    }

    @VisibleForTesting
    KeyStore loadKeyStore(InputStream stream, char[] password) throws CertificateException, NoSuchAlgorithmException, IOException, KeyStoreException {
        KeyStore ks = getInstance(getKeyStoreType());
        ks.load(stream, password);

        return ks;
    }

    @VisibleForTesting
    void storeKey(KeyStore ks, PrivateKey pk) {
        KeyStore privateKeyStore = loadKeyStore(pk);
        Key privateKey;
        try {
            privateKey = privateKeyStore.getKey(pk.getAlias(), pk.getKeyPassword().toCharArray());
        } catch (KeyStoreException | NoSuchAlgorithmException | UnrecoverableKeyException e) {
            throw new KeyStoreCreationException("Error loading key " + pk.getAlias() + " from KeyStore p12 file", e);
        }

        if (privateKey == null) {
            throw new KeyStoreCreationException("Key with alias '" + pk.getAlias() + "' not found in the p12 file");
        }

        Certificate[] certificates = loadCertificatesForPrivateKey(pk, privateKeyStore);

        try {
            ks.setKeyEntry(pk.getAlias(), privateKey, keystorePassword, certificates);
        } catch (KeyStoreException e) {
            throw new KeyStoreCreationException("Error adding Private Key '" + pk.getAlias() + "' to Key Store", e);
        }
    }

    @VisibleForTesting
    Certificate[] loadCertificatesForPrivateKey(PrivateKey pk, KeyStore privateKeyStore) {
        Certificate[] certificates;
        try {
            certificates = privateKeyStore.getCertificateChain(pk.getAlias());
        } catch (KeyStoreException e) {
            throw new KeyStoreCreationException("Error loading certificate chain for key " + pk.getAlias() + " from KeyStore p12 file", e);
        }
        return certificates;
    }

    @VisibleForTesting
    KeyStore loadKeyStore(PrivateKey pk) {
        try (InputStream stream = pk.getPrivateKeyFile().getWithIO()) {
            return loadKeyStore(stream, pk.getKeyPassword().toCharArray());
        } catch (IOException | KeyStoreException | NoSuchAlgorithmException | CertificateException e) {
            throw new KeyStoreCreationException("Error loading Key file for '" + pk.getAlias() + "'", e);
        }
    }

    @NotNull
    public char[] getKeystorePassword() {
        return keystorePassword;
    }

    @NotNull
    public String getKeyStoreType() {
        return "PKCS12";
    }
}
