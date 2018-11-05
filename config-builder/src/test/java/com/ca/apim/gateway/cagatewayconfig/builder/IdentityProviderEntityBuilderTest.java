/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayconfig.builder;

import com.ca.apim.gateway.cagatewayconfig.beans.IdentityProvider.IdentityProviderType;
import com.ca.apim.gateway.cagatewayconfig.builder.EntityBuilder.BundleType;
import com.ca.apim.gateway.cagatewayconfig.beans.Bundle;
import com.ca.apim.gateway.cagatewayconfig.beans.TrustedCert;
import com.ca.apim.gateway.cagatewayconfig.beans.BindOnlyLdapIdentityProviderDetail;
import com.ca.apim.gateway.cagatewayconfig.beans.FederatedIdentityProviderDetail;
import com.ca.apim.gateway.cagatewayconfig.beans.IdentityProvider;
import com.ca.apim.gateway.cagatewayconfig.util.IdGenerator;
import com.ca.apim.gateway.cagatewayconfig.util.TestUtils;
import com.ca.apim.gateway.cagatewayconfig.util.entity.EntityTypes;
import com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentParseException;
import com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentTools;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.testcontainers.shaded.com.google.common.collect.ImmutableMap;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.ca.apim.gateway.cagatewayconfig.util.gateway.BundleElementNames.*;
import static com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentUtils.getChildElementAttributeValues;
import static com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentUtils.getSingleElement;
import static java.util.Arrays.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Created by chaoy01 on 2018-08-17.
 */
class IdentityProviderEntityBuilderTest {

    private static final String SIMPLE_LDAP_CONFIG = "simple ldap config";

    @Test
    void buildUnsupportedIDP() {
        final IdentityProviderEntityBuilder builder = new IdentityProviderEntityBuilder(new IdGenerator());
        final Bundle bundle = new Bundle();
        final IdentityProvider identityProvider = new IdentityProvider();
        identityProvider.setType(IdentityProviderType.INTERNAL);
        bundle.putAllIdentityProviders(new HashMap<String, IdentityProvider>() {{
            put("unsupported idp", identityProvider);
        }});
        assertThrows(EntityBuilderException.class, () -> builder.build(bundle, BundleType.ENVIRONMENT, DocumentTools.INSTANCE.getDocumentBuilder().newDocument()));
    }

    @Test
    void buildNoIdentityProviders() {
        final IdentityProviderEntityBuilder builder = new IdentityProviderEntityBuilder(new IdGenerator());
        final Bundle bundle = new Bundle();
        final List<Entity> identityProviderEntities = builder.build(bundle, BundleType.DEPLOYMENT, DocumentTools.INSTANCE.getDocumentBuilder().newDocument());
        assertEquals(0, identityProviderEntities.size());
    }

    @Test
    void buildBindOnlyIPWithoutIPDetail() {
        final IdentityProviderEntityBuilder builder = new IdentityProviderEntityBuilder(new IdGenerator());
        final Bundle bundle = new Bundle();
        final IdentityProvider identityProvider = new IdentityProvider();
        identityProvider.setType(IdentityProviderType.BIND_ONLY_LDAP);
        bundle.putAllIdentityProviders(new HashMap<String, IdentityProvider>() {{
            put("simple ldap config", identityProvider);
        }});
        assertThrows(EntityBuilderException.class, () -> builder.build(bundle, BundleType.ENVIRONMENT, DocumentTools.INSTANCE.getDocumentBuilder().newDocument()));
    }

