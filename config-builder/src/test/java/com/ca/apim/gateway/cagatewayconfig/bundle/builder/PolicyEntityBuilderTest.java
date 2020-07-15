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
import com.ca.apim.gateway.cagatewayconfig.util.gateway.MappingProperties;
import com.ca.apim.gateway.cagatewayconfig.util.properties.PropertyConstants;
import com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentTools;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Sets;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.reflections.Reflections;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.*;

import static com.ca.apim.gateway.cagatewayconfig.util.gateway.BuilderUtils.mapPropertiesElements;
import static com.ca.apim.gateway.cagatewayconfig.util.gateway.BundleElementNames.*;
import static com.ca.apim.gateway.cagatewayconfig.util.gateway.MappingProperties.MAP_BY;
import static com.ca.apim.gateway.cagatewayconfig.util.gateway.MappingProperties.MAP_TO;
import static com.ca.apim.gateway.cagatewayconfig.util.policy.PolicyXMLElements.*;
import static com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentUtils.*;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;
import static org.junit.jupiter.api.Assertions.*;

class PolicyEntityBuilderTest {

    private Policy policy;
    private Bundle bundle;
    private Document document;
    private final String TEST_ENCASS = "EncassName";
    private PolicyXMLBuilder policyXMLBuilder;

    @BeforeEach
    void beforeEach() {
        policy = new Policy();
        policy.setPath("test/policy/path.xml");
        bundle = new Bundle();
        bundle.setDependencies(new HashSet<>());
        document = DocumentTools.INSTANCE.getDocumentBuilder().newDocument();
        Set<PolicyAssertionBuilder> policyAssertionBuilders = new HashSet<>();
        Reflections reflections = new Reflections();
        reflections.getSubTypesOf(PolicyAssertionBuilder.class).forEach(e -> {
            try {
                policyAssertionBuilders.add(e.newInstance());
            } catch (InstantiationException ex) {
                ex.printStackTrace();
            } catch (IllegalAccessException ex) {
                ex.printStackTrace();
            }
        });
        policyXMLBuilder = new PolicyXMLBuilder(policyAssertionBuilders);

    }

    @Test
    void build() {
        Element policyElement = createElementWithAttributesAndChildren(
                document,
                "wsp:Policy",
                ImmutableMap.of("xmlns:L7p", "http://www.layer7tech.com/ws/policy", "xmlns:wsp", "http://schemas.xmlsoap.org/ws/2002/12/policy&quot"),
                createSetVariableAssertion(document, "var", "value"),
                createIncludeAssertionElement(document, "include"),
                createHardcodedAssertionElement(document, "response")
        );
        Element encassElement = createEncapsulatedAssertionElement(document, TEST_ENCASS, "encass");
        encassElement.setAttribute(EncapsulatedAssertionBuilder.ENCASS_NAME, TEST_ENCASS);
        policyElement.appendChild(encassElement);
        document.appendChild(policyElement);

        policy.setPolicyXML(DocumentTools.INSTANCE.elementToString(document.getDocumentElement()));
        policy.setParentFolder(Folder.ROOT_FOLDER);
        policy.setGuid("policyGuid");
        policy.setId("policyID");
        policy.setName(policy.getPath());
        Encass encass = new Encass();
        encass.setGuid("encass");
        encass.setName(TEST_ENCASS);
        Set<Annotation> annotations = new HashSet<>();
        Annotation annotation = new Annotation(AnnotationType.SHARED);
        annotations.add(annotation);
        policy.setAnnotations(annotations);
        bundle.getEncasses().put(TEST_ENCASS, encass);
        bundle.getPolicies().put("Policy", policy);
        Policy include = new Policy();
        include.setParentFolder(Folder.ROOT_FOLDER);
        include.setPath("test/policy/include");
        include.setName(include.getPath());
        include.setId("includeID");
        include.setGuid("includeGuid");
        annotations = new HashSet<>();
        annotation = new Annotation(AnnotationType.SHARED);
        annotations.add(annotation);
        include.setAnnotations(annotations);
        include.setPolicyXML("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<wsp:Policy xmlns:L7p=\"http://www.layer7tech.com/ws/policy\" xmlns:wsp=\"http://schemas.xmlsoap.org/ws/2002/12/policy\">\n" +
                "    <wsp:All wsp:Usage=\"Required\">\n" +
                "        <L7p:CommentAssertion>\n" +
                "            <L7p:Comment stringValue=\"Policy Fragment: includedPolicy\"/>\n" +
                "        </L7p:CommentAssertion>\n" +
                "    </wsp:All>\n" +
                "</wsp:Policy>");
        bundle.getPolicies().put("include", include);


        PolicyEntityBuilder builder = new PolicyEntityBuilder(DocumentTools.INSTANCE, new IdGenerator(), policyXMLBuilder);
        final List<Entity> entities = builder.build(bundle, BundleType.DEPLOYMENT, DocumentTools.INSTANCE.getDocumentBuilder().newDocument());

        assertFalse(entities.isEmpty());
        assertEquals(2, entities.size());
        entities.stream().collect(toMap(Entity::getId, identity())).forEach((i, e) -> {
            assertEquals(EntityTypes.POLICY_TYPE, e.getType());
            assertNotNull(e.getXml());
            assertTrue(e.getMappingProperties().containsKey(MAP_BY));
            assertEquals(MappingProperties.PATH, e.getMappingProperties().get(MAP_BY));
            assertTrue(e.getMappingProperties().containsKey(MAP_TO));
            assertEquals(e.getName(), e.getMappingProperties().get(MAP_TO));
            assertTrue("test/policy/include".equals(e.getName()) || "test/policy/path.xml".equals(e.getName()));
            assertTrue("includeID".equals(e.getId()) || "policyID".equals(e.getId()));
        });
    }

