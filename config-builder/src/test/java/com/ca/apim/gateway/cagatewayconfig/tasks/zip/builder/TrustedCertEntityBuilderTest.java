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
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Element;

import javax.net.ssl.HttpsURLConnection;
import java.io.File;
import java.math.BigInteger;
import java.net.URL;
import java.security.cert.X509Certificate;
import java.util.Base64;
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
    void buildTrustedCertFromUrl() throws Exception {
        //Set up Cert data
        final String GOOGLE_URL = "https://google.ca";
        URL url = new URL(GOOGLE_URL);
        HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
        conn.connect();
        X509Certificate googleCert = (X509Certificate) conn.getServerCertificates()[0];

        final TrustedCertEntityBuilder builder = new TrustedCertEntityBuilder(DocumentTools.INSTANCE.getDocumentBuilder().newDocument(), new IdGenerator());

        final Bundle bundle = new Bundle();
        final TrustedCert trustedCert = new TrustedCert();
        trustedCert.setProperties(ImmutableMap.of("key1", "value1", "key2", "value2"));
        trustedCert.setUrl(GOOGLE_URL);

        bundle.putAllTrustedCerts(ImmutableMap.of("fake-cert", trustedCert));

        final List<Entity> trustedCerts = builder.build(bundle);
        assertEquals(1, trustedCerts.size());

        final Element trustedCertEntityXml = verifyTrustedCertElement(trustedCerts);

        verifyProperties(trustedCertEntityXml);

        final Element certDataXml = getSingleElement(trustedCertEntityXml, CERT_DATA);
        assertEquals(googleCert.getIssuerDN().getName(), getSingleChildElementTextContent(certDataXml, ISSUER_NAME));
        assertEquals(googleCert.getSerialNumber(), new BigInteger(getSingleChildElementTextContent(certDataXml, SERIAL_NUMBER)));
        assertEquals(googleCert.getSubjectDN().getName(), getSingleChildElementTextContent(certDataXml, SUBJECT_NAME));
        assertEquals(Base64.getEncoder().encodeToString(googleCert.getEncoded()), getSingleChildElementTextContent(certDataXml, ENCODED));
    }

    @Test
    void buildTrustedCertUsingPem() throws DocumentParseException {
        final String trustedCertLocation = new File(getClass().getClassLoader().getResource("multi-cert.pem").getFile()).getPath();

        final TrustedCertEntityBuilder builder = new TrustedCertEntityBuilder(DocumentTools.INSTANCE.getDocumentBuilder().newDocument(), new IdGenerator());

        final Bundle bundle = new Bundle();
        final TrustedCert trustedCert = new TrustedCert();
        trustedCert.setFile("multi-cert.pem");
        trustedCert.setProperties(ImmutableMap.of("key1", "value1", "key2", "value2"));

        bundle.putAllTrustedCerts(ImmutableMap.of("fake-cert", trustedCert));
        bundle.putAllCertificateFiles(ImmutableMap.of("multi-cert.pem", trustedCertLocation));

        final List<Entity> trustedCerts = builder.build(bundle);
        assertEquals(1, trustedCerts.size());

        final Element trustedCertEntityXml = verifyTrustedCertElement(trustedCerts);

        verifyProperties(trustedCertEntityXml);

        verifyMultiCertDetails(trustedCertEntityXml);
    }

    @Test
    void buildTrustedCertUsingDer() throws DocumentParseException {
        final String trustedCertLocation = new File(getClass().getClassLoader().getResource("multi-cert.der").getFile()).getPath();

        final TrustedCertEntityBuilder builder = new TrustedCertEntityBuilder(DocumentTools.INSTANCE.getDocumentBuilder().newDocument(), new IdGenerator());

        final Bundle bundle = new Bundle();
        final TrustedCert trustedCert = new TrustedCert();
        trustedCert.setFile("multi-cert.pem");
        trustedCert.setProperties(ImmutableMap.of("key1", "value1", "key2", "value2"));

        bundle.putAllTrustedCerts(ImmutableMap.of("fake-cert", trustedCert));
        bundle.putAllCertificateFiles(ImmutableMap.of("multi-cert.pem", trustedCertLocation));

        final List<Entity> trustedCerts = builder.build(bundle);
        assertEquals(1, trustedCerts.size());

        final Element trustedCertEntityXml = verifyTrustedCertElement(trustedCerts);

        verifyProperties(trustedCertEntityXml);

        verifyMultiCertDetails(trustedCertEntityXml);
    }

    @Test
    void buildTrustedCertUsingCertData() throws DocumentParseException {
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

        final Element trustedCertEntityXml = verifyTrustedCertElement(trustedCerts);

        verifyProperties(trustedCertEntityXml);

        final Element certDataXml = getSingleElement(trustedCertEntityXml, CERT_DATA);
        assertEquals(EXPECT_ISSUER, getSingleChildElementTextContent(certDataXml, ISSUER_NAME));
        assertEquals(EXPECT_BIG_INT, new BigInteger(getSingleChildElementTextContent(certDataXml, SERIAL_NUMBER)));
        assertEquals(EXPECT_SUB_NAME, getSingleChildElementTextContent(certDataXml, SUBJECT_NAME));
        assertEquals(EXPECT_DATA, getSingleChildElementTextContent(certDataXml, ENCODED));
    }

    private void verifyProperties(Element trustedCertEntityXml) throws DocumentParseException {
        final Element trustedCertProps = getSingleElement(trustedCertEntityXml, PROPERTIES);
        final List<Element> propertyList = getChildElements(trustedCertProps, PROPERTY);
        assertEquals(2, propertyList.size());
        final Element property1 = propertyList.get(0);
        final Element property2 = propertyList.get(1);
        assertEquals("key1", property1.getAttributes().getNamedItem(ATTRIBUTE_KEY).getTextContent());
        assertEquals("key2", property2.getAttributes().getNamedItem(ATTRIBUTE_KEY).getTextContent());
        assertEquals("value1", getSingleChildElementTextContent(property1, STRING_VALUE));
        assertEquals("value2", getSingleChildElementTextContent(property2, STRING_VALUE));
    }

    private void verifyMultiCertDetails(Element trustedCertEntityXml) throws DocumentParseException {
        final Element certDataXml = getSingleElement(trustedCertEntityXml, CERT_DATA);
        assertEquals("C=UK, ST=Dublin, L=Dublin, O=CA Issuers Inc, OU=Issuers, CN=Simple Issuer 01, STREET=Culloden Street", getSingleChildElementTextContent(certDataXml, ISSUER_NAME));
        assertEquals(new BigInteger("7414677490662590507"), new BigInteger(getSingleChildElementTextContent(certDataXml, SERIAL_NUMBER)));
        assertEquals("C=UK, ST=Dublin, L=Dublin, O=CA Issuers Inc, OU=Issuers, CN=Simple Issuer 01, STREET=Culloden Street", getSingleChildElementTextContent(certDataXml, SUBJECT_NAME));
        assertEquals("MIIDADCCAmmgAwIBAgIIZuY55KMHSCswDQYJKoZIhvcNAQELBQAwgY8xGDAWBgNVBAkMD0N1bGxvZGVuIFN0cmVldDEZMBcGA1UEAwwQU2ltcGxlIElzc3VlciAwMTEQMA4GA1UECwwHSXNzdWVyczEXMBUGA1UECgwOQ0EgSXNzdWVycyBJbmMxDzANBgNVBAcMBkR1YmxpbjEPMA0GA1UECAwGRHVibGluMQswCQYDVQQGEwJVSzAeFw0xODA5MDYyMjUzMzdaFw0yNDAxMjIyMDA2NTVaMIGPMRgwFgYDVQQJDA9DdWxsb2RlbiBTdHJlZXQxGTAXBgNVBAMMEFNpbXBsZSBJc3N1ZXIgMDExEDAOBgNVBAsMB0lzc3VlcnMxFzAVBgNVBAoMDkNBIElzc3VlcnMgSW5jMQ8wDQYDVQQHDAZEdWJsaW4xDzANBgNVBAgMBkR1YmxpbjELMAkGA1UEBhMCVUswgZ8wDQYJKoZIhvcNAQEBBQADgY0AMIGJAoGBAIlFlR/yMwcVY6/VSpqa+/YSdJWBUvj+f8NbS1uebkBpSdUIBAK9VS6uP6m3kpzhCk42RdDU2qnEhnbVEwWYteY7SSBDra7bCxXkpnyjBdPf1igc0NHbjd283XXl6Tj4sOe2iArmc/OEE8oMu5m4UdZN8kTZVkPQ9SxuzJrnnIa5AgMBAAGjYzBhMB0GA1UdDgQWBBT0PCQYvL+gH3b33igNZh54P779VDAPBgNVHRMBAf8EBTADAQH/MB8GA1UdIwQYMBaAFPQ8JBi8v6AfdvfeKA1mHng/vv1UMA4GA1UdDwEB/wQEAwIBhjANBgkqhkiG9w0BAQsFAAOBgQA4alW2LSJaqm/cFUU2WpUfhwTTJ79Y3F866HHgrbc1Yh1MndWkeghpFXejw4nH8xt3XNFEp7BpCuoSdIIVHjABem1rtxYoV6eZ4UuefWTlFmVdma1VOBn7ZpfylrGMY8AQFs0JRHojBbEa3duilVoDEV3hC9eDL2LF5XeBlPFxrw==", getSingleChildElementTextContent(certDataXml, ENCODED));
    }

    @NotNull
    private Element verifyTrustedCertElement(List<Entity> trustedCerts) {
        final Entity trustedCertEntity = trustedCerts.get(0);
        assertEquals(TRUSTED_CERT_TYPE, trustedCertEntity.getType());
        assertNotNull(trustedCertEntity.getId());
        final Element trustedCertEntityXml = trustedCertEntity.getXml();
        assertEquals(TRUSTED_CERT, trustedCertEntityXml.getTagName());
        assertEquals("fake-cert", getSingleChildElementTextContent(trustedCertEntityXml, NAME));
        return trustedCertEntityXml;
    }

}