    @Test
    void buildBindOnlyIPWithMissingDetails() {
        final IdentityProviderEntityBuilder builder = new IdentityProviderEntityBuilder(new IdGenerator());
        final Bundle bundle = new Bundle();
        final IdentityProvider identityProvider = new IdentityProvider();
        identityProvider.setType(IdentityProviderType.BIND_ONLY_LDAP);

        //omit the serverUrls
        final BindOnlyLdapIdentityProviderDetail identityProviderDetail = new BindOnlyLdapIdentityProviderDetail();
        identityProviderDetail.setUseSslClientAuthentication(false);
        identityProviderDetail.setBindPatternPrefix("testpre");
        identityProviderDetail.setBindPatternSuffix("testsuf");

        identityProvider.setIdentityProviderDetail(identityProviderDetail);
        bundle.putAllIdentityProviders(new HashMap<String, IdentityProvider>() {{
            put("simple ldap config", identityProvider);
        }});
        assertThrows(EntityBuilderException.class, () -> builder.build(bundle, BundleType.ENVIRONMENT, DocumentTools.INSTANCE.getDocumentBuilder().newDocument()));
    }

    @Test
    void buildOneBindOnlyIP() throws DocumentParseException {
        final IdentityProviderEntityBuilder builder = new IdentityProviderEntityBuilder(new IdGenerator());
        final Bundle bundle = new Bundle();
        final IdentityProvider identityProvider = createBindOnlyLdap();
        bundle.putAllIdentityProviders(new HashMap<String, IdentityProvider>() {{
            put(SIMPLE_LDAP_CONFIG, identityProvider);
        }});

        final List<Entity> identityProviders = builder.build(bundle, BundleType.ENVIRONMENT, DocumentTools.INSTANCE.getDocumentBuilder().newDocument());

        assertEquals(1, identityProviders.size());
        final Element identityProviderEntityXml = verifyIdProvider(identityProviders, "simple ldap config", IdentityProviderType.BIND_ONLY_LDAP);
        final Element idProviderProperties = getSingleElement(identityProviderEntityXml, PROPERTIES);
        final NodeList propertyList = idProviderProperties.getElementsByTagName(PROPERTY);
        assertEquals(2, propertyList.getLength());
        Node property1 = propertyList.item(0);
        Node property2 = propertyList.item(1);
        if (!"key1".equals(property1.getAttributes().getNamedItem(ATTRIBUTE_KEY).getTextContent())) {
            property2 = propertyList.item(0);
            property1 = propertyList.item(1);
        }
        assertEquals("key1", property1.getAttributes().getNamedItem(ATTRIBUTE_KEY).getTextContent());
        assertEquals("key2", property2.getAttributes().getNamedItem(ATTRIBUTE_KEY).getTextContent());
        assertEquals("value1", getSingleElement((Element) property1, STRING_VALUE).getTextContent());
        assertEquals("value2", getSingleElement((Element) property2, STRING_VALUE).getTextContent());

        final Element bindOnlyLdapIdentityProviderDetailXml = getSingleElement(identityProviderEntityXml, BIND_ONLY_ID_PROV_DETAIL);
        final Element serverUrls = getSingleElement(bindOnlyLdapIdentityProviderDetailXml, SERVER_URLS);
        final NodeList serverList = serverUrls.getElementsByTagName(STRING_VALUE);
        assertEquals(2, serverList.getLength());
        assertEquals("http://ldap:port", serverList.item(0).getTextContent());
        assertEquals("http://ldap:port2", serverList.item(1).getTextContent());
        final Element useSslClientAuthXml = getSingleElement(bindOnlyLdapIdentityProviderDetailXml, USE_SSL_CLIENT_AUTH);
        assertFalse(Boolean.parseBoolean(useSslClientAuthXml.getTextContent()));
        final Element bindPatternPrefix = getSingleElement(bindOnlyLdapIdentityProviderDetailXml, BIND_PATTERN_PREFIX);
        assertEquals("testpre", bindPatternPrefix.getTextContent());
        final Element bindPatternSuffix = getSingleElement(bindOnlyLdapIdentityProviderDetailXml, BIND_PATTERN_SUFFIX);
        assertEquals("testsuf", bindPatternSuffix.getTextContent());
    }

