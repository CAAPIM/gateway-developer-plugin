/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayconfig.bundle;

import com.ca.apim.gateway.cagatewayconfig.beans.Bundle;
import com.ca.apim.gateway.cagatewayconfig.bundle.loader.CassandraConnectionsLoader;
import com.ca.apim.gateway.cagatewayconfig.bundle.loader.DependencyBundleLoadException;
import com.ca.apim.gateway.cagatewayconfig.bundle.loader.DependencyLoaderRegistry;
import com.ca.apim.gateway.cagatewayconfig.bundle.loader.JdbcConnectionLoader;
import com.ca.apim.gateway.cagatewayconfig.util.TestUtils;
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
import java.util.List;
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
class DependencyBundleLoaderTest {

    @Mock
    private DocumentTools documentTools;
    private DependencyLoaderRegistry registry;

    @BeforeEach
    void before() {
        this.registry = new DependencyLoaderRegistry(
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

        DependencyBundleLoader loader = new DependencyBundleLoader(documentTools, registry);
        final Bundle bundle = loader.load(mock(File.class));

        assertNotNull(bundle);
        assertFalse(bundle.getCassandraConnections().isEmpty());
        assertFalse(bundle.getJdbcConnections().isEmpty());
        assertEquals(1, bundle.getCassandraConnections().size());
        assertEquals(1, bundle.getJdbcConnections().size());
    }

    @Test
    void tryLoadParseException() throws DocumentParseException {
        when(documentTools.parse(any(File.class))).thenThrow(DocumentParseException.class);
        assertThrows(DependencyBundleLoadException.class, () -> new DependencyBundleLoader(documentTools, registry).load(mock(File.class)));
    }

}