/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayconfig.tasks.zip.builder;

import com.ca.apim.gateway.cagatewayconfig.tasks.zip.beans.Bundle;
import com.ca.apim.gateway.cagatewayconfig.tasks.zip.beans.Encass;
import com.ca.apim.gateway.cagatewayconfig.tasks.zip.beans.EncassParam;
import com.ca.apim.gateway.cagatewayconfig.tasks.zip.beans.Policy;
import com.ca.apim.gateway.cagatewayconfig.tasks.zip.builder.EntityBuilder.BundleType;
import com.ca.apim.gateway.cagatewayconfig.util.IdGenerator;
import com.ca.apim.gateway.cagatewayconfig.util.entity.EntityTypes;
import com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentTools;
import org.junit.jupiter.api.Test;
import org.testcontainers.shaded.com.google.common.collect.ImmutableMap;
import org.w3c.dom.Element;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

import static com.ca.apim.gateway.cagatewayconfig.tasks.zip.builder.EntityBuilder.BundleType.DEPLOYMENT;
import static com.ca.apim.gateway.cagatewayconfig.tasks.zip.builder.EntityBuilder.BundleType.ENVIRONMENT;
import static com.ca.apim.gateway.cagatewayconfig.util.gateway.BundleElementNames.*;
import static com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentUtils.*;
import static java.util.stream.Collectors.toList;
import static org.junit.jupiter.api.Assertions.*;

class EncassEntityBuilderTest {

    private static final IdGenerator ID_GENERATOR = new IdGenerator();
    private static final String TEST_ENCASS = "TestEncass";
    private static final String TEST_GUID = UUID.randomUUID().toString();
    private static final String TEST_POLICY_ID = "PolicyID";

    @Test
    void buildFromEmptyBundle_noEncass() {
        EncassEntityBuilder builder = new EncassEntityBuilder(ID_GENERATOR);
        final List<Entity> entities = builder.build(new Bundle(), DEPLOYMENT, DocumentTools.INSTANCE.getDocumentBuilder().newDocument());

        assertTrue(entities.isEmpty());
    }

    @Test
    void buildWithNoPolicy() {
        final Bundle bundle = new Bundle();
        assertThrows(EntityBuilderException.class, () -> buildBundleWithEncass(bundle, DEPLOYMENT));
    }

    @Test
    void buildDeploymentWithEncass() {
        final Bundle bundle = new Bundle();
        putPolicy(bundle);
        buildBundleWithEncass(bundle, DEPLOYMENT);
    }

    @Test
    void buildEnvironmentWithPBS() {
        final Bundle bundle = new Bundle();
        putPolicy(bundle);
        buildBundleWithEncass(bundle, ENVIRONMENT);
    }

    private static void putPolicy(Bundle bundle) {
        Policy policy = new Policy();
        policy.setName(TEST_ENCASS);
        policy.setId(TEST_POLICY_ID);
        bundle.getPolicies().put(TEST_ENCASS, policy);
    }

    private static void buildBundleWithEncass(Bundle bundle, BundleType deployment) {
        EncassEntityBuilder builder = new EncassEntityBuilder(ID_GENERATOR);
        Encass encass = buildTestEncass();
        bundle.putAllEncasses(ImmutableMap.of(TEST_ENCASS, encass));

        final List<Entity> entities = builder.build(bundle, deployment, DocumentTools.INSTANCE.getDocumentBuilder().newDocument());

        assertFalse(entities.isEmpty());
        assertEquals(1, entities.size());

        Entity entity = entities.get(0);
        assertEquals(TEST_ENCASS, entity.getName());
        assertNotNull(entity.getId());
        assertNotNull(entity.getXml());
        assertEquals(EntityTypes.ENCAPSULATED_ASSERTION_TYPE, entity.getType());

        Element xml = entity.getXml();
        assertEquals(ENCAPSULATED_ASSERTION, xml.getTagName());
        assertNotNull(getSingleChildElement(xml, NAME));
        assertEquals(TEST_ENCASS, getSingleChildElementTextContent(xml, NAME));
        assertNotNull(getSingleChildElement(xml, GUID));
        assertEquals(TEST_GUID, getSingleChildElementTextContent(xml, GUID));
        assertNotNull(getSingleChildElement(xml, POLICY_REFERENCE));
        assertEquals(TEST_POLICY_ID, getSingleChildElement(xml, POLICY_REFERENCE).getAttribute(ATTRIBUTE_ID));
        Element arguments = getSingleChildElement(xml, ENCAPSULATED_ARGUMENTS);
        assertNotNull(arguments);
        List<Element> argumentElements = getChildElements(arguments, ENCAPSULATED_ASSERTION_ARGUMENT);
        assertFalse(argumentElements.isEmpty());
        assertEquals(encass.getArguments().size(), argumentElements.size());
        new ArrayList<>(argumentElements).forEach(e -> {
            assertNotNull(getSingleChildElement(e, ARGUMENT_NAME));
            String argumentName = getSingleChildElementTextContent(e, ARGUMENT_NAME);
            assertTrue(encass.getArguments().stream().map(EncassParam::getName).collect(toList()).contains(argumentName));
            assertNotNull(getSingleChildElement(e, ARGUMENT_TYPE));
            assertEquals(encass.getArguments().stream().filter(p -> p.getName().equals(argumentName)).findFirst().map(EncassParam::getType).orElse(null), getSingleChildElementTextContent(e, ARGUMENT_TYPE));
            argumentElements.remove(e);
        });

        Element results = getSingleChildElement(xml, ENCAPSULATED_RESULTS);
        assertNotNull(results);
        List<Element> resultElements = getChildElements(results, ENCAPSULATED_ASSERTION_RESULT);
        assertFalse(resultElements.isEmpty());
        assertEquals(encass.getResults().size(), resultElements.size());
        new ArrayList<>(resultElements).forEach(e -> {
            assertNotNull(getSingleChildElement(e, RESULT_NAME));
            String resultName = getSingleChildElementTextContent(e, RESULT_NAME);
            assertTrue(encass.getResults().stream().map(EncassParam::getName).collect(toList()).contains(resultName));
            assertNotNull(getSingleChildElement(e, RESULT_TYPE));
            assertEquals(encass.getResults().stream().filter(p -> p.getName().equals(resultName)).findFirst().map(EncassParam::getType).orElse(null), getSingleChildElementTextContent(e, RESULT_TYPE));
            resultElements.remove(e);
        });
        assertTrue(argumentElements.isEmpty());
    }

    private static Encass buildTestEncass() {
        Encass encass = new Encass();
        encass.setGuid(TEST_GUID);
        encass.setArguments(new HashSet<>());
        EncassParam param1 = new EncassParam();
        param1.setName("Param1");
        param1.setType("string");
        EncassParam param2 = new EncassParam();
        param2.setName("Param2");
        param2.setType("message");
        encass.getArguments().add(param1);
        encass.getArguments().add(param2);
        encass.setResults(new HashSet<>());
        EncassParam result1 = new EncassParam();
        result1.setName("Result1");
        result1.setType("string");
        EncassParam result2 = new EncassParam();
        result2.setName("Result2");
        result2.setType("message");
        encass.getResults().add(result1);
        encass.getResults().add(result2);
        return encass;
    }

}