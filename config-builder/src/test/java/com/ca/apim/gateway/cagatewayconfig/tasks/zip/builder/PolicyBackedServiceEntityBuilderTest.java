/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayconfig.tasks.zip.builder;

import com.ca.apim.gateway.cagatewayconfig.tasks.zip.beans.Bundle;
import com.ca.apim.gateway.cagatewayconfig.tasks.zip.beans.Policy;
import com.ca.apim.gateway.cagatewayconfig.tasks.zip.beans.PolicyBackedService;
import com.ca.apim.gateway.cagatewayconfig.tasks.zip.beans.PolicyBackedServiceOperation;
import com.ca.apim.gateway.cagatewayconfig.tasks.zip.builder.EntityBuilder.BundleType;
import com.ca.apim.gateway.cagatewayconfig.util.IdGenerator;
import com.ca.apim.gateway.cagatewayconfig.util.entity.EntityTypes;
import com.ca.apim.gateway.cagatewayconfig.util.gateway.BundleElementNames;
import com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentTools;
import org.junit.jupiter.api.Test;
import org.testcontainers.shaded.com.google.common.collect.ImmutableMap;
import org.w3c.dom.Element;

import java.util.HashSet;
import java.util.List;

import static com.ca.apim.gateway.cagatewayconfig.tasks.zip.builder.EntityBuilder.BundleType.DEPLOYMENT;
import static com.ca.apim.gateway.cagatewayconfig.tasks.zip.builder.EntityBuilder.BundleType.ENVIRONMENT;
import static com.ca.apim.gateway.cagatewayconfig.util.gateway.BundleElementNames.*;
import static com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentUtils.getSingleChildElement;
import static com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentUtils.getSingleChildElementTextContent;
import static org.junit.jupiter.api.Assertions.*;

class PolicyBackedServiceEntityBuilderTest {

    private static final String TEST_PBS = "TestPBS";
    private static final IdGenerator ID_GENERATOR = new IdGenerator();
    private static final String TEST_INTERFACE_NAME = "com.l7tech.objectmodel.polback.BackgroundTask";
    private static final String TEST_OPERATION_NAME = "run";
    private static final String TEST_POLICY = "testPBS.xml";
    private static final String TEST_POLICY_ID = "PolicyID";

    @Test
    void buildFromEmptyBundle_noPBS() {
        PolicyBackedServiceEntityBuilder builder = new PolicyBackedServiceEntityBuilder(ID_GENERATOR);
        final List<Entity> entities = builder.build(new Bundle(), DEPLOYMENT, DocumentTools.INSTANCE.getDocumentBuilder().newDocument());

        assertTrue(entities.isEmpty());
    }

    @Test
    void buildWithNoPolicy() {
        final Bundle bundle = new Bundle();
        assertThrows(EntityBuilderException.class, () -> buildBundleWithPBS(bundle, DEPLOYMENT));
    }

    @Test
    void buildDeploymentWithPBS() {
        final Bundle bundle = new Bundle();
        putPolicy(bundle);
        buildBundleWithPBS(bundle, DEPLOYMENT);
    }

    @Test
    void buildEnvironmentWithPBS() {
        final Bundle bundle = new Bundle();
        putPolicy(bundle);
        buildBundleWithPBS(bundle, ENVIRONMENT);
    }

    private static void putPolicy(Bundle bundle) {
        Policy policy = new Policy();
        policy.setName("testPBS");
        policy.setId(TEST_POLICY_ID);
        bundle.getPolicies().put(TEST_POLICY, policy);
    }

    private static void buildBundleWithPBS(Bundle bundle, BundleType deployment) {
        PolicyBackedServiceEntityBuilder builder = new PolicyBackedServiceEntityBuilder(ID_GENERATOR);
        bundle.putAllPolicyBackedServices(ImmutableMap.of(TEST_PBS, buildTestPBS()));

        final List<Entity> entities = builder.build(bundle, deployment, DocumentTools.INSTANCE.getDocumentBuilder().newDocument());

        assertFalse(entities.isEmpty());
        assertEquals(1, entities.size());

        Entity entity = entities.get(0);
        assertEquals(TEST_PBS, entity.getName());
        assertNotNull(entity.getId());
        assertNotNull(entity.getXml());
        assertEquals(EntityTypes.POLICY_BACKED_SERVICE_TYPE, entity.getType());

        Element xml = entity.getXml();
        assertEquals(POLICY_BACKED_SERVICE, xml.getTagName());
        assertNotNull(getSingleChildElement(xml, NAME));
        assertEquals(TEST_PBS, getSingleChildElementTextContent(xml, NAME));
        assertNotNull(getSingleChildElement(xml, BundleElementNames.INTERFACE_NAME));
        assertEquals(TEST_INTERFACE_NAME, getSingleChildElementTextContent(xml, BundleElementNames.INTERFACE_NAME));
        Element operations = getSingleChildElement(xml, POLICY_BACKED_SERVICE_OPERATIONS);
        assertNotNull(operations);
        Element operation = getSingleChildElement(operations, POLICY_BACKED_SERVICE_OPERATION);
        assertNotNull(operation);
        assertNotNull(getSingleChildElement(operation, BundleElementNames.POLICY_ID));
        assertEquals(TEST_POLICY_ID, getSingleChildElementTextContent(operation, BundleElementNames.POLICY_ID));
        assertNotNull(getSingleChildElement(operation, OPERATION_NAME));
        assertEquals(TEST_OPERATION_NAME, getSingleChildElementTextContent(operation, OPERATION_NAME));
    }

    private static PolicyBackedService buildTestPBS() {
        PolicyBackedService pbs = new PolicyBackedService();
        pbs.setInterfaceName(TEST_INTERFACE_NAME);
        PolicyBackedServiceOperation operation = new PolicyBackedServiceOperation();
        operation.setOperationName(TEST_OPERATION_NAME);
        operation.setPolicy(TEST_POLICY);
        pbs.setOperations(new HashSet<>());
        pbs.getOperations().add(operation);
        return pbs;
    }

}