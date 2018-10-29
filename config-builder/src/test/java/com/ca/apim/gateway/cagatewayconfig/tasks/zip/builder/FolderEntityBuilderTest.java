/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayconfig.tasks.zip.builder;

import com.ca.apim.gateway.cagatewayconfig.tasks.zip.beans.Bundle;
import com.ca.apim.gateway.cagatewayconfig.tasks.zip.beans.Folder;
import com.ca.apim.gateway.cagatewayconfig.tasks.zip.builder.EntityBuilder.BundleType;
import com.ca.apim.gateway.cagatewayconfig.util.IdGenerator;
import com.ca.apim.gateway.cagatewayconfig.util.entity.EntityTypes;
import com.ca.apim.gateway.cagatewayconfig.util.string.EncodeDecodeUtils;
import com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentTools;
import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Element;

import java.util.List;
import java.util.Map;

import static com.ca.apim.gateway.cagatewayconfig.tasks.zip.beans.Folder.ROOT_FOLDER_NAME;
import static com.ca.apim.gateway.cagatewayconfig.tasks.zip.builder.EntityBuilder.BundleType.DEPLOYMENT;
import static com.ca.apim.gateway.cagatewayconfig.tasks.zip.builder.EntityBuilder.BundleType.ENVIRONMENT;
import static com.ca.apim.gateway.cagatewayconfig.util.TestUtils.createFolder;
import static com.ca.apim.gateway.cagatewayconfig.util.TestUtils.createRoot;
import static com.ca.apim.gateway.cagatewayconfig.util.gateway.BundleElementNames.*;
import static com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentUtils.getSingleChildElement;
import static java.util.Optional.ofNullable;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;
import static org.apache.commons.lang.StringUtils.EMPTY;
import static org.apache.commons.lang.StringUtils.trimToNull;
import static org.junit.jupiter.api.Assertions.*;

class FolderEntityBuilderTest {

    private static final IdGenerator ID_GENERATOR = new IdGenerator();
    private static final String FOLDER_1 = "Folder1";
    private static final String FOLDER_2 = "Folder2";
    private static final String FOLDER_3 = "Fo-_¯-¯_-lder3";

    @Test
    void buildFromEmptyBundle_noFolders() {
        FolderEntityBuilder builder = new FolderEntityBuilder(ID_GENERATOR);
        final List<Entity> entities = builder.build(new Bundle(), DEPLOYMENT, DocumentTools.INSTANCE.getDocumentBuilder().newDocument());

        assertTrue(entities.isEmpty());
    }

    @Test
    void buildMissingRoot() {
        FolderEntityBuilder builder = new FolderEntityBuilder(ID_GENERATOR);
        Bundle bundle = new Bundle();
        bundle.putAllFolders(createTestFolders(null));

        assertThrows(EntityBuilderException.class, () -> builder.build(bundle, DEPLOYMENT, DocumentTools.INSTANCE.getDocumentBuilder().newDocument()));
    }

    @Test
    void buildEnvironment() {
        build(ENVIRONMENT);
    }

    @Test
    void buildDeployment() {
        build(DEPLOYMENT);
    }

    private static void build(BundleType bundleType) {
        FolderEntityBuilder builder = new FolderEntityBuilder(ID_GENERATOR);
        Bundle bundle = new Bundle();
        Folder root = createRoot();
        bundle.getFolders().put(EMPTY, root);
        bundle.putAllFolders(createTestFolders(root));

        final List<Entity> entities = builder.build(bundle, bundleType, DocumentTools.INSTANCE.getDocumentBuilder().newDocument());

        assertNotNull(entities);
        assertFalse(entities.isEmpty());
        assertEquals(4, entities.size());

        final Map<String, Entity> entitiesMap = entities.stream().collect(toMap(Entity::getName, identity()));
        entitiesMap.forEach((k, entity) -> {
            k = ROOT_FOLDER_NAME.equals(k) ? EMPTY : k;

            Folder folder = bundle.getFolders().get(EncodeDecodeUtils.encodePath(k));
            assertNotNull(folder);
            assertEquals(EncodeDecodeUtils.decodePath(folder.getName()), entity.getName());
            assertNotNull(entity.getId());
            assertNotNull(entity.getXml());
            assertEquals(EntityTypes.FOLDER_TYPE, entity.getType());

            Element xml = entity.getXml();
            assertEquals(FOLDER, xml.getTagName());
            assertNotNull(getSingleChildElement(xml, NAME));
            assertEquals(folder.getId(), trimToNull(xml.getAttribute(ATTRIBUTE_ID)));
            assertEquals(ofNullable(folder.getParentFolder()).orElse(new Folder()).getId(), trimToNull(xml.getAttribute(ATTRIBUTE_FOLDER_ID)));
        });

    }

    private static Map<String, Folder> createTestFolders(Folder root) {
        Folder folder1 = createFolder(FOLDER_1, FOLDER_1, root);
        Folder folder2 = createFolder(FOLDER_2, FOLDER_2, root);
        Folder folder3 = createFolder(FOLDER_3, FOLDER_3, folder2);

        return ImmutableMap.of(FOLDER_1, folder1, FOLDER_2, folder2, FOLDER_3, folder3);
    }

}