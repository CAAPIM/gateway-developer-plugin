/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayconfig.bundle.builder;

import com.ca.apim.gateway.cagatewayconfig.ProjectInfo;
import com.ca.apim.gateway.cagatewayconfig.beans.*;
import com.ca.apim.gateway.cagatewayconfig.bundle.builder.EntityBuilder.BundleType;
import com.ca.apim.gateway.cagatewayconfig.util.IdGenerator;
import com.ca.apim.gateway.cagatewayconfig.util.entity.AnnotationType;
import com.ca.apim.gateway.cagatewayconfig.util.entity.EntityTypes;
import com.ca.apim.gateway.cagatewayconfig.util.properties.PropertyConstants;
import com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentTools;
import org.junit.jupiter.api.Test;
import org.testcontainers.shaded.com.google.common.collect.ImmutableMap;
import org.w3c.dom.Element;

import java.util.*;

import static com.ca.apim.gateway.cagatewayconfig.util.gateway.BuilderUtils.mapPropertiesElements;
import static com.ca.apim.gateway.cagatewayconfig.util.gateway.BundleElementNames.*;
import static com.ca.apim.gateway.cagatewayconfig.util.properties.PropertyConstants.*;
import static com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentUtils.*;
import static java.util.stream.Collectors.toList;
import static org.junit.jupiter.api.Assertions.*;

class EncassEntityBuilderTest {

    private static final IdGenerator ID_GENERATOR = new IdGenerator();
    private static final String TEST_ENCASS = "TestEncass";
    private static final String TEST_POLICY_PATH= "test/policy.xml";
    private static final String TEST_GUID = UUID.randomUUID().toString();
    private static final String TEST_POLICY_ID = "PolicyID";
    private static final String TEST_GOID = ID_GENERATOR.generate();
    private static final ProjectInfo projectInfo = new ProjectInfo("my-bundle", "my-bundle-group", "1.0", "qa");

    @Test
    void buildFromEmptyBundle_noEncass() {
        EncassEntityBuilder builder = new EncassEntityBuilder(ID_GENERATOR);
        final List<Entity> entities = builder.build(new Bundle(), BundleType.DEPLOYMENT, DocumentTools.INSTANCE.getDocumentBuilder().newDocument());

        assertTrue(entities.isEmpty());
    }

    @Test
    void buildWithNoPolicy() {
        final Bundle bundle = new Bundle();
        assertThrows(EntityBuilderException.class, () -> buildBundleWithEncass(bundle, BundleType.DEPLOYMENT, TEST_POLICY_PATH, TEST_ENCASS));
    }

    @Test
    void buildDeploymentWithEncass() {
        final Bundle bundle = new Bundle();
        putPolicy(bundle, TEST_POLICY_PATH, TEST_POLICY_ID, TEST_POLICY_PATH);
        buildBundleWithEncass(bundle, BundleType.DEPLOYMENT, TEST_POLICY_PATH, TEST_ENCASS);
    }

