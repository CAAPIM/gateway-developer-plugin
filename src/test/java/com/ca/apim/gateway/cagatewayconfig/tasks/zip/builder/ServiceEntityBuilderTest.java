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
import org.junit.Assert;
import org.junit.Test;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ServiceEntityBuilderTest {

    @Test
    public void buildNoServices() {
        ServiceEntityBuilder builder = new ServiceEntityBuilder(DocumentFileUtils.INSTANCE, DocumentTools.INSTANCE.getDocumentBuilder().newDocument(), new IdGenerator());

        Bundle bundle = new Bundle();

        List<Entity> serviceEntities = builder.build(bundle);

        Assert.assertEquals(0, serviceEntities.size());
    }

    @Test(expected = EntityBuilderException.class)
    public void buildServicesWithoutPolicy() {
        ServiceEntityBuilder builder = new ServiceEntityBuilder(DocumentFileUtils.INSTANCE, DocumentTools.INSTANCE.getDocumentBuilder().newDocument(), new IdGenerator());

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
        builder.build(bundle);
    }

    @Test
    public void buildOneServices() throws DocumentParseException {
        ServiceEntityBuilder builder = new ServiceEntityBuilder(DocumentFileUtils.INSTANCE, DocumentTools.INSTANCE.getDocumentBuilder().newDocument(), new IdGenerator());

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
            put("key2", "value2");
        }});

        bundle.putAllServices(new HashMap<String, Service>() {{
            put("/my/policy.xml", service);
        }});

        List<Entity> services = builder.build(bundle);

        Assert.assertEquals(1, services.size());

        Entity serviceEntity = services.get(0);

        Assert.assertEquals("SERVICE", serviceEntity.getType());
        Assert.assertNotNull(serviceEntity.getId());
        Element serviceEntityXml = serviceEntity.getXml();
        Assert.assertEquals("l7:Service", serviceEntityXml.getTagName());
        Element serviceDetails = DocumentTools.INSTANCE.getSingleElement(serviceEntityXml, "l7:ServiceDetail");
        Element serviceName = DocumentTools.INSTANCE.getSingleElement(serviceDetails, "l7:Name");
        Assert.assertEquals(policy.getName(), serviceName.getTextContent());

        Element serviceMappings = DocumentTools.INSTANCE.getSingleElement(serviceDetails, "l7:ServiceMappings");
        Element serviceHttpMappings = DocumentTools.INSTANCE.getSingleElement(serviceMappings, "l7:HttpMapping");
        Element serviceUrlPattern = DocumentTools.INSTANCE.getSingleElement(serviceHttpMappings, "l7:UrlPattern");
        Assert.assertEquals(service.getUrl(), serviceUrlPattern.getTextContent());

        Element serviceHttpVerbs = DocumentTools.INSTANCE.getSingleElement(serviceHttpMappings, "l7:Verbs");
        NodeList verbList = serviceHttpVerbs.getElementsByTagName("l7:Verb");
        Assert.assertEquals(2, verbList.getLength());
        Assert.assertEquals("POST", verbList.item(0).getTextContent());
        Assert.assertEquals("GET", verbList.item(1).getTextContent());

        Element serviceProperties = DocumentTools.INSTANCE.getSingleElement(serviceDetails, "l7:Properties");
        NodeList propertyList = serviceProperties.getElementsByTagName("l7:Property");
        Assert.assertEquals(2, propertyList.getLength());
        Node property1 = propertyList.item(0);
        Node property2 = propertyList.item(1);
        if (!"property.key1".equals(property1.getAttributes().getNamedItem("key").getTextContent())) {
            property2 = propertyList.item(0);
            property1 = propertyList.item(1);
        }
        Assert.assertEquals("property.key1", property1.getAttributes().getNamedItem("key").getTextContent());
        Assert.assertEquals("property.key2", property2.getAttributes().getNamedItem("key").getTextContent());
        Assert.assertEquals("value1", DocumentTools.INSTANCE.getSingleElement((Element) property1, "l7:StringValue").getTextContent());
        Assert.assertEquals("value2", DocumentTools.INSTANCE.getSingleElement((Element) property2, "l7:StringValue").getTextContent());

        Element serviceResources = DocumentTools.INSTANCE.getSingleElement(serviceEntityXml, "l7:Resources");
        Element serviceResourceSet = DocumentTools.INSTANCE.getSingleElement(serviceResources, "l7:ResourceSet");
        Element serviceResource = DocumentTools.INSTANCE.getSingleElement(serviceResourceSet, "l7:Resource");
        Assert.assertEquals("policy", serviceResource.getAttributes().getNamedItem("type").getTextContent());
        Assert.assertNotNull(serviceResource.getTextContent());
    }
}