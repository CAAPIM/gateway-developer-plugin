/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayexport.util;

import com.ca.apim.gateway.cagatewayconfig.beans.*;
import com.ca.apim.gateway.cagatewayconfig.util.entity.EntityTypes;
import com.google.common.collect.ImmutableMap;
import org.jetbrains.annotations.NotNull;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import static com.ca.apim.gateway.cagatewayconfig.util.gateway.BuilderUtils.buildPropertiesElement;
import static com.ca.apim.gateway.cagatewayconfig.util.gateway.BundleElementNames.*;
import static com.ca.apim.gateway.cagatewayconfig.util.gateway.BundleElementNames.RESOURCE;
import static com.ca.apim.gateway.cagatewayconfig.util.gateway.BundleElementNames.TYPE;
import static com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentUtils.*;
import static com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentUtils.createElementWithChildren;
import static com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentUtils.createElementWithTextContent;

public class TestUtils {

    public static Element createServiceXml(Document document, boolean withProperties) {
        Element element = createElementWithAttributesAndChildren(
                document,
                SERVICE,
                ImmutableMap.of(ATTRIBUTE_ID, "id"),
                createElementWithAttributesAndChildren(
                        document,
                        SERVICE_DETAIL,
                        ImmutableMap.of(ATTRIBUTE_ID, "id", ATTRIBUTE_FOLDER_ID, "folder"),
                        createElementWithTextContent(document, NAME, "service"),
                        createElementWithChildren(
                                document,
                                SERVICE_MAPPINGS,
                                createElementWithChildren(
                                        document,
                                        HTTP_MAPPING,
                                        createElementWithTextContent(document, URL_PATTERN, "/service"),
                                        createElementWithChildren(
                                                document,
                                                VERBS,
                                                createElementWithTextContent(document, VERB, "GET"),
                                                createElementWithTextContent(document, VERB, "POST"),
                                                createElementWithTextContent(document, VERB, "PUT"),
                                                createElementWithTextContent(document, VERB, "DELETE")
                                        )
                                )
                        ),
                        buildPropertiesElement(
                                withProperties ? ImmutableMap.of("property.prop", "value", "property.ENV.prop", "value2") : Collections.emptyMap(),
                                document
                        )
                ),
                createElementWithChildren(
                        document,
                        RESOURCES,
                        createElementWithAttributesAndChildren(
                                document,
                                RESOURCE_SET,
                                ImmutableMap.of("tag", "policy"),
                                createElementWithAttributesAndTextContent(
                                        document,
                                        RESOURCE,
                                        ImmutableMap.of("type", "policy"),
                                        "policy"
                                )
                        )
                )
        );

        return createElementWithChildren(
                document,
                ITEM,
                createElementWithTextContent(document, ID, "id"),
                createElementWithTextContent(document, TYPE, EntityTypes.SERVICE_TYPE),
                createElementWithChildren(
                        document,
                        RESOURCE,
                        element
                )
        );
    }

    @NotNull
    public static Folder createFolder(String folderName, String folderId, Folder parent) {
        Folder folder = new Folder();
        folder.setName(folderName);
        folder.setId(folderId);
        folder.setParentFolder(parent);
        return folder;
    }

    public static Policy createPolicy(final String name, final String id, final String guid, final String parentFolderId, Element policyElement, String policyString) {
        return createPolicy(name, id, guid, parentFolderId, policyElement, policyString, null);
    }

    public static Policy createPolicy(final String name, final String id, final String guid, final String parentFolderId, Element policyElement, String policyString, String tag) {
        Policy policy = new Policy();
        policy.setName(name);
        policy.setId(id);
        policy.setGuid(guid);
        policy.setParentFolder(new Folder(parentFolderId, null));
        policy.setPolicyDocument(policyElement);
        policy.setPolicyXML(policyString);
        policy.setTag(tag);
        return policy;
    }

    public static CassandraConnection createCassandraConnection(String name, String id) {
        CassandraConnection cassandraConnection = new CassandraConnection();
        cassandraConnection.setName(name);
        cassandraConnection.setId(id);
        return cassandraConnection;
    }

    public static ClusterProperty createClusterProperty(String name, String value, String id) {
        ClusterProperty clusterProperty = new ClusterProperty();
        clusterProperty.setName(name);
        clusterProperty.setValue(value);
        clusterProperty.setId(id);
        return clusterProperty;
    }

    public static Encass createEncass(final String name, final String id, final String guid, String policyId) {
        return createEncass(name, id, guid, policyId, null, null);
    }

    public static Encass createEncass(final String name, final String id, final String guid, String policyId, Set<EncassArgument> arguments, Set<EncassResult> results) {
        Encass encass = new Encass();
        encass.setName(name);
        encass.setId(id);
        encass.setGuid(guid);
        encass.setPolicyId(policyId);
        encass.setArguments(arguments);
        encass.setResults(results);
        return encass;
    }

    public static PolicyBackedService createPolicyBackedService(final String name, final String id, String interfaceName, final Map<String, String> operations) {
        PolicyBackedService pbs = new PolicyBackedService();
        pbs.setName(name);
        pbs.setId(id);
        pbs.setInterfaceName(interfaceName);
        pbs.setOperations(new LinkedHashSet<>());
        operations.forEach((k,v) -> pbs.getOperations().add(new PolicyBackedServiceOperation(k, v)));
        return pbs;
    }

    public static Service createService(final String name, final String id, final Folder parentFolder, Element serviceDetailsElement, String policy) {
        Service service = new Service();
        service.setName(name);
        service.setId(id);
        service.setParentFolder(parentFolder);
        service.setServiceDetailsElement(serviceDetailsElement);
        service.setPolicy(policy);
        return service;
    }

    public static JmsDestination createJmsDestination(String name, String id) {
        JmsDestination jmsDestination = new JmsDestination();
        jmsDestination.setName(name);
        jmsDestination.setId(id);
        jmsDestination.setIsInbound(false);
        jmsDestination.setProviderType(null); // null for Generic JMS
        jmsDestination.setIsInbound(false);
        jmsDestination.setInitialContextFactoryClassName("org.apache.activemq.jndi.ActiveMQInitialContextFactory");
        jmsDestination.setJndiUrl("tcp://qaactivemq:61616");
        jmsDestination.setDestinationType(JmsDestination.DestinationType.QUEUE);
        jmsDestination.setConnectionFactoryName("my-qcf-name");
        jmsDestination.setDestinationName("my-queue");
        return jmsDestination;
    }
}