    @NotNull
    private static IdentityProvider createBindOnlyLdap() {
        final IdentityProvider identityProvider = new IdentityProvider();
        identityProvider.setType(IdentityProvider.IdentityProviderType.BIND_ONLY_LDAP);
        identityProvider.setProperties(new HashMap<String, Object>() {{
            put("key1", "value1");
            put("key2", "value2");
        }});

        final BindOnlyLdapIdentityProviderDetail identityProviderDetail = new BindOnlyLdapIdentityProviderDetail();
        identityProviderDetail.setUseSslClientAuthentication(false);
        identityProviderDetail.setBindPatternPrefix("testpre");
        identityProviderDetail.setBindPatternSuffix("testsuf");
        identityProviderDetail.setServerUrls(new LinkedHashSet<>(Arrays.asList("http://ldap:port", "http://ldap:port2")));
        identityProvider.setIdentityProviderDetail(identityProviderDetail);
        return identityProvider;
    }

    @Test
    void buildFedIPCertReferenceNotFound() {
        final IdentityProviderEntityBuilder builder = new IdentityProviderEntityBuilder(new IdGenerator());
        final IdentityProvider identityProvider = new IdentityProvider();
        identityProvider.setType(IdentityProviderType.FEDERATED);
        final Set<String> certReferences = new HashSet<>(asList("cert1","cert2"));
        final FederatedIdentityProviderDetail identityProviderDetail = new FederatedIdentityProviderDetail();
        identityProviderDetail.setCertificateReferences(certReferences);
        identityProvider.setIdentityProviderDetail(identityProviderDetail);
        final Bundle bundle = new Bundle();
        bundle.putAllIdentityProviders(new HashMap<String, IdentityProvider>() {{
            put("fail IDP", identityProvider);
        }});

        assertThrows(EntityBuilderException.class, () -> builder.build(bundle, BundleType.ENVIRONMENT, DocumentTools.INSTANCE.getDocumentBuilder().newDocument()));
    }

    @Test
    void buildFedIPMissingCertReferences() {
        final IdentityProviderEntityBuilder builder = new IdentityProviderEntityBuilder(new IdGenerator());
        final IdentityProvider identityProvider = new IdentityProvider();
        identityProvider.setType(IdentityProvider.IdentityProviderType.FEDERATED);
        final FederatedIdentityProviderDetail identityProviderDetail = new FederatedIdentityProviderDetail();
        identityProvider.setIdentityProviderDetail(identityProviderDetail);
        final Bundle bundle = new Bundle();
        bundle.putAllIdentityProviders(new HashMap<String, IdentityProvider>() {{
            put("fail IDP", identityProvider);
        }});

        assertThrows(EntityBuilderException.class, () -> builder.build(bundle, BundleType.ENVIRONMENT, DocumentTools.INSTANCE.getDocumentBuilder().newDocument()));
    }

    @Test
    void buildFedIPNoDetails() throws DocumentParseException {
        final IdentityProviderEntityBuilder builder = new IdentityProviderEntityBuilder(new IdGenerator());
        final IdentityProvider identityProvider = new IdentityProvider();
        identityProvider.setType(IdentityProviderType.FEDERATED);
        final Bundle bundle = new Bundle();
        bundle.putAllIdentityProviders(new HashMap<String, IdentityProvider>() {{
            put("fed IP no details", identityProvider);
        }});

        final List<Entity> identityProviders = builder.build(bundle, BundleType.ENVIRONMENT, DocumentTools.INSTANCE.getDocumentBuilder().newDocument());

        Element identityProviderEntityXml = verifyIdProvider(identityProviders, "fed IP no details", IdentityProviderType.FEDERATED);
        //No identityProviderDetail element
        assertThrows(DocumentParseException.class, () -> getSingleElement(identityProviderEntityXml, FEDERATED_ID_PROV_DETAIL));
    }

