/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayconfig.builder;

import com.ca.apim.gateway.cagatewayconfig.beans.Bundle;
import com.ca.apim.gateway.cagatewayconfig.beans.TrustedCert;
import com.ca.apim.gateway.cagatewayconfig.beans.TrustedCert.CertificateData;
import com.ca.apim.gateway.cagatewayconfig.util.IdGenerator;
import com.ca.apim.gateway.cagatewayconfig.util.TestUtils;
import com.ca.apim.gateway.cagatewayconfig.util.entity.EntityTypes;
import com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentParseException;
import com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentTools;
import com.google.common.collect.ImmutableMap;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.w3c.dom.Element;

import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.security.*;
import java.security.cert.*;
import java.security.cert.Certificate;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.ca.apim.gateway.cagatewayconfig.util.entity.EntityTypes.TRUSTED_CERT_TYPE;
import static com.ca.apim.gateway.cagatewayconfig.util.gateway.BundleElementNames.*;
import static com.ca.apim.gateway.cagatewayconfig.util.gateway.ConnectionUtils.createAcceptAllSocketFactory;
import static com.ca.apim.gateway.cagatewayconfig.util.properties.PropertyConstants.*;
import static com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentUtils.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class TrustedCertEntityBuilderTest {

    private static final String URL_NAME = "https://www.ca.com";
    private static final String CERT_NAME = "multi-cert";
    private CertificateFactory certFact;
    private X509Certificate testCert;

    @BeforeAll
    void setUp() throws Exception {
        certFact = CertificateFactory.getInstance("X.509");
        final File trustedCertLocation = new File(getClass().getClassLoader().getResource(CERT_NAME + ".pem").getFile());
        try (FileInputStream is = new FileInputStream(trustedCertLocation)) {
            testCert = (X509Certificate) certFact.generateCertificate(is);
        }
    }

    @Test
    void buildNoTrustedCerts() {
        final TrustedCertEntityBuilder builder = new TrustedCertEntityBuilder(new IdGenerator(), createAcceptAllSocketFactory(), certFact);
        final Bundle bundle = new Bundle();
        final List<Entity> trustedCertEntities = builder.build(bundle, EntityBuilder.BundleType.ENVIRONMENT, DocumentTools.INSTANCE.getDocumentBuilder().newDocument());
        assertEquals(0, trustedCertEntities.size());
    }

    @Test
    void buildTrustedCertFromNonExistentFile() {
        final TrustedCertEntityBuilder builder = new TrustedCertEntityBuilder(new IdGenerator(), null, certFact);

        final Bundle bundle = new Bundle();
        final TrustedCert trustedCert = new TrustedCert(ImmutableMap.of(VERIFY_HOSTNAME, true), null);
        bundle.putAllTrustedCerts(ImmutableMap.of(CERT_NAME, trustedCert));
        // No certs to load from
        assertThrows(EntityBuilderException.class, () -> builder.build(bundle, EntityBuilder.BundleType.ENVIRONMENT, DocumentTools.INSTANCE.getDocumentBuilder().newDocument()));
    }

    @Test
    void buildTrustedCertFromUrl() throws Exception {
        //Set up mock socket factory
        SSLSocketFactory sf = mock(SSLSocketFactory.class);
        SSLSocket socket = mock(SSLSocket.class);
        SSLSession sslSession = mock(SSLSession.class);
        when(sf.createSocket(anyString(), anyInt())).thenReturn(socket);
        when(socket.getSession()).thenReturn(sslSession);
        when(sslSession.getPeerCertificates()).thenReturn(new X509Certificate[]{testCert});

        final TrustedCertEntityBuilder builder = new TrustedCertEntityBuilder(new IdGenerator(), sf, certFact);

        final Bundle bundle = new Bundle();
        final TrustedCert trustedCert = new TrustedCert(ImmutableMap.of(VERIFY_HOSTNAME, true), null);
        bundle.putAllTrustedCerts(ImmutableMap.of(URL_NAME, trustedCert));

        final List<Entity> trustedCerts = builder.build(bundle, EntityBuilder.BundleType.ENVIRONMENT, DocumentTools.INSTANCE.getDocumentBuilder().newDocument());
        assertEquals(1, trustedCerts.size());

        final Element trustedCertEntityXml = verifyTrustedCertElement(trustedCerts, true);
        verifyProperties(trustedCertEntityXml);
        verifyCertDetails(trustedCertEntityXml);
    }

    @Test
    void tryBuildTrustedCertFromUrlReturningEmpty() throws Exception {
        //Set up mock socket factory
        SSLSocketFactory sf = mock(SSLSocketFactory.class);
        SSLSocket socket = mock(SSLSocket.class);
        SSLSession sslSession = mock(SSLSession.class);
        when(sf.createSocket(anyString(), anyInt())).thenReturn(socket);
        when(socket.getSession()).thenReturn(sslSession);
        when(sslSession.getPeerCertificates()).thenReturn(new X509Certificate[]{});

        final TrustedCertEntityBuilder builder = new TrustedCertEntityBuilder(new IdGenerator(), sf, certFact);

        final Bundle bundle = new Bundle();
        final TrustedCert trustedCert = new TrustedCert(ImmutableMap.of(VERIFY_HOSTNAME, true), null);
        bundle.putAllTrustedCerts(ImmutableMap.of(URL_NAME, trustedCert));

        assertThrows(EntityBuilderException.class, () -> builder.build(bundle, EntityBuilder.BundleType.ENVIRONMENT, DocumentTools.INSTANCE.getDocumentBuilder().newDocument()));
    }

    @Test
    void tryBuildTrustedCertFromUrlInvalidCertType() throws Exception {
        //Set up mock socket factory
        SSLSocketFactory sf = mock(SSLSocketFactory.class);
        SSLSocket socket = mock(SSLSocket.class);
        SSLSession sslSession = mock(SSLSession.class);
        when(sf.createSocket(anyString(), anyInt())).thenReturn(socket);
        when(socket.getSession()).thenReturn(sslSession);
        when(sslSession.getPeerCertificates()).thenReturn(new Certificate[]{ new Certificate("MockCertificate") {
            @Override
            public byte[] getEncoded() {
                return new byte[0];
            }

            @Override
            public void verify(PublicKey key) {

            }

            @Override
            public void verify(PublicKey key, String sigProvider) {

            }

            @Override
            public String toString() {
                return null;
            }

            @Override
            public PublicKey getPublicKey() {
                return null;
            }
        }});

        final TrustedCertEntityBuilder builder = new TrustedCertEntityBuilder(new IdGenerator(), sf, certFact);

        final Bundle bundle = new Bundle();
        final TrustedCert trustedCert = new TrustedCert(ImmutableMap.of(VERIFY_HOSTNAME, true), null);
        bundle.putAllTrustedCerts(ImmutableMap.of(URL_NAME, trustedCert));

        assertThrows(EntityBuilderException.class, () -> builder.build(bundle, EntityBuilder.BundleType.ENVIRONMENT, DocumentTools.INSTANCE.getDocumentBuilder().newDocument()));
    }

    @Test
    void tryBuildTrustedCertFromUrlThrowingException() throws Exception {
        //Set up mock socket factory
        SSLSocketFactory sf = mock(SSLSocketFactory.class);
        SSLSocket socket = mock(SSLSocket.class);
        SSLSession sslSession = mock(SSLSession.class);
        when(sf.createSocket(anyString(), anyInt())).thenReturn(socket);
        when(socket.getSession()).thenReturn(sslSession);
        doThrow(IOException.class).when(socket).startHandshake();

        final TrustedCertEntityBuilder builder = new TrustedCertEntityBuilder(new IdGenerator(), sf, certFact);

        final Bundle bundle = new Bundle();
        final TrustedCert trustedCert = new TrustedCert(ImmutableMap.of(VERIFY_HOSTNAME, true), null);
        bundle.putAllTrustedCerts(ImmutableMap.of(URL_NAME, trustedCert));

        assertThrows(EntityBuilderException.class, () -> builder.build(bundle, EntityBuilder.BundleType.ENVIRONMENT, DocumentTools.INSTANCE.getDocumentBuilder().newDocument()));
    }

    @Test
    void buildTrustedCertUsingPem() throws Exception {
        final File trustedCertLocation = new File(getClass().getClassLoader().getResource(CERT_NAME + ".pem").getFile());

        final TrustedCertEntityBuilder builder = new TrustedCertEntityBuilder(new IdGenerator(), null, certFact);
        final Bundle bundle = new Bundle();
        final TrustedCert trustedCert = new TrustedCert(ImmutableMap.of(VERIFY_HOSTNAME, true), null);
        bundle.putAllTrustedCerts(ImmutableMap.of(CERT_NAME, trustedCert));
        bundle.putAllCertificateFiles(ImmutableMap.of(CERT_NAME, () -> new FileInputStream(trustedCertLocation)));

        final List<Entity> trustedCerts = builder.build(bundle, EntityBuilder.BundleType.ENVIRONMENT, DocumentTools.INSTANCE.getDocumentBuilder().newDocument());
        assertEquals(1, trustedCerts.size());

        final Element trustedCertEntityXml = verifyTrustedCertElement(trustedCerts, false);
        verifyProperties(trustedCertEntityXml);
        verifyCertDetails(trustedCertEntityXml);
    }

    @Test
    void buildTrustedCertUsingDer() throws Exception {
        final File trustedCertLocation = new File(getClass().getClassLoader().getResource(CERT_NAME + ".der").getFile());

        final TrustedCertEntityBuilder builder = new TrustedCertEntityBuilder(new IdGenerator(), null, certFact);
        final Bundle bundle = new Bundle();
        final TrustedCert trustedCert = new TrustedCert(ImmutableMap.of(VERIFY_HOSTNAME, true), null);
        bundle.putAllTrustedCerts(ImmutableMap.of(CERT_NAME, trustedCert));
        bundle.putAllCertificateFiles(ImmutableMap.of(CERT_NAME, () -> new FileInputStream(trustedCertLocation)));

        final List<Entity> trustedCerts = builder.build(bundle, EntityBuilder.BundleType.ENVIRONMENT, DocumentTools.INSTANCE.getDocumentBuilder().newDocument());
        assertEquals(1, trustedCerts.size());

        final Element trustedCertEntityXml = verifyTrustedCertElement(trustedCerts, false);

        verifyProperties(trustedCertEntityXml);
        verifyCertDetails(trustedCertEntityXml);
    }

    @Test
    void buildTrustedCertUsingCertData() throws Exception {
        final String EXPECT_ISSUER = "issuer";
        final BigInteger EXPECT_BIG_INT = new BigInteger("1234");
        final String EXPECT_SUB_NAME = "subName";
        final String EXPECT_DATA = "data";

        final TrustedCertEntityBuilder builder = new TrustedCertEntityBuilder(new IdGenerator(), null, certFact);
        final Bundle bundle = new Bundle();
        final CertificateData certData = new CertificateData(EXPECT_ISSUER, EXPECT_BIG_INT, EXPECT_SUB_NAME, EXPECT_DATA);
        final TrustedCert trustedCert = new TrustedCert(ImmutableMap.of(VERIFY_HOSTNAME, true), certData);
        bundle.putAllTrustedCerts(ImmutableMap.of(CERT_NAME, trustedCert));

        final List<Entity> trustedCerts = builder.build(bundle, EntityBuilder.BundleType.ENVIRONMENT, DocumentTools.INSTANCE.getDocumentBuilder().newDocument());
        assertEquals(1, trustedCerts.size());

        final Element trustedCertEntityXml = verifyTrustedCertElement(trustedCerts, false);

        verifyProperties(trustedCertEntityXml);

        final Element certDataXml = getSingleElement(trustedCertEntityXml, CERT_DATA);
        assertEquals(EXPECT_ISSUER, getSingleChildElementTextContent(certDataXml, ISSUER_NAME));
        assertEquals(EXPECT_BIG_INT, new BigInteger(getSingleChildElementTextContent(certDataXml, SERIAL_NUMBER)));
        assertEquals(EXPECT_SUB_NAME, getSingleChildElementTextContent(certDataXml, SUBJECT_NAME));
        assertEquals(EXPECT_DATA, getSingleChildElementTextContent(certDataXml, ENCODED));
    }

    @Test
    void buildDeploymentBundle() {
        final String EXPECT_ISSUER = "issuer";
        final BigInteger EXPECT_BIG_INT = new BigInteger("1234");
        final String EXPECT_SUB_NAME = "subName";
        final String EXPECT_DATA = "data";

        final Bundle bundle = new Bundle();
        final CertificateData certData = new CertificateData(EXPECT_ISSUER, EXPECT_BIG_INT, EXPECT_SUB_NAME, EXPECT_DATA);
        final TrustedCert trustedCert = new TrustedCert(ImmutableMap.of(VERIFY_HOSTNAME, true), certData);
        bundle.putAllTrustedCerts(ImmutableMap.of(CERT_NAME, trustedCert));

        TestUtils.testDeploymentBundleWithOnlyMapping(
                new TrustedCertEntityBuilder(new IdGenerator(), null, certFact),
                bundle,
                DocumentTools.INSTANCE.getDocumentBuilder().newDocument(),
                EntityTypes.TRUSTED_CERT_TYPE,
                Stream.of(CERT_NAME).collect(Collectors.toList())
        );
    }

    @Test
    void tryGetMalformedURL() {
        final TrustedCertEntityBuilder builder = new TrustedCertEntityBuilder(new IdGenerator(), null, certFact);
        assertThrows(EntityBuilderException.class, () -> builder.getUrl("ttps://malformed.url.com"));
    }

    private void verifyProperties(Element trustedCertEntityXml) throws DocumentParseException {
        final Element trustedCertProps = getSingleElement(trustedCertEntityXml, PROPERTIES);
        final List<Element> propertyList = getChildElements(trustedCertProps, PROPERTY);
        assertEquals(8, propertyList.size());
        List<String> availableProps = new ArrayList<>();
        Collections.addAll(availableProps,
                TRUST_ANCHOR,
                TRUSTED_AS_SAML_ATTESTING_ENTITY,
                TRUSTED_FOR_SSL,
                TRUSTED_SIGNING_SERVER_CERTS,
                TRUSTING_SIGNING_CLIENT_CERTS,
                VERIFY_HOSTNAME,
                REVOCATION_CHECKING_ENABLED,
                TRUSTED_AS_SAML_ISSUER
        );

        propertyList.forEach(e ->  {
            String propKey = e.getAttributes().getNamedItem(ATTRIBUTE_KEY).getTextContent();
            assertTrue(availableProps.contains(propKey));
            if (propKey.equals(VERIFY_HOSTNAME)) {
                //only set this one property to true before verifying
                assertTrue(Boolean.parseBoolean(getSingleChildElementTextContent(e, BOOLEAN_VALUE)));
            } else {
                assertFalse(Boolean.parseBoolean(getSingleChildElementTextContent(e, BOOLEAN_VALUE)));

            }
            availableProps.remove(e.getAttributes().getNamedItem(ATTRIBUTE_KEY).getTextContent());
        });
        assertEquals(0, availableProps.size());
    }

    private void verifyCertDetails(Element trustedCertEntityXml) throws DocumentParseException, CertificateEncodingException {
        final Element certDataXml = getSingleElement(trustedCertEntityXml, CERT_DATA);
        assertEquals(testCert.getIssuerDN().getName(), getSingleChildElementTextContent(certDataXml, ISSUER_NAME));
        assertEquals(testCert.getSerialNumber(), new BigInteger(getSingleChildElementTextContent(certDataXml, SERIAL_NUMBER)));
        assertEquals(testCert.getSubjectDN().getName(), getSingleChildElementTextContent(certDataXml, SUBJECT_NAME));
        assertEquals(Base64.getEncoder().encodeToString(testCert.getEncoded()), getSingleChildElementTextContent(certDataXml, ENCODED));
    }

    @NotNull
    private Element verifyTrustedCertElement(List<Entity> trustedCerts, boolean isUrl) {
        final Entity trustedCertEntity = trustedCerts.get(0);
        assertEquals(TRUSTED_CERT_TYPE, trustedCertEntity.getType());
        assertNotNull(trustedCertEntity.getId());
        final Element trustedCertEntityXml = trustedCertEntity.getXml();
        assertEquals(TRUSTED_CERT, trustedCertEntityXml.getTagName());
        assertEquals(isUrl? URL_NAME : CERT_NAME, getSingleChildElementTextContent(trustedCertEntityXml, NAME));
        return trustedCertEntityXml;
    }

}