    @Test
    void buildEncassDeploymentWithWrongGuidAndId() {
        final Bundle bundle = new Bundle();
        EncassEntityBuilder builder = new EncassEntityBuilder(ID_GENERATOR);
        Encass encass = buildTestEncass(TEST_GUID, TEST_GOID, TEST_POLICY_PATH);
        encass.setName(TEST_ENCASS);
        Set<Annotation> annotations = new HashSet<>();
        Annotation annotation = new Annotation(AnnotationType.BUNDLE_HINTS);
        annotation.setGuid("");
        annotation.setId("");
        annotations.add(annotation);
        annotations.add(new Annotation(AnnotationType.SHARED));
        encass.setAnnotations(annotations);
        encass.setParentEntityShared(encass.getAnnotations().contains(new Annotation(AnnotationType.SHARED)));
        bundle.putAllEncasses(ImmutableMap.of(TEST_ENCASS, encass));

        Policy policy = new Policy();
        policy.setName(TEST_POLICY_PATH);
        policy.setId(TEST_POLICY_ID);
        bundle.getPolicies().put(TEST_POLICY_PATH, policy);
        AnnotatedEntity annotatedEntity = encass.getAnnotatedEntity();
        AnnotatedBundle annotatedBundle = new AnnotatedBundle(bundle, annotatedEntity, projectInfo);
        annotatedBundle.putAllEncasses(ImmutableMap.of(TEST_ENCASS, encass));
        annotatedBundle.getPolicies().put(TEST_POLICY_PATH, policy);
        List<Entity> entities = builder.build(annotatedBundle, BundleType.DEPLOYMENT, DocumentTools.INSTANCE.getDocumentBuilder().newDocument());

        assertFalse(entities.isEmpty());
        assertEquals(1, entities.size());

        Entity entity = entities.get(0);
        String name = annotatedBundle.applyUniqueName(TEST_ENCASS, BundleType.DEPLOYMENT, true);
        assertEquals(name, entity.getName());
        assertNotNull(entity.getId(), TEST_GOID);
        assertEquals(entity.getGuid(), TEST_GUID);


        annotations = new HashSet<>();
        annotation = new Annotation(AnnotationType.BUNDLE_HINTS);
        annotation.setGuid("wrongGuid");
        annotation.setId("wrongId");
        annotations.add(annotation);
        annotations.add(new Annotation(AnnotationType.SHARED));
        encass.setAnnotations(annotations);
        encass.setParentEntityShared(encass.getAnnotations().contains(new Annotation(AnnotationType.SHARED)));
        encass.setAnnotatedEntity(null);
        annotatedEntity = encass.getAnnotatedEntity();
        annotatedBundle = new AnnotatedBundle(bundle, annotatedEntity, projectInfo);
        annotatedBundle.putAllEncasses(ImmutableMap.of(TEST_ENCASS, encass));
        annotatedBundle.getPolicies().put(TEST_POLICY_PATH, policy);
        entities = builder.build(annotatedBundle, BundleType.DEPLOYMENT, DocumentTools.INSTANCE.getDocumentBuilder().newDocument());

        entity = entities.get(0);
        name = annotatedBundle.applyUniqueName(TEST_ENCASS, BundleType.DEPLOYMENT, true);
        assertEquals(name, entity.getName());
        assertNotNull(entity.getId(), TEST_GOID);
        assertEquals(entity.getGuid(), TEST_GUID);
    }

    @Test
    void buildDeploymentWithEncassEncodedPolicyName() {
        final Bundle bundle = new Bundle();
        putPolicy(bundle, "example-_¯-¯_-slashed", TEST_POLICY_ID, "example/folder-_¯-¯_-slashed/example-_¯-¯_-slashed.xml");
        buildBundleWithEncass(bundle, BundleType.DEPLOYMENT, "example/folder-_¯-¯_-slashed/example-_¯-¯_-slashed.xml", "example-_¯-¯_-slashed");
    }

    @Test
    void buildEnvironmentWithEncass() {
        final Bundle bundle = new Bundle();
        putPolicy(bundle, TEST_POLICY_PATH, TEST_POLICY_ID, TEST_POLICY_PATH);

        EncassEntityBuilder builder = new EncassEntityBuilder(ID_GENERATOR);
        Encass encass = buildTestEncass(TEST_GUID, "testGoid", TEST_POLICY_PATH);
        bundle.putAllEncasses(ImmutableMap.of(TEST_ENCASS, encass));

        final List<Entity> entities = builder.build(bundle, BundleType.ENVIRONMENT, DocumentTools.INSTANCE.getDocumentBuilder().newDocument());
        assertTrue(entities.isEmpty());
    }

    private static void putPolicy(Bundle bundle, String name, String id, String path) {
        Policy policy = new Policy();
        policy.setName(name);
        policy.setId(id);
        bundle.getPolicies().put(path, policy);
    }

