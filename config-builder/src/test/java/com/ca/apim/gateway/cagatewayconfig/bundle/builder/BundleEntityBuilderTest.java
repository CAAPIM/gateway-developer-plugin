/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayconfig.bundle.builder;

import com.ca.apim.gateway.cagatewayconfig.beans.*;
import com.ca.apim.gateway.cagatewayconfig.bundle.builder.EntityBuilder.BundleType;
import com.ca.apim.gateway.cagatewayconfig.util.IdGenerator;
import com.ca.apim.gateway.cagatewayconfig.util.entity.AnnotationConstants;
import com.ca.apim.gateway.cagatewayconfig.util.entity.EntityTypes;
import com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentTools;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.reflections.Reflections;
import org.testcontainers.shaded.com.google.common.collect.ImmutableMap;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.*;

import static com.ca.apim.gateway.cagatewayconfig.beans.Folder.ROOT_FOLDER;
import static com.ca.apim.gateway.cagatewayconfig.beans.Folder.ROOT_FOLDER_NAME;
import static com.ca.apim.gateway.cagatewayconfig.util.TestUtils.createFolder;
import static com.ca.apim.gateway.cagatewayconfig.util.TestUtils.createRoot;
import static com.ca.apim.gateway.cagatewayconfig.util.entity.EntityTypes.LISTEN_PORT_TYPE;
import static com.ca.apim.gateway.cagatewayconfig.util.gateway.BundleElementNames.*;
import static com.ca.apim.gateway.cagatewayconfig.util.gateway.MappingActions.NEW_OR_EXISTING;
import static com.ca.apim.gateway.cagatewayconfig.util.gateway.MappingActions.NEW_OR_UPDATE;
import static com.ca.apim.gateway.cagatewayconfig.util.properties.PropertyConstants.*;
import static com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentUtils.*;
import static java.util.Collections.singleton;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class BundleEntityBuilderTest {

    private static final IdGenerator ID_GENERATOR = new IdGenerator();
    private static final String TEST_ENCASS = "TestEncass";
    private static final String TEST_ENCASS_POLICY = "TestEncassPolicy";
    private static final String TEST_DEP_ENCASS = "TestDepEncass";
    private static final String TEST_DEP_ENCASS_POLICY = "TestDepEncassPolicy";
    private static final String TEST_ENCASS_ANNOTATION_NAME = "TestEncassAnnotationName";
    private static final String TEST_GUID = UUID.randomUUID().toString();
    private static final String TEST_POLICY_ID = "PolicyID";
    private static final String TEST_ENCASS_ID = "EncassID";
    private static final String TEST_DEP_POLICY_ID = "DepPolicyID";
    private static final String TEST_DEP_ENCASS_ID = "DepEncassID";
    private static final EntityTypeRegistry entityTypeRegistry = new EntityTypeRegistry(new Reflections());

    // This class is covered by testing others, so a simple testing is enough here.
    @Test
    void build() {
        BundleEntityBuilder builder = new BundleEntityBuilder(singleton(new TestEntityBuilder()),
                new BundleDocumentBuilder(), new BundleMetadataBuilder(), entityTypeRegistry);

        final Map<String, Pair<Element, BundleMetadata>> element = builder.build(new Bundle(), BundleType.DEPLOYMENT,
                DocumentTools.INSTANCE.getDocumentBuilder().newDocument(), "my-bundle", "my-bundle-group", "1.0");
        assertNotNull(element);
    }

    private static class TestEntityBuilder implements EntityBuilder {
        @Override
        public List<Entity> build(Bundle bundle, BundleType bundleType, Document document) {
            return Collections.singletonList(EntityBuilderHelper.getEntityWithOnlyMapping(LISTEN_PORT_TYPE, "Test", "Test"));
        }

        @Override
        public @NotNull Integer getOrder() {
            return 0;
        }
    }

    @Test
    void testEncassAnnotatedBundle() {
        FolderEntityBuilder folderBuilder = new FolderEntityBuilder(ID_GENERATOR);
        PolicyEntityBuilder policyBuilder = new PolicyEntityBuilder(DocumentTools.INSTANCE, ID_GENERATOR);
        EncassEntityBuilder encassBuilder = new EncassEntityBuilder(ID_GENERATOR);

        Bundle bundle = new Bundle();
        Folder root = createRoot();
        bundle.getFolders().put(EMPTY, root);

        Folder dummyFolder = createFolder("dummy", TEST_GUID, ROOT_FOLDER);
        dummyFolder.setParentFolder(Folder.ROOT_FOLDER);
        bundle.getFolders().put(dummyFolder.getPath(), dummyFolder);

        Set<Annotation> encassAnnotations = new HashSet<>();
        Annotation bundleAnnotation = new Annotation(AnnotationConstants.ANNOTATION_TYPE_BUNDLE);
        bundleAnnotation.setName(TEST_ENCASS_ANNOTATION_NAME);
        Annotation reusableAnnotation = new Annotation(AnnotationConstants.ANNOTATION_TYPE_REUSABLE);
        encassAnnotations.add(bundleAnnotation);
        encassAnnotations.add(reusableAnnotation);

        Set<Annotation> depPolicyAnnotations = new HashSet<>();
        depPolicyAnnotations.add(reusableAnnotation);

        Policy depPolicy = buildTestPolicyWithAnnotation(TEST_DEP_ENCASS_POLICY, TEST_DEP_POLICY_ID, TEST_GUID, depPolicyAnnotations);
        bundle.getPolicies().put(TEST_DEP_ENCASS_POLICY, depPolicy);

        Encass depEncass = buildTestEncassWithAnnotation(TEST_DEP_ENCASS, TEST_DEP_ENCASS_ID, TEST_GUID, TEST_DEP_ENCASS_POLICY, Collections.EMPTY_SET);
        bundle.getEncasses().put(TEST_DEP_ENCASS, depEncass);

        Set<Dependency> usedEntities = new LinkedHashSet<>();
        usedEntities.add(new Dependency(TEST_DEP_POLICY_ID, Policy.class, TEST_DEP_ENCASS_POLICY, EntityTypes.POLICY_TYPE));
        usedEntities.add(new Dependency(TEST_DEP_ENCASS_ID, Encass.class, TEST_DEP_ENCASS, EntityTypes.ENCAPSULATED_ASSERTION_TYPE));
        usedEntities.add(new Dependency(TEST_ENCASS_ID, Encass.class, TEST_ENCASS, EntityTypes.ENCAPSULATED_ASSERTION_TYPE));

        Policy policy = buildTestPolicyWithAnnotation(TEST_ENCASS_POLICY, TEST_POLICY_ID, TEST_GUID, Collections.EMPTY_SET);
        policy.setUsedEntities(usedEntities);
        bundle.getPolicies().put(TEST_ENCASS_POLICY, policy);

        Encass encass = buildTestEncassWithAnnotation(TEST_ENCASS, TEST_ENCASS_ID, TEST_GUID, TEST_ENCASS_POLICY, encassAnnotations);
        bundle.getEncasses().put(TEST_ENCASS, encass);

        Set<EntityBuilder> entityBuilders = new HashSet<>();
        entityBuilders.add(folderBuilder);
        entityBuilders.add(policyBuilder);
        entityBuilders.add(encassBuilder);

        buildAndValidateAnnotatedBundle(bundle, entityBuilders, "my-bundle-encass-TestEncass-" + TEST_ENCASS_POLICY + "-1.0", NEW_OR_UPDATE, TEST_ENCASS, NEW_OR_EXISTING,
                TEST_DEP_ENCASS_POLICY, NEW_OR_EXISTING, "my-bundle-encass-TestEncass-" + TEST_DEP_ENCASS + "-1.0", NEW_OR_UPDATE);
    }

    @Test
    void testEncassAnnotatedRedeployableBundle() {
        FolderEntityBuilder folderBuilder = new FolderEntityBuilder(ID_GENERATOR);
        PolicyEntityBuilder policyBuilder = new PolicyEntityBuilder(DocumentTools.INSTANCE, ID_GENERATOR);
        EncassEntityBuilder encassBuilder = new EncassEntityBuilder(ID_GENERATOR);

        Bundle bundle = new Bundle();
        Folder root = createRoot();
        bundle.getFolders().put(EMPTY, root);

        Set<Annotation> encassAnnotations = new HashSet<>();
        Annotation bundleAnnotation = new Annotation(AnnotationConstants.ANNOTATION_TYPE_BUNDLE);
        bundleAnnotation.setName(TEST_ENCASS_ANNOTATION_NAME);
        Annotation reusableAnnotation = new Annotation(AnnotationConstants.ANNOTATION_TYPE_REUSABLE);
        Annotation redeployableAnnotation = new Annotation(AnnotationConstants.ANNOTATION_TYPE_REDEPLOYABLE);
        encassAnnotations.add(bundleAnnotation);
        encassAnnotations.add(reusableAnnotation);
        encassAnnotations.add(redeployableAnnotation);

        Set<Annotation> depPolicyAnnotations = new HashSet<>();
        depPolicyAnnotations.add(reusableAnnotation);

        Policy depPolicy = buildTestPolicyWithAnnotation(TEST_DEP_ENCASS_POLICY, TEST_DEP_POLICY_ID, TEST_GUID, depPolicyAnnotations);
        bundle.getPolicies().put(TEST_DEP_ENCASS_POLICY, depPolicy);

        Set<Annotation> depEncassAnnotations = new HashSet<>();
        depEncassAnnotations.add(reusableAnnotation);

        Encass depEncass = buildTestEncassWithAnnotation(TEST_DEP_ENCASS, TEST_DEP_ENCASS_ID, TEST_GUID, TEST_DEP_ENCASS_POLICY, depEncassAnnotations);
        bundle.getEncasses().put(TEST_DEP_ENCASS, depEncass);

        Set<Dependency> usedEntities = new LinkedHashSet<>();
        usedEntities.add(new Dependency(TEST_DEP_POLICY_ID, Policy.class, TEST_DEP_ENCASS_POLICY, EntityTypes.POLICY_TYPE));
        usedEntities.add(new Dependency(TEST_DEP_ENCASS_ID, Encass.class, TEST_DEP_ENCASS, EntityTypes.ENCAPSULATED_ASSERTION_TYPE));
        usedEntities.add(new Dependency(TEST_ENCASS_ID, Encass.class, TEST_ENCASS, EntityTypes.ENCAPSULATED_ASSERTION_TYPE));

        Policy policy = buildTestPolicyWithAnnotation(TEST_ENCASS_POLICY, TEST_POLICY_ID, TEST_GUID, Collections.EMPTY_SET);
        policy.setUsedEntities(usedEntities);
        bundle.getPolicies().put(TEST_ENCASS_POLICY, policy);

        Encass encass = buildTestEncassWithAnnotation(TEST_ENCASS, TEST_ENCASS_ID, TEST_GUID, TEST_ENCASS_POLICY, encassAnnotations);
        bundle.getEncasses().put(TEST_ENCASS, encass);

        Set<EntityBuilder> entityBuilders = new HashSet<>();
        entityBuilders.add(folderBuilder);
        entityBuilders.add(policyBuilder);
        entityBuilders.add(encassBuilder);

        buildAndValidateAnnotatedBundle(bundle, entityBuilders, "my-bundle-encass-TestEncass-" + TEST_ENCASS_POLICY + "-1.0", NEW_OR_UPDATE, TEST_ENCASS, NEW_OR_UPDATE,
                TEST_DEP_ENCASS_POLICY, NEW_OR_UPDATE, TEST_DEP_ENCASS, NEW_OR_UPDATE);

    }

    private static void buildAndValidateAnnotatedBundle(Bundle bundle, Set<EntityBuilder> entityBuilders,
                                                        String expEncassPolicyName, String expEncassPolicyAction, String expEncassName, String expEncassAction,
                                                        String expDepEncassPolicyName, String expDepEncassPolicyAction, String expDepEncassName, String expDepEncassAction) {
        BundleEntityBuilder builder = new BundleEntityBuilder(entityBuilders, new BundleDocumentBuilder(), new BundleMetadataBuilder(), entityTypeRegistry);
        Map<String, Pair<Element, BundleMetadata>> bundles = builder.build(bundle, BundleType.DEPLOYMENT,
                DocumentTools.INSTANCE.getDocumentBuilder().newDocument(), "my-bundle", "my-bundle-group", "1.0");
        assertNotNull(bundles);
        assertEquals(1, bundles.size());
        for (Map.Entry<String, Pair<Element, BundleMetadata>> bundleEntry : bundles.entrySet()) {
            assertEquals(TEST_ENCASS_ANNOTATION_NAME + "-" + "1.0", bundleEntry.getKey());
            final Element element = bundleEntry.getValue().getLeft();
            assertNotNull(element);
            assertEquals(BundleDocumentBuilder.GATEWAY_MANAGEMENT, element.getAttribute(BundleDocumentBuilder.L7));
            assertEquals(BUNDLE, element.getTagName());
            final Element references = getSingleChildElement(element, REFERENCES);
            assertNotNull(references);
            final List<Element> itemList = getChildElements(references, ITEM);
            assertNotNull(itemList);
            assertEquals(5, itemList.size());
            final Element folderElement = itemList.get(0);
            assertEquals(ROOT_FOLDER_NAME, getSingleChildElementTextContent(folderElement, NAME));
            assertEquals(EntityTypes.FOLDER_TYPE, getSingleChildElementTextContent(folderElement, TYPE));
            assertNotNull(getSingleChildElement(folderElement, RESOURCE));
            final Element policyElement = itemList.get(1);
            assertEquals(expEncassPolicyName, getSingleChildElementTextContent(policyElement, NAME));
            assertEquals(EntityTypes.POLICY_TYPE, getSingleChildElementTextContent(policyElement, TYPE));
            assertNotNull(getSingleChildElement(policyElement, RESOURCE));
            final Element depPolicyElement = itemList.get(2);
            assertEquals(expDepEncassPolicyName, getSingleChildElementTextContent(depPolicyElement, NAME));
            assertEquals(EntityTypes.POLICY_TYPE, getSingleChildElementTextContent(depPolicyElement, TYPE));
            assertNotNull(getSingleChildElement(depPolicyElement, RESOURCE));
            final Element encassElement = itemList.get(3);
            assertEquals(expEncassName, getSingleChildElementTextContent(encassElement, NAME));
            assertEquals(EntityTypes.ENCAPSULATED_ASSERTION_TYPE, getSingleChildElementTextContent(encassElement, TYPE));
            assertNotNull(getSingleChildElement(encassElement, RESOURCE));
            final Element depEncassElement = itemList.get(4);
            assertEquals(expDepEncassName, getSingleChildElementTextContent(depEncassElement, NAME));
            assertEquals(EntityTypes.ENCAPSULATED_ASSERTION_TYPE, getSingleChildElementTextContent(depEncassElement, TYPE));
            assertNotNull(getSingleChildElement(depEncassElement, RESOURCE));

            final Element mappings = getSingleChildElement(element, MAPPINGS);
            assertNotNull(mappings);
            final List<Element> mappingsList = getChildElements(mappings, MAPPING);
            assertNotNull(mappingsList);
            assertEquals(5, mappingsList.size());

            final Element folderMapping = mappingsList.get(0);
            assertEquals(NEW_OR_EXISTING, folderMapping.getAttribute(ATTRIBUTE_ACTION));
            assertEquals(EntityTypes.FOLDER_TYPE, folderMapping.getAttribute(ATTRIBUTE_TYPE));

            final Element policyMapping = mappingsList.get(1);
            assertEquals(expEncassPolicyAction, policyMapping.getAttribute(ATTRIBUTE_ACTION));
            assertEquals(EntityTypes.POLICY_TYPE, policyMapping.getAttribute(ATTRIBUTE_TYPE));

            final Element depPolicyMapping = mappingsList.get(2);
            assertEquals(expDepEncassPolicyAction, depPolicyMapping.getAttribute(ATTRIBUTE_ACTION));
            assertEquals(EntityTypes.POLICY_TYPE, depPolicyMapping.getAttribute(ATTRIBUTE_TYPE));

            final Element encassMapping = mappingsList.get(3);
            assertEquals(expEncassAction, encassMapping.getAttribute(ATTRIBUTE_ACTION));
            assertEquals(EntityTypes.ENCAPSULATED_ASSERTION_TYPE, encassMapping.getAttribute(ATTRIBUTE_TYPE));

            final Element depEncassMapping = mappingsList.get(4);
            assertEquals(expDepEncassAction, depEncassMapping.getAttribute(ATTRIBUTE_ACTION));
            assertEquals(EntityTypes.ENCAPSULATED_ASSERTION_TYPE, depEncassMapping.getAttribute(ATTRIBUTE_TYPE));
        }
    }

    private static Encass buildTestEncassWithAnnotation(String encassName, String encassId, String encassGuid, String policyPath, Set<Annotation> annotations) {
        Encass encass = new Encass();
        encass.setName(encassName);
        encass.setPolicy(policyPath);
        encass.setId(encassId);
        encass.setGuid(encassGuid);
        encass.setAnnotations(annotations);
        encass.setProperties(ImmutableMap.of(
                PALETTE_FOLDER, DEFAULT_PALETTE_FOLDER_LOCATION,
                PALETTE_ICON_RESOURCE_NAME, "someImage",
                ALLOW_TRACING, "false",
                DESCRIPTION, "someDescription",
                PASS_METRICS_TO_PARENT, "false"));
        return encass;
    }

    private static Policy buildTestPolicyWithAnnotation(String policyName, String policyId, String policyGuid, Set<Annotation> annotations) {
        Policy policy = new Policy();
        policy.setParentFolder(Folder.ROOT_FOLDER);
        policy.setName(policyName);
        policy.setId(policyId);
        policy.setGuid(policyGuid);
        policy.setPolicyXML("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<wsp:Policy xmlns:L7p=\"http://www.layer7tech.com/ws/policy\" xmlns:wsp=\"http://schemas.xmlsoap.org/ws/2002/12/policy\">\n" +
                "    <wsp:All wsp:Usage=\"Required\">\n" +
                "        <L7p:CommentAssertion>\n" +
                "            <L7p:Comment stringValue=\"Policy Fragment: includedPolicy\"/>\n" +
                "        </L7p:CommentAssertion>\n" +
                "    </wsp:All>\n" +
                "</wsp:Policy>");
        policy.setPath(policyName);
        policy.setAnnotations(annotations);
        return policy;
    }
}