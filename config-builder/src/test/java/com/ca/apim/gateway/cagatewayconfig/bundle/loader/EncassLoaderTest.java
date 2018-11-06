/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayconfig.bundle.loader;

import com.ca.apim.gateway.cagatewayconfig.beans.Bundle;
import com.ca.apim.gateway.cagatewayconfig.beans.Encass;
import com.ca.apim.gateway.cagatewayconfig.beans.Policy;
import com.ca.apim.gateway.cagatewayconfig.util.entity.EntityTypes;
import com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentTools;
import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.UUID;

import static com.ca.apim.gateway.cagatewayconfig.util.gateway.BundleElementNames.*;
import static com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentUtils.*;
import static org.junit.jupiter.api.Assertions.*;

class EncassLoaderTest {

    private static final String TEST_GUID = UUID.randomUUID().toString();
    private static final String POLICY_ID = "PolicyID";
    private static final String TEST_POLICY_PATH = "PolicyPath";
    private static final String TEST_NAME = "EncassName";
    private EncassLoader loader = new EncassLoader();

    @Test
    void load() {
        Policy policy = new Policy();
        policy.setId(POLICY_ID);
        policy.setPath(TEST_POLICY_PATH);

        Bundle bundle = new Bundle();
        bundle.getPolicies().put(policy.getPath(), policy);
        load(bundle);
    }

    @Test
    void loadNoPolicy() {
        assertThrows(BundleLoadException.class, () -> load(new Bundle()));
    }

    @Test
    void loadMultiplePolicies() {
        Policy policy = new Policy();
        policy.setId(POLICY_ID);
        policy.setPath(TEST_POLICY_PATH);

        Policy secondPolicy = new Policy();
        secondPolicy.setId(POLICY_ID);
        secondPolicy.setPath(TEST_POLICY_PATH + "2");


        Bundle bundle = new Bundle();
        bundle.getPolicies().put(policy.getPath(), policy);
        bundle.getPolicies().put(secondPolicy.getPath(), secondPolicy);
        assertThrows(BundleLoadException.class, () -> load(bundle));
    }

    private void load(Bundle bundle) {
        loader.load(bundle, createEncassXml(DocumentTools.INSTANCE.getDocumentBuilder().newDocument()));

        assertFalse(bundle.getEncasses().isEmpty());
        assertEquals(1, bundle.getEncasses().size());
        assertNotNull(bundle.getEncasses().get(TEST_NAME));

        Encass entity = bundle.getEncasses().get(TEST_NAME);
        assertNotNull(entity);
        assertEquals(TEST_GUID, entity.getGuid());
        assertEquals("id", entity.getId());
    }

    private static Element createEncassXml(Document document) {
        Element encassElement = createElementWithAttributesAndChildren(
                document,
                ENCAPSULATED_ASSERTION,
                ImmutableMap.of(ATTRIBUTE_ID, "id"),
                createElementWithAttribute(document, POLICY_REFERENCE, ATTRIBUTE_ID, POLICY_ID),
                createElementWithTextContent(document, GUID, TEST_GUID),
                createElementWithTextContent(document, NAME, TEST_NAME)
        );

        encassElement.appendChild(
                createElementWithChildren(
                        document,
                        ENCAPSULATED_ARGUMENTS,
                        createElementWithChildren(
                            document,
                            ENCAPSULATED_ASSERTION_ARGUMENT,
                            createElementWithTextContent(document, ORDINAL, String.valueOf(1)),
                            createElementWithTextContent(document, ARGUMENT_NAME, "param"),
                            createElementWithTextContent(document, ARGUMENT_TYPE, "string"),
                            createElementWithTextContent(document, GUI_PROMPT, Boolean.TRUE.toString())
                    )
                )
        );
        encassElement.appendChild(
                createElementWithChildren(
                        document,
                        ENCAPSULATED_RESULTS,
                        createElementWithChildren(
                                document,
                                ENCAPSULATED_ASSERTION_RESULT,
                                createElementWithTextContent(document, RESULT_NAME, "result"),
                                createElementWithTextContent(document, RESULT_TYPE, "string")
                        )
                )
        );

        return createElementWithChildren(
                document,
                ITEM,
                createElementWithTextContent(document, ID, "id"),
                createElementWithTextContent(document, TYPE, EntityTypes.ENCAPSULATED_ASSERTION_TYPE),
                createElementWithChildren(
                        document,
                        RESOURCE,
                        encassElement
                )
        );
    }
}