    @Test
    void buildDeploymentBundleWithWrongGuidAndId() {
        Element policyElement = createElementWithAttributesAndChildren(
                document,
                "wsp:Policy",
                ImmutableMap.of("xmlns:L7p", "http://www.layer7tech.com/ws/policy", "xmlns:wsp", "http://schemas.xmlsoap.org/ws/2002/12/policy&quot"),
                createSetVariableAssertion(document, "var", "value"),
                createHardcodedAssertionElement(document, "response")
        );
        Element encassElement = createEncapsulatedAssertionElement(document, TEST_ENCASS, "encass");
        encassElement.setAttribute(EncapsulatedAssertionBuilder.ENCASS_NAME, TEST_ENCASS);
        policyElement.appendChild(encassElement);
        document.appendChild(policyElement);

        policy.setPolicyXML(DocumentTools.INSTANCE.elementToString(document.getDocumentElement()));
        policy.setParentFolder(Folder.ROOT_FOLDER);
        policy.setGuid("policyGuid");
        policy.setId("policyID");
        policy.setName(policy.getPath());

        Set<Annotation> annotations = new HashSet<>();
        Annotation annotation = new Annotation(AnnotationType.BUNDLE_HINTS);
        annotation.setGuid("");
        annotation.setId("");
        annotations.add(annotation);
        annotation = new Annotation(AnnotationType.SHARED);
        annotations.add(annotation);
        policy.setAnnotations(annotations);
        policy.setParentEntityShared(policy.isShared());

        Encass encass = new Encass();
        encass.setGuid("encassGuid");
        encass.setName(TEST_ENCASS);
        Annotation encassAnnotation = new Annotation(AnnotationType.SHARED);
        annotations = new HashSet<>();
        annotations.add(encassAnnotation);
        encass.setAnnotations(annotations);
        encass.setParentEntityShared(encass.isShared());
        bundle.getEncasses().put(TEST_ENCASS, encass);
        bundle.getPolicies().put("Policy", policy);


        AnnotatedEntity annotatedEntity = encass.getAnnotatedEntity();
        annotatedEntity.setEntityName(encass.getName());
        AnnotatedBundle annotatedBundle = new AnnotatedBundle(bundle, annotatedEntity, new ProjectInfo("", "", ""));
        annotatedBundle.putAllEncasses(org.testcontainers.shaded.com.google.common.collect.ImmutableMap.of(TEST_ENCASS, encass));
        annotatedBundle.getPolicies().put("Policy", policy);

        PolicyEntityBuilder builder = new PolicyEntityBuilder(DocumentTools.INSTANCE, new IdGenerator(), policyXMLBuilder);
        List<Entity> entities = builder.build(annotatedBundle, BundleType.DEPLOYMENT, DocumentTools.INSTANCE.getDocumentBuilder().newDocument());

        assertFalse(entities.isEmpty());
        assertEquals(1, entities.size());
        Entity e = entities.get(0);
        assertEquals(EntityTypes.POLICY_TYPE, e.getType());
        assertEquals(e.getId(), "policyID");

        //wrong guid and id
        annotations = new HashSet<>();
        annotation = new Annotation(AnnotationType.SHARED);
        annotations.add(annotation);
        annotation = new Annotation(AnnotationType.BUNDLE_HINTS);
        annotation.setGuid("wrongGuid");
        annotation.setId("wrongId");
        annotations.add(annotation);
        policy.setAnnotations(annotations);
        policy.setAnnotatedEntity(null);

        builder = new PolicyEntityBuilder(DocumentTools.INSTANCE, new IdGenerator(), policyXMLBuilder);
        entities = builder.build(annotatedBundle, BundleType.DEPLOYMENT, DocumentTools.INSTANCE.getDocumentBuilder().newDocument());

        assertFalse(entities.isEmpty());
        assertEquals(1, entities.size());
        e = entities.get(0);
        assertEquals(EntityTypes.POLICY_TYPE, e.getType());
        assertEquals(e.getId(), "policyID");
    }

