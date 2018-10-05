/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayconfig.tasks.zip.builder;

import com.ca.apim.gateway.cagatewayconfig.tasks.zip.beans.Bundle;
import com.ca.apim.gateway.cagatewayconfig.tasks.zip.beans.PrivateKey;
import com.ca.apim.gateway.cagatewayconfig.util.file.FileUtils;
import com.ca.apim.gateway.cagatewayconfig.util.gateway.CertificateUtils;
import com.google.common.collect.ImmutableMap;
import org.jetbrains.annotations.NotNull;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.File;
import java.security.cert.CertificateFactory;
import java.util.List;
import java.util.stream.Stream;

import static com.ca.apim.gateway.cagatewayconfig.util.entity.EntityTypes.PRIVATE_KEY_TYPE;
import static com.ca.apim.gateway.cagatewayconfig.util.gateway.BuilderUtils.buildAndAppendPropertiesElement;
import static com.ca.apim.gateway.cagatewayconfig.util.gateway.BundleElementNames.*;
import static com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentUtils.createElementWithAttributes;
import static com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentUtils.createElementWithChildren;
import static java.util.Arrays.sort;
import static java.util.Comparator.comparing;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;

@Singleton
public class PrivateKeyEntityBuilder implements EntityBuilder {

    private static final Integer ORDER = 1200;
    private final CertificateFactory certificateFactory;

    @Inject
    PrivateKeyEntityBuilder(final CertificateFactory certificateFactory) {
        this.certificateFactory = certificateFactory;
    }

    @Override
    public List<Entity> build(Bundle bundle, Document document) {
        return bundle.getPrivateKeys().entrySet().stream().map(e -> buildPrivateKeyEntity(e.getKey(), e.getValue(), document)).collect(toList());
    }

    private Entity buildPrivateKeyEntity(String alias, PrivateKey privateKey, Document document) {
        final String id = privateKey.getKeyStoreType().generateKeyId(alias);
        final Element privateKeyElem = createElementWithAttributes(
                document,
                PRIVATE_KEY,
                ImmutableMap.of(
                        ATTRIBUTE_ID, id,
                        ATTRIBUTE_KEYSTORE_ID, privateKey.getKeyStoreType().getId(),
                        ATTRIBUTE_ALIAS, alias)
        );
        buildAndAppendCertificateChainElement(privateKey, privateKeyElem, document);
        buildAndAppendPropertiesElement(ImmutableMap.of("keyAlgorithm", privateKey.getAlgorithm()), document, privateKeyElem);

        return new Entity(PRIVATE_KEY_TYPE, alias, id, privateKeyElem);
    }

    private void buildAndAppendCertificateChainElement(PrivateKey privateKey, Element privateKeyElem, Document document) {
        File[] certificateFiles = ofNullable(new File(privateKey.getPrivateKeyDirectory(), "certificateChain").listFiles())
                .orElse(new File[0]);
        sort(certificateFiles, comparing(File::getName));
        final Element[] certificates = Stream.of(certificateFiles)
                .map(f -> CertificateUtils.buildCertDataFromFile(f, document, certificateFactory)).toArray(Element[]::new);
        privateKeyElem.appendChild(createElementWithChildren(document, CERTIFICATE_CHAIN, certificates));
    }

    @Override
    public @NotNull Integer getOrder() {
        return ORDER;
    }
}
