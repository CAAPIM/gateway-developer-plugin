/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayconfig.util.gateway;

import org.junit.Assert;
import org.junit.Test;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.util.HashMap;

public class BuilderUtilsTest {

    @Test
    public void buildPropertiesElement() throws ParserConfigurationException {
        Element properties = BuilderUtils.buildPropertiesElement(new HashMap<String, Object>() {{
            put("key1", "value1");
        }}, DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument());

        NodeList propertyNodes = properties.getElementsByTagName("l7:Property");
        Assert.assertEquals(1, propertyNodes.getLength());
        for (int i = 0; i < propertyNodes.getLength(); i++) {
            Node property = propertyNodes.item(i);
            Node key = property.getAttributes().getNamedItem("key");
            if ("key1".equals(key.getTextContent())) {
                if (property instanceof Element) {
                    NodeList value = ((Element) property).getElementsByTagName("l7:StringValue");
                    Assert.assertEquals("value1", value.item(0).getTextContent());
                    return;
                }
            }
        }
        Assert.fail("Did not find property for service property key1");
    }

    @Test
    public void buildPropertiesElementEmptyProperties() throws ParserConfigurationException {
        Element properties = BuilderUtils.buildPropertiesElement(new HashMap<>(), DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument());

        NodeList propertyNodes = properties.getElementsByTagName("l7:Property");
        Assert.assertEquals(0, propertyNodes.getLength());
    }
}