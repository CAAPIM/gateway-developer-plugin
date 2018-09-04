/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayconfig.tasks.zip.builder;

import com.ca.apim.gateway.cagatewayconfig.tasks.zip.beans.Bundle;
import com.ca.apim.gateway.cagatewayconfig.tasks.zip.beans.identityprovider.BindOnlyLdapIdentityProviderDetail;
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

import static com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentUtils.getSingleElement;
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
        identityProvider.setType(IdentityProvider.IdentityProviderType.BIND_ONLY_LDAP);
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
        identityProvider.setType(IdentityProvider.IdentityProviderType.BIND_ONLY_LDAP);

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
        identityProvider.setType(IdentityProvider.IdentityProviderType.BIND_ONLY_LDAP);
        identityProvider.setProperties(new HashMap<String, String>() {{
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
        assertEquals("l7:IdentityProvider", identityProviderEntityXml.getTagName());
        final Element identityProviderNameXml = getSingleElement(identityProviderEntityXml, "l7:Name");
        assertEquals("simple ldap config", identityProviderNameXml.getTextContent());
        final Element identityProviderTypeXml = getSingleElement(identityProviderEntityXml, "l7:IdentityProviderType");
        assertEquals(IdentityProvider.IdentityProviderType.BIND_ONLY_LDAP.getValue(), identityProviderTypeXml.getTextContent());

        final Element idProviderProperties = getSingleElement(identityProviderEntityXml, "l7:Properties");
        final NodeList propertyList = idProviderProperties.getElementsByTagName("l7:Property");
        assertEquals(2, propertyList.getLength());
        Node property1 = propertyList.item(0);
        Node property2 = propertyList.item(1);
        if (!"property.key1".equals(property1.getAttributes().getNamedItem("key").getTextContent())) {
            property2 = propertyList.item(0);
            property1 = propertyList.item(1);
        }
        assertEquals("property.key1", property1.getAttributes().getNamedItem("key").getTextContent());
        assertEquals("property.key2", property2.getAttributes().getNamedItem("key").getTextContent());
        assertEquals("value1", getSingleElement((Element) property1, "l7:StringValue").getTextContent());
        assertEquals("value2", getSingleElement((Element) property2, "l7:StringValue").getTextContent());

        final Element bindOnlyLdapIdentityProviderDetailXml = getSingleElement(identityProviderEntityXml, "l7:BindOnlyLdapIdentityProviderDetail");
        final Element serverUrls = getSingleElement(bindOnlyLdapIdentityProviderDetailXml, "l7:ServerUrls");
        final NodeList serverList = serverUrls.getElementsByTagName("l7:StringValue");
        assertEquals(2, serverList.getLength());
        assertEquals("http://ldap:port", serverList.item(0).getTextContent());
        assertEquals("http://ldap:port2", serverList.item(1).getTextContent());
        final Element useSslClientAuthXml = getSingleElement(bindOnlyLdapIdentityProviderDetailXml, "l7:UseSslClientAuthentication");
        assertFalse(Boolean.parseBoolean(useSslClientAuthXml.getTextContent()));
        final Element bindPatternPrefix = getSingleElement(bindOnlyLdapIdentityProviderDetailXml, "l7:BindPatternPrefix");
        assertEquals("testpre", bindPatternPrefix.getTextContent());
        final Element bindPatternSuffix = getSingleElement(bindOnlyLdapIdentityProviderDetailXml, "l7:BindPatternSuffix");
        assertEquals("testsuf", bindPatternSuffix.getTextContent());
    }

}