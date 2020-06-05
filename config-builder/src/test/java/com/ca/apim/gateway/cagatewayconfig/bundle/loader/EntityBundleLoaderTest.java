/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayconfig.bundle.loader;

import com.ca.apim.gateway.cagatewayconfig.beans.Bundle;
import com.ca.apim.gateway.cagatewayconfig.beans.Encass;
import com.ca.apim.gateway.cagatewayconfig.beans.Policy;
import com.ca.apim.gateway.cagatewayconfig.bundle.builder.BundleDefinedEntities;
import com.ca.apim.gateway.cagatewayconfig.bundle.builder.DefaultMetadata;
import com.ca.apim.gateway.cagatewayconfig.util.TestUtils;
import com.ca.apim.gateway.cagatewayconfig.util.entity.EntityTypes;
import com.ca.apim.gateway.cagatewayconfig.util.file.JsonFileUtils;
import com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentParseException;
import com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentTools;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.Extensions;
import org.mockito.*;
import org.mockito.junit.jupiter.*;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import static com.ca.apim.gateway.cagatewayconfig.util.gateway.BundleElementNames.ITEM;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@Extensions({ @ExtendWith(MockitoExtension.class) })
class EntityBundleLoaderTest {

    @Mock
    private DocumentTools documentTools;
    private BundleEntityLoaderRegistry registry;

    @BeforeEach
    void before() {
        this.registry = new BundleEntityLoaderRegistry(
                Stream.of(
                        new CassandraConnectionsLoader(),
                        new JdbcConnectionLoader()
                ).collect(toSet())
        );
    }

    @Test
    void load() throws DocumentParseException {
        Document realDocument = DocumentTools.INSTANCE.getDocumentBuilder().newDocument();
        Document mockDocument = mock(Document.class);
        when(mockDocument.getElementsByTagName(eq(ITEM))).thenReturn(new NodeList() {

            private List<Element> elements = Stream.of(
                    TestUtils.createJdbcXml(realDocument),
                    TestUtils.createCassandraXml(realDocument, true, true),
                    TestUtils.createUnsupportedElement(realDocument)
            ).collect(toList());

            @Override
            public Node item(int index) {
                return elements.get(index);
            }

            @Override
            public int getLength() {
                return elements.size();
            }
        });

        when(documentTools.parse(any(File.class))).thenReturn(mockDocument);

        EntityBundleLoader loader = new EntityBundleLoader(documentTools, registry, JsonFileUtils.INSTANCE);
        final Bundle bundle = loader.load(mock(File.class), BundleLoadingOperation.EXPORT);

        assertNotNull(bundle);
        assertFalse(bundle.getCassandraConnections().isEmpty());
        assertFalse(bundle.getJdbcConnections().isEmpty());
        assertEquals(1, bundle.getCassandraConnections().size());
        assertEquals(1, bundle.getJdbcConnections().size());
    }

    @Test
    public void testLoadMetadataForEncass(){
        JsonFileUtils jsonFileUtils = mock(JsonFileUtils.class);
        Set<DefaultMetadata> metadataSet = new HashSet<>();
        DefaultMetadata encassMetadata =  new DefaultMetadata();
        encassMetadata.setName("TestEncass");
        encassMetadata.setType(EntityTypes.ENCAPSULATED_ASSERTION_TYPE);
        encassMetadata.setId("testId");
        encassMetadata.setGuid("testGuid");
        metadataSet.add(encassMetadata);
        BundleDefinedEntities bundleDefinedEntities = new BundleDefinedEntities();
        bundleDefinedEntities.setDefinedEntities(metadataSet);
        when(jsonFileUtils.readBundleMetadataFile(any(File.class))).thenReturn(bundleDefinedEntities);
        EntityBundleLoader loader = new EntityBundleLoader(documentTools, registry, jsonFileUtils);
        Bundle bundle = loader.loadMetadata(mock(File.class), BundleLoadingOperation.EXPORT);
        Map<String, Encass> encassMap = bundle.getEncasses();
        assertNotNull(encassMap.get("TestEncass"));
    }

    @Test
    public void testLoadMetadataForPolicy(){
        JsonFileUtils jsonFileUtils = mock(JsonFileUtils.class);
        Set<DefaultMetadata> metadataSet = new HashSet<>();
        DefaultMetadata policyMetadata =  new DefaultMetadata();
        policyMetadata.setName("TestPolicy");
        policyMetadata.setType(EntityTypes.POLICY_TYPE);
        policyMetadata.setId("testId");
        policyMetadata.setGuid("testGuid");
        metadataSet.add(policyMetadata);
        BundleDefinedEntities bundleDefinedEntities = new BundleDefinedEntities();
        bundleDefinedEntities.setDefinedEntities(metadataSet);
        when(jsonFileUtils.readBundleMetadataFile(any(File.class))).thenReturn(bundleDefinedEntities);
        EntityBundleLoader loader = new EntityBundleLoader(documentTools, registry, jsonFileUtils);
        Bundle bundle = loader.loadMetadata(mock(File.class), BundleLoadingOperation.EXPORT);
        Map<String, Policy> policyMap = bundle.getPolicies();
        assertNotNull(policyMap.get("TestPolicy"));
    }

    @Test
    void tryLoadParseException() throws DocumentParseException {
        when(documentTools.parse(any(File.class))).thenThrow(DocumentParseException.class);
        assertThrows(BundleLoadException.class, () -> new EntityBundleLoader(documentTools, registry, JsonFileUtils.INSTANCE).load(mock(File.class), BundleLoadingOperation.EXPORT));
    }

}