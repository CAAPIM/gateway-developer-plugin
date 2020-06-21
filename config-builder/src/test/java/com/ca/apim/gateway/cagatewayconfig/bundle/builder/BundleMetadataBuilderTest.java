/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayconfig.bundle.builder;

import com.ca.apim.gateway.cagatewayconfig.BundleFileBuilder;
import com.ca.apim.gateway.cagatewayconfig.ProjectInfo;
import com.ca.apim.gateway.cagatewayconfig.beans.*;
import com.ca.apim.gateway.cagatewayconfig.config.loader.EntityLoader;
import com.ca.apim.gateway.cagatewayconfig.config.loader.EntityLoaderRegistry;
import com.ca.apim.gateway.cagatewayconfig.environment.BundleCache;
import com.ca.apim.gateway.cagatewayconfig.util.entity.AnnotationConstants;
import com.ca.apim.gateway.cagatewayconfig.util.entity.EntityTypes;
import com.ca.apim.gateway.cagatewayconfig.util.file.DocumentFileUtils;
import com.ca.apim.gateway.cagatewayconfig.util.file.JsonFileUtils;
import com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentTools;
import com.fasterxml.jackson.core.JsonProcessingException;
import io.github.glytching.junit.extension.folder.TemporaryFolder;
import io.github.glytching.junit.extension.folder.TemporaryFolderExtension;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.testcontainers.shaded.com.google.common.collect.ImmutableMap;

import java.io.File;
import java.util.*;

import static com.ca.apim.gateway.cagatewayconfig.bundle.builder.BuilderConstants.BUNDLE_TYPE_ALL;
import static com.ca.apim.gateway.cagatewayconfig.bundle.builder.BundleEntityBuilderTestHelper.*;
import static com.ca.apim.gateway.cagatewayconfig.util.file.DocumentFileUtils.*;
import static com.ca.apim.gateway.cagatewayconfig.util.file.JsonFileUtils.METADATA_FILE_NAME_SUFFIX;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith({TemporaryFolderExtension.class, MockitoExtension.class})
public class BundleMetadataBuilderTest {

    @Mock
    EntityLoaderRegistry entityLoaderRegistry;
    @Mock
    BundleCache bundleCache;

    private static final ProjectInfo projectInfo = new ProjectInfo("my-bundle", "my-bundle-group", "1.0");

