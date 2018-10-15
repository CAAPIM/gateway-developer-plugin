/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayconfig.tasks.zip.builder;

import com.ca.apim.gateway.cagatewayconfig.tasks.zip.beans.Bundle;
import com.ca.apim.gateway.cagatewayconfig.tasks.zip.beans.PrivateKey;
import com.ca.apim.gateway.cagatewayconfig.util.keystore.KeystoreHelper;
import com.google.common.collect.ImmutableMap;
import org.jetbrains.annotations.NotNull;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.File;
import java.nio.file.Files;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.List;
import java.util.stream.Stream;

import static com.ca.apim.gateway.cagatewayconfig.util.entity.EntityTypes.PRIVATE_KEY_TYPE;
import static com.ca.apim.gateway.cagatewayconfig.util.gateway.BuilderUtils.buildAndAppendPropertiesElement;
import static com.ca.apim.gateway.cagatewayconfig.util.gateway.BundleElementNames.*;
import static com.ca.apim.gateway.cagatewayconfig.util.gateway.CertificateUtils.createCertDataElementFromCert;
import static com.ca.apim.gateway.cagatewayconfig.util.gateway.MappingActions.NEW_OR_EXISTING;
import static com.ca.apim.gateway.cagatewayconfig.util.gateway.MappingProperties.FAIL_ON_NEW;
import static com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentUtils.createElementWithAttributes;
import static com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentUtils.createElementWithChildren;
import static java.util.stream.Collectors.toList;

@Singleton
public class PrivateKeyEntityBuilder implements EntityBuilder {

    private static final Integer ORDER = 1200;

    private final KeystoreHelper keystoreHelper;

    @Inject
    PrivateKeyEntityBuilder(final KeystoreHelper keystoreHelper) {
        this.keystoreHelper = keystoreHelper;
    }

    @Override
    public List<Entity> build(Bundle bundle, BundleType bundleType, Document document) {
        return bundle.getPrivateKeys().entrySet().stream().map(e -> buildPrivateKeyEntity(bundle, e.getKey(), e.getValue(), document)).collect(toList());
    }

    private Entity buildPrivateKeyEntity(Bundle bundle, String alias, PrivateKey privateKey, Document document) {
        final String id = privateKey.getKeyStoreType().generateKeyId(alias);
        final Element privateKeyElem = createElementWithAttributes(
                document,
                PRIVATE_KEY,
                ImmutableMap.of(
                        ATTRIBUTE_ID, id,
                        ATTRIBUTE_KEYSTORE_ID, privateKey.getKeyStoreType().getId(),
                        ATTRIBUTE_ALIAS, alias)
        );
        buildAndAppendCertificateChainElement(bundle, privateKey, privateKeyElem, document);
        buildAndAppendPropertiesElement(ImmutableMap.of(ATTRIBUTE_KEY_ALGORITHM, privateKey.getAlgorithm()), document, privateKeyElem);

        Entity entity = EntityBuilderHelper.getEntityWithNameMapping(PRIVATE_KEY_TYPE, alias, id, privateKeyElem);
        entity.setMappingAction(NEW_OR_EXISTING);
        entity.setMappingProperty(FAIL_ON_NEW, true);
        return entity;
    }

    private void buildAndAppendCertificateChainElement(Bundle bundle, PrivateKey privateKey, Element privateKeyElem, Document document) {
        File privateKeyFile = new File(bundle.getPrivateKeysDirectory() + File.separator + privateKey.getAlias() + ".p12");
        if (!privateKeyFile.exists()) {
            throw new EntityBuilderException("Private Key file for key '" + privateKey.getAlias() + "' not found in the private keys directory specified");
        }
        privateKey.setPrivateKeyFile(() -> Files.newInputStream(privateKeyFile.toPath()));
        final KeyStore keyStore = keystoreHelper.loadKeyStore(privateKey);
        final Certificate[] certificates = keystoreHelper.loadCertificatesForPrivateKey(privateKey, keyStore);

        final Element[] certificatesElements = Stream.of(certificates)
                .map(c -> createCertDataElementFromCert((X509Certificate) c, document)).toArray(Element[]::new);
        privateKeyElem.appendChild(createElementWithChildren(document, CERTIFICATE_CHAIN, certificatesElements));
    }

    @Override
    public @NotNull Integer getOrder() {
        return ORDER;
    }
}
