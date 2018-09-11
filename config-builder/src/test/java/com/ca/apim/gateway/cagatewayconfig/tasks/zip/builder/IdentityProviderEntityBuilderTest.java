/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayconfig.tasks.zip.builder;

import com.ca.apim.gateway.cagatewayconfig.tasks.zip.beans.Bundle;
import com.ca.apim.gateway.cagatewayconfig.tasks.zip.beans.identityprovider.BindOnlyLdapIdentityProviderDetail;
import com.ca.apim.gateway.cagatewayconfig.tasks.zip.beans.identityprovider.FederatedIdentityProviderDetail;
import com.ca.apim.gateway.cagatewayconfig.tasks.zip.beans.identityprovider.IdentityProvider;
import com.ca.apim.gateway.cagatewayconfig.util.IdGenerator;
import com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentParseException;
import com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentTools;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import static com.ca.apim.gateway.cagatewayconfig.util.gateway.BundleElementNames.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Created by chaoy01 on 2018-08-17.
 */
class IdentityProviderEntityBuilderTest {

    @Test
    void buildNoIdentityProviders() {
        final IdentityProviderEntityBuilder builder = new IdentityProviderEntityBuilder(DocumentTools.INSTANCE.getDocumentBuilder().newDocument(), new IdGenerator());
        final Bundle bundle = new Bundle();
        final List<Entity> identityProviderEntities = builder.build(bundle);
        assertEquals(0, identityProviderEntities.size());
    }

    @Test
    void buildBindOnlyIPWithoutIPDetail() {
        final IdentityProviderEntityBuilder builder = new IdentityProviderEntityBuilder(DocumentTools.INSTANCE.getDocumentBuilder().newDocument(), new IdGenerator());
        final Bundle bundle = new Bundle();
        final IdentityProvider identityProvider = new IdentityProvider();
        identityProvider.setType(IdentityProvider.Type.BIND_ONLY_LDAP);
        bundle.putAllIdentityProviders(new HashMap<String, IdentityProvider>() {{
            put("simple ldap config", identityProvider);
        }});
        Assertions.assertThrows(EntityBuilderException.class, () -> builder.build(bundle));
    }

    @Test
    void buildBindOnlyIPWithMissingDetails() {
        final IdentityProviderEntityBuilder builder = new IdentityProviderEntityBuilder(DocumentTools.INSTANCE.getDocumentBuilder().newDocument(), new IdGenerator());
        final Bundle bundle = new Bundle();
        final IdentityProvider identityProvider = new IdentityProvider();
        identityProvider.setType(IdentityProvider.Type.BIND_ONLY_LDAP);

        //omit the serverUrls
        final BindOnlyLdapIdentityProviderDetail identityProviderDetail = new BindOnlyLdapIdentityProviderDetail();
        identityProviderDetail.setUseSslClientAuthentication(false);
        identityProviderDetail.setBindPatternPrefix("testpre");
        identityProviderDetail.setBindPatternSuffix("testsuf");

        identityProvider.setIdentityProviderDetail(identityProviderDetail);
        bundle.putAllIdentityProviders(new HashMap<String, IdentityProvider>() {{
            put("simple ldap config", identityProvider);
        }});
        Assertions.assertThrows(EntityBuilderException.class, () -> builder.build(bundle));
    }