    @Test
    public void testAnnotatedEncassBundleFileNames(final TemporaryFolder temporaryFolder) {
        BundleEntityBuilder builder = createBundleEntityBuilder();

        Bundle bundle = createBundle(BASIC_ENCASS_POLICY, false, false, false);
        Encass encass = buildTestEncassWithAnnotation(TEST_GUID, TEST_ENCASS_POLICY, false);
        bundle.putAllEncasses(ImmutableMap.of(TEST_ENCASS, encass));

        when(entityLoaderRegistry.getEntityLoaders()).thenReturn(Collections.singleton(new TestBundleLoader(bundle)));

        List<DependentBundle> dummyList = new ArrayList<>();
        dummyList.add(new DependentBundle(new File("test")));

        BundleFileBuilder bundleFileBuilder = new BundleFileBuilder(DocumentTools.INSTANCE, DocumentFileUtils.INSTANCE,
                JsonFileUtils.INSTANCE, entityLoaderRegistry, builder, bundleCache);

        File bundleOutput = temporaryFolder.createDirectory("output");
        try {
            bundleFileBuilder.buildBundle(temporaryFolder.getRoot(), bundleOutput, dummyList, projectInfo);

            assertTrue(bundleOutput.exists());
            assertEquals(3, bundleOutput.listFiles().length);
            for (File generatedFile : bundleOutput.listFiles()) {

                if (StringUtils.endsWith(generatedFile.getName(), DELETE_BUNDLE_EXTENSION)) {
                    assertEquals(TEST_ENCASS_ANNOTATION_NAME + "-1.0-policy" + DELETE_BUNDLE_EXTENSION,
                            generatedFile.getName());
                } else if (StringUtils.endsWith(generatedFile.getName(), INSTALL_BUNDLE_EXTENSION)) {
                    assertEquals(TEST_ENCASS_ANNOTATION_NAME + "-1.0-policy" + INSTALL_BUNDLE_EXTENSION,
                            generatedFile.getName());
                } else {
                    assertEquals(TEST_ENCASS_ANNOTATION_NAME + "-1.0" + METADATA_FILE_NAME_SUFFIX,
                            generatedFile.getName());
                }
            }
        } finally {
            deleteDirectory(bundleOutput);
        }

        bundle.getEncasses().clear();
        // Remove "name" attribute from the @bundle annotation.
        encass.getAnnotations().parallelStream()
                .filter(ann -> AnnotationConstants.ANNOTATION_TYPE_BUNDLE.equals(ann.getType()))
                .findFirst().get().setName(null);
        bundle.putAllEncasses(ImmutableMap.of(TEST_ENCASS, encass));
        encass.setAnnotatedEntity(null);
        when(entityLoaderRegistry.getEntityLoaders()).thenReturn(Collections.singleton(new TestBundleLoader(bundle)));

        bundleOutput = temporaryFolder.createDirectory("output");
        try {
            bundleFileBuilder.buildBundle(temporaryFolder.getRoot(), bundleOutput, dummyList, projectInfo);

            bundleOutput = new File(temporaryFolder.getRoot(), "output");
            assertTrue(bundleOutput.exists());
            assertEquals(3, bundleOutput.listFiles().length);
            for (File generatedFile : bundleOutput.listFiles()) {
                if (StringUtils.endsWith(generatedFile.getName(), DELETE_BUNDLE_EXTENSION)) {
                    assertEquals("my-bundle-" + encass.getName() + "-1.0-policy" + DELETE_BUNDLE_EXTENSION,
                            generatedFile.getName());
                } else if (StringUtils.endsWith(generatedFile.getName(), INSTALL_BUNDLE_EXTENSION)) {
                    assertEquals("my-bundle-" + encass.getName() + "-1.0-policy" + INSTALL_BUNDLE_EXTENSION,
                            generatedFile.getName());
                } else {
                    assertEquals("my-bundle-" + encass.getName() + "-1.0" + METADATA_FILE_NAME_SUFFIX,
                            generatedFile.getName());
                }
            }
        } finally {
            deleteDirectory(bundleOutput);
        }


        // Check filenames if projectVersion is not provided
        bundleOutput = temporaryFolder.createDirectory("output");
        try {
            ProjectInfo projectInfoBlankVersion = new ProjectInfo("my-bundle", "my-group", null);
            bundleFileBuilder.buildBundle(temporaryFolder.getRoot(), bundleOutput, dummyList, projectInfoBlankVersion);

            bundleOutput = new File(temporaryFolder.getRoot(), "output");
            assertTrue(bundleOutput.exists());
            assertEquals(3, bundleOutput.listFiles().length);
            for (File generatedFile : bundleOutput.listFiles()) {
                if (StringUtils.endsWith(generatedFile.getName(), DELETE_BUNDLE_EXTENSION)) {
                    assertEquals("my-bundle-" + encass.getName() + "-policy" + DELETE_BUNDLE_EXTENSION,
                            generatedFile.getName());
                } else if (StringUtils.endsWith(generatedFile.getName(), INSTALL_BUNDLE_EXTENSION)) {
                    assertEquals("my-bundle-" + encass.getName() + "-policy" + INSTALL_BUNDLE_EXTENSION,
                            generatedFile.getName());
                } else {
                    assertEquals("my-bundle-" + encass.getName() + METADATA_FILE_NAME_SUFFIX,
                            generatedFile.getName());
                }
            }
        } finally {
            deleteDirectory(bundleOutput);
        }
    }