    @Test
    void maybeAddPolicy() {
        Policy policy1 = new Policy();
        policy1.setName("policy1");
        policy1.setPath("policy1");
        Policy policy2 = new Policy();
        policy2.setName("policy2");
        policy2.setPath("policy2");
        policy1.getDependencies().add(policy2);

        ArrayList<Policy> orderedPolicies = new ArrayList<>();
        HashSet<Policy> seenPolicies = new HashSet<>();
        PolicyEntityBuilder.maybeAddPolicy(bundle, policy1, orderedPolicies, seenPolicies);

        assertEquals(2, orderedPolicies.size());
        assertEquals(policy1, orderedPolicies.get(1));
        assertEquals(policy2, orderedPolicies.get(0));

        PolicyEntityBuilder.maybeAddPolicy(bundle, policy2, orderedPolicies, seenPolicies);

        assertEquals(2, orderedPolicies.size());
        assertEquals(policy1, orderedPolicies.get(1));
        assertEquals(policy2, orderedPolicies.get(0));

        // test dependency loop
        policy2.getDependencies().add(policy1);
        assertThrows(EntityBuilderException.class, () -> PolicyEntityBuilder.maybeAddPolicy(bundle, policy2, new ArrayList<>(), new HashSet<>()));
    }

    @Test
    void buildPolicyEntityTest() {
        PolicyEntityBuilder policyEntityBuilder = new PolicyEntityBuilder(DocumentTools.INSTANCE, new IdGenerator(), policyXMLBuilder);

        Policy policyToBuild = new Policy();
        policyToBuild.setPath("/path");
        policyToBuild.setName("path");
        policyToBuild.setId("policy-id");
        policyToBuild.setGuid("policy-guid-123");
        Folder parentFolder = new Folder();
        parentFolder.setId("folder-id");
        policyToBuild.setParentFolder(parentFolder);

        Entity policyEntity = policyEntityBuilder.buildPolicyEntity(policyToBuild, null, bundle, document);

        assertEquals(policyToBuild.getId(), policyEntity.getId());
    }

    @Test
    void buildPolicyEntityTestPBS() {
        PolicyEntityBuilder policyEntityBuilder = new PolicyEntityBuilder(DocumentTools.INSTANCE, new IdGenerator(), policyXMLBuilder);

        Policy policyToBuild = new Policy();
        policyToBuild.setPath("my/policy/path.xml");
        policyToBuild.setName("path");
        policyToBuild.setId("policy-id");
        policyToBuild.setGuid("policy-guid-123");
        Folder parentFolder = new Folder();
        parentFolder.setId("folder-id");
        policyToBuild.setParentFolder(parentFolder);

        PolicyBackedService policyBackedService = new PolicyBackedService();
        policyBackedService.setInterfaceName("pbs-interface");
        policyBackedService.setOperations(Sets.newHashSet(new PolicyBackedServiceOperation("my-op", policyToBuild.getPath())));
        bundle.getPolicyBackedServices().put("pbs", policyBackedService);
        Entity policyEntity = policyEntityBuilder.buildPolicyEntity(policyToBuild, null, bundle, document);

        assertEquals(policyToBuild.getId(), policyEntity.getId());
    }

    @Test
    void buildPolicyEntityTestGlobal() {
        PolicyEntityBuilder policyEntityBuilder = new PolicyEntityBuilder(DocumentTools.INSTANCE, new IdGenerator(), policyXMLBuilder);

        Policy policyToBuild = new Policy();
        policyToBuild.setPath("my/policy/global.xml");
        policyToBuild.setName("global.xml");
        policyToBuild.setId("global-policy-id");
        policyToBuild.setGuid("global-policy-guid-123");
        policyToBuild.setTag("global-policy");
        policyToBuild.setPolicyType(PolicyType.GLOBAL);
        Folder parentFolder = new Folder();
        parentFolder.setId("folder-id");
        policyToBuild.setParentFolder(parentFolder);

        Entity policyEntity = policyEntityBuilder.buildPolicyEntity(policyToBuild, null, bundle, document);

        assertEquals(policyToBuild.getId(), policyEntity.getId());
        assertEquals(policyToBuild.getPath(), policyEntity.getName());
        assertNotNull(policyEntity.getXml());
        Element policyDetail = getSingleChildElement(policyEntity.getXml(), POLICY_DETAIL);
        assertNotNull(policyDetail);
        String type = getSingleChildElementTextContent(policyDetail, POLICY_TYPE);
        assertNotNull(type);
        assertEquals(PolicyType.GLOBAL.getType(), type);
        final Map<String, Object> properties = mapPropertiesElements(getSingleChildElement(policyDetail, PROPERTIES), PROPERTIES);
        assertFalse(properties.isEmpty());
        assertEquals("global-policy", properties.get(PropertyConstants.PROPERTY_TAG));
        assertNull(properties.get(PropertyConstants.PROPERTY_SUBTAG));
    }