    @Test
    void buildOneBindOnlyIP() throws DocumentParseException {
        final IdentityProviderEntityBuilder builder = new IdentityProviderEntityBuilder(DocumentTools.INSTANCE.getDocumentBuilder().newDocument(), new IdGenerator());

        final Bundle bundle = new Bundle();

        final IdentityProvider identityProvider = new IdentityProvider();
        identityProvider.setType(IdentityProvider.Type.BIND_ONLY_LDAP);
        identityProvider.setProperties(new HashMap<String, Object>() {{
            put("key1", "value1");
            put("key2", "value2");
        }});

        final BindOnlyLdapIdentityProviderDetail identityProviderDetail = new BindOnlyLdapIdentityProviderDetail();
        identityProviderDetail.setUseSslClientAuthentication(false);
        identityProviderDetail.setBindPatternPrefix("testpre");
        identityProviderDetail.setBindPatternSuffix("testsuf");
        identityProviderDetail.setServerUrls(Arrays.asList("http://ldap:port", "http://ldap:port2"));

        identityProvider.setIdentityProviderDetail(identityProviderDetail);
        bundle.putAllIdentityProviders(new HashMap<String, IdentityProvider>() {{
            put("simple ldap config", identityProvider);
        }});

        final List<Entity> identityProviders = builder.build(bundle);
        assertEquals(1, identityProviders.size());

        final Entity identityProviderEntity = identityProviders.get(0);
        assertEquals("ID_PROVIDER_CONFIG", identityProviderEntity.getType());
        assertNotNull(identityProviderEntity.getId());
        final Element identityProviderEntityXml = identityProviderEntity.getXml();
        assertEquals(ID_PROV, identityProviderEntityXml.getTagName());
        final Element identityProviderNameXml = DocumentTools.INSTANCE.getSingleElement(identityProviderEntityXml, NAME);
        assertEquals("simple ldap config", identityProviderNameXml.getTextContent());
        final Element identityProviderTypeXml = DocumentTools.INSTANCE.getSingleElement(identityProviderEntityXml, ID_PROV_TYPE);
        assertEquals(IdentityProvider.Type.BIND_ONLY_LDAP.getValue(), identityProviderTypeXml.getTextContent());

        final Element idProviderProperties = DocumentTools.INSTANCE.getSingleElement(identityProviderEntityXml, PROPERTIES);
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
        assertEquals("value1", DocumentTools.INSTANCE.getSingleElement((Element) property1, STRING_VALUE).getTextContent());
        assertEquals("value2", DocumentTools.INSTANCE.getSingleElement((Element) property2, STRING_VALUE).getTextContent());

        final Element bindOnlyLdapIdentityProviderDetailXml = DocumentTools.INSTANCE.getSingleElement(identityProviderEntityXml, BIND_ONLY_ID_PROV_DETAIL);
        final Element serverUrls = DocumentTools.INSTANCE.getSingleElement(bindOnlyLdapIdentityProviderDetailXml, SERVER_URLS);
        final List<String> serverList = DocumentTools.getChildElementsTextContents(serverUrls, STRING_VALUE);
        assertEquals(2, serverList.size());
        assertEquals("http://ldap:port", serverList.get(0));
        assertEquals("http://ldap:port2", serverList.get(1));
        final Element useSslClientAuthXml = DocumentTools.INSTANCE.getSingleElement(bindOnlyLdapIdentityProviderDetailXml, USE_SSL_CLIENT_AUTH);
        assertFalse(Boolean.parseBoolean(useSslClientAuthXml.getTextContent()));
        final Element bindPatternPrefix = DocumentTools.INSTANCE.getSingleElement(bindOnlyLdapIdentityProviderDetailXml, BIND_PATTERN_PREFIX);
        assertEquals("testpre", bindPatternPrefix.getTextContent());
        final Element bindPatternSuffix = DocumentTools.INSTANCE.getSingleElement(bindOnlyLdapIdentityProviderDetailXml, BIND_PATTERN_SUFFIX);
        assertEquals("testsuf", bindPatternSuffix.getTextContent());
    }

    @Test
    void buildOneFedIPWithCertReferences() throws DocumentParseException {
        final String FED_ID_NAME = "simple fed id with cert references";
        final IdentityProviderEntityBuilder builder = new IdentityProviderEntityBuilder(DocumentTools.INSTANCE.getDocumentBuilder().newDocument(), new IdGenerator());

        final Bundle bundle = new Bundle();

        final IdentityProvider identityProvider = new IdentityProvider();
        identityProvider.setType(IdentityProvider.Type.FEDERATED);

        final List<String> certReferences = Arrays.asList("cert1","cert2");
        final FederatedIdentityProviderDetail identityProviderDetail = new FederatedIdentityProviderDetail();
        identityProviderDetail.setCertificateReferences(certReferences);
        identityProvider.setIdentityProviderDetail(identityProviderDetail);
        bundle.putAllIdentityProviders(new HashMap<String, IdentityProvider>() {{
            put(FED_ID_NAME, identityProvider);
        }});

        final List<Entity> identityProviders = builder.build(bundle);
        assertEquals(1, identityProviders.size());

        final Entity identityProviderEntity = identityProviders.get(0);
        assertEquals("ID_PROVIDER_CONFIG", identityProviderEntity.getType());
        assertNotNull(identityProviderEntity.getId());
        final Element identityProviderEntityXml = identityProviderEntity.getXml();
        assertEquals(ID_PROV, identityProviderEntityXml.getTagName());
        final Element identityProviderNameXml = DocumentTools.INSTANCE.getSingleElement(identityProviderEntityXml, NAME);
        assertEquals(FED_ID_NAME, identityProviderNameXml.getTextContent());
        final Element identityProviderTypeXml = DocumentTools.INSTANCE.getSingleElement(identityProviderEntityXml, ID_PROV_TYPE);
        assertEquals(IdentityProvider.Type.FEDERATED.getValue(), identityProviderTypeXml.getTextContent());

        final Element fedIdentityProviderDetailXml = DocumentTools.INSTANCE.getSingleElement(identityProviderEntityXml, FEDERATED_ID_PROV_DETAIL);
        final Element certRefs = DocumentTools.INSTANCE.getSingleElement(fedIdentityProviderDetailXml, CERTIFICATE_REFERENCES);
        final List<String> certList = DocumentTools.getChildElementAttributeValues(certRefs, REFERENCE, ATTRIBUTE_ID);
        assertEquals(2, certList.size());
        assertEquals(certList.get(0), "cert1");
        assertEquals(certList.get(1), "cert2");
    }

}