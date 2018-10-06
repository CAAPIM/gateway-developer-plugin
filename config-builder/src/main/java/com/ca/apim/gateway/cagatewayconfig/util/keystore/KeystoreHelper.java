/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayconfig.util.keystore;

import com.ca.apim.gateway.cagatewayconfig.tasks.zip.beans.PrivateKey;
import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.*;
import java.nio.file.Files;
import java.security.*;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.util.Collection;

import static com.ca.apim.gateway.cagatewayconfig.util.gateway.CertificateUtils.loadCertificateFromFile;
import static java.security.KeyStore.getInstance;
import static java.util.Arrays.sort;
import static java.util.Arrays.stream;
import static java.util.Comparator.comparing;
import static java.util.Optional.ofNullable;

@Singleton
public class KeystoreHelper {

    // TODO add a proper password
    private static final char[] KEYSTORE = "GW_KEYSTORE".toCharArray();
    private static final String P12_FILE_NAME = "key.p12";
    private static final String TXT_FILE_NAME = "key.txt";

    private final CertificateFactory certificateFactory;

    @Inject
    KeystoreHelper(final CertificateFactory certificateFactory) {
        this.certificateFactory = certificateFactory;
    }

    public byte[] createKeyStore(Collection<PrivateKey> privateKeys) {
        Security.addProvider(new BouncyCastleProvider());

        // create a default Keystore
        KeyStore ks;
        try {
            ks = loadKeyStore(null, KEYSTORE);
        } catch (CertificateException | NoSuchAlgorithmException | IOException | KeyStoreException e) {
            throw new KeyStoreCreationException("Error creating default key store", e);
        }

        // for each private key, loads its key and cert chain and add to the keystore
        privateKeys.forEach(pk -> storeKey(certificateFactory, ks, pk));

        // transform it to byte array so it can be written properly anywhere
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        try {
            ks.store(stream, KEYSTORE);
        } catch (IOException | NoSuchAlgorithmException | CertificateException | KeyStoreException e) {
            throw new KeyStoreCreationException("Error saving Key Store", e);
        }
        return stream.toByteArray();
    }

    private KeyStore loadKeyStore(InputStream stream, char[] password) throws CertificateException, NoSuchAlgorithmException, IOException, KeyStoreException {
        KeyStore ks = getInstance("PKCS12");
        ks.load(stream, password);

        return ks;
    }

    private void storeKey(CertificateFactory certificateFactory, KeyStore ks, PrivateKey pk) {
        try {
            ks.setKeyEntry(pk.getAlias(), loadKey(pk), KEYSTORE, loadCertificateChain(pk, certificateFactory));
        } catch (KeyStoreException e) {
            throw new KeyStoreCreationException("Error adding Private Key '" + pk.getAlias() + "' to Key Store", e);
        }
    }

    private Key loadKey(PrivateKey pk) {
        File p12File = new File(pk.getPrivateKeyDirectory(), P12_FILE_NAME);
        File txtFile = new File(pk.getPrivateKeyDirectory(), TXT_FILE_NAME);

        if (p12File.exists() && txtFile.exists()) {
            throw new KeyStoreCreationException("Can have the Private Key '" + pk.getAlias() + "' stored in p12 or txt files, but not both");
        }
        if (p12File.exists()) {
            return loadKeyFromPKCS12KeyStore(pk, p12File);
        }
        if (txtFile.exists()) {
            return loadKeyFromTextFile(pk, txtFile);
        }

        throw new KeyStoreCreationException("Key file for Private Key " + pk.getAlias() + " not found");
    }

    private Key loadKeyFromTextFile(PrivateKey pk, File txtFile) {
        try (InputStreamReader reader = new InputStreamReader(Files.newInputStream(txtFile.toPath()))) {
            PEMParser parser = new PEMParser(reader);
            PrivateKeyInfo privateKeyInfo = (PrivateKeyInfo) parser.readObject();

            return new JcaPEMKeyConverter().getPrivateKey(privateKeyInfo);
        } catch (IOException e) {
            throw new KeyStoreCreationException("Error loading Key file for '" + pk.getAlias() + "'", e);
        }
    }

    private Key loadKeyFromPKCS12KeyStore(PrivateKey pk, File p12File) {
        try (InputStream stream = Files.newInputStream(p12File.toPath())) {
            KeyStore pks = loadKeyStore(stream, pk.getKeyPassword().toCharArray());
            return pks.getKey(pk.getAlias(), pk.getKeyPassword().toCharArray());
        } catch (IOException | KeyStoreException | NoSuchAlgorithmException | UnrecoverableKeyException | CertificateException e) {
            throw new KeyStoreCreationException("Error loading Key file for '" + pk.getAlias() + "'", e);
        }
    }

    private Certificate[] loadCertificateChain(PrivateKey privateKey, CertificateFactory certificateFactory) {
        File[] certificateFiles = ofNullable(new File(privateKey.getPrivateKeyDirectory(), "certificateChain").listFiles())
                .orElse(new File[0]);
        sort(certificateFiles, comparing(File::getName));

        return stream(certificateFiles)
                .map(f -> loadCertificateFromFile(
                        () -> Files.newInputStream(f.toPath()),
                        certificateFactory)
                )
                .toArray(Certificate[]::new);
    }
}