    private static void buildBundleWithEncass(Bundle bundle, BundleType deployment, String policyPath, String encassName) {
        EncassEntityBuilder builder = new EncassEntityBuilder(ID_GENERATOR);
        Encass encass = buildTestEncass(EncassEntityBuilderTest.TEST_GUID, "testGoid", policyPath);
        encass.setName(encassName);
        bundle.putAllEncasses(ImmutableMap.of(encassName, encass));

        final List<Entity> entities = builder.build(bundle, deployment, DocumentTools.INSTANCE.getDocumentBuilder().newDocument());

        assertFalse(entities.isEmpty());
        assertEquals(1, entities.size());

        Entity entity = entities.get(0);
        assertEquals(encassName, entity.getName());
        assertNotNull(entity.getId());
        assertNotNull(entity.getXml());
        assertEquals(EntityTypes.ENCAPSULATED_ASSERTION_TYPE, entity.getType());

        Element xml = entity.getXml();
        assertEquals(ENCAPSULATED_ASSERTION, xml.getTagName());
        assertNotNull(getSingleChildElement(xml, NAME));
        assertEquals(encassName, getSingleChildElementTextContent(xml, NAME));
        assertNotNull(getSingleChildElement(xml, GUID));
        assertEquals(EncassEntityBuilderTest.TEST_GUID, getSingleChildElementTextContent(xml, GUID));
        assertNotNull(getSingleChildElement(xml, POLICY_REFERENCE));
        assertEquals(EncassEntityBuilderTest.TEST_POLICY_ID, getSingleChildElement(xml, POLICY_REFERENCE).getAttribute(ATTRIBUTE_ID));
        assertNotNull(getSingleChildElement(xml, PROPERTIES));
        Map<String, Object> props = mapPropertiesElements(getSingleChildElement(xml, PROPERTIES), PROPERTIES);
        assertEquals(5, props.size());
        assertEquals(DEFAULT_PALETTE_FOLDER_LOCATION, props.get(PALETTE_FOLDER));
        assertEquals("someImage", props.get(PALETTE_ICON_RESOURCE_NAME));
        assertEquals("false", props.get(ALLOW_TRACING));
        assertEquals("someDescription", props.get(PropertyConstants.DESCRIPTION));
        assertEquals("false", props.get(PASS_METRICS_TO_PARENT));

        Element arguments = getSingleChildElement(xml, ENCAPSULATED_ARGUMENTS);
        assertNotNull(arguments);
        List<Element> argumentElements = getChildElements(arguments, ENCAPSULATED_ASSERTION_ARGUMENT);
        assertFalse(argumentElements.isEmpty());
        assertEquals(encass.getArguments().size(), argumentElements.size());
        new ArrayList<>(argumentElements).forEach(e -> {
            assertNotNull(getSingleChildElement(e, ARGUMENT_NAME));
            String argumentName = getSingleChildElementTextContent(e, ARGUMENT_NAME);
            assertTrue(encass.getArguments().stream().map(EncassArgument::getName).collect(toList()).contains(argumentName));
            assertNotNull(getSingleChildElement(e, ARGUMENT_TYPE));
            assertEquals(encass.getArguments().stream().filter(p -> p.getName().equals(argumentName)).findFirst().map(EncassArgument::getType).orElse(null), getSingleChildElementTextContent(e, ARGUMENT_TYPE));
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
            assertTrue(encass.getResults().stream().map(EncassResult::getName).collect(toList()).contains(resultName));
            assertNotNull(getSingleChildElement(e, RESULT_TYPE));
            assertEquals(encass.getResults().stream().filter(p -> p.getName().equals(resultName)).findFirst().map(EncassResult::getType).orElse(null), getSingleChildElementTextContent(e, RESULT_TYPE));
            resultElements.remove(e);
        });
        assertTrue(argumentElements.isEmpty());
    }

    private static Encass buildTestEncass(String encassGuid, String encassId, String policyPath) {
        Encass encass = new Encass();
        encass.setPolicy(policyPath);
        encass.setGuid(encassGuid);
        encass.setId(encassId);
        encass.setArguments(new HashSet<>());
        EncassArgument param1 = new EncassArgument();
        param1.setName("Param1");
        param1.setType("string");
        EncassArgument param2 = new EncassArgument();
        param2.setName("Param2");
        param2.setType("message");
        encass.getArguments().add(param1);
        encass.getArguments().add(param2);
        encass.setResults(new HashSet<>());
        EncassResult result1 = new EncassResult();
        result1.setName("Result1");
        result1.setType("string");
        EncassResult result2 = new EncassResult();
        result2.setName("Result2");
        result2.setType("message");
        encass.getResults().add(result1);
        encass.getResults().add(result2);
        encass.setProperties(ImmutableMap.of(
                PALETTE_FOLDER, DEFAULT_PALETTE_FOLDER_LOCATION,
                PALETTE_ICON_RESOURCE_NAME, "someImage",
                ALLOW_TRACING, "false",
                PropertyConstants.DESCRIPTION, "someDescription",
                PASS_METRICS_TO_PARENT, "false"));
        return encass;
    }

}