    @Test
    void buildOneFedIPWithCertReferences() throws DocumentParseException {
        final String FED_ID_NAME = "simple fed id with cert references";
        final IdentityProviderEntityBuilder builder = new IdentityProviderEntityBuilder(new IdGenerator());
        final Bundle bundle = new Bundle();
        final TrustedCert cert1 = new TrustedCert();
        cert1.setId("cert1");
        final TrustedCert cert2 = new TrustedCert();
        cert2.setId("cert2");
        bundle.putAllTrustedCerts(new HashMap<String, TrustedCert>() {{
            put("cert1", cert1);
            put("cert2", cert2);
        }});
        final IdentityProvider identityProvider = new IdentityProvider();
        identityProvider.setType(IdentityProviderType.FEDERATED);
        final Set<String> certReferences = new LinkedHashSet<>(asList("cert1","cert2"));
        final FederatedIdentityProviderDetail identityProviderDetail = new FederatedIdentityProviderDetail();
        identityProviderDetail.setCertificateReferences(certReferences);
        identityProvider.setIdentityProviderDetail(identityProviderDetail);
        bundle.putAllIdentityProviders(new HashMap<String, IdentityProvider>() {{
            put(FED_ID_NAME, identityProvider);
        }});

        final List<Entity> identityProviders = builder.build(bundle, BundleType.ENVIRONMENT, DocumentTools.INSTANCE.getDocumentBuilder().newDocument());

        assertEquals(1, identityProviders.size());
        final Element identityProviderEntityXml = verifyIdProvider(identityProviders, FED_ID_NAME, IdentityProviderType.FEDERATED);
        final Element fedIdentityProviderDetailXml = getSingleElement(identityProviderEntityXml, FEDERATED_ID_PROV_DETAIL);
        final Element certRefs = getSingleElement(fedIdentityProviderDetailXml, CERTIFICATE_REFERENCES);
        final List<String> certList = getChildElementAttributeValues(certRefs, REFERENCE, ATTRIBUTE_ID);
        assertEquals(2, certList.size());
        assertEquals(certList.get(0), "cert1");
        assertEquals(certList.get(1), "cert2");
    }

    @NotNull
    private Element verifyIdProvider(List<Entity> identityProviders, String idProvName, IdentityProviderType bindOnlyLdap) throws DocumentParseException {
        final Entity identityProviderEntity = identityProviders.get(0);
        assertEquals("ID_PROVIDER_CONFIG", identityProviderEntity.getType());
        assertNotNull(identityProviderEntity.getId());
        final Element identityProviderEntityXml = identityProviderEntity.getXml();
        assertEquals(ID_PROV, identityProviderEntityXml.getTagName());
        final Element identityProviderNameXml = getSingleElement(identityProviderEntityXml, NAME);
        assertEquals(idProvName, identityProviderNameXml.getTextContent());
        final Element identityProviderTypeXml = getSingleElement(identityProviderEntityXml, ID_PROV_TYPE);
        assertEquals(bindOnlyLdap.getValue(), identityProviderTypeXml.getTextContent());
        return identityProviderEntityXml;
    }

    @Test
    void buildEmptyDeploymentBundle() {
        TestUtils.testDeploymentBundleWithOnlyMapping(
                new IdentityProviderEntityBuilder(new IdGenerator()),
                new Bundle(),
                DocumentTools.INSTANCE.getDocumentBuilder().newDocument(),
                EntityTypes.ID_PROVIDER_CONFIG_TYPE,
                Collections.emptyList()
        );
    }

    @Test
    void buildDeploymentBundle() {
        final Bundle bundle = new Bundle();
        bundle.putAllIdentityProviders(ImmutableMap.of(SIMPLE_LDAP_CONFIG, createBindOnlyLdap()));

        TestUtils.testDeploymentBundleWithOnlyMapping(
                new IdentityProviderEntityBuilder(new IdGenerator()),
                bundle,
                DocumentTools.INSTANCE.getDocumentBuilder().newDocument(),
                EntityTypes.ID_PROVIDER_CONFIG_TYPE,
                Stream.of(SIMPLE_LDAP_CONFIG).collect(Collectors.toList())
        );
    }

}