    @Test
    public void testAnnotatedEncassMetadata() throws JsonProcessingException {
        BundleEntityBuilder builder = createBundleEntityBuilder();

        Bundle bundle = createBundle(ENCASS_POLICY_WITH_ENV_DEPENDENCIES, true, true, false);
        Encass encass = buildTestEncassWithAnnotation(TEST_GUID, TEST_ENCASS_POLICY, false);
        String metadataId = ID_GENERATOR.generate();
        encass.getAnnotations().stream()
                .filter(a -> AnnotationConstants.ANNOTATION_TYPE_BUNDLE.equals(a.getType()))
                .findFirst().get().setId(metadataId);
        bundle.putAllEncasses(ImmutableMap.of(TEST_ENCASS, encass));

        Map<String, BundleArtifacts> bundles = builder.build(bundle, EntityBuilder.BundleType.DEPLOYMENT,
                DocumentTools.INSTANCE.getDocumentBuilder().newDocument(), projectInfo);
        assertNotNull(bundles);
        assertEquals(1, bundles.size());
        BundleMetadata metadata = bundles.get(TEST_ENCASS_ANNOTATION_NAME + "-1.0").getBundleMetadata();
        assertNotNull(metadata);
        assertEquals(metadataId, metadata.getId());
        assertEquals(TEST_ENCASS_ANNOTATION_NAME, metadata.getName());
        assertEquals(TEST_ENCASS_ANNOTATION_DESC, metadata.getDescription());
        assertEquals(TEST_ENCASS_ANNOTATION_TAGS, metadata.getTags());
        assertTrue(metadata.isPortalTemplate());

        verifyAnnotatedEncassBundleMetadata(bundles, bundle, encass, false, false, true);
    }

    /**
     * Test annotated encass metadata which contain only type of annotation is annotation details. For example,
     * only "@bundle" without name, description and tags
     */
    @Test
    public void testAnnotatedEncassMetadata_ExcludingOptionalAnnotationFields() throws JsonProcessingException {
                BundleEntityBuilder builder = createBundleEntityBuilder();

        Bundle bundle = createBundle(ENCASS_POLICY_WITH_ENV_DEPENDENCIES, true, true, true);
        Encass encass = buildTestEncassWithAnnotation(TEST_GUID, TEST_ENCASS_POLICY, true);
        encass.getAnnotations().forEach(a -> {
            a.setId(null);
            a.setName(null);
            a.setDescription(null);
            a.setTags(Collections.emptySet());
        });
        bundle.putAllEncasses(ImmutableMap.of(TEST_ENCASS, encass));

        Map<String, BundleArtifacts> bundles = builder.build(bundle, EntityBuilder.BundleType.DEPLOYMENT,
                DocumentTools.INSTANCE.getDocumentBuilder().newDocument(), projectInfo);
        assertNotNull(bundles);
        assertEquals(1, bundles.size());
        BundleMetadata metadata = bundles.get("my-bundle-" + encass.getName() + "-1.0").getBundleMetadata();
        assertNotNull(metadata);
        assertNotNull(metadata.getId());
        assertNotSame(metadata.getId(), metadata.getDefinedEntities().iterator().next().getId());
        assertEquals("my-bundle-" + encass.getName(), metadata.getName());
        assertEquals(encass.getProperties().get("description"), metadata.getDescription());
        assertEquals(0, metadata.getTags().size());

        verifyAnnotatedEncassBundleMetadata(bundles, bundle, encass, true, true, true);
    }

    @Test
    public void testHasRoutingInMetadata() {
        BundleEntityBuilder builder = createBundleEntityBuilder();

        Bundle bundle = createBundle(BASIC_ENCASS_POLICY, false, false, false);
        Encass encass = buildTestEncassWithAnnotation(TEST_GUID, TEST_ENCASS_POLICY, true);
        bundle.putAllEncasses(ImmutableMap.of(TEST_ENCASS, encass));

        Map<String, BundleArtifacts> bundles = builder.build(bundle, EntityBuilder.BundleType.DEPLOYMENT,
                DocumentTools.INSTANCE.getDocumentBuilder().newDocument(), new ProjectInfo("my-bundle", "my-bundle-group", "1.0"));
        assertNotNull(bundles);
        assertEquals(1, bundles.size());
        BundleMetadata metadata = bundles.get(TEST_ENCASS_ANNOTATION_NAME + "-1.0").getBundleMetadata();
        assertNotNull(metadata);
        assertEquals(TEST_ENCASS_ANNOTATION_NAME, metadata.getName());
        assertFalse(metadata.isHasRouting());
    }

