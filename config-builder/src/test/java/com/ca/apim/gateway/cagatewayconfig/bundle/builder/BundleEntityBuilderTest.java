/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayconfig.bundle.builder;

import com.ca.apim.gateway.cagatewayconfig.beans.*;
import com.ca.apim.gateway.cagatewayconfig.bundle.builder.EntityBuilder.BundleType;
import com.ca.apim.gateway.cagatewayconfig.util.IdGenerator;
import com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentTools;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.testcontainers.shaded.com.google.common.collect.ImmutableMap;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.*;

import static com.ca.apim.gateway.cagatewayconfig.util.entity.EntityTypes.LISTEN_PORT_TYPE;
import static com.ca.apim.gateway.cagatewayconfig.util.gateway.BundleElementNames.BUNDLE;
import static com.ca.apim.gateway.cagatewayconfig.util.properties.PropertyConstants.*;
import static java.util.Collections.singleton;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class BundleEntityBuilderTest {

    private static final IdGenerator ID_GENERATOR = new IdGenerator();
    private static final String TEST_ENCASS = "TestEncass";
    private static final String TEST_ENCASS_POLICY = "TestEncassPolicy";
    private static final String TEST_ENCASS_ANNOTATION_NAME = "TestEncassAnnotationName";
    private static final String TEST_POLICY_PATH= "test/policy.xml";
    private static final String TEST_GUID = UUID.randomUUID().toString();
    private static final String TEST_POLICY_ID = "PolicyID";

    // This class is covered by testing others, so a simple testing is enough here.
    @Test
    void build() {
        BundleEntityBuilder builder = new BundleEntityBuilder(singleton(new TestEntityBuilder()), new BundleDocumentBuilder());

        final Map<String, Element> element = builder.build(new Bundle(), BundleType.DEPLOYMENT, DocumentTools.INSTANCE.getDocumentBuilder().newDocument(), "my-bundle", "1.0");
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
    void testEncassWithAnnotation() {
        final Bundle bundle = new Bundle();
        PolicyEntityBuilder policyBuilder = new PolicyEntityBuilder(DocumentTools.INSTANCE);
        EncassEntityBuilder encassBuilder = new EncassEntityBuilder(ID_GENERATOR);

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

        Set<EntityBuilder> entityBuilders = new HashSet<>();
        entityBuilders.add(policyBuilder);
        entityBuilders.add(encassBuilder);

        BundleEntityBuilder builder = new BundleEntityBuilder(entityBuilders, new BundleDocumentBuilder());
        final Map<String, Element> bundles = builder.build(bundle, BundleType.DEPLOYMENT, DocumentTools.INSTANCE.getDocumentBuilder().newDocument(), "my-bundle", "1.0");
        assertNotNull(bundles);
        assertEquals(1, bundles.size());
        for (Map.Entry<String, Element> bundleEntry : bundles.entrySet()) {
            assertEquals(TEST_ENCASS_ANNOTATION_NAME + "-" + "1.0", bundleEntry.getKey());
            final Element element = bundleEntry.getValue();
            assertNotNull(element);
            assertEquals(BundleDocumentBuilder.GATEWAY_MANAGEMENT, element.getAttribute(BundleDocumentBuilder.L7));
            assertEquals(BUNDLE, element.getTagName());
        }
    }

    private static Encass buildTestEncassWithAnnotation(String encassGuid, String policyPath) {
        Encass encass = new Encass();
        encass.setPolicy(policyPath);
        encass.setGuid(encassGuid);
        encass.setAnnotations(new HashSet<>());
        EntityAnnotation annotation = new EntityAnnotation();
        annotation.setType("@bundle");
        annotation.setName(TEST_ENCASS_ANNOTATION_NAME);
        annotation.setDescription(TEST_ENCASS_ANNOTATION_NAME);
        encass.getAnnotations().add(annotation);
        encass.setProperties(ImmutableMap.of(
                PALETTE_FOLDER, DEFAULT_PALETTE_FOLDER_LOCATION,
                PALETTE_ICON_RESOURCE_NAME, "someImage",
                ALLOW_TRACING, "false",
                DESCRIPTION, "someDescription",
                PASS_METRICS_TO_PARENT, "false"));
        return encass;
    }
}