/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayconfig.util.gateway;

import com.ca.apim.gateway.cagatewayconfig.bundle.loader.BundleLoadException;
import com.ca.apim.gateway.cagatewayconfig.util.TestUtils;
import com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentUtils;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static com.ca.apim.gateway.cagatewayconfig.util.gateway.BundleElementNames.*;
import static java.util.Collections.emptyMap;
import static org.junit.jupiter.api.Assertions.*;

class BuilderUtilsTest {

    @Test
    void buildPropertiesElement() throws ParserConfigurationException {
        Element properties = BuilderUtils.buildPropertiesElement(new HashMap<String, Object>() {{
            put("keyString", "value1");
            put("keyInteger", 1);
            put("keyLong", 1L);
            put("keyBoolean", true);
        }}, DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument());

        NodeList propertyNodes = properties.getElementsByTagName("l7:Property");
        for (Node property : DocumentUtils.nodeList(propertyNodes)) {
            Node key = property.getAttributes().getNamedItem("key");
            switch (key.getTextContent()) {
                case "keyString":
                    NodeList valueString = ((Element) property).getElementsByTagName(STRING_VALUE);
                    assertEquals("value1", valueString.item(0).getTextContent());
                    break;
                case "keyInteger":
                    NodeList valueInt = ((Element) property).getElementsByTagName(INTEGER_VALUE);
                    assertEquals(1, Integer.valueOf(valueInt.item(0).getTextContent()).intValue());
                    break;
                case "keyLong":
                    NodeList valueLong = ((Element) property).getElementsByTagName(LONG_VALUE);
                    assertEquals(1L, Long.valueOf(valueLong.item(0).getTextContent()).longValue());
                    break;
                case "keyBoolean":
                    NodeList valueBoolean = ((Element) property).getElementsByTagName(BOOLEAN_VALUE);
                    assertEquals(true, Boolean.valueOf(valueBoolean.item(0).getTextContent()));
                    break;
                default:
                    fail("Unexpected key " + key.getTextContent());
            }
        }
    }

    @Test
    void buildPropertiesElementEmptyProperties() throws ParserConfigurationException {
        Element properties = BuilderUtils.buildPropertiesElement(new HashMap<>(), DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument());

        NodeList propertyNodes = properties.getElementsByTagName("l7:Property");
        assertEquals(0, propertyNodes.getLength());
    }

    @Test
    void mapInvalidPropertiesNode() {
        assertThrows(BundleLoadException.class, () -> BuilderUtils.mapPropertiesElements(Mockito.mock(Element.class), PROPERTIES));
    }


    @Test
    void mapProperties() throws ParserConfigurationException {
        Map<String, Object> map = new HashMap<String, Object>() {{
            put("keyString", "value1");
            put("keyInteger", 1);
            put("keyLong", 1L);
            put("keyBoolean", true);
            put("keyDate", new Date());
            put("keyNull", null);
        }};

        Element properties = BuilderUtils.buildPropertiesElement(map, DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument());
        map.remove("keyNull");

        final Map<String, Object> props = BuilderUtils.mapPropertiesElements(properties, PROPERTIES);
        TestUtils.assertPropertiesContent(map, props);
    }


    @Test
    void mapNoProperties() {
        final Map<String, Object> props = BuilderUtils.mapPropertiesElements(null, PROPERTIES);
        assertEquals(props, emptyMap());
    }
}