/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayconfig.tasks.zip.builder;

import com.ca.apim.gateway.cagatewayconfig.tasks.zip.beans.Bundle;
import com.ca.apim.gateway.cagatewayconfig.tasks.zip.beans.PrivateKey;
import com.ca.apim.gateway.cagatewayconfig.util.gateway.CertificateUtils;
import com.google.common.collect.ImmutableMap;
import org.jetbrains.annotations.NotNull;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.inject.Singleton;
import java.math.BigInteger;
import java.util.List;

import static com.ca.apim.gateway.cagatewayconfig.util.entity.EntityTypes.PRIVATE_KEY_TYPE;
import static com.ca.apim.gateway.cagatewayconfig.util.gateway.BundleElementNames.*;
import static com.ca.apim.gateway.cagatewayconfig.util.gateway.MappingActions.NEW_OR_EXISTING;
import static com.ca.apim.gateway.cagatewayconfig.util.gateway.MappingProperties.FAIL_ON_NEW;
import static com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentUtils.createElementWithAttributes;
import static com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentUtils.createElementWithChildren;
import static java.util.stream.Collectors.toList;

@Singleton
public class PrivateKeyEntityBuilder implements EntityBuilder {

    private static final Integer ORDER = 1200;

    @Override
    public List<Entity> build(Bundle bundle, BundleType bundleType, Document document) {
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
        // this needs to be added in order for the Gateway to be able to load the bundle with the certificate.
        buildAndAppendCertificateChainElement(privateKeyElem, document);

        Entity entity = EntityBuilderHelper.getEntityWithNameMapping(PRIVATE_KEY_TYPE, alias, id, privateKeyElem);
        entity.setMappingAction(NEW_OR_EXISTING);
        entity.setMappingProperty(FAIL_ON_NEW, true);
        return entity;
    }

    private void buildAndAppendCertificateChainElement(Element privateKeyElem, Document document) {
        // Need to add certificate data here in order for the gateway to be able to load the bundle. The cert data will not be loaded
        privateKeyElem.appendChild(createElementWithChildren(document, CERTIFICATE_CHAIN, CertificateUtils.createCertDataElementFromCert("", BigInteger.valueOf(0), "", "MIIBfTCCASegAwIBAgIJAPH69zKKw4ixMA0GCSqGSIb3DQEBBQUAMA8xDTALBgNVBAMTBHRlc3QwHhcNMTgxMDEzMDMyODI1WhcNMzgxMDA4MDMyODI1WjAPMQ0wCwYDVQQDEwR0ZXN0MFwwDQYJKoZIhvcNAQEBBQADSwAwSAJBAIS+Vr8zPOBmSclkUtW/z0UXaMjhg7dix6IUZs+UoSiw/2GXfU2vc3renVAbn3AZaJEqnxgrcX4nldqt0WBIP4sCAwEAAaNmMGQwDgYDVR0PAQH/BAQDAgXgMBIGA1UdJQEB/wQIMAYGBFUdJQAwHQYDVR0OBBYEFN/aeDDEAB6MTxZhMhf/eJKnmaE5MB8GA1UdIwQYMBaAFN/aeDDEAB6MTxZhMhf/eJKnmaE5MA0GCSqGSIb3DQEBBQUAA0EAdolvh7bMX5ZMkM/yntJlBdzS8ukM/ULh8I11wKd6dDltyMuk9rOP0iEk1nsSFuFL0uQ4kIe12KyDwr8ns7VKvQ==", document)));
    }

    @Override
    public @NotNull Integer getOrder() {
        return ORDER;
    }
}
