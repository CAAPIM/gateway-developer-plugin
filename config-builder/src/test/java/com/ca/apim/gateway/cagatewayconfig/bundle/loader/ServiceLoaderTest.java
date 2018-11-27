/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayconfig.bundle.loader;

import com.ca.apim.gateway.cagatewayconfig.beans.Bundle;
import com.ca.apim.gateway.cagatewayconfig.beans.Service;
import com.ca.apim.gateway.cagatewayconfig.util.entity.EntityTypes;
import com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentTools;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static com.ca.apim.gateway.cagatewayconfig.util.TestUtils.assertPropertiesContent;
import static com.ca.apim.gateway.cagatewayconfig.util.TestUtils.createServiceXml;
import static org.junit.jupiter.api.Assertions.*;

class ServiceLoaderTest {

    @Test
    void load() {
        ServiceLoader loader = new ServiceLoader();
        Bundle bundle = new Bundle();
        loader.load(bundle, createServiceXml(DocumentTools.INSTANCE.getDocumentBuilder().newDocument(),
                true, false, false, false));

        assertFalse(bundle.getServices().isEmpty());
        assertEquals(1, bundle.getServices().size());
        Service service = bundle.getServices().get("service");
        assertNotNull(service);
        assertEquals("service", service.getName());
        assertEquals("/service", service.getUrl());
        assertEquals("id", service.getId());
        assertNotNull(service.getServiceDetailsElement());
        assertFalse(service.getHttpMethods().isEmpty());
        assertEquals(4, service.getHttpMethods().size());
        assertTrue(service.getHttpMethods().containsAll(Arrays.asList("GET", "POST", "PUT", "DELETE")));
        assertNotNull(service.getParentFolder());
        assertEquals("folder", service.getParentFolder().getId());
        assertEquals("policy", service.getPolicy());
        Map<String, Object> expected = new HashMap<>();
        expected.put("prop", "value");
        expected.put("ENV.prop", null);
        assertPropertiesContent(expected, service.getProperties());
    }

    @Test
    void soapServiceLoad() {
        ServiceLoader loader = new ServiceLoader();
        Bundle bundle = new Bundle();
        loader.load(bundle, createServiceXml(
                DocumentTools.INSTANCE.getDocumentBuilder().newDocument(), true, true, false, false
        ));

        assertFalse(bundle.getServices().isEmpty());
        assertEquals(1, bundle.getServices().size());
        Service service = bundle.getServices().get("service");
        assertNotNull(service);
        assertEquals("service", service.getName());
        assertEquals("/soap-service", service.getUrl());
        assertEquals("soapId", service.getId());
        assertNotNull(service.getServiceDetailsElement());
        assertFalse(service.getHttpMethods().isEmpty());
        assertEquals(4, service.getHttpMethods().size());
        assertTrue(service.getHttpMethods().containsAll(Arrays.asList("GET", "POST", "PUT", "DELETE")));
        assertNotNull(service.getParentFolder());
        assertEquals("folder", service.getParentFolder().getId());
        assertEquals("wsdl file", service.getWsdl().getWsdlXml());
        assertEquals("1.1", service.getWsdl().getSoapVersion());
        assertTrue(service.getWsdl().isWssProcessingEnabled());
        Map<String, Object> expected = new HashMap<>();
        expected.put("prop", "value");
        expected.put("ENV.prop", null);
        assertPropertiesContent(expected, service.getProperties());
    }

    @Test
    void testEntityType() {
        ServiceLoader loader = new ServiceLoader();
        assertEquals(EntityTypes.SERVICE_TYPE, loader.getEntityType());
    }

    @Test
    void soapServiceLoad_invalidbundle_throwsException() {
        assertThrows(BundleLoadException.class, () -> {
            ServiceLoader loader = new ServiceLoader();
            Bundle bundle = new Bundle();
            loader.load(bundle, createServiceXml(
                    DocumentTools.INSTANCE.getDocumentBuilder().newDocument(), true, true, false, true
            ));
        });
        assertThrows(BundleLoadException.class, () -> {
            ServiceLoader loader = new ServiceLoader();
            Bundle bundle = new Bundle();
            loader.load(bundle, createServiceXml(
                    DocumentTools.INSTANCE.getDocumentBuilder().newDocument(), true, true, true, false
            ));
        });
    }
}