    @Test
    void buildPolicyEntityTestInternal() {
        PolicyEntityBuilder policyEntityBuilder = new PolicyEntityBuilder(DocumentTools.INSTANCE, new IdGenerator(), policyXMLBuilder);

        Policy policyToBuild = new Policy();
        policyToBuild.setPath("my/policy/internal.xml");
        policyToBuild.setName("internal.xml");
        policyToBuild.setId("internal-policy-id");
        policyToBuild.setGuid("internal-policy-guid-123");
        policyToBuild.setTag("internal-policy");
        policyToBuild.setPolicyType(PolicyType.INTERNAL);
        Folder parentFolder = new Folder();
        parentFolder.setId("folder-id");
        policyToBuild.setParentFolder(parentFolder);

        Entity policyEntity = policyEntityBuilder.buildPolicyEntity(policyToBuild, null, bundle, document);

        assertEquals(policyToBuild.getId(), policyEntity.getId());
        assertEquals(policyToBuild.getPath(), policyEntity.getName());
        assertNotNull(policyEntity.getXml());
        Element policyDetail = getSingleChildElement(policyEntity.getXml(), POLICY_DETAIL);
        assertNotNull(policyDetail);
        String type = getSingleChildElementTextContent(policyDetail, POLICY_TYPE);
        assertNotNull(type);
        assertEquals(PolicyType.INTERNAL.getType(), type);
        final Map<String, Object> properties = mapPropertiesElements(getSingleChildElement(policyDetail, PROPERTIES), PROPERTIES);
        assertFalse(properties.isEmpty());
        assertEquals("internal-policy", properties.get(PropertyConstants.PROPERTY_TAG));
        assertNull(properties.get(PropertyConstants.PROPERTY_SUBTAG));
    }

    private Element createHardcodedAssertionElement(Document document, String body) {
        Element hardcodedAssertion = document.createElement(HARDCODED_RESPONSE);

        Element bodyElement = document.createElement(RESPONSE_BODY);
        bodyElement.appendChild(document.createCDATASection(body));
        hardcodedAssertion.appendChild(bodyElement);
        return hardcodedAssertion;
    }

    @NotNull
    private Element createSetVariableAssertion(Document document, String variableName, String variableValue) {
        Element setVariableAssertion = document.createElement(SET_VARIABLE);

        Element expression = document.createElement(EXPRESSION);
        if (variableValue != null) {
            expression.appendChild(document.createCDATASection(variableValue));
        }
        setVariableAssertion.appendChild(expression);
        Element variableToSet = document.createElement(VARIABLE_TO_SET);
        variableToSet.setAttribute(PolicyEntityBuilder.STRING_VALUE, variableName);
        setVariableAssertion.appendChild(variableToSet);
        return setVariableAssertion;
    }

    private Element createIncludeAssertionElement(Document document, String policyPath) {
        Element includeAssertion = document.createElement(INCLUDE);

        Element guidElement = document.createElement(POLICY_GUID);
        guidElement.setAttribute(IncludeAssertionBuilder.POLICY_PATH, policyPath);
        includeAssertion.appendChild(guidElement);
        return includeAssertion;
    }


    @NotNull
    private Element createEncapsulatedAssertionElement(Document document, String name, String guid) {
        Element setEncapsulatedAssertion = document.createElement(ENCAPSULATED);

        Element nameElement = document.createElement(ENCAPSULATED_ASSERTION_CONFIG_NAME);
        setEncapsulatedAssertion.appendChild(nameElement);
        nameElement.setAttribute(PolicyEntityBuilder.STRING_VALUE, name);

        Element guidElement = document.createElement(ENCAPSULATED_ASSERTION_CONFIG_GUID);
        setEncapsulatedAssertion.appendChild(guidElement);
        guidElement.setAttribute(PolicyEntityBuilder.STRING_VALUE, guid);
        return setEncapsulatedAssertion;
    }
}