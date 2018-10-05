/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayconfig.tasks.zip.builder;

import com.ca.apim.gateway.cagatewayconfig.tasks.zip.beans.Bundle;
import com.ca.apim.gateway.cagatewayconfig.tasks.zip.beans.Folder;
import com.ca.apim.gateway.cagatewayconfig.tasks.zip.beans.Policy;
import com.ca.apim.gateway.cagatewayconfig.tasks.zip.beans.Service;
import com.ca.apim.gateway.cagatewayconfig.util.IdGenerator;
import com.ca.apim.gateway.cagatewayconfig.util.file.DocumentFileUtils;
import com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentParseException;
import com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentTools;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentUtils.getSingleElement;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class ServiceEntityBuilderTest {

    @Test
    public void buildNoServices() {
        ServiceEntityBuilder builder = new ServiceEntityBuilder(DocumentFileUtils.INSTANCE, new IdGenerator());

        Bundle bundle = new Bundle();

        List<Entity> serviceEntities = builder.build(bundle, EntityBuilder.BundleType.DEPLOYMENT, DocumentTools.INSTANCE.getDocumentBuilder().newDocument());

        assertEquals(0, serviceEntities.size());
    }

    @Test
    public void buildServicesWithoutPolicy() {
        ServiceEntityBuilder builder = new ServiceEntityBuilder(DocumentFileUtils.INSTANCE, new IdGenerator());

        Bundle bundle = new Bundle();

        Service service = new Service();
        service.setHttpMethods(Stream.of("POST", "GET").collect(Collectors.toSet()));
        service.setUrl("/my/service/url");
        service.setProperties(new HashMap<String, String>() {{
            put("key1", "value1");
            put("key2", "value2");
        }});

        bundle.putAllServices(new HashMap<String, Service>() {{
            put("/my/policy/path", service);
        }});
        assertThrows(EntityBuilderException.class, () -> builder.build(bundle, EntityBuilder.BundleType.DEPLOYMENT, DocumentTools.INSTANCE.getDocumentBuilder().newDocument()));
    }

    @Test
    public void buildOneServices() throws DocumentParseException {
        ServiceEntityBuilder builder = new ServiceEntityBuilder(DocumentFileUtils.INSTANCE, new IdGenerator());

        Bundle bundle = new Bundle();

        Folder parentFolder = new Folder();
        parentFolder.setId("asd");
        parentFolder.setName("my");
        parentFolder.setPath("my");

        bundle.putAllFolders(new HashMap<String, Folder>() {{
            put(parentFolder.getPath(), parentFolder);
        }});

        Policy policy = new Policy();
        policy.setName("policy");
        policy.setPath("/my/policy.xml");
        policy.setPolicyXML("<?xml version=\"1.0\" encoding=\"UTF-8\"?> <wsp:Policy xmlns:L7p=\"http://www.layer7tech.com/ws/policy\" xmlns:wsp=\"http://schemas.xmlsoap.org/ws/2002/12/policy\"> <wsp:All wsp:Usage=\"Required\"/> </wsp:Policy>\n");
        policy.setPolicyDocument(DocumentTools.INSTANCE.parse(policy.getPolicyXML()).getDocumentElement());
        policy.setParentFolder(parentFolder);
        bundle.putAllPolicies(new HashMap<String, Policy>() {{
            put(policy.getPath(), policy);
        }});

        Service service = new Service();
        service.setHttpMethods(Stream.of("POST", "GET").collect(Collectors.toSet()));
        service.setUrl("/my/service/url");
        service.setProperties(new HashMap<String, String>() {{
            put("key1", "value1");
            put("ENV.key.environment", "something");
        }});

        bundle.putAllServices(new HashMap<String, Service>() {{
            put("/my/policy.xml", service);
        }});

        List<Entity> services = builder.build(bundle, EntityBuilder.BundleType.DEPLOYMENT, DocumentTools.INSTANCE.getDocumentBuilder().newDocument());

        assertEquals(1, services.size());

        Entity serviceEntity = services.get(0);

        assertEquals("SERVICE", serviceEntity.getType());
        assertNotNull(serviceEntity.getId());
        Element serviceEntityXml = serviceEntity.getXml();
        assertEquals("l7:Service", serviceEntityXml.getTagName());
        Element serviceDetails = getSingleElement(serviceEntityXml, "l7:ServiceDetail");
        Element serviceName = getSingleElement(serviceDetails, "l7:Name");
        assertEquals(policy.getName(), serviceName.getTextContent());

        Element serviceMappings = getSingleElement(serviceDetails, "l7:ServiceMappings");
        Element serviceHttpMappings = getSingleElement(serviceMappings, "l7:HttpMapping");
        Element serviceUrlPattern = getSingleElement(serviceHttpMappings, "l7:UrlPattern");
        assertEquals(service.getUrl(), serviceUrlPattern.getTextContent());

        Element serviceHttpVerbs = getSingleElement(serviceHttpMappings, "l7:Verbs");
        NodeList verbList = serviceHttpVerbs.getElementsByTagName("l7:Verb");
        assertEquals(2, verbList.getLength());
        assertEquals("POST", verbList.item(0).getTextContent());
        assertEquals("GET", verbList.item(1).getTextContent());

        Element serviceProperties = getSingleElement(serviceDetails, "l7:Properties");
        NodeList propertyList = serviceProperties.getElementsByTagName("l7:Property");
        assertEquals(2, propertyList.getLength());
        Node property1 = propertyList.item(0);
        Node property2 = propertyList.item(1);
        if (!"property.key1".equals(property1.getAttributes().getNamedItem("key").getTextContent())) {
            property2 = propertyList.item(0);
            property1 = propertyList.item(1);
        }
        assertEquals("property.key1", property1.getAttributes().getNamedItem("key").getTextContent());
        assertEquals("property.ENV.key.environment", property2.getAttributes().getNamedItem("key").getTextContent());
        assertEquals("value1", getSingleElement((Element) property1, "l7:StringValue").getTextContent());
        assertEquals("SERVICE_PROPERTY_ENV.key.environment", getSingleElement((Element) property2, "l7:StringValue").getTextContent());

        Element serviceResources = getSingleElement(serviceEntityXml, "l7:Resources");
        Element serviceResourceSet = getSingleElement(serviceResources, "l7:ResourceSet");
        Element serviceResource = getSingleElement(serviceResourceSet, "l7:Resource");
        assertEquals("policy", serviceResource.getAttributes().getNamedItem("type").getTextContent());
        assertNotNull(serviceResource.getTextContent());
    }
}