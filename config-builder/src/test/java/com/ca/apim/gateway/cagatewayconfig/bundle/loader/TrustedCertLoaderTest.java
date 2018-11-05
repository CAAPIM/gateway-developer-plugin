/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayconfig.bundle.loader;

import com.ca.apim.gateway.cagatewayconfig.beans.Bundle;
import com.ca.apim.gateway.cagatewayconfig.beans.TrustedCert;
import com.ca.apim.gateway.cagatewayconfig.beans.TrustedCert.CertificateData;
import com.ca.apim.gateway.cagatewayconfig.util.entity.EntityTypes;
import com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentTools;
import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.beans.BeanInfo;
import java.beans.FeatureDescriptor;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.math.BigInteger;
import java.util.Map;
import java.util.function.Function;

import static com.ca.apim.gateway.cagatewayconfig.util.gateway.BuilderUtils.buildAndAppendPropertiesElement;
import static com.ca.apim.gateway.cagatewayconfig.util.gateway.BundleElementNames.*;
import static com.ca.apim.gateway.cagatewayconfig.util.gateway.CertificateUtils.createCertDataElementFromCert;
import static com.ca.apim.gateway.cagatewayconfig.util.properties.PropertyConstants.*;
import static com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentUtils.*;
import static java.util.Arrays.stream;
import static java.util.stream.Collectors.toMap;
import static org.junit.jupiter.api.Assertions.*;

class TrustedCertLoaderTest {

    private static final ImmutableMap<String, Object> CERT_PROPERTIES = ImmutableMap.<String, Object>builder()
            .put(VERIFY_HOSTNAME, true)
            .put(TRUSTED_FOR_SSL, true)
            .put(TRUSTED_AS_SAML_ATTESTING_ENTITY, true)
            .put(TRUST_ANCHOR, true)
            .put(REVOCATION_CHECKING_ENABLED, true)
            .put(TRUSTING_SIGNING_CLIENT_CERTS, true)
            .put(TRUSTED_SIGNING_SERVER_CERTS, true)
            .put(TRUSTED_AS_SAML_ISSUER, true)
            .build();

    @Test
    void load() throws Exception {
        TrustedCertLoader loader = new TrustedCertLoader();
        Bundle bundle = new Bundle();
        loader.load(bundle, buildTrustedCertElement(DocumentTools.INSTANCE.getDocumentBuilder().newDocument(), BigInteger.valueOf(123456L)));

        assertFalse(bundle.getTrustedCerts().isEmpty());
        assertEquals(1, bundle.getTrustedCerts().size());

        TrustedCert cert = bundle.getTrustedCerts().get("Cert");
        assertNotNull(cert);
        assertNotNull(cert.getCertificateData());

        CertificateData certData = cert.getCertificateData();
        assertEquals("Encoded", certData.getEncodedData());
        assertEquals("Issuer", certData.getIssuerName());
        assertEquals("SN", certData.getSubjectName());
        assertEquals(BigInteger.valueOf(123456L), certData.getSerialNumber());

        final BeanInfo info = Introspector.getBeanInfo(TrustedCert.class);
        final Map<String, PropertyDescriptor> properties = stream(info.getPropertyDescriptors()).collect(toMap(FeatureDescriptor::getName, Function.identity()));
        CERT_PROPERTIES.forEach((k,v) -> {
            try {
                boolean value = (boolean) properties.get(k).getReadMethod().invoke(cert);
                assertTrue(value, "property " + k + " is not true");
            } catch (Exception e) {
                throw new RuntimeException(e.getMessage(), e);
            }
        });
    }

    @Test
    void loadNoSerialNumber() throws Exception {
        TrustedCertLoader loader = new TrustedCertLoader();
        Bundle bundle = new Bundle();
        assertThrows(BundleLoadException.class, () -> loader.load(bundle, buildTrustedCertElement(DocumentTools.INSTANCE.getDocumentBuilder().newDocument(), null)));
    }

    private static Element buildTrustedCertElement(Document document, BigInteger serial) {
        final Element trustedCertElem = createElementWithAttributesAndChildren(
                document,
                TRUSTED_CERT,
                ImmutableMap.of(ATTRIBUTE_ID, "Cert"),
                createElementWithTextContent(document, NAME, "Cert")
        );
        Element certData = createElementWithChildren(
                document,
                CERT_DATA,
                createElementWithTextContent(document, ISSUER_NAME, "Issuer")
        );
        if (serial != null) {
            certData.appendChild(createElementWithTextContent(document, SERIAL_NUMBER, serial));
        }
        certData.appendChild(createElementWithTextContent(document, SUBJECT_NAME, "SN"));
        certData.appendChild(createElementWithTextContent(document, ENCODED, "Encoded"));
        trustedCertElem.appendChild(certData);

        buildAndAppendPropertiesElement(CERT_PROPERTIES, document, trustedCertElem);
        return createElementWithChildren(
                document,
                ITEM,
                createElementWithTextContent(document, ID, "id"),
                createElementWithTextContent(document, TYPE, EntityTypes.TRUSTED_CERT_TYPE),
                createElementWithChildren(
                        document,
                        RESOURCE,
                        trustedCertElem
                )
        );
    }
}