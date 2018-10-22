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
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.ca.apim.gateway.cagatewayconfig.util.entity.EntityTypes.SERVICE_TYPE;
import static com.ca.apim.gateway.cagatewayconfig.util.gateway.BundleElementNames.*;
import static com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentUtils.getSingleElement;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ServiceEntityBuilderTest {

    @Test
    void buildNoServices() {
        ServiceEntityBuilder builder = new ServiceEntityBuilder(DocumentFileUtils.INSTANCE, new IdGenerator());

        Bundle bundle = new Bundle();

        List<Entity> serviceEntities = builder.build(bundle, EntityBuilder.BundleType.DEPLOYMENT, DocumentTools.INSTANCE.getDocumentBuilder().newDocument());

        assertEquals(0, serviceEntities.size());
    }

    @Test
    void buildServicesWithoutPolicy() {
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
    void buildOneServices() throws DocumentParseException {
        ServiceEntityBuilder builder = new ServiceEntityBuilder(DocumentFileUtils.INSTANCE, new IdGenerator());

        Bundle bundle = new Bundle();

        Folder serviceParentFolder = setUpFolderAndPolicy(bundle);

        Service service = new Service();
        service.setHttpMethods(Stream.of("POST", "GET").collect(Collectors.toSet()));
        service.setUrl("/my/service/url");
        service.setPolicy("/my/policy.xml");
        service.setParentFolder(serviceParentFolder);
        service.setProperties(new HashMap<String, String>() {{
            put("key1", "value1");
            put("ENV.key.environment", "something");
        }});

        bundle.putAllServices(new HashMap<String, Service>() {{
            put("/v1/service1", service);
        }});

        List<Entity> services = builder.build(bundle, EntityBuilder.BundleType.DEPLOYMENT, DocumentTools.INSTANCE.getDocumentBuilder().newDocument());

        assertEquals(1, services.size());

        Entity serviceEntity = services.get(0);

        verifyService(service, serviceEntity, 1);
    }

    @Test
    void buildTwoServicesSamePolicy() throws DocumentParseException {
        ServiceEntityBuilder builder = new ServiceEntityBuilder(DocumentFileUtils.INSTANCE, new IdGenerator());

        Bundle bundle = new Bundle();
        Folder serviceParentFolder = setUpFolderAndPolicy(bundle);

        Service service1 = new Service();
        service1.setHttpMethods(Stream.of("POST", "GET").collect(Collectors.toSet()));
        service1.setUrl("/my/service/url");
        service1.setPolicy("/my/policy.xml");
        service1.setParentFolder(serviceParentFolder);
        service1.setProperties(new HashMap<String, String>() {{
            put("key1", "value1");
            put("ENV.key.environment", "something");
        }});

        Service service2 = new Service();
        service2.setHttpMethods(Stream.of("POST", "GET").collect(Collectors.toSet()));
        service2.setUrl("/my/url");
        service2.setPolicy("/my/policy.xml");
        service2.setParentFolder(bundle.getFolders().get("my"));
        service2.setProperties(new HashMap<String, String>() {{
            put("key2", "value2");
            put("ENV.key.environment", "something");
        }});

        bundle.putAllServices(new HashMap<String, Service>() {{
            put("my/v1/service1", service1);
            put("my/service2", service2);
        }});

        List<Entity> services = builder.build(bundle, EntityBuilder.BundleType.DEPLOYMENT, DocumentTools.INSTANCE.getDocumentBuilder().newDocument());

        assertEquals(2, services.size());
        if (services.get(0).getName().equals("service1")) {
            verifyService(service1, services.get(0), 1);
            verifyService(service2, services.get(1), 2);
        } else {
            verifyService(service2, services.get(0), 2);
            verifyService(service1, services.get(1), 1);
        }
    }

    @NotNull
    private Folder setUpFolderAndPolicy(Bundle bundle) throws DocumentParseException {
        Folder parentFolder = new Folder();
        parentFolder.setId("asd");
        parentFolder.setName("my");
        parentFolder.setPath("my");

        Folder serviceParentFolder = new Folder();
        serviceParentFolder.setId("test");
        serviceParentFolder.setName("v1");
        serviceParentFolder.setPath("my/v1");

        bundle.putAllFolders(new HashMap<String, Folder>() {{
            put(parentFolder.getPath(), parentFolder);
            put(serviceParentFolder.getPath(), serviceParentFolder);
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
        return serviceParentFolder;
    }

    private void verifyService(Service service, Entity serviceEntity, int serviceNumber) throws DocumentParseException {
        assertEquals(SERVICE_TYPE, serviceEntity.getType());
        assertNotNull(serviceEntity.getId());
        Element serviceEntityXml = serviceEntity.getXml();
        assertEquals(SERVICE, serviceEntityXml.getTagName());
        Element serviceDetails = getSingleElement(serviceEntityXml, SERVICE_DETAIL);
        Element serviceName = getSingleElement(serviceDetails, NAME);
        assertEquals("service" + serviceNumber, serviceName.getTextContent());

        Element serviceMappings = getSingleElement(serviceDetails, SERVICE_MAPPINGS);
        Element serviceHttpMappings = getSingleElement(serviceMappings, HTTP_MAPPING);
        Element serviceUrlPattern = getSingleElement(serviceHttpMappings, URL_PATTERN);
        assertEquals(service.getUrl(), serviceUrlPattern.getTextContent());

        Element serviceHttpVerbs = getSingleElement(serviceHttpMappings, VERBS);
        NodeList verbList = serviceHttpVerbs.getElementsByTagName(VERB);
        assertEquals(2, verbList.getLength());
        assertEquals("POST", verbList.item(0).getTextContent());
        assertEquals("GET", verbList.item(1).getTextContent());

        Element serviceProperties = getSingleElement(serviceDetails, PROPERTIES);
        NodeList propertyList = serviceProperties.getElementsByTagName(PROPERTY);
        assertEquals(2, propertyList.getLength());
        Node property1 = propertyList.item(0);
        Node property2 = propertyList.item(1);
        if (!("property.key" + serviceNumber).equals(property1.getAttributes().getNamedItem(ATTRIBUTE_KEY).getTextContent())) {
            property2 = propertyList.item(0);
            property1 = propertyList.item(1);
        }
        assertEquals("property.key" + serviceNumber, property1.getAttributes().getNamedItem(ATTRIBUTE_KEY).getTextContent());
        assertEquals("property.ENV.key.environment", property2.getAttributes().getNamedItem(ATTRIBUTE_KEY).getTextContent());
        assertEquals("value" + serviceNumber, getSingleElement((Element) property1, STRING_VALUE).getTextContent());
        assertEquals("SERVICE_PROPERTY_ENV.key.environment", getSingleElement((Element) property2, STRING_VALUE).getTextContent());

        Element serviceResources = getSingleElement(serviceEntityXml, RESOURCES);
        Element serviceResourceSet = getSingleElement(serviceResources, RESOURCE_SET);
        Element serviceResource = getSingleElement(serviceResourceSet, RESOURCE);
        assertEquals("policy", serviceResource.getAttributes().getNamedItem(ATTRIBUTE_TYPE).getTextContent());
        assertNotNull(serviceResource.getTextContent());
    }
}