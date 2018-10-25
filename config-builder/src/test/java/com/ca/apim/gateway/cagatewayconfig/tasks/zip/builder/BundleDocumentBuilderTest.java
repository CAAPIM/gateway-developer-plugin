/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayconfig.tasks.zip.builder;

import com.ca.apim.gateway.cagatewayconfig.util.entity.EntityTypes;
import com.ca.apim.gateway.cagatewayconfig.util.gateway.BuilderUtils;
import com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentTools;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.List;
import java.util.stream.Stream;

import static com.ca.apim.gateway.cagatewayconfig.tasks.zip.builder.BundleDocumentBuilder.GATEWAY_MANAGEMENT;
import static com.ca.apim.gateway.cagatewayconfig.tasks.zip.builder.BundleDocumentBuilder.L7;
import static com.ca.apim.gateway.cagatewayconfig.tasks.zip.builder.EntityBuilderHelper.getEntityWithNameMapping;
import static com.ca.apim.gateway.cagatewayconfig.tasks.zip.builder.EntityBuilderHelper.getEntityWithOnlyMapping;
import static com.ca.apim.gateway.cagatewayconfig.util.TestUtils.assertPropertiesContent;
import static com.ca.apim.gateway.cagatewayconfig.util.TestUtils.createCassandraXml;
import static com.ca.apim.gateway.cagatewayconfig.util.gateway.BuilderUtils.mapPropertiesElements;
import static com.ca.apim.gateway.cagatewayconfig.util.gateway.BundleElementNames.*;
import static com.ca.apim.gateway.cagatewayconfig.util.gateway.MappingActions.NEW_OR_EXISTING;
import static com.ca.apim.gateway.cagatewayconfig.util.gateway.MappingProperties.FAIL_ON_NEW;
import static com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentUtils.*;
import static java.util.stream.Collectors.toList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class BundleDocumentBuilderTest {

    private BundleDocumentBuilder builder = new BundleDocumentBuilder();

    @Test
    void build (){
        Document document = DocumentTools.INSTANCE.getDocumentBuilder().newDocument();
        Entity cassandra = getEntityWithNameMapping(EntityTypes.CASSANDRA_CONNECTION_TYPE, "Test", "Test", createCassandraXml(document, true, true));
        cassandra.setMappingAction(NEW_OR_EXISTING);
        cassandra.setMappingProperty(FAIL_ON_NEW, true);
        Entity jdbc = getEntityWithOnlyMapping(EntityTypes.JDBC_CONNECTION, "Test", "Test");
        List<Entity> entities = Stream.of(cassandra, jdbc).collect(toList());

        final Element element = builder.build(document, entities);
        assertNotNull(element);
        assertEquals(GATEWAY_MANAGEMENT, element.getAttribute(L7));
        assertEquals(BUNDLE, element.getTagName());
        final Element references = getSingleChildElement(element, REFERENCES);
        assertNotNull(references);
        final List<Element> itemList = getChildElements(references, ITEM);
        assertNotNull(itemList);
        assertEquals(1, itemList.size());
        final Element item = itemList.get(0);
        assertEquals(cassandra.getName(), getSingleChildElementTextContent(item, NAME));
        assertEquals(cassandra.getId(), getSingleChildElementTextContent(item, ID));
        assertEquals(cassandra.getType(), getSingleChildElementTextContent(item, TYPE));
        assertNotNull(getSingleChildElement(item, RESOURCE));

        final Element mappings = getSingleChildElement(element, MAPPINGS);
        assertNotNull(mappings);
        final List<Element> mappingsList = getChildElements(mappings, MAPPING);
        assertNotNull(mappingsList);
        assertEquals(2, mappingsList.size());
        final Element cassandraMapping = mappingsList.get(0);
        assertEquals(NEW_OR_EXISTING, cassandraMapping.getAttribute(ATTRIBUTE_ACTION));
        assertEquals(cassandra.getId(), cassandraMapping.getAttribute(ATTRIBUTE_SRCID));
        assertEquals(cassandra.getType(), cassandraMapping.getAttribute(ATTRIBUTE_TYPE));
        assertPropertiesContent(cassandra.getMappingProperties(), mapPropertiesElements(getSingleChildElement(cassandraMapping, PROPERTIES), PROPERTIES));

        final Element jdbcMapping = mappingsList.get(1);
        assertEquals(NEW_OR_EXISTING, jdbcMapping.getAttribute(ATTRIBUTE_ACTION));
        assertEquals(jdbc.getId(), jdbcMapping.getAttribute(ATTRIBUTE_SRCID));
        assertEquals(jdbc.getType(), jdbcMapping.getAttribute(ATTRIBUTE_TYPE));
        assertPropertiesContent(jdbc.getMappingProperties(), mapPropertiesElements(getSingleChildElement(jdbcMapping, PROPERTIES), PROPERTIES));
    }

}