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
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.testcontainers.shaded.com.google.common.collect.ImmutableMap;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.reflections.Reflections;
import java.security.cert.CertificateFactory;
import java.util.*;

import static com.ca.apim.gateway.cagatewayconfig.beans.EntityUtils.createEntityInfo;
import static com.ca.apim.gateway.cagatewayconfig.beans.Folder.ROOT_FOLDER;
import static com.ca.apim.gateway.cagatewayconfig.beans.Folder.ROOT_FOLDER_NAME;
import static com.ca.apim.gateway.cagatewayconfig.util.TestUtils.createFolder;
import static com.ca.apim.gateway.cagatewayconfig.util.TestUtils.createRoot;
import static com.ca.apim.gateway.cagatewayconfig.util.entity.EntityTypes.LISTEN_PORT_TYPE;
import static com.ca.apim.gateway.cagatewayconfig.util.gateway.BundleElementNames.*;
import static com.ca.apim.gateway.cagatewayconfig.util.properties.PropertyConstants.*;
import static com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentUtils.*;
import static java.util.Collections.singleton;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

class BundleEntityBuilderTest {

    private static final IdGenerator ID_GENERATOR = new IdGenerator();
    private static final String TEST_ENCASS = "TestEncass";
    private static final String TEST_ENCASS_POLICY = "TestEncassPolicy";
    private static final String TEST_ENCASS_ANNOTATION_NAME = "TestEncassAnnotationName";
    private static final String TEST_POLICY_PATH = "test/policy.xml";
    private static final String TEST_GUID = UUID.randomUUID().toString();
    private static final String TEST_POLICY_ID = "PolicyID";
    private static final String TEST_ENCASS_ID = "EncassID";
    private EntityTypeRegistry entityTypeRegistry = new EntityTypeRegistry(new Reflections());

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

        Policy policy = new Policy();
        policy.setParentFolder(Folder.ROOT_FOLDER);
        policy.setName(TEST_ENCASS_POLICY);
        policy.setId(TEST_POLICY_ID);
        policy.setGuid(TEST_GUID);
        policy.setPolicyXML("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<wsp:Policy xmlns:L7p=\"http://www.layer7tech.com/ws/policy\" xmlns:wsp=\"http://schemas.xmlsoap.org/ws/2002/12/policy\">\n" +
                "    <wsp:All wsp:Usage=\"Required\">\n" +
                "        <L7p:CommentAssertion>\n" +
                "            <L7p:Comment stringValue=\"Policy Fragment: includedPolicy\"/>\n" +
                "        </L7p:CommentAssertion>\n" +
                "    </wsp:All>\n" +
                "</wsp:Policy>");
        policy.setPath(TEST_ENCASS_POLICY);
        bundle.getPolicies().put(TEST_ENCASS_POLICY, policy);

        Encass encass = buildTestEncassWithAnnotation(TEST_GUID, TEST_ENCASS_POLICY);
        bundle.putAllEncasses(ImmutableMap.of(TEST_ENCASS, encass));

        Map<Dependency, List<Dependency>> dependencyListMap = new HashMap<>();
        List<Dependency> dependencies = new ArrayList<>();
        dependencies.add(new Dependency(TEST_ENCASS_ID, Encass.class, TEST_ENCASS, EntityTypes.ENCAPSULATED_ASSERTION_TYPE));
        dependencyListMap.put(new Dependency(TEST_POLICY_ID, Policy.class, TEST_ENCASS_POLICY, EntityTypes.POLICY_TYPE), dependencies);
        bundle.setDependencyMap(dependencyListMap);

        Set<EntityBuilder> entityBuilders = new HashSet<>();
        entityBuilders.add(folderBuilder);
        entityBuilders.add(policyBuilder);
        entityBuilders.add(encassBuilder);

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
            assertEquals(3, itemList.size());
            final Element item1 = itemList.get(0);
            assertEquals(ROOT_FOLDER_NAME, getSingleChildElementTextContent(item1, NAME));
            assertEquals(EntityTypes.FOLDER_TYPE, getSingleChildElementTextContent(item1, TYPE));
            assertNotNull(getSingleChildElement(item1, RESOURCE));
            final Element item2 = itemList.get(1);
            assertEquals("my-bundle-encass-TestEncass-" +TEST_ENCASS_POLICY + "-1.0", getSingleChildElementTextContent(item2, NAME));
            assertEquals(EntityTypes.POLICY_TYPE, getSingleChildElementTextContent(item2, TYPE));
            assertNotNull(getSingleChildElement(item2, RESOURCE));
            final Element item3 = itemList.get(2);
            assertEquals(TEST_ENCASS, getSingleChildElementTextContent(item3, NAME));
            assertEquals(EntityTypes.ENCAPSULATED_ASSERTION_TYPE, getSingleChildElementTextContent(item3, TYPE));
            assertNotNull(getSingleChildElement(item3, RESOURCE));
        }
    }

    private static Encass buildTestEncassWithAnnotation(String encassGuid, String policyPath) {
        Encass encass = new Encass();
        encass.setName(TEST_ENCASS);
        encass.setPolicy(policyPath);
        encass.setId(TEST_ENCASS_ID);
        encass.setGuid(encassGuid);
        Set<Annotation> annotations = new HashSet<>();
        Annotation annotation = new Annotation("@bundle");
        annotation.setName(TEST_ENCASS_ANNOTATION_NAME);
        annotations.add(annotation);
        annotations.add(new Annotation(AnnotationConstants.ANNOTATION_TYPE_REUSABLE));
        encass.setAnnotations(annotations);
        encass.setProperties(ImmutableMap.of(
                PALETTE_FOLDER, DEFAULT_PALETTE_FOLDER_LOCATION,
                PALETTE_ICON_RESOURCE_NAME, "someImage",
                ALLOW_TRACING, "false",
                DESCRIPTION, "someDescription",
                PASS_METRICS_TO_PARENT, "false"));
        return encass;
    }
}