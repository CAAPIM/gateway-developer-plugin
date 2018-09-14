/*
 * Copyright (c) 2018. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayconfig.tasks.zip.builder;

import com.ca.apim.gateway.cagatewayconfig.tasks.zip.beans.Bundle;
import com.ca.apim.gateway.cagatewayconfig.tasks.zip.beans.TrustedCert;
import com.ca.apim.gateway.cagatewayconfig.util.IdGenerator;
import com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentParseException;
import com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentTools;
import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Element;

import java.math.BigInteger;
import java.util.List;

import static com.ca.apim.gateway.cagatewayconfig.tasks.zip.beans.TrustedCert.*;
import static com.ca.apim.gateway.cagatewayconfig.util.entity.EntityTypes.TRUSTED_CERT_TYPE;
import static com.ca.apim.gateway.cagatewayconfig.util.gateway.BundleElementNames.*;
import static com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentUtils.getChildElements;
import static com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentUtils.getSingleChildElementTextContent;
import static com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentUtils.getSingleElement;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class TrustedCertEntityBuilderTest {

    @Test
    void buildNoTrustedCerts() {
        final TrustedCertEntityBuilder builder = new TrustedCertEntityBuilder(DocumentTools.INSTANCE.getDocumentBuilder().newDocument(), new IdGenerator());
        final Bundle bundle = new Bundle();
        final List<Entity> trustedCertEntities = builder.build(bundle);
        assertEquals(0, trustedCertEntities.size());
    }

    @Test
    void buildTrustedCert() throws DocumentParseException {
        final String EXPECT_ISSUER = "issuer";
        final BigInteger EXPECT_BIG_INT = new BigInteger("1234");
        final String EXPECT_SUB_NAME = "subName";
        final String EXPECT_DATA = "data";

        final TrustedCertEntityBuilder builder = new TrustedCertEntityBuilder(DocumentTools.INSTANCE.getDocumentBuilder().newDocument(), new IdGenerator());

        final Bundle bundle = new Bundle();
        final CertificateData certData = new CertificateData(EXPECT_ISSUER, EXPECT_BIG_INT, EXPECT_SUB_NAME, EXPECT_DATA);
        final TrustedCert trustedCert = new TrustedCert(ImmutableMap.of("key1", "value1", "key2", "value2"), certData);

        bundle.putAllTrustedCerts(ImmutableMap.of("fake-cert", trustedCert));

        final List<Entity> trustedCerts = builder.build(bundle);
        assertEquals(1, trustedCerts.size());

        final Entity trustedCertEntity = trustedCerts.get(0);
        assertEquals(TRUSTED_CERT_TYPE, trustedCertEntity.getType());
        assertNotNull(trustedCertEntity.getId());
        final Element trustedCertEntityXml = trustedCertEntity.getXml();
        assertEquals(TRUSTED_CERT, trustedCertEntityXml.getTagName());
        assertEquals("fake-cert", getSingleChildElementTextContent(trustedCertEntityXml, NAME));

        final Element trustedCertProps = getSingleElement(trustedCertEntityXml, PROPERTIES);
        final List<Element> propertyList = getChildElements(trustedCertProps, PROPERTY);
        assertEquals(2, propertyList.size());
        final Element property1 = propertyList.get(0);
        final Element property2 = propertyList.get(1);
        assertEquals("key1", property1.getAttributes().getNamedItem(ATTRIBUTE_KEY).getTextContent());
        assertEquals("key2", property2.getAttributes().getNamedItem(ATTRIBUTE_KEY).getTextContent());
        assertEquals("value1", getSingleChildElementTextContent(property1, STRING_VALUE));
        assertEquals("value2", getSingleChildElementTextContent(property2, STRING_VALUE));

        final Element certDataXml = getSingleElement(trustedCertEntityXml, CERT_DATA);
        assertEquals(EXPECT_ISSUER, getSingleChildElementTextContent(certDataXml, ISSUER_NAME));
        assertEquals(EXPECT_BIG_INT, new BigInteger(getSingleChildElementTextContent(certDataXml, SERIAL_NUMBER)));
        assertEquals(EXPECT_SUB_NAME, getSingleChildElementTextContent(certDataXml, SUBJECT_NAME));
        assertEquals(EXPECT_DATA, getSingleChildElementTextContent(certDataXml, ENCODED));
    }

}