    @Test
    public void testUnAnnotatedBundleMetadata() throws JsonProcessingException {
        BundleEntityBuilder builder = createBundleEntityBuilder();
        // create un-annotated bundle
        Bundle bundle = createBundle(ENCASS_POLICY_WITH_ENV_DEPENDENCIES, true, false, false);
        // create encass with the empty set of annotations and add it to the bundle
        Encass encass = buildTestEncassWithAnnotation(TEST_ENCASS, TEST_ENCASS_ID, TEST_GUID, TEST_ENCASS_POLICY, Collections.emptySet());
        bundle.putAllEncasses(ImmutableMap.of(TEST_ENCASS, encass));

        Map<String, BundleArtifacts> bundles = builder.build(bundle, EntityBuilder.BundleType.DEPLOYMENT,
                DocumentTools.INSTANCE.getDocumentBuilder().newDocument(), projectInfo);
        assertNotNull(bundles);
        assertEquals(1, bundles.size());
        BundleMetadata metadata = bundles.get(projectInfo.getName() + "-1.0").getBundleMetadata();
        assertNotNull(metadata);
        assertEquals(projectInfo.getName(), metadata.getName());
        assertEquals(projectInfo.getGroupName(), metadata.getGroupName());
        assertEquals(BUNDLE_TYPE_ALL, metadata.getType());
        assertEquals(StringUtils.EMPTY, metadata.getDescription());
        assertEquals(Collections.emptyList(), metadata.getTags());
        assertTrue(metadata.isReusable());
        assertTrue(metadata.isRedeployable());
        assertFalse(metadata.isPortalTemplate());

        Collection<Metadata> definedEntities = metadata.getDefinedEntities();
        assertEquals(2, definedEntities.size());
        definedEntities.stream().forEach(definedEntityData -> {
            switch (definedEntityData.getType()) {
                case EntityTypes.ENCAPSULATED_ASSERTION_TYPE:
                    assertEquals(TEST_ENCASS, definedEntityData.getName());
                    assertEquals(TEST_ENCASS_ID, definedEntityData.getId());
                    assertEquals(TEST_GUID, definedEntityData.getGuid());
                    break;
                case EntityTypes.POLICY_TYPE:
                    assertEquals(TEST_ENCASS_POLICY, definedEntityData.getName());
                    assertEquals(TEST_POLICY_ID, definedEntityData.getId());
                    assertEquals(TEST_GUID, definedEntityData.getGuid());
                    break;
                default:
                    break;
            }
        });
    }

    private void deleteDirectory(File directory) {
        Arrays.stream(directory.listFiles()).forEach(f -> f.delete());
        directory.delete();
    }

    static class TestBundleLoader implements EntityLoader {
        private final Bundle bundle;

        TestBundleLoader(Bundle bundle) {
            this.bundle = bundle;
        }

        @Override
        public void load(Bundle bundle, File rootDir) {
            bundle.setDependencyMap(this.bundle.getDependencyMap());
            bundle.setDependencies(this.bundle.getDependencies());
            bundle.setFolderTree(this.bundle.getFolderTree());
            bundle.setLoadingMode(bundle.getLoadingMode());
            bundle.putAllFolders(this.bundle.getFolders());
            bundle.putAllPolicies(this.bundle.getPolicies());
            bundle.putAllServices(this.bundle.getServices());
            bundle.putAllEncasses(this.bundle.getEncasses());
            bundle.putAllTrustedCerts(this.bundle.getTrustedCerts());
            bundle.putAllJdbcConnections(this.bundle.getJdbcConnections());
        }

        @Override
        public void load(Bundle bundle, String name, String value) {

        }

        @Override
        public Object loadSingle(String name, File entitiesFile) {
            return null;
        }

        @Override
        public String getEntityType() {
            return null;
        }
    }
}
